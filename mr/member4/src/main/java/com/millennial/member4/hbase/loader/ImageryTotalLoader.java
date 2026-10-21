package com.millennial.member4.hbase.loader;

import com.millennial.member4.hbase.HBaseUtil;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 意象总次数结果入库程序
 * 读取MR输出的part-r-00000文件，写入 imagery_total 表
 */
public class ImageryTotalLoader {

    private static final String TABLE_NAME = "poetry:imagery_total";
    private static final byte[] FAMILY = Bytes.toBytes("info");
    private static final byte[] COLUMN_COUNT = Bytes.toBytes("count");

    public static void main(String[] args) throws IOException {
        // 参数校验：ZooKeeper地址  MR输出结果文件路径
        if (args.length != 2) {
            System.err.println("Usage: ImageryTotalLoader <zk-quorum> <mr-result-file-path>");
            System.exit(1);
        }

        String zkQuorum = args[0];
        String resultFilePath = args[1];

        try {
            // 1. 初始化HBase连接，自动建表
            HBaseUtil.init(zkQuorum);
            HBaseUtil.createTable(TABLE_NAME, "info");

            // 2. 获取表对象
            Table table = HBaseUtil.getTable(TABLE_NAME);

            // 3. 逐行读取MR输出，写入HBase
            try (BufferedReader br = new BufferedReader(new FileReader(resultFilePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // MR输出默认制表符分隔 key\tvalue
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

                    // 构造Put对象，RowKey为意象名
                    Put put = new Put(Bytes.toBytes(imagery));
                    put.addColumn(FAMILY, COLUMN_COUNT, Bytes.toBytes(count));
                    table.put(put);
                }
                System.out.println("✅ 意象总次数数据入库完成！");
            } finally {
                table.close();
            }
        } finally {
            HBaseUtil.close();
        }
    }
}
