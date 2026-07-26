package com.lh.idlestore.oss.infrastructure.storage.strategy.impl;

import com.lh.idlestore.oss.infrastructure.storage.config.MinioProperties;
import com.lh.idlestore.oss.infrastructure.storage.strategy.FileStorageStrategy;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component("minio")
public class MinioStorageStrategy implements FileStorageStrategy {

    @Resource
    private MinioProperties minioProperties;

    @Resource
    private MinioClient minioClient;

    @Override
    @SneakyThrows
    public void uploadFile(MultipartFile file, String bucketName, String objectKey) {
        log.info("上传文件至 MinIO: bucket={}, objectKey={}", bucketName, objectKey);
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectKey)
                .stream(file.getInputStream(), file.getSize(), -1L)
                .contentType(file.getContentType())
                .build());
    }

    @Override
    public String getStorageProvider() {
        return "MINIO";
    }

    @Override
    public String getDefaultBucketName() {
        return minioProperties.getBucketName();
    }

    @Override
    public String getAccessUrl(String bucketName, String objectKey) {
        return minioProperties.getEndpoint().replaceAll("/+$", "") + "/" + bucketName + "/" + objectKey;
    }

    @Override
    @SneakyThrows
    public void deleteFile(String bucketName, String objectKey) {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectKey)
                .build());
    }
}
