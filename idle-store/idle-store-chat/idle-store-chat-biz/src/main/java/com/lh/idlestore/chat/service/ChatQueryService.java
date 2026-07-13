package com.lh.idlestore.chat.service;

import com.lh.idlestore.chat.model.vo.response.ChatMessagePageVO;
import com.lh.idlestore.chat.model.vo.response.ChatSessionListItemVO;
import com.lh.idlestore.chat.model.vo.request.MarkSessionReadReqVO;

import java.util.List;

public interface ChatQueryService {

    List<ChatSessionListItemVO> listSessions();

    ChatMessagePageVO getHistory(Long sessionId, Long beforeSequence, Integer limit);

    ChatMessagePageVO syncMessages(Long sessionId, Long afterSequence, Integer limit);

    boolean markRead(Long sessionId, MarkSessionReadReqVO request);
}
