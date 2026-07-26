package com.lh.idlestore.oss.infrastructure.storage.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 Client 配置
 **/
@Configuration
public class AliyunOssConfiguration {

    @Resource
    private AliyunOssProperties aliyunOssProperties;

    /**
     * 构建 阿里云 OSS 客户端
     */
    @Bean
    public OSS aliyunOssClient() {
        // 设置访问凭证
        DefaultCredentialProvider credentialsProvider = CredentialsProviderFactory
                .newDefaultCredentialProvider(
                        aliyunOssProperties.getAccessKey(),
                        aliyunOssProperties.getSecretKey()
                );

        // 创建 OSSClient 实例
        return new OSSClientBuilder().build(aliyunOssProperties.getEndpoint(), credentialsProvider);
    }
}
