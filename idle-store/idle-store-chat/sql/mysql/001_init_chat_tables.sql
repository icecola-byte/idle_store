-- ============================================================================
-- idle-store-chat MySQL 初始化脚本
--
-- 请在聊天服务独立使用的 MySQL 数据库中执行本脚本。
-- MySQL 只保存强一致的会话状态、成员状态和待投递事件；消息正文保存在 Cassandra。
--
-- 表说明：
--   1. t_chat_session         会话元数据及最后一条消息摘要
--   2. t_chat_session_member  用户在会话中的未读数和已读位置
--   3. t_chat_message_outbox  保证 RocketMQ 消息可靠投递的本地消息表
-- ============================================================================


-- ============================================================================
-- 聊天会话表
--
-- session_key 生成规则：
--   单聊：DIRECT:较小用户ID:较大用户ID，例如 DIRECT:10:20
--   系统通知：SYSTEM:接收用户ID，例如 SYSTEM:10
--
-- 单聊用户 ID 必须从小到大排列，唯一索引用于避免同一对用户创建两个会话。
-- last_sequence 在发送消息的 MySQL 事务中原子递增，是会话内消息的严格递增序号。
-- ============================================================================
CREATE TABLE IF NOT EXISTS t_chat_session
(
    session_id          BIGINT       NOT NULL COMMENT '会话ID，由分布式ID服务生成',
    session_type        TINYINT      NOT NULL COMMENT '会话类型：1-单聊，2-系统通知',
    session_key         VARCHAR(100) NOT NULL COMMENT '会话唯一键，单聊为DIRECT:小用户ID:大用户ID，系统通知为SYSTEM:用户ID',
    last_sequence       BIGINT       NOT NULL DEFAULT 0 COMMENT '会话内最新消息序号，发送消息时原子递增',
    last_message_id     BIGINT                DEFAULT NULL COMMENT '最后一条消息ID',
    last_message        VARCHAR(500)          DEFAULT NULL COMMENT '最后一条消息摘要，用于会话列表展示',
    last_message_time   DATETIME(3)           DEFAULT NULL COMMENT '最后一条消息发送时间',
    create_time         DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_time         DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                                              ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    is_deleted          TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (session_id),
    UNIQUE KEY uk_session_key (session_key),
    KEY idx_last_message_time (last_message_time)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    COMMENT = '聊天会话表';


-- ============================================================================
-- 会话成员表
--
-- 单聊会话包含两条成员记录，系统通知会话只包含接收用户的一条成员记录。
-- unread_count 用于快速展示角标，避免每次进入会话列表都扫描 Cassandra 统计。
-- last_read_sequence 表示用户已确认读到的最大会话序号，用于已读同步和离线补拉。
-- ============================================================================
CREATE TABLE IF NOT EXISTS t_chat_session_member
(
    session_id         BIGINT      NOT NULL COMMENT '会话ID',
    user_id            BIGINT      NOT NULL COMMENT '会话成员用户ID',
    unread_count       INT         NOT NULL DEFAULT 0 COMMENT '该用户在当前会话中的未读消息数',
    last_read_sequence BIGINT      NOT NULL DEFAULT 0 COMMENT '该用户已确认读到的最大会话序号',
    create_time        DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '加入会话时间',
    update_time        DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                                            ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    is_deleted         TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '用户是否隐藏该会话：0-否，1-是；收到新消息时恢复为0',
    PRIMARY KEY (session_id, user_id),
    KEY idx_user_session_list (user_id, is_deleted, update_time),
    KEY idx_user_unread (user_id, is_deleted, unread_count)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    COMMENT = '聊天会话成员表';


-- ============================================================================
-- 聊天消息 Outbox 表
--
-- 发送消息时在同一个 MySQL 事务内完成：
--   1. 递增 t_chat_session.last_sequence，并更新最后一条消息摘要；
--   2. 增加接收方的 unread_count；
--   3. 插入一条状态为待投递的 Outbox 事件。
--
-- 后台任务把待投递事件发送到 RocketMQ，Broker 确认后更新为已发送；失败则延迟重试。
-- 消费者以 message_id/sequence 保证幂等，然后写 Cassandra 并尝试通过 WebSocket 推送。
-- 本表不是聊天记录永久存储，可按保留周期清理已发送记录。
-- ============================================================================
CREATE TABLE IF NOT EXISTS t_chat_message_outbox
(
    event_id           BIGINT       NOT NULL COMMENT 'Outbox事件ID，由分布式ID服务生成',
    message_id         BIGINT       NOT NULL COMMENT '全局唯一消息ID',
    session_id         BIGINT       NOT NULL COMMENT '消息所属会话ID',
    sequence           BIGINT       NOT NULL COMMENT '消息在会话内的严格递增序号',
    sender_id          BIGINT       NOT NULL COMMENT '发送方用户ID，系统通知固定为0',
    receiver_id        BIGINT       NOT NULL COMMENT '接收方用户ID',
    client_message_id  VARCHAR(64)  NOT NULL COMMENT '客户端生成的消息请求ID，用于防止重复发送',
    event_type         VARCHAR(50)  NOT NULL DEFAULT 'CHAT_MESSAGE_CREATED' COMMENT '事件类型',
    event_payload      JSON         NOT NULL COMMENT '发送到RocketMQ并写入Cassandra的完整事件JSON',
    event_status       TINYINT      NOT NULL DEFAULT 0 COMMENT '投递状态：0-待投递，1-已发送，2-投递失败待重试',
    retry_count        INT          NOT NULL DEFAULT 0 COMMENT '已投递次数',
    next_retry_time    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '下次允许投递时间',
    sent_time          DATETIME(3)           DEFAULT NULL COMMENT 'RocketMQ确认接收时间',
    create_time        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_time        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                                             ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (event_id),
    UNIQUE KEY uk_message_id (message_id),
    UNIQUE KEY uk_sender_client_message (sender_id, client_message_id),
    UNIQUE KEY uk_session_sequence (session_id, sequence),
    KEY idx_status_retry (event_status, next_retry_time, event_id),
    KEY idx_create_time (create_time)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    COMMENT = '聊天消息可靠投递Outbox表';
