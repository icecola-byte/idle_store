import hyRequest from "../index.ts";

import type { ResponseData } from '../types'
import {
    ICommodityCategoryAllResult,
    ICommodityLogisticsModeAllResult,
    ICommoditySelectCondition,
    ICommodityStatusAllResult
} from "./types.ts";


enum CommodityAPI {
    logisticsQuery = '/manager/commodity/logistics/all',
    categoryQuery = '/manager/commodity/category/all',
    statusQuery = '/manager/commodity/status/all',
    commodityQuery = '/manager/commodity/manager/commodityInfo/page',
    commodityAudit = '/manager/commodity/manage/commodityInfo'
}

export function getCommodityLogisticsMode(){
    return hyRequest.get<ResponseData<ICommodityLogisticsModeAllResult>>({
        url: CommodityAPI.logisticsQuery,
    })
}

export function getCommodityCategory(){
    return hyRequest.get<ResponseData<ICommodityCategoryAllResult>>({
        url: CommodityAPI.categoryQuery,
    })
}

export function getCommodityStatus(){
    return hyRequest.get<ResponseData<ICommodityStatusAllResult>>({
        url: CommodityAPI.statusQuery,
    })
}

export function getCommodityInfo(condition: ICommoditySelectCondition) {
    return hyRequest.post<ResponseData<any>>({
        url: CommodityAPI.commodityQuery,
        data: condition,
    })
}

export function auditCommodity(commodityId: string, operation: number, managerId: number){
    return hyRequest.put<ResponseData>({
        url: CommodityAPI.commodityAudit + '/' + commodityId + '/' + operation + '/' + managerId
    })
}