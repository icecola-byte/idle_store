<template>
    <div class="operation-btn-group">
        <el-button type="primary" plain round  @click="addCommunityDialogVisible = true" size="default" :icon="CirclePlus">新增</el-button>
    </div>
  <!-- 新增社区 -->
    <el-dialog v-model="addCommunityDialogVisible" title="新增社区" width="500"
               @close="cancelAddCommunityInfo">
        <el-form ref="ruleFormRef"
                 :model="addCommunityInfo"
                 :rules="rules">
            <el-form-item label="所在街道" prop="streetCode">
                <el-cascader
                    v-model="addCommunityInfo.streetCode"
                    :props="streetProps"
                    clearable
                />
            </el-form-item>
            <el-form-item label="社区名称" prop="communityName">
                <el-input type="text" v-model="addCommunityInfo.communityName"></el-input>
            </el-form-item>
            <el-form-item label="社区贸易地点" prop="tradingPlace">
                <el-input type="text" v-model="addCommunityInfo.tradingPlace"></el-input>
            </el-form-item>
            <el-form-item label="管理员姓名" prop="managerName">
                <el-input type="text" v-model="addCommunityInfo.managerName"/>
            </el-form-item>
            <el-form-item label="管理员手机号" prop="managerPhone">
                <el-input v-model="addCommunityInfo.managerPhone" autocomplete="off" :maxlength="11" />
            </el-form-item>
        </el-form>
        <template #footer>
            <div class="dialog-footer">
                <el-button @click="cancelAddCommunityInfo">取消</el-button>
                <el-button type="primary" @click="addCommunity(ruleFormRef)">
                    提交
                </el-button>
            </div>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import {CirclePlus} from "@element-plus/icons-vue";
import {inject, reactive, ref} from "vue";
import {CascaderProps, ElMessage, FormInstance, FormRules} from "element-plus";
import {IAddCommunityInfo} from "../../../service/Community/types.ts";
import {getStreetCascadeInfoAPI, postCommunityAPI} from "../../../service/Community/community.api.ts";
import {failMessage} from "../../../utils/el.message.ts";

const refreshOp = inject("refreshOp");
let addCommunityDialogVisible = ref(false);
const changeCommunityDialogVisible = (val: boolean) => {
    addCommunityDialogVisible.value = val;
}

const ruleFormRef = ref<FormInstance>();

const addCommunityInfo = reactive<IAddCommunityInfo>({
    managerPhone: '',
    managerName: '',
    communityName: '',
    streetCode: [],
    tradingPlace: '',
});

// 社区信息
const streetProps: CascaderProps = {
    lazy: true,
    lazyLoad(node, resolve) {
        const { level } = node;
        let value = node.value ?? '';
        getStreetCascadeInfoAPI(level + 1, String(value))
            .then(res => {
                const nodes = res.data.map((item) => {
                    return {
                        value: item.value,
                        label: item.text,
                        leaf: !item.haveChildren
                    }
                });
                resolve(nodes);
            })
    },
}

const addCommunity = async(formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate((valid, fields) => {
        if (valid) {
            let communityPostData = {} as IAddCommunityInfo;
            Object.assign(communityPostData, addCommunityInfo);
            console.log(addCommunityInfo)
            communityPostData.streetCode = addCommunityInfo.streetCode[addCommunityInfo.streetCode.length - 1];
            console.log(communityPostData);
            postCommunityAPI(communityPostData).then(res => {
                showMessage(res.code);
                refreshOp();
            }).finally(() => {
                changeCommunityDialogVisible(false);
            })
        } else {
            failMessage("请正确填写信息");
        }
    })
}
// 1.4 取消新增操作(清空表单)
const cancelAddCommunityInfo = () => {
    addCommunityInfo.managerPhone = '';
    addCommunityInfo.managerName = '';
    addCommunityInfo.communityName = '';
    addCommunityInfo.streetCode = [];
    addCommunityInfo.tradingPlace = '';
    changeCommunityDialogVisible(false);
}

const showMessage = (code: number) => {
    if(code == 200){
        ElMessage({
            message: '添加成功',
            type: 'success'
        })
    }else {
        ElMessage({
            message: '添加失败',
            type: 'warning'
        })
    }
}

// 1.5 手机号校验
const rules = reactive<FormRules<IAddCommunityInfo>>({

    managerPhone: [
        {
            required:true,
            message: '请输入正确的手机号',
            trigger: 'blur',
            validator: (rule, value) => (/^1(3\d|4[5-9]|5[0-35-9]|6[2567]|7[0-8]|8\d|9[0-35-9])\d{8}$/).test(value)
        }
    ],
    communityName:[
        {
            required: true,
            message: '请输入社区名称',
            trigger: 'blur'
        }
    ],
    streetCode: [
        {
            required: true,
            message: '请选择所在街道',
            trigger: 'change'
        }
    ],
    tradingPlace: [
        {
            required: true,
            message: '请输入社区交易地点',
            trigger: 'blur'
        }
    ],
    managerName: [
        {
            required: true,
            message: '请输入管理员姓名',
            trigger: 'blur'
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