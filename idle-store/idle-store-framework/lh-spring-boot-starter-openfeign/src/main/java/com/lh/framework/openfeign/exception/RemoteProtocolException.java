package com.lh.framework.openfeign.exception;

/**
 * 下游响应不符合服务间约定时抛出的异常。
 *
 * <p>例如响应对象为 null，或者使用 required 调用时响应 data 为 null。
 * 这通常意味着服务端实现、Feign 接口声明或统一响应封装存在问题。</p>
 */
public class RemoteProtocolException extends RemoteCallException {

    /**
     * @param service 下游服务名称
     * @param operation 下游操作名称
     * @param message 具体的协议错误描述
     */
    public RemoteProtocolException(
            String service,
            String operation,
            String message) {
        super(service, operation, message, null);
    }
}
