package com.millennial.member4.backend.config;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * HBase 连接配置类
 */
@Configuration
public class HBaseConfig {

    @Value("${hbase.zookeeper.quorum}")
    private String zkQuorum;

    @Value("${hbase.zookeeper.port}")
    private String zkPort;

    @Bean
    public Connection hbaseConnection() throws IOException {
        // 使用全限定类名，避免与Spring的@Configuration注解重名冲突
        org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", zkQuorum);
        conf.set("hbase.zookeeper.property.clientPort", zkPort);
        return ConnectionFactory.createConnection(conf);
    }
}
