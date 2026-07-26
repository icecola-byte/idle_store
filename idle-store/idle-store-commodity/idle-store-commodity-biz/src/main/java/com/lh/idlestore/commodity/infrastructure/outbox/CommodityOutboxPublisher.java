package com.lh.idlestore.commodity.infrastructure.outbox;

import com.lh.idlestore.commodity.infrastructure.outbox.config.CommodityOutboxProperties;
import com.lh.idlestore.commodity.infrastructure.outbox.constant.CommodityOutboxConstants;
import com.lh.idlestore.commodity.mq.constant.CommodityMqConstants;
import com.lh.idlestore.commodity.repository.dataobject.CommodityOutboxDO;
import com.lh.idlestore.commodity.repository.mapper.CommodityOutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时扫描 Outbox，并可靠投递 RocketMQ。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommodityOutboxPublisher {

    private final CommodityOutboxMapper commodityOutboxMapper;

    private final RocketMQTemplate rocketMQTemplate;

    private final TransactionTemplate transactionTemplate;

    @Scheduled(fixedDelayString = "${commodity.outbox.publish-delay:1s}")
    public void publish() {
        for (int i = 0; i < CommodityMqConstants.PUBLISH_BATCH_SIZE; i++) {
            Boolean processed = transactionTemplate.execute(
                    status -> publishOne()
            );

            if (!Boolean.TRUE.equals(processed)) {
                break;
            }
        }
    }

    /**
     * 在单独事务内抢占并处理一条事件。
     */
    private boolean publishOne() {
        List<CommodityOutboxDO> events =
                commodityOutboxMapper.selectReadyForUpdate(LocalDateTime.now(), 1);

        if (events.isEmpty()) {
            return false;
        }

        CommodityOutboxDO event = events.get(0);

        try {
            Message<String> message = MessageBuilder
                    .withPayload(event.getEventPayload())
                    .setHeader(
                            RocketMQHeaders.KEYS,
                            event.getEventId().toString()
                    )
                    .build();

            SendResult sendResult = rocketMQTemplate.syncSend(
                    CommodityMqConstants.OUTBOX_TOPIC
                            + ":"
                            + event.getEventType(),
                    message,
                    CommodityMqConstants.SEND_TIMEOUT_MILLIS
            );

            if (sendResult == null
                    || sendResult.getSendStatus() != SendStatus.SEND_OK) {
                throw new IllegalStateException(
                        "RocketMQ 发送状态异常：" + sendResult
                );
            }

            int updatedRows = commodityOutboxMapper.markSent(
                    event.getEventId(),
                    LocalDateTime.now()
            );

            if (updatedRows != 1) {
                throw new IllegalStateException(
                        "更新 Outbox 已发送状态失败"
                );
            }

            return true;
        } catch (Exception exception) {
            int updatedRows = commodityOutboxMapper.markRetry(
                    event.getEventId(),
                    calculateNextRetryTime(event.getRetryCount())
            );

            if (updatedRows != 1) {
                throw new IllegalStateException(
                        "更新 Outbox 重试状态失败",
                        exception
                );
            }

            log.error(
                    "发送商品服务 Outbox 事件失败，eventId={}",
                    event.getEventId(),
                    exception
            );

            return true;
        }
    }

    /**
     * 指数退避：1s、2s、4s、8s……最长 300s。
     */
    private LocalDateTime calculateNextRetryTime(Integer retryCount) {

        int count = retryCount == null ? 0 : retryCount;

        long delaySeconds = Math.min(
                CommodityOutboxConstants.MAX_RETRY_DELAY_SECONDS,
                1L << Math.min(count, CommodityOutboxConstants.MAX_RETRY_EXPONENT)
        );

        return LocalDateTime.now().plusSeconds(delaySeconds);
    }
}
