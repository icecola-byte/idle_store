package com.lh.idlestore.chat.service.impl;

import com.lh.framework.biz.context.holder.LoginUserContextHolder;
import com.lh.framework.common.exception.BizException;
import com.lh.framework.web.enums.CommonResponseCodeEnum;
import com.lh.idlestore.chat.constant.ChatConstants;
import com.lh.idlestore.chat.repository.mysql.dataobject.ChatSessionDO;
import com.lh.idlestore.chat.repository.mysql.dataobject.ChatSessionMemberDO;
import com.lh.idlestore.chat.repository.mysql.mapper.ChatSessionMapper;
import com.lh.idlestore.chat.repository.mysql.mapper.ChatSessionMemberMapper;
import com.lh.idlestore.chat.enums.ChatResponseCodeEnum;
import com.lh.idlestore.chat.enums.ChatSessionTypeEnum;
import com.lh.idlestore.chat.model.vo.response.CreateDirectSessionRespVO;
import com.lh.idlestore.chat.model.vo.request.CreateDirectSessionReqVO;
import com.lh.idlestore.chat.remote.DistributedIdGeneratorRemoteService;
import com.lh.idlestore.chat.remote.UserRemoteService;
import com.lh.idlestore.chat.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl
        implements ChatSessionService {

    private final ChatSessionMapper sessionMapper;

    private final ChatSessionMemberMapper memberMapper;

    private final DistributedIdGeneratorRemoteService distributedIdRemoteService;

    private final UserRemoteService userRemoteService;

    private final TransactionTemplate transactionTemplate;

    @Override
    public CreateDirectSessionRespVO
    getOrCreateDirectSession(
            CreateDirectSessionReqVO request) {

        Long currentUserId =
                LoginUserContextHolder.getUserId();

        if (currentUserId == null) {
            throw new IllegalStateException(
                    "用户上下文缺失");
        }

        Long targetUserId = validateAndGetTargetUserId(
                request, currentUserId);

        userRemoteService.checkUserExists(targetUserId);

        String sessionKey =
                ChatConstants.buildDirectSessionKey(
                        currentUserId,
                        targetUserId);

        // 先查询是否已经存在会话。
        ChatSessionDO existing =
                sessionMapper.selectBySessionKey(
                        sessionKey);

        if (existing != null) {
            return buildResponse(
                    existing,
                    targetUserId);
        }

        // 远程生成ID必须放在MySQL事务外。
        Long sessionId =
                distributedIdRemoteService.nextId(
                        ChatConstants
                                .DIRECT_SESSION_ID_KEY);

        try {
            ChatSessionDO session =
                    transactionTemplate.execute(status ->
                            createSessionInTransaction(
                                    sessionId,
                                    sessionKey,
                                    currentUserId,
                                    targetUserId));

            if (session == null) {
                throw new BizException(
                        ChatResponseCodeEnum
                                .CREATE_SESSION_FAILED);
            }

            return buildResponse(
                    session,
                    targetUserId);
        } catch (DuplicateKeyException exception) {
            /*
             * 两个用户可能同时点击发消息。
             * session_key唯一索引只能允许一个事务创建成功。
             * 另一个事务发生唯一键冲突后，重新查询即可。
             */
            ChatSessionDO concurrentCreated =
                    sessionMapper.selectBySessionKey(
                            sessionKey);

            if (concurrentCreated == null) {
                throw new BizException(
                        ChatResponseCodeEnum
                                .CREATE_SESSION_FAILED);
            }

            return buildResponse(
                    concurrentCreated,
                    targetUserId);
        }
    }

    private ChatSessionDO createSessionInTransaction(
            Long sessionId,
            String sessionKey,
            Long currentUserId,
            Long targetUserId) {

        /*
         * 第一次查询之后，其他请求可能已经创建会话。
         * 所以进入事务后再检查一次。
         */
        ChatSessionDO existing =
                sessionMapper.selectBySessionKey(
                        sessionKey);

        if (existing != null) {
            return existing;
        }

        LocalDateTime now = LocalDateTime.now();

        ChatSessionDO session =
                ChatSessionDO.builder()
                        .sessionId(sessionId)
                        .sessionType(
                                ChatSessionTypeEnum.DIRECT)
                        .sessionKey(sessionKey)
                        .lastSequence(0L)
                        .lastMessageId(null)
                        .lastMessage(null)
                        .lastMessageTime(null)
                        .createTime(now)
                        .updateTime(now)
                        .isDeleted(false)
                        .build();

        int sessionRows = sessionMapper.insert(session);

        if (sessionRows != 1) {
            throw new BizException(
                    ChatResponseCodeEnum
                            .CREATE_SESSION_FAILED);
        }

        ChatSessionMemberDO currentMember =
                buildMember(
                        sessionId,
                        currentUserId,
                        now);

        ChatSessionMemberDO targetMember =
                buildMember(
                        sessionId,
                        targetUserId,
                        now);

        int currentMemberRows =
                memberMapper.insert(currentMember);

        if (currentMemberRows != 1) {
            throw new BizException(
                    ChatResponseCodeEnum
                            .CREATE_SESSION_FAILED);
        }

        int targetMemberRows =
                memberMapper.insert(targetMember);

        if (targetMemberRows != 1) {
            throw new BizException(
                    ChatResponseCodeEnum
                            .CREATE_SESSION_FAILED);
        }

        return session;
    }

    private ChatSessionMemberDO buildMember(
            Long sessionId,
            Long userId,
            LocalDateTime createTime) {

        return ChatSessionMemberDO.builder()
                .sessionId(sessionId)
                .userId(userId)
                .unreadCount(0)
                .lastReadSequence(0L)
                .createTime(createTime)
                .updateTime(createTime)
                .isDeleted(false)
                .build();
    }

    private Long validateAndGetTargetUserId(
            CreateDirectSessionReqVO request,
            Long currentUserId) {

        if (request == null
                || request.getTargetUserId() == null
                || request.getTargetUserId() <= 0) {
            throw new BizException(
                    CommonResponseCodeEnum.PARAM_NOT_VALID);
        }

        Long targetUserId =
                request.getTargetUserId();

        if (currentUserId.equals(targetUserId)) {
            throw new BizException(
                    CommonResponseCodeEnum.PARAM_NOT_VALID);
        }

        return targetUserId;
    }

    private CreateDirectSessionRespVO buildResponse(
            ChatSessionDO session,
            Long targetUserId) {

        return CreateDirectSessionRespVO.builder()
                .sessionId(session.getSessionId())
                .sessionType(
                        session.getSessionType()
                                .getValue())
                .targetUserId(targetUserId)
                .createTime(session.getCreateTime())
                .build();
    }
}
