package com.lh.idlestore.chat.service.impl;

import com.lh.framework.biz.context.holder.LoginUserContextHolder;
import com.lh.framework.common.exception.BizException;
import com.lh.framework.common.util.JsonUtils;
import com.lh.framework.web.enums.CommonResponseCodeEnum;
import com.lh.idlestore.chat.constant.ChatConstants;
import com.lh.idlestore.chat.repository.mysql.dataobject.ChatMessageOutboxDO;
import com.lh.idlestore.chat.repository.mysql.dataobject.ChatSessionDO;
import com.lh.idlestore.chat.repository.mysql.dataobject.ChatSessionMemberDO;
import com.lh.idlestore.chat.repository.mysql.mapper.ChatMessageOutboxMapper;
import com.lh.idlestore.chat.repository.mysql.mapper.ChatSessionMapper;
import com.lh.idlestore.chat.repository.mysql.mapper.ChatSessionMemberMapper;
import com.lh.idlestore.chat.enums.ChatMessageTypeEnum;
import com.lh.idlestore.chat.enums.ChatOutboxStatusEnum;
import com.lh.idlestore.chat.enums.ChatResponseCodeEnum;
import com.lh.idlestore.chat.enums.ChatSessionTypeEnum;
import com.lh.idlestore.chat.model.event.ChatMessageCreatedEvent;
import com.lh.idlestore.chat.model.vo.request.SendMessageReqVO;
import com.lh.idlestore.chat.model.vo.response.SendMessageRespVO;
import com.lh.idlestore.chat.remote.DistributedIdGeneratorRemoteService;
import com.lh.idlestore.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatSessionMemberMapper memberMapper;
    private final ChatMessageOutboxMapper outboxMapper;
    private final DistributedIdGeneratorRemoteService distributedIdRemoteService;
    private final TransactionTemplate transactionTemplate;

    @Override
    public SendMessageRespVO sendMessage(
            SendMessageReqVO request) {

        Long senderId = LoginUserContextHolder.getUserId();
        if (senderId == null) {
            throw new IllegalStateException("用户上下文缺失");
        }
        // 校验参数
        ChatMessageTypeEnum messageType = validate(request);

        // 先检查幂等，避免客户端重试时重复发送。
        // 避免用户请求发送信息后，后端报存，但是后端返回结果给前端时信息丢失，导致前端重试再次发送
        // 再次发送时 clientMessageId 还是同一个，后端就可以根据这个避免重复存入
        ChatMessageOutboxDO existing =
                outboxMapper.selectBySenderAndClientMessageId(
                        senderId, request.getClientMessageId());

        if (existing != null) {
            return buildResponse(existing);
        }

        // 可以发送了，远程调用生成分布式ID
        Long messageId = distributedIdRemoteService.nextId(
                ChatConstants.MESSAGE_ID_KEY);

        Long eventId = distributedIdRemoteService.nextId(
                ChatConstants.OUTBOX_EVENT_ID_KEY);

        try {
            SendMessageRespVO response =
                    transactionTemplate.execute(status ->
                            executeSendTransaction(
                                    request,
                                    messageType,
                                    senderId,
                                    messageId,
                                    eventId)
                    );

            return response;
        } catch (DuplicateKeyException exception) {
            // 处理两个相同clientMessageId请求并发进入的情况。
            ChatMessageOutboxDO duplicate =
                    outboxMapper.selectBySenderAndClientMessageId(
                            senderId, request.getClientMessageId());

            if (duplicate != null) {
                return buildResponse(duplicate);
            }
            throw exception;
        }
    }

    private ChatMessageTypeEnum validate(SendMessageReqVO request) {
        // 会话ID是不是在范围内
        if (request == null
                || request.getSessionId() == null
                || request.getSessionId() <= 0) {
            throw new BizException(
                    CommonResponseCodeEnum.PARAM_NOT_VALID);
        }

        String clientMessageId = request.getClientMessageId();
        // 客户端生成的 UUID
        if (StringUtils.isBlank(clientMessageId)
                || clientMessageId.length()
                > ChatConstants.CLIENT_MESSAGE_ID_MAX_LENGTH) {
            throw new BizException(
                    CommonResponseCodeEnum.PARAM_NOT_VALID);
        }
        // 消息类型对不对
        ChatMessageTypeEnum messageType;
        try {
            messageType = ChatMessageTypeEnum.fromValue(
                    request.getMessageType());
        } catch (IllegalArgumentException exception) {
            throw new BizException(
                    ChatResponseCodeEnum.MESSAGE_TYPE_INVALID);
        }
        // 消息内容不能为空
        String content = request.getContent();
        if (StringUtils.isBlank(content)) {
            throw new BizException(
                    ChatResponseCodeEnum.MESSAGE_CONTENT_INVALID);
        }
        // 不同消息类型有不同的内容最大长度限制
        if (messageType == ChatMessageTypeEnum.TEXT
                && content.length()
                > ChatConstants.TEXT_MESSAGE_MAX_LENGTH) {
            throw new BizException(
                    ChatResponseCodeEnum.MESSAGE_CONTENT_INVALID);
        }
        // 图片也有长度限制 TODO 加上限制必须是自己的图片 OSS 地址
        if (messageType == ChatMessageTypeEnum.IMAGE
                && content.length() > 2048) {
            throw new BizException(
                    ChatResponseCodeEnum.MESSAGE_CONTENT_INVALID);
        }

        if (messageType == ChatMessageTypeEnum.CARD
                && content.length() > 4000) {
            throw new BizException(
                    ChatResponseCodeEnum.MESSAGE_CONTENT_INVALID);
        }

        return messageType;
    }

    private SendMessageRespVO executeSendTransaction(
            SendMessageReqVO request,
            ChatMessageTypeEnum messageType,
            Long senderId,
            Long messageId,
            Long eventId) {
        // select ... for update 来对这个数据上锁
        ChatSessionDO session =
                chatSessionMapper.selectByIdForUpdate(
                        request.getSessionId());

        if (session == null) {
            throw new BizException(
                    ChatResponseCodeEnum.SESSION_NOT_FOUND);
        }
        // 发送的只能是单聊信息，不能是系统消息
        if (session.getSessionType()
                != ChatSessionTypeEnum.DIRECT) {
            throw new BizException(
                    ChatResponseCodeEnum.SESSION_NOT_FOUND);
        }
        // 发送方，也就是自己
        ChatSessionMemberDO senderMember =
                memberMapper.selectBySessionAndUser(
                        session.getSessionId(), senderId);

        if (senderMember == null) {
            throw new BizException(
                    ChatResponseCodeEnum.NOT_SESSION_MEMBER);
        }
        // 接收方
        ChatSessionMemberDO receiverMember =
                memberMapper.selectOtherMember(
                        session.getSessionId(), senderId);

        if (receiverMember == null) {
            throw new BizException(
                    ChatResponseCodeEnum.RECEIVER_NOT_FOUND);
        }
        // 会话中存储的最新消息序列化 + 1
        long sequence = session.getLastSequence() + 1;
        Instant instant = Instant.now();
        LocalDateTime sendTime = LocalDateTime.ofInstant(
                instant, ZoneId.systemDefault());
        // 更新会话信息(最新消息序列号,最后一条消息,消息摘要,发送时间)
        int sessionRows = chatSessionMapper.updateLastMessage(
                session.getSessionId(),
                sequence,
                messageId,
                buildPreview(messageType, request.getContent()),
                sendTime);

        if (sessionRows != 1) {
            throw new BizException(
                    ChatResponseCodeEnum.SEND_MESSAGE_FAILED);
        }
        // 接收方更新未读数量
        int memberRows = memberMapper.increaseUnread(
                session.getSessionId(),
                receiverMember.getUserId());

        if (memberRows != 1) {
            throw new BizException(
                    ChatResponseCodeEnum.SEND_MESSAGE_FAILED);
        }
        // 消息存储事件(发送的消息内容存储)
        ChatMessageCreatedEvent event =
                ChatMessageCreatedEvent.builder()
                        .eventId(eventId)
                        .messageId(messageId)
                        .sessionId(session.getSessionId())
                        .sequence(sequence)
                        .senderId(senderId)
                        .receiverId(receiverMember.getUserId())
                        .messageType(
                                messageType.getValue().byteValue())
                        .content(request.getContent())
                        .clientMessageId(
                                request.getClientMessageId())
                        .sendTime(instant)
                        .build();

        ChatMessageOutboxDO outbox =
                ChatMessageOutboxDO.builder()
                        .eventId(eventId)
                        .messageId(messageId)
                        .sessionId(session.getSessionId())
                        .sequence(sequence)
                        .senderId(senderId)
                        .receiverId(receiverMember.getUserId())
                        .clientMessageId(
                                request.getClientMessageId())
                        .eventType(
                                ChatConstants.MESSAGE_CREATED_EVENT)
                        .eventPayload(JsonUtils.toJsonString(event))
                        .eventStatus(ChatOutboxStatusEnum.PENDING)
                        .retryCount(0)
                        .nextRetryTime(sendTime)
                        .createTime(sendTime)
                        .updateTime(sendTime)
                        .build();

        outboxMapper.insert(outbox);

        return buildResponse(outbox);
    }

    private SendMessageRespVO buildResponse(
            ChatMessageOutboxDO outbox) {

        return SendMessageRespVO.builder()
                .messageId(outbox.getMessageId())
                .sessionId(outbox.getSessionId())
                .sequence(outbox.getSequence())
                .clientMessageId(outbox.getClientMessageId())
                .sendTime(outbox.getCreateTime())
                .build();
    }

    private String buildPreview(
            ChatMessageTypeEnum type, String content) {

        return switch (type) {
            case TEXT -> content.length() <= ChatConstants.MESSAGE_PREVIEW_MAX_LENGTH
                    ? content : content.substring(0, ChatConstants.MESSAGE_PREVIEW_MAX_LENGTH);
            case IMAGE -> "[图片]";
            case CARD -> "[业务卡片]";
        };
    }
}
