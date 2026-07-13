package com.lh.idlestore.chat.model.vo.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateDirectSessionReqVO {

    /**
     * 要聊天的对方用户ID。
     */
    @NotNull(message = "目标用户ID不能为空")
    @Positive(message = "目标用户ID必须大于0")
    private Long targetUserId;
}
