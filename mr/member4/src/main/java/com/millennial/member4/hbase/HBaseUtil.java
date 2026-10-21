package com.millennial.member4.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;

import java.io.IOException;

/**
 * 4号成员 HBase工具类
 * 封装连接、建表、资源释放操作
 */
public class HBaseUtil {

    private static Connection connection;
    private static Admin admin;

    /**
     * 初始化HBase连接
     * @param zkQuorum ZooKeeper地址，如 localhost:2181
     */
    public static void init(String zkQuorum) throws IOException {
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", zkQuorum);
        connection = ConnectionFactory.createConnection(conf);
        admin = connection.getAdmin();
    }

    /**
     * 创建表，表已存在则跳过
     */
    public static void createTable(String tableName, String... familyNames) throws IOException {
        TableName tn = TableName.valueOf(tableName);
        if (admin.tableExists(tn)) {
            return;
        }
        TableDescriptorBuilder builder = TableDescriptorBuilder.newBuilder(tn);
        for (String family : familyNames) {
            ColumnFamilyDescriptor familyDesc = ColumnFamilyDescriptorBuilder.of(family);
            builder.setColumnFamily(familyDesc);
        }
        admin.createTable(builder.build());
    }

    /**
     * 获取表操作对象
     */
    public static org.apache.hadoop.hbase.client.Table getTable(String tableName) throws IOException {
        return connection.getTable(TableName.valueOf(tableName));
    }

    /**
     * 关闭所有资源
     */
    public static void close() throws IOException {
        if (admin != null) admin.close();
        if (connection != null) connection.close();
    }
}
