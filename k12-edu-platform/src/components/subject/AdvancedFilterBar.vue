<template>
  <div class="advanced-filter-bar">
    <div class="advanced-filter-bar__main">
      <div class="advanced-filter-bar__modes">
        <button
          type="button"
          :class="['filter-option-btn', { 'is-active': resourceMode === 'single' }]"
          @click="$emit('update:resourceMode', 'single')"
        >找单份</button>
        <button
          type="button"
          :class="['filter-option-btn', { 'is-active': resourceMode === 'suite' }]"
          @click="$emit('update:resourceMode', 'suite')"
        >找成套</button>
      </div>

      <template v-if="resourceMode === 'single'">
        <span class="advanced-filter-bar__divider" />
        <FilterOptionRow
          :options="sortOptions"
          :model-value="sortType"
          compact
          @update:model-value="onSortChange"
        />
      </template>
    </div>

    <div class="advanced-filter-bar__right">
      <button
        type="button"
        :class="['batch-toggle-btn', { 'is-active': batchMode }]"
        @click="$emit('update:batchMode', !batchMode)"
      >
        <svg class="batch-icon" viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
          <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14z"/>
        </svg>
        <span v-if="!batchMode">批量下载</span>
        <span v-else>退出批量</span>
      </button>
      <span class="result-count">共 {{ totalCount }} 条结果</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import FilterOptionRow from '../shared/FilterOptionRow.vue'
import type { FilterRowOption } from '@/composables/useFilterRowOverflow'

defineProps<{
  resourceMode: 'single' | 'suite'
  sortType: 'comprehensive' | 'latest' | 'downloads'
  totalCount: number
  batchMode: boolean
}>()

const emit = defineEmits<{
  'update:resourceMode': [value: 'single' | 'suite']
  'update:sortType': [value: 'comprehensive' | 'latest' | 'downloads']
  'update:batchMode': [value: boolean]
}>()

const sortOptions: FilterRowOption[] = [
  { value: 'comprehensive', label: '综合' },
  { value: 'latest', label: '最新' },
  { value: 'downloads', label: '下载量' },
]

function onSortChange(value: string) {
  emit('update:sortType', value as 'comprehensive' | 'latest' | 'downloads')
}
</script>

<style scoped>
.advanced-filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 20px;
  background: #fff;
  border-bottom: 1px solid #f0eeeb;
  min-height: 52px;
}

.advanced-filter-bar__main {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  overflow: hidden;
}

.advanced-filter-bar__modes {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.advanced-filter-bar__divider {
  width: 1px;
  height: 20px;
  background: #e8ecf1;
  flex-shrink: 0;
}

.advanced-filter-bar__main :deep(.filter-option-row) {
  flex: 1;
  min-width: 0;
  padding: 0;
}

.advanced-filter-bar__right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

/* 批量下载按钮 */
.batch-toggle-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  border: 1px solid var(--border-color, #E8ECF4);
  border-radius: var(--radius-round, 999px);
  background: var(--bg-card, #fff);
  font-size: 13px;
  color: var(--text-regular, #4A4A68);
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
  white-space: nowrap;
}

.batch-toggle-btn:hover {
  border-color: var(--color-primary, #4361EE);
  color: var(--color-primary, #4361EE);
  background: var(--color-primary-bg, #EBF0FF);
}

.batch-toggle-btn.is-active {
  background: var(--color-primary, #4361EE);
  color: #fff;
  border-color: var(--color-primary, #4361EE);
}

.batch-icon {
  display: block;
}

.result-count {
  flex-shrink: 0;
  font-size: 13px;
  color: #94a3b8;
  white-space: nowrap;
}
</style>
