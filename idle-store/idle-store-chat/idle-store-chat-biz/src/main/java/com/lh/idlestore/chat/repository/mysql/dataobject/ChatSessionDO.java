package com.lh.idlestore.chat.repository.mysql.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lh.idlestore.chat.enums.ChatSessionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天会话
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_chat_session")
public class ChatSessionDO {

    /**
     * 会话ID，由分布式ID服务生成
     */
    @TableId(type = IdType.INPUT)
    private Long sessionId;

    /**
     * 会话类型：1-单聊，2-系统通知
     */
    private ChatSessionTypeEnum sessionType;

    /**
     * 会话唯一键
     * 单聊：DIRECT:较小用户ID:较大用户ID
     * 系统：SYSTEM:用户ID
     */
    private String sessionKey;

    /**
     * 会话内最新消息序号
     */
    private Long lastSequence;

    /**
     * 最后一条消息ID
     */
    private Long lastMessageId;

    /**
     * 最后一条消息摘要
     */
    private String lastMessage;

    /**
     * 最后一条消息发送时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除：false-未删除，true-已删除
     */
    private Boolean isDeleted;
}
