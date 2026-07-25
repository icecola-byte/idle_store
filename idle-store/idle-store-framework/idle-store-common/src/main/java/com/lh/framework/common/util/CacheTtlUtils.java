package com.lh.framework.common.util;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public final class CacheTtlUtils {

    private CacheTtlUtils() {}

    /**
     * 在基础过期时间上增加随机时间。
     *
     * @param baseTtl      基础过期时间
     * @param randomRange  随机增加的最大范围
     * @return 最终过期时间：[baseTtl, baseTtl + randomRange)
     */
    public static Duration randomTtl(Duration baseTtl, Duration randomRange) {
        if (baseTtl == null || baseTtl.isZero() || baseTtl.isNegative()) {
            throw new IllegalArgumentException("baseTtl 必须大于 0");
        }

        if (randomRange == null || randomRange.isZero()) {
            return baseTtl;
        }

        if (randomRange.isNegative()) {
            throw new IllegalArgumentException("randomRange 不能小于 0");
        }

        long randomMillis = ThreadLocalRandom.current()
                .nextLong(randomRange.toMillis());

        return baseTtl.plusMillis(randomMillis);
    }
}
