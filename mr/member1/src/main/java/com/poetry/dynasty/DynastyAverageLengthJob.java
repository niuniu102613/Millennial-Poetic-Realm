
package com.poetry.dynasty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class DynastyAverageLengthJob {

    enum DataCounter {
        HEADER_RECORDS,
        INVALID_RECORDS,
        EMPTY_DYNASTY,
        INVALID_LENGTH
    }

    /**
     * 解析单行 CSV，支持带引号字段、字段内逗号和双引号转义。
     * 不支持带引号字段中的真实换行符。
     */
    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
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

    /**
     * Mapper:
     * 输出 key = dynasty，value = content_len
     */
    public static class AverageLengthMapper
            extends Mapper<LongWritable, Text, Text, LongWritable> {

        private final Text dynastyKey = new Text();
        private final LongWritable lengthValue = new LongWritable();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString();

            if (line.trim().isEmpty()) {
                return;
            }

            // CSV 表头只应出现在文件首行
            if (key.get() == 0 && line.startsWith("poem_id,")) {
                context.getCounter(DataCounter.HEADER_RECORDS).increment(1);
                return;
            }

            String[] fields = parseCsvLine(line);

            // 表头：poem_id,title,author,dynasty,content,category,source,content_len
            if (fields.length < 8) {
                context.getCounter(DataCounter.INVALID_RECORDS).increment(1);
                return;
            }

            String dynasty = fields[3].trim();
            String lengthText = fields[7].trim();

            if (dynasty.isEmpty()) {
                context.getCounter(DataCounter.EMPTY_DYNASTY).increment(1);
                return;
            }

            try {
                long length = Long.parseLong(lengthText);

                if (length < 0) {
                    context.getCounter(DataCounter.INVALID_LENGTH).increment(1);
                    return;
                }

                dynastyKey.set(dynasty);
                lengthValue.set(length);

                context.write(dynastyKey, lengthValue);

            } catch (NumberFormatException e) {
                context.getCounter(DataCounter.INVALID_LENGTH).increment(1);
            }
        }
    }

    /**
     * Reducer:
     * 计算每个朝代的总长度 / 有效诗词数量。
     */
    public static class AverageLengthReducer
            extends Reducer<Text, LongWritable, Text, DoubleWritable> {

        private final DoubleWritable result = new DoubleWritable();

        @Override
        protected void reduce(Text dynasty, Iterable<LongWritable> values,
                              Context context)
                throws IOException, InterruptedException {

            long totalLength = 0L;
            long poemCount = 0L;

            for (LongWritable value : values) {
                totalLength += value.get();
                poemCount++;
            }

            if (poemCount > 0) {
                double average = (double) totalLength / poemCount;
                result.set(average);
                context.write(dynasty, result);
            }
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.err.println(
                    "Usage: DynastyAverageLengthJob <input> <output>");
            System.exit(2);
        }

        Configuration conf = new Configuration();

        Path inputPath = new Path(args[0]);
        Path outputPath = new Path(args[1]);

        // 避免输出目录已存在导致 Hadoop 直接失败
        org.apache.hadoop.fs.FileSystem fs =
                outputPath.getFileSystem(conf);

        if (fs.exists(outputPath)) {
            System.err.println("Output path already exists: " + outputPath);
            System.exit(2);
        }

        Job job = Job.getInstance(conf,
                "Poetry Dynasty Average Length");

        job.setJarByClass(DynastyAverageLengthJob.class);

        job.setMapperClass(AverageLengthMapper.class);
        job.setReducerClass(AverageLengthReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, outputPath);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
