package com.lh.idlestore.oss.infrastructure.storage.strategy;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件策略接口
 **/
public interface FileStorageStrategy {

    /**
     * 文件上传
     */
    String uploadFile(MultipartFile file, String bucketName);

}
