package com.lh.idlestore.gateway.filter;

import cn.dev33.satoken.reactor.context.SaReactorHolder;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.lh.framework.common.constant.GlobalConstants.USER_ID;

/**
 * 转发请求时，将用户 ID 添加到 Header 请求头中，透传给下游服务
 **/
@Component
@Slf4j
@Order(-99)
public class UserIdHeaderFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("==================> TokenConvertFilter");

        return SaReactorHolder.sync(() -> {
            try {
                return Optional.of(StpUtil.getLoginIdAsLong());
            } catch (Exception e) {
                return Optional.<Long>empty();
            }
        }).flatMap(userIdOptional -> {
            // 无论是否登录，都先删除客户端可能伪造的 userId
            ServerWebExchange cleanedExchange = exchange.mutate()
                    .request(builder -> builder.headers(headers -> headers.remove(USER_ID)))
                    .build();

            if (userIdOptional.isEmpty()) {
                return chain.filter(cleanedExchange);
            }
            Long userId = userIdOptional.get();

            ServerWebExchange newExchange = cleanedExchange.mutate()
                    .request(builder -> builder.header(USER_ID, String.valueOf(userId)))
                    .build();

            return chain.filter(newExchange);
        });
    }
}
