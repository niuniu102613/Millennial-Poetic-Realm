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
import java.util.Map;
import java.util.TreeMap;

/**
 * Task 1: count works per author and output the topN authors.
 * Input format: tab-separated record per line, fields:
 *        id \t title \t author \t dynasty \t content \t category \t source \t content_len
 * Output format: author \t workCount (ordered by count descending).
 */
public class AuthorWorkCountTopN
{

    /**
     * Mapper phase: extract the author field, emit <author, 1>.
     */
    public static class AuthorCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
        // reuse objects to avoid frequent object creation
        private final Text authorKey = new Text();
        private static final IntWritable ONE = new IntWritable(1);

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            String line = value.toString().trim();
            // skip empty lines
            if (line.isEmpty()) {
                return;
            }

            // split the record by tab
            String[] fields = line.split("\t");
            // dirty-data filter: need at least 3 fields (author is the 3rd field, index 2)
            if (fields.length >= 3) {
                String authorName = fields[2].trim();
                // skip empty author names and anonymous works ("无名氏")
                if (!authorName.isEmpty() && !"无名氏".equals(authorName)) {
                    authorKey.set(authorName);
                    context.write(authorKey, ONE);
                }
            }
        }
    }

    /**
     * Reducer phase: accumulate the total work count per author, keep a TopN set,
     * and emit the sorted result at the end.
     * Also used as a Combiner to do local aggregation on the map side.
     */
    public static class AuthorCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        // ascending TreeMap: key = work count, value = author name, auto-sorted by count
        private TreeMap<Integer, String> topNMap;
        private int topNNum;

        /**
         * setup: read the TopN value from the job configuration.
         */
        @Override
        protected void setup(Context context) {
            Configuration conf = context.getConfiguration();
            // topN value passed from the driver, default to 10
            topNNum = conf.getInt("top.n", 10);
            topNMap = new TreeMap<>();
        }

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            // total works of the current author
            int total = 0;
            for (IntWritable count : values) {
                total += count.get();
            }

            // add the current author to the TopN candidates
            topNMap.put(total, key.toString());

            // once over N, remove the smallest entry
            if (topNMap.size() > topNNum) {
                topNMap.remove(topNMap.firstKey());
            }
        }

        /**
         * cleanup: runs before the reduce task finishes, emit TopN in descending order.
         */
        @Override
        protected void cleanup(Context context)
                throws IOException, InterruptedException {
            // iterate the reversed TreeMap, from largest count to smallest
            for (Map.Entry<Integer, String> entry : topNMap.descendingMap().entrySet()) {
                context.write(new Text(entry.getValue()), new IntWritable(entry.getKey()));
            }
        }
    }

    /**
     * Driver: configure the job and submit it.
     */
    public static void main(String[] args) throws Exception {
        // parameter check
        if (args.length < 3) {
            System.err.println("Usage: AuthorWorkCountTopN <input> <output> <topN>");
            System.err.println("Example: hadoop jar poem-analysis.jar com.poem.analysis.AuthorWorkCountTopN /poem/input /poem/output/top10 10");
            System.exit(-1);
        }

        // 1. initialize configuration
        Configuration conf = new Configuration();
        // pass the TopN value to the reducers
        conf.setInt("top.n", Integer.parseInt(args[2]));

        // 2. create the Job instance
        Job job = Job.getInstance(conf, "author_work_count_topn");
        // set the jar entry class
        job.setJarByClass(AuthorWorkCountTopN.class);

        // 3. bind Mapper, Combiner and Reducer
        job.setMapperClass(AuthorCountMapper.class);
        job.setCombinerClass(AuthorCountReducer.class); // local aggregation on map side
        job.setReducerClass(AuthorCountReducer.class);

        // 4. set map output key/value types
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        // 5. set final output key/value types
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // 6. one reducer task keeps the global TopN consistent
        job.setNumReduceTasks(1);

        // 7. set HDFS input/output paths
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // 8. submit the job and wait for completion
        boolean success = job.waitForCompletion(true);
        System.exit(success ? 0 : 1);
    }
}
