<template>
    <div class="community-select-container" v-loading="isLoading">
        <el-form :model="communitySelectForm" @submit.prevent :inline="true" class="demo-form-inline">
            <el-form-item label="所属社区">
                <el-cascader
                        :options="communityArray"
                        :props="communitySelectProps"
                        collapse-tags
                        filterable
                        collapse-tags-tooltip
                        placeholder="全部"
                        clearable
                        @change="communitySelectChange"
                />
            </el-form-item>
            <el-form-item label="社区名称">
                <el-input v-model="communitySelectForm.communityName" />
            </el-form-item>
            <el-form-item label="管理员姓名">
                <el-input v-model="communitySelectForm.managerName" />
            </el-form-item>
            <el-form-item label="管理员电话">
                <el-input v-model="communitySelectForm.managerPhone" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="queryCommunity">查询</el-button>
            </el-form-item>
        </el-form>
    </div>
</template>

<script lang="ts" setup>

import {onMounted, ref} from "vue";
import {getRegisterCommunityInfoAPI} from "../../../service/Community/community.api.ts";
import emitter from "../../../utils/event.bus.ts";

const {communitySelectForm, queryCommunityInfo, setPageToOne} = defineProps(["communitySelectForm", "queryCommunityInfo", "setPageToOne"]);

// 商品分类选择配置
const communitySelectProps = { multiple: true, value: 'value', label: 'text' }
// 商品分类信息
const communityArray = ref([]);

emitter.on("refresh", info => {
    if(info === '社区管理'){
        isLoading.value = true;
        getRegisterCommunityInfoAPI().then(res => {

            communityArray.value = res.data;
        }).finally(() => {
            isLoading.value = false;
        })
    }
})


let isLoading = ref(false);
onMounted(() => {

    isLoading.value = true;
    getRegisterCommunityInfoAPI().then(res => {

        communityArray.value = res.data;
    }).finally(() => {
        isLoading.value = false;
    })


});

const communitySelectChange = (value) => {
    communitySelectForm.streetId = getCommunityStreetIdSelect(value);
}

const getCommunityStreetIdSelect = (value) => {
    return value.map(i => {
        return i[4] ?? i[3] ?? i[2] ?? i[1] ?? i[0];
    });
}

const queryCommunity = ()=> {
    setPageToOne();
    queryCommunityInfo();
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