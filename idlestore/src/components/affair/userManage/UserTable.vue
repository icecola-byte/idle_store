<template>
    <UserAddDialog/>
      <el-table :data="userData"
                stripe
                border
                size="small"
                highlight-current-row
                row-key="userId"
                table-layout="auto">
          <el-table-column fixed type="index" label="序号" :index="index => index + 1 + (pageInfo.currentPage - 1) * pageInfo.pageSize" align="center"/>
          <el-table-column label="头像" align="center">
              <template #default="scope">
                  <div style="display: flex; align-items: center; justify-content: center">
                      <el-image class="user-avatar" fit="cover" preview-teleported :src="scope.row.avatarUrl" :preview-src-list="[scope.row.avatarUrl]"/>
                  </div>
              </template>
          </el-table-column>
          <el-table-column prop="username" label="用户名" align="center"/>
          <el-table-column prop="phone" label="手机号" align="center"/>
          <el-table-column label="性别" align="center" width="50px">
              <template #default="scope">
                  <div style="display: flex; align-items: center; justify-content: space-evenly">
                      <span class="user-sex">{{scope.row.sex == 1 ? '男' : '女' }}</span>
                  </div>
              </template>
          </el-table-column>
          <el-table-column prop="registerTime" label="注册时间" align="center" sortable>
              <template #default="scope">
                  <div style="display: flex; align-items: center; justify-content: space-evenly">
                      <span class="user-register-time">{{scope.row.registerTime.replace("T", " ") }}</span>
                  </div>
              </template>
          </el-table-column>
          <el-table-column prop="coinBalance" label="硬币余额" align="center" sortable/>
          <el-table-column label="详细地址" align="center" width="150px">
              <template #default="scope">
                  <div style="display: flex; align-items: center; justify-content: space-evenly" class="user-address">
                      <span class="user-address-hide">...</span>
                      <span class="user-address-text">{{scope.row.addressDetail ? scope.row.addressDetail : '未知' }}</span>
                  </div>
              </template>
          </el-table-column>
          <el-table-column label="账号状态" align="center">
              <template #default="scope">
                  <div style="display: flex; align-items: center; justify-content: space-evenly">
                      <span>{{scope.row.isValid == true ? '封禁中' : '使用中' }}</span>
                  </div>
              </template>
          </el-table-column>
          <el-table-column label="操作" align="center">
              <template #default="scope">
                  <div style="display: flex; align-items: center; justify-content: space-evenly">
                      <UserUpdateDialog :userInfo="scope.row"/>
                  </div>
              </template>
          </el-table-column>
      </el-table>

</template>

<script lang="ts" setup>
  import UserAddDialog from "./UserAddDialog.vue";
  import UserUpdateDialog from "./UserUpdateDialog.vue";

  defineProps(["userData", "pageInfo"]);

</script>

<style lang="scss" scoped>
  .user-avatar{
    width: 40px;
    height: 40px;
    border-radius: 4px;
  }
  .user-username{
    margin-left: 10px;
  }
  .user-address{
      .user-address-text{
          display: none;
      }
  }
  .user-address:hover{
      .user-address-text{
          display: inherit;
      }
      .user-address-hide{
          display: none;
      }
  }
</style>