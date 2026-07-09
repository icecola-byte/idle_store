package com.lh.idlestore.auth.remote;

import com.lh.framework.common.exception.BizException;
import com.lh.framework.openfeign.core.RemoteCallExecutor;
import com.lh.framework.openfeign.exception.RemoteBusinessException;
import com.lh.framework.openfeign.exception.RemoteCallException;
import com.lh.idlestore.auth.enums.AuthResponseCodeEnum;
import com.lh.idlestore.user.api.UserFeignApi;
import com.lh.idlestore.user.constant.UserApiConstants;
import com.lh.idlestore.user.dto.request.FindUserByPhoneRequest;
import com.lh.idlestore.user.dto.request.MiniLoginRequest;
import com.lh.idlestore.user.dto.response.FindUserByPhoneResponse;
import com.lh.idlestore.user.dto.response.MiniLoginResponse;
import com.lh.idlestore.user.enums.UserResponseCodeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户服务远程调用。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserRemoteService {

    private final UserFeignApi userFeignApi;
    private final RemoteCallExecutor remoteCallExecutor;

    public MiniLoginResponse login(String phone) {
        MiniLoginRequest request = new MiniLoginRequest();
        request.setPhone(phone);

        try {
            return remoteCallExecutor.required(
                    UserApiConstants.SERVICE_NAME,
                    UserApiConstants.OPERATION_LOGIN,
                    () -> userFeignApi.login(request)
            );
            // 远程调用异常大致分为两类
            // 1 远程服务没问题，业务出现异常，可以将对方业务的异常转化为自己的异常，或者统一抛出一种异常
            // 2 远程服务有问题，统一抛出 USER_SERVICE_CALL_FAILED
        } catch (RemoteBusinessException exception) {
            log.warn(
                    "用户服务登录业务失败，errorCode={}, message={}",
                    exception.getRemoteErrorCode(),
                    exception.getRemoteErrorMessage()
            );

            if (exception.hasErrorCode(UserResponseCodeEnum.REGISTER_FAIL)) {
                throw new BizException(
                        AuthResponseCodeEnum.USER_REGISTER_ERROR
                );
            }

            throw new BizException(AuthResponseCodeEnum.LOGIN_FAIL);
        } catch (RemoteCallException exception) {
            throw new BizException(
                    AuthResponseCodeEnum.USER_SERVICE_CALL_FAILED
            );
        }
    }

    public FindUserByPhoneResponse findUserByPhone(String phone) {
        FindUserByPhoneRequest request = new FindUserByPhoneRequest();
        request.setPhone(phone);


        try {
            return remoteCallExecutor.required(
                    UserApiConstants.SERVICE_NAME,
                    UserApiConstants.OPERATION_FIND_BY_PHONE,
                    () -> userFeignApi.findByPhone(request)
            );
        } catch (RemoteBusinessException exception) {
            log.warn(
                    "用户服务查询失败，errorCode={}, message={}",
                    exception.getRemoteErrorCode(),
                    exception.getRemoteErrorMessage()
            );

            if (exception.hasErrorCode(
                    UserResponseCodeEnum.USER_NOT_FOUND)) {
                throw new BizException(
                        AuthResponseCodeEnum.USER_NOT_FOUND
                );
            }

            throw new BizException(
                    AuthResponseCodeEnum.USER_SERVICE_CALL_FAILED
            );
        } catch (RemoteCallException exception) {
            throw new BizException(
                    AuthResponseCodeEnum.USER_SERVICE_CALL_FAILED
            );
        }
    }
}
