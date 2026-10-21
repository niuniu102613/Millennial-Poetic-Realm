package com.millennial.member4.mr.job5;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Emily Job5 朝代×意象统计 驱动类
 * 运行参数：输入路径 输出路径
 */
public class ImageryByDynastyDriver {

    public static void main(String[] args) throws Exception {
        // 参数校验
        if (args.length != 2) {
            System.err.println("Usage: ImageryByDynastyDriver <input-path> <output-path>");
            System.exit(1);
        }

        // 初始化配置与Job
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Member4_Job5_ImageryByDynasty");
        job.setJarByClass(ImageryByDynastyDriver.class);

        // 指定Mapper和Reducer
        job.setMapperClass(ImageryByDynastyMapper.class);
        job.setReducerClass(ImageryByDynastyReducer.class);

        // Map输出类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        // 最终输出类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // 输入输出路径
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // 提交任务
        boolean success = job.waitForCompletion(true);
        System.exit(success ? 0 : 1);
    }
}
