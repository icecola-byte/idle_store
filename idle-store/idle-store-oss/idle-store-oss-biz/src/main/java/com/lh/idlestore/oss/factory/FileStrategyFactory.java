package com.lh.idlestore.oss.factory;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.lh.idlestore.oss.config.StorageProperties;
import com.lh.idlestore.oss.strategy.FileStrategy;
import com.lh.idlestore.oss.strategy.impl.AliyunOssFileStrategy;
import com.lh.idlestore.oss.strategy.impl.MinioFileStrategy;
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
public class FileStrategyFactory {

    @Resource
    private StorageProperties storageProperties;

    @Resource
    private Map<String, FileStrategy> fileStrategyMap;

    @Resource
    private Environment environment;

    public FileStrategy getFileStrategy() {
        log.info("storageProperties.type = {}", storageProperties.getType());
        log.info("environment.storage.type = {}", environment.getProperty("storage.type"));

        String type = storageProperties.getType();
        log.info("当前存储类型: {}", type);

        FileStrategy fileStrategy = fileStrategyMap.get(type);
        if (fileStrategy == null) {
            throw new IllegalArgumentException("不可用的存储类型: " + type);
        }

        return fileStrategy;
    }

}