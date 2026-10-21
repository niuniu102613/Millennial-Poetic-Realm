package com.millennial.member4.mr.job4;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Emily Job4 意象TopN Reducer
 * 聚合所有意象次数后，内存排序输出TopN
 */
public class ImageryTopNReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    private final Map<String, Integer> countMap = new HashMap<>();
    private int topN;

    @Override
    protected void setup(Context context) {
        // 从配置中读取TopN数量，默认10
        topN = context.getConfiguration().getInt("imagery.top.n", 10);
    }

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context) {
        // 累加次数，存入内存Map
        int sum = 0;
        for (IntWritable val : values) {
            sum += val.get();
        }
        countMap.put(key.toString(), sum);
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        // 按出现次数降序排序
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(countMap.entrySet());
        entryList.sort(Comparator.comparingInt(Map.Entry<String, Integer>::getValue).reversed());

        // 截取TopN并输出
        int limit = Math.min(topN, entryList.size());
        IntWritable outValue = new IntWritable();
        Text outKey = new Text();
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> entry = entryList.get(i);
            outKey.set(entry.getKey());
            outValue.set(entry.getValue());
            context.write(outKey, outValue);
        }
    }
}
