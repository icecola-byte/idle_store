package com.lh.idlestore.user.enums;

import com.lh.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * User 服务对外公开的稳定错误码契约。
 */
@Getter
@AllArgsConstructor
public enum UserResponseCodeEnum implements BaseExceptionInterface {

    NICK_NAME_VALID_FAIL("USER-20001", "昵称请设置2-12个字符，不能使用@《/等特殊字符"),
    PHONE_VALID_FAIL("USER-20002", "手机号格式不正确"),
    SEX_VALID_FAIL("USER-20003", "性别错误"),
    REGISTER_FAIL("USER-20004", "注册失败"),
    UPLOAD_AVATAR_FAIL("USER-20005", "头像上传失败"),
    PHONE_ALREADY_EXISTS("USER-20006", "该手机号已被其他用户使用"),
    USER_NOT_FOUND("USER-20007", "该用户不存在"),

    OSS_SERVICE_CALL_FAILED("USER-30001", "文件服务调用失败");

    private final String errorCode;
    private final String errorMessage;
}
