package com.lh.framework.biz.operationlog.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lh.framework.biz.operationlog.aspect.ApiOperationLogAspect;
import com.lh.framework.biz.operationlog.support.OperationLogSanitizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ApiOperationLogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OperationLogSanitizer operationLogSanitizer(
            ObjectMapper objectMapper) {
        return new OperationLogSanitizer(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiOperationLogAspect apiOperationLogAspect(
            OperationLogSanitizer sanitizer) {
        return new ApiOperationLogAspect(sanitizer);
    }
}
