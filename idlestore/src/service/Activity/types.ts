export interface IAddActivityInfo {
    communityId: string,
    startTime: object,
    endTime: object,
    tradingPlace: string,
    activityManager: string,
    managerPhone: string
}

export interface ISelectActivityCondition{
    dates: Array<any>,
    tradingPlace: string,
    name: string,
    communityId: string,
    phone: string,
    page: 1,
    pageSize: 10,
}