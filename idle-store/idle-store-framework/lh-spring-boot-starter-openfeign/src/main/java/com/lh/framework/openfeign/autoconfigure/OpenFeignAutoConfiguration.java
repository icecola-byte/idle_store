package com.lh.framework.openfeign.autoconfigure;

import com.lh.framework.openfeign.core.RemoteCallExecutor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * OpenFeign 远程调用增强组件的自动配置。
 *
 * <p>业务模块引入本 starter 后，会自动获得一个 {@link RemoteCallExecutor} Bean，
 * 无需在每个服务中重复声明。若业务模块已经自行定义了同类型 Bean，
 * {@link ConditionalOnMissingBean} 会使这里的默认 Bean 自动让位。</p>
 */
@AutoConfiguration
public class OpenFeignAutoConfiguration {

    /**
     * 创建默认的远程调用执行器。
     *
     * @return 远程调用执行器
     */
    @Bean
    @ConditionalOnMissingBean(RemoteCallExecutor.class)
    public RemoteCallExecutor remoteCallExecutor() {
        return new RemoteCallExecutor();
    }
}
