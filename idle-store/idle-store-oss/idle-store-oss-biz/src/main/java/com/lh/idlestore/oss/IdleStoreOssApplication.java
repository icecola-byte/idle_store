package com.lh.idlestore.oss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.lh.idlestore.oss.repository.mapper")
public class IdleStoreOssApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdleStoreOssApplication.class, args);
    }
}
