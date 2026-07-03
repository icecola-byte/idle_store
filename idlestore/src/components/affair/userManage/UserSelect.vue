<template>
  <div class="user-select-container">
      <el-form :inline="true" :model="userSelectFrom" class="demo-form-inline">
          <el-form-item label="手机号">
              <el-input v-model="userSelectFrom.phone" clearable />
          </el-form-item>
          <el-form-item label="用户名">
              <el-input v-model="userSelectFrom.username" clearable />
          </el-form-item>
          <el-form-item label="性别">
              <el-select
                      v-model="userSelectFrom.sex"
                      placeholder="所有"
              >
                  <el-option label="所有" value=""/>
                  <el-option label="男" :value="1" />
                  <el-option label="女" :value="0" />
              </el-select>
          </el-form-item>
          <el-form-item label="账号状态">
              <el-select
                      v-model="userSelectFrom.status"
                      placeholder="所有"
              >
                  <el-option label="所有" value=""/>
                  <el-option label="使用中" :value="0" />
                  <el-option label="封禁中" :value="1" />
              </el-select>
          </el-form-item>
          <el-form-item>
              <el-button type="primary" @click="queryPage">查询</el-button>
          </el-form-item>
      </el-form>
  </div>
</template>

<script lang="ts" setup>

const {userSelectFrom, queryUserInfo, setPageToOne} = defineProps(["userSelectFrom", "queryUserInfo", "setPageToOne"]);

const queryPage = () => {
    setPageToOne();
    queryUserInfo();
}

</script>

<style lang="scss" scoped>
.demo-form-inline .el-input {
    --el-input-width: 220px;
}

.demo-form-inline .el-select {
    --el-select-width: 220px;
}
</style>