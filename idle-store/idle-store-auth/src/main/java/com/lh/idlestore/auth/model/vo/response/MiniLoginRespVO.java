package com.lh.idlestore.auth.model.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小程序端登录/注册返回
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MiniLoginRespVO {

    private Long userId;
    private String token;
    private String communityId;
    private Boolean status;
}
