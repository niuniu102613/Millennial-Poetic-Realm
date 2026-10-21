package com.millennial.member4.mr.job3;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Emily Job3 意象统计 Mapper
 * 输入：CSV原始行
 * 输出：<意象名, 1>  （一首诗中每个意象仅输出一次）
 */
public class ImageryCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private final Text outKey = new Text();
    private final IntWritable outValue = new IntWritable(1);

    // 预置古典诗词常见意象词典，可自行扩展
    private static final String[] IMAGERY_DICT = {
            "月", "风", "云", "山", "水", "花", "鸟", "酒", "剑",
            "春", "秋", "江", "河", "雪", "雨", "柳", "梅", "菊", "楼", "舟"
    };

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();

        // 1. 跳过CSV表头
        if (line.startsWith("poem_id")) {
            return;
        }

        // 2. 字段校验，脏数据直接跳过
        String[] fields = line.split(",");
        if (fields.length != 8) {
            return;
        }

        // 3. 提取诗词正文 content（第5列，索引从0开始为4）
        String content = fields[4].trim();
        if (content.isEmpty()) {
            return;
        }

        // 4. 匹配意象，去重（一首诗同一个意象只算一次）
        Set<String> existedImagery = new HashSet<>();
        for (String imagery : IMAGERY_DICT) {
            if (content.contains(imagery)) {
                existedImagery.add(imagery);
            }
        }

        // 5. 输出匹配到的意象
        for (String img : existedImagery) {
            outKey.set(img);
            context.write(outKey, outValue);
        }
    }
}
