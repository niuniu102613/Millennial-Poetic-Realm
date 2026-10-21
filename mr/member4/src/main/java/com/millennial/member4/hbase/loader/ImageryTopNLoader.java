package com.millennial.member4.hbase.loader;

import com.millennial.member4.hbase.HBaseUtil;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 意象TopN结果入库程序
 * 读取MR输出的排序结果，写入 imagery_topn 表
 * RowKey：两位排名序号（00、01...），按排名正序存储
 */
public class ImageryTopNLoader {

    private static final String TABLE_NAME = "poetry:imagery_topn";
    private static final byte[] FAMILY = Bytes.toBytes("info");
    private static final byte[] COL_IMAGERY = Bytes.toBytes("imagery");
    private static final byte[] COL_COUNT = Bytes.toBytes("count");

    public static void main(String[] args) throws IOException {
        // 参数校验：ZooKeeper地址  MR输出结果文件路径
        if (args.length != 2) {
            System.err.println("Usage: ImageryTopNLoader <zk-quorum> <mr-result-file-path>");
            System.exit(1);
        }

        String zkQuorum = args[0];
        String resultFilePath = args[1];
        int rank = 0;

        try {
            // 初始化连接，自动建表
            HBaseUtil.init(zkQuorum);
            HBaseUtil.createTable(TABLE_NAME, "info");

            Table table = HBaseUtil.getTable(TABLE_NAME);

            try (BufferedReader br = new BufferedReader(new FileReader(resultFilePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\t");
                    if (parts.length != 2) {
                        continue;
                    }

                    String imagery = parts[0].trim();
                    int count;
                    try {
                        count = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException e) {
                        continue;
                    }

                    // RowKey：两位序号补零，保证字典序正序
                    String rowKey = String.format("%02d", rank);
                    Put put = new Put(Bytes.toBytes(rowKey));
                    put.addColumn(FAMILY, COL_IMAGERY, Bytes.toBytes(imagery));
                    put.addColumn(FAMILY, COL_COUNT, Bytes.toBytes(count));
                    table.put(put);

                    rank++;
                }
                System.out.println("✅ 意象TopN数据入库完成！共写入 " + rank + " 条");
            } finally {
                table.close();
            }
        } finally {
            HBaseUtil.close();
        }
    }
}
