package com.lh.idlestore.oss.api;

import com.lh.idlestore.oss.api.config.FeignFormConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import com.lh.idlestore.oss.constant.OssApiConstants;
import com.lh.framework.common.response.Response;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import com.lh.idlestore.oss.dto.FileUploadResp;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = OssApiConstants.SERVICE_NAME,
        configuration = FeignFormConfiguration.class
)
public interface FileFeignApi {

    /**
     * 上传文件并返回文件元数据 ID。
     */
    @PostMapping(
            value = OssApiConstants.API_PREFIX + "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    Response<FileUploadResp> uploadFile(@RequestPart(value = "file") MultipartFile file);

    /**
     * 按文件 ID 批量获取访问 URL。
     */
    @PostMapping(value = OssApiConstants.API_PREFIX + "/access-urls")
    Response<Map<Long, String>> getAccessUrls(@RequestBody List<Long> fileIds);

    /**
     * 删除文件对象。
     */
    @DeleteMapping(value = OssApiConstants.API_PREFIX + "/{fileId}")
    Response<Void> deleteFile(@PathVariable("fileId") Long fileId);


}
