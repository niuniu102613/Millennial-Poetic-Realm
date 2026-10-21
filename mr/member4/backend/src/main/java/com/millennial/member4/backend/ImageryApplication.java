package com.millennial.member4.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Emily 意象分析后端 启动类
 * 独立端口运行，提供意象相关4个接口
 */
@SpringBootApplication
public class ImageryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImageryApplication.class, args);
    }
}
