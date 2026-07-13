package com.lh.idlestore.chat.repository.mysql.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lh.idlestore.chat.repository.mysql.dataobject.ChatSessionDO;
import org.apache.ibatis.annotations.Param;

/**
 * 聊天会话 MySQL Mapper。
 */
public interface ChatSessionMapper extends BaseMapper<ChatSessionDO> {

    ChatSessionDO selectBySessionKey(@Param("sessionKey") String sessionKey);

    /**
     * 发送消息事务中锁定会话行，调用方在同一事务内分配 sequence。
     */
    ChatSessionDO selectByIdForUpdate(@Param("sessionId") Long sessionId);

    int updateLastMessage(@Param("sessionId") Long sessionId,
                          @Param("sequence") Long sequence,
                          @Param("messageId") Long messageId,
                          @Param("lastMessage") String lastMessage,
                          @Param("lastMessageTime") java.time.LocalDateTime lastMessageTime);
}
