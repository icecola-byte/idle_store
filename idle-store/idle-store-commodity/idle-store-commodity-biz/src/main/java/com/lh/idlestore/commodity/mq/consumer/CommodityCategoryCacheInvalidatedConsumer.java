package com.lh.idlestore.commodity.mq.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lh.idlestore.commodity.mq.constant.CommodityMqConstants;
import com.lh.idlestore.commodity.infrastructure.cache.local.CommodityCategoryLocalCache;
import com.lh.idlestore.commodity.infrastructure.cache.redis.CommodityCategoryRedisCache;
import com.lh.idlestore.commodity.mq.event.CommodityCategoryCacheInvalidatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 广播清理每个商品服务实例的商品分类缓存。
 */
@RocketMQMessageListener(
        consumerGroup = CommodityMqConstants.CATEGORY_CACHE_CONSUMER_GROUP,
        topic = CommodityMqConstants.OUTBOX_TOPIC,
        selectorExpression = CommodityMqConstants.CATEGORY_CACHE_INVALIDATED_TAG,
        messageModel = MessageModel.BROADCASTING
)
@Slf4j
@Component
@RequiredArgsConstructor
public class CommodityCategoryCacheInvalidatedConsumer implements RocketMQListener<String> {

    private final ObjectMapper objectMapper;

    private final CommodityCategoryRedisCache commodityCategoryRedisCache;

    private final CommodityCategoryLocalCache commodityCategoryLocalCache;

    @Override
    public void onMessage(String eventPayload) {
        // 消息 JSON
        CommodityCategoryCacheInvalidatedEvent event = parseEvent(eventPayload);
        log.info("receive commodity category cache invalidated event: {}", event);

        commodityCategoryRedisCache.deleteCategories();
        commodityCategoryLocalCache.clearAll();

        log.info("商品分类缓存清理完成，eventId={}", event.getEventId());
    }

    public CommodityCategoryCacheInvalidatedEvent parseEvent(String eventPayload) {
        try {
            return objectMapper.readValue(
                    eventPayload,
                    CommodityCategoryCacheInvalidatedEvent.class
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException(
                    "商品分类缓存失效事件反序列化失败",
                    exception
            );
        }
    }
}
