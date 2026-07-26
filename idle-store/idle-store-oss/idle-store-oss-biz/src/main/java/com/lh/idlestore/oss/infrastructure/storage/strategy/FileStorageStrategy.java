package com.lh.idlestore.oss.infrastructure.storage.strategy;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件策略接口
 **/
public interface FileStorageStrategy {

    String getStorageProvider();

    String getDefaultBucketName();

    void uploadFile(MultipartFile file, String bucketName, String objectKey);

    String getAccessUrl(String bucketName, String objectKey);

    void deleteFile(String bucketName, String objectKey);

}
