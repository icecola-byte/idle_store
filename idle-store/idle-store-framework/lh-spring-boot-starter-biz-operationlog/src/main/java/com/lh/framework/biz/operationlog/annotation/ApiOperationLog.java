package com.lh.framework.biz.operationlog.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface ApiOperationLog {
    /**
     * API 功能描述
     *
     * @return
     */
    @AliasFor("value")
    String description() default "";

    @AliasFor("description")
    String value() default "";

    /**
     * 是否记录请求参数。
     */
    boolean logRequest() default true;

    /**
     * 是否记录响应结果。
     */
    boolean logResponse() default true;

}