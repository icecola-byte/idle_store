<template>
    <div class="order-select-container" v-loading="isLoading">
        <el-form :model="orderSelectForm" @submit.prevent :inline="true" class="demo-form-inline">
            <el-form-item label="买家用户名">
                <el-input v-model="orderSelectForm.buyerName" />
            </el-form-item>
            <el-form-item label="买家手机号">
                <el-input v-model="orderSelectForm.buyerPhone" />
            </el-form-item>
            <el-form-item label="卖家用户名">
                <el-input v-model="orderSelectForm.sellerName" />
            </el-form-item>
            <el-form-item label="卖家手机号">
                <el-input v-model="orderSelectForm.sellerPhone" />
            </el-form-item>
            <el-form-item label="商品名称">
                <el-input v-model="orderSelectForm.commodityName" />
            </el-form-item>
            <el-form-item label="物流方式">
                <el-select v-model="logisticsModeSelect" multiple placeholder="所有"
                           @change="logisticsModeSelectChange">
                    <el-option
                            v-for="item in logisticMode"
                            :key="item.key"
                            :label="item.value"
                            :value="Number(item.key)"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="商品分类">
                <el-cascader
                        :options="categoryArray"
                        :props="categorySelectProps"
                        v-model="tempOrderCategorySelect"
                        collapse-tags
                        filterable
                        collapse-tags-tooltip
                        placeholder="全部"
                        clearable
                        @change="categorySelectChange"
                />
            </el-form-item>
            <el-form-item label="订单状态">
                <el-select v-model="orderSelectForm.status" multiple placeholder="所有">
                    <el-option
                            v-for="item in orderStatus"
                            :key="item.status"
                            :label="item.statusName"
                            :value="Number(item.status)"
                    />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="queryOrder">查询</el-button>
                <el-button type="primary" @click="iResetCommodityInfo">重置</el-button>
            </el-form-item>
        </el-form>
    </div>
</template>

<script lang="ts" setup>

import {onMounted, ref} from "vue";
import axios from "axios";
import {getCommodityCategory, getCommodityLogisticsMode} from "../../../service/Commodity/commodity.api.ts";
import {getOrderStatus} from "../../../service/Order/order.api.ts";

const {orderSelectForm, resetOrderInfo, queryOrderInfo, setPageToOne} = defineProps(["orderSelectForm", "queryOrderInfo", "resetOrderInfo", "setPageToOne"]);

// 所有的物流方式
const logisticMode = ref([]);
// 选中的物流方式
const logisticsModeSelect = ref(orderSelectForm.logisticsMode);
// 商品分类选择配置
const categorySelectProps = { multiple: true, value: 'value', label: 'text' }
// 商品分类信息
const categoryArray = ref([]);
// 订单状态
const orderStatus = ref([]) as Array<{status: number, statusName: string}>;

let isLoading = ref(false);
onMounted(() => {
    isLoading.value = true;

    // 获取物流方式、商品分类信息、商品状态信息
    axios.all([getCommodityLogisticsMode(), getCommodityCategory(), getOrderStatus()])
        .then(axios.spread((logisticsModeTask, categoryTask, statusTask) => {
            // 初始时默认选择全部
            for(let key in logisticsModeTask.data){
                logisticMode.value.push({key: key, value: logisticsModeTask.data[key]});
            }
            categoryArray.value = categoryTask.data;
            orderStatus.value = statusTask.data;
        })).catch(error => {})
        .finally(() => {
            isLoading.value = false;
        })

})

// 物流方式相关函数
const logisticsModeSelectChange = () => {
    setLogisticModeValue();
}

const setLogisticModeValue = () => {
    orderSelectForm.logisticsMode = logisticsModeSelect.value;
}

// 商品分类的选择
const tempOrderCategorySelect = ref([])
const categorySelectChange = (value) => {
    orderSelectForm.categoryId = getCategorySelectSet(value);
}

const getCategorySelectSet = (value) => {
    return value.map(i => {
        return Number(i[1]);
    });
}

// 查询按钮
const queryOrder = () => {
    setPageToOne();
    queryOrderInfo();
}

// 重置按钮
const iResetCommodityInfo = () => {
    logisticsModeSelect.value = [];
    logisticMode.value.map(i => {
        i.disabled = false
    });
    tempOrderCategorySelect.value = [];
    resetOrderInfo();
}
</script>

<style lang="scss" scoped>
.demo-form-inline .el-input {
  --el-input-width: 160px;
}

.demo-form-inline .el-select {
  --el-select-width: 220px;
}
</style>