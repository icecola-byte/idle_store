import { defineStore } from "pinia";
import emitter from "../utils/event.bus.ts";
import {managerInfoStore} from "./manager.info.ts";

export const homeTagsStore = defineStore('homeTags', {
    state: () => {
        if(!managerInfoStore().managerInfo.isSuper){
            return {
                tabNumber: 1 as number,
                tabsSelectedValue: '1' as string,
                tabsArray: [{
                    title: '待办事项',
                    name: '1',
                    content: 'BackLog',
                    closeable: false
                }] as Array<{title:string, name: string, content: string, closeable: boolean}>,
                navigationMenu: [
                    {index: '1-1', title: '商品审核', content: 'CommodityAudit'},
                    {index: '1-2', title: '上门回收', content: 'OrderAudit'},
                    {index: '2', title: '用户管理', content: 'UserManage'},
                    {index: '3-1', title: '社区活动', content: 'ActivityManage'},
                    {index: '3-2', title: '数据统计', content: 'DataVisualization'},
                    {index: '4', title: '商品管理', content: 'CommodityManage'},
                    {index: '5', title: '订单管理', content: 'OrderManage'}
                ]
            }
        }else {
            return {
                tabNumber: 1 as number,
                tabsSelectedValue: '1' as string,
                tabsArray: [] as Array<{title:string, name: string, content: string, closeable: boolean}>,
                navigationMenu: [
                    {index: '1', title: '社区管理', content: 'CommunityManage'},
                    {index: '2', title: '数据统计', content: 'DataVisualization'},
                ]
            }
        }


    },
    actions: {
        addTab(tabName: string){
            // 先判断是否已经存在
            if(this.tabsArray.filter(tab => tab.title === tabName).length === 0){
                const newTabName = `${++this.tabNumber}`;
                this.tabsArray.push({
                    title: tabName,
                    name: newTabName,
                    content: this.navigationMenu.filter(menu => menu.title === tabName)[0].content,
                    closeable: true
                });
                this.tabsSelectedValue = newTabName
            }else {
                for(let i = 0; i < this.tabsArray.length; i++){
                    if(this.tabsArray[i].title === tabName){
                        this.tabsSelectedValue = this.tabsArray[i].name
                        break;
                    }
                }
            }

        },
        removeTab(targetName: string){
            let activeName = this.tabsSelectedValue;
            // 如果移除的是已激活的标签, 那么激活标签要进行替换
            if(targetName === activeName){
                this.tabsArray.forEach((tab, index) => {
                    if(tab.name === targetName){
                        const nextTab = this.tabsArray[index + 1] || this.tabsArray[index - 1]
                        if(nextTab){
                            activeName = nextTab.name
                        }
                    }
                })
            }

            this.tabsSelectedValue = activeName;
            //this.tabNumber--;
            this.tabsArray = this.tabsArray.filter((tab) => tab.name !== targetName)
        },
        refresh(){
            emitter.emit("refresh", this.tabsArray[Number(this.tabsSelectedValue) - 1 ].title);
        },
        setState(isManager: boolean) {
            if(isManager){
                this.tabNumber = 1;
                this.tabsSelectedValue = '1';
                this.tabsArray.length = 0;
                this.navigationMenu.length = 0;
                this.navigationMenu.push({index: '1', title: '社区管理', content: 'CommunityManage'});
                this.navigationMenu.push({index: '2', title: '数据统计', content: 'DataVisualization'});
            }else{
                this.tabNumber = 1;
                this.tabsSelectedValue = '1';
                this.tabsArray.length = 0;
                this.tabsArray.push({
                    title: '待办事项',
                    name: '1',
                    content: 'BackLog',
                    closeable: false
                });
                this.navigationMenu.length = 0;
                this.navigationMenu.push({index: '1-1', title: '商品审核', content: 'CommodityAudit'});
                this.navigationMenu.push({index: '1-2', title: '上门回收', content: 'OrderAudit'});
                this.navigationMenu.push({index: '2', title: '用户管理', content: 'UserManage'});
                this.navigationMenu.push({index: '3-1', title: '社区活动', content: 'ActivityManage'});
                this.navigationMenu.push({index: '3-2', title: '数据统计', content: 'DataVisualization'});
                this.navigationMenu.push({index: '4', title: '商品管理', content: 'CommodityManage'});
                this.navigationMenu.push({index: '5', title: '订单管理', content: 'OrderManage'});
            }
        }
    },
    // persist: true
});