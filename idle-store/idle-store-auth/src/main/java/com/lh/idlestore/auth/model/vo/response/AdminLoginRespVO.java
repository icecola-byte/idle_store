package com.lh.idlestore.auth.model.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理端登录返回
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminLoginRespVO {

    private Long userId;
    private String token;
    private String phone;
    private String communityId;
}
