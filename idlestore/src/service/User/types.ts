export interface IUserSelectCondition{
    phone?: string,
    username?: string,
    sex?: number,
    status?: number,
    communityId: string
    page: number,
    size: number,
}

export interface IAddUserInfo{
    phone: string,
    username?: string,
    avatarUrl?: string,
    sex: number,
}

export interface IUserInfo{
    userId: number,
    avatarUrl: string,
    username: string,
    phone: string,
    sex: number,
    status: number,
    registerTime: object,
    coinBalance: number,
    addressDetail: string,
    isValid: boolean
}

export interface IUserSelectResult {
    currentPage: number,
    pageSize: number,
    data: Array<IUserInfo>,
    total: number,
}