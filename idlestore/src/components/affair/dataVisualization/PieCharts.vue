<template>
    <div class="pie-charts-container">
        <div class="title">{{ title }}</div>
        <div class="echarts" id="charts" ref="chart"></div>
    </div>
</template>

<script setup lang="ts">
import * as echarts from "echarts"
import {onMounted, ref} from "vue";
import {managerInfoStore} from "../../../store/manager.info.ts";
import emitter from "../../../utils/event.bus.ts";

// 全局事件总线 --> 刷新按钮
emitter.on("refresh", info => {
    if(info === '数据统计'){
        getChartData();
    }
})

let chart = ref()
const {title, getData} = defineProps(["title", "getData"]);
let myChart;
const communityId = managerInfoStore().managerInfo.communityId;

onMounted(() => {

    // 等DOM渲染好了再渲染图标, 避免拿不到DOM的宽高
    setTimeout(()=>{

        myChart = echarts.init(chart.value);
        let option = {
            tooltip: {
                trigger: 'item'
            },
            legend: {
                left: 'right'
            },
            series: [
                {
                    name: title,
                    type: 'pie',
                    data:[],
                    radius: '50%',
                    // 调整位置, 第一个为上下方向, 第二个是左右
                    center: ['50%', '61%'],
                    label: {
                      normal: {
                          formatter: '{c}',
                          textStyle: {
                              fontWeight: 'normal',
                              fontSize: 15
                          }
                      }
                    },
                    emphasis: {
                        itemStyle: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        }
                    }
                }
            ]
        };

        myChart.setOption(option);
        getChartData();
        window.addEventListener(
            "resize",
            () => {
                myChart.resize();
            },
            false
        );
    },0);

})

const getChartData = () => {
    getData(communityId)
        .then(res => {
            myChart.setOption({
                series: [{
                    name: title,
                    data: res.data,
                }]
            })
        })
}

</script>

<style lang="scss" scoped>
.pie-charts-container{
    width: 100%;
    height: 30%;
    background-color: #ffffff;
    border-radius: 6px;
    padding: 10px;
    margin-bottom: 10px;
    .title{
        font-size: 0.9rem;
        border-bottom: 1px solid #f5f5f5;
        padding-bottom: 5px;
        font-weight: bold;
    }
    .title::before{
        content: "";
        border: 1px solid #91cc75;
        margin-right: 5px;
    }
    .echarts{
        width: 100%;
        height: 90%;
    }
}

</style>