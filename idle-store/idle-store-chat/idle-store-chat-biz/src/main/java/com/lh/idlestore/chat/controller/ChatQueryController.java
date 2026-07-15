package com.lh.idlestore.chat.controller;

import com.lh.framework.common.response.Response;
import com.lh.idlestore.chat.model.vo.response.ChatMessagePageVO;
import com.lh.idlestore.chat.model.vo.response.ChatSessionListItemVO;
import com.lh.idlestore.chat.model.vo.request.MarkSessionReadReqVO;
import com.lh.idlestore.chat.service.ChatQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatQueryController {

    private final ChatQueryService chatQueryService;

    @GetMapping("/sessions")
    public Response<List<ChatSessionListItemVO>> listSessions() {
        return Response.success(chatQueryService.listSessions());
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public Response<ChatMessagePageVO> getHistory(
            @PathVariable Long sessionId,
            @RequestParam(required = false) Long beforeSequence,
            @RequestParam(required = false) Integer limit) {
        return Response.success(chatQueryService.getHistory(sessionId, beforeSequence, limit));
    }

    @GetMapping("/sessions/{sessionId}/messages/sync")
    public Response<ChatMessagePageVO> syncMessages(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "0") Long afterSequence,
            @RequestParam(required = false) Integer limit) {
        return Response.success(chatQueryService.syncMessages(sessionId, afterSequence, limit));
    }

    @PostMapping("/sessions/{sessionId}/read")
    public Response<Boolean> markRead(
            @PathVariable Long sessionId,
            @Validated @RequestBody MarkSessionReadReqVO request) {
        return Response.success(chatQueryService.markRead(sessionId, request));
    }
}
