package com.lh.idlestore.gateway.filter;

import com.lh.idlestore.gateway.config.GatewayConfigProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 拦截内部端点请求：只允许服务间 Feign 直连，不允许经网关外部访问。
 * 拦截路径前缀通过 {@code gateway.internal-paths.blocked-prefixes} 配置。
 */
@Component
public class InternalEndpointBlockFilter implements GlobalFilter, Ordered {

    private final GatewayConfigProperties properties;

    public InternalEndpointBlockFilter(GatewayConfigProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        for (String prefix : properties.getBlockedPrefixes()) {
            if (path.startsWith(prefix)) {
                exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
                return exchange.getResponse().setComplete();
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
