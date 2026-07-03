package com.lh.framework.threadpool.autoconfigure;

import com.lh.framework.threadpool.core.ThreadPoolExecutorFactory;
import com.lh.framework.threadpool.properties.ThreadPoolsProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(ThreadPoolsProperties.class)
public class ThreadPoolAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ThreadPoolExecutorFactory.class)
    public ThreadPoolExecutorFactory threadPoolExecutorFactory() {
        return new ThreadPoolExecutorFactory();
    }
}
