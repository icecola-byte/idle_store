package com.lh.idlestore.oss.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * 上传文件
     */
    String uploadFile(MultipartFile file);
}
