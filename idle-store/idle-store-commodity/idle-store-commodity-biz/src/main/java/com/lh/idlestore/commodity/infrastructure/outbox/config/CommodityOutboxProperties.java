package com.lh.idlestore.commodity.infrastructure.outbox.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@ConfigurationProperties(prefix = "commodity.outbox")
@Component
@Data
public class CommodityOutboxProperties {

    private Duration publishDelay = Duration.ofSeconds(1);
}
