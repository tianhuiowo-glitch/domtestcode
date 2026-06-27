package com.dorm.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * MyBatis 配置类及通用 Bean 注册
 * 主要配置项已在 application.yml 中设置
 *
 * @author dorm-server
 */
@Configuration
public class MyBatisConfig {

    // MyBatis 核心配置已在 application.yml mybatis 节点中定义：
    // - mapper-locations: classpath:mapper/*.xml
    // - map-underscore-to-camel-case: true

    /**
     * BCrypt 密码编码器 Bean
     * 供 AuthServiceImpl 使用，避免每次 new 创建
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
