package com.lh.idlestore.oss.infrastructure.storage.strategy.impl;

import com.aliyun.oss.OSS;
import com.lh.idlestore.oss.infrastructure.storage.config.AliyunOssProperties;
import com.lh.idlestore.oss.infrastructure.storage.strategy.FileStorageStrategy;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 阿里云 OSS 文件上传策略
 **/
@Slf4j
@Component("aliyun")
public class AliyunOssStorageStrategy implements FileStorageStrategy  {

    @Resource
    private AliyunOssProperties aliyunOssProperties;

    @Resource
    private OSS ossClient;

    @Override
    @SneakyThrows
    public void uploadFile(MultipartFile file, String bucketName, String objectKey) {
        log.info("上传文件至阿里云 OSS: bucket={}, objectKey={}", bucketName, objectKey);
        ossClient.putObject(bucketName, objectKey, file.getInputStream());
    }

    @Override
    public String getStorageProvider() {
        return "ALIYUN_OSS";
    }

    @Override
    public String getDefaultBucketName() {
        return aliyunOssProperties.getBucketName();
    }

    @Override
    public String getAccessUrl(String bucketName, String objectKey) {
        return String.format("https://%s.%s/%s", bucketName, aliyunOssProperties.getEndpoint(), objectKey);
    }

    @Override
    public void deleteFile(String bucketName, String objectKey) {
        ossClient.deleteObject(bucketName, objectKey);
    }
}
