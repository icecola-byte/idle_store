package com.lh.framework.threadpool.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "thread-pools")
public class ThreadPoolsProperties {

    private Map<String, ExecutorProperties> executors =
            new LinkedHashMap<>();

    public ExecutorProperties getRequired(String name) {
        ExecutorProperties properties = executors.get(name);
        if (properties == null) {
            throw new IllegalStateException(
                    "Missing thread pool configuration: "
                            + "thread-pools.executors." + name
            );
        }
        return properties;
    }

    @Data
    public static class ExecutorProperties {

        private int corePoolSize = 5;

        private int maxPoolSize = 20;

        private int queueCapacity = 100;

        private int keepAliveSeconds = 60;

        private int awaitTerminationSeconds = 60;

        private String threadNamePrefix;

        private RejectionPolicy rejectionPolicy =
                RejectionPolicy.CALLER_RUNS;
    }

    public enum RejectionPolicy {
        CALLER_RUNS,
        ABORT,
        DISCARD,
        DISCARD_OLDEST
    }
}
