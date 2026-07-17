package com.lh.idlestore.chat.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lh.idlestore.chat.constant.ChatMqConstants;
import com.lh.idlestore.chat.repository.cassandra.entity.ChatMessageEntity;
import com.lh.idlestore.chat.repository.cassandra.entity.ChatMessagePrimaryKey;
import com.lh.idlestore.chat.repository.cassandra.repository.ChatMessageRepository;
import com.lh.idlestore.chat.model.event.ChatMessageCreatedEvent;
import com.lh.idlestore.chat.websocket.ChatWebSocketPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = ChatMqConstants.MESSAGE_TOPIC,
        selectorExpression =
                ChatMqConstants.MESSAGE_CREATED_TAG,
        consumerGroup =
                ChatMqConstants.MESSAGE_PERSIST_CONSUMER_GROUP
)
public class ChatMessageCreatedConsumer
        implements RocketMQListener<String> {

    private final ObjectMapper objectMapper;

    private final ChatMessageRepository chatMessageRepository;

    private final ChatWebSocketPushService webSocketPushService;

    @Override
    public void onMessage(String payload) {
        // 消息 JSON --> Object
        ChatMessageCreatedEvent event = parseEvent(payload);

        validateEvent(event);
        // 存入 k-v 数据库
        ChatMessageEntity message = ChatMessageEntity.builder()
                .key(ChatMessagePrimaryKey.of(
                        event.getSessionId(),
                        event.getSequence()))
                .messageId(event.getMessageId())
                .senderId(event.getSenderId())
                .receiverId(event.getReceiverId())
                .messageType(event.getMessageType())
                .content(event.getContent())
                .clientMessageId(
                        event.getClientMessageId())
                .sendTime(event.getSendTime())
                .recalled(false)
                .recallTime(null)
                .build();

        try {
            chatMessageRepository.save(message);

            // 推送失败不回滚消费，离线消息由HTTP同步接口补偿。
            webSocketPushService.pushMessage(event);

            log.info(
                    "聊天消息写入Cassandra成功，"
                            + "eventId={}，messageId={}，"
                            + "sessionId={}，sequence={}",
                    event.getEventId(),
                    event.getMessageId(),
                    event.getSessionId(),
                    event.getSequence());
        } catch (Exception exception) {
            log.error(
                    "聊天消息写入Cassandra失败，"
                            + "eventId={}，messageId={}",
                    event.getEventId(),
                    event.getMessageId(),
                    exception);

            // 必须抛出异常，RocketMQ才会重试。
            throw new IllegalStateException(
                    "聊天消息持久化失败", exception);
        }
    }

    private ChatMessageCreatedEvent parseEvent(
            String payload) {
        try {
            return objectMapper.readValue(
                    payload,
                    ChatMessageCreatedEvent.class);
        } catch (Exception exception) {
            log.error(
                    "聊天消息事件反序列化失败，payload={}",
                    payload,
                    exception);

            throw new IllegalArgumentException(
                    "非法聊天消息事件", exception);
        }
    }

    /**
     * 校验字段完整性
     */
    private void validateEvent(
            ChatMessageCreatedEvent event) {
        if (event == null
                || event.getEventId() == null
                || event.getMessageId() == null
                || event.getSessionId() == null
                || event.getSequence() == null
                || event.getSequence() <= 0
                || event.getSenderId() == null
                || event.getReceiverId() == null
                || event.getMessageType() == null
                || event.getSendTime() == null) {

            throw new IllegalArgumentException(
                    "聊天消息事件字段不完整");
        }
    }
}
