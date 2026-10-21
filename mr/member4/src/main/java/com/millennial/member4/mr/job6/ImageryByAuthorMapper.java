package com.millennial.member4.mr.job6;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Emily Job6 作者×意象统计 Mapper
 * 输入：CSV原始行
 * 输出：<作者名_意象名, 1>  一首诗同一作者同一意象仅计一次
 */
public class ImageryByAuthorMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private final Text outKey = new Text();
    private final IntWritable outValue = new IntWritable(1);

    // 统一意象词典
    private static final String[] IMAGERY_DICT = {
            "月", "风", "云", "山", "水", "花", "鸟", "酒", "剑",
            "春", "秋", "江", "河", "雪", "雨", "柳", "梅", "菊", "楼", "舟"
    };

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();

        // 跳过CSV表头
        if (line.startsWith("poem_id")) {
            return;
        }

        // 字段校验，脏数据跳过
        String[] fields = line.split(",");
        if (fields.length != 8) {
            return;
        }

        // 提取作者（第3列，索引2）和诗词正文（第5列，索引4）
        String author = fields[2].trim();
        String content = fields[4].trim();
        if (author.isEmpty() || content.isEmpty()) {
            return;
        }

        // 意象匹配、单首诗内去重
        Set<String> existedImagery = new HashSet<>();
        for (String imagery : IMAGERY_DICT) {
            if (content.contains(imagery)) {
                existedImagery.add(imagery);
            }
        }

        // 输出 作者_意象 组合键
        for (String img : existedImagery) {
            outKey.set(author + "_" + img);
            context.write(outKey, outValue);
        }
    }
}
