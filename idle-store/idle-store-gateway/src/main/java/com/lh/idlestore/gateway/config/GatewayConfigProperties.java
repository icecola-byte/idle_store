package com.lh.idlestore.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Gateway 内部端点拦截配置。
 */
@Component
@ConfigurationProperties(prefix = "gateway.internal-paths")
public class GatewayConfigProperties {

    /**
     * 禁止经网关外部访问的路径前缀列表。
     */
    private List<String> blockedPrefixes = new ArrayList<>();

    public List<String> getBlockedPrefixes() {
        return blockedPrefixes;
    }

    public void setBlockedPrefixes(List<String> blockedPrefixes) {
        this.blockedPrefixes = blockedPrefixes;
    }
}
