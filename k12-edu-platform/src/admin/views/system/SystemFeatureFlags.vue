<template>
  <div v-loading="loading">
    <el-table :data="flags" border stripe size="small">
      <el-table-column prop="label" label="开关" min-width="160" />
      <el-table-column prop="description" label="说明" min-width="240" show-overflow-tooltip />
      <el-table-column label="范围" width="100">
        <template #default="{ row }">
          <el-tag :type="row.scope === 'runtime' ? 'success' : 'info'" size="small">
            {{ row.scope === 'runtime' ? '运行时' : '构建期' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch
            v-model="row.enabled"
            :disabled="!canEdit || row.scope === 'buildTime'"
            @change="(v) => onToggle(row, Boolean(v))"
          />
        </template>
      </el-table-column>
    </el-table>
    <p v-if="!canEdit" class="hint">只读：需要 admin:system:config_edit 才能修改</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getFeatureFlags,
  updateSystemConfig,
  type FeatureFlagItem,
} from '@/admin/api/system'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

const adminStore = useAdminAuthStore()
const canEdit = adminStore.hasPermission('admin:system:config_edit')
const loading = ref(false)
const flags = ref<FeatureFlagItem[]>([])

async function load() {
  loading.value = true
  try {
    const data = await getFeatureFlags()
    flags.value = data.flags ?? []
  } finally {
    loading.value = false
  }
}

async function onToggle(row: FeatureFlagItem, enabled: boolean) {
  if (!canEdit || row.scope === 'buildTime') return
  try {
    await updateSystemConfig('feature', { [row.key]: enabled })
    ElMessage.success(`${row.label} 已${enabled ? '开启' : '关闭'}`)
  } catch {
    row.enabled = !enabled
  }
}

onMounted(load)
</script>

<style scoped>
.hint {
  margin-top: 12px;
  color: #909399;
  font-size: 13px;
}
</style>

