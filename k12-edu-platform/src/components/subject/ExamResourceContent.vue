<template>
  <!-- Exam 模式资源列表区（筛选已并入顶部 browse-filter-panel） -->
  <div class="exam-section">
    <!-- 模式切换 + 排序 -->
    <div class="exam-toolbar">
      <button :class="['mode-btn', { active: resourceMode === 'single' }]" @click="$emit('update:resourceMode', 'single')">找单份</button>
      <button :class="['mode-btn', { active: resourceMode === 'suite' }]" @click="$emit('update:resourceMode', 'suite')">找成套</button>
      <span :class="['sort-btn', { active: sortType === 'comprehensive' }]" @click="$emit('update:sortType', 'comprehensive')">综合</span>
      <span :class="['sort-btn', { active: sortType === 'latest' }]" @click="$emit('update:sortType', 'latest')">最新</span>
      <span class="result-count">共 {{ total }} 条结果</span>
    </div>

    <!-- 骨架加载 -->
    <div v-if="loading" class="topic-skeleton-list exam-skeleton">
      <div v-for="i in 5" :key="i" class="topic-skeleton-row" />
    </div>

    <!-- 空状态 -->
    <EmptyState
      v-else-if="!resources.length"
      :description="browseSummary ? `当前筛选：${browseSummary}` : undefined"
      compact
    />

    <!-- 资源列表 -->
    <div v-else class="exam-resource-list">
      <div v-for="item in resources" :key="item.id" class="exam-resource-item" @click="$emit('open-resource', item)">
        <div :class="'doc-icon-box ' + getDocIconClass(item)">{{ getDocIconLetter(item) }}</div>
        <div class="exam-resource-info">
          <div class="exam-resource-title">{{ item.title }}</div>
          <div class="exam-resource-meta">
            <span>{{ (item.uploadTime || '').substring(0, 10) }}</span><span>|</span>
            <span>{{ item.downloadCount || 0 }}次下载</span><span>|</span>
            <span>{{ item.uploader || '教学网资源' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <AppPagination
      :currentPage="currentPage"
      :totalPages="totalPages"
      :totalCount="total"
      :visiblePages="visiblePages"
      @update:currentPage="$emit('update:currentPage', $event)"
    />
  </div>
</template>

<script setup lang="ts">
import EmptyState from '@/components/shared/EmptyState.vue'
import AppPagination from '@/components/shared/AppPagination.vue'

defineProps<{
  resources: any[]
  total: number
  loading: boolean
  currentPage: number
  totalPages: number
  visiblePages: (number | string)[]
  resourceMode: 'single' | 'suite'
  sortType: string
  browseSummary?: string
  getDocIconClass: (item: any) => string
  getDocIconLetter: (item: any) => string
}>()

defineEmits<{
  'update:resourceMode': [value: 'single' | 'suite']
  'update:sortType': [value: string]
  'update:currentPage': [value: number]
  'open-resource': [item: any]
}>()
</script>

<style scoped>
/* 考试布局工具栏（找单份/找成套/综合/最新） */
.exam-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.mode-btn {
  padding: 5px 16px;
  border-radius: 16px;
  background: #fff;
  color: #5D4E37;
  border: 1px solid #E0D9D0;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.mode-btn.active { background: var(--color-primary, #4361EE); color: #fff; border-color: transparent; }
.sort-btn {
  padding: 4px 12px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: color 0.2s;
}
.sort-btn.active { color: var(--color-primary, #4361EE); font-weight: 600; }
.result-count {
  margin-left: auto;
  font-size: 13px;
  color: #999;
}

/* 骨架加载 */
.topic-skeleton-list { padding: 8px 0; }
.topic-skeleton-row {
  height: 56px;
  margin: 0 0 10px;
  border-radius: 8px;
  background: linear-gradient(90deg, #f5f5f5 25%, #ebebeb 50%, #f5f5f5 75%);
  background-size: 200% 100%;
  animation: topic-shimmer 1.2s ease-in-out infinite;
}
.exam-skeleton .topic-skeleton-row {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}
@keyframes topic-shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* 资源列表 */
.exam-resource-list {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
  overflow: hidden;
}
.exam-resource-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 20px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.15s;
}
.exam-resource-item:last-child { border-bottom: none; }
.exam-resource-item:hover { background: #fafbfc; }
.exam-resource-info { flex: 1; min-width: 0; }
.exam-resource-title { font-size: 15px; font-weight: 500; color: #333; line-height: 1.4; }
.exam-resource-meta {
  display: flex; gap: 4px;
  font-size: 12px; color: #999;
  margin-top: 4px; flex-wrap: wrap;
}

/* 文档图标 */
.doc-icon-box {
  display: inline-flex; align-items: center; justify-content: center;
  width: 32px; height: 32px; border-radius: 6px; font-size: 10px; font-weight: 700; flex-shrink: 0;
}
.doc-icon-box.icon-pdf-box { background: #C0392B; color: #fff; }
.doc-icon-box.icon-ppt-box { background: #D35400; color: #fff; }
.doc-icon-box.icon-word-box { background: #2B5797; color: #fff; }
</style>
