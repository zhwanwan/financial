package com.imooc.seller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 销售端启动类
 * (exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
 */
@SpringBootApplication
@EnableCaching
@EntityScan("com.imooc.entity")
@EnableScheduling
public class SellerApp {
    public static void main(String[] args) {
        SpringApplication.run(SellerApp.class);
    }
}

