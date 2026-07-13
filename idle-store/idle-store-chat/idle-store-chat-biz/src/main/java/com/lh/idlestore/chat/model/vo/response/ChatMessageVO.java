package com.lh.idlestore.chat.model.vo.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long messageId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sessionId;
    private Long sequence;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long senderId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long receiverId;
    private Integer messageType;
    private String content;
    private String clientMessageId;
    private Instant sendTime;
    private Boolean recalled;
}
