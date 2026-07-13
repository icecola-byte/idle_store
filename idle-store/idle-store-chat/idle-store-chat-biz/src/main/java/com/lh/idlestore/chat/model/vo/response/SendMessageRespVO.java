package com.lh.idlestore.chat.model.vo.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息已被聊天服务接收。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRespVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long messageId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long sessionId;

    /**
     * 消息在会话内的顺序。
     */
    private Long sequence;

    private String clientMessageId;

    private LocalDateTime sendTime;
}
