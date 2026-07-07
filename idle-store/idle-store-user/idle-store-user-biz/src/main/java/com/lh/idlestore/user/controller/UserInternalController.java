package com.lh.idlestore.user.controller;

import com.lh.framework.common.response.Response;
import com.lh.idlestore.user.api.UserFeignApi;
import com.lh.idlestore.user.dto.request.BatchFindUsersRequest;
import com.lh.idlestore.user.dto.request.FindUserByPhoneRequest;
import com.lh.idlestore.user.dto.request.MiniLoginRequest;
import com.lh.idlestore.user.dto.response.FindUserByPhoneResponse;
import com.lh.idlestore.user.dto.response.MiniLoginResponse;
import com.lh.idlestore.user.dto.response.UserBriefResponse;
import com.lh.idlestore.user.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户服务内部 Feign 端点，仅供服务间调用。
 */
@RestController
public class UserInternalController implements UserFeignApi {

    @Resource
    private UserService userService;

    @Override
    public Response<MiniLoginResponse> login(MiniLoginRequest request) {
        return Response.success(userService.login(request));
    }

    @Override
    public Response<FindUserByPhoneResponse> findByPhone(FindUserByPhoneRequest request) {
        return Response.success(userService.findByPhone(request));
    }

    @Override
    public Response<UserBriefResponse> findById(Long userId) {
        return Response.success(userService.findById(userId));
    }

    @Override
    public Response<List<UserBriefResponse>> findByIds(BatchFindUsersRequest request) {
        return Response.success(userService.findByIds(request));
    }
}
