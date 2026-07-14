package com.lh.idlestore.chat.controller;

import com.lh.framework.biz.operationlog.annotation.ApiOperationLog;
import com.lh.framework.common.response.Response;
import com.lh.idlestore.chat.model.vo.response.CreateDirectSessionRespVO;
import com.lh.idlestore.chat.model.vo.request.CreateDirectSessionReqVO;
import com.lh.idlestore.chat.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat/sessions")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    /**
     * 获取或创建单聊会话。
     */
    @PostMapping("/direct")
    @ApiOperationLog(description = "获取或创建单聊会话")
    public Response<CreateDirectSessionRespVO>
    getOrCreateDirectSession(
            @Validated @RequestBody
            CreateDirectSessionReqVO request) {

        return Response.success(chatSessionService
                .getOrCreateDirectSession(request));
    }
}
