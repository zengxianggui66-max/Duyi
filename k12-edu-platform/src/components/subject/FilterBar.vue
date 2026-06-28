<template>
  <div class="filter-bar-container">
    <FilterOptionRow
      label="学科："
      :options="subjectOptions"
      :model-value="currentSubject?.key ?? ''"
      @update:model-value="onSelectSubject"
    />
    <div v-if="topVisibleColumns.length" class="column-row">
      <FilterOptionRow
        class="column-row__options"
        label="栏目："
        :options="topColumnOptions"
        :model-value="activeColumn"
        @update:model-value="$emit('update:activeColumn', $event)"
      />
      <div v-if="groupedColumns.length" class="column-row__more">
        <button type="button" class="more-columns-btn" @click="openMoreColumns">
          更多栏目
          <span class="more-columns-btn__count">({{ totalMoreCount }})</span>
        </button>
      </div>
    </div>

    <transition name="drawer-fade">
      <div
        v-if="showMoreColumns"
        class="more-columns-drawer-mask"
        @click.self="closeMoreColumns"
      >
        <aside class="more-columns-drawer">
          <div class="more-columns-drawer__header">
            <h4>更多栏目</h4>
            <button type="button" class="more-columns-drawer__close" @click="closeMoreColumns">×</button>
          </div>

          <div class="more-columns-drawer__search">
            <input
              v-model.trim="searchKeyword"
              type="text"
              placeholder="搜索栏目，如：小升初、期末、作文"
            />
          </div>

          <div v-if="recentColumns.length && !normalizedKeyword" class="more-columns-drawer__section">
            <div class="more-columns-group__label">最近常用</div>
            <div class="more-columns-group__items">
              <button
                v-for="col in recentColumns"
                :key="'recent-' + col.name"
                type="button"
                :class="['more-columns-item', { 'is-active': activeColumn === col.name }]"
                @click="onSelectColumn(col.name)"
              >
                <span v-if="col.icon" class="more-columns-item__icon">{{ col.icon }}</span>
                <span class="more-columns-item__name">{{ col.name }}</span>
                <span v-if="showColumnCounts && (safeColumnCountMap[col.name] ?? 0) > 0" class="more-columns-item__count">
                  {{ safeColumnCountMap[col.name] }}
                </span>
              </button>
            </div>
          </div>

          <div v-if="hotColumns.length && !normalizedKeyword" class="more-columns-drawer__section">
            <div class="more-columns-group__label">热门栏目</div>
            <div class="more-columns-group__items">
              <button
                v-for="col in hotColumns"
                :key="'hot-' + col.name"
                type="button"
                :class="['more-columns-item', { 'is-active': activeColumn === col.name }]"
                @click="onSelectColumn(col.name)"
              >
                <span v-if="col.icon" class="more-columns-item__icon">{{ col.icon }}</span>
                <span class="more-columns-item__name">{{ col.name }}</span>
                <span v-if="showColumnCounts && (safeColumnCountMap[col.name] ?? 0) > 0" class="more-columns-item__count">
                  {{ safeColumnCountMap[col.name] }}
                </span>
              </button>
            </div>
          </div>

          <div class="more-columns-panel__groups">
            <div
              v-for="group in filteredGroupedColumns"
              :key="group.key"
              class="more-columns-group"
            >
              <div class="more-columns-group__label">
                <span>{{ group.label }}</span>
                <span
                  v-if="showGroupCounts && (safeGroupCountMap[group.key] ?? 0) > 0"
                  class="more-columns-group__count"
                >
                  {{ safeGroupCountMap[group.key] }}
                </span>
              </div>
              <div class="more-columns-group__items">
                <button
                  v-for="col in group.columns"
                  :key="col.name"
                  type="button"
                  :class="['more-columns-item', { 'is-active': activeColumn === col.name }]"
                  @click="onSelectColumn(col.name)"
                >
                  <span v-if="col.icon" class="more-columns-item__icon">{{ col.icon }}</span>
                  <span class="more-columns-item__name">{{ col.name }}</span>
                  <span v-if="showColumnCounts && (safeColumnCountMap[col.name] ?? 0) > 0" class="more-columns-item__count">
                    {{ safeColumnCountMap[col.name] }}
                  </span>
                </button>
              </div>
            </div>
            <div v-if="!filteredGroupedColumns.length" class="more-columns-drawer__empty">
              未找到匹配栏目
            </div>
          </div>
        </aside>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import FilterOptionRow from '../shared/FilterOptionRow.vue'
