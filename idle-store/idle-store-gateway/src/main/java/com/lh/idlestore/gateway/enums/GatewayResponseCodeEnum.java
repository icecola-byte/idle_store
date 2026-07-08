package com.lh.idlestore.gateway.enums;

import com.lh.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GatewayResponseCodeEnum implements BaseExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("GATEWAY-50000", "系统繁忙，请稍后再试"),
    UNAUTHORIZED("GATEWAY-40100", "未登录或登录状态已失效"),
    FORBIDDEN("GATEWAY-40300", "权限不足"),


    // ----------- 业务异常状态码 -----------
    ;

    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;

}
