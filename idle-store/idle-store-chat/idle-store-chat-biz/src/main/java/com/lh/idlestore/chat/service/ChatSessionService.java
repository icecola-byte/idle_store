package com.lh.idlestore.chat.service;

import com.lh.idlestore.chat.model.vo.response.CreateDirectSessionRespVO;
import com.lh.idlestore.chat.model.vo.request.CreateDirectSessionReqVO;

public interface ChatSessionService {

    CreateDirectSessionRespVO getOrCreateDirectSession(
            CreateDirectSessionReqVO request);
}
