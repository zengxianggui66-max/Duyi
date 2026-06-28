<template>
  <div class="profile-uploads-panel">
    <!-- Stats Cards -->
    <div class="stats-row">
      <div v-for="(s, i) in uploadStatCards" :key="i" class="stat-card">
        <span class="stat-value">{{ s.value }}</span>
        <span class="stat-label">{{ s.label }}</span>
      </div>
    </div>

    <!-- Filter Toolbar -->
    <div class="filter-bar">
      <div class="status-tabs">
        <el-radio-group
          :model-value="activeStatusFilter"
          size="small"
          @change="onStatusFilterChange"
        >
          <el-radio-button
            v-for="tab in statusTabs"
            :key="tab.key"
            :value="tab.key"
          >
            {{ tab.label }}
          </el-radio-button>
        </el-radio-group>
      </div>

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
      </div>

      <div class="filter-actions">
        <el-input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索资源名称"
          size="small"
          clearable
          @clear="onSearchClear"
          @keyup.enter="onSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button size="small" type="primary" @click="onSearch">搜索</el-button>
        <el-button type="primary" size="small" @click="handleUpload">
          <el-icon><Plus /></el-icon> 新建上传
        </el-button>
      </div>
    </div>

    <!-- Item List -->
    <div class="list-wrap" v-loading="loading">
      <el-empty v-if="!loading && listItems.length === 0" :description="emptyText" />

      <div v-else class="item-list">
        <div v-for="(row, idx) in listItems" :key="rowKey(row, idx)" class="item-row">
          <div class="item-icon">{{ sectionIcon(activeSection) }}</div>
          <div class="item-info">
            <p class="item-title" :title="row.title">{{ row.title }}</p>
            <span class="item-meta">
              <span class="meta-tag" :class="statusClass(row)">{{ statusLabel(row.status, row.auditStatus, row.publishStatus) }}</span>
              <span class="meta-divider">|</span>
              {{ row.typeName || '资源' }}
              <span class="meta-divider">|</span>
              {{ row.updateTime }}
            </span>
            <p v-if="isRejectedItem(row) && row.rejectReason" class="item-reject-reason">
              驳回原因：{{ row.rejectReason }}
              <span v-if="row.rejectedAt" class="reject-time">（{{ row.rejectedAt }}）</span>
            </p>
          </div>
          <div class="item-actions">
            <el-button
              v-if="isRejectedItem(row)"
              type="warning"
              size="small"
              plain
              @click="handleEdit(row)"
            >
              查看原因
            </el-button>
            <el-button
              v-if="isRejectedItem(row)"
              type="primary"
              size="small"
              @click="handleReupload(row)"
            >
              重新上传
            </el-button>
            <el-button
              v-if="isDraftItem(row)"
              type="primary"
              size="small"
              @click="handleEdit(row)"
            >
              继续编辑
            </el-button>
            <el-tooltip v-if="!isDraftItem(row)" content="下载" placement="top">
              <el-button size="small" circle class="act-btn" @click="handleDownload(row)">
                <el-icon><Download /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip v-if="!isDraftItem(row)" content="编辑" placement="top">
              <el-button size="small" circle class="act-btn" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip v-else content="继续编辑草稿" placement="top">
              <el-button size="small" circle class="act-btn" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </el-button>
            </el-tooltip>
            <el-dropdown trigger="click" @command="(cmd: string) => handleMore(cmd, row)">
              <el-button size="small" circle class="act-btn">
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="!isDraftItem(row)" command="share">分享</el-dropdown-item>
                  <el-dropdown-item v-if="isDraftItem(row)" command="delete" divided>
                    删除草稿
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="displayTotal > 0" class="pagination-wrap">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="displayTotal"
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
import { computed } from 'vue'
import { Plus, Download, Edit, MoreFilled, Search } from '@element-plus/icons-vue'
import { useMyResources } from '@/composables/useMyResources'
import type { MyResourceListItem } from '@/composables/useMyResources'
import { MY_RESOURCE_SECTIONS } from '@/constants/myResources'

