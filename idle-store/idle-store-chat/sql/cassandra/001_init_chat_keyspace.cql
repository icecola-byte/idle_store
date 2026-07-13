-- ============================================================================
-- idle-store-chat Cassandra 初始化脚本
--
-- Cassandra 保存聊天消息历史；会话、未读数、已读位置和 Outbox 仍由 MySQL 保存。
-- 开发环境副本因子使用 1。生产环境需要把 datacenter1 改为真实数据中心名称，
-- 并根据 Cassandra 节点数量将副本因子调整为 3（至少不能超过节点数）。
-- ============================================================================

CREATE KEYSPACE IF NOT EXISTS idle_store_chat
WITH replication = {
    'class': 'NetworkTopologyStrategy',
    'datacenter1': '1'
}
AND durable_writes = true;

USE idle_store_chat;


-- ============================================================================
-- 按会话查询的消息表
--
-- 分区键：(session_id, bucket)
-- 聚簇键：sequence，按倒序存储
--
-- bucket 按会话序号分桶，每 100000 条消息一个桶：
--   bucket = floor((sequence - 1) / 100000)
-- 例如序号 1～100000 属于桶 0，100001～200000 属于桶 1。
-- 采用序号分桶后，可以由 last_read_sequence 直接定位离线消息所在分区，
-- 同时避免活跃会话的单个 Cassandra 分区无限增长。
--
-- content 内容约定：
--   TEXT  -> 文本内容
--   IMAGE -> OSS 对象地址
--   CARD  -> 包含 businessType、businessId 等字段的 JSON
-- ============================================================================
CREATE TABLE IF NOT EXISTS message_by_session
(
    session_id       bigint,
    bucket           int,
    sequence         bigint,
    message_id       bigint,
    sender_id        bigint,
    receiver_id      bigint,
    message_type     tinyint,
    content          text,
    client_message_id text,
    send_time        timestamp,
    is_recalled      boolean,
    recall_time      timestamp,
    PRIMARY KEY ((session_id, bucket), sequence)
)
WITH CLUSTERING ORDER BY (sequence DESC)
AND comment = '按会话和序号分桶存储的聊天消息历史';
