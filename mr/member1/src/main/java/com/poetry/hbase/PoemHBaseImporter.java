
package com.poetry.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PoemHBaseImporter {

    private static final String TABLE_NAME = "qianyan:poems";
    private static final int BATCH_SIZE = 500;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println(
                    "用法：PoemHBaseImporter <CSV路径> <ZooKeeper地址>"
            );
            System.exit(1);
        }

        String csvPath = args[0];
        String zkHost = args[1];

        Configuration conf = HBaseConfiguration.create();

        // 使用实际的 ZooKeeper 主机名或 IP 地址
        conf.set("hbase.zookeeper.quorum", zkHost);

        long imported = 0;
        long skipped = 0;

        try (Connection connection = ConnectionFactory.createConnection(conf);
             Table table = connection.getTable(TableName.valueOf(TABLE_NAME));
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(
                             new java.io.FileInputStream(csvPath),
                             StandardCharsets.UTF_8
                     ))) {

            String line;
            boolean firstLine = true;
            List<Put> batch = new ArrayList<>(BATCH_SIZE);

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    if (line.startsWith("\uFEFF")) {
                        line = line.substring(1);
                    }
                    if (line.startsWith("poem_id,")) {
                        continue;
                    }
                }

                List<String> fields = parseCsvLine(line);

                if (fields.size() < 8) {
                    skipped++;
                    continue;
                }

                String poemId = fields.get(0).trim();
                if (poemId.isEmpty()) {
                    skipped++;
                    continue;
                }

                Put put = new Put(Bytes.toBytes(poemId));

                put.addColumn(Bytes.toBytes("info"),
                        Bytes.toBytes("title"),
                        Bytes.toBytes(fields.get(1)));

                put.addColumn(Bytes.toBytes("info"),
                        Bytes.toBytes("author"),
                        Bytes.toBytes(fields.get(2)));

                put.addColumn(Bytes.toBytes("info"),
                        Bytes.toBytes("dynasty"),
                        Bytes.toBytes(fields.get(3)));

                put.addColumn(Bytes.toBytes("info"),
                        Bytes.toBytes("category"),
                        Bytes.toBytes(fields.get(5)));

                put.addColumn(Bytes.toBytes("info"),
                        Bytes.toBytes("source"),
                        Bytes.toBytes(fields.get(6)));

                put.addColumn(Bytes.toBytes("text"),
                        Bytes.toBytes("content"),
                        Bytes.toBytes(fields.get(4)));

                put.addColumn(Bytes.toBytes("text"),
                        Bytes.toBytes("content_len"),
                        Bytes.toBytes(fields.get(7)));

                batch.add(put);

                if (batch.size() >= BATCH_SIZE) {
                    table.put(batch);
                    imported += batch.size();
                    batch.clear();

                    if (imported % 5000 == 0) {
                        System.out.println("已导入：" + imported);
                    }
                }
            }

            if (!batch.isEmpty()) {
                table.put(batch);
                imported += batch.size();
            }
        }

        System.out.println("========== 导入完成 ==========");
        System.out.println("成功写入记录数：" + imported);
        System.out.println("跳过记录数：" + skipped);
    }

    /**
     * 解析 CSV 单行，支持双引号包裹字段及字段中的逗号。
     * 不支持跨多行的带引号字段；当前数据若存在这种情况需使用专用 CSV 库。
     */
    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }

        fields.add(field.toString());
        return fields;
    }
}
