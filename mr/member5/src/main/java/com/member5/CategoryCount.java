package com.member5;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Job 1: 各诗词类别数量统计
 * 输出格式：category \t count
 * 数据列：poem_id,title,author,dynasty,content,category,source,content_len
 *  category = fields[length-3]，source = fields[length-2]，content_len = fields[length-1]
 */
public class CategoryCount {

    public static class CategoryMapper extends Mapper<Object, Text, Text, IntWritable> {
        private final static IntWritable one = new IntWritable(1);
        private Text category = new Text();

        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            if (line.startsWith("poem_id") || line.isEmpty()) {
                return;
            }
            String[] fields = line.split(",");
            // 至少要有 8 个字段（poem_id,title,author,dynasty,content,category,source,content_len）
            // category 是倒数第3列（最后一列是content_len，倒数第2列是source）
            if (fields.length >= 8) {
                String cat = fields[fields.length - 3].trim();
                if (!cat.isEmpty()) {
                    category.set(cat);
                    context.write(category, one);
                }
            }
        }
    }

    public static class CategoryReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            context.write(key, new IntWritable(sum));
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: CategoryCount <input> <output>");
            System.exit(2);
        }
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "CategoryCount");
        job.setJarByClass(CategoryCount.class);

        job.setMapperClass(CategoryMapper.class);
        job.setCombinerClass(CategoryReducer.class);
        job.setReducerClass(CategoryReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
