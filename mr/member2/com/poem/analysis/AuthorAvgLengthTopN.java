package com.poem.analysis;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Task 3: TopN authors ranked by the AVERAGE length of their works.
 * Input: tab-separated poem records, fields[2]=author, fields[7]=content_len.
 * Output: author \t averageLength (descending).
 * Run:   hadoop jar <jar> com.poem.analysis.AuthorAvgLengthTopN <input> <output> <topN>
 */
public class AuthorAvgLengthTopN {

    /** Mapper: emit <author, contentLength> for every valid record. */
    public static class LengthMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
        private final Text authorKey = new Text();
        private final IntWritable lengthVal = new IntWritable();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            String line = value.toString().trim();
            if (line.isEmpty()) {
                return;
            }
            String[] fields = line.split("\t");
            if (fields.length >= 8) {
                String author = fields[2].trim();
                String lenStr = fields[7].trim();
                if (author.isEmpty() || lenStr.isEmpty()) {
                    return;
                }
                if ("无名氏".equals(author)) {
                    return;
                }
                try {
                    lengthVal.set(Integer.parseInt(lenStr));
                    authorKey.set(author);
                    context.write(authorKey, lengthVal);
                } catch (NumberFormatException ignored) {
                    // skip dirty length field
                }
            }
        }
    }

    /**
     * Reducer: one call per author -> sum / count = average.
     * Keeps a global ascending TreeMap<avg, author> and trims it to topN.
     * Requires a single reducer task so all authors share one TreeMap.
     */
    public static class AvgReducer extends Reducer<Text, IntWritable, Text, DoubleWritable> {
        private TreeMap<Double, String> topMap;
        private int topN;

        @Override
        protected void setup(Context context) {
            topN = context.getConfiguration().getInt("top.n", 10);
            topMap = new TreeMap<>();
        }

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            long sum = 0;
            int count = 0;
            for (IntWritable v : values) {
                sum += v.get();
                count++;
            }
            if (count == 0) {
                return;
            }
            double avg = (double) sum / count;
            topMap.put(avg, key.toString());
            if (topMap.size() > topN) {
                topMap.remove(topMap.firstKey());
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            for (Map.Entry<Double, String> e : topMap.descendingMap().entrySet()) {
                context.write(new Text(e.getValue()), new DoubleWritable(e.getKey()));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: AuthorAvgLengthTopN <input> <output> <topN>");
            System.exit(-1);
        }
        Configuration conf = new Configuration();
        conf.setInt("top.n", Integer.parseInt(args[2]));
        Job job = Job.getInstance(conf, "author_avg_length_topn");
        job.setJarByClass(AuthorAvgLengthTopN.class);
        job.setMapperClass(LengthMapper.class);
        // no combiner: average (sum/count) cannot be combined on map side
        job.setReducerClass(AvgReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);
        job.setNumReduceTasks(1);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
