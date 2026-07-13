package com.lh.idlestore.chat.repository.cassandra.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;

/**
 * Cassandra 聊天消息记录。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("message_by_session")
public class ChatMessageEntity {

    @PrimaryKey
    private ChatMessagePrimaryKey key;

    @Column("message_id")
    private Long messageId;

    @Column("sender_id")
    private Long senderId;

    @Column("receiver_id")
    private Long receiverId;

    /**
     * 消息类型值，对应 ChatMessageTypeEnum.value。
     */
    @Column("message_type")
    private Byte messageType;

    private String content;

    /**
     * Cassandra timestamp 推荐使用 Instant。
     */
    @Column("client_message_id")
    private String clientMessageId;

    @Column("send_time")
    private Instant sendTime;

    @Column("is_recalled")
    private Boolean recalled;

    @Column("recall_time")
    private Instant recallTime;
}
