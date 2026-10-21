
package com.poetry.dynasty;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class DynastyAuthorCountJob {

    // 统计空字段和无效记录，方便检查数据质量
    enum DataCounter {
        HEADER_RECORDS,
        INVALID_RECORDS,
        EMPTY_AUTHOR,
        EMPTY_DYNASTY
    }

    /**
     * 第一阶段 Mapper：
     * 从 CSV 中提取 dynasty 和 author，
     * 输出 dynasty -> author。
     */
    public static class DistinctAuthorMapper
            extends Mapper<LongWritable, Text, Text, Text> {

        private final Text dynastyKey = new Text();
        private final Text authorValue = new Text();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString();

            if (line.trim().isEmpty()) {
                return;
            }

            // 跳过 CSV 表头
            if (key.get() == 0 && line.startsWith("poem_id,")) {
                context.getCounter(DataCounter.HEADER_RECORDS).increment(1);
                return;
            }

            String[] fields = parseCsvLine(line);

            // 字段顺序：poem_id,title,author,dynasty,content,...
            if (fields.length < 8) {
                context.getCounter(DataCounter.INVALID_RECORDS).increment(1);
                return;
            }

            String author = fields[2].trim();
            String dynasty = fields[3].trim();

            if (author.isEmpty()) {
                context.getCounter(DataCounter.EMPTY_AUTHOR).increment(1);
                return;
            }

            if (dynasty.isEmpty()) {
                context.getCounter(DataCounter.EMPTY_DYNASTY).increment(1);
                return;
            }

            dynastyKey.set(dynasty);
            authorValue.set(author);

            context.write(dynastyKey, authorValue);
        }
    }

    /**
     * 第一阶段 Reducer：
     * 在每个朝代内部对作者去重。
     * 输出 dynasty -> 每个不同的 author。
     */
    public static class DistinctAuthorReducer
            extends Reducer<Text, Text, Text, Text> {

        @Override
        protected void reduce(Text dynasty, Iterable<Text> authors,
                              Context context)
                throws IOException, InterruptedException {

            Set<String> distinctAuthors = new HashSet<>();

            for (Text author : authors) {
                distinctAuthors.add(author.toString());
            }

            Text authorValue = new Text();

            for (String author : distinctAuthors) {
                authorValue.set(author);
                context.write(dynasty, authorValue);
            }
        }
    }

    /**
     * 第二阶段 Mapper：
     * 读取第一阶段输出的 dynasty \t author，
     * 将每个去重后的作者转换成 1。
     */
    public static class AuthorCountMapper
            extends Mapper<LongWritable, Text, Text, IntWritable> {

        private final Text dynastyKey = new Text();
        private static final IntWritable ONE = new IntWritable(1);

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString();
            int separator = line.indexOf('\t');

            if (separator <= 0 || separator == line.length() - 1) {
                return;
            }

            String dynasty = line.substring(0, separator).trim();
            String author = line.substring(separator + 1).trim();

            if (dynasty.isEmpty() || author.isEmpty()) {
                return;
            }

            dynastyKey.set(dynasty);
            context.write(dynastyKey, ONE);
        }
    }

    /**
     * 第二阶段 Reducer：
     * 汇总各朝代的不同作者数量。
     */
    public static class AuthorCountReducer
            extends Reducer<Text, IntWritable, Text, IntWritable> {

        private final IntWritable result = new IntWritable();

        @Override
        protected void reduce(Text dynasty, Iterable<IntWritable> values,
                              Context context)
                throws IOException, InterruptedException {

            int count = 0;

            for (IntWritable value : values) {
                count += value.get();
            }

            result.set(count);
            context.write(dynasty, result);
        }
    }

    /**
     * 简单 CSV 行解析器：
     * 支持引号包裹字段、字段内逗号以及双引号转义。
     * 不支持带引号字段内的真实换行符。
     */
    private static String[] parseCsvLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }

        fields.add(field.toString());
        return fields.toArray(new String[0]);
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.err.println(
                    "Usage: DynastyAuthorCountJob <input> <output>");
            System.exit(2);
        }

        Path inputPath = new Path(args[0]);
        Path outputPath = new Path(args[1]);
        Path tempPath = new Path(args[1] + "_distinct_authors");

        Configuration conf = new Configuration();

        FileSystem fs = outputPath.getFileSystem(conf);

        if (fs.exists(outputPath)) {
            System.err.println("Output path already exists: " + outputPath);
            System.exit(2);
        }

        if (fs.exists(tempPath)) {
            System.err.println("Temporary path already exists: " + tempPath);
            System.err.println("Please remove it after confirming it is safe.");
            System.exit(2);
        }

        // Job 1：按朝代去重作者
        Job distinctJob = Job.getInstance(conf,
                "Poetry Dynasty Distinct Authors");

        distinctJob.setJarByClass(DynastyAuthorCountJob.class);
        distinctJob.setMapperClass(DistinctAuthorMapper.class);
        distinctJob.setReducerClass(DistinctAuthorReducer.class);

        distinctJob.setMapOutputKeyClass(Text.class);
        distinctJob.setMapOutputValueClass(Text.class);
        distinctJob.setOutputKeyClass(Text.class);
        distinctJob.setOutputValueClass(Text.class);

        distinctJob.setInputFormatClass(TextInputFormat.class);
        distinctJob.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(distinctJob, inputPath);
        FileOutputFormat.setOutputPath(distinctJob, tempPath);

        if (!distinctJob.waitForCompletion(true)) {
            System.err.println("Distinct-author job failed.");
            System.exit(1);
        }

        // Job 2：统计各朝代去重后的作者数量
        Job countJob = Job.getInstance(conf,
                "Poetry Dynasty Author Count");

        countJob.setJarByClass(DynastyAuthorCountJob.class);
        countJob.setMapperClass(AuthorCountMapper.class);
        countJob.setReducerClass(AuthorCountReducer.class);

        countJob.setMapOutputKeyClass(Text.class);
        countJob.setMapOutputValueClass(IntWritable.class);
        countJob.setOutputKeyClass(Text.class);
        countJob.setOutputValueClass(IntWritable.class);

        countJob.setInputFormatClass(TextInputFormat.class);
        countJob.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(countJob, tempPath);
        FileOutputFormat.setOutputPath(countJob, outputPath);

        boolean success = countJob.waitForCompletion(true);

        // 第二阶段结束后清理临时结果
        if (fs.exists(tempPath)) {
            fs.delete(tempPath, true);
        }

        System.exit(success ? 0 : 1);
    }
}
