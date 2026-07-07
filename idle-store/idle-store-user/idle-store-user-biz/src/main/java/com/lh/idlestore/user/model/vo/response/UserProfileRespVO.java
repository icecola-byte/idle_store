package com.lh.idlestore.user.model.vo.response;

import com.lh.idlestore.user.enums.SexEnum;
import com.lh.idlestore.user.enums.UserStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileRespVO {

    private Long userId;
    private String phone;
    private String username;
    private String communityId;
    private String avatarUrl;
    private Integer coinBalance;
    private SexEnum sex;
    private String realName;
    private String addressDetail;
    private LocalDateTime registerTime;
    private UserStatusEnum status;
}
