package com.dorm.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 企業宿舍管理系統 - 主启动类
 *
 * @author dorm-server
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan("com.dorm.server.mapper")
@EnableAsync
public class DormServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DormServerApplication.class, args);
    }
}
