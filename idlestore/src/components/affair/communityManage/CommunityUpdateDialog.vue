<template>
    <el-tag type="primary" @click="changeCommunityDialogVisible(true)" size="small" effect="light">修改</el-tag>
  <!-- 更新用户信息对话框 -->
    <el-dialog v-model="updateCommunityDialogVisible" title="修改社区信息" width="500"
               :append-to-body="true"
               draggable>
        <el-descriptions
                class="margin-top"
                :column="1"
                border
        >
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <span class="label-text">所在省</span>
                    </div>
                </template>
                <span>{{communityInfo.provinceName}}</span>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <span class="label-text">所在市</span>
                    </div>
                </template>
                <span>{{communityInfo.cityName}}</span>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <span class="label-text">所在区</span>
                    </div>
                </template>
                <span>{{communityInfo.streetName}}</span>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <span class="label-text">社区名称</span>
                    </div>
                </template>
                <span>{{communityInfo.communityName}}</span>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon>
                            <Iphone />
                        </el-icon>
                        <span class="label-text">管理员姓名</span>
                    </div>
                </template>
                <div class="manager-name-input-container">
                    <el-input @blur="managerNameInputCompleted" class="manager-name-input" v-model="waitUpdateCommunityInfo.managerName" size="small" v-if="updateManagerName"></el-input>
                    <el-text v-if="!updateManagerName">{{waitUpdateCommunityInfo.managerName}}</el-text>
                    <el-tag @click="updateManagerName = true" v-if="!updateManagerName">修改</el-tag>
                </div>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon>
                            <Iphone />
                        </el-icon>
                        <span class="label-text">管理员手机号</span>
                    </div>
                </template>
                <div class="manager-phone-input-container">
                    <el-input @blur="managerPhoneInputCompleted" class="manager-phone-input" v-model="waitUpdateCommunityInfo.managerPhone" size="small" v-if="updateManagerPhone"></el-input>
                    <el-text v-if="!updateManagerPhone">{{waitUpdateCommunityInfo.managerPhone}}</el-text>
                    <el-tag @click="updateManagerPhone = true" v-if="!updateManagerPhone">修改</el-tag>
                </div>
            </el-descriptions-item>
        </el-descriptions>
        <template #footer>
            <div class="dialog-footer">
                <el-button @click="cancelUpdateCommunityInfo">取消</el-button>
                <el-button type="primary" @click="updateCommunityInfo">
                    提交
                </el-button>
            </div>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import {Lock, User, Iphone, Camera,Location,Male,Coin} from "@element-plus/icons-vue";
import {inject, reactive, ref} from "vue";
import {updateUserAPI} from "../../../service/User/user.api.ts";
import {IUserInfo} from "../../../service/User/types.ts";
import {failMessage, successMessage} from "../../../utils/el.message.ts";
import {updateCommunityAPI} from "../../../service/Community/community.api.ts";

const {communityInfo} = defineProps(["communityInfo"]);
const refreshOp = inject("refreshOp");

// 待修改用户的信息
let waitUpdateCommunityInfo = reactive({}) as IUserInfo;
Object.assign(waitUpdateCommunityInfo, communityInfo);

const updateCommunityDialogVisible = ref(false);
// 更新操作
const updateCommunityInfo = () => {
    updateCommunityAPI(waitUpdateCommunityInfo).then(res => {
        if(res.code === 200){
            successMessage(res.message);
        }else{
            failMessage(res.message);
        }
        refreshOp();
    }).finally(() => {
        updateCommunityDialogVisible.value = false;
    })
}

const cancelUpdateCommunityInfo = () => {
    changeCommunityDialogVisible(false);
}

const changeCommunityDialogVisible = (val) => {
    updateCommunityDialogVisible.value = val;
}

const updateManagerPhone = ref(false);
const updateManagerName = ref(false);

const managerPhoneInputCompleted = () => {
    updateManagerPhone.value = false;
}

const managerNameInputCompleted = () => {
    updateManagerName.value = false;
}
</script>

<style lang="scss" scoped>
.cell-item{
  display: flex;
  align-items: center;
  justify-content: center;
  width: 70%;
  .label-text{
    margin-left: 10px;
  }
}
.manager-phone-input-container, .manager-name-input-container{
  display: flex;
  align-items: center;
  justify-content: space-between;
  .manager-phone-input, .manager-name-input{
    width: 140px;
  }
}


</style>