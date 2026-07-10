package com.lh.idlestore.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.google.common.base.Preconditions;
import com.lh.framework.common.exception.BizException;
import com.lh.idlestore.auth.constant.VerificationCodeCacheKeys;
import com.lh.idlestore.auth.enums.LoginTypeEnum;
import com.lh.idlestore.auth.enums.AuthResponseCodeEnum;
import com.lh.idlestore.auth.model.vo.request.MiniLoginReqVO;
import com.lh.idlestore.auth.model.vo.request.AdminLoginReqVO;
import com.lh.idlestore.auth.model.vo.response.AdminLoginRespVO;
import com.lh.idlestore.auth.model.vo.response.MiniLoginRespVO;
import com.lh.idlestore.auth.remote.UserRemoteService;
import com.lh.idlestore.auth.service.AuthService;
import com.lh.idlestore.user.dto.response.FindUserByPhoneResponse;
import com.lh.idlestore.user.dto.response.MiniLoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final RedisTemplate<String, Object> redisTemplate;

    private final PasswordEncoder passwordEncoder;
    private final UserRemoteService userRemoteService;

    /**
     * 小程序端用户的登录与注册
     */
    @Override
    public MiniLoginRespVO login(MiniLoginReqVO miniLoginReqVO) {
        String phone = miniLoginReqVO.getPhone();

        // RPC: 调用用户服务，注册用户 --> 先查询，查不到就注册
        MiniLoginResponse response = userRemoteService.login(phone);
        if (Objects.isNull(response)) {
            throw new BizException(AuthResponseCodeEnum.LOGIN_FAIL);
        }
        // SaToken 登录用户，并返回 token 令牌
        StpUtil.login(response.getUserId());
        SaTokenInfo token = StpUtil.getTokenInfo();
        return MiniLoginRespVO.builder()
                .userId(response.getUserId())
                .token(token.getTokenValue())
                .communityId(response.getCommunityId())
                .status(response.getStatus())
                .build();
    }


    @Override
    public AdminLoginRespVO login(AdminLoginReqVO adminLoginReqVO) {
        String phone = adminLoginReqVO.getPhone();
        Integer type = adminLoginReqVO.getType();

        LoginTypeEnum loginTypeEnum = LoginTypeEnum.valueOf(type);

        Long userId = null;
        FindUserByPhoneResponse findUserByPhoneResponse = null;

        // 判断登录类型
        switch (loginTypeEnum) {
            case VERIFICATION_CODE: // 验证码登录
                String verificationCode = adminLoginReqVO.getCode();

                // 校验入参验证码是否为空，因为需要根据业务判断，如果是验证码登陆不能为空，如果是密码登陆可以为空，所以不能用校验注解
                // if (StringUtils.isBlank(verificationCode)) {
                //    return Response.fail(AuthResponseCodeEnum.PARAM_NOT_VALID.getErrorCode(), "验证码不能为空");
                // }
                // 配合全局异常
                Preconditions.checkArgument(StringUtils.isNotBlank(verificationCode), "验证码不能为空");
                // 构建验证码 Redis Key
                String key = VerificationCodeCacheKeys.verificationCode(phone);
                // 查询存储在 Redis 中该用户的登录验证码
                String sentCode = (String) redisTemplate.opsForValue().get(key);

                // 判断用户提交的验证码，与 Redis 中的验证码是否一致
                if (!StringUtils.equals(verificationCode, sentCode)) {
                    throw new BizException(AuthResponseCodeEnum.VERIFICATION_CODE_ERROR);
                }

                // 通过手机号查询记录
                findUserByPhoneResponse = userRemoteService.findUserByPhone(phone);

                log.info("==> 用户注册状态查询完成, registered: {}", findUserByPhoneResponse != null);

                // 判断是否注册
                if (Objects.isNull(findUserByPhoneResponse)) {
                    // 若此用户还没有注册，提示注册
                    throw new BizException(AuthResponseCodeEnum.USER_NOT_FOUND);
                } else {
                    // 已注册，则获取其用户 ID
                    userId = findUserByPhoneResponse.getUserId();
                }
                break;
            case PASSWORD: // 密码登录
                findUserByPhoneResponse = userRemoteService.findUserByPhone(phone);
                String password = adminLoginReqVO.getPassword();

                if (Objects.isNull(findUserByPhoneResponse)) {
                    throw new BizException(AuthResponseCodeEnum.USER_NOT_FOUND);
                }

                // 加密密码
                String encodePassword = findUserByPhoneResponse.getPasswordHash();
                boolean isCorrect = passwordEncoder.matches(password, encodePassword);

                if (!isCorrect) {
                    throw new BizException(AuthResponseCodeEnum.PHONE_OR_PASSWORD_ERROR);
                }

                userId = findUserByPhoneResponse.getUserId();
                break;
            default:
                break;
        }

        // SaToken 登录用户，并返回 token 令牌
        StpUtil.login(userId);
        String token = StpUtil.getTokenInfo().tokenValue;
        AdminLoginRespVO adminLoginRespVO = AdminLoginRespVO.builder()
                .userId(userId)
                .token(token)
                .communityId(findUserByPhoneResponse.getCommunityId())
                .phone(phone)
                .build();

        return adminLoginRespVO;
    }

    @Override
    @Transactional
    public String adminRegister(AdminLoginReqVO adminLoginReqVO) {
//        String phone = adminLoginReqVO.getPhone();
        return null;
    }

    @Override
    public void logout() {
        // 退出登陆，根据当前 token 退出，因为允许多端登陆
        // 如果是 StpUtil.logout(userId) 那么就会把多端的 token 都删除了
        StpUtil.logout();
    }

}
