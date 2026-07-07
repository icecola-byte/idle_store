package com.lh.idlestore.user;

import com.lh.idlestore.oss.api.FileFeignApi;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = {FileFeignApi.class})
@MapperScan("com.lh.idlestore.user.repository.mapper")
public class IdleStoreUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdleStoreUserApplication.class, args);
    }

}
