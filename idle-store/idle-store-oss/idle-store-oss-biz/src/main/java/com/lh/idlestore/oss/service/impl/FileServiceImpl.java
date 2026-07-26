package com.lh.idlestore.oss.service.impl;

import com.lh.idlestore.oss.infrastructure.storage.resolver.FileStorageStrategyResolver;
import com.lh.idlestore.oss.service.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Resource
    private FileStorageStrategyResolver fileStorageStrategyResolver;

    private static final String BUCKET_NAME = "idlestore";

    @Override
    public String uploadFile(MultipartFile file) {
        // 上传文件
        String url = fileStorageStrategyResolver.resolve().uploadFile(file, BUCKET_NAME);

        return url;
    }
}
