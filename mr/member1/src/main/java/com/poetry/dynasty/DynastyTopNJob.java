
package com.poetry.dynasty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class DynastyTopNJob {

    private static final String TOP_N_KEY = "poetry.dynasty.top.n";

    /**
     * Mapper:
     * 从 CSV 中读取 dynasty，输出 dynasty -> 1。
     */
    public static class DynastyMapper
            extends Mapper<LongWritable, Text, Text, IntWritable> {

        private static final IntWritable ONE = new IntWritable(1);
        private final Text dynastyKey = new Text();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString();

            if (line.trim().isEmpty()) {
                return;
            }

            // 跳过 CSV 表头
            if (key.get() == 0 && line.startsWith("poem_id,")) {
                return;
            }

            String[] fields = parseCsvLine(line);

            if (fields.length < 8) {
                return;
            }

            String dynasty = fields[3].trim();

            if (!dynasty.isEmpty()) {
                dynastyKey.set(dynasty);
                context.write(dynastyKey, ONE);
            }
        }
    }

    /**
     * Combiner:
     * 在 Map 端提前统计相同朝代的局部数量。
     */
    public static class SumCombiner
            extends Reducer<Text, IntWritable, Text, IntWritable> {

        private final IntWritable result = new IntWritable();

        @Override
        protected void reduce(Text dynasty, Iterable<IntWritable> values,
                              Context context)
                throws IOException, InterruptedException {

            int sum = 0;

            for (IntWritable value : values) {
                sum += value.get();
            }

            result.set(sum);
            context.write(dynasty, result);
        }
    }

    /**
     * Reducer:
     * 汇总全部朝代的数量，再选出 TopN。
     */
    public static class TopNReducer
            extends Reducer<Text, IntWritable, Text, IntWritable> {

        private final List<DynastyCount> allDynasties = new ArrayList<>();
        private int topN;

        @Override
        protected void setup(Context context) {
            topN = context.getConfiguration().getInt(TOP_N_KEY, 5);
        }

        @Override
        protected void reduce(Text dynasty, Iterable<IntWritable> values,
                              Context context) {

            int sum = 0;

            for (IntWritable value : values) {
                sum += value.get();
            }

            allDynasties.add(new DynastyCount(dynasty.toString(), sum));
        }

        @Override
        protected void cleanup(Context context)
                throws IOException, InterruptedException {

            // 数量从高到低；数量相同时按朝代名称排序
            allDynasties.sort(
                    Comparator.comparingInt(DynastyCount::getCount)
                            .reversed()
                            .thenComparing(DynastyCount::getDynasty)
            );

            int limit = Math.min(topN, allDynasties.size());

            for (int i = 0; i < limit; i++) {
                DynastyCount item = allDynasties.get(i);
                context.write(
                        new Text(item.getDynasty()),
                        new IntWritable(item.getCount())
                );
            }
        }
    }

    private static class DynastyCount {
        private final String dynasty;
        private final int count;

        DynastyCount(String dynasty, int count) {
            this.dynasty = dynasty;
            this.count = count;
        }

        String getDynasty() {
            return dynasty;
        }

        int getCount() {
            return count;
        }
    }

    /**
     * CSV 解析器：支持引号、字段内逗号、双引号转义。
     * 不支持带引号字段内的真实换行符。
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

    public static void main(String[] args) throws Exception {

        if (args.length < 2 || args.length > 3) {
            System.err.println(
                    "Usage: DynastyTopNJob <input> <output> [topN]");
            System.exit(2);
        }

        Configuration conf = new Configuration();

        int topN = 5;

        if (args.length == 3) {
            topN = Integer.parseInt(args[2]);
        }

        if (topN <= 0) {
            System.err.println("topN must be greater than 0.");
            System.exit(2);
        }

        conf.setInt(TOP_N_KEY, topN);

        Path inputPath = new Path(args[0]);
        Path outputPath = new Path(args[1]);

        org.apache.hadoop.fs.FileSystem fs =
                outputPath.getFileSystem(conf);

        if (fs.exists(outputPath)) {
            System.err.println("Output path already exists: " + outputPath);
            System.exit(2);
        }

        Job job = Job.getInstance(conf, "Poetry Dynasty TopN");

        job.setJarByClass(DynastyTopNJob.class);
        job.setMapperClass(DynastyMapper.class);
        job.setCombinerClass(SumCombiner.class);
        job.setReducerClass(TopNReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // TopN 必须集中到一个 Reducer 排序
        job.setNumReduceTasks(1);

        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, outputPath);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
