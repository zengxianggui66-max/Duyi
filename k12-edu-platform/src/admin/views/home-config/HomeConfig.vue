<template>
  <AdminPageShell title="频道中心" desc="首页轮播、快捷入口、热门词与频道运营（专区 Tab / 功能入口 / 特色频道）">
    <el-tabs :model-value="activeTab" class="home-config-tabs" @tab-change="onTabChange">
      <el-tab-pane label="轮播 Banner" name="banners" />
      <el-tab-pane label="快捷入口" name="quick-entries" />
      <el-tab-pane label="热门词" name="hot-words" />
      <el-tab-pane label="首页频道 · 专区 Tab" name="panel-tabs" />
      <el-tab-pane label="置顶推荐" name="panel-featured" />
      <el-tab-pane label="最新内容" name="latest-columns" />
      <el-tab-pane label="功能入口频道" name="func-channels" />
      <el-tab-pane label="专题频道" name="feature-channels" />
      <el-tab-pane label="聚合预览" name="preview" />
    </el-tabs>
    <router-view />
  </AdminPageShell>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminPageShell from '@/admin/components/AdminPageShell.vue'

const route = useRoute()
const router = useRouter()

const activeTab = computed(() => {
  const seg = route.path.split('/').pop()
  const known = ['banners', 'quick-entries', 'hot-words', 'panel-tabs', 'panel-featured', 'latest-columns', 'func-channels', 'feature-channels', 'preview']
  if (seg && known.includes(seg)) return seg
  return 'banners'
})

function onTabChange(name: string | number) {
  router.push(`/admin/home-config/${name}`)
}
</script>

<style scoped>
.home-config-tabs {
  margin-bottom: 16px;
}
</style>
