<template>
  <div class="backlog-container" v-loading="isLoading">
      <template v-if="commodityInfo.number !== 0">
          <BackLogCard :title="'商品审核'">
            <div class="card-content">
                <div class="card-content-item">
                    <div class="text-description"><span>待审核商品数量:</span></div>
                    <div class="text"><span class="text-red"> {{ commodityInfo.number }}</span> 个</div>
                </div>
                <div class="card-content-item">
                    <div class="text-description">最长时间未处理: </div>
                    <div class="text"><span> {{ commodityInfo.longestTime }}</span></div>
                </div>
            </div>
          </BackLogCard>
      </template>
      <template v-if="orderInfo.number !== 0">
          <BackLogCard :title="'上门回收'">
              <div class="card-content">
                  <div class="card-content-item">
                      <div class="text-description"><span>待处理数量:</span></div>
                      <div class="text"><span class="text-red"> {{ orderInfo.number }}</span> 个</div>
                  </div>
                  <div class="card-content-item">
                      <div class="text-description">今日待取件数量: </div>
                      <div class="text"><span :class="orderInfo.todayNumber > 0 ? 'text-red' : ''"> {{ orderInfo.todayNumber }}</span></div>
                  </div>
                  <div class="card-content-item">
                      <div class="text-description">明日待取件数量: </div>
                      <div class="text"><span :class="orderInfo.tomorrowNumber > 0 ? 'text-red' : ''"> {{ orderInfo.tomorrowNumber }}</span></div>
                  </div>
                  <div class="card-content-item" v-if="orderInfo.notCompleted > 0">
                      <div class="text-description">逾期数量: </div>
                      <div class="text"><span class="text-red"> {{ orderInfo.notCompleted }}</span></div>
                      <el-tooltip
                          content="请及时联系用户更换时间"
                      >
                          <el-icon class="el-icon-tips" size="16px" color="#333333"><Warning /></el-icon>
                      </el-tooltip>
                  </div>
              </div>
          </BackLogCard>
      </template>
      <template v-if="commodityInfo.number === 0 && orderInfo.number === 0">
          <BackLogCard :title="'今天也要开心鸭~'">

          </BackLogCard>
      </template>

  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref} from "vue";
import {getBackLogList} from "../../service/BackLog/backlog.ts";
import {managerInfoStore} from "../../store/manager.info.ts";
import BackLogCard from "./BackLogCard.vue";
import {ICommodityBackLogInfo, IOrderBackLogInfo} from "../../service/BackLog/types.ts";
import { Warning } from "@element-plus/icons-vue";
import emitter from "../../utils/event.bus.ts";


emitter.on("refresh", info => {
    if(info === '待办事项'){
        toGetBackLogList();
    }
})


  const {managerInfo} = managerInfoStore();
  let orderInfo = reactive({}) as IOrderBackLogInfo
  let commodityInfo = reactive({}) as ICommodityBackLogInfo

  let isLoading = ref(false)
  onMounted(() => {
      // 获取待办事项
      toGetBackLogList()
  })

const toGetBackLogList = () => {
    isLoading.value = true;
    getBackLogList(managerInfo.communityId)
        .then(res => {
            Object.assign(orderInfo, res.data.orderBackLogInfo)
            Object.assign(commodityInfo, res.data.commodityBackLogInfo)
            isLoading.value = false;
        });
}
</script>

<style lang="scss" scoped>
  .backlog-container{
    display: flex;
      >*{
          margin-right: 20px;
      }
      .card-content-item{
          display: flex;
          align-items: center;
          padding-top: 10px;
          font-size: 0.8rem;
          color: #a8abb2;

          .text-description{
              margin-right: 4px;
          }
          .text-red{
              color: #fb3c00;
          }
          .el-icon-tips{
              margin-left: 8px;
          }
      }
  }
</style>