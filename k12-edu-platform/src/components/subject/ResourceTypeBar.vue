<template>
  <div class="resource-type-bar">
    <FilterOptionRow
      :key="scopeKey || 'default'"
      label="类型："
      :options="allOptions"
      :model-value="activeResourceType"
      @update:model-value="onSelect"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import FilterOptionRow from '../shared/FilterOptionRow.vue'
import type { FilterRowOption } from '@/composables/useFilterRowOverflow'
import {
  resolveTabTypeCount,
  sortResourceTypeNames,
} from '@/utils/sortResourceTypes'
import { syncPrepTabCountFallbackKey } from '@/constants/syncPrepColumnFilters'

type Item = string | { name: string; icon?: string }

function getName(item: Item): string {
  return typeof item === 'string' ? item : item.name
}

function getIcon(item: Item): string {
  return typeof item === 'string' ? '' : (item.icon || '')
}

const props = withDefaults(
  defineProps<{
    resourceTypes: Item[]
    teacherBookTypes: Item[]
    activeResourceType: string
    /** 当前目录上下文角标（实时） */
    typeCounts?: Record<string, number>
    /** 课本级数量（仅用于 Tab 排序） */
    sortCounts?: Record<string, number>
    showCounts?: boolean
    sortReady?: boolean
    statsReady?: boolean
    statsLoading?: boolean
    /** 目录/筛选 scope 变化时用于重置「更多」展开态 */
    scopeKey?: string
  }>(),
  {
    typeCounts: () => ({}),
    sortCounts: () => ({}),
    showCounts: false,
    sortReady: false,
    statsReady: false,
    statsLoading: false,
    scopeKey: '',
  },
)

const emit = defineEmits<{
  'select-resource-type': [type: string]
}>()

function tabCount(name: string): number {
  return (
    resolveTabTypeCount(
      props.typeCounts,
      name,
      syncPrepTabCountFallbackKey,
    ) ?? 0
  )
}

function shouldShowTab(name: string): boolean {
  return !!name
}

function resolveBadge(name: string, count: number): number | undefined {
  if (!props.showCounts) return undefined
  const canShow =
    props.statsReady || (props.statsLoading && Object.keys(props.typeCounts).length > 0)
  if (!canShow) return undefined
  return count
}

function buildOptions(items: Item[]): FilterRowOption[] {
  const names = items.map(getName)
  const orderSource =
    props.sortReady && Object.keys(props.sortCounts).length > 0
      ? props.sortCounts
      : props.typeCounts
  const sortEnabled =
    props.sortReady || Object.keys(props.typeCounts).length > 0
  const ordered = sortResourceTypeNames(names, orderSource, sortEnabled)
  const byName = new Map<string, Item>()
  for (const item of items) {
    byName.set(getName(item), item)
  }
  return ordered
    .filter((name) => shouldShowTab(name))
    .map((name) => {
      const count = tabCount(name)
      return {
        value: name,
        label: name,
        icon: getIcon(byName.get(name) ?? name) || undefined,
        badge: resolveBadge(name, count),
      }
    })
}

/** 主栏 + 长尾类型合并为一条，由 FilterOptionRow 单行溢出到「更多」 */
const allOptions = computed(() => {
  const seen = new Set<string>()
  const merged: Item[] = []
  for (const item of [...props.resourceTypes, ...props.teacherBookTypes]) {
    const name = getName(item)
    if (seen.has(name)) continue
    seen.add(name)
    merged.push(item)
  }
  return buildOptions(merged)
})

function onSelect(type: string) {
  emit('select-resource-type', type)
}
</script>

<style scoped>
.resource-type-bar {
  padding: 4px 20px 8px;
  background: #fff;
  border-bottom: 1px solid #f0eeeb;
  flex-shrink: 0;
  overflow: visible;
}
</style>
