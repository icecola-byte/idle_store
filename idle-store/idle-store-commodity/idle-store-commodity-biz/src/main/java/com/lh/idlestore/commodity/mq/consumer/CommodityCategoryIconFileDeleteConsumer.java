package com.lh.idlestore.commodity.mq.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lh.idlestore.commodity.mq.constant.CommodityMqConstants;
import com.lh.idlestore.commodity.mq.event.CommodityCategoryIconFileDeleteRequestEvent;
import com.lh.idlestore.commodity.remote.OssRemoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = CommodityMqConstants.ICON_FILE_DELETE_GROUP,
        topic = CommodityMqConstants.OUTBOX_TOPIC,
        selectorExpression = CommodityMqConstants.CATEGORY_ICON_FILE_DELETE_REQUEST_TAG
)
@RequiredArgsConstructor
public class CommodityCategoryIconFileDeleteConsumer implements RocketMQListener<String> {

    private final ObjectMapper objectMapper;

    private final OssRemoteService ossRemoteService;

    @Override
    public void onMessage(String s) {
        CommodityCategoryIconFileDeleteRequestEvent event = parseEvent(s);
        // 不捕获异常，报错直接抛出
        ossRemoteService.deleteFile(event.getFileId());
    }

    private CommodityCategoryIconFileDeleteRequestEvent parseEvent(String s) {
        try {
            return objectMapper.readValue(s, CommodityCategoryIconFileDeleteRequestEvent.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException(
                    "商品分类图标删除事件反序列化失败",
                    exception
            );
        }
    }
}
