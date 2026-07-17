package com.lh.idlestore.chat.websocket;

import com.lh.idlestore.chat.model.event.ChatMessageCreatedEvent;
import com.lh.idlestore.chat.model.vo.response.ChatMessageVO;
import com.lh.idlestore.chat.model.ws.ChatPushMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatWebSocketPushService {

    private final ChatWebSocketSessionRegistry sessionRegistry;

    public boolean pushMessage(ChatMessageCreatedEvent event) {
        ChatMessageVO message = ChatMessageVO.builder()
                .messageId(event.getMessageId())
                .sessionId(event.getSessionId())
                .sequence(event.getSequence())
                .senderId(event.getSenderId())
                .receiverId(event.getReceiverId())
                .messageType(event.getMessageType() == null
                        ? null : event.getMessageType().intValue())
                .content(event.getContent())
                .clientMessageId(event.getClientMessageId())
                .sendTime(event.getSendTime())
                .recalled(false)
                .build();
        return sessionRegistry.push(event.getReceiverId(),
                ChatPushMessage.builder().type("CHAT_MESSAGE").data(message).build());
    }
}
