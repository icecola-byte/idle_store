package com.lh.idlestore.chat.model.command;

import lombok.Data;

@Data
public class SendSystemNotificationCommand {

    private Long receiverId;

    /**
     * 调用方生成的全局唯一业务请求ID。
     */
    private String requestId;

    private Integer messageType;

    private String content;
}