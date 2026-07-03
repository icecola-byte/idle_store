package com.lh.framework.openfeign.exception;

import lombok.Getter;

/**
 * 所有远程调用异常的基类。
 *
 * <p>异常中固定携带下游服务名称和操作名称，便于日志检索和问题定位。
 * 具体失败类型由子类表达：</p>
 * <ul>
 *     <li>{@link RemoteBusinessException}：下游正常响应，但业务处理失败；</li>
 *     <li>{@link RemoteProtocolException}：下游响应不符合约定；</li>
 *     <li>{@link RemoteTransportException}：连接、超时或 HTTP 调用失败。</li>
 * </ul>
 */
@Getter
public abstract class RemoteCallException extends RuntimeException {

    /** 下游服务名称，例如 idle-store-user。 */
    private final String service;

    /** 下游操作名称，例如 findById。 */
    private final String operation;

    /**
     * @param service 下游服务名称
     * @param operation 下游操作名称
     * @param message 异常描述
     * @param cause 原始异常；没有原始异常时可为 null
     */
    protected RemoteCallException(
            String service,
            String operation,
            String message,
            Throwable cause) {
        super(message, cause);
        this.service = service;
        this.operation = operation;
    }
}
