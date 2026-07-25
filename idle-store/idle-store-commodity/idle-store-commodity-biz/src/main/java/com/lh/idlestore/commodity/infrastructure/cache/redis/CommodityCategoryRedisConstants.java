package com.lh.idlestore.commodity.infrastructure.cache.redis;

import java.time.Duration;

public final class CommodityCategoryRedisConstants {

    private CommodityCategoryRedisConstants() {}

    /**
     * 正常分类缓存基础时间。
     */
    public static final Duration NORMAL_TTL = Duration.ofHours(24);

    /**
     * 正常分类缓存随机增加范围。
     */
    public static final Duration NORMAL_TTL_RANDOM_RANGE = Duration.ofHours(2);

    /**
     * 空结果缓存时间。
     */
    public static final Duration EMPTY_TTL = Duration.ofMinutes(5);

    /**
     * 空结果缓存时间随机范围。
     */
    public static final Duration EMPTY_TTL_RANDOM_RANGE =
            Duration.ofMinutes(2);
}
