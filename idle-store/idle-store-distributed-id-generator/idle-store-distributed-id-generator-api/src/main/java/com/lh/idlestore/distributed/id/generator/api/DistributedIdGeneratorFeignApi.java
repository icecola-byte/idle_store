package com.lh.idlestore.distributed.id.generator.api;

import com.lh.idlestore.distributed.id.generator.constant.DistributedIdGeneratorApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = DistributedIdGeneratorApiConstants.SERVICE_NAME)
public interface DistributedIdGeneratorFeignApi {

    @GetMapping(
            value = DistributedIdGeneratorApiConstants.API_PREFIX
                    + "/segment/get/{key}"
    )
    String getSegmentId(@PathVariable("key") String key);

    @GetMapping(
            value = DistributedIdGeneratorApiConstants.API_PREFIX
                    + "/snowflake/get/{key}"
    )
    String getSnowflakeId(@PathVariable("key") String key);

}
