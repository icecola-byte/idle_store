package com.lh.idlestore.chat.repository.mysql.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lh.idlestore.chat.enums.ChatOutboxStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息可靠投递事件。
 *
 * 本表只负责 RocketMQ 投递，不是聊天记录的永久存储。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_chat_message_outbox")
public class ChatMessageOutboxDO {

    @TableId(type = IdType.INPUT)
    private Long eventId;

    private Long messageId;

    private Long sessionId;

    private Long sequence;

    private Long senderId;

    private Long receiverId;

    private String clientMessageId;

    private String eventType;

    /**
     * 完整事件 JSON，消费者直接使用该内容写入 Cassandra。
     */
    private String eventPayload;

    private ChatOutboxStatusEnum eventStatus;

    private Integer retryCount;

    private LocalDateTime nextRetryTime;

    private LocalDateTime sentTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
