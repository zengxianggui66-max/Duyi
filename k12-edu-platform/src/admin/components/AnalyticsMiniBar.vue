<template>
  <div class="mini-bar">
    <div v-for="item in items" :key="item.label" class="mini-bar__row">
      <span class="mini-bar__label" :title="item.label">{{ item.label }}</span>
      <div class="mini-bar__track">
        <div class="mini-bar__fill" :style="{ width: barWidth(item.value) }" />
      </div>
      <span class="mini-bar__value">{{ formatValue(item.value) }}</span>
    </div>
    <p v-if="!items.length" class="mini-bar__empty">{{ emptyText }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

export interface MiniBarItem {
  label: string
  value: number
}

const props = withDefaults(
  defineProps<{
    items: MiniBarItem[]
    emptyText?: string
  }>(),
  { emptyText: '暂无数据' },
)

const maxValue = computed(() => {
  if (!props.items.length) return 1
  return Math.max(...props.items.map((i) => i.value), 1)
})

function barWidth(value: number) {
  const pct = Math.round((value / maxValue.value) * 100)
  return `${Math.max(pct, value > 0 ? 4 : 0)}%`
}

function formatValue(value: number) {
  if (value >= 10000) return `${(value / 10000).toFixed(1)}万`
  return String(value)
}
</script>

<style scoped>
.mini-bar__row {
  display: grid;
  grid-template-columns: 72px 1fr 48px;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.mini-bar__label {
  font-size: 12px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.mini-bar__track {
  height: 10px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
}
.mini-bar__fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #79bbff);
  border-radius: 4px;
  transition: width 0.3s ease;
}
.mini-bar__value {
  font-size: 12px;
  color: #303133;
  text-align: right;
}
.mini-bar__empty {
  margin: 0;
  font-size: 13px;
  color: #909399;
}
</style>
