<template>
    <div class="charts-container">
        <div class="pie-chart">
<!--            <div class="title"><el-icon><Odometer /></el-icon><span>状态</span></div>-->
            <PieCharts :title="'用户 - 状态'" :getData="getUserDataByStatus"></PieCharts>
            <PieCharts :title="'商品 - 状态'" :getData="getCommodityDataByStatus"></PieCharts>
            <PieCharts :title="'订单 - 状态'" :getData="getOrderDataByStatus"></PieCharts>
        </div>
        <div class="line-chart">
            <LineCharts :title="'交易信息'" :getData="getOrderDataByDealQuery">
                <el-tooltip placement="bottom">
                    <template #content>
                        <span>商品量 > 商品交易量 订单较少</span><br/>
                        <span>商品量 = 商品交易量 订单成功率高</span><br/>
                        <span>商品量 < 商品交易量 订单取消率高</span>
                    </template>
                    <el-icon class="el-icon-tips" size="16px" color="#333333"><Warning /></el-icon>
                </el-tooltip>
            </LineCharts>
            <OrderLineCharts :title="'订单信息'" :getData="getOrderDataByDealAndCategory"
                        ref="OrderInfoChart">
                <div class="m-4 order-line-category">
                    <el-cascader :options="categoryOptions" :props="{checkStrictly: true, value: 'value', label: 'text'}"
                                 size="small"
                                 placeholder="全部"
                                 clearable
                                 @change="categoryChange"/>
                </div>
            </OrderLineCharts>
        </div>
        <div class="bar-chart">
            <BarCharts :title="'商品 - 分类'" :getData="getCommodityDataByCategory"/>
        </div>
    </div>

</template>

<script lang="ts" setup>
import PieCharts from "./PieCharts.vue";
import {onMounted, onUnmounted, reactive, ref, watch} from "vue";
import {
    getCommodityDataByCategory,
    getCommodityDataByStatus, getOrderDataByDealAndCategory, getOrderDataByDealQuery,
    getOrderDataByStatus,
    getUserDataByStatus
} from "../../../service/Data";
import LineCharts from "./LineCharts.vue";
import {Warning} from "@element-plus/icons-vue";
import BarCharts from "./BarCharts.vue";
import {getCommodityCategory} from "../../../service/Commodity/commodity.api.ts";
import OrderLineCharts from "./OrderLineCharts.vue";
import emitter from "../../../utils/event.bus.ts";
import {managerInfoStore} from "../../../store/manager.info.ts";
import {getRegisterCommunityInfoAPI} from "../../../service/Community/community.api.ts";
import {homeTagsStore} from "../../../store/home.tags.ts";

// 全局事件总线 --> 刷新按钮
emitter.on("refresh", info => {
    if(info === '数据统计'){
        OrderInfoChart.value.CategoryChange(category.categoryId);
    }
})

let categoryOptions = ref([]);
let category = reactive({categoryId: 0});
let OrderInfoChart = ref(null);
const CategoryChange = (data) => {
    OrderInfoChart.value.CategoryChange(data)
}

let communityArray = ref([]);

onMounted(() => {
    getCommodityCategory().then(res => {
        categoryOptions.value = res.data;
        watch(() => category, () => {
            CategoryChange(category.categoryId);
        }, {immediate: true, deep: true})
        // category.categoryId = Number(categoryOptions.value[0].value);
    });

});


const categoryChange = (e) => {
    category.categoryId = e == undefined ? 0 : e[1] ?? e[0]
}

</script>

<style lang="scss" scoped>
.charts-container{
  width: 100%;
  height: 100%;
  display: flex;
  .pie-chart{
    width: 20%;
    height: 100%;
      .title{
          width: 100%;
          display: flex;
          align-items: center;
          justify-content: flex-start;
          position: relative;
          bottom: 15px;
          background-color: #f5d372;
          color: #747536;
          padding: 5px 10px;
      }
  }
  .line-chart{
    width: 54%;
    height: 46%;
    margin: 0 30px;
  }
  .bar-chart{
    width: 28%;
    height: 100%;
  }
  .el-icon-tips{
    margin-left: 10px;
    margin-top: 2px;
  }
  .order-line-category{
    margin-left: 10px;
    margin-top: -3px;
  }
}


</style>