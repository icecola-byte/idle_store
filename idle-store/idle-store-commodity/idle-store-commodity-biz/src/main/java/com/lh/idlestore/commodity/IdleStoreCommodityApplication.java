package com.lh.idlestore.commodity;

import com.lh.idlestore.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import com.lh.idlestore.oss.api.FileFeignApi;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.lh.idlestore.commodity.repository.mapper")
@EnableFeignClients(clients = {DistributedIdGeneratorFeignApi.class, FileFeignApi.class})
@EnableScheduling
public class IdleStoreCommodityApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdleStoreCommodityApplication.class, args);
    }
}
