package com.lh.idlestore.commodity.infrastructure.cache.redis;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lh.framework.common.util.CacheTtlUtils;
import com.lh.idlestore.commodity.infrastructure.cache.dto.CommodityCategoryCacheDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommodityCategoryRedisCache {

    private static final TypeReference<List<CommodityCategoryCacheDTO>>
            CATEGORIES_TYPE_REFERENCE = new TypeReference<>() {};

    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;

    /**
     * 查询启用商品分类缓存。
     *
     * 返回 null 表示 Redis 未命中。
     * 返回空 List 表示 Redis 命中，但当前没有启用分类。
     */
    public List<CommodityCategoryCacheDTO> getCategories() {
        String key = CommodityCategoryRedisKeys.buildCategoryKey();

        Object cachedCategories = redisTemplate.opsForValue().get(key);
        if (cachedCategories == null) {
            return null;
        }

        return objectMapper.convertValue(
                cachedCategories,
                CATEGORIES_TYPE_REFERENCE
        );
    }

    /**
     * 写入商品分类下的缓存。
     *
     * 空列表也会缓存，用来防止不存在的数据反复打到数据库。
     */
    public void setCategories(
            List<CommodityCategoryCacheDTO> categories) {
        String key = CommodityCategoryRedisKeys.buildCategoryKey();
        // 防止传进来 null 值，这是不合理的
        List<CommodityCategoryCacheDTO> cacheValue =
                categories == null ? List.of() : categories;

        Duration ttl = getCacheTtl(cacheValue);

        redisTemplate.opsForValue().set(key, cacheValue, ttl);
    }

    /**
     * 删除商品分类缓存。
     */
    public void deleteCategories() {
        String key = CommodityCategoryRedisKeys.buildCategoryKey();
        redisTemplate.delete(key);
    }

    private Duration getCacheTtl(List<CommodityCategoryCacheDTO> categories) {
        if (CollUtil.isEmpty(categories)) {
            return CacheTtlUtils.randomTtl(
                    CommodityCategoryRedisConstants.EMPTY_TTL,
                    CommodityCategoryRedisConstants.EMPTY_TTL_RANDOM_RANGE
            );
        }

        return CacheTtlUtils.randomTtl(
                CommodityCategoryRedisConstants.NORMAL_TTL,
                CommodityCategoryRedisConstants.NORMAL_TTL_RANDOM_RANGE
        );
    }
}
