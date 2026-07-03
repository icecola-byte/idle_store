<template>
  <div class="back-log-card-container">
      <div class="title">{{title}}</div>
      <slot></slot>
      <div class="button-container">
          <el-button type="primary" @click="gotoAuditCommodity">处理</el-button>
      </div>
  </div>
</template>

<script setup lang="ts">
  import {homeTagsStore} from "../../store/home.tags.ts";

  const {title} = defineProps(["title"]);
  const gotoAuditCommodity = () => {
      homeTagsStore().addTab(title);
  }

</script>

<style lang="scss" scoped>
  .back-log-card-container{
    background-color: #ffffff;
    padding: 15px 20px 20px 20px;
    width: 300px;
    border-radius: 6px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;

    .title{
      margin-bottom: 10px
    }
    .title::before{
      content: "";
      border: 1px solid #67c23a;
      margin-right: 10px;
    }
    .button-container{
      flex-grow: 1;
      display: flex;
      align-items: flex-end;
      justify-content: center;
      bottom: 20px;
      margin-top: 40px;
    }
  }
</style>