package com.lh.idlestore.commodity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
public enum LogisticsModeEnum {

    RECYCLING(1, "上门回收"),
    REPLACEMENT(2, "物物置换"),
    HOME_DELIVERY(4, "送货上门"),
    FIXED_LOCATION(8, "社区贸易");

    @EnumValue
    private final Integer code;

    private final String description;

    public static boolean isValid(Integer code) {
        return Arrays.stream(LogisticsModeEnum.values())
                .anyMatch(status -> Objects.equals(code, status.getCode()));
    }

    public static LogisticsModeEnum fromCode(Integer code) {
        return Arrays.stream(LogisticsModeEnum.values())
                .filter(status -> Objects.equals(code, status.getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("非法商品状态：" + code));
    }

}
