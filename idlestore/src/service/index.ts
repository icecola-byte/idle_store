// service统一出口
import HYRequest from './request'
import { BASE_URL, TIME_OUT } from './request/config'
import localCache from '../utils/cache'

const hyRequest = new HYRequest({
    baseURL: BASE_URL,
    timeout: TIME_OUT,
    // showLoading: true, // 全局配置显示loading
    interceptors: {
        requestInterceptor: (config) => {
            // console.log('单个实例-请求成功的拦截')
            // 携带token的拦截
            const token = localCache.getCache('token')
            if (token && config.headers) {
                config.headers.Authorization = `Bearer ${token}`
            }
            return config
        },
        requestInterceptorCatch: (err) => {
            // console.log('单个实例-请求失败的拦截')
            return err
        },
        responseInterceptor: (res) => {
            // console.log('单个实例-响应成功的拦截')
            return res
        },
        responseInterceptorCatch: (err) => {
            // console.log('单个实例-响应失败的拦截')
            return err
        }
    }
})

export default hyRequest
