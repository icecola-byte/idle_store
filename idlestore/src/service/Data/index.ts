import hyRequest from "../index.ts";

import type { ResponseData } from '../types'


enum DataAPI {
    userDataByStatusQuery = '/manager/data/user/status',
    commodityDataByStatusQuery = '/manager/data/commodity/status',
    orderDataByStatusQuery = '/manager/data/order/status',
    commodityDataByCategoryQuery = '/manager/data/commodity/category',
    orderDataByDealQuery = '/manager/data/order/deal',
    orderDataByDealAndCategoryQuery = '/manager/data/order/deal/category'
}

export function getUserDataByStatus(communityId: string){
    return hyRequest.get<ResponseData>({
        url: DataAPI.userDataByStatusQuery,
        params: {
            communityId: communityId
        }
    })
}
export function getCommodityDataByStatus(communityId: string){
    return hyRequest.get<ResponseData>({
        url: DataAPI.commodityDataByStatusQuery,
        params: {
            communityId: communityId
        }
    })
}
export function getOrderDataByStatus(communityId: string){
    return hyRequest.get<ResponseData>({
        url: DataAPI.orderDataByStatusQuery,
        params: {
            communityId: communityId
        }
    })
}

export function getCommodityDataByCategory(communityId: string | null, year: number | null){
    return hyRequest.get<ResponseData>({
        url: DataAPI.commodityDataByCategoryQuery,
        params: {
            communityId: communityId,
            year: year
        }
    })
}

export function getOrderDataByDealAndCategory(communityId: string | null, categoryId: number | null){
    return hyRequest.get<ResponseData>({
        url: DataAPI.orderDataByDealAndCategoryQuery,
        params: {
            communityId: communityId,
            categoryId: categoryId
        }
    })
}

export function getOrderDataByDealQuery(communityId: string){
    return hyRequest.get<ResponseData>({
        url: DataAPI.orderDataByDealQuery,
        params: {
            communityId: communityId,
        }
    })
}