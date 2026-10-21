package com.poem.analysis;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Task 2: count the number of DISTINCT authors for each dynasty.
 * Input: tab-separated poem records, fields:
 *        [0]=id [1]=title [2]=author [3]=dynasty [4]=content ... [7]=content_len
 * Output: dynasty \t authorCount
 * Run:   hadoop jar <jar> com.poem.analysis.DynastyAuthorCount <input> <output>
 */
public class DynastyAuthorCount {

    /** Mapper: emit <dynasty, author> for every valid poem record. */
    public static class DynastyAuthorMapper extends Mapper<LongWritable, Text, Text, Text> {
        private final Text outKey = new Text();
        private final Text outVal = new Text();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            String line = value.toString().trim();
            if (line.isEmpty()) {
                return;
            }
            String[] fields = line.split("\t");
            // need at least: id, title, author, dynasty
            if (fields.length >= 4) {
                String author = fields[2].trim();
                String dynasty = fields[3].trim();
                if (author.isEmpty() || dynasty.isEmpty()) {
                    return;
                }
                // skip anonymous works, consistent with AuthorWorkCountTopN
                if ("无名氏".equals(author)) {
                    return;
                }
                outKey.set(dynasty);
                outVal.set(author);
                context.write(outKey, outVal);
            }
        }
    }

    /** Reducer: one call per dynasty; deduplicate authors with a Set. */
    public static class DynastyAuthorReducer extends Reducer<Text, Text, Text, IntWritable> {
        private final IntWritable result = new IntWritable();

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            Set<String> authors = new HashSet<>();
            for (Text author : values) {
                authors.add(author.toString());
            }
            result.set(authors.size());
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: DynastyAuthorCount <input> <output>");
            System.exit(-1);
        }
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "dynasty_author_count");
        job.setJarByClass(DynastyAuthorCount.class);
        job.setMapperClass(DynastyAuthorMapper.class);
        // no combiner: distinct-author count cannot be combined on map side
        job.setReducerClass(DynastyAuthorReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setNumReduceTasks(1);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
