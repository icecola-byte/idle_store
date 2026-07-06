package com.lh.idlestore.oss.api;

import com.lh.idlestore.oss.config.FeignFormConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import com.lh.idlestore.oss.constant.OssApiConstants;
import com.lh.framework.common.response.Response;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
        name = OssApiConstants.SERVICE_NAME,
        configuration = FeignFormConfiguration.class
)
public interface FileFeignApi {

    /**
     * 文件上传
     */
    @PostMapping(
            value = OssApiConstants.API_PREFIX + "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    Response<String> uploadFile(@RequestPart(value = "file") MultipartFile file);


}
