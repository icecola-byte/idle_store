package com.lh.framework.openfeign.exception;

import com.lh.framework.common.exception.BaseExceptionInterface;
import lombok.Getter;

import java.util.Objects;

/**
 * 下游服务返回业务失败时抛出的异常。
 *
 * <p>它表示 HTTP 请求和响应协议都是正常的，但统一响应中的 success=false。
 * 下游返回的稳定错误码和错误信息会被原样保存，调用方可以识别特定错误码，
 * 再转换为当前服务的业务语义。</p>
 */
@Getter
public class RemoteBusinessException extends RemoteCallException {

    /** 下游服务返回的业务错误码，例如 USER-20007。 */
    private final String remoteErrorCode;

    /** 下游服务返回的业务错误信息。 */
    private final String remoteErrorMessage;

    /**
     * @param service 下游服务名称
     * @param operation 下游操作名称
     * @param errorCode 下游业务错误码
     * @param errorMessage 下游业务错误信息
     */
    public RemoteBusinessException(
            String service,
            String operation,
            String errorCode,
            String errorMessage) {
        super(service, operation, errorMessage, null);
        this.remoteErrorCode = errorCode;
        this.remoteErrorMessage = errorMessage;
    }

    /**
     * 判断下游错误码是否与指定的错误码定义一致。
     *
     * @param error 下游 API 模块公开的错误码定义
     * @return 错误码相同时返回 true
     */
    public boolean hasErrorCode(BaseExceptionInterface error) {
        return Objects.equals(
                remoteErrorCode,
                error.getErrorCode()
        );
    }
}
