export interface ICommodityBackLogInfo{
    number: number,
    longestTime: string,
}

export interface IOrderBackLogInfo{
    number: number,
    todayNumber: number,
    tomorrowNumber: number,
    notCompleted: number
}

export interface IBackLogData {
    commodityBackLogInfo: ICommodityBackLogInfo,
    orderBackLogInfo: IOrderBackLogInfo
}