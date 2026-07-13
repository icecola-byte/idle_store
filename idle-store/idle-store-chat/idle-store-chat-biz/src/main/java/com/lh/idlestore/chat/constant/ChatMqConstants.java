package com.lh.idlestore.chat.constant;

public final class ChatMqConstants {

    private ChatMqConstants() {
    }

    public static final String MESSAGE_TOPIC =
            "IDLE_STORE_CHAT_MESSAGE";

    public static final String MESSAGE_CREATED_TAG =
            "MESSAGE_CREATED";

    public static final String PRODUCER_GROUP =
            "idle-store-chat-producer";

    public static final String MESSAGE_PERSIST_CONSUMER_GROUP =
            "idle-store-chat-message-persist-consumer";

    public static final int SEND_TIMEOUT_MILLIS = 3000;

    public static final int PUBLISH_BATCH_SIZE = 50;
}
