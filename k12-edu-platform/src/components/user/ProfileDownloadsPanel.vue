<template>
  <div class="profile-downloads-panel">
    <div class="stats-row">
      <div class="stat-card">
        <span class="stat-value">{{ stats.total }}</span>
        <span class="stat-label">下载总数</span>
      </div>
      <div class="stat-card stat-week">
        <span class="stat-value">{{ stats.weekCount }}</span>
        <span class="stat-label">本周下载</span>
      </div>
      <div class="stat-card stat-today">
        <span class="stat-value">{{ stats.todayCount }}</span>
        <span class="stat-label">今日下载</span>
      </div>
    </div>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索资源名称..."
          clearable
          size="small"
          style="width: 240px"
          @keyup.enter="onSearch"
          @clear="onSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button size="small" type="primary" @click="onSearch">搜索</el-button>
      </div>
      <div class="toolbar-right">
        <el-button
          v-if="selectedIds.length"
          size="small"
          type="danger"
          plain
          @click="onBatchDelete"
        >
          删除选中 ({{ selectedIds.length }})
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="table-wrap">
      <el-table
        :data="items"
        style="width: 100%"
        stripe
        size="small"
        empty-text=""
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="42" />
        <el-table-column label="名称" min-width="260">
          <template #default="{ row }">
            <div class="name-cell">
              <span class="file-icon" :class="fileIconClass(row)">{{ fileIconText(row) }}</span>
              <div class="name-info">
                <span class="name-title">{{ row.resourceTitle || '未命名资源' }}</span>
                <span class="name-meta">{{ rowItemMeta(row) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :type="teachingTypeTagType(row) as any">
              {{ row.teachingType || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下载时间" width="130" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" type="primary" link @click="handleView(row)">
                查看
              </el-button>
              <el-button size="small" type="success" link @click="handleDownload(row)">
                再次下载
              </el-button>
              <el-button size="small" type="danger" link @click="handleDelete(row)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <div class="empty-state">
            <div class="empty-icon">
              <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
                <rect x="20" y="12" width="40" height="52" rx="4" stroke="#d0d5dd" stroke-width="2" fill="#f9fafb"/>
                <path d="M32 36h16M32 44h10" stroke="#d0d5dd" stroke-width="2" stroke-linecap="round"/>
                <circle cx="56" cy="56" r="18" fill="#f0f5ff" stroke="#7c8cf7" stroke-width="2"/>
                <path d="M56 48v16M48 56h16" stroke="#7c8cf7" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <p class="empty-text">暂无下载记录</p>
            <p class="empty-hint">浏览资源并下载后，记录将自动保存在这里</p>
          </div>
        </template>
      </el-table>
    </div>

    <div v-if="total > 0" class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        background
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useMyDownloads } from '@/composables/useMyDownloads'
import type { DownloadItem } from '@/api/download'

const {
  loading,
  items,
  total,
  currentPage,
  pageSize,
  searchKeyword,
  stats,
  setSearchKeyword,
  handleView,
  handleDownload,
  handleDelete,
  handleBatchDelete,
  formatDate,
} = useMyDownloads()

const selectedIds = ref<number[]>([])

function onSelectionChange(rows: DownloadItem[]) {
  selectedIds.value = rows.map((r) => r.id)
}

function onBatchDelete() {
  void handleBatchDelete([...selectedIds.value]).then(() => {
    selectedIds.value = []
  })
}

function onSearch() {
  setSearchKeyword(searchKeyword.value)
}

function fileIconClass(row: DownloadItem): string {
  const ext = (row.fileExt || '').toLowerCase()
  if (['ppt', 'pptx'].includes(ext)) return 'icon-ppt'
  if (['doc', 'docx'].includes(ext)) return 'icon-word'
  if (['pdf'].includes(ext)) return 'icon-pdf'
  if (['mp4', 'avi', 'mov', 'wmv'].includes(ext)) return 'icon-video'
  if (['xls', 'xlsx'].includes(ext)) return 'icon-excel'
  return 'icon-default'
}

function fileIconText(row: DownloadItem): string {
  const ext = (row.fileExt || '').toLowerCase()
  if (['ppt', 'pptx'].includes(ext)) return 'P'
  if (['doc', 'docx'].includes(ext)) return 'W'
  if (['pdf'].includes(ext)) return 'PDF'
  if (['mp4', 'avi', 'mov', 'wmv'].includes(ext)) return '▶'
  if (['xls', 'xlsx'].includes(ext)) return 'E'
  return '📄'
}

function rowItemMeta(row: DownloadItem): string {
  return [row.subject, row.gradeName].filter(Boolean).join(' · ')
}

function teachingTypeTagType(row: DownloadItem): string {
  const typeMap: Record<string, string> = {
    课件: 'warning',
    教案: 'primary',
    试卷: 'danger',
    练习: 'success',
    学案: 'info',
    视频: '',
  }
  return typeMap[row.teachingType || ''] || ''
}
</script>

<style scoped>
.profile-downloads-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stats-row {
  display: flex;
  gap: 12px;
}
.stat-card {
  flex: 1;
  padding: 14px 20px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 8px;
  text-align: center;
  color: #fff;
}
.stat-card.stat-week {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}
.stat-card.stat-today {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
}
.stat-value {
  display: block;
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 4px;
  display: block;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.table-wrap {
  border: 1px solid var(--border-light);
  border-radius: 8px;
  overflow: hidden;
}
.name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.file-icon {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}
.file-icon.icon-ppt { background: #d35400; }
.file-icon.icon-word { background: #2b5797; }
.file-icon.icon-pdf { background: #c0392b; }
.file-icon.icon-video { background: #16a085; }
.file-icon.icon-excel { background: #27ae60; }
.file-icon.icon-default { background: #7f8c8d; font-size: 16px; }
.name-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.name-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.name-meta {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}
.time-text {
  font-size: 13px;
  color: var(--text-regular);
}
.action-btns {
  display: flex;
  gap: 4px;
  justify-content: center;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}
.empty-icon {
  margin-bottom: 12px;
}
.empty-text {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 4px;
}
.empty-hint {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 4px;
}

.el-table {
  --el-table-border-color: var(--border-light);
}
.el-table th.el-table__cell {
  background-color: #f5f7fa;
  font-weight: 600;
  color: var(--text-regular);
}
</style>

