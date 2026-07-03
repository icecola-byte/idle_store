<template>
    <el-table :data="orderData"
              stripe
              border
              size="small"
              highlight-current-row
              row-key="orderId"
              table-layout="auto">
        <el-table-column fixed type="index" label="序号" :index="index => index + 1 + (pageInfo.currentPage - 1) * pageInfo.pageSize" align="center"/>
        <el-table-column prop="orderId" label="订单编号" align="center"/>
        <el-table-column label="买家用户名" align="center">
            <template #default="scope">
                <div style="display: flex; align-items: center; justify-content: space-evenly;">
                    {{scope.row.buyerInfo.name}}
                </div>
            </template>
        </el-table-column>
        <el-table-column label="卖家用户名" align="center">
            <template #default="scope">
                <div style="display: flex; align-items: center; justify-content: space-evenly;">
                    {{scope.row.sellerInfo.name}}
                </div>
            </template>
        </el-table-column>
        <el-table-column prop="commodityInfo" label="商品名" align="center">
            <template #default="scope">
                <div style="display: flex; align-items: center; justify-content: space-evenly;">
                    {{scope.row.commodityInfo.name}}
                </div>
            </template>
        </el-table-column>
        <el-table-column prop="orderQuantity" label="下单数量" align="center"/>
        <el-table-column prop="orderTime" label="下单时间" align="center">
            <template #default="scope">
                <div style="display:flex;align-items: center;justify-content: center;">
                    <span>{{scope.row.orderTime.replace("T", " ")}}</span>
                </div>
            </template>
        </el-table-column>
        <el-table-column prop="appointmentTime" label="上门取件日期" align="center">
            <template #default="scope">
                <div style="display:flex;align-items: center;justify-content: center;">
                    <span>{{scope.row.appointmentTime == null ? '...' : scope.row.appointmentTime.split("T")[0]}}</span>
                </div>
            </template>
        </el-table-column>
        <el-table-column prop="price" label="成交金额" align="center"/>
        <el-table-column prop="logisticsMode" label="物流方式" align="center">
            <template #default="scope">
                <div style="display:flex;align-items: center;justify-content: center;">
                    <el-tag :type="logisticsModeColor[getLogisticsCode(scope.row.logisticsMode)]">{{scope.row.logisticsMode}}</el-tag>
                </div>
            </template>
        </el-table-column>
        <el-table-column prop="status.statusName" label="订单状态" align="center"/>
        <el-table-column prop="tradingPlace" label="交易地点" align="center"/>
        <el-table-column label="操作" align="center">
            <template #default="scope">
                <div style="display:flex;align-items: center;justify-content: center;">
                    <el-tag type="primary" @click="openDrawer(scope.row)">详情</el-tag>
                </div>
            </template>
        </el-table-column>
        <el-drawer
                v-model="openDrawerToSeeOrderInfo"
                :title="'订单编号: ' + showOrderInfo.orderId"
                :append-to-body="true"
                direction="ttb"
                size="70%"
                style="background-color: #ffffff;"
        >
            <div class="drawer-order-info">
                <div class="sellerInfo-container">
                    <div class="description-text">
                        卖家信息
                    </div>
                    <div class="picture-container">
                        <el-image class="user-avatar" :src="showOrderInfo.sellerInfo.avatar" fit="cover"/>
                    </div>
                    <div class="user-other-info">
                        <div class="user-name">
                            {{showOrderInfo.sellerInfo.name}}
                        </div>
                        <div class="user-phone">
                            {{showOrderInfo.sellerInfo.phone}}
                        </div>
                    </div>
                </div>
                <div class="orderInfo-container">
                    <div class="picture-container">
                        <el-image class="drawer-commodity-picture"
                                  :preview-teleported
                                  :src="showOrderInfo.commodityInfo.picture"
                                  fit="cover"/>
                    </div>
                    <div class="order-other-info">
                        <div class="description-text">
                            订单信息
                        </div>
                        <div class="commodity-name" style="display: flex; justify-content: space-evenly">
                            {{showOrderInfo.commodityInfo.name}}
                        </div>
                        <div class="commodity-category" style="display: flex; width: 120%; justify-content: space-between">
                            <span>商品分类</span> <span>{{showOrderInfo.commodityInfo.category}}</span>
                        </div>
                        <div class="logistics-container" style="display: flex; width: 120%; justify-content: space-between">
                            <span>物流方式</span> <span>{{showOrderInfo.logisticsMode}}</span>
                        </div>
                        <div class="order-quantity-container" style="display: flex; width: 120%; justify-content: space-between">
                            <span>下单数量</span> <span>{{showOrderInfo.orderQuantity}}</span>
                        </div>
                        <div class="order-time-container" style="display: flex; width: 120%; justify-content: space-between">
                            <span>下单时间</span> <span>{{showOrderInfo.orderTime.replace("T", " ")}}</span>
                        </div>
                        <div class="price-container" style="display: flex; width: 120%; justify-content: space-between">
                            <span>成交金额</span> <span>{{showOrderInfo.price}}</span>
                        </div>
                        <div class="status-container" style="display: flex; width: 120%; justify-content: space-between">
                            <span>订单状态</span> <span>{{showOrderInfo.status.statusName}}</span>
                        </div>
                        <div class="trading-place-container" style="display: flex; width: 120%; justify-content: space-between">
                            <span>交易地点</span> <span>{{showOrderInfo.tradingPlace}}</span>
                        </div>
                        <div class="coupon-container" style="display: flex; width: 120%; justify-content: space-between">
                            <span>优惠卷</span> <span>{{showOrderInfo.coupon}}</span>
                        </div>
                    </div>

                </div>
                <div class="buyerInfo-container">
                    <div class="description-text">
                        买家信息
                    </div>
                    <div class="picture-container">
                        <el-image class="user-avatar" :src="showOrderInfo.buyerInfo.avatar" fit="cover"/>
                    </div>
                    <div class="user-other-info">
                        <div class="user-name">
                            {{showOrderInfo.buyerInfo.name}}
                        </div>
                        <div class="user-phone">
                            {{showOrderInfo.buyerInfo.phone}}
                        </div>
                    </div>

                </div>
            </div>
            <template v-if="showOrderInfo.logisticsMode == '上门回收' && showOrderInfo.status.status == 1" #footer>
                <el-button type="info" plain @click="cancel">取消订单</el-button>
                <el-button type="success" plain @click="coinInputDialogVisible = true">完成订单</el-button>
            </template>
            <el-dialog
                v-model="coinInputDialogVisible"
                title="请输入硬币发放数量"
                width="500"
                align-center
            >
                <el-input-number v-model="coinNumber" :min="1" :step-strictly="true" :controls="false"
                size="small"></el-input-number>
                <template #footer>
                    <div class="dialog-footer">
                        <el-button @click="coinInputDialogVisible = false">取消</el-button>
                        <el-button type="primary" @click="coinInputComplete">确认</el-button>
                    </div>
                </template>
            </el-dialog>
        </el-drawer>
    </el-table>
