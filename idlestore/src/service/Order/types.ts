export interface IOrderSelectCondition{

    communityId: string,
    buyerPhone?: string,
    buyerName?: string,
    sellerPhone?: string,
    sellerName?: string,
    categoryId?: Array<number>,
    status?: Array<number>,
    logisticsMode?: Array<number>,
    commodityName?: string,
    page: number,
    pageSize: number,
}

export interface IOrderStatusAllResult{
    statusMap: object,
}