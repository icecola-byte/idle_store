package com.lh.framework.openfeign.core;

import com.lh.framework.common.response.Response;
import com.lh.framework.openfeign.exception.RemoteBusinessException;
import com.lh.framework.openfeign.exception.RemoteCallException;
import com.lh.framework.openfeign.exception.RemoteProtocolException;
import com.lh.framework.openfeign.exception.RemoteTransportException;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Feign 远程调用的统一执行入口。
 *
 * <p>该类不负责发起 HTTP 请求，真正的请求仍由传入的 Feign 方法完成。
 * 它负责统一完成以下工作：</p>
 * <ul>
 *     <li>记录调用服务、操作名称和耗时；</li>
 *     <li>将 Feign 网络或 HTTP 异常转换为 {@link RemoteTransportException}；</li>
 *     <li>检查统一响应 {@link Response} 是否为空、是否成功；</li>
 *     <li>根据调用场景决定响应 data 是否允许为空。</li>
 * </ul>
 *
 * <p>业务适配层应捕获 {@link RemoteCallException}，再转换为当前服务自己的
 * 业务错误码，避免直接向上层暴露 Feign 的实现异常。</p>
 */
@Slf4j
public class RemoteCallExecutor {

    /**
     * 执行必须返回业务数据的远程调用。
     *
     * <p>适用于“查询详情”“创建后返回 ID”等成功时 data 必须存在的场景。
     * 响应为空、响应失败或 data 为空都会抛出对应的远程调用异常。</p>
     *
     * @param service 下游服务名称，用于异常上下文和日志，例如 idle-store-user
     * @param operation 本次调用的操作名称，例如 findById
     * @param invocation 实际执行的 Feign 方法
     * @param <T> 响应数据类型
     * @return 非空的响应数据
     */
    public <T> T required(
            String service,
            String operation,
            Supplier<Response<T>> invocation) {

        Response<T> response = invoke(service, operation, invocation);
        validateResponse(service, operation, response);

        if (response.getData() == null) {
            throw new RemoteProtocolException(
                    service, operation, "远程响应数据为空"
            );
        }

        return response.getData();
    }

    /**
     * 执行允许业务数据为空的远程调用。
     *
     * <p>即使 data 允许为空，响应对象本身仍不能为空，且 success 必须为 true。
     * 调用方通过 {@link Optional} 明确处理“查不到数据”等合法空结果。</p>
     *
     * @param service 下游服务名称
     * @param operation 本次调用的操作名称
     * @param invocation 实际执行的 Feign 方法
     * @param <T> 响应数据类型
     * @return 包装响应数据的 Optional
     */
    public <T> Optional<T> optional(
            String service,
            String operation,
            Supplier<Response<T>> invocation) {

        Response<T> response = invoke(service, operation, invocation);
        validateResponse(service, operation, response);
        return Optional.ofNullable(response.getData());
    }

    /**
     * 执行不需要返回业务数据的远程调用。
     *
     * <p>适用于删除、通知、状态更新等只关心调用是否成功的场景。
     * 该方法会检查响应对象和 success 状态，只忽略 data 字段。</p>
     *
     * @param service 下游服务名称
     * @param operation 本次调用的操作名称
     * @param invocation 实际执行的 Feign 方法
     */
    public void execute(
            String service,
            String operation,
            Supplier<? extends Response<?>> invocation) {

        Response<?> response = invoke(service, operation, invocation);
        validateResponse(service, operation, response);
    }

    /**
     * 执行底层调用并统一转换 Feign 异常。
     *
     * <p>通常业务代码应使用 {@link #required(String, String, Supplier)}、
     * {@link #optional(String, String, Supplier)} 或
     * {@link #execute(String, String, Supplier)}。只有返回值不是统一
     * {@link Response} 时，才直接使用此方法。</p>
     *
     * @param service 下游服务名称
     * @param operation 本次调用的操作名称
     * @param invocation 实际调用逻辑
     * @param <T> 原始返回值类型
     * @return 调用的原始返回值
     */
    public <T> T invoke(
            String service,
            String operation,
            Supplier<T> invocation) {

        long start = System.currentTimeMillis();

        try {
            return invocation.get();
        } catch (RemoteCallException exception) {
            throw exception;
        } catch (RetryableException exception) {
            log.error(
                    "远程服务连接失败，service={}, operation={}",
                    service, operation, exception
            );
            throw new RemoteTransportException(
                    service, operation, null, exception
            );
        } catch (FeignException exception) {
            log.error(
                    "远程服务调用失败，service={}, operation={}, status={}",
                    service, operation, exception.status(), exception
            );
            throw new RemoteTransportException(
                    service,
                    operation,
                    exception.status(),
                    exception
            );
        } finally {
            log.debug(
                    "远程调用完成，service={}, operation={}, cost={}ms",
                    service,
                    operation,
                    System.currentTimeMillis() - start
            );
        }
    }

    /**
     * 校验所有统一响应都必须满足的基础协议。
     *
     * <p>响应对象为空属于服务间协议异常；success=false 表示下游明确返回了
     * 业务失败，需要保留下游错误码和错误信息。</p>
     */
    private void validateResponse(
            String service,
            String operation,
            Response<?> response) {

        if (response == null) {
            throw new RemoteProtocolException(
                    service, operation, "远程响应为空"
            );
        }

        if (!response.isSuccess()) {
            throw new RemoteBusinessException(
                    service,
                    operation,
                    response.getErrorCode(),
                    response.getMessage()
            );
        }
    }
}
