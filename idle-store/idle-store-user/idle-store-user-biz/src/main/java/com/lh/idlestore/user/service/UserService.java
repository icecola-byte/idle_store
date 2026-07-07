package com.lh.idlestore.user.service;

import com.lh.idlestore.user.model.vo.request.UpdateUserInfoReqVO;
import com.lh.idlestore.user.model.vo.response.UserProfileRespVO;
import com.lh.idlestore.user.dto.request.FindUserByPhoneRequest;
import com.lh.idlestore.user.dto.request.MiniLoginRequest;
import com.lh.idlestore.user.dto.request.BatchFindUsersRequest;
import com.lh.idlestore.user.dto.response.FindUserByPhoneResponse;
import com.lh.idlestore.user.dto.response.MiniLoginResponse;
import com.lh.idlestore.user.dto.response.UserBriefResponse;

import java.util.List;

/**用户业务
 **/
public interface UserService {

    /**
     * 更新用户信息
     */
    UserProfileRespVO updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO);

    /**
     * 用户注册
     */
    MiniLoginResponse login(MiniLoginRequest request);

    /**
     * 根据手机号查询用户信息
     */
    FindUserByPhoneResponse findByPhone(FindUserByPhoneRequest request);

    /**
     * 根据用户ID查询公开信息。
     */
    UserBriefResponse findById(Long userId);

    /**
     * 批量查询用户公开信息。
     */
    List<UserBriefResponse> findByIds(BatchFindUsersRequest request);

    /**
     * 查询当前用户信息
     */
    UserProfileRespVO getCurrentUserProfile();
}
