package com.lh.idlestore.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 供其他服务查询的用户公开信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBriefResponse {

    private Long userId;

    private String username;

    private String avatarUrl;

    private Integer status;
}
