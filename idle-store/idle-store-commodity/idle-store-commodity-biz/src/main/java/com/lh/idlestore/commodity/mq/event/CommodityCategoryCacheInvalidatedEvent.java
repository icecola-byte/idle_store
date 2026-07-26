package com.lh.idlestore.commodity.mq.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品分类缓存失效事件。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommodityCategoryCacheInvalidatedEvent {

    private Long eventId;
}
