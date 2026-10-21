package com.millennial.member4.mr.job3;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Emily Job3 意象统计 驱动类
 * 运行参数：输入路径 输出路径
 */
public class ImageryCountDriver {

    public static void main(String[] args) throws Exception {
        // 1. 参数校验
        if (args.length != 2) {
            System.err.println("Usage: ImageryCountDriver <input-path> <output-path>");
            System.exit(1);
        }

        // 2. 初始化配置与Job
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Member4_Job3_ImageryCount");

        // 3. 指定主类
        job.setJarByClass(ImageryCountDriver.class);

        // 4. 指定Mapper和Reducer
        job.setMapperClass(ImageryCountMapper.class);
        job.setReducerClass(ImageryCountReducer.class);

        // 5. Map输出类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        // 6. 最终输出类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // 7. 设置输入输出路径
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // 8. 提交任务
        boolean success = job.waitForCompletion(true);
        System.exit(success ? 0 : 1);
    }
}
