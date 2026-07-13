package com.lh.idlestore.chat.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ChatMessageTypeEnum {

    TEXT(1, "文本"),
    IMAGE(2, "图片"),
    CARD(3, "业务卡片");

    @EnumValue
    private final Integer value;

    private final String description;

    public static ChatMessageTypeEnum fromValue(Integer value) {
        return Arrays.stream(values())
                .filter(item -> item.value.equals(value))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("非法消息类型：" + value));
    }
}
