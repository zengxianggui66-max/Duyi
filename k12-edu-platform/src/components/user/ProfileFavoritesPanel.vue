<template>
  <div class="profile-favorites-panel">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card stat-total">
        <span class="stat-value">{{ collectStats.total }}</span>
        <span class="stat-label">收藏总数</span>
      </div>
      <div class="stat-card stat-primary">
        <span class="stat-value">{{ collectStats.primaryCount }}</span>
        <span class="stat-label">小学区</span>
      </div>
      <div class="stat-card stat-junior">
        <span class="stat-value">{{ collectStats.juniorCount }}</span>
        <span class="stat-label">初中区</span>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-bar">
      <div class="filter-selects">
        <el-select
          v-model="activeSection"
          class="filter-select"
          size="default"
          @change="onSectionChange"
        >
          <el-option
            v-for="sec in sectionConfig"
            :key="sec.key"
            :label="sec.icon + ' ' + sec.name"
            :value="sec.key"
          />
        </el-select>

        <el-select
          v-model="activeSubject"
          class="filter-select"
          size="default"
          placeholder="全部学科"
          clearable
          @change="onSubjectChange"
        >
          <el-option label="全部学科" value="" />
          <el-option
            v-for="sub in currentSubjects"
            :key="sub.key"
            :label="sub.icon + ' ' + sub.name"
            :value="sub.key"
          />
        </el-select>

        <el-select
          v-model="activeType"
          class="filter-select"
          size="default"
          placeholder="全部类型"
          clearable
          @change="onTypeChange"
        >
          <el-option
            v-for="t in currentResourceTypes"
            :key="t.key"
            :label="t.icon + ' ' + t.name"
            :value="t.key"
          />
        </el-select>

        <el-select
          :model-value="sortOrder"
          class="filter-select filter-select-sort"
          size="default"
          @change="onSortChange"
        >
          <el-option label="最新收藏" value="desc" />
          <el-option label="最早收藏" value="asc" />
        </el-select>
      </div>

      <div class="filter-actions">
        <el-input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索收藏的资源名称..."
          :prefix-icon="Search"
          clearable
          @clear="onSearch"
          @keyup.enter="onSearch"
        />
        <el-button size="default" type="primary" @click="onSearch">搜索</el-button>
      </div>
    </div>

    <!-- 资源列表 -->
    <div class="list-wrap" v-loading="loading">
      <!-- 空状态 -->
      <el-empty v-if="!loading && items.length === 0">
        <template #image>
          <svg width="100" height="100" viewBox="0 0 100 100" fill="none">
            <rect x="25" y="15" width="50" height="65" rx="5" stroke="#d0d5dd" stroke-width="2" fill="#f9fafb"/>
            <path d="M38 45h24M38 55h16" stroke="#d0d5dd" stroke-width="2" stroke-linecap="round"/>
            <path d="M50 30l8 8-8 8" stroke="#d0d5dd" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </template>
        <p class="empty-text">暂无收藏资源</p>
        <p class="empty-hint">浏览资源详情页，点击收藏即可添加</p>
      </el-empty>

      <!-- 列表 -->
      <div v-else class="item-list">
        <div v-for="(row, idx) in items" :key="row.id ?? idx" class="item-row">
          <span class="file-icon" :class="fileIconClass(row)">{{ fileIconText(row) }}</span>
          <div class="item-info">
            <p class="item-title" :title="row.title">{{ row.title || '未命名资源' }}</p>
            <span class="item-meta">
              <el-tag v-if="row.teachingType" size="small" effect="plain" :type="teachingTypeTagType(row) as any">
                {{ row.teachingType }}
              </el-tag>
              <span v-if="row.subject" class="meta-text">{{ row.subject }}</span>
              <span class="meta-divider">|</span>
              <span class="meta-text">{{ formatDate(row.collectTime) }} 收藏</span>
            </span>
          </div>
          <div class="item-actions">
            <el-tooltip content="下载" placement="top">
              <el-button size="small" circle class="act-btn" @click="handleDownload(row)">
                <el-icon><Download /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="取消收藏" placement="top">
              <el-button size="small" circle class="act-btn act-btn-danger" @click="handleUncollect(row)">
                <el-icon><StarFilled /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-wrap">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          background
          @update:current-page="onPageChange"
          @update:page-size="onSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Download, StarFilled } from '@element-plus/icons-vue'
import { useMyCollections } from '@/composables/useMyCollections'
import type { CollectItem } from '@/api/collect'

