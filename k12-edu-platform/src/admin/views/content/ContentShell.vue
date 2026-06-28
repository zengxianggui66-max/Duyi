<template>
  <AdminPageShell title="内容中心" desc="专题 / 传统文化 / 竞赛 / 资讯 / 主题班会 — 专区内容与发布运营">
    <el-tabs :model-value="activeTab" class="content-shell-tabs" @tab-change="onTabChange">
      <el-tab-pane label="专题资料" name="topic" />
      <el-tab-pane label="传统文化" name="culture" />
      <el-tab-pane label="竞赛专区" name="competition" />
      <el-tab-pane label="教育资讯" name="news" />
      <el-tab-pane label="主题班会" name="theme-class-meeting" />
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

const TAB_ROUTES: Record<string, string> = {
  topic: '/admin/content/topic',
  culture: '/admin/content/culture',
  competition: '/admin/content/competition',
  news: '/admin/content/news',
  'theme-class-meeting': '/admin/content/theme-class-meeting',
}

const activeTab = computed(() => {
  const seg = route.path.split('/').pop()
  if (seg && seg in TAB_ROUTES) return seg
  return 'topic'
})

function onTabChange(name: string | number) {
  const path = TAB_ROUTES[String(name)]
  if (path) router.push(path)
}
</script>

<style scoped>
.content-shell-tabs {
  margin-bottom: 16px;
}
</style>
