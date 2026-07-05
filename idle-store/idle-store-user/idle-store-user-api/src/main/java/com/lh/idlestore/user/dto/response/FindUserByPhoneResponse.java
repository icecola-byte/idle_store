package com.lh.idlestore.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 *  根据手机号查询用户信息
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUserByPhoneResponse {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 密码哈希
     */
    private String passwordHash;

    private String communityId;

}
