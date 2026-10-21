
package com.poetry.dynasty;

import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

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

public class DynastyCountJob {

    /**
     * 输入：CSV 的一行
     * 输出：(朝代, 1)
     *
     * CSV 字段顺序：
     * poem_id,title,author,dynasty,content,category,source,content_len
     */
    public static class DynastyMapper
            extends Mapper<LongWritable, Text, Text, IntWritable> {

        private static final IntWritable ONE = new IntWritable(1);
        private final Text dynastyKey = new Text();

        @Override
        protected void map(
                LongWritable key,
                Text value,
                Context context
        ) throws IOException, InterruptedException {

            String line = value.toString();

            if (line == null || line.trim().isEmpty()) {
                return;
            }

            try (CSVParser parser = CSVParser.parse(
                    new StringReader(line),
                    CSVFormat.DEFAULT
            )) {
                for (CSVRecord record : parser) {

                    // 跳过 CSV 表头
                    if (record.size() > 0
                            && "poem_id".equalsIgnoreCase(
                            record.get(0).trim())) {
                        return;
                    }

                    // 字段数不足，无法读取 dynasty 字段
                    if (record.size() < 8) {
                        context.getCounter(
                                "Poetry",
                                "INVALID_RECORDS"
                        ).increment(1);
                        return;
                    }

                    // dynasty 是第 4 列，索引为 3
                    String dynasty = record.get(3).trim();

                    if (dynasty.isEmpty()) {
                        context.getCounter(
                                "Poetry",
                                "EMPTY_DYNASTY"
                        ).increment(1);
                        return;
                    }

                    dynastyKey.set(dynasty);
                    context.write(dynastyKey, ONE);
                }
            } catch (IllegalArgumentException e) {
                context.getCounter(
                        "Poetry",
                        "INVALID_CSV_RECORDS"
                ).increment(1);
            }
        }
    }

    /**
     * 合并同一个 Mapper 中的局部计数
     */
    public static class DynastyCombiner
            extends Reducer<Text, IntWritable, Text, IntWritable> {

        private final IntWritable result = new IntWritable();

        @Override
        protected void reduce(
                Text key,
                Iterable<IntWritable> values,
                Context context
        ) throws IOException, InterruptedException {

            int sum = 0;

            for (IntWritable value : values) {
                sum += value.get();
            }

            result.set(sum);
            context.write(key, result);
        }
    }

    /**
     * 汇总每个朝代的诗词数量
     */
    public static class DynastyReducer
            extends Reducer<Text, IntWritable, Text, IntWritable> {

        private final IntWritable result = new IntWritable();

        @Override
        protected void reduce(
                Text key,
                Iterable<IntWritable> values,
                Context context
        ) throws IOException, InterruptedException {

            int sum = 0;

            for (IntWritable value : values) {
                sum += value.get();
            }

            result.set(sum);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.err.println(
                    "Usage: DynastyCountJob <input_path> <output_path>"
            );
            System.exit(2);
        }

        Configuration conf = new Configuration();

        Job job = Job.getInstance(
                conf,
                "Poetry Dynasty Count"
        );

        job.setJarByClass(DynastyCountJob.class);

        job.setMapperClass(DynastyMapper.class);
        job.setCombinerClass(DynastyCombiner.class);
        job.setReducerClass(DynastyReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // 一个 Reducer，便于初期检查单个结果文件
        job.setNumReduceTasks(1);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
