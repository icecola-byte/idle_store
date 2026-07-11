package com.lh.idlestore.auth.config;

import com.lh.framework.threadpool.properties.ThreadPoolsProperties;
import com.lh.framework.threadpool.core.ThreadPoolExecutorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration(proxyBeanMethods = false)
public class AuthThreadPoolConfiguration {

    @Bean
    public ThreadPoolTaskExecutor smsExecutor(
            ThreadPoolsProperties properties,
            ThreadPoolExecutorFactory factory) {

        return factory.create(properties.getRequired("sms")
        );
    }
}
