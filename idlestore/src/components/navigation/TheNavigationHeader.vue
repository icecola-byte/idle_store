<template>
    <div class="navigation-header">
        <el-image class="logo-image" src="../../../apple2.png" fit="fill" alt="logo"/>
        <text class="header-text" v-if="!toggleMenu.toggle">{{applicationName}}</text>
        <el-icon @click="toggle" :class="[state ? 'arrow-right' : 'arrow-left']">
            <ArrowRight />
        </el-icon>
    </div>
</template>

<script lang="ts" setup>
    import {ref} from "vue";
    import { ArrowRight } from '@element-plus/icons-vue'
    import {useToggleMenuStore} from "../../store/navigation.ts";

    const applicationName = ref("社区闲置物品回收")
    let state = ref(true);
    const toggleMenu = useToggleMenuStore();
    const toggle = () => {
        state.value = !state.value;
        toggleMenu.toggleMenu();
    }


</script>

<style lang="scss" scoped>
    .navigation-header{
        display: flex;
        align-items: center;
        padding: 10px 0;
        box-sizing: border-box;
        color: #dddddd;
        font-size: 0.8rem;
        justify-content: space-evenly;
        .logo-image{
            width: 24px;
            height: 24px;
        }
        // 箭头旋转
        .arrow-right{
            transition: all 0.3s ease;
        }
        .arrow-left{
            transform: rotate(-180deg);
            transition: all 0.3s ease;
        }
    }
</style>