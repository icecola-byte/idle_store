package com.lh.idlestore.chat.controller;

import com.lh.framework.biz.operationlog.annotation.ApiOperationLog;
import com.lh.framework.common.response.Response;
import com.lh.idlestore.chat.model.vo.request.SendMessageReqVO;
import com.lh.idlestore.chat.model.vo.response.SendMessageRespVO;
import com.lh.idlestore.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat/messages")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    /**
     * 发送单聊消息。
     */
    @PostMapping
    @ApiOperationLog(description = "发送单聊消息")
    public Response<SendMessageRespVO> sendMessage(
            @Validated @RequestBody
            SendMessageReqVO request) {

        return Response.success(chatMessageService.sendMessage(request));
    }
}
