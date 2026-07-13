package com.lh.idlestore.chat.repository.mysql.projection;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话列表数据库查询结果。
 */
@Data
public class ChatSessionSummaryProjection {

    private Long sessionId;
    private Integer sessionType;
    private Long peerUserId;
    private Long lastSequence;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer unreadCount;
    private Long lastReadSequence;
    private LocalDateTime createTime;
}
