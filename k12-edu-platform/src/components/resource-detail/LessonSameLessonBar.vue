<template>
  <div class="lesson-same-bar">
    <div class="bar-context">
      <router-link v-if="subjectBackLink" :to="subjectBackLink" class="back-link">
        ← 返回本课
      </router-link>
      <span v-if="lessonName" class="lesson-label">{{ lessonName }}</span>
      <span v-if="parentUnit" class="unit-label">{{ parentUnit }}</span>
    </div>

    <div class="type-switcher">
      <button
        v-for="tab in typeTabs"
        :key="tab.type"
        type="button"
        :class="['type-btn', { active: tab.active }]"
        @click="$emit('select-type', tab.type)"
      >
        {{ tab.type }}
        <span v-if="tab.count > 0" class="type-count">{{ tab.count }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { RouteLocationRaw } from 'vue-router'

const props = defineProps<{
  lessonName?: string
  parentUnit?: string
  activeType?: string
  subjectBackLink?: RouteLocationRaw | null
  typeCounts?: Record<string, number>
}>()

defineEmits<{
  'select-type': [type: string]
}>()

const DEFAULT_TYPES = ['课件', '教案', '练习', '学案', '试卷']

const typeTabs = computed(() => {
  const counts = props.typeCounts || {}
  const types = new Set([...DEFAULT_TYPES, ...Object.keys(counts)])
  return Array.from(types).map((type) => ({
    type,
    count: counts[type] || 0,
    active: props.activeType === type,
  }))
})
</script>

<style scoped>
.lesson-same-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f0f7ff 0%, #fff 100%);
  border: 1px solid #e1ecfa;
  border-radius: 10px;
}
.bar-context {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 13px;
}
.back-link {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
}
.back-link:hover {
  text-decoration: underline;
}
.lesson-label {
  font-weight: 600;
  color: #303133;
}
.unit-label {
  color: #909399;
}
.type-switcher {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.type-btn {
  padding: 5px 12px;
  border: 1px solid #e4e9f0;
  border-radius: 16px;
  background: #fff;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}
.type-btn:hover {
  border-color: #409eff;
  color: #409eff;
}
.type-btn.active {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
}
.type-count {
  margin-left: 4px;
  opacity: 0.85;
}
</style>
