<template>
    <div class="user-manage-container">
        <CommunitySelect :communitySelectForm="communitySelectForm" :queryCommunityInfo="queryCommunityInfo" :setPageToOne="setPageToOne"/>
        <el-divider />
        <div v-loading="loading">
            <CommunityTable :communityData="communityData" :pageInfo="pageInfo"/>
        </div>
        <Pagination :pageInfo="pageInfo" :changeCurrentPage="changeCurrentPage"/>
    </div>

</template>

<script lang="ts" setup>

import {computed, onMounted, provide, reactive, ref} from "vue";
import emitter from "../../../utils/event.bus.ts";
import CommunitySelect from "./CommunitySelect.vue";
import {getCommunityByPageAPI} from "../../../service/Community/community.api.ts";
import CommunityTable from "./CommunityTable.vue";
import Pagination from "../Pagination.vue";

// 全局事件总线 --> 刷新按钮
emitter.on("refresh", info => {
    if(info === '社区管理'){
        queryCommunityInfo();
    }
})



// 社区查询条件
const communitySelectForm = reactive({
    streetId: [],
    communityName: '',
    managerName: '',
    managerPhone: '',
    page: 1,
    pageSize: 10,
}) as {
    pageSize: number;
    managerPhone: string;
    communityName: string;
    page: number;
    managerName: string;
    streetId: any[]
};

// 分页信息
const total = ref(0);
const pageSize = ref(10);
const currentPage = ref(1);
let communityData = ref([]);
let loading = ref(false);

// 分页信息整合
let pageInfo = computed(() => {
    return {
        total: total.value,
        pageSize: pageSize.value,
        currentPage: currentPage.value,
    }
});

const setPageToOne = () => {
    communitySelectForm.page = 1
}

const changeCurrentPage = (currentPage) => {
    communitySelectForm.page = currentPage;
    queryCommunityInfo();
}
const queryCommunityInfo = () => {
    loading.value = true;
    getCommunityByPageAPI(communitySelectForm).then(res =>{
        communityData.value = res.data.data;
        total.value = res.data.total;
        currentPage.value = res.data.currentPage;
        pageSize.value = res.data.pageSize;
    }).finally(() => {
        loading.value = false;
    });
}
// 向孙子组件传递查询事件
provide("refreshOp", queryCommunityInfo);
onMounted(() => {
    queryCommunityInfo();
})



</script>

<style lang="scss" scoped>
.user-manage-container{
  background-color: #ffffff;
  padding: 20px;
}
</style>