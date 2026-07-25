package com.lh.idlestore.commodity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

/**
 * 商品分类状态。
 */
@Getter
@RequiredArgsConstructor
public enum CategoryStatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    @EnumValue
    private final Integer code;

    private final String description;

    public static boolean isValid(Integer code) {
        return Arrays.stream(values())
                .anyMatch(status -> Objects.equals(code, status.code));
    }

    public static CategoryStatusEnum fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(status -> Objects.equals(code, status.code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("非法商品分类状态：" + code));
    }
}
