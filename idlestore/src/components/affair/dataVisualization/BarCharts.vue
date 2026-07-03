<template>
    <div class="bar-charts-container">
        <div class="title">{{title}}</div>
        <div class="echarts" id="charts" ref="chart"></div>
    </div>
</template>

<script lang="ts" setup>
import {onMounted, ref} from "vue";
import * as echarts from "echarts";
import {managerInfoStore} from "../../../store/manager.info.ts";
import emitter from "../../../utils/event.bus.ts";

// 全局事件总线 --> 刷新按钮
emitter.on("refresh", info => {
    if(info === '数据统计'){
        getChartData();
    }
})

const {title, getData} = defineProps(["title", "getData"]);
let chart = ref();
let myChart;

let communityId = managerInfoStore().managerInfo.communityId;
const labelOption = {
    show: true,
    position: 'inside',
    distance: 15,
    align: 'left',
    verticalAlign: 'middle',
    rotate: 0,
    // 设置为0时不显示
    formatter: function (params){
        if(params.value > 0){
            return params.value;
        }else {
            return '';
        }
    },
    fontSize: 16,
    rich: {
        name: {}
    }
};
onMounted(() => {


    setTimeout(()=>{

        myChart = echarts.init(chart.value);
        myChart.setOption<echarts.EChartsOption>({
            series: []
        });

        let option = {
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    type: 'shadow'
                }
            },
            legend: {
                data: []
            },
            toolbox: {
                show: true,
                orient: 'vertical',
                left: 'right',
                top: 'center',
                feature: {
                    mark: { show: true },
                    dataView: { show: true, readOnly: false },
                    magicType: { show: true, type: ['stack'] },
                    saveAsImage: { show: true }
                }
            },
            yAxis: [
                {
                    type: 'category',
                    axisTick: { show: false },
                    data: [],
                    axisLabel: {
                        interval: 0,
                        formatter: function(value){
                            let res = "";
                            for(let i = 0; i < value.length; i++){
                                res += value[i];
                                if((i + 1) % 2 === 0){
                                    res += "\n";
                                }
                            }
                            return res;
                        }
                    }
                }
            ],
            xAxis: [
                {
                    axisLabel: {
                        interval: 0,
                    },
                    type: 'value'
                }
            ],
            series: []
        };
        myChart.setOption(option);
        getChartData();

    }, 0);
})

const getChartData = () => {
    getData(communityId).then(res => {
        myChart.setOption({
            legend: {
                data: res.data.xdata,
            },
            yAxis: [
                {
                    type: 'category',
                    axisTick: { show: false },
                    data: res.data.ydata,
                }
            ],
            series: res.data.data.map((i, index) => {
                return {
                    name: res.data.xdata[index],
                    type: 'bar',
                    label: labelOption,
                    emphasis: {
                        focus: 'series'
                    },
                    data: i
                };
            })
        })
    })
}
</script>

<style lang="scss" scoped>
.bar-charts-container{
  width: 100%;
  height: 96%;
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
    height: 100%;
  }
}
</style>