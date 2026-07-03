import hyRequest from "../index.ts";

import type { IBackLogData } from './types'
import type { ResponseData } from '../types'


enum BacklogAPI {
    BackLogList = '/manager/backLog/list',
}

export function getBackLogList(communityId: string) {
    return hyRequest.get<ResponseData<IBackLogData>>({
        url: BacklogAPI.BackLogList,
        params: {
            communityId: communityId
        },
        showLoading: true,
    })
}