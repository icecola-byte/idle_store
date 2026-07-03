<template>
    <el-tag type="primary" @click="changeUserDialogVisible(true)" size="small" effect="light">修改</el-tag>
  <!-- 更新用户信息对话框 -->
    <el-dialog v-model="updateUserDialogVisible" title="修改用户信息" width="500"
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
                        <el-icon>
                            <User />
                        </el-icon>
                        <span class="label-text">用户名</span>
                    </div>
                </template>
                <div class="username-input-container">
                    <el-input @blur="usernameInputCompleted" class="username-input" v-model="waitUpdateUserInfo.username" size="small" v-if="updateUserName" maxlength="10"></el-input>
                    <el-text v-if="!updateUserName">{{waitUpdateUserInfo.username}}</el-text>
                    <el-tag @click="updateUserName = true" v-if="!updateUserName">修改</el-tag>
                </div>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon>
                            <Iphone />
                        </el-icon>
                        <span class="label-text">手机号</span>
                    </div>
                </template>
                {{waitUpdateUserInfo.phone}}
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon>
                            <Camera />
                        </el-icon>
                        <span class="label-text">头像</span>
                    </div>
                </template>
                <el-image class="user-avatar" fit="cover" :src="waitUpdateUserInfo.avatarUrl"></el-image>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon>
                            <Male />
                        </el-icon>
                        <span class="label-text">性别</span>
                    </div>
                </template>
                <el-tag size="small" effect="light" :type="waitUpdateUserInfo.sex === 1 ? 'primary':'danger'">{{waitUpdateUserInfo.sex === 1 ? '男' : '女'}}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon>
                            <Coin />
                        </el-icon>
                        <span class="label-text">硬币余额</span>
                    </div>
                </template>
                {{waitUpdateUserInfo.coinBalance}}
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon>
                            <Location />
                        </el-icon>
                        <span class="label-text">详细地址</span>
                    </div>
                </template>
                {{waitUpdateUserInfo.addressDetail == null ? '未知' :waitUpdateUserInfo.addressDetail}}
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon>
                            <Lock />
                        </el-icon>
                        <span class="label-text">账号状态</span>
                    </div>
                </template>
                <div class="user-status-input-container">
                    <el-text v-if="!updateUserStatus">{{waitUpdateUserInfo.isValid == false ? '使用中' : '封禁中'}}</el-text>
                    <el-tag @click="updateUserStatus = true" v-if="!updateUserStatus">修改</el-tag>
                    <el-select
                            @blur="updateUserStatus = false"
                            v-if="updateUserStatus"
                            v-model="waitUpdateUserInfo.isValid"
                            class="user-status-select"
                            size="small"
                    >
                        <el-option label="封禁中" :value="true" />
                        <el-option label="使用中" :value="false" />
                    </el-select>
                </div>

            </el-descriptions-item>
        </el-descriptions>
        <template #footer>
            <div class="dialog-footer">
                <el-button @click="cancelUpdateUserInfo">取消</el-button>
                <el-button type="primary" @click="updateUserInfo">
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

const {userInfo} = defineProps(["userInfo"]);
const refreshOp = inject("refreshOp");

// 待修改用户的信息
let waitUpdateUserInfo = reactive({}) as IUserInfo;
Object.assign(waitUpdateUserInfo, userInfo);

const updateUserDialogVisible = ref(false);
// 更新操作
const updateUserInfo = () => {
    updateUserAPI(waitUpdateUserInfo).then(res => {
        if(res.code === 200){
            successMessage('更新成功');
        }else{
            failMessage(res.message);
        }
        refreshOp();
    }).finally(() => {
        updateUserDialogVisible.value = false;
    })
}

const cancelUpdateUserInfo = () => {
    changeUserDialogVisible(false);
}

const changeUserDialogVisible = (val) => {
    updateUserDialogVisible.value = val;
}

const updateUserName = ref(false);
const updateUserStatus = ref(false);

const usernameInputCompleted = () => {
    updateUserName.value = false;
}
</script>

<style lang="scss" scoped>
  .user-avatar{
    width: 64px;
    height: 64px;
  }
  .cell-item{
    display: flex;
    align-items: center;
    justify-content: center;
    width: 70%;
    .label-text{
      margin-left: 10px;
    }
  }
  .username-input-container{
    display: flex;
    align-items: center;
    justify-content: space-between;
    .username-input{
      width: 140px;
    }
  }
  .user-status-input-container{
    display: flex;
    align-items: center;
    justify-content: space-between;
    .user-status-select{
      width: 140px;
    }
  }


</style>