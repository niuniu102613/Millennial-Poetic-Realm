package com.member5;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.*;

/**
 * Job 2: 诗词类别TopN统计
 * 输出格式：count \t category（按count降序）
 * 支持通过 -D topn=N 指定返回前N个类别，默认10
 */
public class CategoryTopN {

    public static class CategoryMapper extends Mapper<Object, Text, Text, IntWritable> {
        private final static IntWritable one = new IntWritable(1);
        private Text category = new Text();

        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            if (line.startsWith("poem_id") || line.isEmpty()) {
                return;
            }
            String[] fields = line.split(",");
            if (fields.length >= 8) {
                String cat = fields[fields.length - 3].trim();
                if (!cat.isEmpty()) {
                    category.set(cat);
                    context.write(category, one);
                }
            }
        }
    }

    /**
     * 单Reducer + TreeMap 实现TopN
     * 类别数量不多（通常几十个），单Reducer完全够用
     */
    public static class TopNReducer extends Reducer<Text, IntWritable, IntWritable, Text> {
        private int topN = 10;
        // 按计数降序排列，相同计数的类别放一起
        private TreeMap<Integer, List<String>> countMap = new TreeMap<>(Collections.reverseOrder());

        @Override
        protected void setup(Context context) {
            Configuration conf = context.getConfiguration();
            topN = conf.getInt("topn", 10);
        }

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            countMap.computeIfAbsent(sum, k -> new ArrayList<>()).add(key.toString());
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            int count = 0;
            outer:
            for (Map.Entry<Integer, List<String>> entry : countMap.entrySet()) {
                int freq = entry.getKey();
                for (String category : entry.getValue()) {
                    if (count >= topN) break outer;
                    context.write(new IntWritable(freq), new Text(category));
                    count++;
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: CategoryTopN <input> <output> [topN]");
            System.exit(2);
        }
        Configuration conf = new Configuration();
        // 第三个参数可选：指定TopN
        int topN = 10;
        if (args.length >= 3) {
            topN = Integer.parseInt(args[2]);
        }
        conf.setInt("topn", topN);

        Job job = Job.getInstance(conf, "CategoryTopN");
        job.setJarByClass(CategoryTopN.class);

        job.setMapperClass(CategoryMapper.class);
        job.setReducerClass(TopNReducer.class);
        // TopN只需要1个Reducer
        job.setNumReduceTasks(1);

        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
