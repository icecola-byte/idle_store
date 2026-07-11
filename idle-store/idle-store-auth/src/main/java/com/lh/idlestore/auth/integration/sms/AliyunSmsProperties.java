package com.lh.idlestore.auth.integration.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "aliyun")
@Component
@Data
public class AliyunSmsProperties {
    private String accessKeyId;
    private String accessKeySecret;
}