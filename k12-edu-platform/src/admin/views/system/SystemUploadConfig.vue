<template>
  <div v-loading="loading">
    <el-alert
      v-if="restartHint"
      type="warning"
      :closable="false"
      show-icon
      title="修改 maxSizeMb 需重启 resource 服务后生效"
      style="margin-bottom: 16px"
    />
    <el-form label-width="140px" style="max-width: 560px">
      <el-form-item label="单文件上限(MB)">
        <el-input-number v-model="form.maxSizeMb" :min="1" :max="2048" />
      </el-form-item>
      <el-form-item label="允许格式">
        <el-select v-model="form.allowedFormats" multiple filterable allow-create style="width: 100%">
          <el-option v-for="ext in form.allowedFormats" :key="ext" :label="ext" :value="ext" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button v-if="canEdit" type="primary" :loading="saving" @click="save">保存</el-button>
        <span v-else class="readonly-hint">只读：需要 config_edit 权限</span>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getSystemConfig, updateSystemConfig } from '@/admin/api/system'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

const adminStore = useAdminAuthStore()
const canEdit = adminStore.hasPermission('admin:system:config_edit')
const loading = ref(false)
const saving = ref(false)
const restartHint = ref(false)
const form = reactive<{ maxSizeMb: number; allowedFormats: string[] }>({
  maxSizeMb: 500,
  allowedFormats: [],
})

async function load() {
  loading.value = true
  try {
    const data = await getSystemConfig('upload')
    form.maxSizeMb = Number(data.values.maxSizeMb ?? 500)
    form.allowedFormats = Array.isArray(data.values.allowedFormats)
      ? (data.values.allowedFormats as string[])
      : []
    restartHint.value = data.fields?.some((f) => f.key === 'maxSizeMb' && f.requiresRestart) ?? false
  } finally {
    loading.value = false
  }
}

async function save() {
  if (!canEdit) return
  saving.value = true
  try {
    await updateSystemConfig('upload', {
      maxSizeMb: form.maxSizeMb,
      allowedFormats: form.allowedFormats,
    })
    ElMessage.success('上传配置已保存')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.readonly-hint {
  color: #909399;
  font-size: 13px;
}
</style>


