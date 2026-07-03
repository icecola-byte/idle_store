<template>
    <div class="order-manage-container">
        <OrderSelect :orderSelectForm="orderSelectForm"
                     :queryOrderInfo="queryOrderInfo"
                     :resetOrderInfo="resetOrderInfo"
                     :setPageToOne="setPageToOne"
                         />
        <el-divider/>
        <div v-loading="loading">
            <OrderTable :orderData="orderData" :pageInfo="pageInfo"/>
        </div>
        <Pagination :pageInfo="pageInfo" :changeCurrentPage="changeCurrentPage"/>
    </div>
</template>

<script setup lang="ts">

import {computed, onMounted, provide, reactive, ref} from "vue";
import {managerInfoStore} from "../../../store/manager.info.ts";
import OrderSelect from "./OrderSelect.vue";
import {IOrderSelectCondition} from "../../../service/Order/types.ts";
import {getOrderInfo} from "../../../service/Order/order.api.ts";
import OrderTable from "./OrderTable.vue";
import Pagination from "../Pagination.vue";
import emitter from "../../../utils/event.bus.ts";

// 全局事件总线 --> 刷新按钮
emitter.on("refresh", info => {
    if(info === '订单管理' || info === '上门回收'){
        queryOrderInfo();
    }
})
const {orderAudit} = defineProps(["orderAudit"]);



const orderSelectForm = reactive({
    communityId: managerInfoStore().managerInfo.communityId,
    buyerPhone: '',
    buyerName: '',
    sellerPhone: '',
    sellerName: '',
    commodityName: '',
    categoryId: [],
    status: [],
    logisticsMode: [],
    page: 1,
    pageSize: 10,
}) as IOrderSelectCondition
if(orderAudit){
    orderSelectForm.status = [1];
    orderSelectForm.logisticsMode = [1];
}
const orderData = ref([]);
let loading = ref(false);
//
const total = ref(0);
const pageSize = ref(10);
const currentPage = ref(1);

// 分页信息整合
let pageInfo = computed(() => {
    return {
        total: total.value,
        pageSize: pageSize.value,
        currentPage: currentPage.value,
    }
});

const changeCurrentPage = (currentPage) => {
    orderSelectForm.page = currentPage;
    queryOrderInfo();
}

onMounted(() => {
    queryOrderInfo();
})
const queryOrderInfo = () => {
    loading.value = true;
    getOrderInfo(orderSelectForm).then(res => {
        total.value = res.data.total;
        currentPage.value = res.data.currentPage;
        pageSize.value = res.data.pageSize;
        orderData.value = res.data.data;
        console.log(orderData.value);
    }).finally(() => {
        loading.value = false;
    })
}
// // 向孙子组件传递查询事件
provide("refreshOp", queryOrderInfo);
const setPageToOne = () => {
    orderSelectForm.page = 1;
}

const resetOrderInfo = () => {
    orderSelectForm.buyerPhone = '';
    orderSelectForm.buyerName = '';
    orderSelectForm.sellerPhone = '';
    orderSelectForm.sellerName = '';
    orderSelectForm.commodityName = '';
    orderSelectForm.categoryId = [];
    orderSelectForm.status = [];
    orderSelectForm.logisticsMode = [];
    orderSelectForm.page = 1;
    queryOrderInfo();
}
</script>

<style lang="scss" scoped>
.order-manage-container{
  background-color: #ffffff;
  padding: 20px;
}
</style>