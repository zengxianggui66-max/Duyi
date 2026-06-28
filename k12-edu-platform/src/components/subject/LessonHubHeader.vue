<template>
  <div v-if="lessonName" class="lesson-hub-header">
    <div class="lesson-title-row">
      <h2 class="lesson-title">{{ lessonName }}</h2>
      <span v-if="parentUnit" class="lesson-unit">{{ parentUnit }}</span>
    </div>
    <p class="lesson-meta">{{ metaLine }}</p>

    <div class="lesson-actions">
      <el-button type="primary" size="small" @click="$emit('upload')">
        上传本课资源
      </el-button>
    </div>

    <div v-if="suiteHint" class="lesson-suite-card">
      <span class="suite-icon">📦</span>
      <div class="suite-body">
        <div class="suite-title">{{ suiteHint.title }}</div>
        <div class="suite-sub">{{ suiteHint.sub }}</div>
      </div>
      <span class="suite-count">共 {{ suiteHint.fileCount }} 份</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  lessonName: string
  parentUnit: string
  stageName?: string
  subjectName?: string
  gradeLevelName?: string
  versionName?: string
  column?: string
  suiteHint?: { title: string; sub: string; fileCount: number } | null
}>()

const metaLine = computed(() =>
  [
    props.stageName,
    props.subjectName,
    props.gradeLevelName,
    props.versionName,
    props.column,
  ]
    .filter(Boolean)
    .join(' · '),
)

defineEmits<{
  upload: []
}>()
</script>

<style scoped>
.lesson-hub-header {
  margin-bottom: 16px;
  padding: 16px 18px;
  background: linear-gradient(135deg, #f0f7ff 0%, #fff 100%);
  border: 1px solid #e1ecfa;
  border-radius: 12px;
}
.lesson-title-row {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 6px;
}
.lesson-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1f2f3a;
}
.lesson-unit {
  font-size: 13px;
  color: #606266;
}
.lesson-meta {
  margin: 0 0 10px;
  font-size: 13px;
  color: #909399;
}
.lesson-actions {
  margin-bottom: 12px;
}
.lesson-suite-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: #fff;
  border-radius: 10px;
  border: 1px dashed #b3d4ff;
}
.suite-icon {
  font-size: 28px;
  flex-shrink: 0;
}
.suite-body {
  flex: 1;
  min-width: 0;
}
.suite-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.suite-sub {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.suite-count {
  font-size: 12px;
  color: #409eff;
  white-space: nowrap;
}
</style>
