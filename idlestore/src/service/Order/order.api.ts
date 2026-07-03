import hyRequest from "../index.ts";

import type { ResponseData } from '../types'
import {
    IOrderSelectCondition,
    IOrderStatusAllResult
} from "./types.ts";


enum OrderAPI {
    statusQuery = '/manager/order/status/all',
    orderQuery = '/manager/order/orderInfo',
    cancelOrder = '/manager/order/info',
    completeOrder = '/manager/order/info'
}

export function getOrderStatus(){
    return hyRequest.get<ResponseData<IOrderStatusAllResult>>({
        url: OrderAPI.statusQuery
    })
}

export function getOrderInfo(condition: IOrderSelectCondition){
    return hyRequest.post<ResponseData<any>>({
        url: OrderAPI.orderQuery,
        data: condition
    })
}

export function cancelOrder(orderId: number){
    return hyRequest.delete<ResponseData<any>>({
        url: OrderAPI.cancelOrder + '/' + orderId
    })
}
// TODO: 硬币的发放
export function completeOrder(orderId: number, sellerId: number, coin: number){
    return hyRequest.put<ResponseData<any>>({
        url: OrderAPI.completeOrder,
        params: {
            coin: coin,
        },
        data: {
            orderId: orderId,
            status: 1,
            sellerId: sellerId
        }
    })
}