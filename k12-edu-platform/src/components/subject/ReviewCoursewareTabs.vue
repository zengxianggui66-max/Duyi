<template>
  <div v-if="visible" class="review-courseware-tabs" role="tablist" aria-label="复习课件分类">
    <button
      type="button"
      role="tab"
      :class="['scope-tab', { active: modelValue === 'unit' }]"
      :aria-selected="modelValue === 'unit'"
      @click="$emit('update:modelValue', 'unit')"
    >
      单元复习
    </button>
    <button
      type="button"
      role="tab"
      :class="['scope-tab', { active: modelValue === 'special' }]"
      :aria-selected="modelValue === 'special'"
      @click="$emit('update:modelValue', 'special')"
    >
      专项复习
    </button>
    <p class="scope-hint">
      {{ modelValue === 'unit' ? '按教材单元浏览期末串讲课件' : '按全册知识点浏览专项复习课件' }}
    </p>
  </div>
</template>

<script setup lang="ts">
import type { ReviewScope } from '@/config/finalReviewConfig'

defineProps<{
  visible: boolean
  modelValue: ReviewScope
}>()

defineEmits<{
  'update:modelValue': [value: ReviewScope]
}>()
</script>

<style scoped>
.review-courseware-tabs {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px 12px;
  padding: 10px 16px;
  border-bottom: 1px solid #eef2f6;
  background: #fafbfc;
}

.scope-tab {
  padding: 5px 16px;
  border-radius: 16px;
  border: 1px solid #dcdfe6;
  background: #fff;
  color: #606266;
  font-size: 13px;
  cursor: pointer;
  transition: 0.2s;
}

.scope-tab:hover {
  color: #409eff;
  border-color: #c6e2ff;
}

.scope-tab.active {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
}

.scope-hint {
  margin: 0;
  font-size: 12px;
  color: #909399;
}
</style>
