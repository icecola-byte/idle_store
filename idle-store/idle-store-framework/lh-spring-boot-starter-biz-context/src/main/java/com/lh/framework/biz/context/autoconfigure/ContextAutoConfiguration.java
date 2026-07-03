package com.lh.framework.biz.context.autoconfigure;

import com.lh.framework.biz.context.filter.UserContextFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ContextAutoConfiguration {

    @Bean
    public FilterRegistrationBean<UserContextFilter> filterFilterRegistrationBean() {
        UserContextFilter filter = new UserContextFilter();
        return new FilterRegistrationBean<>(filter);
    }
}
