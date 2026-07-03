import hyRequest from "../index.ts";

import type { ResponseData } from '../types'
import {UnwrapNestedRefs} from "vue";
import {IAddCommunityInfo} from "./types.ts";

enum CommunityAPI {
    communityQuery = '/manager/community/page',
    registerCommunityQuery = '/manager/community/communityInfo',
    communityInfoUpdate = '/manager/community/communityInfo',
    streetCascadeQuery = '/manager/community/street',
    communityInsert = '/manager/community/communityInfo'
}

export function getCommunityByPageAPI(communitySelectFrom: UnwrapNestedRefs<{
    pageSize: number;
    managerPhone: string;
    communityName: string;
    page: number;
    managerName: string;
    streetId: any[]
}>){
    return hyRequest.get<ResponseData>({
        url: CommunityAPI.communityQuery,
        params: {
            pageSize: communitySelectFrom.pageSize,
            managerPhone: communitySelectFrom.managerPhone,
            communityName: communitySelectFrom.communityName,
            page: communitySelectFrom.page,
            managerName: communitySelectFrom.managerName,
            streetId: '' + communitySelectFrom.streetId.join(",")
        }
    })
}

export function getRegisterCommunityInfoAPI(){
    return hyRequest.get<ResponseData>({
        url: CommunityAPI.registerCommunityQuery,
    })
}

export function updateCommunityAPI(data){
    return hyRequest.put<ResponseData>({
        url: CommunityAPI.communityInfoUpdate,
        data: data
    })
}

export function getStreetCascadeInfoAPI(level: number, id: string){
    return hyRequest.get<ResponseData>({
        url: CommunityAPI.streetCascadeQuery,
        params: {
            level: level,
            id: id,
        }
    })
}

export function postCommunityAPI(communityInfo: IAddCommunityInfo){
    return hyRequest.post<ResponseData>({
        url: CommunityAPI.communityInsert,
        data: communityInfo
    })
}