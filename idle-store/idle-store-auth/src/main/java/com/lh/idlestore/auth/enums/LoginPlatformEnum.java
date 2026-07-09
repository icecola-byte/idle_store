package com.lh.idlestore.auth.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 登录平台类型
 * 用于 SaToken 多账号体系，区分不同端的登录 session
 */
@Getter
@AllArgsConstructor
public enum LoginPlatformEnum {

    /** 小程序端 */
    MINI("mini", "小程序端"),

    /** 管理端 */
    ADMIN("admin", "管理端");

    /** 平台标识（对应 SaToken 的 loginType） */
    private final String type;

    /** 平台描述 */
    private final String description;

    public static LoginPlatformEnum valueOfType(String type) {
        for (LoginPlatformEnum platform : LoginPlatformEnum.values()) {
            if (Objects.equals(type, platform.getType())) {
                return platform;
            }
        }
        return null;
    }
}


