package com.lh.idlestore.chat.remote;

import com.lh.framework.common.exception.BizException;
import com.lh.idlestore.chat.enums.ChatResponseCodeEnum;
import com.lh.idlestore.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 分布式ID生成服务远程调用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedIdGeneratorRemoteService {

    private final DistributedIdGeneratorFeignApi idGeneratorFeignApi;

    public Long nextId(String key) {
        try {
            String result = idGeneratorFeignApi.getSnowflakeId(key);
            long id = Long.parseLong(result);

            if (id <= 0) {
                throw new IllegalStateException("分布式ID必须大于0");
            }
            return id;
        } catch (Exception exception) {
            log.error("调用分布式ID服务失败，key={}", key, exception);
            throw new BizException(ChatResponseCodeEnum.GENERATE_ID_FAILED);
        }
    }
}
