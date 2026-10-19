package com.poetry.mr.job2;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordFreqDriver {
    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.err.println("Usage: CharFreqDriver <inputPath> <outputPath>");
            System.exit(2);
        }
        System.out.println("args.length = " + args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.println("  args[" + i + "] = " + args[i]);
        }
        System.out.println("INPUT  = " + args[0]);
        System.out.println("OUTPUT = " + args[1]);


        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "High Frequency Word Stats");

        job.setJarByClass(WordFreqDriver.class);
        job.setMapperClass(WordFreqMapper.class);
        job.setReducerClass(WordFreqReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
