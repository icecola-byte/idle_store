package com.lh.framework.cache.core;

import com.lh.framework.cache.config.CacheProperties;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.github.benmanes.caffeine.cache.Cache;

/**
 * 命名缓存注册器。
 *
 * 业务侧只需要通过 cacheName 获取缓存，不需要关心缓存实例怎么创建。
 */
public class NamedCaffeineCacheRegistry {

    private final CacheProperties.CaffeineProperties properties;

    private final CaffeineCacheFactory cacheFactory;

    /**
     * 保存所有已经创建过的本地缓存实例。
     *
     * 同一个 cacheName 在一个应用进程内只会创建一次。
     */
    private final ConcurrentMap<String, Cache<Object, Object>> cacheMap =
            new ConcurrentHashMap<>();

    public NamedCaffeineCacheRegistry(CacheProperties.CaffeineProperties properties, CaffeineCacheFactory cacheFactory) {

        this.properties = properties;
        this.cacheFactory = cacheFactory;
    }

    /**
     * 根据 cacheName 获取缓存。
     *
     * 如果配置里没有这个 cacheName，会使用 default-cache 配置创建。
     */
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String cacheName) {
        Cache<Object, Object> cache = cacheMap.computeIfAbsent(
                cacheName,
                this::createCache
        );

        return (Cache<K, V>) cache;
    }

    /**
     * 清空指定命名缓存。
     */
    public void invalidateAll(String cacheName) {
        getCache(cacheName).invalidateAll();
    }

    /**
     * 清空所有已经创建的本地缓存。
     */
    public void invalidateAll() {
        cacheMap.values().forEach(Cache::invalidateAll);
    }

    private Cache<Object, Object> createCache(String cacheName) {
        CacheProperties.CacheSpec cacheSpec = properties.getCaches()
                .getOrDefault(cacheName, properties.getDefaultCache());

        return cacheFactory.create(cacheSpec);
    }
}
