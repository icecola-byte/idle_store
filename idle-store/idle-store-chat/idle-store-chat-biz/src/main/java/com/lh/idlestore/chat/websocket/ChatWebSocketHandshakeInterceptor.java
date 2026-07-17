package com.lh.idlestore.chat.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class ChatWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    public static final String USER_ID_ATTRIBUTE = "chatUserId";

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        String userIdHeader = request.getHeaders().getFirst("userId");
        if (userIdHeader == null || userIdHeader.isBlank()) {
            return false;
        }
        try {
            long userId = Long.parseLong(userIdHeader);
            if (userId <= 0) {
                return false;
            }
            attributes.put(USER_ID_ATTRIBUTE, userId);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // 无需清理，连接关闭时由 Handler 移除会话。
    }
}
