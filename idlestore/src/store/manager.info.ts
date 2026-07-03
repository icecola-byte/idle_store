import {defineStore} from "pinia";
import {ILoginResult} from "../service/Login/types.ts";

export const managerInfoStore = defineStore('managerInfo', {
    state: () => {
        return {
            managerInfo:{} as ILoginResult
        }
    },
    actions: {
        setManagerInfo(managerInfo: ILoginResult){
            this.managerInfo = managerInfo
        }
    },
    persist: true
});