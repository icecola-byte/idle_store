export interface ICommoditySelectCondition{

    communityId: string,
    phone?: string,
    username?: string,
    categoryId?: Array<number>,
    status?: Array<number>,
    logisticsMode?: number,
    drainagePlan?: number,
    commodityName?: string,
    isDelete?:number,
    page: number,
    pageSize: number,
}

export interface ICommodityLogisticsModeAllResult{
    logisticsMap: object,
}

export interface ICommodityCategoryAllResult{
    categoryMap: Array<object>,
}

export interface ICommodityStatusAllResult{
    statusMap: object,
}