package com.member5;

import com.member5.CategoryCount;
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
 * 数据列：poem_id,title,author,dynasty,content,category,source,content_len（共8列）
 * category = 倒数第3列，source = 倒数第2列，content_len = 最后1列
 */
public class CategoryCountTest {

    private MapDriver<Object, Text, Text, IntWritable> mapDriver;
    private ReduceDriver<Text, IntWritable, Text, IntWritable> reduceDriver;
    private MapReduceDriver<Object, Text, Text, IntWritable, Text, IntWritable> mapReduceDriver;

    @Before
    public void setUp() {
        CategoryCount.CategoryMapper mapper = new CategoryCount.CategoryMapper();
        CategoryCount.CategoryReducer reducer = new CategoryCount.CategoryReducer();
        mapDriver = MapDriver.newMapDriver(mapper);
        reduceDriver = ReduceDriver.newReduceDriver(reducer);
        mapReduceDriver = MapReduceDriver.newMapReduceDriver(mapper, reducer);
    }

    @Test
    public void testMapperNormalLine() throws IOException {
        // 8列：poem_id,title,author,dynasty,content,category,source,content_len
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
        // content 中包含英文逗号，从末尾取 category（倒数第3列）
        mapDriver.withInput(new Text("2"), new Text("2,易水歌,荆轲,先秦,风萧萧兮易水寒,壮士一去兮不复还,悲壮,古文观止,14"));
        mapDriver.withOutput(new Text("悲壮"), new IntWritable(1));
        mapDriver.runTest();
    }

    @Test
    public void testReducer() throws IOException {
        List<IntWritable> values = Arrays.asList(
                new IntWritable(1), new IntWritable(1), new IntWritable(1)
        );
        reduceDriver.withInput(new Text("思乡"), values);
        reduceDriver.withOutput(new Text("思乡"), new IntWritable(3));
        reduceDriver.runTest();
    }

    @Test
    public void testMapReduce() throws IOException {
        mapReduceDriver.withInput(new Text("1"), new Text("1,静夜思,李白,唐代,床前明月光,思乡,唐诗三百首,20"));
        mapReduceDriver.withInput(new Text("2"), new Text("2,九月九日忆山东兄弟,王维,唐代,独在异乡为异客,思乡,唐诗三百首,28"));
        mapReduceDriver.withInput(new Text("3"), new Text("3,出塞,王昌龄,唐代,秦时明月汉时关,边塞,唐诗三百首,28"));
        mapReduceDriver.withOutput(new Text("思乡"), new IntWritable(2));
        mapReduceDriver.withOutput(new Text("边塞"), new IntWritable(1));
        mapReduceDriver.runTest();
    }
}
