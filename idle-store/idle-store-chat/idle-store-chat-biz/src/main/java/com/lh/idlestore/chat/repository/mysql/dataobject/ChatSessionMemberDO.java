package com.lh.idlestore.chat.repository.mysql.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天会话成员
 *
 * 数据库使用 session_id + user_id 联合主键，
 * 因此不要使用 BaseMapper.updateById()。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_chat_session_member")
public class ChatSessionMemberDO {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 当前用户在该会话中的未读消息数量
     */
    private Integer unreadCount;

    /**
     * 用户已确认读到的最大会话序号
     */
    private Long lastReadSequence;

    /**
     * 加入会话时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除会话：false-未删除，true-已删除
     */
    private Boolean isDeleted;
}
