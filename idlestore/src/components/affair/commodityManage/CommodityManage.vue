<template>
  <div class="commodity-manage-container">
      <CommoditySelect :commoditySelectForm="commoditySelectForm"
                       :queryCommodityInfo="queryCommodityInfo"
                       :setPageToOne="setPageToOne"
                       :resetCommodityInfo="resetCommodityInfo"/>
      <el-divider/>
      <div v-loading="loading">
          <CommodityTable :commodityData="commodityData" :pageInfo="pageInfo"/>
      </div>
      <Pagination :pageInfo="pageInfo" :changeCurrentPage="changeCurrentPage"/>
  </div>
</template>

<script setup lang="ts">

import CommoditySelect from "./CommoditySelect.vue";
import {computed, onMounted, provide, reactive, ref} from "vue";
import {managerInfoStore} from "../../../store/manager.info.ts";
import {getCommodityInfo} from "../../../service/Commodity/commodity.api.ts";
import {ICommoditySelectCondition} from "../../../service/Commodity/types.ts";
import CommodityTable from "./CommodityTable.vue";
import emitter from "../../../utils/event.bus.ts";
import Pagination from "../Pagination.vue";

const {audit} = defineProps(["audit"]);

// 全局事件总线 --> 刷新按钮
emitter.on("refresh", info => {
    if(info === '商品管理' || info === '商品审核'){
        queryCommodityInfo();
    }
})



const commoditySelectForm = reactive({
    communityId: managerInfoStore().managerInfo.communityId,
    phone: '',
    username: '',
    categoryId: [],
    status: [],
    logisticsMode: 0,
    drainagePlan: '',
    commodityName: '',
    isDelete: '',
    page: 1,
    pageSize: 10,
}) as ICommoditySelectCondition
if(audit){
    commoditySelectForm.status = [1];
}

const commodityData = ref([]);
let loading = ref(false);

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
    commoditySelectForm.page = currentPage;
    queryCommodityInfo();
}

onMounted(() => {
    queryCommodityInfo();
})
const queryCommodityInfo = () => {
    loading.value = true;
    getCommodityInfo(commoditySelectForm).then(res => {
        total.value = res.data.total;
        currentPage.value = res.data.currentPage;
        pageSize.value = res.data.pageSize;
        commodityData.value = res.data.data;
    }).finally(() => {
        loading.value = false;
    })
}
// 向孙子组件传递查询事件
provide("refreshOp", queryCommodityInfo);
const setPageToOne = () => {
    commoditySelectForm.page = 1;
}

const resetCommodityInfo = () => {
  commoditySelectForm.phone = '';
  commoditySelectForm.username = '';
  commoditySelectForm.categoryId = [];
  commoditySelectForm.status = [];
  commoditySelectForm.logisticsMode = 0;
  commoditySelectForm.drainagePlan = undefined;
  commoditySelectForm.commodityName = '';
  commoditySelectForm.isDelete = undefined;
  commoditySelectForm.page = 1;
  queryCommodityInfo();
}
</script>

<style lang="scss" scoped>
.commodity-manage-container{
  background-color: #ffffff;
  padding: 20px;
}
</style>