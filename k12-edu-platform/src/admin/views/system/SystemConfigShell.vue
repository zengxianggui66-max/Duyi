<template>
  <AdminPageShell title="系统管理" desc="操作审计、配置与健康状态">
    <el-tabs :model-value="activeTab" class="system-config-tabs" @tab-change="onTabChange">
      <el-tab-pane v-if="canLogView" label="操作日志" name="logs" />
      <el-tab-pane v-if="canLogView" label="登录日志" name="login-logs" />
      <el-tab-pane v-if="canConfigView" label="上传配置" name="upload-config" />
      <el-tab-pane v-if="canConfigView" label="预览配置" name="preview-config" />
      <el-tab-pane v-if="canConfigView" label="存储状态" name="storage" />
      <el-tab-pane v-if="canConfigEdit" label="功能开关" name="feature-flags" />
    </el-tabs>
    <router-view />
  </AdminPageShell>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminPageShell from '@/admin/components/AdminPageShell.vue'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminAuthStore()

const canLogView = computed(
  () =>
    adminStore.hasPermission('admin:system:log_view') ||
    adminStore.hasPermission('admin:system:config_edit'),
)
const canConfigView = computed(
  () =>
    adminStore.hasPermission('admin:system:config_view') ||
    adminStore.hasPermission('admin:system:config_edit'),
)
const canConfigEdit = computed(() => adminStore.hasPermission('admin:system:config_edit'))

const activeTab = computed(() => {
  const seg = route.path.split('/').pop() || 'logs'
  const known = ['logs', 'login-logs', 'upload-config', 'preview-config', 'storage', 'feature-flags']
  return known.includes(seg) ? seg : 'logs'
})

function onTabChange(name: string | number) {
  router.push(`/admin/system/${name}`)
}
</script>

<style scoped>
.system-config-tabs {
  margin-bottom: 16px;
}
</style>
