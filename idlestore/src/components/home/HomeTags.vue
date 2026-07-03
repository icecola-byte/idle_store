<template>
    <el-tabs
            v-model="tagsInfo.tabsSelectedValue"
            type="card"
            class="demo-tabs"
            @tab-remove="tagsInfo.removeTab"
    >
        <el-tab-pane
                v-for="item in tagsInfo.tabsArray"
                :key="item.name"
                :label="item.title"
                :name="item.name"
                :closable="item.closeable"
        >
            <el-row class="el-tab-pan-container">
                <el-col :span="24">
                    <component :is="getComponent(item.content)" :audit="item.content ==='CommodityAudit'"
                        :orderAudit="item.content === 'OrderAudit'"></component>
                </el-col>
            </el-row>
        </el-tab-pane>
    </el-tabs>
</template>

<script lang="ts" setup>
    import { homeTagsStore } from "../../store/home.tags.ts";
    import BackLog from "../affair/BackLog.vue";
    import CommodityManage from "../affair/commodityManage/CommodityManage.vue";
    import UserManage from "../affair/userManage/UserManage.vue";
    import OrderManage from "../affair/orderManage/OrderManage.vue";
    import ActivityManage from "../affair/activity/ActivityManage.vue";
    import DataManage from "../affair/dataVisualization/DataManage.vue";
    import CommunityManage from "../affair/communityManage/CommunityManage.vue";

    const tagsInfo = homeTagsStore();

    function getComponent(componentName){
        if(componentName === 'BackLog')
            return BackLog;
        else if(componentName === 'CommodityManage'){
            return CommodityManage;
        }else if(componentName === 'UserManage'){
            return UserManage
        }else if(componentName === 'CommodityAudit'){
            return CommodityManage;
        }else if(componentName === 'OrderManage'){
            return OrderManage;
        }else if(componentName === 'OrderAudit'){
            return OrderManage;
        }else if(componentName === 'ActivityManage'){
            return ActivityManage;
        }else if(componentName === 'DataVisualization'){
            return DataManage;
        }else if(componentName === 'CommunityManage'){
            return CommunityManage;
        }
    }
</script>

<style lang="scss" scoped>
    .el-tab-pan-container{
        padding: 32px;
        background-color: #f2f2f2;
        min-height: 89.5vh;
    }
</style>