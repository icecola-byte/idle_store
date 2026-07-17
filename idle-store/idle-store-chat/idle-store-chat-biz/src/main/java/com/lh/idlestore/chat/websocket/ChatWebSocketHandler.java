package com.lh.idlestore.chat.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatWebSocketSessionRegistry sessionRegistry;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        sessionRegistry.register(userId, session);
        log.info("WebSocket连接建立，userId={}，sessionId={}", userId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
            throws Exception {
        if ("PING".equalsIgnoreCase(message.getPayload())) {
            session.sendMessage(new TextMessage("PONG"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserId(session);
        if (userId != null) {
            sessionRegistry.remove(userId, session.getId());
        }
        log.info("WebSocket连接关闭，userId={}，sessionId={}，status={}",
                userId, session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception)
            throws Exception {
        Long userId = getUserId(session);
        if (userId != null) {
            sessionRegistry.remove(userId, session.getId());
        }
        log.warn("WebSocket传输异常，userId={}，sessionId={}",
                userId, session.getId(), exception);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    private Long getUserId(WebSocketSession session) {
        Object value = session.getAttributes().get(
                ChatWebSocketHandshakeInterceptor.USER_ID_ATTRIBUTE);
        return value instanceof Long ? (Long) value : null;
    }
}
