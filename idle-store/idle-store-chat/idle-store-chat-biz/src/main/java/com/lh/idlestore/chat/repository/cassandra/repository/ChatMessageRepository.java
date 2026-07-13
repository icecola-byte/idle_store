package com.lh.idlestore.chat.repository.cassandra.repository;

import com.lh.idlestore.chat.repository.cassandra.entity.ChatMessageEntity;
import com.lh.idlestore.chat.repository.cassandra.entity.ChatMessagePrimaryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Cassandra 聊天消息 Repository。
 *
 * 查询必须携带 session_id 和 bucket，禁止跨分区扫描。
 */
public interface ChatMessageRepository
        extends CassandraRepository<ChatMessageEntity, ChatMessagePrimaryKey> {

    @Query("SELECT * FROM message_by_session "
            + "WHERE session_id = :sessionId AND bucket = :bucket "
            + "AND sequence < :beforeSequence LIMIT :limit")
    List<ChatMessageEntity> findHistory(@Param("sessionId") Long sessionId,
                                    @Param("bucket") Integer bucket,
                                    @Param("beforeSequence") Long beforeSequence,
                                    @Param("limit") int limit);

    @Query("SELECT * FROM message_by_session "
            + "WHERE session_id = :sessionId AND bucket = :bucket "
            + "AND sequence > :afterSequence ORDER BY sequence ASC LIMIT :limit")
    List<ChatMessageEntity> findAfterSequence(@Param("sessionId") Long sessionId,
                                          @Param("bucket") Integer bucket,
                                          @Param("afterSequence") Long afterSequence,
                                          @Param("limit") int limit);
}
