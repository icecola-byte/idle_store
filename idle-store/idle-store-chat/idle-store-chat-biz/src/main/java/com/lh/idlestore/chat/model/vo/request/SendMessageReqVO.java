package com.lh.idlestore.chat.model.vo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户发送单聊消息请求。
 */
@Data
public class SendMessageReqVO {

    /**
     * 会话ID。
     */
    @NotNull(message = "会话ID不能为空")
    @Positive(message = "会话ID必须大于0")
    private Long sessionId;

    /**
     * 客户端生成的UUID。
     * 同一次发送重试时必须保持不变。
     */
    @NotBlank(message = "客户端消息ID不能为空")
    @Size(max = 64, message = "客户端消息ID不能超过64个字符")
    private String clientMessageId;

    /**
     * 消息类型：1-文本，2-图片，3-业务卡片。
     */
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;

    /**
     * 文本、OSS地址或者卡片JSON。
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;
}