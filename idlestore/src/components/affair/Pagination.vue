<template>
    <div class="pagination-container">
        <el-pagination
                v-model:current-page="pageInfo.currentPage"
                small
                background
                :page-size="pageInfo.pageSize"
                :total="pageInfo.total"
                layout="total,->,prev, pager, next,jumper"
                @current-change="changeCurrentPage"
        />
    </div>

</template>

<script lang="ts" setup>
defineProps(["pageInfo", "changeCurrentPage"]);
</script>

<style lang="scss" scoped>
.pagination-container{
  margin-top: 15px;
}
</style>