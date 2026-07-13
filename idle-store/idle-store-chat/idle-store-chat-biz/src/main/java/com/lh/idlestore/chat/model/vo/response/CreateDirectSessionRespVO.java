package com.lh.idlestore.chat.model.vo.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDirectSessionRespVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long sessionId;

    private Integer sessionType;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetUserId;

    private LocalDateTime createTime;
}
