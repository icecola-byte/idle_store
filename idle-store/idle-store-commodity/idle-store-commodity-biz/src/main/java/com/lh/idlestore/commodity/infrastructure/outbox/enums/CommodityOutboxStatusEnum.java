package com.lh.idlestore.commodity.infrastructure.outbox.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 商品服务 Outbox 事件投递状态。
 */
@Getter
@RequiredArgsConstructor
public enum CommodityOutboxStatusEnum {

    PENDING(0, "待投递"),
    SENT(1, "已发送"),
    RETRY(2, "投递失败待重试");

    @EnumValue
    private final Integer value;

    private final String description;
}
