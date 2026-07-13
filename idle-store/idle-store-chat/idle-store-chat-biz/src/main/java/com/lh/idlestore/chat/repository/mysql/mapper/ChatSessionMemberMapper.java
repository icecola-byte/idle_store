package com.lh.idlestore.chat.repository.mysql.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lh.idlestore.chat.repository.mysql.dataobject.ChatSessionMemberDO;
import com.lh.idlestore.chat.repository.mysql.projection.ChatSessionSummaryProjection;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话成员 MySQL Mapper。
 *
 * 该表使用联合主键，不能调用 BaseMapper 的按单一主键方法。
 */
public interface ChatSessionMemberMapper extends BaseMapper<ChatSessionMemberDO> {

    ChatSessionMemberDO selectBySessionAndUser(@Param("sessionId") Long sessionId,
                                               @Param("userId") Long userId);

    ChatSessionMemberDO selectBySessionAndUserForUpdate(@Param("sessionId") Long sessionId,
                                                        @Param("userId") Long userId);

    int increaseUnread(@Param("sessionId") Long sessionId,
                       @Param("userId") Long userId);

    int markRead(@Param("sessionId") Long sessionId,
                 @Param("userId") Long userId,
                 @Param("lastReadSequence") Long lastReadSequence,
                 @Param("currentLastSequence") Long currentLastSequence);

    ChatSessionMemberDO selectOtherMember(
            @Param("sessionId") Long sessionId,
            @Param("currentUserId") Long currentUserId
    );

    List<ChatSessionSummaryProjection> selectSessionSummaries(@Param("userId") Long userId);
}
