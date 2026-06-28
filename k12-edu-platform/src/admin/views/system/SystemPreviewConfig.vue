<template>
  <div v-loading="loading">
    <el-form label-width="180px" style="max-width: 640px">
      <el-form-item label="预览总开关">
        <el-switch v-model="form.enabled" />
      </el-form-item>
      <el-form-item label="LibreOffice 开关">
        <el-switch v-model="form.libreofficeEnabled" />
      </el-form-item>
      <el-form-item label="LibreOffice 路径">
        <el-input v-model="form.libreofficePath" placeholder="soffice.exe 完整路径" />
      </el-form-item>
      <el-form-item label="POI 降级">
        <el-switch v-model="form.poiFallbackEnabled" />
      </el-form-item>
      <el-form-item label="异步预览">
        <el-switch v-model="form.asyncEnabled" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
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
const loading = ref(false)
const saving = ref(false)
const canEdit = adminStore.hasPermission('admin:system:config_edit')

const form = reactive({
  enabled: true,
  libreofficeEnabled: true,
  libreofficePath: '',
  poiFallbackEnabled: true,
  asyncEnabled: true,
})

async function load() {
  loading.value = true
  try {
    const data = await getSystemConfig('preview')
    form.enabled = Boolean(data.values.enabled)
    form.libreofficeEnabled = Boolean(data.values.libreofficeEnabled)
    form.libreofficePath = String(data.values.libreofficePath ?? '')
    form.poiFallbackEnabled = Boolean(data.values.poiFallbackEnabled)
    form.asyncEnabled = Boolean(data.values.asyncEnabled)
  } finally {
    loading.value = false
  }
}

async function save() {
  if (!canEdit) {
    ElMessage.warning('无编辑权限')
    return
  }
  saving.value = true
  try {
    await updateSystemConfig('preview', { ...form })
    ElMessage.success('预览配置已保存')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
