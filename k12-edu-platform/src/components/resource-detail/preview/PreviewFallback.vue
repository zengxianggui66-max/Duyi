<template>
  <div class="preview-unsupported">
    <el-icon size="48"><Document /></el-icon>
    <p>{{ message || '该文件类型暂不支持在线预览' }}</p>
    <p class="tip">点击"下载"按钮下载后查看</p>
  </div>
</template>

<script setup lang="ts">
import { Document } from '@element-plus/icons-vue'

defineProps<{
  message?: string
}>()
</script>

<style scoped>
.preview-unsupported {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}
.tip {
  font-size: 13px;
  margin-top: 8px;
}
</style>
