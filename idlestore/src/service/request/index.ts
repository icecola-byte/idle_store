import axios from 'axios'
import type { AxiosInstance} from "axios";
import type {HYRequestInterceptors, HYRequestConfig} from "./type.ts";

import 'element-plus/es/components/loading/style/css'
import {ElLoading, ElMessage} from "element-plus";
import router from "../../router";
const DEFAULT_LOADING = false

class HYRequest<T = any> {
    instance: AxiosInstance
    interceptors?: HYRequestInterceptors
    showLoading?: boolean
    loading?: any // loading 组件实例
    constructor(config: HYRequestConfig<T>) {
        this.instance = axios.create(config)
        this.showLoading =
            config.showLoading === undefined ? DEFAULT_LOADING : config.showLoading

        this.interceptors = config.interceptors
        this.instance.interceptors.request.use(
            this.interceptors?.requestInterceptor,
            this.interceptors?.requestInterceptorCatch
        )
        this.instance.interceptors.response.use(
            this.interceptors?.responseInterceptor,
            this.interceptors?.responseInterceptorCatch
        )

        // 给所有实例添加全局的拦截器
        this.instance.interceptors.request.use(
            (config) => {
                // console.log('所有的实例都有的拦截器: 请求成功拦截')
                if (this.showLoading) {
                    this.loading = ElLoading.service({
                        lock: true,
                        text: 'Loading',
                        background: 'rgba(0, 0, 0, 0.2)',
                        fullscreen: true
                    })
                }
                return config
            },
            (err) => {
                // console.log('所有的实例都有的拦截器: 请求失败拦截')
                return err
            }
        )
        this.instance.interceptors.response.use(
            (res) => {
                // console.log('所有的实例都有的拦截器: 响应成功拦截')
                this.loading?.close() // 将loading移除
                if (res.response && res.response.status == 401) {

                    router.replace("/login");
                    ElMessage({
                        type: 'warning',
                        message: 'token已过期, 请重新登录'
                    })
                }
                // 这里还可以对返回的数据进行判断
                return res.data
            },
            (err) => {
                // console.log('所有的实例都有的拦截器: 响应失败拦截')

                this.loading?.close() // 将loading移除
                // 例子: 判断不同的HttpErrorCode显示不同的错误信息
                return err
            }
        )

    }
    request<T = any>(config: HYRequestConfig): Promise<T> {
        return new Promise((resolve, reject) => {
            // 判断某个请求是否需要显示loading
            if (config.showLoading === false) {
                this.showLoading = config.showLoading
            }
            this.instance
                .request<T, T>(config)
                .then((res) => {
                    // 3.将结果resolve返回出去
                    resolve(res)
                })
                .catch((err) => {
                    reject(err)
                    return err
                })
                .finally(() => {
                    // 将showLoading设置true, 这样不会影响下一个请求
                    this.showLoading = DEFAULT_LOADING
                })
        })
    }

    get<T = any>(config: HYRequestConfig<T>): Promise<T> {
        return this.request<T>({ ...config, method: 'GET' })
    }
    post<T = any>(config: HYRequestConfig<T>): Promise<T> {
        return this.request<T>({ ...config, method: 'POST' })
    }
    delete<T = any>(config: HYRequestConfig<T>): Promise<T> {
        return this.request<T>({ ...config, method: 'DELETE' })
    }

    put<T = any>(config: HYRequestConfig<T>): Promise<T> {
        return this.request<T>({ ...config, method: 'PUT' })
    }
}

export default HYRequest


