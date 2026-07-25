package com.lh.framework.cache.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.lh.framework.cache.config.CacheProperties;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine 缓存工厂。
 *
 * 负责根据配置创建真正的本地缓存实例。
 */
public class CaffeineCacheFactory {

    public <K, V> Cache<K, V> create(CacheProperties.CacheSpec cacheSpec) {
        return Caffeine.newBuilder()
                .maximumSize(cacheSpec.getMaximumSize())
                .expireAfterWrite(
                        cacheSpec.getExpireAfterWriteSeconds(),
                        TimeUnit.SECONDS
                )
                .build();
    }
}