const {
  activeSection,
  activeSubject,
  activeType,
  activeStatusFilter,
  statusTabs,
  currentPage,
  pageSize,
  loading,
  displayTotal,
  listItems,
  uploadStats,
  sectionConfig,
  currentSubjects,
  currentResourceTypes,
  selectSection,
  selectSubject,
  selectType,
  selectStatusFilter,
  searchKeyword,
  setSearchKeyword,
  handleUpload,
  handleDownload,
  handleEdit,
  handleReupload,
  handleMore,
  statusLabel,
  isDraftItem,
  isRejectedItem,
} = useMyResources({ pageSize: 20 })

const uploadStatCards = computed(() => [
  { value: uploadStats.value.total, label: '上传总数' },
  { value: uploadStats.value.draft, label: '草稿' },
  { value: uploadStats.value.published, label: '已发布' },
  { value: uploadStats.value.pending, label: '待审核' },
  { value: uploadStats.value.totalDownloads, label: '总下载' },
])

const emptyText = computed(() => {
  if (searchKeyword.value.trim()) return `未搜索到「${searchKeyword.value.trim()}」相关资源`
  if (activeStatusFilter.value === 'draft') return '暂无草稿，上传时可点击「保存草稿」'
  return '暂无上传记录，点击右上角按钮上传资源'
})

function onStatusFilterChange(val: string | number | boolean | undefined) {
  selectStatusFilter(val as import('@/composables/useMyResources').MyResourceStatusFilter)
}

function sectionIcon(section: string): string {
  return MY_RESOURCE_SECTIONS.find((s) => s.key === section)?.icon || '📤'
}

function statusClass(row: MyResourceListItem): string {
  if (row.auditStatus === 1 && row.publishStatus === 0) return 'status-approved'
  if (row.status == null) return ''
  if (row.status === 1) return 'status-published'
  if (row.status === 0) return 'status-pending'
  if (row.status === -1) return 'status-draft'
  if (row.status === 2) return 'status-rejected'
  if (row.status === 3) return 'status-offline'
  return ''
}

function rowKey(row: MyResourceListItem, idx: number) {
  return row.id ?? idx
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

function onPageChange(page: number) {
  currentPage.value = page
}

function onSizeChange(size: number) {
  pageSize.value = size
}

function onSearch() {
  setSearchKeyword(searchKeyword.value)
}

function onSearchClear() {
  setSearchKeyword('')
}
</script>

<style scoped>
.profile-uploads-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Stats */
.stats-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.stat-card {
  flex: 1;
  min-width: 80px;
  padding: 12px 16px;
  background: var(--bg-body);
  border-radius: var(--radius-sm);
  text-align: center;
  border: 1px solid var(--border-light);
}
.stat-value {
  display: block;
  font-size: 22px;
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1.4;
}
.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
}

/* Filter Bar */
.filter-bar {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 12px;
}
.status-tabs :deep(.el-radio-button__inner) {
  padding: 8px 14px;
}
.filter-selects {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.filter-select {
  width: 140px;
}
.filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}
.search-input {
  width: 200px;
}

/* Item List */
.list-wrap {
  min-height: 200px;
}
.item-list {
  display: flex;
  flex-direction: column;
  gap: 1px;
  background: var(--border-light);
  border-radius: var(--radius-sm);
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
  background: var(--el-fill-color-light);
}
.item-icon {
  font-size: 24px;
  flex-shrink: 0;
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
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.meta-tag {
  display: inline-block;
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 11px;
  line-height: 1.5;
}
.meta-tag.status-published {
  background: #e8f5e9;
  color: #2e7d32;
}
.meta-tag.status-pending {
  background: #fff3e0;
  color: #e65100;
}
.meta-tag.status-approved {
  background: #e8f5e9;
  color: #2e7d32;
}
.meta-tag.status-draft {
  background: #f3e5f5;
  color: #6a1b9a;
}
.meta-tag.status-rejected {
  background: #ffebee;
  color: #c62828;
}
.meta-tag.status-offline {
  background: #eceff1;
  color: #546e7a;
}
.meta-divider {
  color: var(--border-dark);
}

.item-reject-reason {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: #c62828;
  background: #fff5f5;
  padding: 6px 8px;
  border-radius: 4px;
  border-left: 3px solid #ef5350;
}
.item-reject-reason .reject-time {
  color: #909399;
}

/* Actions */
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
  color: var(--text-secondary);
}
.act-btn:hover {
  background: var(--el-fill-color);
  color: var(--color-primary);
}

/* Pagination */
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>
