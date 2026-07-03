<template>
  <ActivityAddDialog/>
    <el-table :data="activityData"
              stripe
              border
              size="small"
              highlight-current-row
              row-key="activityId"
              table-layout="auto">
        <el-table-column fixed type="index" label="序号" :index="index => index + 1 + (pageInfo.currentPage - 1) * pageInfo.pageSize" align="center"/>

        <el-table-column prop="startTime" label="开始时间" align="center" sortable>
            <template #default="scope">
                <div style="display: flex; align-items: center; justify-content: space-evenly">
                    <span>{{scope.row.startTime.substring(0,16)}}</span>
                </div>
            </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" align="center" sortable>
            <template #default="scope">
                <div style="display: flex; align-items: center; justify-content: space-evenly">
                    <span >{{scope.row.endTime.substring(0,16)}}</span>
                </div>
            </template>
        </el-table-column>
        <el-table-column prop="activityManager" label="负责人" align="center" width="100px"/>
        <el-table-column prop="managerPhone" label="联系方式" align="center"/>
        <el-table-column prop="tradingPlace" label="活动地点" align="center"/>
        <el-table-column prop="commodityNumber" label="商品数量" align="center" sortable width="100px"/>
        <el-table-column prop="soldOutNumber" label="售出数量" align="center" sortable width="100px"/>
        <el-table-column prop="participantNumber" label="商家参与数量" align="center" sortable width="150px"/>
        <el-table-column label="状态" align="center">
            <template #default="scope">
                <div style="display: flex; align-items: center; justify-content: space-evenly">
                    <span>{{dateUtil.parseDateTime(scope.row.startTime) > Date.now() ? '未开始' : (dateUtil.parseDateTime(scope.row.endTime) < Date.now() ? '已结束' : '进行中')}}</span>
                </div>
            </template>
        </el-table-column>
        <el-table-column label="操作" align="center">
            <template #default="scope">
                <div style="display: flex; align-items: center; justify-content: space-evenly">
                    <el-tag type="primary" @click="seeActivityCommodity(scope.row.activityId)">查看商品</el-tag>
                </div>
            </template>
        </el-table-column>
    </el-table>
    <el-drawer
        v-model="commodityDrawer"
        title="商品列表"
        :append-to-body="true"
    >
        <div class="activity-commodity-container">
            <template v-for="item in activityCommodity" :key="commodityId">
                <div class="activity-commodity-item">
                    <div class="name">
                        <span>{{item.name}}</span>
                    </div>
                    <div class="picture-number">
                        <div class="picture">
                            <el-image class="commodity-picture" :src="item.pictureUrl" fit="cover"></el-image>
                        </div>
                        <div class="number">
                            <span>总数：{{item.amount}}</span>
                            <span>已售出：{{item.soldOutNumber}}</span>
                        </div>
                    </div>
                </div>
            </template>
        </div>
    </el-drawer>
</template>

<script setup lang="ts">

import ActivityAddDialog from "./ActivityAddDialog.vue";
import dateUtil from '../../../utils/dateFormat'
import {ref} from "vue";
import {queryActivityCommodityAPI} from "../../../service/Activity/activity.api.ts";

defineProps(["activityData", "pageInfo"]);

let commodityDrawer = ref(false);
const activityCommodity = ref([]);

const seeActivityCommodity = (activityId: number) => {
    commodityDrawer.value = true;
    queryActivityCommodityAPI(activityId).then(res => {
        activityCommodity.value = res.data
    })
}
</script>

<style lang="scss" scoped>
    .activity-commodity-item{
        display: flex;
        flex-direction: column;
        border: 1px solid #f5f5f5;
        padding: 10px;
        box-shadow: 1px 1px 15px #f5f5f5;
        margin-top: 10px;
        .picture-number{
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-top: 10px;
            .picture {
                .commodity-picture{
                    width: 90px;
                    height: 90px;
                    border-radius: 4px;
                }
            }
            .number{
                display: flex;
                flex-direction: column;
                align-items: flex-end;
                color: #a8abb2;
            }
        }
    }
</style>