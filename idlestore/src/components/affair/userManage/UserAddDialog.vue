<template>
    <div class="operation-btn-group">
        <el-button type="primary" plain round  @click="addUserDialogVisible = true" size="default" :icon="CirclePlus">新增</el-button>
    </div>
  <!-- 新增用户对话框 -->
    <el-dialog v-model="addUserDialogVisible" title="新增用户" width="500"
    @close="cancelAddUserInfo">
        <el-form :model="addUserInfo"
                 :rules="rules">
            <el-form-item label="手机号" prop="phone">
                <el-input v-model="addUserInfo.phone" autocomplete="off" :maxlength="11" />
            </el-form-item>
            <el-form-item label="用户头像">
                <el-upload
                        drag
                        action="http://localhost:8080/image/upload"
                        name="image"
                        show-file-list
                        method="post"
                        list-type="picture"
                        style="width: 210px;"
                        :limit="1"
                        :on-success="uploadImageSuccess"
                >
                    <el-icon class="el-icon--upload"><upload-filled/></el-icon>
                    <div class="el-upload__text">
                        拖拽文件到这里或 <em>点击以上传</em>
                    </div>
                    <template #tip>
                        <div class="el-upload__tip">
                            小于 10MB 的 png/jpg 图片
                        </div>
                    </template>
                </el-upload>
            </el-form-item>
            <el-form-item label="用户名">
                <el-input type="text" placeholder="新开的小店" v-model="addUserInfo.username"/>
            </el-form-item>
            <el-form-item label="性别">
                <el-radio-group v-model="addUserInfo.sex">
                    <el-radio :value="1">男</el-radio>
                    <el-radio :value="0">女</el-radio>
                </el-radio-group>
            </el-form-item>
        </el-form>
        <template #footer>
            <div class="dialog-footer">
                <el-button @click="cancelAddUserInfo">取消</el-button>
                <el-button type="primary" @click="addUser">
                    提交
                </el-button>
            </div>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import {CirclePlus, UploadFilled} from "@element-plus/icons-vue";
import {inject, reactive, ref} from "vue";
import {ElMessage, FormRules} from "element-plus";
import {IAddUserInfo} from "../../../service/User/types.ts";
import {addUserAPI} from "../../../service/User/user.api.ts";
import {managerInfoStore} from "../../../store/manager.info.ts";

const refreshOp = inject("refreshOp");
let addUserDialogVisible = ref(false);
const changeUserDialogVisible = (val: boolean) => {
    addUserDialogVisible.value = val;
}
// 1.新增用户
// 1.1 用户信息

const addUserInfo = reactive({
    phone: '',
    username: '',
    avatarUrl: '',
    sex: 1,
    communityId: managerInfoStore().managerInfo.communityId
}) as IAddUserInfo;
// 1.2 上传头像成功
const uploadImageSuccess = (response: any) => {
    addUserInfo.avatarUrl = response.data;
}

// 1.3 新增用户操作
const addUser = () => {
    addUserAPI(addUserInfo).then(res => {
        showMessage(res.code);
        refreshOp();
    }).finally(() => {
        changeUserDialogVisible(false);
    })
}
// 1.4 取消新增操作(清空表单)
const cancelAddUserInfo = () => {
    addUserInfo.phone = '';
    addUserInfo.username = '';
    addUserInfo.avatarUrl = '';
    addUserInfo.sex = 1;
    changeUserDialogVisible(false);
}

const showMessage = (code: number) => {
    if(code == 200){
        ElMessage({
            message: '添加成功',
            type: 'success'
        })
    }else {
        ElMessage({
            message: '添加失败, 用户已存在',
            type: 'warning'
        })
    }
}

// 1.5 手机号校验
const rules = reactive<FormRules<IAddUserInfo>>({
    phone: [
        {
            required:true,
            message: '请输入正确的手机号',
            trigger: 'blur',
            validator: (rule, value) => (/^1(3\d|4[5-9]|5[0-35-9]|6[2567]|7[0-8]|8\d|9[0-35-9])\d{8}$/).test(value)
        }
    ]
})
</script>

<style lang="scss" scoped>
.operation-btn-group{
  margin-top: -18px;
  margin-bottom: 10px;
}
</style>