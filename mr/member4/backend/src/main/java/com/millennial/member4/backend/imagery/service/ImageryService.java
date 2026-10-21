package com.millennial.member4.backend.imagery.service;

import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImageryService {

    @Resource
    private Connection hbaseConnection;

    private static final byte[] FAMILY = Bytes.toBytes("info");
    private static final byte[] COL_COUNT = Bytes.toBytes("count");

    /**
     * 1. 获取所有意象总次数
     */
    public Map<String, Integer> getImageryTotal() throws IOException {
        Map<String, Integer> resultMap = new HashMap<>();
        try (Table table = hbaseConnection.getTable(
                org.apache.hadoop.hbase.TableName.valueOf("poetry:imagery_total"));
             ResultScanner scanner = table.getScanner(new Scan())) {

            for (Result res : scanner) {
                String imagery = Bytes.toString(res.getRow());
                int count = Bytes.toInt(res.getValue(FAMILY, COL_COUNT));
                resultMap.put(imagery, count);
            }
        }
        return resultMap;
    }

    /**
     * 2. 获取意象TopN排行榜
     */
    public List<Map<String, Object>> getImageryTopN(int limit) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Table table = hbaseConnection.getTable(
                org.apache.hadoop.hbase.TableName.valueOf("poetry:imagery_topn"));
             ResultScanner scanner = table.getScanner(new Scan().setLimit(limit))) {

            for (Result res : scanner) {
                Map<String, Object> item = new HashMap<>();
                String imagery = Bytes.toString(res.getValue(FAMILY, Bytes.toBytes("imagery")));
                int count = Bytes.toInt(res.getValue(FAMILY, COL_COUNT));
                item.put("imagery", imagery);
                item.put("count", count);
                list.add(item);
            }
        }
        return list;
    }

    /**
     * 3. 按朝代获取意象分布
     */
    public Map<String, Integer> getImageryByDynasty(String dynasty) throws IOException {
        Map<String, Integer> resultMap = new HashMap<>();
        Scan scan = new Scan();
        if (dynasty != null && !dynasty.isEmpty()) {
            scan.setRowPrefixFilter(Bytes.toBytes(dynasty + "_"));
        }

        try (Table table = hbaseConnection.getTable(
                org.apache.hadoop.hbase.TableName.valueOf("poetry:imagery_by_dynasty"));
             ResultScanner scanner = table.getScanner(scan)) {

            for (Result res : scanner) {
                String rowKey = Bytes.toString(res.getRow());
                int count = Bytes.toInt(res.getValue(FAMILY, COL_COUNT));
                resultMap.put(rowKey, count);
            }
        }
        return resultMap;
    }

    /**
     * 4. 按作者获取意象偏好
     */
    public Map<String, Integer> getImageryByAuthor(String author) throws IOException {
        Map<String, Integer> resultMap = new HashMap<>();
        Scan scan = new Scan();
        if (author != null && !author.isEmpty()) {
            scan.setRowPrefixFilter(Bytes.toBytes(author + "_"));
        }

        try (Table table = hbaseConnection.getTable(
                org.apache.hadoop.hbase.TableName.valueOf("poetry:imagery_by_author"));
             ResultScanner scanner = table.getScanner(scan)) {

            for (Result res : scanner) {
                String rowKey = Bytes.toString(res.getRow());
                int count = Bytes.toInt(res.getValue(FAMILY, COL_COUNT));
                resultMap.put(rowKey, count);
            }
        }
        return resultMap;
    }
}
