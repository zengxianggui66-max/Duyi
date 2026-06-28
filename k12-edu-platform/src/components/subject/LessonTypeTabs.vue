<template>
  <div class="lesson-type-tabs">
    <button
      v-for="stat in typeStats"
      :key="stat.type"
      type="button"
      :class="['type-tab', { active: activeType === stat.type }]"
      @click="$emit('select', stat.type)"
    >
      {{ stat.type }}
      <span class="type-count">({{ stat.count }})</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import type { LessonTypeStat } from '@/composables/useLessonHub'

defineProps<{
  typeStats: LessonTypeStat[]
  activeType: string
}>()

defineEmits<{
  select: [type: string]
}>()
</script>

<style scoped>
.lesson-type-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eef2f6;
}
.type-tab {
  padding: 6px 14px;
  border: 1px solid #e4e9f0;
  border-radius: 20px;
  background: #f8fafd;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}
.type-tab:hover {
  border-color: #409eff;
  color: #409eff;
}
.type-tab.active {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
}
.type-count {
  margin-left: 2px;
  font-size: 12px;
  opacity: 0.9;
}
</style>
