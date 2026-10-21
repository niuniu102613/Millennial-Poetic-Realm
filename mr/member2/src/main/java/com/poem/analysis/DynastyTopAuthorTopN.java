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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Task 4: for each dynasty, pick the topN authors by number of works.
 * Input: tab-separated poem records, fields[2]=author, fields[3]=dynasty.
 * Output: dynasty \t author \t workCount (per dynasty, count descending).
 * Run:   hadoop jar <jar> com.poem.analysis.DynastyTopAuthorTopN <input> <output> <topNPerDynasty>
 */
public class DynastyTopAuthorTopN {

    /** Mapper: emit <dynasty\tauthor, 1> for every valid poem record. */
    public static class DynastyAuthorMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
        private final Text outKey = new Text();
        private static final IntWritable ONE = new IntWritable(1);

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            String line = value.toString().trim();
            if (line.isEmpty()) {
                return;
            }
            String[] fields = line.split("\t");
            if (fields.length >= 4) {
                String author = fields[2].trim();
                String dynasty = fields[3].trim();
                if (author.isEmpty() || dynasty.isEmpty()) {
                    return;
                }
                if ("无名氏".equals(author)) {
                    return;
                }
                outKey.set(dynasty + "\t" + author);
                context.write(outKey, ONE);
            }
        }
    }

    /**
     * Reducer: one call per (dynasty, author) pair -> total works.
     * Maintains one TreeMap<count, author> per dynasty and keeps only topN.
     * Requires a single reducer task so every dynasty shares this instance.
     */
    public static class DynastyTopReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private Map<String, TreeMap<Integer, String>> topPerDynasty;
        private int topN;

        @Override
        protected void setup(Context context) {
            topN = context.getConfiguration().getInt("top.n", 3);
            topPerDynasty = new HashMap<>();
        }

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            String[] parts = key.toString().split("\t");
            if (parts.length < 2) {
                return;
            }
            String dynasty = parts[0];
            String author = parts[1];
            int count = 0;
            for (IntWritable v : values) {
                count += v.get();
            }
            TreeMap<Integer, String> map =
                    topPerDynasty.computeIfAbsent(dynasty, k -> new TreeMap<>());
            map.put(count, author);
            if (map.size() > topN) {
                map.remove(map.firstKey());
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            for (Map.Entry<String, TreeMap<Integer, String>> entry : topPerDynasty.entrySet()) {
                String dynasty = entry.getKey();
                for (Map.Entry<Integer, String> a : entry.getValue().descendingMap().entrySet()) {
                    context.write(new Text(dynasty + "\t" + a.getValue()),
                            new IntWritable(a.getKey()));
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: DynastyTopAuthorTopN <input> <output> <topNPerDynasty>");
            System.exit(-1);
        }
        Configuration conf = new Configuration();
        conf.setInt("top.n", Integer.parseInt(args[2]));
        Job job = Job.getInstance(conf, "dynasty_top_authors");
        job.setJarByClass(DynastyTopAuthorTopN.class);
        job.setMapperClass(DynastyAuthorMapper.class);
        job.setReducerClass(DynastyTopReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setNumReduceTasks(1);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