import { type SubjectItem } from '../../config/subjectConfig'
import type { ColumnGroupItem, ColumnItem } from '../../composables/useColumnFilter'

type Subject = SubjectItem
type ColumnItemInput = string | ColumnItem

const RECENT_COLUMNS_KEY = 'resource.filterbar.recent.columns'

function getColumnName(col: ColumnItemInput): string {
  return typeof col === 'string' ? col : col.name
}

function getColumnIcon(col: ColumnItemInput): string {
  return typeof col === 'string' ? '' : (col.icon || '')
}

const props = defineProps<{
  currentSubjects: Subject[]
  currentSubject: Subject | null
  topVisibleColumns: ColumnItemInput[]
  groupedColumns: ColumnGroupItem[]
  activeColumn: string
  groupCountMap?: Record<string, number>
  showGroupCounts?: boolean
  columnCountMap?: Record<string, number>
  showColumnCounts?: boolean
}>()

const emit = defineEmits<{
  'select-subject': [subject: Subject]
  'update:activeColumn': [value: string]
}>()

const subjectOptions = computed(() =>
  props.currentSubjects.map((s) => ({
    value: s.key,
    label: s.name,
    showDot: !!s.isNew,
  })),
)

const topColumnOptions = computed(() =>
  props.topVisibleColumns.map((col) => {
    const item = typeof col === 'string' ? { name: col } : col
    return {
      value: getColumnName(col),
      label: getColumnName(col),
      icon: getColumnIcon(col) || undefined,
      priority: 'priority' in item ? !!item.priority : false,
    }
  }),
)

const showMoreColumns = ref(false)
const searchKeyword = ref('')
const recentColumnNames = ref<string[]>([])
const normalizedKeyword = computed(() => searchKeyword.value.trim())
const safeColumnCountMap = computed<Record<string, number>>(() => props.columnCountMap || {})
const safeGroupCountMap = computed<Record<string, number>>(() => props.groupCountMap || {})

const totalMoreCount = computed(() =>
  props.groupedColumns.reduce((acc, group) => acc + group.columns.length, 0),
)

const filteredGroupedColumns = computed(() => {
  const keyword = normalizedKeyword.value
  if (!keyword) {
    return props.groupedColumns.map((group) => ({
      ...group,
      columns: sortColumnsByCount(group.columns),
    }))
  }
  return props.groupedColumns
    .map((group) => ({
      ...group,
      columns: sortColumnsByCount(
        group.columns.filter((col) => col.name.includes(keyword)),
      ),
    }))
    .filter((group) => group.columns.length > 0)
})

const recentColumns = computed(() => {
  const map = new Map<string, ColumnItem>()
  for (const group of props.groupedColumns) {
    for (const col of group.columns) map.set(col.name, col)
  }
  return recentColumnNames.value
    .map((name, index) => {
      const col = map.get(name)
      if (!col) return null
      const count = props.columnCountMap?.[name] || 0
      const recentWeight = Math.max(0, 10 - index) * 10
      const score = recentWeight + count
      return { col, score }
    })
    .filter((item): item is { col: ColumnItem; score: number } => !!item)
    .sort((a, b) => b.score - a.score)
    .map((item) => item.col)
})

const hotColumns = computed(() => {
  const all = props.groupedColumns.flatMap((g) => g.columns)
  const recentSet = new Set(recentColumns.value.map((c) => c.name))
  return [...all]
    .filter((col) => !recentSet.has(col.name))
    .sort((a, b) => {
      const diff = (props.columnCountMap?.[b.name] || 0) - (props.columnCountMap?.[a.name] || 0)
      if (diff !== 0) return diff
      return a.name.localeCompare(b.name, 'zh-Hans-CN')
    })
    .slice(0, 8)
})

function openMoreColumns() {
  showMoreColumns.value = true
}

