package com.lh.idlestore.chat.constant;

public final class ChatConstants {

    private ChatConstants() {
    }

    public static final String MESSAGE_ID_KEY = "chat_message";

    public static final String OUTBOX_EVENT_ID_KEY = "chat_outbox_event";

    public static final String MESSAGE_CREATED_EVENT =
            "CHAT_MESSAGE_CREATED";

    public static final String MESSAGE_PERSIST_CONSUMER_GROUP =
            "idle-store-chat-message-persist-consumer";

    public static final int CLIENT_MESSAGE_ID_MAX_LENGTH = 64;

    public static final int TEXT_MESSAGE_MAX_LENGTH = 2000;

    public static final int MESSAGE_PREVIEW_MAX_LENGTH = 500;

    public static final Long SYSTEM_SENDER_ID = 0L;

    public static final String SYSTEM_SESSION_ID_KEY =
            "chat_system_session";

    public static final String DIRECT_SESSION_ID_KEY =
            "chat_direct_session";

    public static String buildDirectSessionKey(
            Long firstUserId,
            Long secondUserId) {

        long smaller = Math.min(firstUserId, secondUserId);
        long larger = Math.max(firstUserId, secondUserId);

        return "DIRECT:" + smaller + ":" + larger;
    }

    public static String buildSystemSessionKey(Long userId) {
        return "SYSTEM:" + userId;
    }
}
