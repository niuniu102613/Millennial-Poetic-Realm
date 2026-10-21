package com.millennial.member4.hbase.loader;

import com.millennial.member4.hbase.HBaseUtil;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 朝代×意象统计结果入库程序
 * 读取MR输出结果，写入 imagery_by_dynasty 表
 * RowKey：朝代_意象名
 */
public class ImageryByDynastyLoader {

    private static final String TABLE_NAME = "poetry:imagery_by_dynasty";
    private static final byte[] FAMILY = Bytes.toBytes("info");
    private static final byte[] COL_COUNT = Bytes.toBytes("count");

    public static void main(String[] args) throws IOException {
        // 参数校验：ZooKeeper地址  MR输出结果文件路径
        if (args.length != 2) {
            System.err.println("Usage: ImageryByDynastyLoader <zk-quorum> <mr-result-file-path>");
            System.exit(1);
        }

        String zkQuorum = args[0];
        String resultFilePath = args[1];
        int count = 0;

        try {
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

                    String rowKey = parts[0].trim();
                    int num;
                    try {
                        num = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException e) {
                        continue;
                    }

                    Put put = new Put(Bytes.toBytes(rowKey));
                    put.addColumn(FAMILY, COL_COUNT, Bytes.toBytes(num));
                    table.put(put);
                    count++;
                }
                System.out.println("✅ 朝代×意象数据入库完成！共写入 " + count + " 条");
            } finally {
                table.close();
            }
        } finally {
            HBaseUtil.close();
        }
    }
}
