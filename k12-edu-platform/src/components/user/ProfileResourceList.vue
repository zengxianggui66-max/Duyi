<template>
  <div class="profile-resource-list">
    <div v-if="stats.length" class="stats-row">
      <div v-for="(s, i) in stats" :key="i" class="stat-card">
        <span class="stat-value">{{ s.value }}</span>
        <span class="stat-label">{{ s.label }}</span>
      </div>
    </div>

    <div class="panel-body">
      <aside class="panel-sidebar">
        <div class="section-nav">
          <div
            v-for="section in sectionConfig"
            :key="section.key"
            class="section-nav-item"
            :class="{ active: activeSection === section.key }"
            @click="$emit('select-section', section.key)"
          >
            <span>{{ section.icon }}</span>
            <span>{{ section.name }}</span>
          </div>
        </div>
        <div class="subject-nav">
          <div class="subject-nav-title">{{ subjectNavTitle }}</div>
          <button
            class="subject-nav-item"
            :class="{ active: !activeSubject }"
            @click="$emit('select-subject', '')"
          >
            全部
          </button>
          <button
            v-for="sub in currentSubjects"
            :key="sub.key"
            class="subject-nav-item"
            :class="{ active: activeSubject === sub.key }"
            @click="$emit('select-subject', sub.key)"
          >
            {{ sub.name }}
          </button>
        </div>
      </aside>

      <main class="panel-main" v-loading="loading">
        <div v-if="showUploadBtn" class="toolbar">
          <el-button type="primary" size="small" @click="$emit('upload')">上传资源</el-button>
        </div>
        <div class="type-tabs">
          <span
            v-for="type in resourceTypes"
            :key="type.key"
            class="type-tab"
            :class="{ active: activeType === type.key }"
            @click="$emit('select-type', type.key)"
          >
            {{ type.icon }} {{ type.name }}
            <em>{{ getTypeCount(type.key) }}</em>
          </span>
        </div>

        <el-empty v-if="!loading && rows.length === 0" :description="emptyText" />

        <div v-else class="item-list">
          <div v-for="(row, idx) in rows" :key="rowKey(row, idx)" class="item-row">
            <span class="item-icon">{{ mode === 'uploads' ? '📤' : '⭐' }}</span>
            <div class="item-info">
              <p class="item-title">{{ rowTitle(row) }}</p>
              <span class="item-meta">{{ rowMeta(row) }}</span>
            </div>
            <div class="item-actions">
              <el-button size="small" type="success" plain @click="$emit('download', row)">
                下载
              </el-button>
              <el-button
                v-if="secondaryLabel"
                size="small"
                plain
                @click="$emit('secondary', row)"
              >
                {{ secondaryLabel }}
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="total > 0" class="pagination-wrap">
          <el-pagination
            :current-page="currentPage"
            :page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next"
            background
            @update:current-page="$emit('update:currentPage', $event)"
            @update:page-size="$emit('update:pageSize', $event)"
          />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { CollectItem } from '@/api/collect'
import type { MyResourceListItem } from '@/composables/useMyResources'
import type {
  MyResourceSectionItem,
  MyResourceNavItem,
  MyResourceTypeTab,
  MyResourceSection,
} from '@/constants/myResources'

const props = defineProps<{
  mode: 'favorites' | 'uploads'
  stats: { value: number; label: string }[]
  sectionConfig: MyResourceSectionItem[]
  activeSection: MyResourceSection
  activeSubject: string
  activeType: string
  currentSubjects: MyResourceNavItem[]
  subjectNavTitle: string
  resourceTypes: MyResourceTypeTab[]
  loading: boolean
  rows: CollectItem[] | MyResourceListItem[]
  total: number
  currentPage: number
  pageSize: number
  getTypeCount: (key: string) => number
  emptyText: string
  showUploadBtn?: boolean
  secondaryLabel?: string
  statusLabel?: (s?: number) => string
}>()

defineEmits<{
  'select-section': [key: MyResourceSection]
  'select-subject': [key: string]
  'select-type': [key: string]
  download: [row: CollectItem | MyResourceListItem]
  secondary: [row: CollectItem | MyResourceListItem]
  upload: []
  'update:currentPage': [page: number]
  'update:pageSize': [size: number]
}>()

function rowKey(row: CollectItem | MyResourceListItem, idx: number) {
  const id = (row as { id?: number }).id
  return id ?? idx
}

function rowTitle(row: CollectItem | MyResourceListItem) {
  return row.title || '未命名资源'
}

function rowMeta(row: CollectItem | MyResourceListItem) {
  if ('collectTime' in row) {
    const c = row as CollectItem
    return [c.teachingType, c.subject, c.collectTime?.slice(0, 10)].filter(Boolean).join(' · ')
  }
  const u = row as MyResourceListItem
  const status = props.statusLabel?.(u.status)
  return [u.typeName, u.subjectName, u.updateTime, status].filter(Boolean).join(' · ')
}
</script>

<style scoped>
.profile-resource-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.stats-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.stat-card {
  flex: 1;
  min-width: 90px;
  padding: 10px 14px;
  background: var(--bg-body);
  border-radius: var(--radius-sm);
  text-align: center;
}
.stat-value {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary);
}
.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
}
.panel-body {
  display: flex;
  gap: 16px;
}
.panel-sidebar {
  width: 150px;
  flex-shrink: 0;
}
.section-nav {
  background: var(--bg-body);
  border-radius: var(--radius-sm);
  padding: 6px;
  margin-bottom: 10px;
}
.section-nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 8px;
  font-size: 12px;
  border-radius: 4px;
  cursor: pointer;
}
.section-nav-item.active {
  background: var(--color-primary);
  color: #fff;
}
.subject-nav-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 6px;
}
.subject-nav-item {
  display: block;
  width: 100%;
  text-align: left;
  padding: 5px 8px;
  border: none;
  background: none;
  font-size: 12px;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 2px;
}
.subject-nav-item.active {
  background: var(--color-primary-bg);
  color: var(--color-primary);
}
.panel-main {
  flex: 1;
  min-width: 0;
}
.toolbar {
  margin-bottom: 10px;
}
.type-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}
.type-tab {
  padding: 5px 10px;
  font-size: 12px;
  background: var(--bg-body);
  border-radius: 4px;
  cursor: pointer;
}
.type-tab.active {
  background: var(--color-primary);
  color: #fff;
}
.type-tab em {
  font-style: normal;
  margin-left: 4px;
}
.item-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.item-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: var(--bg-body);
  border-radius: var(--radius-sm);
}
.item-icon {
  font-size: 22px;
}
.item-info {
  flex: 1;
  min-width: 0;
}
.item-title {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-meta {
  font-size: 12px;
  color: var(--text-secondary);
}
.item-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}
.pagination-wrap {
  margin-top: 14px;
  display: flex;
  justify-content: center;
}
</style>
