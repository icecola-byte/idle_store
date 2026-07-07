package com.lh.idlestore.user.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 用户状态
 */
@Getter
@AllArgsConstructor
public enum UserStatusEnum {

    NORMAL(0),
    BANNED(1);

    @EnumValue
    private final Integer value;

    public static boolean isValid(Integer value) {
        return Arrays.stream(UserStatusEnum.values())
                .anyMatch(status -> Objects.equals(value, status.getValue()));
    }

    public static UserStatusEnum fromValue(Integer value) {
        return Arrays.stream(UserStatusEnum.values())
                .filter(status -> Objects.equals(value, status.getValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("非法用户状态：" + value));
    }
}
