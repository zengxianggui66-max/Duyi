<template>
  <el-breadcrumb class="admin-breadcrumb" separator="/">
    <el-breadcrumb-item :to="{ path: '/admin/analytics/overview' }">管理端</el-breadcrumb-item>
    <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const currentTitle = computed(() => route.meta.title as string | undefined)
</script>

<style scoped>
.admin-breadcrumb {
  margin-bottom: 16px;
}
</style>
