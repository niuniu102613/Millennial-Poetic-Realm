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
 * Job 3: 朝代 × 类别 统计
 * 输出格式：dynasty \t category \t count
 * 这样前端按朝代分组、按类别对比更方便
 */
public class DynastyCategoryCount {

    public static class DynastyCategoryMapper extends Mapper<Object, Text, Text, IntWritable> {
        private final static IntWritable one = new IntWritable(1);
        private Text dynastyCategory = new Text();

        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            if (line.startsWith("poem_id") || line.isEmpty()) {
                return;
            }
            String[] fields = line.split(",");
            if (fields.length >= 8) {
                // dynasty 是第4列（索引3）
                String dynasty = fields[3].trim();
                // category 是倒数第3列
                String category = fields[fields.length - 3].trim();
                if (!dynasty.isEmpty() && !category.isEmpty()) {
                    // 用制表符分隔，方便后续解析
                    dynastyCategory.set(dynasty + "\t" + category);
                    context.write(dynastyCategory, one);
                }
            }
        }
    }

    public static class DynastyCategoryReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
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
            System.err.println("Usage: DynastyCategoryCount <input> <output>");
            System.exit(2);
        }
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "DynastyCategoryCount");
        job.setJarByClass(DynastyCategoryCount.class);

        job.setMapperClass(DynastyCategoryMapper.class);
        job.setCombinerClass(DynastyCategoryReducer.class);
        job.setReducerClass(DynastyCategoryReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
