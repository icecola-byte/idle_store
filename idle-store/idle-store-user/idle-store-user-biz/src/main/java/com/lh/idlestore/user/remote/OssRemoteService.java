package com.lh.idlestore.user.remote;

import com.lh.framework.common.exception.BizException;
import com.lh.framework.openfeign.core.RemoteCallExecutor;
import com.lh.framework.openfeign.exception.RemoteCallException;
import com.lh.idlestore.oss.api.FileFeignApi;
import com.lh.idlestore.oss.constant.OssApiConstants;
import com.lh.idlestore.user.enums.UserResponseCodeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * OSS 文件服务远程调用。
 */
@Component
@RequiredArgsConstructor
public class OssRemoteService {

    private final FileFeignApi fileFeignApi;
    private final RemoteCallExecutor remoteCallExecutor;

    public String uploadFile(MultipartFile file) {
        try {
            return remoteCallExecutor.required(
                    OssApiConstants.SERVICE_NAME,
                    OssApiConstants.OPERATION_UPLOAD_FILE,
                    () -> fileFeignApi.uploadFile(file)
            );
        } catch (RemoteCallException exception) {
            throw new BizException(UserResponseCodeEnum.OSS_SERVICE_CALL_FAILED);
        }
    }
}
