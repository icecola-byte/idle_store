package com.lh.idlestore.user.model.vo.request;

import com.lh.idlestore.user.enums.SexEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改用户信息
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 因为用户修改信息可能不会修改所有项，所以要单独在业务层进行判断，而不是每个都加校验注解
public class UpdateUserInfoReqVO {

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 用户名
     */
    private String username;

    /**
     * 所在社区ID
     */
    private String communityId;

    /**
     * 头像文件 ID
     */
    private Long avatarFileId;

    /**
     * 性别
     * @see com.lh.idlestore.user.enums.SexEnum
     */
    private SexEnum sex;

    /**
     * 真实地址
     */
    private String realName;

    /**
     * 详细地址
     */
    private String addressDetail;
}
