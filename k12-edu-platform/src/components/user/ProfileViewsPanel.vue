<template>
  <div class="profile-views-panel">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card stat-total">
        <span class="stat-value">{{ viewStats.total }}</span>
        <span class="stat-label">浏览总数</span>
      </div>
      <div class="stat-card stat-week">
        <span class="stat-value">{{ viewStats.weekCount }}</span>
        <span class="stat-label">本周浏览</span>
      </div>
      <div class="stat-card stat-today">
        <span class="stat-value">{{ viewStats.todayCount }}</span>
        <span class="stat-label">今日浏览</span>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-bar">
      <div class="filter-selects">
        <el-select
          v-model="activeSection"
          class="filter-select"
          size="small"
          @change="onSectionChange"
        >
          <el-option label="📂 全部学段" value="all" />
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
          size="small"
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
          size="small"
          placeholder="全部类型"
          clearable
          @change="onTypeChange"
        >
          <el-option label="全部类型" value="all" />
          <el-option
            v-for="t in currentTypes"
            :key="t"
            :label="t"
            :value="t"
          />
        </el-select>

        <el-select
          :model-value="sortOrder"
          class="filter-select filter-select-sort"
          size="small"
          @change="onSortChange"
        >
          <el-option label="最新浏览" value="desc" />
          <el-option label="最早浏览" value="asc" />
        </el-select>
      </div>

      <div class="filter-actions">
        <el-input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索资源名称..."
          size="small"
          :prefix-icon="Search"
          clearable
          @clear="onSearch"
          @keyup.enter="onSearch"
        />
        <el-button size="small" type="primary" @click="onSearch">搜索</el-button>
        <el-popconfirm
          title="确定清空所有浏览记录？"
          confirm-button-text="清空"
          @confirm="clearAll"
        >
          <template #reference>
            <el-button size="small" type="danger" plain class="clear-btn">
              <el-icon><Delete /></el-icon>
              <span class="clear-btn-text">清空</span>
            </el-button>
          </template>
        </el-popconfirm>
      </div>
    </div>

    <!-- 资源列表 -->
    <div class="list-wrap">
      <!-- 空状态 -->
      <el-empty v-if="items.length === 0">
        <template #image>
          <svg width="100" height="100" viewBox="0 0 100 100" fill="none">
            <rect x="25" y="15" width="50" height="65" rx="5" stroke="#d0d5dd" stroke-width="2" fill="#f9fafb"/>
            <path d="M38 45h24M38 55h16" stroke="#d0d5dd" stroke-width="2" stroke-linecap="round"/>
            <circle cx="50" cy="32" r="6" stroke="#d0d5dd" stroke-width="2"/>
          </svg>
        </template>
        <p class="empty-text">暂无浏览记录</p>
        <p class="empty-hint">浏览资源详情页，记录将自动保存在这里</p>
      </el-empty>

      <!-- 列表 -->
      <div v-else class="item-list">
        <div v-for="(row, idx) in items" :key="row.id ?? idx" class="item-row" @click="handleView(row)">
          <span class="file-icon" :class="fileIconClass(row)">{{ fileIconText(row) }}</span>
          <div class="item-info">
            <p class="item-title" :title="row.title">{{ row.title || '未命名资源' }}</p>
            <span class="item-meta">
              <el-tag v-if="row.teachingType" size="small" effect="plain" :type="teachingTypeTagType(row.teachingType) as any">
                {{ row.teachingType }}
              </el-tag>
              <span v-if="row.subject" class="meta-text">{{ row.subject }}</span>
              <span v-if="row.stage || row.gradeName" class="meta-text">
                {{ row.stage || '' }}{{ row.gradeName ? ' · ' + row.gradeName : '' }}
              </span>
              <span class="meta-divider">|</span>
              <span class="meta-text">{{ formatViewTime(row.time) }} 浏览</span>
            </span>
          </div>
          <div class="item-actions" @click.stop>
            <el-tooltip content="查看详情" placement="top">
              <el-button size="small" circle class="act-btn" @click="handleView(row)">
                <el-icon><View /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="下载" placement="top">
              <el-button size="small" circle class="act-btn" @click="handleDownload(row)">
                <el-icon><Download /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="删除记录" placement="top">
              <el-button size="small" circle class="act-btn act-btn-danger" @click="removeItem(row.id)">
                <el-icon><Delete /></el-icon>
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
          @update:current-page="currentPage = $event"
          @update:page-size="pageSize = $event"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Search, Download, Delete, View } from '@element-plus/icons-vue'
import { useMyViews } from '@/composables/useMyViews'

const {
  activeSection,
  activeSubject,
  activeType,
  currentPage,
  pageSize,
  total,
  items,
  viewStats,
  sectionConfig,
  currentSubjects,
  currentTypes,
  searchKeyword,
  sortOrder,
  selectSection,
  selectSubject,
  selectType,
  setSearchKeyword,
  setSortOrder,
  removeItem,
  clearAll,
  formatViewTime,
  fileIconClass,
  fileIconText,
  teachingTypeTagType,
  handleView,
  handleDownload,
} = useMyViews()

function onSectionChange(val: string) {
  selectSection(val)
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
</script>

<style scoped>
.profile-views-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
  overflow: hidden;
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
.stat-card.stat-week {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
}
.stat-card.stat-today {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
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
  width: 100%;
  min-width: 0;
}
.filter-selects {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: nowrap;
  flex: 1;
  min-width: 0;
}
.filter-select {
  width: 104px;
  flex-shrink: 0;
}
.filter-select-sort {
  width: 100px;
}
.filter-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  margin-left: auto;
}
.search-input {
  width: 148px;
}
.clear-btn .el-icon {
  margin-right: 2px;
}
.clear-btn-text {
  font-size: 13px;
}

@media (max-width: 960px) {
  .filter-bar {
    flex-wrap: wrap;
  }
  .filter-selects {
    flex: 1 1 100%;
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
  cursor: pointer;
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


