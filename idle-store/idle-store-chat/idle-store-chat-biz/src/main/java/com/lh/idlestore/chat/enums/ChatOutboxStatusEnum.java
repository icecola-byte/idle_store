package com.lh.idlestore.chat.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Outbox 事件投递状态。
 */
@Getter
@AllArgsConstructor
public enum ChatOutboxStatusEnum {

    PENDING(0, "待投递"),
    SENT(1, "已发送"),
    RETRY(2, "投递失败待重试");

    @EnumValue
    private final Integer value;

    private final String description;

    public static ChatOutboxStatusEnum fromValue(Integer value) {
        return Arrays.stream(values())
                .filter(item -> item.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("非法Outbox状态：" + value));
    }
}
