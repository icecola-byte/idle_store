<template>
    <div class="line-charts-container">
        <div class="title">
            <span>{{ title }}</span>
            <slot></slot>
        </div>
        <div class="echarts" id="charts" ref="chart"></div>
    </div>
</template>

<script lang="ts" setup>
import {onMounted, ref} from "vue";
import * as echarts from "echarts";
import {managerInfoStore} from "../../../store/manager.info.ts";

let chart = ref();
const {title, getData} = defineProps(["title", "getData"]);
let myChart = null;
let communityId = managerInfoStore().managerInfo.communityId;
onMounted(() => {

    setTimeout(() => {

        myChart = echarts.init(chart.value);

        let option = {
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: []
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            toolbox: {
                feature: {
                    saveAsImage: {
                        title: '保存为图片'
                    }
                }
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: []
            },
            yAxis: {
                type: 'value'
            },
            series: []
        };
        myChart.setOption(option);

        window.addEventListener(
            "resize",
            () => {
                myChart.resize();
            },
            false
        );
    }, 0)

});
const CategoryChange = (data) => {
    if(myChart !== null){
        getData(communityId, data)
            .then(res => {
                console.log(res);
                let label = res.data.data.map(i => i.name);
                let data = res.data.data.map(i => {i["smooth"] = true; i["type"] = "line"; return i;});
                myChart.setOption({
                    legend: {
                        data: label,
                    },
                    xAxis: {
                        data: res.data.xData,
                    },
                    series: data,
                })
            })
    }

}
defineExpose({CategoryChange});

</script>

<style lang="scss" scoped>
   .line-charts-container{
       width: 100%;
       height: 100%;
       background-color: #ffffff;
       border-radius: 6px;
       padding: 10px;
       margin-bottom: 10px;
       .title{
           font-size: 0.9rem;
           border-bottom: 1px solid #f5f5f5;
           padding-bottom: 5px;
           font-weight: bold;
           display: flex;
           align-items: stretch;
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