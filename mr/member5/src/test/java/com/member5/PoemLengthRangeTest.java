package com.member5;

import com.member5.PoemLengthRange.LengthRangeMapper;
import com.member5.PoemLengthRange.LengthRangeReducer;
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
 * 直接使用 content_len 字段（最后一列），不自己去标点算长度
 */
public class PoemLengthRangeTest {

    private MapDriver<Object, Text, Text, IntWritable> mapDriver;
    private ReduceDriver<Text, IntWritable, Text, IntWritable> reduceDriver;
    private MapReduceDriver<Object, Text, Text, IntWritable, Text, IntWritable> mapReduceDriver;

    @Before
    public void setUp() {
        LengthRangeMapper mapper = new LengthRangeMapper();
        LengthRangeReducer reducer = new LengthRangeReducer();
        mapDriver = MapDriver.newMapDriver(mapper);
        reduceDriver = ReduceDriver.newReduceDriver(reducer);
        mapReduceDriver = MapReduceDriver.newMapReduceDriver(mapper, reducer);
    }

    @Test
    public void testMapperShortPoem() throws IOException {
        // content_len = 20，属于 0-20 区间
        mapDriver.withInput(new Text("1"), new Text("1,静夜思,李白,唐代,床前明月光,思乡,唐诗三百首,20"));
        mapDriver.withOutput(new Text("0-20"), new IntWritable(1));
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
    public void testMapperMediumPoem() throws IOException {
        // content_len = 48，属于 21-50 区间
        mapDriver.withInput(new Text("3"), new Text("3,出塞,王昌龄,唐代,秦时明月汉时关,边塞,唐诗三百首,48"));
        mapDriver.withOutput(new Text("21-50"), new IntWritable(1));
        mapDriver.runTest();
    }

    @Test
    public void testMapperBoundary() throws IOException {
        // 边界值：20 -> 0-20；21 -> 21-50；50 -> 21-50；51 -> 51-100
        mapDriver.withInput(new Text("1"), new Text("1,a,b,唐,c,d,e,20"));
        mapDriver.withOutput(new Text("0-20"), new IntWritable(1));
        mapDriver.runTest();

        mapDriver.withInput(new Text("2"), new Text("2,a,b,唐,c,d,e,21"));
        mapDriver.withOutput(new Text("21-50"), new IntWritable(1));
        mapDriver.runTest();

        mapDriver.withInput(new Text("3"), new Text("3,a,b,唐,c,d,e,100"));
        mapDriver.withOutput(new Text("51-100"), new IntWritable(1));
        mapDriver.runTest();

        mapDriver.withInput(new Text("4"), new Text("4,a,b,唐,c,d,e,101"));
        mapDriver.withOutput(new Text("101-200"), new IntWritable(1));
        mapDriver.runTest();

        mapDriver.withInput(new Text("5"), new Text("5,a,b,唐,c,d,e,201"));
        mapDriver.withOutput(new Text("200+"), new IntWritable(1));
        mapDriver.runTest();
    }

    @Test
    public void testMapperInvalidLength() throws IOException {
        // content_len 不是数字，跳过
        mapDriver.withInput(new Text("9"), new Text("9,a,b,唐,c,d,e,xxx"));
        mapDriver.runTest();
    }

    @Test
    public void testReducer() throws IOException {
        List<IntWritable> values = Arrays.asList(
                new IntWritable(1), new IntWritable(1), new IntWritable(1)
        );
        reduceDriver.withInput(new Text("0-20"), values);
        reduceDriver.withOutput(new Text("0-20"), new IntWritable(3));
        reduceDriver.runTest();
    }

    @Test
    public void testMapReduce() throws IOException {
        mapReduceDriver.withInput(new Text("1"), new Text("1,静夜思,李白,唐代,床前明月光,思乡,唐诗三百首,20"));
        mapReduceDriver.withInput(new Text("2"), new Text("2,春晓,孟浩然,唐代,春眠不觉晓,山水,唐诗三百首,20"));
        mapReduceDriver.withInput(new Text("3"), new Text("3,出塞,王昌龄,唐代,秦时明月汉时关,边塞,唐诗三百首,48"));

        // 0-20: 2 首，21-50: 1 首
        mapReduceDriver.withOutput(new Text("0-20"), new IntWritable(2));
        mapReduceDriver.withOutput(new Text("21-50"), new IntWritable(1));
        mapReduceDriver.runTest();
    }
}
