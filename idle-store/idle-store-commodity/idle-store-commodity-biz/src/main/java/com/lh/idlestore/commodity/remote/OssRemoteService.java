package com.lh.idlestore.commodity.remote;

import com.lh.framework.common.exception.BizException;
import com.lh.framework.openfeign.core.RemoteCallExecutor;
import com.lh.framework.openfeign.exception.RemoteCallException;
import com.lh.idlestore.commodity.enums.CommodityResponseCodeEnum;
import com.lh.idlestore.oss.api.FileFeignApi;
import com.lh.idlestore.oss.constant.OssApiConstants;
import com.lh.idlestore.oss.dto.FileUploadResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


/**
 * OSS 文件服务远程调用。
 */
@Component
@RequiredArgsConstructor
public class OssRemoteService {

    private final FileFeignApi fileFeignApi;
    private final RemoteCallExecutor remoteCallExecutor;

    public FileUploadResp uploadFile(MultipartFile file) {
        try {
            return remoteCallExecutor.required(
                    OssApiConstants.SERVICE_NAME,
                    OssApiConstants.OPERATION_UPLOAD_FILE,
                    () -> fileFeignApi.uploadFile(file)
            );
        } catch (RemoteCallException exception) {
            throw new BizException(CommodityResponseCodeEnum.OSS_SERVICE_CALL_FAILED);
        }
    }

    public Map<Long, String> getAccessUrls(List<Long> fileIds) {
        try {
            return remoteCallExecutor.required(
                    OssApiConstants.SERVICE_NAME,
                    OssApiConstants.OPERATION_GET_ACCESS_URLS,
                    () -> fileFeignApi.getAccessUrls(fileIds)
            );
        } catch (RemoteCallException exception) {
            throw new BizException(CommodityResponseCodeEnum.OSS_SERVICE_CALL_FAILED);
        }
    }

    public void deleteFile(Long fileId) {
        try {
            remoteCallExecutor.execute(
                    OssApiConstants.SERVICE_NAME,
                    OssApiConstants.OPERATION_DELETE_FILE,
                    () -> fileFeignApi.deleteFile(fileId)
            );
        } catch (RemoteCallException exception) {
            throw new BizException(CommodityResponseCodeEnum.OSS_SERVICE_CALL_FAILED);
        }
    }

}
