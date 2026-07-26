package com.lh.idlestore.commodity.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lh.idlestore.commodity.infrastructure.outbox.enums.CommodityAggregateTypeEnum;
import com.lh.idlestore.commodity.infrastructure.outbox.enums.CommodityEventTypeEnum;
import com.lh.idlestore.commodity.infrastructure.outbox.enums.CommodityOutboxStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 商品服务可靠事件 Outbox 表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_commodity_outbox")
public class CommodityOutboxDO {

    /**
     * Outbox 事件 ID，由分布式 ID 服务生成。
     */
    @TableId(type = IdType.INPUT)
    private Long eventId;

    /**
     * 聚合类型，例如 CATEGORY、COMMODITY。
     */
    private CommodityAggregateTypeEnum aggregateType;

    /**
     * 业务聚合 ID，例如分类 ID、商品 ID。
     */
    private Long aggregateId;

    /**
     * 事件类型，例如 CATEGORY_CACHE_INVALIDATED。
     */
    private CommodityEventTypeEnum eventType;

    /**
     * 发送到 RocketMQ 的完整事件 JSON。
     */
    private String eventPayload;

    /**
     * 0-待投递，1-已发送，2-投递失败待重试。
     */
    private CommodityOutboxStatusEnum eventStatus;

    /**
     * 已失败重试次数。
     */
    private Integer retryCount;

    /**
     * 下一次允许投递的时间。
     */
    private LocalDateTime nextRetryTime;

    /**
     * RocketMQ 确认接收时间。
     */
    private LocalDateTime sentTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
