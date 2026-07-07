package com.lh.idlestore.user.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lh.idlestore.user.enums.SexEnum;
import com.lh.idlestore.user.enums.UserStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_user")
public class UserDO {

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long userId;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 密码哈希
     */

    private String passwordHash;

    /**
     * 用户名
     */
    private String username;

    /**
     * 所在社区ID
     */
    private String communityId;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 注册时间
     */
    private LocalDateTime registerTime;

    /**
     * 硬币余额
     */
    private Integer coinBalance;

    /**
     * 账号是否封禁
     */
    private UserStatusEnum status;

    /**
     * 性别
     */
    private SexEnum sex;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 详细地址
     */
    private String addressDetail;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Boolean isDeleted;
}