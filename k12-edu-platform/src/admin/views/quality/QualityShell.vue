<template>
  <AdminPageShell title="质量治理" desc="敏感词库、预览失败、旧接口调用观测与审核数据分析">
    <el-tabs :model-value="activeTab" class="quality-shell-tabs" @tab-change="onTabChange">
      <el-tab-pane label="质量大盘" name="dashboard" />
      <el-tab-pane label="敏感词库" name="sensitive-words" />
      <el-tab-pane label="预览失败" name="preview-fails" />
      <el-tab-pane label="旧接口调用" name="legacy-api-usage" />
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
  const known = ['dashboard', 'sensitive-words', 'preview-fails', 'legacy-api-usage']
  if (seg && known.includes(seg)) return seg
  return 'dashboard'
})

function onTabChange(name: string | number) {
  router.push(`/admin/quality/${name}`)
}
</script>

<style scoped>
.quality-shell-tabs {
  margin-bottom: 16px;
}
</style>