</template>

<script lang="ts" setup>
import {inject, onMounted, reactive, readonly, ref} from "vue";
import {getCommodityLogisticsMode} from "../../../service/Commodity/commodity.api.ts";
import {cancelOrder, completeOrder} from "../../../service/Order/order.api.ts";
import {failMessage, successMessage} from "../../../utils/el.message.ts";

defineProps(["orderData", "pageInfo"])
const refreshOp = inject("refreshOp");

const getLogisticsCode = (mode:string) => {
    switch (mode){
        case '上门回收': return 1;
        case '物物置换': return 2;
        case '送货上门': return 4;
        case '社区贸易': return 8;
    }
}
let logisticsModeColor = readonly({
    1: 'success',
    2: 'primary',
    4: 'info',
    8: 'warning',
})
let logisticsMode = reactive({});
onMounted(() => {
    getCommodityLogisticsMode().then(res => {
        logisticsMode = res.data;
    })
})

const openDrawerToSeeOrderInfo = ref(false);
const coinInputDialogVisible = ref(false);
const coinNumber = ref(1);
let showOrderInfo = reactive({});

const openDrawer = (content) => {
    showOrderInfo = content;
    openDrawerToSeeOrderInfo.value = true;
}

const getLogisticsModeList = (logistic: number): Array<{type: string, label: string}> => {
    let logisticsArray = [];
    let i = 1;
    while(logistic != 0){
        if(logistic & 1){
            logisticsArray.push({type: logisticsModeColor[i], label: logisticsMode[i]});
        }
        i = i << 1;
        logistic = logistic >> 1;
    }
    return logisticsArray;
}

const cancel = () => {
    cancelOrder(showOrderInfo.orderId).then(res => {
        if(res.code == 200){
            successMessage("取消成功")
        }else if(res.code == 500){
            failMessage("取消失败");
        }
    }).finally(() => {
        openDrawerToSeeOrderInfo.value = false;
        refreshOp();
    })
}

const coinInputComplete = () => {
    completeOrder(showOrderInfo.orderId, showOrderInfo.sellerInfo.userId, coinNumber.value)
        .then(res => {
            openDrawerToSeeOrderInfo.value = false;
            refreshOp();
            if(res.code == 200){
                successMessage("订单已完成");
            }else if(res.code == 500){
                failMessage("操作失败");
            }
        }).catch(err => {
            openDrawerToSeeOrderInfo.value = false;
        refreshOp();
        }).finally(() => {
            coinInputDialogVisible.value = false;
            coinNumber.value = 1;

    })
}
</script>

<style lang="scss" scoped>
.commodity-picture{
  width: 40px;
  height: 40px;
  border-radius: 4px;
}
.drawer-order-info{
  color: #2c5c7f;
  display: flex;
  height: 100%;
  justify-content: space-evenly;
  >div{
    width: 18%;
    display: flex;
    flex-direction: column;
    align-items: center;
    border: 1px solid #f5f5f5;
    box-shadow: 1px 1px 15px #f3f0e1;
    border-radius: 10px;
    position: relative;
    transition: 0.3s;
    background-color: #f3f0e1;
    .description-text{
      position: absolute;
      top: 18px;
      left: 18px;
      font-size: 0.9rem;
    }
    .picture-container{
        height: 60%;
        display: flex;
        align-items: center;
        justify-content: center;
    }
  }
  >div:hover{
    // 荧光效果
    box-shadow: 0 0 10px #f3f0e1 inset, 0 0 15px #f3f0e1, 1px 1px 15px #f3f0e1;
    translate: -6px -6px;
  }
  .sellerInfo-container, .buyerInfo-container{
    color: #2c5c7f;
    .user-avatar{
      width: 150px;
      height: 150px;
      border-radius: 50%;
    }
    .user-name{
      font-size: 1.2rem;
    }
  }
  .orderInfo-container{
    border: 1px solid #f3f0e1;
    .commodity-name{
      font-size: 1.2rem;
    }
    .picture-container{
      display: flex;
      .drawer-commodity-picture{
        width: 150px;
        height: 150px;
        border-radius: 10px;
      }
    }
  }
    .order-other-info, .user-other-info{
        height: 60%;
        display: flex;
        margin-top: -20%;
        flex-direction: column;
        align-items: center;
        justify-content: space-evenly;
        font-size: 0.9rem;
    }
}
</style>