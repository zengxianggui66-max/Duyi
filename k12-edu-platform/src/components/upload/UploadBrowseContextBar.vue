<template>
  <el-alert
    v-if="fromBrowse"
    type="info"
    :closable="false"
    show-icon
    class="browse-context-bar"
  >
    <template #title>
      <span class="bar-title">同步备课上传</span>
      <el-tag size="small" type="primary" effect="plain">{{ module }}</el-tag>
    </template>
    <p class="bar-summary">{{ summary }}</p>
    <p v-if="lessonName" class="bar-hint">资源将关联到课文「{{ lessonName }}」，上传后可在该课文资源列表中查看。</p>
  </el-alert>
</template>

<script setup lang="ts">
defineProps<{
  fromBrowse: boolean
  module: string
  summary: string
  lessonName?: string
}>()
</script>

<style scoped>
.browse-context-bar {
  margin-bottom: 20px;
}
.bar-title {
  font-weight: 600;
  margin-right: 8px;
}
.bar-summary {
  margin: 6px 0 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}
.bar-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: #909399;
}
</style>
