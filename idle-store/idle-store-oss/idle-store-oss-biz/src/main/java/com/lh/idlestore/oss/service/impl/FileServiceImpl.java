package com.lh.idlestore.oss.service.impl;

import com.lh.idlestore.oss.factory.FileStrategyFactory;
import com.lh.idlestore.oss.service.FileService;
import com.lh.idlestore.oss.strategy.FileStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Resource
    private FileStrategyFactory fileStrategyFactory;

    private static final String BUCKET_NAME = "idlestore";

    @Override
    public String uploadFile(MultipartFile file) {
        // 上传文件
        String url = fileStrategyFactory.getFileStrategy().uploadFile(file, BUCKET_NAME);

        return url;
    }
}
