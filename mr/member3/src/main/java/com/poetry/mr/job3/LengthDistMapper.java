package com.poetry.mr.job3;

import com.poetry.mr.util.CSVUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.List;

public class LengthDistMapper extends Mapper<LongWritable, Text, IntWritable, IntWritable> {

    private final IntWritable outKey = new IntWritable();
    private final IntWritable outVal = new IntWritable(1);

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        List<String> fields = CSVUtils.parseCSVLine(line);

        if (fields.size() < 8 || "poem_id".equals(fields.get(0))) return;

        try {
            int len = Integer.parseInt(CSVUtils.getContentLen(fields).trim());
            if (len > 0) {
                outKey.set(len);
                context.write(outKey, outVal);
            }
        } catch (NumberFormatException e) {
            // skip malformed row
        }
    }
}
