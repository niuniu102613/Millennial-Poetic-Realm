package com.millennial.member4.mr.job4;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Emily Job4 意象TopN 驱动类
 * 运行参数：输入路径 输出路径 [TopN数量]
 * 示例：data/test_data.csv output/job4_test 10
 */
public class ImageryTopNDriver {

    public static void main(String[] args) throws Exception {
        // 参数校验：至少2个，第3个为可选TopN
        if (args.length < 2) {
            System.err.println("Usage: ImageryTopNDriver <input-path> <output-path> [topN]");
            System.exit(1);
        }

        // 初始化配置
        Configuration conf = new Configuration();
        // 设置TopN数量，默认10
        if (args.length >= 3) {
            conf.setInt("imagery.top.n", Integer.parseInt(args[2]));
        } else {
            conf.setInt("imagery.top.n", 10);
        }

        Job job = Job.getInstance(conf, "Member4_Job4_ImageryTopN");
        job.setJarByClass(ImageryTopNDriver.class);

        // 指定Mapper和Reducer
        job.setMapperClass(ImageryTopNMapper.class);
        job.setReducerClass(ImageryTopNReducer.class);

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
