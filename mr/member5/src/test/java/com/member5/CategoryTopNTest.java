package com.member5;

import com.member5.CategoryTopN.CategoryMapper;
import com.member5.CategoryTopN.TopNReducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class CategoryTopNTest {

    private MapDriver<Object, Text, Text, IntWritable> mapDriver;
    private ReduceDriver<Text, IntWritable, IntWritable, Text> reduceDriver;
    private MapReduceDriver<Object, Text, Text, IntWritable, IntWritable, Text> mapReduceDriver;

    @Before
    public void setUp() {
        CategoryMapper mapper = new CategoryMapper();
        TopNReducer reducer = new TopNReducer();
        mapDriver = MapDriver.newMapDriver(mapper);
        reduceDriver = ReduceDriver.newReduceDriver(reducer);
        mapReduceDriver = MapReduceDriver.newMapReduceDriver(mapper, reducer);
    }

    @Test
    public void testMapperNormalLine() throws IOException {
        mapDriver.withInput(new Text("1"), new Text("1,静夜思,李白,唐代,床前明月光,思乡,唐诗三百首,20"));
        mapDriver.withOutput(new Text("思乡"), new IntWritable(1));
        mapDriver.runTest();
    }

    @Test
    public void testMapperSkipsHeader() throws IOException {
        mapDriver.withInput(new Text("0"), new Text("poem_id,title,author,dynasty,content,category,source,content_len"));
        mapDriver.runTest();
    }

    @Test
    public void testMapperSkipsEmptyLine() throws IOException {
        mapDriver.withInput(new Text("0"), new Text(""));
        mapDriver.runTest();
    }

    @Test
    public void testMapperContentWithEnglishComma() throws IOException {
        mapDriver.withInput(new Text("2"), new Text("2,易水歌,荆轲,先秦,风萧萧兮易水寒,壮士一去兮不复还,悲壮,古文观止,14"));
        mapDriver.withOutput(new Text("悲壮"), new IntWritable(1));
        mapDriver.runTest();
    }

    @Test
    public void testReducerTopN() throws IOException {
        Configuration conf = new Configuration();
        conf.setInt("topn", 3);
        reduceDriver.setConfiguration(conf);

        reduceDriver.withInput(new Text("思乡"), Arrays.asList(new IntWritable(10)));
        reduceDriver.withInput(new Text("边塞"), Arrays.asList(new IntWritable(8)));
        reduceDriver.withInput(new Text("咏物"), Arrays.asList(new IntWritable(6)));
        reduceDriver.withInput(new Text("山水"), Arrays.asList(new IntWritable(4)));
        reduceDriver.withInput(new Text("爱情"), Arrays.asList(new IntWritable(2)));

        reduceDriver.withOutput(new IntWritable(10), new Text("思乡"));
        reduceDriver.withOutput(new IntWritable(8), new Text("边塞"));
        reduceDriver.withOutput(new IntWritable(6), new Text("咏物"));
        reduceDriver.runTest();
    }

    @Test
    public void testReducerDefaultTopN() throws IOException {
        reduceDriver.withInput(new Text("思乡"), Arrays.asList(new IntWritable(5)));
        reduceDriver.withInput(new Text("边塞"), Arrays.asList(new IntWritable(3)));

        reduceDriver.withOutput(new IntWritable(5), new Text("思乡"));
        reduceDriver.withOutput(new IntWritable(3), new Text("边塞"));
        reduceDriver.runTest();
    }

    @Test
    public void testMapReduce() throws IOException {
        Configuration conf = new Configuration();
        conf.setInt("topn", 2);
        mapReduceDriver.setConfiguration(conf);

        mapReduceDriver.withInput(new Text("1"), new Text("1,静夜思,李白,唐代,床前明月光,思乡,唐诗三百首,20"));
        mapReduceDriver.withInput(new Text("2"), new Text("2,九月九日忆山东兄弟,王维,唐代,独在异乡为异客,思乡,唐诗三百首,28"));
        mapReduceDriver.withInput(new Text("3"), new Text("3,出塞,王昌龄,唐代,秦时明月汉时关,边塞,唐诗三百首,28"));
        mapReduceDriver.withInput(new Text("4"), new Text("4,春晓,孟浩然,唐代,春眠不觉晓,山水,唐诗三百首,20"));

        // Top2: 思乡(2) 一定第一；边塞(1)和山水(1)并列第二，取哪个取决于TreeMap顺序
        mapReduceDriver.withOutput(new IntWritable(2), new Text("思乡"));
        mapReduceDriver.runTest();
    }
}
