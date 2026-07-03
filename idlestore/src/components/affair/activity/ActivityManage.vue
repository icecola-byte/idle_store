<template>
    <div class="activity-manage-container">
        <ActivitySelect :activitySelectForm="activitySelectForm" :setPageToOne="setPageToOne" :queryActivityInfo="queryActivityInfo"/>
        <el-divider />
        <div v-loading="loading">
            <ActivityTable :activityData="activityData" :pageInfo="pageInfo"/>
        </div>
        <Pagination :pageInfo="pageInfo" :changeCurrentPage="changeCurrentPage"/>
    </div>

</template>

<script lang="ts" setup>

import {computed, onMounted, provide, reactive, ref} from "vue";

import Pagination from "../Pagination.vue";
import ActivitySelect from "./ActivitySelect.vue";
import ActivityTable from "./ActivityTable.vue";
import {queryActivityAPI} from "../../../service/Activity/activity.api.ts";
import {ISelectActivityCondition} from "../../../service/Activity/types.ts";
import dateUtil from '../../../utils/dateFormat';
import {managerInfoStore} from "../../../store/manager.info.ts";
import emitter from "../../../utils/event.bus.ts";

emitter.on("refresh", info => {
    if(info === '社区活动'){
        queryActivityInfo();
    }
})

let loading = ref(false)
const activitySelectForm = reactive({
    dates: [],
    tradingPlace: '',
    communityId: managerInfoStore().managerInfo.communityId,
    name: '',
    phone: '',
    page: 1,
    pageSize: 10,
}) as ISelectActivityCondition;

const activityData = ref([]);

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
const setPageToOne = () => {
    activitySelectForm.page = 1;
}

const queryActivityInfo = () =>{
    if(loading.value === true){
        return ;
    }
    loading.value = true;
    const data = {
        dates: activitySelectForm.dates !== null ? activitySelectForm.dates.map(i => dateUtil.formatDateTime(i)) : [],
        tradingPlace: activitySelectForm.tradingPlace,
        communityId: activitySelectForm.communityId,
        name: activitySelectForm.name,
        phone: activitySelectForm.phone,
        page: activitySelectForm.page,
        pageSize: activitySelectForm.pageSize,
    } as ISelectActivityCondition
    queryActivityAPI(data).then(res => {
        total.value = res.data.total;
        currentPage.value = res.data.currentPage;
        pageSize.value = res.data.pageSize;
        activityData.value = res.data.data;
    }).finally(() => {
        loading.value = false;
    })
}
const changeCurrentPage = (currentPage) => {
    activitySelectForm.page = currentPage;
    queryActivityInfo();
}
onMounted(() => {
    queryActivityInfo();
})
</script>

<style lang="scss" scoped>
.activity-manage-container{
  background-color: #ffffff;
  padding: 20px;
}
</style>