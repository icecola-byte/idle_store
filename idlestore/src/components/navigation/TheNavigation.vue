<template>
    <TheNavigationHeader :isCollapse="toggleMenu.toggle"/>
    <el-menu
        class="el-menu-vertical-demo"
        :collapse="toggleMenu.toggle"
        :collapse-transition="false"
        active-text-color="#ffd04b"
        background-color="#545c64"
        text-color="#fff"
        @select="navigatorSelected"
    >
        <template v-if="isSuper">
            <el-menu-item index="1">
                <el-icon><OfficeBuilding /></el-icon>
                <template #title>
                    <span>社区管理</span>
                </template>
            </el-menu-item>
            <el-menu-item index="2">
                <el-icon><DataLine /></el-icon>
                <template #title>
                    <span>数据统计</span>
                </template>
            </el-menu-item>
        </template>
        <template v-else>
            <el-sub-menu index="1">
                <template #title>
                    <el-icon><Watch /></el-icon>
                    <span>待办事项</span>
                </template>
                <el-menu-item-group>
                    <el-menu-item index="1-1">
                        <span>商品审核</span>
                    </el-menu-item>
                    <el-menu-item index="1-2">
                        <span>上门回收</span>
                    </el-menu-item>
                </el-menu-item-group>
            </el-sub-menu>
            <el-menu-item index="2">
                <el-icon><User /></el-icon>
                <template #title>用户管理</template>
            </el-menu-item>
            <el-sub-menu index="3">
                <template #title>
                    <el-icon><OfficeBuilding /></el-icon>
                    <span>社区管理</span>
                </template>
                <el-menu-item-group>
                    <el-menu-item index="3-1">
                        <span>社区活动</span>
                    </el-menu-item>
                    <el-menu-item index="3-2">
                        <span>数据统计</span>
                    </el-menu-item>
                </el-menu-item-group>
            </el-sub-menu>
            <el-menu-item index="4">
                <el-icon><Goods /></el-icon>
                <template #title>商品管理</template>
            </el-menu-item>
            <el-menu-item index="5">
                <el-icon><Tickets /></el-icon>
                <template #title>订单管理</template>
            </el-menu-item>
        </template>
    </el-menu>


</template>

<script lang="ts" setup>
    import {
        User,
        Watch,
        Goods,
        OfficeBuilding,
        Tickets,
        DataLine
    } from '@element-plus/icons-vue'
    import TheNavigationHeader from "./TheNavigationHeader.vue";
    import { useToggleMenuStore } from "../../store/navigation.ts";
    import { homeTagsStore } from "../../store/home.tags.ts";
    import {managerInfoStore} from "../../store/manager.info.ts";
    const isSuper = managerInfoStore().managerInfo.isSuper;

    // 菜单的缩放
    const toggleMenu = useToggleMenuStore();
    const homeTags = homeTagsStore();
    // 根据选中的标签添加 homeTag
    const navigatorSelected = (index) => {
        const tag = homeTags.navigationMenu.filter(tag => tag.index === index)[0];
        homeTags.addTab(tag.title);
    }
</script>

<style scoped lang="scss">
    .el-menu-vertical-demo:not(.el-menu--collapse) {

    }
    // 去掉白框
    .el-menu{
        border-right: none !important;
    }
    .el-menu-item{
        transition: border-left .1s ease !important;
    }
    .el-menu-item:hover{
        border-left: 3px solid rgb(255, 208, 75) !important;
    }
</style>