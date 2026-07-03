package com.lh.framework.threadpool.core;

import com.lh.framework.threadpool.properties.ThreadPoolsProperties;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolExecutorFactory {

    public ThreadPoolTaskExecutor create(ThreadPoolsProperties.ExecutorProperties properties) {

        validate(properties);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.setKeepAliveSeconds(properties.getKeepAliveSeconds());
        executor.setThreadNamePrefix(properties.getThreadNamePrefix());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(
                properties.getAwaitTerminationSeconds()
        );
        executor.setRejectedExecutionHandler(
                createRejectedExecutionHandler(
                        properties.getRejectionPolicy()
                )
        );

        // 不调用 initialize()，由 Spring 管理 Bean 生命周期
        return executor;
    }

    private void validate(ThreadPoolsProperties.ExecutorProperties properties) {

        Assert.notNull(
                properties,
                "Thread pool properties must not be null"
        );
        Assert.isTrue(
                properties.getCorePoolSize() > 0,
                "core-pool-size must be greater than 0"
        );
        Assert.isTrue(
                properties.getMaxPoolSize()
                        >= properties.getCorePoolSize(),
                "max-pool-size must be greater than or equal "
                        + "to core-pool-size"
        );
        Assert.isTrue(
                properties.getQueueCapacity() >= 0,
                "queue-capacity must be greater than or equal to 0"
        );
        Assert.isTrue(
                properties.getKeepAliveSeconds() >= 0,
                "keep-alive-seconds must be greater than or equal to 0"
        );
        Assert.isTrue(
                properties.getAwaitTerminationSeconds() >= 0,
                "await-termination-seconds must be greater "
                        + "than or equal to 0"
        );
    }

    private RejectedExecutionHandler
    createRejectedExecutionHandler(
            ThreadPoolsProperties.RejectionPolicy policy) {

        Assert.notNull(
                policy,
                "Thread pool rejection policy must not be null"
        );

        return switch (policy) {
            case CALLER_RUNS ->
                    new ThreadPoolExecutor.CallerRunsPolicy();
            case ABORT ->
                    new ThreadPoolExecutor.AbortPolicy();
            case DISCARD ->
                    new ThreadPoolExecutor.DiscardPolicy();
            case DISCARD_OLDEST ->
                    new ThreadPoolExecutor.DiscardOldestPolicy();
        };
    }
}
