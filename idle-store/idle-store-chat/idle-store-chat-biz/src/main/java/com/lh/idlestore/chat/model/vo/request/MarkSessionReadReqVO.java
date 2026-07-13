package com.lh.idlestore.chat.model.vo.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class MarkSessionReadReqVO {

    @NotNull(message = "已读序号不能为空")
    @PositiveOrZero(message = "已读序号不能小于0")
    private Long lastReadSequence;
}
