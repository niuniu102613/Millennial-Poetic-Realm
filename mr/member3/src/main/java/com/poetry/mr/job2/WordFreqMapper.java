package com.poetry.mr.job2;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.poetry.mr.util.CSVUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordFreqMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    /** JiebaSegmenter 初始化重（加载词典），放在 setup 里只加载一次 */
    private JiebaSegmenter segmenter;

    private final Text outKey = new Text();
    private final IntWritable outVal = new IntWritable(1);

    /** 文言/白话常见虚词停用表：过滤后结果更像"词" */
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "之", "乎", "者", "也", "而", "以", "于", "其", "且", "则", "乃", "焉",
            "矣", "哉", "兮", "何", "如", "似", "共", "又", "复", "莫", "无", "未",
            "自", "但", "虽", "纵", "却", "须", "即", "欲", "为", "因", "缘", "故",
            "在", "是", "有", "了", "着", "过", "与", "及", "或", "此", "该", "等",
            "上", "下", "中", "内", "外", "间", "时", "处", "此中", "何处", "何时"
    ));

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        segmenter = new JiebaSegmenter();
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        List<String> fields = CSVUtils.parseCSVLine(line);

        if (fields.size() < 5 || "poem_id".equals(fields.get(0))) return;

        // 直接把带标点的原文交给 jieba，它能正确保留词边界
        String content = CSVUtils.getContent(fields);
        List<String> words = segmenter.sentenceProcess(content);

        for (String w : words) {
            String word = w.trim();
            if (word.length() < 2) continue;              // 丢弃单字
            if (STOP_WORDS.contains(word)) continue;      // 丢弃虚词
            if (!containsChinese(word)) continue;         // 丢弃纯标点/数字
            outKey.set(word);
            context.write(outKey, outVal);
        }
    }

    private boolean containsChinese(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (CSVUtils.isChinese(s.charAt(i))) return true;
        }
        return false;
    }
}
