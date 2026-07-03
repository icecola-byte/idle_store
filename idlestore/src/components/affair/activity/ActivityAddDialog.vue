<template>
      <div class="operation-btn-group">
          <el-button type="primary" plain round  @click="addActivityDialogVisible = true" size="default" :icon="CirclePlus">新增</el-button>
      </div>
      <!-- 新增活动 -->
      <el-dialog v-model="addActivityDialogVisible" title="新增活动" width="500"
                 @close="cancelAddActivity">
          <el-form :model="addActivityInfo">
              <el-form-item label="起止日期">
                  <el-date-picker
                          v-model="addActivityInfo.date"
                          type="daterange"
                          start-placeholder="开始日期"
                          end-placeholder="结束日期"
                          :disabled-date="disabledDateFun"
                          :default-value="[new Date(), new Date()]"
                  />
              </el-form-item>
              <el-form-item label="起止时间">
                  <el-time-select
                          v-model="addActivityInfo.startTime"
                          style="width: 180px;margin-right: 40px;"
                          :max-time="addActivityInfo.endTime"
                          class="mr-4"
                          placeholder="开始时间"
                          start="07:00"
                          step="00:30"
                          end="20:00"
                  />
                  <el-time-select
                          v-model="addActivityInfo.endTime"
                          style="width: 180px"
                          :min-time="addActivityInfo.startTime"
                          placeholder="结束时间"
                          start="07:00"
                          step="00:30"
                          end="20:00"
                  />
              </el-form-item>
              <el-form-item label="活动地点">
                <el-input type="text" v-model="addActivityInfo.tradingPlace">
                </el-input>
              </el-form-item>
              <el-form-item label="负责人">
                <el-input type="text"  v-model="addActivityInfo.name"></el-input>
              </el-form-item>
              <el-form-item label="联系方式">
                  <el-input type="text"  v-model="addActivityInfo.phone"></el-input>
              </el-form-item>
          </el-form>
          <template #footer>
              <div class="dialog-footer">
                  <el-button @click="cancelAddActivity">取消</el-button>
                  <el-button type="primary" @click="addActivity">提交</el-button>
              </div>
          </template>
      </el-dialog>
</template>

<script lang="ts" setup>
import {CirclePlus} from "@element-plus/icons-vue";
import {inject, reactive, ref} from "vue";
import {managerInfoStore} from "../../../store/manager.info.ts";
import {failMessage, successMessage} from "../../../utils/el.message.ts";
import {addActivityAPI} from "../../../service/Activity/activity.api.ts";
import dateUtil from '../../../utils/dateFormat'


const refreshOp = inject("refreshOp");
let addActivityDialogVisible = ref(false);
const managerInfo = managerInfoStore().managerInfo;
const addActivityInfo = reactive({
    date: [],
    startTime: '',
    endTime: '',
    name: managerInfo.name,
    phone: managerInfo.phone,
    tradingPlace: managerInfo.tradingPlace,
});
const cancelAddActivity = () => {
    addActivityDialogVisible.value = false;
    resetAddActivityInfo();
}

const resetAddActivityInfo = () => {
    addActivityInfo.date = [];
    addActivityInfo.startTime = '';
    addActivityInfo.endTime = '';
    addActivityInfo.name = managerInfo.name;
    addActivityInfo.phone = managerInfo.phone;
    addActivityInfo.tradingPlace = managerInfo.tradingPlace;
}

const addActivity = () => {
    // 校验
    const message = regActivityInfo()
    if(message != null){
        return failMessage(message);
    }
    // 数据处理
    const data = {
        communityId: managerInfo.communityId,
        startTime: addActivityInfo.date[0],
        endTime: addActivityInfo.date[1],
        tradingPlace: addActivityInfo.tradingPlace,
        activityManager: addActivityInfo.name,
        managerPhone: addActivityInfo.phone,
    }
    data.startTime.setHours(Number(addActivityInfo.startTime.split(":")[0]));
    data.startTime.setMinutes(Number(addActivityInfo.startTime.split(":")[1]));
    data.endTime.setHours(Number(addActivityInfo.endTime.split(":")[0]));
    data.endTime.setMinutes(Number(addActivityInfo.endTime.split(":")[1]));
    data.startTime = dateUtil.formatDateTime(data.startTime);
    data.endTime = dateUtil.formatDateTime(data.endTime);
    // 请求添加活动
    addActivityAPI(data).then(res => {
        if(res.code == 200){
            successMessage(res.message);
        }else if(res.code == 500){
            failMessage(res.message);
        }
    }).finally(() => {
        cancelAddActivity();
        refreshOp();
    })
}

const regActivityInfo = () => {
    let message = null;
    if(addActivityInfo.date.length != 2){
        message = '请输入起止日期';
    }else if(addActivityInfo.startTime === ''){
        message = '请输入开始时间';
    }else if(addActivityInfo.endTime === ''){
        message = '请输入结束时间';
    }else if(addActivityInfo.tradingPlace === ''){
        message = '请输入交易地点';
    }else if(addActivityInfo.name === ''){
        message = '请输入负责人姓名';
    }else if(addActivityInfo.phone === ''){
        message = '请输入负责人联系电话';
    }
    return message;
}

// 活动只能从明天开始举办
const disabledDateFun = (time) => {
    return time.getTime() < new Date().getTime();
}


</script>

<style lang="scss" scoped>
.operation-btn-group{
  margin-top: -18px;
  margin-bottom: 10px;
}
</style>