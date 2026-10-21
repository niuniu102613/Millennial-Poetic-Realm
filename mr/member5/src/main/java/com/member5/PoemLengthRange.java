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
 * Job 4: 诗词长度区间统计
 * 直接使用数据中的 content_len 字段（最后一列），保证口径一致
 * 区间划分：0-20, 21-50, 51-100, 101-200, 200+
 * 输出格式：range \t count
 */
public class PoemLengthRange {

    public static class LengthRangeMapper extends Mapper<Object, Text, Text, IntWritable> {
        private final static IntWritable one = new IntWritable(1);
        private Text range = new Text();

        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            if (line.startsWith("poem_id") || line.isEmpty()) {
                return;
            }
            String[] fields = line.split(",");
            if (fields.length >= 8) {
                // 最后一列就是 content_len，直接用
                String lenStr = fields[fields.length - 1].trim();
                int length;
                try {
                    length = Integer.parseInt(lenStr);
                } catch (NumberFormatException e) {
                    return; // 长度非法就跳过
                }

                // 划分区间
                String rangeLabel;
                if (length <= 20) {
                    rangeLabel = "0-20";
                } else if (length <= 50) {
                    rangeLabel = "21-50";
                } else if (length <= 100) {
                    rangeLabel = "51-100";
                } else if (length <= 200) {
                    rangeLabel = "101-200";
                } else {
                    rangeLabel = "200+";
                }

                range.set(rangeLabel);
                context.write(range, one);
            }
        }
    }

    public static class LengthRangeReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
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
            System.err.println("Usage: PoemLengthRange <input> <output>");
            System.exit(2);
        }
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "PoemLengthRange");
        job.setJarByClass(PoemLengthRange.class);

        job.setMapperClass(LengthRangeMapper.class);
        job.setCombinerClass(LengthRangeReducer.class);
        job.setReducerClass(LengthRangeReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
