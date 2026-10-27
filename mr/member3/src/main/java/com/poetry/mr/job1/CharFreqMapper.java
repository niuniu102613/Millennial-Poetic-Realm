package com.poetry.mr.job1;

import com.poetry.mr.util.CSVUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.List;

public class CharFreqMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private final Text outKey = new Text();
    private final IntWritable outVal = new IntWritable(1);

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        List<String> fields = CSVUtils.parseCSVLine(line);

        if (fields.size() < 5 || "poem_id".equals(fields.get(0))) return;

        String content = CSVUtils.removePunctuation(CSVUtils.getContent(fields));
        for (char c : content.toCharArray()) {
            if (CSVUtils.isStopChar(c)) continue;
            outKey.set(String.valueOf(c));
            context.write(outKey, outVal);
        }
    }
}
