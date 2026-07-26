package com.lh.idlestore.oss.service;

import com.lh.idlestore.oss.dto.FileUploadResp;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface FileService {

    /**
     * 上传文件
     */
    FileUploadResp uploadFile(MultipartFile file);

    Map<Long, String> getAccessUrls(List<Long> fileIds);

    void deleteFile(Long fileId);
}
