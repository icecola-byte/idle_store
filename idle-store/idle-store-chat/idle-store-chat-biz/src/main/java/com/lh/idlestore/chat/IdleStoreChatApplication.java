package com.lh.idlestore.chat;

import com.lh.idlestore.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import com.lh.idlestore.user.api.UserFeignApi;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.lh.idlestore.chat.repository.mysql.mapper")
@SpringBootApplication
@EnableFeignClients(clients = {DistributedIdGeneratorFeignApi.class, UserFeignApi.class})
@EnableScheduling
public class IdleStoreChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdleStoreChatApplication.class, args);
    }
}
