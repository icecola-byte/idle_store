package com.lh.idlestore.commodity.infrastructure.cache.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.lh.framework.cache.core.NamedCaffeineCacheRegistry;
import com.lh.idlestore.commodity.infrastructure.cache.dto.CommodityCategoryCacheDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class CommodityCategoryLocalCache {

    private static final String ALL_CATEGORY_KEY = "all";

    private final NamedCaffeineCacheRegistry caffeineCacheRegistry;

    /**
     * 获取启用分类平铺列表。
     *
     * <p>Caffeine 的 cache.get(key, loader) 会在本地缓存未命中时执行 loader，
     * 并把 loader 返回值自动写入本地缓存。</p>
     */
    public List<CommodityCategoryCacheDTO> getCategories(
            Supplier<List<CommodityCategoryCacheDTO>> loader) {

        Cache<String, List<CommodityCategoryCacheDTO>> cache =
                caffeineCacheRegistry.getCache(
                        CommodityCategoryLocalCacheNames.CATEGORY_LIST
                );

        return cache.get(ALL_CATEGORY_KEY, key -> loader.get());
    }

    public void clearAll() {
        caffeineCacheRegistry.invalidateAll(
                CommodityCategoryLocalCacheNames.CATEGORY_LIST
        );
    }
}
