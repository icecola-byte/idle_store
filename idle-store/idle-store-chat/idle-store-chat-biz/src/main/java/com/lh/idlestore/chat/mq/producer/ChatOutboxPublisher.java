package com.lh.idlestore.chat.mq.producer;

import com.lh.idlestore.chat.constant.ChatMqConstants;
import com.lh.idlestore.chat.repository.mysql.dataobject.ChatMessageOutboxDO;
import com.lh.idlestore.chat.repository.mysql.mapper.ChatMessageOutboxMapper;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatOutboxPublisher {

    private final ChatMessageOutboxMapper outboxMapper;
    private final RocketMQTemplate rocketMQTemplate;
    private final TransactionTemplate transactionTemplate;

    // 上一次 publish() 执行结束后，再等待 chat.outbox.publish-delay 后再执行 publish()
    @Scheduled(fixedDelayString = "${chat.outbox.publish-delay:1000}")
    public void publish() {
        for (int i = 0;
             i < ChatMqConstants.PUBLISH_BATCH_SIZE;
             i++) {

            Boolean processed =
                    transactionTemplate.execute(status ->
                            publishOne());

            if (!Boolean.TRUE.equals(processed)) {
                break;
            }
        }
    }

    private boolean publishOne() {
        // 查询待投递以及投递失败待重试的消息
        List<ChatMessageOutboxDO> events =
                outboxMapper.selectReadyForUpdate(
                        LocalDateTime.now(), 1);

        // 没有则退出
        if (events.isEmpty()) {
            return false;
        }
        // 有则尝试发送 MQ 消息
        ChatMessageOutboxDO event = events.get(0);

        try {
            Message<String> message =
                    MessageBuilder
                            .withPayload(event.getEventPayload())
                            .setHeader(
                                    RocketMQHeaders.KEYS,
                                    // 事件ID
                                    event.getEventId().toString())
                            .build();

            SendResult sendResult = rocketMQTemplate.syncSend( // 同步发送，发送成功后才改写 outbox 的状态
                    ChatMqConstants.MESSAGE_TOPIC + ":"
                            + ChatMqConstants.MESSAGE_CREATED_TAG,
                    message,
                    ChatMqConstants.SEND_TIMEOUT_MILLIS);

            if (sendResult == null
                    || sendResult.getSendStatus()
                    != SendStatus.SEND_OK) {
                throw new IllegalStateException(
                        "RocketMQ发送状态异常：" + sendResult);
            }
            // 发送成功则修改 outbox 状态
            int rows = outboxMapper.markSent(
                    event.getEventId(),
                    LocalDateTime.now());

            if (rows != 1) {
                throw new IllegalStateException(
                        "更新Outbox发送状态失败");
            }

            return true;
        } catch (Exception exception) {
            // 发生异常则重试
            LocalDateTime nextRetryTime =
                    calculateNextRetryTime(
                            // 已重试的次数
                            event.getRetryCount());

            int retryRows = outboxMapper.markRetry(
                    event.getEventId(),
                    nextRetryTime);

            if (retryRows != 1) {
                throw new IllegalStateException(
                        "更新Outbox重试状态失败");
            }

            log.error(
                    "发送聊天消息事件失败，eventId={}",
                    event.getEventId(),
                    exception);

            return true;
        }
    }

    /**
     * 指数退避重试
     * 失败的次数越多，下一次重试等待的时间越久
     */
    private LocalDateTime calculateNextRetryTime(
            Integer retryCount) {

        int count = retryCount == null ? 0 : retryCount;

        long delaySeconds = Math.min(
                300L,
                1L << Math.min(count, 8)); // 1s 2s 4s 8s 16s 32s ... 256s

        return LocalDateTime.now().plusSeconds(delaySeconds);
    }
}