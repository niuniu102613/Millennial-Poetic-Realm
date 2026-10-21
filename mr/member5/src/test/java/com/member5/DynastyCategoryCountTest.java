package com.member5;

import com.member5.DynastyCategoryCount.DynastyCategoryMapper;
import com.member5.DynastyCategoryCount.DynastyCategoryReducer;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 输出格式：dynasty \t category \t count
 */
public class DynastyCategoryCountTest {

    private MapDriver<Object, Text, Text, IntWritable> mapDriver;
    private ReduceDriver<Text, IntWritable, Text, IntWritable> reduceDriver;
    private MapReduceDriver<Object, Text, Text, IntWritable, Text, IntWritable> mapReduceDriver;

    @Before
    public void setUp() {
        DynastyCategoryMapper mapper = new DynastyCategoryMapper();
        DynastyCategoryReducer reducer = new DynastyCategoryReducer();
        mapDriver = MapDriver.newMapDriver(mapper);
        reduceDriver = ReduceDriver.newReduceDriver(reducer);
        mapReduceDriver = MapReduceDriver.newMapReduceDriver(mapper, reducer);
    }

    @Test
    public void testMapperNormalLine() throws IOException {
        // key = "唐代\t思乡"
        mapDriver.withInput(new Text("1"), new Text("1,静夜思,李白,唐代,床前明月光,思乡,唐诗三百首,20"));
        mapDriver.withOutput(new Text("唐代\t思乡"), new IntWritable(1));
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
        mapDriver.withOutput(new Text("先秦\t悲壮"), new IntWritable(1));
        mapDriver.runTest();
    }

    @Test
    public void testMapperDifferentDynasty() throws IOException {
        mapDriver.withInput(new Text("3"), new Text("3,出塞,王昌龄,唐代,秦时明月汉时关,边塞,唐诗三百首,28"));
        mapDriver.withOutput(new Text("唐代\t边塞"), new IntWritable(1));
        mapDriver.runTest();
    }

    @Test
    public void testReducer() throws IOException {
        List<IntWritable> values = Arrays.asList(
                new IntWritable(1), new IntWritable(1), new IntWritable(1)
        );
        reduceDriver.withInput(new Text("唐代\t思乡"), values);
        reduceDriver.withOutput(new Text("唐代\t思乡"), new IntWritable(3));
        reduceDriver.runTest();
    }

    @Test
    public void testMapReduce() throws IOException {
        mapReduceDriver.withInput(new Text("1"), new Text("1,静夜思,李白,唐代,床前明月光,思乡,唐诗三百首,20"));
        mapReduceDriver.withInput(new Text("2"), new Text("2,九月九日忆山东兄弟,王维,唐代,独在异乡为异客,思乡,唐诗三百首,28"));
        mapReduceDriver.withInput(new Text("3"), new Text("3,出塞,王昌龄,唐代,秦时明月汉时关,边塞,唐诗三百首,28"));
        mapReduceDriver.withInput(new Text("4"), new Text("4,春晓,孟浩然,唐代,春眠不觉晓,山水,唐诗三百首,20"));

        mapReduceDriver.withOutput(new Text("唐代\t思乡"), new IntWritable(2));
        mapReduceDriver.withOutput(new Text("唐代\t边塞"), new IntWritable(1));
        mapReduceDriver.withOutput(new Text("唐代\t山水"), new IntWritable(1));
        mapReduceDriver.runTest();
    }
}
