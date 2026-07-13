package com.lh.idlestore.chat.service;

import com.lh.idlestore.chat.model.vo.request.SendMessageReqVO;
import com.lh.idlestore.chat.model.vo.response.SendMessageRespVO;

public interface ChatMessageService {

    SendMessageRespVO sendMessage(
            SendMessageReqVO request);
}
