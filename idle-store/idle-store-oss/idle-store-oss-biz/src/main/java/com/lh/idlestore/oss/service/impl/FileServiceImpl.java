package com.lh.idlestore.oss.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.base.Preconditions;
import com.lh.idlestore.oss.dto.FileUploadResp;
import com.lh.idlestore.oss.infrastructure.storage.resolver.FileStorageStrategyResolver;
import com.lh.idlestore.oss.infrastructure.storage.strategy.FileStorageStrategy;
import com.lh.idlestore.oss.repository.dataobject.FileObjectDO;
import com.lh.idlestore.oss.repository.mapper.FileObjectMapper;
import com.lh.idlestore.oss.service.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Resource
    private FileStorageStrategyResolver fileStorageStrategyResolver;

    @Resource
    private FileObjectMapper fileObjectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadResp uploadFile(MultipartFile file) {
        validateFile(file);

        FileStorageStrategy strategy = fileStorageStrategyResolver.resolveForUpload();
        String bucketName = strategy.getDefaultBucketName();
        String objectKey = generateObjectKey(file.getOriginalFilename());
        boolean uploaded = false;
        try {
            strategy.uploadFile(file, bucketName, objectKey);
            uploaded = true;

            FileObjectDO fileObject = FileObjectDO.builder()
                    .storageProvider(strategy.getStorageProvider())
                    .bucketName(bucketName)
                    .objectKey(objectKey)
                    .originalFilename(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .fileStatus(0)
                    .build();
            fileObjectMapper.insert(fileObject);

            return new FileUploadResp(
                    fileObject.getFileId(),
                    strategy.getAccessUrl(bucketName, objectKey)
            );
        } catch (Exception exception) {
            if (uploaded) {
                compensateDelete(strategy, bucketName, objectKey);
            }
            throw new IllegalStateException("文件上传或文件元数据保存失败", exception);
        }
    }

    @Override
    public Map<Long, String> getAccessUrls(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return Map.of();
        }
        List<Long> distinctIds = fileIds.stream()
                .filter(Objects::nonNull)
                .filter(fileId -> fileId > 0)
                .distinct()
                .toList();
        if (distinctIds.isEmpty()) {
            return Map.of();
        }

        Collection<FileObjectDO> fileObjects = fileObjectMapper.selectList(
                Wrappers.<FileObjectDO>lambdaQuery()
                        .in(FileObjectDO::getFileId, distinctIds)
                        .eq(FileObjectDO::getFileStatus, 0)
        );
        Map<Long, String> accessUrls = new LinkedHashMap<>();
        for (FileObjectDO fileObject : fileObjects) {
            FileStorageStrategy strategy = fileStorageStrategyResolver.resolveForFile(fileObject);
            accessUrls.put(fileObject.getFileId(), strategy.getAccessUrl(
                    fileObject.getBucketName(), fileObject.getObjectKey()));
        }
        return accessUrls;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long fileId) {
        if (fileId == null || fileId <= 0) {
            return;
        }
        FileObjectDO fileObject = fileObjectMapper.selectById(fileId);
        if (fileObject == null || !Objects.equals(fileObject.getFileStatus(), 0)) {
            return;
        }

        FileStorageStrategy strategy = fileStorageStrategyResolver.resolveForFile(fileObject);
        strategy.deleteFile(fileObject.getBucketName(), fileObject.getObjectKey());
        fileObjectMapper.update(null, Wrappers.<FileObjectDO>lambdaUpdate()
                .eq(FileObjectDO::getFileId, fileId)
                .eq(FileObjectDO::getFileStatus, 0)
                .set(FileObjectDO::getFileStatus, 2)
                .set(FileObjectDO::getDeleteTime, LocalDateTime.now()));
    }

    private void validateFile(MultipartFile file) {
        Preconditions.checkArgument(file != null && !file.isEmpty(), "上传文件不能为空");
    }

    private String generateObjectKey(String originalFilename) {
        String suffix = "";
        if (originalFilename != null) {
            int suffixIndex = originalFilename.lastIndexOf('.');
            if (suffixIndex >= 0) {
                suffix = originalFilename.substring(suffixIndex);
            }
        }
        return UUID.randomUUID().toString().replace("-", "") + suffix;
    }

    private void compensateDelete(FileStorageStrategy strategy, String bucketName, String objectKey) {
        try {
            strategy.deleteFile(bucketName, objectKey);
        } catch (Exception deleteException) {
            log.error("文件元数据写入失败，补偿删除对象失败: bucket={}, objectKey={}", bucketName, objectKey, deleteException);
        }
    }
}
