<template>
    <div class="user-manage-container">
        <UserSelect :userSelectFrom :queryUserInfo="queryUserInfo" :setPageToOne="setPageToOne"/>
        <el-divider />
        <div v-loading="loading">
            <UserTable :userData="userData" :pageInfo="pageInfo"/>
        </div>
        <Pagination :pageInfo="pageInfo" :changeCurrentPage="changeCurrentPage"/>
    </div>

</template>

<script lang="ts" setup>

import UserTable from "./UserTable.vue";
import UserSelect from "./UserSelect.vue";
import {computed, onMounted, provide, reactive, ref} from "vue";
import {IUserSelectCondition} from "../../../service/User/types.ts";
import {getUserSelectByPageAPI} from "../../../service/User/user.api.ts";
import {managerInfoStore} from "../../../store/manager.info.ts";
import emitter from "../../../utils/event.bus.ts";
import Pagination from "../Pagination.vue";

// 全局事件总线 --> 刷新按钮
emitter.on("refresh", info => {
    if(info === '用户管理'){
        queryUserInfo();
    }
})



// 用户查询条件
const userSelectFrom = reactive({
    phone: '',
    sex: '',
    username: '',
    status: '',
    communityId: managerInfoStore().managerInfo.communityId,
    page: 1,
    size: 10,
}) as IUserSelectCondition

// 分页信息
const total = ref(0);
const pageSize = ref(10);
const currentPage = ref(1);
let userData = ref([]);
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
    userSelectFrom.page = 1
}

const changeCurrentPage = (currentPage) => {
    userSelectFrom.page = currentPage;
    queryUserInfo();
}
const queryUserInfo = () => {
    loading.value = true;
    getUserSelectByPageAPI(userSelectFrom).then(res =>{
        userData.value = res.data.data;
        total.value = res.data.total;
        currentPage.value = res.data.currentPage;
        pageSize.value = res.data.pageSize;
    }).finally(() => {
        loading.value = false;
    });
}
// 向孙子组件传递查询事件
provide("refreshOp", queryUserInfo);
onMounted(() => {
    queryUserInfo();
})



</script>

<style lang="scss" scoped>
  .user-manage-container{
    background-color: #ffffff;
    padding: 20px;
  }
</style>