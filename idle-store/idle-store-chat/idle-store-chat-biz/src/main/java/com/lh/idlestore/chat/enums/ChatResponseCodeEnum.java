package com.lh.idlestore.chat.enums;

import com.lh.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatResponseCodeEnum implements BaseExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SESSION_NOT_FOUND("CHAT-20001", "会话不存在"),
    NOT_SESSION_MEMBER("CHAT-20002", "你不是该会话成员"),
    RECEIVER_NOT_FOUND("CHAT-20003", "消息接收方不存在"),
    MESSAGE_TYPE_INVALID("CHAT-20004", "消息类型错误"),
    MESSAGE_CONTENT_INVALID("CHAT-20005", "消息内容错误"),
    CREATE_SESSION_FAILED("CHAT-20006", "创建聊天会话失败"),
    TARGET_USER_NOT_FOUND("CHAT-20007", "目标用户不存在"),
    GENERATE_ID_FAILED("CHAT-30001", "生成消息ID失败"),
    SEND_MESSAGE_FAILED("CHAT-30002", "消息发送失败"),
    USER_SERVICE_CALL_FAILED("CHAT-30003", "用户服务调用失败");


    private final String errorCode;
    private final String errorMessage;
}
