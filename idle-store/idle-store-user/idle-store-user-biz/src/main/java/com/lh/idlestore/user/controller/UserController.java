package com.lh.idlestore.user.controller;

import com.lh.framework.biz.operationlog.annotation.ApiOperationLog;
import com.lh.framework.common.response.Response;
import com.lh.idlestore.user.model.vo.request.UpdateUserInfoReqVO;
import com.lh.idlestore.user.model.vo.response.UserProfileRespVO;
import com.lh.idlestore.user.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户信息修改
     */
    @PutMapping("/profile")
    @ApiOperationLog(description = "更新当前登录用户资料")
    public Response<UserProfileRespVO> updateUserInfo(@Validated @RequestBody UpdateUserInfoReqVO updateUserInfoReqVO) {
        return Response.success(userService.updateUserInfo(updateUserInfoReqVO));
    }

    @GetMapping("/profile")
    @ApiOperationLog(description = "查询当前登录用户资料")
    public Response<UserProfileRespVO> getCurrentUserProfile() {
        return Response.success(userService.getCurrentUserProfile());
    }
}
