package com.lh.idlestore.commodity;

import com.lh.idlestore.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.lh.idlestore.commodity.repository.mapper")
@EnableFeignClients(clients = {DistributedIdGeneratorFeignApi.class})
public class IdleStoreCommodityApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdleStoreCommodityApplication.class, args);
    }
}
