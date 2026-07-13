package com.lh.idlestore.chat.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 聊天消息创建事件。
 *
 * Outbox 将该对象序列化为JSON，之后发送到RocketMQ。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageCreatedEvent {

    private Long eventId;

    private Long messageId;

    private Long sessionId;

    private Long sequence;

    private Long senderId;

    private Long receiverId;

    /**
     * 1-文本，2-图片，3-业务卡片。
     */
    private Byte messageType;

    private String content;

    private String clientMessageId;

    /**
     * 消息在服务端被接受的时间。
     */
    private Instant sendTime;
}