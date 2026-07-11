package com.lh.idlestore.auth.service;

import com.lh.idlestore.auth.model.vo.request.SendVerificationCodeReqVO;

public interface VerificationCodeService {

    /**
     * 发送短信验证码
     *
     * @param sendVerificationCodeReqVO
     * @return
     */
    void send(SendVerificationCodeReqVO sendVerificationCodeReqVO);
}
