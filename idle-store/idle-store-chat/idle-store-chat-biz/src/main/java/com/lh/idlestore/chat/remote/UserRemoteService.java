package com.lh.idlestore.chat.remote;

import com.lh.framework.common.exception.BizException;
import com.lh.framework.openfeign.core.RemoteCallExecutor;
import com.lh.framework.openfeign.exception.RemoteBusinessException;
import com.lh.framework.openfeign.exception.RemoteCallException;
import com.lh.idlestore.chat.enums.ChatResponseCodeEnum;
import com.lh.idlestore.user.api.UserFeignApi;
import com.lh.idlestore.user.constant.UserApiConstants;
import com.lh.idlestore.user.dto.request.BatchFindUsersRequest;
import com.lh.idlestore.user.dto.response.UserBriefResponse;
import com.lh.idlestore.user.enums.UserResponseCodeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户服务远程调用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserRemoteService {

    private final UserFeignApi userFeignApi;
    private final RemoteCallExecutor remoteCallExecutor;

    public UserBriefResponse findById(Long userId) {
        try {
            return remoteCallExecutor.required(
                    UserApiConstants.SERVICE_NAME,
                    UserApiConstants.OPERATION_FIND_BY_ID,
                    () -> userFeignApi.findById(userId)
            );
        } catch (RemoteBusinessException exception) {
            if (exception.hasErrorCode(UserResponseCodeEnum.USER_NOT_FOUND)) {
                throw new BizException(ChatResponseCodeEnum.TARGET_USER_NOT_FOUND);
            }

            log.warn(
                    "用户服务查询失败，userId={}，errorCode={}，message={}",
                    userId,
                    exception.getRemoteErrorCode(),
                    exception.getRemoteErrorMessage()
            );
            throw new BizException(ChatResponseCodeEnum.USER_SERVICE_CALL_FAILED);
        } catch (RemoteCallException exception) {
            throw new BizException(ChatResponseCodeEnum.USER_SERVICE_CALL_FAILED);
        }
    }

    public void checkUserExists(Long userId) {
        findById(userId);
    }

    public Map<Long, UserBriefResponse> findByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> distinctUserIds = userIds.stream()
                .filter(Objects::nonNull)
                .filter(userId -> userId > 0)
                .distinct()
                .limit(100)
                .toList();
        if (distinctUserIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            List<UserBriefResponse> users = remoteCallExecutor.required(
                    UserApiConstants.SERVICE_NAME,
                    UserApiConstants.OPERATION_FIND_BY_IDS,
                    () -> userFeignApi.findByIds(
                            BatchFindUsersRequest.builder()
                                    .userIds(distinctUserIds)
                                    .build())
            );
            return users.stream().collect(Collectors.toMap(
                    UserBriefResponse::getUserId,
                    Function.identity(),
                    (first, second) -> first));
        } catch (RemoteBusinessException exception) {
            log.warn(
                    "用户服务批量查询失败，userIds={}，errorCode={}，message={}",
                    distinctUserIds,
                    exception.getRemoteErrorCode(),
                    exception.getRemoteErrorMessage()
            );
            throw new BizException(ChatResponseCodeEnum.USER_SERVICE_CALL_FAILED);
        } catch (RemoteCallException exception) {
            throw new BizException(ChatResponseCodeEnum.USER_SERVICE_CALL_FAILED);
        }
    }
}
