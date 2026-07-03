<template>
    <div class="home-header-container">
        <div class="refresh-icon-container">
            <el-icon color="#a8abb2" size="22px" @click="refreshOp" class="refresh-icon"><Refresh /></el-icon>
        </div>
        <div class="header-item">
            <el-icon color="#a8abb2"><School /></el-icon>
            <span>{{managerInfo.communityName}}</span>
        </div>
        <div class="header-item">
            <el-icon color="#a8abb2"><Service /></el-icon>
            <span>{{managerInfo.phone}}</span>
        </div>
        <div class="header-item" >
            <el-icon color="#a8abb2" class="exit-icon" @click="loginOut"><SwitchButton /></el-icon>
        </div>
    </div>
</template>

<script setup lang="ts">

  import {managerInfoStore} from "../../store/manager.info.ts";
  import {
      School,
      Service,
      SwitchButton,
      Refresh
  } from '@element-plus/icons-vue'
  import {homeTagsStore} from "../../store/home.tags.ts";
  import router from "../../router";
  import localCache from '../../utils/cache'
  import {ref} from "vue";

  const managerInfo = managerInfoStore().managerInfo;
  // 全局事件总线控制当前标签页的刷新
  const refreshOp = () => {
      homeTagsStore().refresh();
  }

  // 退出 --> 跳转到登录页面并清除本地 token
  const loginOut = () => {
      localCache.deleteCache("token");
      router.replace("/login");
  }
</script>

<style lang="scss" scoped>
    .home-header-container{
        position:relative;
        line-height: 30px;
        padding: 5px;
        border: 1px solid #efefef;
        height: 30px;
        display: flex;
        align-items: stretch;
        justify-content: flex-end;
        .refresh-icon-container{
            position: absolute;
            line-height: 40px;
            left: 20px;
            cursor: pointer;
            .refresh-icon:hover{
                cursor: pointer;
                color: #303133;
            }
        }
        .header-item{
            display: flex;
            align-items: center;
            justify-content: space-evenly;
            padding: 0 20px;
            border-left: 1px solid #efefef;
        }
        .header-item span{
            margin-left: 10px;
            color: #303133;
        }
        .exit-icon:hover{
            cursor: pointer;
            color: #f56c6c;
            border-bottom: 1px solid #f56c6c;
        }
    }
</style>