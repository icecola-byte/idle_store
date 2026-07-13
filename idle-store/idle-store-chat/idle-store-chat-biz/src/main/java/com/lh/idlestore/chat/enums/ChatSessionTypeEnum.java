package com.lh.idlestore.chat.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ChatSessionTypeEnum {

    DIRECT(1, "单聊"),
    SYSTEM(2, "系统通知");

    @EnumValue
    private final Integer value;

    private final String description;

    public static ChatSessionTypeEnum fromValue(Integer value) {
        return Arrays.stream(values())
                .filter(item -> item.value.equals(value))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("非法会话类型：" + value));
    }
}