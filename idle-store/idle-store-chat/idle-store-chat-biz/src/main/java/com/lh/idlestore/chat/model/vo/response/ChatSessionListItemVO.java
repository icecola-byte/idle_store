package com.lh.idlestore.chat.model.vo.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话列表项。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionListItemVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long sessionId;
    private Integer sessionType;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long peerUserId;
    private String displayName;
    private String displayAvatarUrl;
    private Long lastSequence;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer unreadCount;
    private Long lastReadSequence;
}
