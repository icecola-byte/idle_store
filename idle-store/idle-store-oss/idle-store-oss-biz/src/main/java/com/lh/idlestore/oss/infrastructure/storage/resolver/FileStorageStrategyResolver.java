package com.lh.idlestore.oss.infrastructure.storage.resolver;

import com.lh.idlestore.oss.infrastructure.storage.config.StorageProperties;
import com.lh.idlestore.oss.infrastructure.storage.strategy.FileStorageStrategy;
import com.lh.idlestore.oss.repository.dataobject.FileObjectDO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@Slf4j
public class FileStorageStrategyResolver {

    @Resource
    private StorageProperties storageProperties;

    @Resource
    private Map<String, FileStorageStrategy> fileStorageStrategyMap;

    public FileStorageStrategy resolveForUpload() {
        return resolveByBeanName(storageProperties.getType());
    }

    public FileStorageStrategy resolveForFile(FileObjectDO fileObject) {
        String beanName = switch (fileObject.getStorageProvider()) {
            case "MINIO" -> "minio";
            case "ALIYUN_OSS" -> "aliyun";
            default -> throw new IllegalArgumentException("不可用的文件存储提供方: " + fileObject.getStorageProvider());
        };
        return resolveByBeanName(beanName);
    }

    private FileStorageStrategy resolveByBeanName(String type) {
        if (StringUtils.isBlank(type)) {
            throw new IllegalArgumentException("未配置存储类型");
        }
        FileStorageStrategy storageStrategy = fileStorageStrategyMap.get(type.trim().toLowerCase());
        if (storageStrategy == null) {
            throw new IllegalArgumentException("不可用的存储类型: " + type);
        }

        return storageStrategy;
    }

}
