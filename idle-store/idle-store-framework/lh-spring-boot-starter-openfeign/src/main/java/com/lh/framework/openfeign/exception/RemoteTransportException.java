package com.lh.framework.openfeign.exception;

import lombok.Getter;

/**
 * 远程请求在传输层失败时抛出的异常。
 *
 * <p>常见原因包括连接失败、连接超时、读取超时以及下游返回非预期的
 * HTTP 状态码。原始 Feign 异常会作为 cause 保留。</p>
 */
@Getter
public class RemoteTransportException extends RemoteCallException {

    /**
     * 下游返回的 HTTP 状态码；尚未建立连接时可能为 null。
     */
    private final Integer httpStatus;

    /**
     * @param service 下游服务名称
     * @param operation 下游操作名称
     * @param httpStatus HTTP 状态码；连接失败时可为 null
     * @param cause 原始 Feign 异常
     */
    public RemoteTransportException(
            String service,
            String operation,
            Integer httpStatus,
            Throwable cause) {
        super(service, operation, "远程服务调用失败", cause);
        this.httpStatus = httpStatus;
    }
}
