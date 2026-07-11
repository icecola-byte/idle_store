package com.lh.idlestore.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.lh.framework.common.exception.BizException;
import com.lh.idlestore.auth.constant.VerificationCodeCacheKeys;
import com.lh.idlestore.auth.enums.AuthResponseCodeEnum;
import com.lh.idlestore.auth.model.vo.request.SendVerificationCodeReqVO;
import com.lh.idlestore.auth.service.VerificationCodeService;
import com.lh.idlestore.auth.integration.sms.AliyunSmsSender;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource(name = "smsExecutor")
    private ThreadPoolTaskExecutor ssmsExecutor;

    @Resource
    private AliyunSmsSender aliyunSmsSender;

    @Override
    public void send(SendVerificationCodeReqVO sendVerificationCodeReqVO) {
        // 手机号
        String phone = sendVerificationCodeReqVO.getPhone();

        // 构建验证码
        String key = VerificationCodeCacheKeys.verificationCode(phone);

        // 判断是否已经发送了验证码
        boolean isSent = redisTemplate.hasKey(key);
        if (isSent) {
            throw new BizException(AuthResponseCodeEnum.VERIFICATION_CODE_SEND_FREQUENTLY);
        }
        // 6 位随机验证码
        String verificationCode = RandomUtil.randomNumbers(6);

        log.info("==> 短信验证码发送任务已提交");

        // 调用第三方短信发送服务
        ssmsExecutor.submit(() -> {
            String signName = "速通互联验证码";
            String templateCode = "100001";
            String templateParam = String.format(
                    "{\"code\":\"%s\",\"min\":\"%s\"}",
                    verificationCode,
                    "5"
            );
            aliyunSmsSender.sendMessage(signName, templateCode, phone, templateParam);
        });

        // 存储验证码到 redis, 并设置过期时间为 3 分钟
        redisTemplate.opsForValue().set(key, verificationCode, 3, TimeUnit.MINUTES);
    }
}
