package com.lh.framework.biz.operationlog.aspect;

import com.lh.framework.biz.operationlog.annotation.ApiOperationLog;
import com.lh.framework.biz.operationlog.support.OperationLogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * 统一记录接口操作、耗时以及经过脱敏的请求和响应数据。
 */
@Aspect
@Slf4j
@RequiredArgsConstructor
public class ApiOperationLogAspect {

    private final OperationLogSanitizer sanitizer;

    @Around("@annotation(operationLog)")
    public Object doAround(
            ProceedingJoinPoint joinPoint,
            ApiOperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature =
                (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();
        String requestData = operationLog.logRequest()
                ? sanitizer.sanitize(joinPoint.getArgs())
                : "<disabled>";

        log.info(
                "请求开始，description={}，class={}，method={}，request={}",
                operationLog.description(),
                className,
                methodName,
                requestData
        );

        try {
            Object result = joinPoint.proceed();
            String responseData = operationLog.logResponse()
                    ? sanitizer.sanitize(result)
                    : "<disabled>";

            log.info(
                    "请求完成，description={}，cost={}ms，response={}",
                    operationLog.description(),
                    System.currentTimeMillis() - startTime,
                    responseData
            );
            return result;
        } catch (Throwable throwable) {
            log.warn(
                    "请求失败，description={}，cost={}ms，exception={}",
                    operationLog.description(),
                    System.currentTimeMillis() - startTime,
                    throwable.getClass().getSimpleName()
            );
            throw throwable;
        }
    }

}
