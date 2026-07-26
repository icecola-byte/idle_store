package com.lh.idlestore.commodity.infrastructure.outbox.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Outbox 事件所属业务聚合类型。
 */
@Getter
@RequiredArgsConstructor
public enum CommodityAggregateTypeEnum {

    CATEGORY("CATEGORY", "商品分类"),
    COMMODITY("COMMODITY", "商品");

    @EnumValue
    private final String value;

    private final String description;
}
