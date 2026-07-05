package com.lh.idlestore.user.api;

import com.lh.framework.common.response.Response;
import com.lh.idlestore.user.constant.UserApiConstants;
import com.lh.idlestore.user.dto.request.FindUserByPhoneRequest;
import com.lh.idlestore.user.dto.request.MiniLoginRequest;
import com.lh.idlestore.user.dto.request.BatchFindUsersRequest;
import com.lh.idlestore.user.dto.response.FindUserByPhoneResponse;
import com.lh.idlestore.user.dto.response.MiniLoginResponse;
import com.lh.idlestore.user.dto.response.UserBriefResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = UserApiConstants.SERVICE_NAME)
public interface UserFeignApi {

    /**
     * 小程序端登录/注册，供内部服务调用。
     */
    @PostMapping(value = UserApiConstants.INTERNAL_API_PREFIX + "/login")
    Response<MiniLoginResponse> login(@RequestBody MiniLoginRequest request);

    /**
     * 根据手机号查询用户信息，供内部服务调用。
     */
    @PostMapping(value = UserApiConstants.INTERNAL_API_PREFIX + "/findByPhone")
    Response<FindUserByPhoneResponse> findByPhone(@RequestBody FindUserByPhoneRequest request);

    /**
     * 根据用户ID查询公开信息，供内部服务调用。
     */
    @GetMapping(value = UserApiConstants.INTERNAL_API_PREFIX + "/{userId}")
    Response<UserBriefResponse> findById(@PathVariable("userId") Long userId);

    /**
     * 批量查询用户公开信息，供会话列表使用。
     */
    @PostMapping(value = UserApiConstants.INTERNAL_API_PREFIX + "/batch")
    Response<List<UserBriefResponse>> findByIds(@RequestBody BatchFindUsersRequest request);
}
