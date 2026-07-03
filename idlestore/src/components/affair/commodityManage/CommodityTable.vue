<template>
    <el-table :data="commodityData"
              stripe
              border
              size="small"
              highlight-current-row
              row-key="commodityId"
              table-layout="auto">
        <el-table-column fixed type="index" label="序号" :index="index => index + 1 + (pageInfo.currentPage - 1) * pageInfo.pageSize" align="center"/>
        <el-table-column label="商品图片" align="center">
            <template #default="scope">
                <div style="display: flex; align-items: center; justify-content: space-evenly;">
                    <template v-for="(item,index) in scope.row.pictureUrl.split('|')" :key="index">
                        <el-image class="commodity-picture"
                                  fit="cover"
                                  preview-teleported
                                  :src="item"
                                  :preview-src-list="scope.row.pictureUrl.split('|')"
                                  :initial-index="index"/>
                    </template>

                </div>
            </template>
        </el-table-column>
        <el-table-column prop="commodityName" label="商品名称" align="center"/>
        <el-table-column prop="username" label="所属用户" align="center"/>
        <el-table-column prop="phone" label="用户手机号" align="center"/>
        <el-table-column prop="logisticsMode" label="物流方式" align="center">
            <template #default="scope">
                <div style="display: flex; align-items: center; justify-content: flex-start;">
                    <template v-for="item in getLogisticsModeList(scope.row.logisticsMode)">
                        <el-tag :type="item.type">{{item.label}}</el-tag>
                    </template>
                </div>
            </template>
        </el-table-column>
        <el-table-column prop="amount" label="总数" align="center" sortable/>
        <el-table-column prop="soldOutNumber" label="销售量" align="center" sortable/>
        <el-table-column prop="price" label="单价" align="center" sortable/>
        <el-table-column prop="publishTime" label="发布时间" align="center" sortable/>
        <el-table-column prop="appointmentTime" label="上门取货时间" align="center" sortable>
            <template #default="scope">
                <div style="display: flex; align-items: center; justify-content: center;">
                    <span>{{scope.row.appointmentTime != null && scope.row.appointmentTime != '' ? scope.row.appointmentTime.split(' ')[0] : '--'}}</span>
                </div>
            </template>
        </el-table-column>
        <el-table-column prop="remark" label="旧物描述" align="center">
            <template #default="scope">
                <div style="display: flex; align-items: center; justify-content: center">
                    <span>{{scope.row.remark != null && scope.row.remark != '' ? scope.row.remark : '无'}}</span>
                </div>
            </template>
        </el-table-column>
        <el-table-column prop="drainagePlan" label="是否加入引流计划" align="center">
            <template #default="scope">
                <div style="display: flex; align-items: center; justify-content: center">
                    <span>{{scope.row.drainagePlan ? '是' : '否'}}</span>
                </div>
            </template>
        </el-table-column>
        <el-table-column prop="isDelete" label="是否删除" align="center">
            <template #default="scope">
                <div style="display: flex; align-items: center; justify-content: center">
                    <span>{{scope.row.isDelete ? '是' : '否'}}</span>
                </div>
            </template>
        </el-table-column>
        <el-table-column prop="status.statusName" label="商品状态" align="center"/>
        <el-table-column label="操作" align="center">
            <template #default="scope">
                <div style="display: flex; align-items: center; justify-content: center">
                    <CommodityUpdateDialog :commodityInfo="scope.row" :getLogisticsModeList="getLogisticsModeList"/>
                </div>
            </template>
        </el-table-column>
    </el-table>
</template>

<script lang="ts" setup>
import {onMounted, reactive} from "vue";
import {getCommodityLogisticsMode} from "../../../service/Commodity/commodity.api.ts";
import CommodityUpdateDialog from "./CommodityUpdateDialog.vue";

defineProps(["commodityData", "pageInfo"]);

let logisticsModeColor = {
    1: 'success',
    2: 'primary',
    4: 'info',
    8: 'warning',
}
let logisticsMode = reactive({});
onMounted(() => {
    getCommodityLogisticsMode().then(res => {
        logisticsMode = res.data;
    })
})


const getLogisticsModeList = (logistic: number): Array<{type: string, label: string}> => {
    let logisticsArray = [];
    let i = 1;
    while(logistic != 0){
        if(logistic & 1){
            logisticsArray.push({type: logisticsModeColor[i], label: logisticsMode[i]});
        }
        i = i << 1;
        logistic = logistic >> 1;
    }
    return logisticsArray;
}
</script>

<style lang="scss" scoped>
.commodity-picture{
    width: 40px;
    height: 40px;
    border-radius: 4px;
}
</style>