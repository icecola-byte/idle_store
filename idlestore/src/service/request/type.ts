import type { AxiosRequestConfig, AxiosResponse} from "axios";

// 1. 拦截器类型, T 是响应 res.data 的类型
export interface HYRequestInterceptors<T = any> {
    requestInterceptor?: (config: AxiosRequestConfig) => AxiosRequestConfig
    requestInterceptorCatch?: (error: any) => any
    responseInterceptor?: (
        res: AxiosResponse<T>
    ) => AxiosResponse<T> | Promise<AxiosResponse<T>>
    responseInterceptorCatch?: (error: any) => any
}

export interface HYRequestConfig<T = any> extends AxiosRequestConfig{
    interceptors?: HYRequestInterceptors<T>
    showLoading?: boolean
}