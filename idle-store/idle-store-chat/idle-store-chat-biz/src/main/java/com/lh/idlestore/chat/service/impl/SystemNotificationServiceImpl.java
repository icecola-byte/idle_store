package com.lh.idlestore.chat.service.impl;

import com.lh.framework.common.exception.BizException;
import com.lh.framework.web.enums.CommonResponseCodeEnum;
import com.lh.idlestore.chat.constant.ChatConstants;
import com.lh.idlestore.chat.repository.mysql.dataobject.ChatMessageOutboxDO;
import com.lh.idlestore.chat.repository.mysql.dataobject.ChatSessionDO;
import com.lh.idlestore.chat.repository.mysql.dataobject.ChatSessionMemberDO;
import com.lh.idlestore.chat.repository.mysql.mapper.ChatMessageOutboxMapper;
import com.lh.idlestore.chat.repository.mysql.mapper.ChatSessionMapper;
import com.lh.idlestore.chat.repository.mysql.mapper.ChatSessionMemberMapper;
import com.lh.idlestore.chat.enums.ChatResponseCodeEnum;
import com.lh.idlestore.chat.enums.ChatSessionTypeEnum;
import com.lh.idlestore.chat.model.command.SendSystemNotificationCommand;
import com.lh.idlestore.chat.model.vo.response.SendMessageRespVO;
import com.lh.idlestore.chat.remote.DistributedIdGeneratorRemoteService;
import com.lh.idlestore.chat.remote.UserRemoteService;
import com.lh.idlestore.chat.service.SystemNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDateTime;
import com.lh.framework.common.util.JsonUtils;
import com.lh.idlestore.chat.enums.ChatMessageTypeEnum;
import com.lh.idlestore.chat.enums.ChatOutboxStatusEnum;
import com.lh.idlestore.chat.model.event.ChatMessageCreatedEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import java.time.Instant;
import java.time.ZoneId;


