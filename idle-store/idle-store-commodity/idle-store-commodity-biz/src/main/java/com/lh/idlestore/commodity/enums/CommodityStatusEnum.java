package com.lh.idlestore.commodity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

/**
 * 商品状态
 */
@Getter
@RequiredArgsConstructor
public enum CommodityStatusEnum {

    PENDING_REVIEW(1, "待审核"),
    ON_SALE(2, "在售中"),
    SOLD_OUT(3, "已售空"),
    REJECTED(4, "已驳回");

    @EnumValue
    private final Integer code;

    private final String description;

    public static boolean isValid(Integer code) {
        return Arrays.stream(CommodityStatusEnum.values())
                .anyMatch(status -> Objects.equals(code, status.getCode()));
    }

    public static CommodityStatusEnum fromCode(Integer code) {
        return Arrays.stream(CommodityStatusEnum.values())
                .filter(status -> Objects.equals(code, status.getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("非法商品状态：" + code));
    }
}
