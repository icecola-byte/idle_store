package com.lh.idlestore.auth.constant;

/**
 * Auth 服务私有的验证码缓存 Key。
 */
public final class VerificationCodeCacheKeys {

    private static final String VERIFICATION_CODE_PREFIX =
            "verification_code:";

    private VerificationCodeCacheKeys() {
    }

    public static String verificationCode(String phone) {
        return VERIFICATION_CODE_PREFIX + phone;
    }
}
