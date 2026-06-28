<template>
  <div class="module-top-tabs" :class="`theme-${theme}`">
    <span class="tab-item title">{{ title }}</span>
    <span
      v-for="tab in tabs"
      :key="tab.key"
      class="tab-item"
      :class="{ active: modelValue === tab.key }"
      @click="$emit('update:modelValue', tab.key)"
    >
      {{ tab.label }}
    </span>
  </div>
</template>

<script setup lang="ts">
import type { ModuleTab, ModuleTheme } from './types'

defineProps<{
  title: string
  tabs: ModuleTab[]
  modelValue: string
  theme?: ModuleTheme
}>()

defineEmits<{
  'update:modelValue': [key: string]
}>()
</script>

<style scoped>
.module-top-tabs {
  --tab-primary: #ff6b00;
  display: flex;
  align-items: center;
  gap: 28px;
  padding: 16px 24px 0;
  border-bottom: 1px solid #f0f0f0;
  flex-wrap: wrap;
}

.theme-blue {
  --tab-primary: #1890ff;
}

.theme-green {
  --tab-primary: #52c41a;
}

.tab-item {
  font-size: 15px;
  color: #666;
  cursor: pointer;
  padding-bottom: 12px;
  position: relative;
  white-space: nowrap;
  transition: color 0.2s;
  user-select: none;
}

.tab-item:hover {
  color: var(--tab-primary);
}

.tab-item.title {
  font-size: 18px;
  font-weight: 700;
  color: #222;
  cursor: default;
}

.tab-item.title:hover {
  color: #222;
}

.tab-item.active {
  color: var(--tab-primary);
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 3px;
  background: var(--tab-primary);
  border-radius: 2px 2px 0 0;
}
</style>
