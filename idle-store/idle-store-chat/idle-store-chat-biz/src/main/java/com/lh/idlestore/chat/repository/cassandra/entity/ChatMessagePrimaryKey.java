package com.lh.idlestore.chat.repository.cassandra.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;

/**
 * Cassandra 消息表复合主键。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyClass
public class ChatMessagePrimaryKey implements Serializable {

    public static final long BUCKET_SIZE = 100_000L;

    @PrimaryKeyColumn(name = "session_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private Long sessionId;

    @PrimaryKeyColumn(name = "bucket", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private Integer bucket;

    @PrimaryKeyColumn(name = "sequence", ordinal = 2, type = PrimaryKeyType.CLUSTERED,
            ordering = Ordering.DESCENDING)
    private Long sequence;

    public static ChatMessagePrimaryKey of(Long sessionId, Long sequence) {
        if (sessionId == null || sessionId <= 0) {
            throw new IllegalArgumentException("会话ID必须大于0");
        }
        if (sequence == null || sequence <= 0) {
            throw new IllegalArgumentException("消息序号必须大于0");
        }

        int bucket = calculateBucket(sequence);
        return new ChatMessagePrimaryKey(sessionId, bucket, sequence);
    }

    public static int calculateBucket(long sequence) {
        if (sequence <= 0) {
            throw new IllegalArgumentException("消息序号必须大于0");
        }
        return Math.toIntExact((sequence - 1) / BUCKET_SIZE);
    }

    public static long firstSequence(int bucket) {
        return bucket * BUCKET_SIZE + 1;
    }

    public static long lastSequence(int bucket) {
        return (bucket + 1L) * BUCKET_SIZE;
    }
}
