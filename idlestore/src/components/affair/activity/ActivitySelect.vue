<template>
    <div class="activity-select-container">
        <el-form :inline="true" :model="activitySelectForm" class="demo-form-inline">
            <el-form-item label="举办日期">
                <el-date-picker
                    v-model="activitySelectForm.dates"
                    type="dates"
                    placeholder="选择一天或多天"
                />
            </el-form-item>
            <el-form-item label="活动地点">
                <el-input type="text" v-model="activitySelectForm.tradingPlace"></el-input>
            </el-form-item>
            <el-form-item label="负责人姓名">
                <el-input type="text" v-model="activitySelectForm.name"></el-input>
            </el-form-item>
            <el-form-item label="负责人联系方式">
                <el-input type="text" v-model="activitySelectForm.phone"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="queryPage">查询</el-button>
            </el-form-item>
        </el-form>
    </div>
</template>

<script lang="ts" setup>
const  {setPageToOne, queryActivityInfo} = defineProps(["activitySelectForm", "setPageToOne", "queryActivityInfo"]);
const queryPage = () =>{
    setPageToOne();
    queryActivityInfo();
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