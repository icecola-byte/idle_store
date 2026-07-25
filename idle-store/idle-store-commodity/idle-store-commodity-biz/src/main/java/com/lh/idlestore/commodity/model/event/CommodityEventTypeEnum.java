package com.lh.idlestore.commodity.model.event;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 商品服务 Outbox 事件类型。
 */
@Getter
@RequiredArgsConstructor
public enum CommodityEventTypeEnum {

    CATEGORY_CACHE_INVALIDATED(
            "CATEGORY_CACHE_INVALIDATED",
            "商品分类缓存失效"
    );

    @EnumValue
    private final String value;

    private final String description;
}