const {
  activeSection,
  activeSubject,
  activeType,
  currentPage,
  pageSize,
  loading,
  total,
  items,
  collectStats,
  sectionConfig,
  currentSubjects,
  currentResourceTypes,
  searchKeyword,
  sortOrder,
  selectSection,
  selectSubject,
  selectType,
  fetchList,
  fetchStats,
  handleUncollect,
  handleDownload,
  setSearchKeyword,
  setSortOrder,
  formatDate,
  formatExt,
  resolveTypeDisplayName,
} = useMyCollections()

const SearchIcon = Search

function fileIconClass(row: CollectItem): string {
  const ext = (row.fileExt || '').toLowerCase()
  if (['ppt', 'pptx'].includes(ext)) return 'icon-ppt'
  if (['doc', 'docx'].includes(ext)) return 'icon-word'
  if (['pdf'].includes(ext)) return 'icon-pdf'
  if (['mp4', 'avi', 'mov', 'wmv'].includes(ext)) return 'icon-video'
  if (['xls', 'xlsx'].includes(ext)) return 'icon-excel'
  return 'icon-default'
}

function fileIconText(row: CollectItem): string {
  const ext = (row.fileExt || '').toLowerCase()
  if (['ppt', 'pptx'].includes(ext)) return 'P'
  if (['doc', 'docx'].includes(ext)) return 'W'
  if (['pdf'].includes(ext)) return 'PDF'
  if (['mp4', 'avi', 'mov', 'wmv'].includes(ext)) return '▶'
  if (['xls', 'xlsx'].includes(ext)) return 'E'
  return '📄'
}

function teachingTypeTagType(row: CollectItem): string {
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

function onSectionChange(val: string) {
  selectSection(val as any)
}

function onSubjectChange(val: string | '') {
  selectSubject(val)
}

function onTypeChange(val: string | '') {
  selectType(val || 'all')
}

function onSortChange(val: 'desc' | 'asc') {
  setSortOrder(val)
}

function onSearch() {
  setSearchKeyword(searchKeyword.value)
}

function onPageChange(page: number) {
  currentPage.value = page
}

function onSizeChange(size: number) {
  pageSize.value = size
}
</script>

<style scoped>
.profile-favorites-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ===== 统计卡片 ===== */
.stats-row {
  display: flex;
  gap: 12px;
}
.stat-card {
  flex: 1;
  padding: 14px 20px;
  border-radius: 8px;
  text-align: center;
  color: #fff;
}
.stat-card.stat-total {
  background: linear-gradient(135deg, #667eea, #764ba2);
}
.stat-card.stat-primary {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}
.stat-card.stat-junior {
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

/* ===== 筛选工具栏 ===== */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
}
.filter-selects {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
  flex: 1;
  min-width: 0;
}
.filter-select {
  width: 112px;
  flex-shrink: 0;
}
.filter-select-sort {
  width: 108px;
}
.filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  margin-left: auto;
}
.search-input {
  width: 200px;
}

@media (max-width: 960px) {
  .filter-bar {
    flex-wrap: wrap;
  }
  .filter-actions {
    margin-left: 0;
    width: 100%;
  }
  .search-input {
    flex: 1;
    width: auto;
    min-width: 0;
  }
}

/* ===== 资源列表 ===== */
.list-wrap {
  min-height: 200px;
}
.item-list {
  display: flex;
  flex-direction: column;
  gap: 1px;
  background: var(--border-light);
  border-radius: 8px;
  overflow: hidden;
}
.item-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;
  transition: background 0.15s;
}
.item-row:hover {
  background: #f5f7fa;
}

/* 文件图标 */
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

/* 信息区 */
.item-info {
  flex: 1;
  min-width: 0;
}
.item-title {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 500;
  color: #1a1a2e;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-meta {
  font-size: 12px;
  color: #8c8c8c;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.meta-text {
  color: #8c8c8c;
}
.meta-divider {
  color: #d9d9d9;
}

/* 操作区 - hover 显示 */
.item-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.15s;
}
.item-row:hover .item-actions {
  opacity: 1;
}
.act-btn {
  --el-button-size: 28px;
  border: none;
  background: transparent;
  color: #8c8c8c;
}
.act-btn:hover {
  background: #e8eaed;
}
.act-btn.act-btn-danger:hover {
  background: #fce4ec;
  color: #e53935;
}

/* 空状态 */
.empty-text {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0 0 4px;
}
.empty-hint {
  font-size: 12px;
  color: #bfbfbf;
  margin: 0;
}

/* 分页 */
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>

