package com.poetry.mr.job1;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class CharFreqReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    private final IntWritable outVal = new IntWritable();

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
        int sum = 0;
        for (IntWritable v : values) {
            sum += v.get();
        }
        outVal.set(sum);
        context.write(key, outVal);
    }
}
