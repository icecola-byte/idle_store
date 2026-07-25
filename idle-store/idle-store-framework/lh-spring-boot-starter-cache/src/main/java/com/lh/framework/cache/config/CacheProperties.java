package com.lh.framework.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 本地缓存配置。
 *
 * 支持默认配置，也支持每个 cacheName 单独配置。
 */
@Data
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {

    private CaffeineProperties caffeine = new CaffeineProperties();

    @Data
    public static class CaffeineProperties {

        /**
         * 是否启用 Caffeine 本地缓存。
         */
        private boolean enabled = true;

        /**
         * 默认缓存配置。
         *
         * 如果某个 cacheName 没有单独配置，就使用这个默认配置。
         */
        private CacheSpec defaultCache = new CacheSpec();

        /**
         * 多个命名缓存的独立配置。
         *
         * key 是业务侧定义的 cacheName。
         */
        private Map<String, CacheSpec> caches = new HashMap<>();
    }

    @Data
    public static class CacheSpec {

        /**
         * 最大缓存条数。
         */
        private long maximumSize = 1000;

        /**
         * 写入后过期时间，单位：秒。
         */
        private long expireAfterWriteSeconds = 600;
    }
}