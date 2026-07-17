package com.lh.idlestore.chat.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lh.idlestore.chat.model.ws.ChatPushMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketSessionRegistry {

    private static final int SEND_TIME_LIMIT_MILLIS = 5_000;
    private static final int BUFFER_SIZE_LIMIT_BYTES = 512 * 1024;

    private final ObjectMapper objectMapper;
    private final Map<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public void register(Long userId, WebSocketSession session) {
        WebSocketSession safeSession = new ConcurrentWebSocketSessionDecorator(
                session, SEND_TIME_LIMIT_MILLIS, BUFFER_SIZE_LIMIT_BYTES);
        sessions.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet())
                .add(safeSession);
    }

    public void remove(Long userId, String sessionId) {
        Set<WebSocketSession> userSessions = sessions.get(userId);
        if (userSessions == null) {
            return;
        }
        userSessions.removeIf(session -> session.getId().equals(sessionId) || !session.isOpen());
        if (userSessions.isEmpty()) {
            sessions.remove(userId, userSessions);
        }
    }

    public boolean push(Long userId, ChatPushMessage<?> message) {
        Set<WebSocketSession> userSessions = sessions.get(userId);
        if (userSessions == null || userSessions.isEmpty()) {
            return false;
        }
        String payload;
        try {
            payload = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException exception) {
            log.error("序列化WebSocket消息失败，userId={}", userId, exception);
            return false;
        }

        boolean pushed = false;
        for (WebSocketSession session : userSessions) {
            if (!session.isOpen()) {
                continue;
            }
            try {
                session.sendMessage(new TextMessage(payload));
                pushed = true;
            } catch (IOException | RuntimeException exception) {
                log.warn("WebSocket推送失败，userId={}，sessionId={}",
                        userId, session.getId(), exception);
                tryClose(session);
            }
        }
        userSessions.removeIf(session -> !session.isOpen());
        return pushed;
    }

    private void tryClose(WebSocketSession session) {
        try {
            session.close();
        } catch (IOException ignored) {
            // 连接已不可用，无需继续处理。
        }
    }
}
