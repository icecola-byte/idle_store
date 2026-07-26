package com.lh.idlestore.oss.controller;

import com.lh.framework.common.response.Response;
import com.lh.idlestore.oss.dto.FileUploadResp;
import com.lh.idlestore.oss.service.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<FileUploadResp> uploadFile(@RequestPart(value = "file") MultipartFile file) {
        return Response.success(fileService.uploadFile(file));
    }

    @PostMapping("/access-urls")
    public Response<Map<Long, String>> getAccessUrls(@RequestBody List<Long> fileIds) {
        return Response.success(fileService.getAccessUrls(fileIds));
    }

    @DeleteMapping("/{fileId}")
    public Response<Void> deleteFile(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);
        return Response.success();
    }

}
