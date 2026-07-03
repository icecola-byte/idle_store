import hyRequest from "../index.ts";

import type {IAddUserInfo, IUserInfo, IUserSelectCondition, IUserSelectResult} from './types'
import type { ResponseData } from '../types'


enum UserSelectAPI {
    userSelect = '/manager/user/list',
    userManageTitle = '/manager/user/title',
    userAdd = '/manager/userInfo',
    userUpdate = '/manager/userInfo'
}

export function getUserSelectByPageAPI(condition: IUserSelectCondition) {
    return hyRequest.get<ResponseData<IUserSelectResult>>({
        url: UserSelectAPI.userSelect,
        params: condition,
    })
}

export function getUserManageTitle(){
    return hyRequest.get<ResponseData<Array<Object>>>({
        url: UserSelectAPI.userManageTitle,
    })
}

export function addUserAPI(userInfo: IAddUserInfo){
    return hyRequest.post<ResponseData<boolean>>({
        url: UserSelectAPI.userAdd,
        data: userInfo
    })
}

export function updateUserAPI(userInfo: IUserInfo){
    return hyRequest.put<ResponseData<IUserInfo>>({
        url: UserSelectAPI.userUpdate,
        data: userInfo
    })
}