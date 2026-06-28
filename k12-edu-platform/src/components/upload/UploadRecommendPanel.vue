<template>
  <section class="upload-recommend-panel">
    <div class="panel-head">
      <span class="panel-title">根据当前位置推荐</span>
      <span v-if="autoAppliedHint" class="auto-hint">{{ autoAppliedHint }}</span>
    </div>
    <p v-if="contextLine" class="context-line">{{ contextLine }}</p>

    <div v-if="primary" class="primary-row">
      <el-button
        type="primary"
        :plain="activeId !== primary.id"
        class="primary-btn"
        @click="emit('apply', primary.id)"
      >
        <span v-if="primary.matchBrowse" class="star">★</span>
        {{ primary.label }}
      </el-button>
      <span v-if="primary.matchBrowse && activeId === primary.id" class="applied-tag">已自动应用</span>
    </div>

    <div v-if="others.length" class="chip-row">
      <span class="chip-label">本栏目常用</span>
      <el-button
        v-for="rec in others"
        :key="rec.id"
        size="small"
        :type="activeId === rec.id ? 'primary' : 'default'"
        round
        @click="emit('apply', rec.id)"
      >
        {{ rec.teachingType }}
      </el-button>
    </div>

    <el-collapse v-if="legacyTemplates.length" class="more-collapse">
      <el-collapse-item title="更多模板" name="more">
        <el-button
          v-for="tpl in legacyTemplates"
          :key="tpl.id"
          size="small"
          class="legacy-btn"
          @click="emit('legacy', tpl.id)"
        >
          {{ tpl.label }}
        </el-button>
      </el-collapse-item>
    </el-collapse>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { UploadRecommendation } from '@/constants/uploadRecommend'
import { LEGACY_UPLOAD_TEMPLATES } from '@/constants/uploadRecommend'

const props = defineProps<{
  recommendations: UploadRecommendation[]
  activeId: string
  contextLine: string
  autoAppliedHint?: string
  showLegacy?: boolean
  stageKey?: string
}>()

const emit = defineEmits<{
  apply: [id: string]
  legacy: [id: string]
}>()

const primary = computed(() => props.recommendations[0] ?? null)
const others = computed(() => props.recommendations.slice(1, 6))

const legacyTemplates = computed(() => {
  if (!props.showLegacy) return []
  const stage = props.stageKey || ''
  if (stage === 'art' || stage === 'dance') {
    return LEGACY_UPLOAD_TEMPLATES.filter((t) => t.group === '艺术' || t.group === '通用')
  }
  return LEGACY_UPLOAD_TEMPLATES.filter((t) => t.group === '通用')
})
</script>

<style scoped>
.upload-recommend-panel {
  margin-bottom: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #f0f7ff 0%, #f8fbff 100%);
  border: 1px solid #d9ecff;
  border-radius: 8px;
}
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}
.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.auto-hint {
  font-size: 12px;
  color: #409eff;
}
.context-line {
  margin: 8px 0 12px;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}
.primary-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.primary-btn .star {
  margin-right: 4px;
}
.applied-tag {
  font-size: 12px;
  color: #67c23a;
}
.chip-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
.chip-label {
  font-size: 12px;
  color: #909399;
  margin-right: 4px;
}
.more-collapse {
  margin-top: 12px;
  border: none;
  background: transparent;
}
.more-collapse :deep(.el-collapse-item__header) {
  background: transparent;
  font-size: 13px;
  color: #606266;
  height: 36px;
  border: none;
}
.more-collapse :deep(.el-collapse-item__wrap) {
  border: none;
  background: transparent;
}
.legacy-btn {
  margin: 0 8px 8px 0;
}
</style>
