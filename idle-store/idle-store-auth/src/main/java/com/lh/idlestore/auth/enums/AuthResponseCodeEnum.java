package com.lh.idlestore.auth.enums;

import com.lh.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthResponseCodeEnum implements BaseExceptionInterface {

    // ----------- 业务异常状态码 -----------
    VERIFICATION_CODE_SEND_FREQUENTLY("AUTH-20000", "请求太频繁，请3分钟后再试"),
    VERIFICATION_CODE_ERROR("AUTH-20001", "验证码错误"),
    LOGIN_TYPE_ERROR("AUTH-20002", "登录类型错误"),
    USER_NOT_FOUND("AUTH-20003", "该用户不存在"),
    PHONE_OR_PASSWORD_ERROR("AUTH-20004", "手机号或密码错误"),
    USER_REGISTER_ERROR("AUTH-20005", "用户注册失败"),
    LOGIN_FAIL("AUTH-20006", "登录失败"),

    // 调用异常
    USER_SERVICE_CALL_FAILED( "AUTH-30001", "用户服务调用失败"),


    // ----------- 权限异常状态码 -----------
    NOT_LOGIN("AUTH-40100", "未登录，请先登录"),
    NO_ROLE("AUTH-40301", "无此角色，权限不足"),
    NO_PERMISSION("AUTH-40302", "无此权限，权限不足"),


    ;

    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;

}
