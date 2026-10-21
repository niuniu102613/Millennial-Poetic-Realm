
package com.poetry.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

public class PoemHBaseQuery {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("请传入要查询的 poem_id");
            return;
        }

        String poemId = args[0];

        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "192.168.198.128");
        conf.set("hbase.zookeeper.property.clientPort", "2181");

        try (Connection connection = ConnectionFactory.createConnection(conf);
             Table table = connection.getTable(
                     TableName.valueOf("qianyan:poems"))) {

            Get get = new Get(Bytes.toBytes(poemId));
            Result result = table.get(get);

            if (result.isEmpty()) {
                System.out.println("没有找到 poem_id = " + poemId);
                return;
            }

            System.out.println("诗词ID：" + poemId);
            printField(result, "info", "title", "标题");
            printField(result, "info", "author", "作者");
            printField(result, "info", "dynasty", "朝代");
            printField(result, "info", "category", "类别");
            printField(result, "text", "content", "正文");
        }
    }

    private static void printField(
            Result result, String family, String qualifier, String label) {
        byte[] value = result.getValue(
                Bytes.toBytes(family), Bytes.toBytes(qualifier));

        System.out.println(label + "：" +
                (value == null ? "无数据" : Bytes.toString(value)));
    }
}
