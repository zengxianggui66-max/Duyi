<template>
  <div v-loading="loading">
    <div class="toolbar">
      <el-button @click="load">刷新</el-button>
    </div>
    <el-descriptions v-if="status" :column="1" border>
      <el-descriptions-item label="存储提供商">{{ status.provider }}</el-descriptions-item>
      <el-descriptions-item label="已配置">{{ status.configured ? '是' : '否' }}</el-descriptions-item>
      <el-descriptions-item label="可达">
        <el-tag :type="status.reachable ? 'success' : 'danger'" size="small">
          {{ status.reachable ? '正常' : '不可达' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="Bucket">{{ status.bucket || '-' }}</el-descriptions-item>
      <el-descriptions-item label="探测耗时">{{ status.latencyMs ?? '-' }} ms</el-descriptions-item>
      <el-descriptions-item label="本地目录">{{ status.localFallback?.path || '-' }}</el-descriptions-item>
      <el-descriptions-item label="本地可写">
        {{ status.localFallback?.writable ? '是' : '否' }}
      </el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getStorageStatus, type StorageStatus } from '@/admin/api/system'

const loading = ref(false)
const status = ref<StorageStatus | null>(null)

async function load() {
  loading.value = true
  try {
    status.value = await getStorageStatus()
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
</style>
