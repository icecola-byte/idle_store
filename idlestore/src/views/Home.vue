<template>
    <div class="container">
        <el-row>
            <el-col :span="!toggleMenu.toggle ? 3: 1">
                <div class="navigation-menu" :style="{width: toggleMenu.toggle ? '64px' : '100%'}"><TheNavigation/></div>
            </el-col>
            <el-col :span="!toggleMenu.toggle ? 21: 23" :style="{'margin-left': !toggleMenu.toggle ? '0' : '-14px'}">
                <HomeHeader />
                <HomeTags />
            </el-col>
        </el-row>
    </div>
</template>

<script setup lang="ts">
    import TheNavigation from "../components/navigation/TheNavigation.vue";
    import { useToggleMenuStore } from "../store/navigation.ts";
    import HomeHeader from "../components/home/HomeHeader.vue";
    import HomeTags from "../components/home/HomeTags.vue";

    const toggleMenu = useToggleMenuStore();


</script>

<style lang="scss" scoped>
    .container{
        min-height: 100vh;
    }
    .navigation-menu{
        min-height: 100vh;
        height: 100%;
        background-color: #545c64;
        transition: width 0.5s ease;
    }
</style>