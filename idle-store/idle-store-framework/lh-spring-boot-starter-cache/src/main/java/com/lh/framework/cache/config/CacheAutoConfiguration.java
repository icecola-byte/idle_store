package com.lh.framework.cache.config;

import com.lh.framework.cache.core.CaffeineCacheFactory;
import com.lh.framework.cache.core.NamedCaffeineCacheRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 本地缓存自动配置。
 *
 * 引入 starter 后，会自动注册 CaffeineCacheFactory 和 NamedCaffeineCacheRegistry。
 */
@AutoConfiguration
@EnableConfigurationProperties(CacheProperties.class)
@ConditionalOnProperty(
        prefix = "cache.caffeine",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class CacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CaffeineCacheFactory caffeineCacheFactory() {
        return new CaffeineCacheFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public NamedCaffeineCacheRegistry namedCaffeineCacheRegistry(
            CacheProperties properties,
            CaffeineCacheFactory cacheFactory) {

        return new NamedCaffeineCacheRegistry(
                properties.getCaffeine(),
                cacheFactory
        );
    }
}