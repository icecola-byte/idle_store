package com.lh.idlestore.chat.repository.mysql.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lh.idlestore.chat.repository.mysql.dataobject.ChatMessageOutboxDO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息 Outbox MySQL Mapper。
 */
public interface ChatMessageOutboxMapper extends BaseMapper<ChatMessageOutboxDO> {

    /**
     * 使用 SKIP LOCKED 支持多个发布任务并发抢占事件。
     * 调用方必须开启 MySQL 事务。
     */
    List<ChatMessageOutboxDO> selectReadyForUpdate(@Param("now") LocalDateTime now,
                                                   @Param("limit") int limit);

    int markSent(@Param("eventId") Long eventId,
                 @Param("sentTime") LocalDateTime sentTime);

    int markRetry(@Param("eventId") Long eventId,
                  @Param("nextRetryTime") LocalDateTime nextRetryTime);

    ChatMessageOutboxDO selectBySenderAndClientMessageId(
            @Param("senderId") Long senderId,
            @Param("clientMessageId") String clientMessageId
    );
}
