<template>
    <CommunityAddDialog/>
    <el-table :data="communityData"
              stripe
              border
              size="small"
              highlight-current-row
              row-key="communityId"
              table-layout="auto">
        <el-table-column fixed type="index" label="序号" :index="index => index + 1 + (pageInfo.currentPage - 1) * pageInfo.pageSize" align="center"/>
        <el-table-column prop="provinceName" label="所在省" align="center"/>
        <el-table-column prop="cityName" label="所在市" align="center"/>
        <el-table-column prop="streetName" label="所在区" align="center"/>
        <el-table-column prop="communityName" label="社区名称" align="center"/>
        <el-table-column prop="managerName" label="管理员姓名" align="center"/>
        <el-table-column prop="managerPhone" label="管理员电话" align="center"/>
        <el-table-column label="操作" align="center">
            <template #default="scope">
                <div style="display: flex; align-items: center; justify-content: space-evenly">
                    <CommunityUpdateDialog :communityInfo="scope.row"/>
                </div>
            </template>
        </el-table-column>
    </el-table>

</template>

<script lang="ts" setup>

import CommunityUpdateDialog from "./CommunityUpdateDialog.vue";
import CommunityAddDialog from "./CommunityAddDialog.vue";

defineProps(["communityData", "pageInfo"]);

</script>

<style lang="scss" scoped>

</style>