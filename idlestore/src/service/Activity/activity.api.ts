
import {IAddActivityInfo, ISelectActivityCondition} from "./types.ts";
import hyRequest from "../index.ts";
import {ResponseData} from "../types.ts";

enum ActivityAPI {
    addActivity= '/manager/activity/info',
    queryActivity = '/manager/activity/info/page',
    queryActivityCommodity = '/manager/activity/commodity/info'
}

export function addActivityAPI(activityInfo: IAddActivityInfo){
    return hyRequest.post<ResponseData<any>>({
        url: ActivityAPI.addActivity,
        data: activityInfo,
    })
}

export function queryActivityAPI(condition: ISelectActivityCondition){
    return hyRequest.post<ResponseData<any>>({
        url: ActivityAPI.queryActivity,
        data: condition
    })
}
export function queryActivityCommodityAPI(activityId: number){
    return hyRequest.get<ResponseData<any>>({
        url: ActivityAPI.queryActivityCommodity,
        params: {
            activityId: activityId
        }
    })
}