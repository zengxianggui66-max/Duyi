<template>
  <div
    class="filter-chip-group"
    :class="groupClass"
  >
    <span
      v-for="chip in chips"
      :key="chip.key"
      class="chip"
      :class="{ active: modelValue === chip.key }"
      @click="$emit('update:modelValue', chip.key)"
    >
      {{ chip.label }}
    </span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ModuleTheme } from './types'

export interface FilterChip {
  key: string
  label: string
}

const props = defineProps<{
  chips: FilterChip[]
  modelValue: string
  theme?: ModuleTheme
  columns?: 3 | 4
  variant?: 'stage' | 'grid'
}>()

defineEmits<{
  'update:modelValue': [key: string]
}>()

const groupClass = computed(() => {
  const list: string[] = [`theme-${props.theme || 'orange'}`]
  if (props.variant === 'stage') {
    list.push('variant-stage')
  } else {
    list.push('variant-grid')
    if (props.columns === 3) list.push('cols-3')
    if (props.columns === 4) list.push('cols-4')
  }
  return list
})
</script>

<style scoped>
.filter-chip-group {
  --chip-primary: #ff6b00;
  --chip-bg: rgba(255, 107, 0, 0.08);
}

.theme-blue {
  --chip-primary: #1890ff;
  --chip-bg: rgba(24, 144, 255, 0.08);
}

.theme-green {
  --chip-primary: #52c41a;
  --chip-bg: rgba(82, 196, 26, 0.08);
}

.theme-orange {
  --chip-primary: #ff6b00;
  --chip-bg: rgba(255, 107, 0, 0.08);
}

/* 学段：单行紧凑排列 */
.filter-chip-group.variant-stage {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 0 10px;
  padding: 2px 0 6px;
  margin-bottom: 2px;
}

.filter-chip-group.variant-stage .chip {
  flex-shrink: 0;
  padding: 4px 6px;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  border-radius: 4px;
  line-height: 1.2;
  position: relative;
  transition: color 0.2s, background 0.2s;
}

.filter-chip-group.variant-stage .chip:hover {
  color: var(--chip-primary);
  background: var(--chip-bg);
}

.filter-chip-group.variant-stage .chip.active {
  color: var(--chip-primary);
  font-weight: 600;
  background: var(--chip-bg);
}

.filter-chip-group.variant-stage .chip.active::after {
  content: '';
  position: absolute;
  left: 6px;
  right: 6px;
  bottom: 2px;
  height: 2px;
  border-radius: 1px;
  background: var(--chip-primary);
}

/* 学科：网格（侧栏内不溢出） */
.filter-chip-group.variant-grid {
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.filter-chip-group.variant-grid.cols-3 {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 6px 4px;
}

.filter-chip-group.variant-grid.cols-4 {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 6px 3px;
}

.filter-chip-group.variant-grid .chip {
  min-width: 0;
  max-width: 100%;
  padding: 4px 2px;
  font-size: 12px;
  line-height: 1.35;
  color: #555;
  text-align: center;
  border-radius: 4px;
  border: 1px solid transparent;
  white-space: normal;
  word-break: normal;
  overflow-wrap: break-word;
  transition: color 0.15s, background 0.15s, border-color 0.15s;
}

.filter-chip-group.variant-grid .chip:hover {
  color: var(--chip-primary);
  background: var(--chip-bg);
}

.filter-chip-group.variant-grid .chip.active {
  color: var(--chip-primary);
  font-weight: 600;
  background: var(--chip-bg);
  border-color: rgba(255, 107, 0, 0.2);
}

.theme-blue.filter-chip-group.variant-grid .chip.active {
  border-color: rgba(24, 144, 255, 0.25);
}

.theme-green.filter-chip-group.variant-grid .chip.active {
  border-color: rgba(82, 196, 26, 0.25);
}

.chip {
  cursor: pointer;
  user-select: none;
}

.filter-chip-group.variant-stage .chip {
  white-space: nowrap;
}
</style>
