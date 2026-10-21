package com.millennial.member4.mr.job5;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Emily Job5 朝代×意象统计 Reducer
 * 输入：<朝代_意象名, [1,1,1,...]>
 * 输出：<朝代_意象名, 出现总首数>
 */
public class ImageryByDynastyReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    private final IntWritable outValue = new IntWritable();

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        int sum = 0;
        for (IntWritable val : values) {
            sum += val.get();
        }
        outValue.set(sum);
        context.write(key, outValue);
    }
}
