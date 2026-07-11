package com.lh.idlestore.auth.controller;

import com.lh.framework.biz.operationlog.annotation.ApiOperationLog;
import com.lh.framework.common.response.Response;
import com.lh.idlestore.auth.model.vo.request.SendVerificationCodeReqVO;
import com.lh.idlestore.auth.service.VerificationCodeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Slf4j
public class VerificationCodeController {

    @Resource
    private VerificationCodeService verificationCodeService;

    @PostMapping("/verification/code/send")
    @ApiOperationLog(
            description = "发送短信验证码",
            logResponse = false
    )
    public Response<Void> send(@Validated @RequestBody SendVerificationCodeReqVO sendVerificationCodeReqVO) {
        verificationCodeService.send(sendVerificationCodeReqVO);
        return Response.success();
    }

}
