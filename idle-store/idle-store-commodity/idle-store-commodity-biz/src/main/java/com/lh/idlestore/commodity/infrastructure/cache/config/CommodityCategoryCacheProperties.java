package com.lh.idlestore.commodity.infrastructure.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@ConfigurationProperties(prefix = "commodity.cache.category")
@Component
@Data
public class CommodityCategoryCacheProperties {

    private Duration delayedInvalidationDelay = Duration.ofSeconds(3);
}
