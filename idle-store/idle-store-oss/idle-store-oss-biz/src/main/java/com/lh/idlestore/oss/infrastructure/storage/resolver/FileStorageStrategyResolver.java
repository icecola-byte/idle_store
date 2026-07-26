package com.lh.idlestore.oss.infrastructure.storage.resolver;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.lh.idlestore.oss.infrastructure.storage.config.StorageProperties;
import com.lh.idlestore.oss.infrastructure.storage.strategy.FileStorageStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Map;

@Configuration
@Slf4j
public class FileStorageStrategyResolver {

    @Resource
    private StorageProperties storageProperties;

    @Resource
    private Map<String, FileStorageStrategy> fileStorageStrategyMap;

    @Resource
    private Environment environment;

    public FileStorageStrategy resolve() {
        log.info("storageProperties.type = {}", storageProperties.getType());
        log.info("environment.storage.type = {}", environment.getProperty("storage.type"));

        String type = storageProperties.getType();
        log.info("当前存储类型: {}", type);

        FileStorageStrategy storageStrategy = fileStorageStrategyMap.get(type);
        if (storageStrategy == null) {
            throw new IllegalArgumentException("不可用的存储类型: " + type);
        }

        return storageStrategy;
    }

}
