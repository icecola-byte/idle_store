// 登录请求参数
export interface IAccount {
    phone: string
    code?: string        // 验证码，type=1 时必填
    password?: string    // 密码，type=2 时必填
    type: number         // 1=验证码登录, 2=密码登录
}

// 发送验证码请求
export interface ISendCodeReq {
    phone: string
}

// 管理端登录返回
export interface ILoginResult {
    userId: number
    token: string
    phone: string
    username: string
    avatarUrl: string
    roles: string[]
    permissions: string[]
}