function closeMoreColumns() {
  showMoreColumns.value = false
  searchKeyword.value = ''
}

function onSelectColumn(name: string) {
  emit('update:activeColumn', name)
  updateRecentColumns(name)
  closeMoreColumns()
}

function updateRecentColumns(name: string) {
  const next = [name, ...recentColumnNames.value.filter((n) => n !== name)].slice(0, 8)
  recentColumnNames.value = next
  try {
    localStorage.setItem(RECENT_COLUMNS_KEY, JSON.stringify(next))
  } catch {
    // ignore local storage failures
  }
}

function onSelectSubject(key: string) {
  const subject = props.currentSubjects.find((s) => s.key === key)
  if (subject) emit('select-subject', subject)
}

function sortColumnsByCount(columns: ColumnItem[]): ColumnItem[] {
  const counts = props.columnCountMap || {}
  return [...columns].sort((a, b) => {
    const diff = (counts[b.name] || 0) - (counts[a.name] || 0)
    if (diff !== 0) return diff
    return a.name.localeCompare(b.name, 'zh-Hans-CN')
  })
}

onMounted(() => {
  try {
    const raw = localStorage.getItem(RECENT_COLUMNS_KEY)
    if (!raw) return
    const parsed = JSON.parse(raw)
    if (Array.isArray(parsed)) {
      recentColumnNames.value = parsed.filter((v) => typeof v === 'string')
    }
  } catch {
    recentColumnNames.value = []
  }
})
</script>

<style scoped>
.filter-bar-container {
  padding: 0 24px 6px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.column-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.column-row__options {
  flex: 1;
  min-width: 0;
}

.column-row__more {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.more-columns-btn {
  border: 1px solid #dbe4f0;
  background: #fff;
  color: #475569;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  cursor: pointer;
}

.more-columns-btn:hover {
  border-color: var(--color-primary, #4361EE);
  color: var(--color-primary, #4361EE);
}

.more-columns-btn__count {
  margin-left: 4px;
  color: #94a3b8;
}

.more-columns-panel__groups {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
}

.more-columns-group {
  min-width: 260px;
  flex: 1 1 320px;
}

.more-columns-group__label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.more-columns-group__count {
  color: #64748b;
  background: #f1f5f9;
  border-radius: 999px;
  padding: 0 8px;
  line-height: 18px;
  font-size: 12px;
}

.more-columns-group__items {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.more-columns-item {
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #475569;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.more-columns-item:hover {
  border-color: #cbd5e1;
}

.more-columns-item.is-active {
  border-color: var(--color-primary, #4361EE);
  color: var(--color-primary, #4361EE);
  background: #eef2ff;
}

.more-columns-item__icon {
  line-height: 1;
}

.more-columns-item__count {
  margin-left: 2px;
  color: #64748b;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  padding: 0 6px;
  line-height: 16px;
  font-size: 11px;
}

.more-columns-drawer-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.3);
  z-index: 2100;
  display: flex;
  justify-content: flex-end;
}

.more-columns-drawer {
  width: min(520px, 92vw);
  height: 100%;
  background: #fff;
  box-shadow: -8px 0 28px rgba(15, 23, 42, 0.16);
  padding: 16px;
  overflow: auto;
}

.more-columns-drawer__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.more-columns-drawer__header h4 {
  margin: 0;
  font-size: 16px;
  color: #0f172a;
}

.more-columns-drawer__close {
  border: none;
  background: transparent;
  font-size: 22px;
  line-height: 1;
  color: #64748b;
  cursor: pointer;
}

.more-columns-drawer__search {
  margin-bottom: 12px;
}

.more-columns-drawer__search input {
  width: 100%;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 13px;
}

.more-columns-drawer__section {
  margin-bottom: 14px;
}

.more-columns-drawer__empty {
  color: #94a3b8;
  font-size: 13px;
  text-align: center;
  padding: 16px 0;
}

.drawer-fade-enter-active,
.drawer-fade-leave-active {
  transition: opacity 0.2s ease;
}

.drawer-fade-enter-from,
.drawer-fade-leave-to {
  opacity: 0;
}
</style>