@Service
@RequiredArgsConstructor
public class SystemNotificationServiceImpl
        implements SystemNotificationService {

    private final ChatSessionMapper sessionMapper;
    private final ChatSessionMemberMapper memberMapper;
    private final ChatMessageOutboxMapper outboxMapper;
    private final DistributedIdGeneratorRemoteService distributedIdRemoteService;
    private final UserRemoteService userRemoteService;
    private final TransactionTemplate transactionTemplate;

    @Override
    public SendMessageRespVO sendNotification(
            SendSystemNotificationCommand command) {

        ChatMessageTypeEnum messageType = validate(command);

        // 系统通知的senderId固定为0。
        ChatMessageOutboxDO existing =
                outboxMapper.selectBySenderAndClientMessageId(
                        ChatConstants.SYSTEM_SENDER_ID,
                        command.getRequestId());

        if (existing != null) {
            return buildResponse(existing);
        }

        userRemoteService.checkUserExists(command.getReceiverId());

        ChatSessionDO systemSession =
                getOrCreateSystemSession(
                        command.getReceiverId());

        // 远程调用放在MySQL事务外。
        Long messageId =
                distributedIdRemoteService.nextId(
                        ChatConstants.MESSAGE_ID_KEY);

        Long eventId =
                distributedIdRemoteService.nextId(
                        ChatConstants.OUTBOX_EVENT_ID_KEY);

        try {
            return transactionTemplate.execute(status ->
                    writeNotification(
                            systemSession.getSessionId(),
                            command,
                            messageType,
                            messageId,
                            eventId));
        } catch (DuplicateKeyException exception) {
            // 并发请求使用相同requestId时，返回第一次结果。
            ChatMessageOutboxDO duplicate =
                    outboxMapper
                            .selectBySenderAndClientMessageId(
                                    ChatConstants.SYSTEM_SENDER_ID,
                                    command.getRequestId());

            if (duplicate != null) {
                return buildResponse(duplicate);
            }

            throw exception;
        }
    }

    /**
     * 创建或者获取系统通知 Session
     */
    private ChatSessionDO getOrCreateSystemSession(
            Long receiverId) {

        String sessionKey =
                ChatConstants.buildSystemSessionKey(receiverId);

        ChatSessionDO existing =
                sessionMapper.selectBySessionKey(sessionKey);

        if (existing != null) {
            return existing;
        }

        Long sessionId =
                distributedIdRemoteService.nextId(
                        ChatConstants.SYSTEM_SESSION_ID_KEY);

        LocalDateTime now = LocalDateTime.now();

        try {
            transactionTemplate.executeWithoutResult(status -> {
                ChatSessionDO session =
                        ChatSessionDO.builder()
                                .sessionId(sessionId)
                                .sessionType(
                                        ChatSessionTypeEnum.SYSTEM)
                                .sessionKey(sessionKey)
                                .lastSequence(0L)
                                .lastMessageId(null)
                                .lastMessage(null)
                                .lastMessageTime(null)
                                .createTime(now)
                                .updateTime(now)
                                .isDeleted(false)
                                .build();

                sessionMapper.insert(session);

                ChatSessionMemberDO member =
                        ChatSessionMemberDO.builder()
                                .sessionId(sessionId)
                                .userId(receiverId)
                                .unreadCount(0)
                                .lastReadSequence(0L)
                                .createTime(now)
                                .updateTime(now)
                                .isDeleted(false)
                                .build();

                memberMapper.insert(member);
            });
        } catch (DuplicateKeyException ignored) {
            // 另一个并发请求已经创建该用户的系统会话。
        }

        ChatSessionDO result =
                sessionMapper.selectBySessionKey(sessionKey);

        if (result == null) {
            throw new BizException(
                    ChatResponseCodeEnum.SEND_MESSAGE_FAILED);
        }

        return result;
    }

    private SendMessageRespVO writeNotification(
            Long sessionId,
            SendSystemNotificationCommand command,
            ChatMessageTypeEnum messageType,
            Long messageId,
            Long eventId) {

        // 查询并且上锁用于更新
        ChatSessionDO session =
                sessionMapper.selectByIdForUpdate(sessionId);

        if (session == null) {
            throw new BizException(
                    ChatResponseCodeEnum.SESSION_NOT_FOUND);
        }

        if (session.getSessionType()
                != ChatSessionTypeEnum.SYSTEM) {
            throw new BizException(
                    ChatResponseCodeEnum.SESSION_NOT_FOUND);
        }

        ChatSessionMemberDO receiver =
                memberMapper.selectBySessionAndUser(
                        sessionId,
                        command.getReceiverId());

        if (receiver == null) {
            throw new BizException(
                    ChatResponseCodeEnum.RECEIVER_NOT_FOUND);
        }

        long currentSequence =
                session.getLastSequence() == null
                        ? 0L
                        : session.getLastSequence();

        long newSequence = currentSequence + 1;

        Instant sendInstant = Instant.now();

        LocalDateTime sendTime =
                LocalDateTime.ofInstant(
                        sendInstant,
                        ZoneId.systemDefault());
        // 更新会话消息
        int sessionRows =
                sessionMapper.updateLastMessage(
                        sessionId,
                        newSequence,
                        messageId,
                        buildPreview(
                                messageType,
                                command.getContent()),
                        sendTime);

        if (sessionRows != 1) {
            throw new BizException(
                    ChatResponseCodeEnum.SEND_MESSAGE_FAILED);
        }
        // 更新未读消息数量
        int memberRows =
                memberMapper.increaseUnread(
                        sessionId,
                        command.getReceiverId());

        if (memberRows != 1) {
            throw new BizException(
                    ChatResponseCodeEnum.SEND_MESSAGE_FAILED);
        }
        // 消息存入 outbox
        ChatMessageCreatedEvent event =
                ChatMessageCreatedEvent.builder()
                        .eventId(eventId)
                        .messageId(messageId)
                        .sessionId(sessionId)
                        .sequence(newSequence)
                        .senderId(
                                ChatConstants.SYSTEM_SENDER_ID)
                        .receiverId(command.getReceiverId())
                        .messageType(
                                messageType.getValue()
                                        .byteValue())
                        .content(command.getContent())
                        .clientMessageId(
                                command.getRequestId())
                        .sendTime(sendInstant)
                        .build();

        ChatMessageOutboxDO outbox =
                ChatMessageOutboxDO.builder()
                        .eventId(eventId)
                        .messageId(messageId)
                        .sessionId(sessionId)
                        .sequence(newSequence)
                        .senderId(
                                ChatConstants.SYSTEM_SENDER_ID)
                        .receiverId(command.getReceiverId())
                        .clientMessageId(
                                command.getRequestId())
                        .eventType(
                                ChatConstants
                                        .MESSAGE_CREATED_EVENT)
                        .eventPayload(
                                JsonUtils.toJsonString(event))
                        .eventStatus(
                                ChatOutboxStatusEnum.PENDING)
                        .retryCount(0)
                        .nextRetryTime(sendTime)
                        .sentTime(null)
                        .createTime(sendTime)
                        .updateTime(sendTime)
                        .build();

        int outboxRows = outboxMapper.insert(outbox);

        if (outboxRows != 1) {
            throw new BizException(
                    ChatResponseCodeEnum.SEND_MESSAGE_FAILED);
        }

        return buildResponse(outbox);
    }

    private ChatMessageTypeEnum validate(
            SendSystemNotificationCommand command) {

        if (command == null
                || command.getReceiverId() == null
                || command.getReceiverId() <= 0) {
            throw new BizException(
                    CommonResponseCodeEnum.PARAM_NOT_VALID);
        }

        String requestId = command.getRequestId();

        if (StringUtils.isBlank(requestId)
                || requestId.length()
                > ChatConstants.CLIENT_MESSAGE_ID_MAX_LENGTH) {
            throw new BizException(
                    CommonResponseCodeEnum.PARAM_NOT_VALID);
        }

        ChatMessageTypeEnum messageType;

        try {
            messageType =
                    ChatMessageTypeEnum.fromValue(
                            command.getMessageType());
        } catch (IllegalArgumentException exception) {
            throw new BizException(
                    ChatResponseCodeEnum.MESSAGE_TYPE_INVALID);
        }

        String content = command.getContent();

        if (StringUtils.isBlank(content)) {
            throw new BizException(
                    ChatResponseCodeEnum
                            .MESSAGE_CONTENT_INVALID);
        }

        if (messageType == ChatMessageTypeEnum.TEXT
                && content.length()
                > ChatConstants.TEXT_MESSAGE_MAX_LENGTH) {
            throw new BizException(
                    ChatResponseCodeEnum
                            .MESSAGE_CONTENT_INVALID);
        }

        return messageType;
    }

    private String buildPreview(
            ChatMessageTypeEnum messageType,
            String content) {

        return switch (messageType) {
            case TEXT -> {
                int maxLength =
                        ChatConstants
                                .MESSAGE_PREVIEW_MAX_LENGTH;

                yield content.length() <= maxLength
                        ? content
                        : content.substring(0, maxLength);
            }
            case IMAGE -> "[图片]";
            case CARD -> "[系统通知]";
        };
    }

    private SendMessageRespVO buildResponse(
            ChatMessageOutboxDO outbox) {

        return SendMessageRespVO.builder()
                .messageId(outbox.getMessageId())
                .sessionId(outbox.getSessionId())
                .sequence(outbox.getSequence())
                .clientMessageId(
                        outbox.getClientMessageId())
                .sendTime(outbox.getCreateTime())
                .build();
    }
}
