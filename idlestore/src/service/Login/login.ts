import hyRequest from "../index.ts";
import type { IAccount, ILoginResult, ISendCodeReq } from './types'
import type { ResponseData } from '../types'

enum LoginAPI {
    AdminLogin = '/admin/login',
    SendCode = '/verification/code/send',
}

// 管理端登录（POST）
export function getAdminLoginRequest(account: IAccount) {
    return hyRequest.post<ResponseData<ILoginResult>>({
        url: LoginAPI.AdminLogin,
        data: account,
        showLoading: true
    })
}

// 发送短信验证码
export function sendVerificationCode(data: ISendCodeReq) {
    return hyRequest.post<ResponseData<null>>({
        url: LoginAPI.SendCode,
        data: data,
    })
}
