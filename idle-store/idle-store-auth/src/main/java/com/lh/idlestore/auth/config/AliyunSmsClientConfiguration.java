package com.lh.idlestore.auth.config;

import com.aliyun.teaopenapi.models.Config;
import com.aliyun.credentials.Client;
import com.lh.idlestore.auth.integration.sms.AliyunSmsProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AliyunSmsClientConfiguration {

    @Resource
    private AliyunSmsProperties properties;

    @Bean
    public com.aliyun.dypnsapi20170525.Client ssmClient() throws Exception {
        try {
            Client credential = new Client();
            Config config = new Config()
                    .setAccessKeyId(properties.getAccessKeyId())
                    .setAccessKeySecret(properties.getAccessKeySecret())
                    .setCredential(credential);

            config.endpoint = "dypnsapi.aliyuncs.com";
            return new com.aliyun.dypnsapi20170525.Client(config);
        } catch (Exception e) {
            log.error("初始化阿里云短信发送客户端错误: ", e);
            return null;
        }
    }

}
