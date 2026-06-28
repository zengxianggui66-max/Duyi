<template>
  <div :class="['reading-writing-filter-bar', barClass, { 'is-nowrap': nowrap }]">
    <el-select
      v-for="filter in filters"
      :key="filter.key"
      :model-value="filter.modelValue"
      :placeholder="filter.placeholder"
      clearable
      size="default"
      :class="['rw-filter-item', filter.className]"
      @update:model-value="(value) => onFilterValueUpdate(filter, value)"
      @change="onFilterChange"
    >
      <el-option
        v-for="opt in filter.options"
        :key="opt.value"
        :label="formatOptionLabel(opt)"
        :value="opt.value"
      />
    </el-select>

    <el-input
      v-if="keyword"
      :model-value="keyword?.modelValue || ''"
      :placeholder="keyword?.placeholder || ''"
      clearable
      size="default"
      class="rw-filter-search"
      @update:model-value="onKeywordValueUpdate"
      @keyup.enter="onKeywordEnter"
    />

    <el-button
      v-if="searchAction"
      type="primary"
      size="default"
      @click="searchAction.onClick"
    >
      {{ searchAction.text }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
type RawFilterValue = string | number | boolean | null | undefined

interface FilterOption {
  value: string
  label: string
  cnt?: number
}

interface FilterItem {
  key: string
  modelValue: string
  placeholder: string
  options: FilterOption[]
  className?: string
  onUpdate: (value: string) => void
}

interface KeywordConfig {
  modelValue: string
  placeholder: string
  onUpdate: (value: string) => void
  onEnter: () => void
}

interface SearchAction {
  text: string
  onClick: () => void
}

const props = withDefaults(defineProps<{
  filters: FilterItem[]
  onFilterChange?: () => void
  keyword?: KeywordConfig
  searchAction?: SearchAction
  nowrap?: boolean
  barClass?: string
}>(), {
  onFilterChange: undefined,
  keyword: undefined,
  searchAction: undefined,
  nowrap: true,
  barClass: '',
})

function normalizeValue(value: RawFilterValue): string {
  if (value == null) return ''
  return String(value)
}

function formatOptionLabel(opt: FilterOption): string {
  if (typeof opt.cnt === 'number' && opt.cnt > 0) {
    return `${opt.label} (${opt.cnt})`
  }
  return opt.label
}

function onFilterValueUpdate(filter: FilterItem, value: RawFilterValue) {
  filter.onUpdate(normalizeValue(value))
}

function onFilterChange() {
  props.onFilterChange?.()
}

function onKeywordValueUpdate(value: RawFilterValue) {
  props.keyword?.onUpdate(normalizeValue(value))
}

function onKeywordEnter() {
  props.keyword?.onEnter()
}
</script>

<style scoped>
.reading-writing-filter-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  border-bottom: 1px solid #EEF2F6;
}

.reading-writing-filter-bar.is-nowrap {
  flex-wrap: nowrap;
  overflow-x: auto;
}

.rw-filter-item {
  width: 180px;
  min-width: 180px;
}

.rw-filter-search {
  width: 240px;
  min-width: 220px;
}
</style>
