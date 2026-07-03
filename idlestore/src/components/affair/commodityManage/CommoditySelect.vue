<template>
    <div class="commodity-select-container" v-loading="isLoading">
        <el-form :model="commoditySelectForm" @submit.prevent :inline="true" class="demo-form-inline">
            <el-form-item label="用户名">
                <el-input v-model="commoditySelectForm.username" />
            </el-form-item>
            <el-form-item label="用户手机号">
                <el-input v-model="commoditySelectForm.phone" />
            </el-form-item>
            <el-form-item label="商品名称">
                <el-input v-model="commoditySelectForm.commodityName" />
            </el-form-item>
            <el-form-item label="是否加入引流计划">
                <el-select v-model="commoditySelectForm.drainagePlan" placeholder="所有">
                    <el-option label="所有" value="" />
                    <el-option label="是" :value="1" />
                    <el-option label="否" :value="0" />
                </el-select>
            </el-form-item>
            <el-form-item label="物流方式">
                <el-select v-model="logisticsModeSelect" multiple placeholder="所有"
                           @change="logisticsModeSelectChange">
                    <el-option
                        v-for="item in logisticMode"
                        :key="item.key"
                        :label="item.value"
                        :value="Number(item.key)"
                        :disabled="item.disabled"

                    />
                </el-select>
            </el-form-item>
            <el-form-item label="商品分类">
                <el-cascader
                    :options="categoryArray"
                    :props="categorySelectProps"
                    v-model="tempCommodityCategorySelect"
                    collapse-tags
                    filterable
                    collapse-tags-tooltip
                    placeholder="全部"
                    clearable
                    @change="categorySelectChange"
                />
            </el-form-item>
            <el-form-item label="商品状态">
                <el-select v-model="commoditySelectForm.status" multiple placeholder="所有">
                    <el-option
                        v-for="item in commodityStatus"
                        :key="item.key"
                        :label="item.value"
                        :value="Number(item.key)"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="是否删除">
                <el-select v-model="commoditySelectForm.isDelete" placeholder="所有">
                    <el-option label="所有" value="" />
                    <el-option label="是" :value="1" />
                    <el-option label="否" :value="0" />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="queryCommodity">查询</el-button>
                <el-button type="primary" @click="iResetCommodityInfo">重置</el-button>
            </el-form-item>
        </el-form>
    </div>

</template>

<script lang="ts" setup>

import {onMounted, ref} from "vue";
import {
    getCommodityCategory,
    getCommodityLogisticsMode,
    getCommodityStatus
} from "../../../service/Commodity/commodity.api.ts";
import axios from "axios";

const {commoditySelectForm, queryCommodityInfo, setPageToOne, resetCommodityInfo } = defineProps(["commoditySelectForm", "queryCommodityInfo", "setPageToOne", "resetCommodityInfo"]);

// 所有的物流方式
const logisticMode = ref([]);
// 选中的物流方式
const logisticsModeSelect = ref([]);
// 商品分类选择配置
const categorySelectProps = { multiple: true, value: 'value', label: 'text' }
// 商品分类信息
const categoryArray = ref([]);
// 商品状态信息
const commodityStatus = ref([]);

let isLoading = ref(false);
onMounted(() => {
  isLoading.value = true;

  // 获取物流方式、商品分类信息、商品状态信息
  axios.all([getCommodityLogisticsMode(), getCommodityCategory(), getCommodityStatus()])
      .then(axios.spread((logisticsModeTask, categoryTask, statusTask) => {
          // 初始时默认选择全部
          for(let key in logisticsModeTask.data){
              logisticMode.value.push({key: key, value: logisticsModeTask.data[key], disabled: false});
          }
          setLogisticModeValue();
          categoryArray.value = categoryTask.data;
          // 初始时默认选择全部
          for(let key in statusTask.data){
              commodityStatus.value.push({key: key, value: statusTask.data[key]});
          }
      })).catch(error => {})
      .finally(() => {
          isLoading.value = false;
      })

})

// 物流方式相关函数
const logisticsModeSelectChange = () => {
      // 如果选中了上门回收, 那么其他三个就不能选了
      if (logisticsModeSelect.value.includes(1)){
          logisticMode.value.map(i => {
              if(i.key != 1){
                  i.disabled = true
              }
          });
          logisticsModeSelect.value = logisticsModeSelect.value.filter(i => i == 1);
      }else {
          logisticMode.value.map(i => i.disabled = false);
      }
    setLogisticModeValue();
}

const setLogisticModeValue = () => {
    commoditySelectForm.logisticsMode = logisticsModeSelect.value.reduce((acc,total) => Number(acc) + Number(total), 0);
}

// 商品分类的选择
const tempCommodityCategorySelect = ref([])
const categorySelectChange = (value) => {
    commoditySelectForm.categoryId = getCategorySelectSet(value);
}

const getCategorySelectSet = (value) => {
     return value.map(i => {
        return Number(i[1]);
    });
}

// 重置按钮
const iResetCommodityInfo = () => {
    logisticsModeSelect.value = [];
    logisticMode.value.map(i => {
        i.disabled = false
    });
    tempCommodityCategorySelect.value = [];
    resetCommodityInfo();
}

const queryCommodity = () => {
    setPageToOne();
    queryCommodityInfo();
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