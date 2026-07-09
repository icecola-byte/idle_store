package com.lh.idlestore.auth.service;

import com.lh.idlestore.auth.model.vo.request.MiniLoginReqVO;
import com.lh.idlestore.auth.model.vo.request.AdminLoginReqVO;
import com.lh.idlestore.auth.model.vo.response.AdminLoginRespVO;
import com.lh.idlestore.auth.model.vo.response.MiniLoginRespVO;

public interface AuthService {
    /**
     * 小程序端登陆/注册
     */
    MiniLoginRespVO login(MiniLoginReqVO miniLoginReqVO);

    /**
     * 管理端登陆
     */
    AdminLoginRespVO login(AdminLoginReqVO adminLoginReqVO);
    /**
     * 管理端注册
     */
    String adminRegister(AdminLoginReqVO adminLoginReqVO);

    /**
     * 退出登陆
     */
    void logout();
}
