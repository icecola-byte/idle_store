<template>
    <el-tag type="primary" @click="changeCommodityDialogVisible(true)" size="small" effect="light">查看</el-tag>
  <!-- 更新用户信息对话框 -->
    <el-dialog v-model="updateCommodityDialogVisible" title="查看商品信息" width="500"
               :append-to-body="true"
               draggable>
        <el-carousel class="carousel-container" height="180px">
            <el-carousel-item v-for="item in commodityInfo.pictureUrl.split('|')" :key="item">
                <el-image class="commodity-picture" :src="item" fit="scale-down"
                          :preview-src-list="commodityInfo.pictureUrl.split('|')"
                          :preview-teleported="true"/>
            </el-carousel-item>
        </el-carousel>
        <el-descriptions
                class="margin-top"
                :column="1"
                border
        >
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon><Goods /></el-icon>
                        <span class="label-text">商品名称</span>
                    </div>
                </template>
                <div style="display: flex; align-items: center; justify-content: center;">
                {{commodityInfo.commodityName}}
                </div>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon>
                            <User />
                        </el-icon>
                        <span class="label-text">所属用户</span>
                    </div>
                </template>
                <div style="display: flex; align-items: center; justify-content: center;">
                {{commodityInfo.username}}
                </div>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon>
                            <Iphone />
                        </el-icon>
                        <span class="label-text">用户手机号</span>
                    </div>
                </template>
                <div style="display: flex; align-items: center; justify-content: center;">
                {{commodityInfo.phone}}
                </div>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon><Bicycle /></el-icon>
                        <span class="label-text">物流方式</span>
                    </div>
                </template>
                <div style="display: flex; align-items: center; justify-content: center;">
                    <template v-for="item in getLogisticsModeList(commodityInfo.logisticsMode)">
                        <el-tag :type="item.type">{{item.label}}</el-tag>
                    </template>
                </div>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon><GobletFull /></el-icon>
                        <span class="label-text">总数</span>
                    </div>
                </template>
                <div style="display: flex; align-items: center; justify-content: center;">
                {{commodityInfo.amount}}
                </div>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon><Goblet /></el-icon>
                        <span class="label-text">已售出数量</span>
                    </div>
                </template>
                <div style="display: flex; align-items: center; justify-content: center;">
                {{commodityInfo.soldOutNumber}}
                </div>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon>
                            <Coin />
                        </el-icon>
                        <span class="label-text">单价</span>
                    </div>
                </template>
                <div style="display: flex; align-items: center; justify-content: center;">
                {{commodityInfo.price}}
                </div>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon><Watch /></el-icon>
                        <span class="label-text">发布时间</span>
                    </div>
                </template>
                <div style="display: flex; align-items: center; justify-content: center;">
                    {{commodityInfo.publishTime}}
                </div>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon><Bell /></el-icon>
                        <span class="label-text">上门取货时间</span>
                    </div>
                </template>
                <div style="display: flex; align-items: center; justify-content: center;">
                    <span>{{commodityInfo.appointmentTime != null && commodityInfo.appointmentTime != '' ? commodityInfo.appointmentTime.split(' ')[0] : '--'}}</span>
                </div>
            </el-descriptions-item>

            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon><Notebook /></el-icon>
                        <span class="label-text">旧物描述</span>
                    </div>
                </template>
                <div style="display: flex; align-items: center; justify-content: center;">
                {{commodityInfo.remark ? commodityInfo.remark : '无'}}
                </div>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label>
                    <div class="cell-item">
                        <el-icon><Guide /></el-icon>
                        <span class="label-text">商品状态</span>
                    </div>
                </template>
                <div style="display: flex; align-items: center; justify-content: center;">
                {{commodityInfo.status.statusName}}
                </div>
            </el-descriptions-item>
        </el-descriptions>
        <template #footer>
            <div class="dialog-footer">
                <!-- 待审核 -->
                <template v-if="commodityInfo.status.status == 1">
                    <el-button type="warning" plain @click="commodityOperation(commodityInfo.commodityId, 2)">驳回</el-button>
                    <el-button type="success" plain @click="commodityOperation(commodityInfo.commodityId, 1)">通过</el-button>
                </template>
                <template v-else-if="commodityInfo.status.status == 2">
                    <el-button type="warning" plain @click="commodityOperation(commodityInfo.commodityId, 2)">驳回</el-button>
                </template>
            </div>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import {Goods, Coin, Iphone, Goblet, GobletFull, Guide,Notebook, Watch, Bicycle, User, Bell} from "@element-plus/icons-vue";
import {inject, ref} from "vue";
import {auditCommodity} from "../../../service/Commodity/commodity.api.ts";
import {managerInfoStore} from "../../../store/manager.info.ts";
import {failMessage, successMessage} from "../../../utils/el.message.ts";

defineProps(["commodityInfo", "getLogisticsModeList"]);
const refreshOp = inject("refreshOp");

const updateCommodityDialogVisible = ref(false);

const cancelUpdateCommodityInfo = () => {
    changeCommodityDialogVisible(false);
}

const changeCommodityDialogVisible = (val) => {
    updateCommodityDialogVisible.value = val;
}

const commodityOperation = (commodityId: string, op: number) => {
    auditCommodity(commodityId, op, managerInfoStore().managerInfo.id)
        .then(res => {
            if(res.code == 200){
                successMessage("修改成功");
            }else if(res.code == 500){
                failMessage("修改失败")
            }
        }).finally(() => {
            refreshOp();
            cancelUpdateCommodityInfo();
    })
}
</script>

<style lang="scss" scoped>
.commodity-picture{
    width: 100%;
    height: 100%;
}
.carousel-container{
    margin-bottom: 10px;
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
</style>