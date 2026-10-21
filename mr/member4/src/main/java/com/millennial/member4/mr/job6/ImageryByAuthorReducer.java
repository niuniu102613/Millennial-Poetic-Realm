package com.millennial.member4.mr.job6;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Emily Job6 作者×意象统计 Reducer
 * 输入：<作者名_意象名, [1,1,1,...]>
 * 输出：<作者名_意象名, 出现总首数>
 */
public class ImageryByAuthorReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

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
