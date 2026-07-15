package com.lh.idlestore.chat.service.impl;

import com.lh.framework.biz.context.holder.LoginUserContextHolder;
import com.lh.framework.common.exception.BizException;
import com.lh.framework.web.enums.CommonResponseCodeEnum;
import com.lh.idlestore.chat.repository.cassandra.entity.ChatMessageEntity;
import com.lh.idlestore.chat.repository.cassandra.entity.ChatMessagePrimaryKey;
import com.lh.idlestore.chat.repository.cassandra.repository.ChatMessageRepository;
import com.lh.idlestore.chat.repository.mysql.dataobject.ChatSessionDO;
import com.lh.idlestore.chat.repository.mysql.dataobject.ChatSessionMemberDO;
import com.lh.idlestore.chat.repository.mysql.mapper.ChatSessionMapper;
import com.lh.idlestore.chat.repository.mysql.mapper.ChatSessionMemberMapper;
import com.lh.idlestore.chat.enums.ChatResponseCodeEnum;
import com.lh.idlestore.chat.enums.ChatSessionTypeEnum;
import com.lh.idlestore.chat.repository.mysql.projection.ChatSessionSummaryProjection;
import com.lh.idlestore.chat.model.vo.response.ChatMessagePageVO;
import com.lh.idlestore.chat.model.vo.response.ChatMessageVO;
import com.lh.idlestore.chat.model.vo.response.ChatSessionListItemVO;
import com.lh.idlestore.chat.model.vo.request.MarkSessionReadReqVO;
import com.lh.idlestore.chat.remote.UserRemoteService;
import com.lh.idlestore.chat.service.ChatQueryService;
import com.lh.idlestore.user.dto.response.UserBriefResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChatQueryServiceImpl implements ChatQueryService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final ChatSessionMapper sessionMapper;
    private final ChatSessionMemberMapper memberMapper;
    private final ChatMessageRepository messageRepository;
    private final UserRemoteService userRemoteService;
    private final TransactionTemplate transactionTemplate;

    @Override
    public List<ChatSessionListItemVO> listSessions() {
        Long userId = requireCurrentUserId();
        List<ChatSessionSummaryProjection> summaries = memberMapper.selectSessionSummaries(userId);
        Map<Long, UserBriefResponse> users = userRemoteService.findByIds(
                summaries.stream().map(ChatSessionSummaryProjection::getPeerUserId)
                        .filter(Objects::nonNull).toList());
        return summaries.stream()
                .map(summary -> toSessionVO(summary, users.get(summary.getPeerUserId())))
                .toList();
    }

    @Override
    public ChatMessagePageVO getHistory(
            Long sessionId, Long beforeSequence, Integer requestedLimit) {
        Long userId = requireCurrentUserId();
        ChatSessionDO session = requireAccessibleSession(sessionId, userId);
        int limit = normalizeLimit(requestedLimit);
        long latestExclusive = safeLastSequence(session) + 1;
        long cursor = beforeSequence == null ? latestExclusive
                : Math.min(beforeSequence, latestExclusive);
        if (cursor < 0) {
            throw new BizException(CommonResponseCodeEnum.PARAM_NOT_VALID);
        }
        List<ChatMessageVO> messages = loadHistory(sessionId, cursor, limit)
                .stream().map(this::toMessageVO).toList();
        Long next = messages.isEmpty() ? null : messages.get(messages.size() - 1).getSequence();
        return ChatMessagePageVO.builder()
                .messages(messages)
                .nextSequence(next)
                .hasMore(next != null && next > 1 && messages.size() == limit)
                .build();
    }

    @Override
    public ChatMessagePageVO syncMessages(
            Long sessionId, Long afterSequence, Integer requestedLimit) {
        Long userId = requireCurrentUserId();
        ChatSessionDO session = requireAccessibleSession(sessionId, userId);
        long latest = safeLastSequence(session);
        long cursor = afterSequence == null ? 0L : afterSequence;
        if (cursor < 0 || cursor > latest) {
            throw new BizException(CommonResponseCodeEnum.PARAM_NOT_VALID);
        }
        int limit = normalizeLimit(requestedLimit);
        List<ChatMessageVO> messages = loadAfter(sessionId, cursor, latest, limit)
                .stream().map(this::toMessageVO).toList();
        Long next = messages.isEmpty() ? cursor : messages.get(messages.size() - 1).getSequence();
        return ChatMessagePageVO.builder()
                .messages(messages).nextSequence(next).hasMore(next < latest).build();
    }

    @Override
    public boolean markRead(Long sessionId, MarkSessionReadReqVO request) {
        if (sessionId == null || sessionId <= 0 || request == null
                || request.getLastReadSequence() == null) {
            throw new BizException(CommonResponseCodeEnum.PARAM_NOT_VALID);
        }
        Long userId = requireCurrentUserId();
        Boolean updated = transactionTemplate.execute(status -> {
            ChatSessionDO session = sessionMapper.selectByIdForUpdate(sessionId);
            if (session == null) {
                throw new BizException(ChatResponseCodeEnum.SESSION_NOT_FOUND);
            }
            ChatSessionMemberDO member = memberMapper.selectBySessionAndUserForUpdate(sessionId, userId);
            if (member == null) {
                throw new BizException(ChatResponseCodeEnum.NOT_SESSION_MEMBER);
            }
            long currentLast = safeLastSequence(session);
            long acknowledged = request.getLastReadSequence();
            if (acknowledged < 0 || acknowledged > currentLast) {
                throw new BizException(CommonResponseCodeEnum.PARAM_NOT_VALID);
            }
            return memberMapper.markRead(sessionId, userId, acknowledged, currentLast) == 1;
        });
        return Boolean.TRUE.equals(updated);
    }

    private List<ChatMessageEntity> loadHistory(Long sessionId, long beforeExclusive, int limit) {
        List<ChatMessageEntity> result = new ArrayList<>(limit);
        if (beforeExclusive <= 1) {
            return result;
        }
        int bucket = ChatMessagePrimaryKey.calculateBucket(beforeExclusive - 1);
        long cursor = beforeExclusive;
        while (bucket >= 0 && result.size() < limit) {
            result.addAll(messageRepository.findHistory(
                    sessionId, bucket, cursor, limit - result.size()));
            bucket--;
            if (bucket >= 0) {
                cursor = ChatMessagePrimaryKey.lastSequence(bucket) + 1;
            }
        }
        return result;
    }

    private List<ChatMessageEntity> loadAfter(
            Long sessionId, long afterExclusive, long latestSequence, int limit) {
        List<ChatMessageEntity> result = new ArrayList<>(limit);
        if (afterExclusive >= latestSequence) {
            return result;
        }
        int bucket = ChatMessagePrimaryKey.calculateBucket(afterExclusive + 1);
        int lastBucket = ChatMessagePrimaryKey.calculateBucket(latestSequence);
        long cursor = afterExclusive;
        while (bucket <= lastBucket && result.size() < limit) {
            result.addAll(messageRepository.findAfterSequence(
                    sessionId, bucket, cursor, limit - result.size()));
            bucket++;
            if (bucket <= lastBucket) {
                cursor = ChatMessagePrimaryKey.firstSequence(bucket) - 1;
            }
        }
        return result;
    }

    private ChatSessionDO requireAccessibleSession(Long sessionId, Long userId) {
        if (sessionId == null || sessionId <= 0) {
            throw new BizException(CommonResponseCodeEnum.PARAM_NOT_VALID);
        }
        if (memberMapper.selectBySessionAndUser(sessionId, userId) == null) {
            throw new BizException(ChatResponseCodeEnum.NOT_SESSION_MEMBER);
        }
        ChatSessionDO session = sessionMapper.selectById(sessionId);
        if (session == null || Boolean.TRUE.equals(session.getIsDeleted())) {
            throw new BizException(ChatResponseCodeEnum.SESSION_NOT_FOUND);
        }
        return session;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_PAGE_SIZE;
        }
        if (limit <= 0 || limit > MAX_PAGE_SIZE) {
            throw new BizException(CommonResponseCodeEnum.PARAM_NOT_VALID);
        }
        return limit;
    }

    private long safeLastSequence(ChatSessionDO session) {
        return session.getLastSequence() == null ? 0L : session.getLastSequence();
    }

    private Long requireCurrentUserId() {
        Long userId = LoginUserContextHolder.getUserId();
        if (userId == null) {
            throw new IllegalStateException("用户上下文缺失");
        }
        return userId;
    }

    private ChatSessionListItemVO toSessionVO(
            ChatSessionSummaryProjection summary, UserBriefResponse peer) {
        boolean system = Objects.equals(
                summary.getSessionType(), ChatSessionTypeEnum.SYSTEM.getValue());
        return ChatSessionListItemVO.builder()
                .sessionId(summary.getSessionId())
                .sessionType(summary.getSessionType())
                .peerUserId(summary.getPeerUserId())
                .displayName(system ? "系统通知" : peer == null ? "用户已注销" : peer.getUsername())
                .displayAvatarUrl(system || peer == null ? null : peer.getAvatarUrl())
                .lastSequence(summary.getLastSequence())
                .lastMessage(summary.getLastMessage())
                .lastMessageTime(summary.getLastMessageTime())
                .unreadCount(summary.getUnreadCount())
                .lastReadSequence(summary.getLastReadSequence())
                .build();
    }

    private ChatMessageVO toMessageVO(ChatMessageEntity message) {
        return ChatMessageVO.builder()
                .messageId(message.getMessageId())
                .sessionId(message.getKey().getSessionId())
                .sequence(message.getKey().getSequence())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .messageType(message.getMessageType() == null ? null : message.getMessageType().intValue())
                .content(message.getContent())
                .clientMessageId(message.getClientMessageId())
                .sendTime(message.getSendTime())
                .recalled(message.getRecalled())
                .build();
    }
}
