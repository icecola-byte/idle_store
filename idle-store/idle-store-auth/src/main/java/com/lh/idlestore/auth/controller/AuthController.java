package com.lh.idlestore.auth.controller;

import com.lh.framework.biz.operationlog.annotation.ApiOperationLog;
import com.lh.framework.common.response.Response;
import com.lh.idlestore.auth.model.vo.request.MiniLoginReqVO;
import com.lh.idlestore.auth.model.vo.request.AdminLoginReqVO;
import com.lh.idlestore.auth.model.vo.response.AdminLoginRespVO;
import com.lh.idlestore.auth.model.vo.response.MiniLoginRespVO;
import com.lh.idlestore.auth.service.AuthService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/mini/login")
    @ApiOperationLog(
            description = "小程序端用户登录/注册",
            logRequest = false,
            logResponse = false
    )
    public Response<MiniLoginRespVO> login(@Validated @RequestBody MiniLoginReqVO miniLoginReqVO) {
        return Response.success(authService.login(miniLoginReqVO));
    }

    @PostMapping("/admin/login")
    @ApiOperationLog(
            description = "管理端登陆",
            logRequest = false,
            logResponse = false
    )
    public Response<AdminLoginRespVO> login(@Validated @RequestBody AdminLoginReqVO adminLoginReqVO) {
        return Response.success(authService.login(adminLoginReqVO));
    }

    @PostMapping("/admin/logout")
    @ApiOperationLog(description = "管理员登出")
    public Response<Void> logout() {
        authService.logout();
        return Response.success();
    }

//    @PostMapping("/admin/register")
//    @ApiOperationLog(description = "管理端注册")
//    public Response<String> adminRegister(@Validated @RequestBody AdminLoginReqVO adminLoginReqVO) {
//        return userService.adminRegister(adminLoginReqVO);
//    }
}
