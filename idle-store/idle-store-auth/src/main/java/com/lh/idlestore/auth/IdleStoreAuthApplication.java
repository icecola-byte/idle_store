package com.lh.idlestore.auth;

import com.lh.idlestore.user.api.UserFeignApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = {UserFeignApi.class})
public class IdleStoreAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdleStoreAuthApplication.class, args);
    }

}
