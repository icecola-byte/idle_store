package com.lh.idlestore.commodity.infrastructure.cache.redis;

public final class CommodityCategoryRedisKeys {

    private CommodityCategoryRedisKeys() {}

    public static final String COMMODITY_CATEGORY_PREFIX = "commodity_category:enabled:list";

    public static String buildCategoryKey() {
        return COMMODITY_CATEGORY_PREFIX;
    }
}
