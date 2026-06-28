<template>
  <div v-if="resourceMode === 'single'" class="single-resource-section">
    <!-- 批量操作栏（只有 action 按钮，toggle 按钮在 AdvancedFilterBar） -->
    <div v-if="batchMode && resources.length" class="batch-toolbar">
      <div class="batch-toolbar__actions">
        <button
          class="batch-action-btn batch-action-btn--select"
          @click="toggleSelectAll"
        >
          <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
            <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14z"/>
          </svg>
          {{ allSelected ? '取消全选' : '全选' }}
          <span class="sep">|</span>
          <span class="selected-hint">已选 {{ selectedCount }} 项</span>
        </button>
        <button
          class="batch-action-btn batch-action-btn--download"
          :disabled="selectedCount === 0"
          @click="onBatchDownload"
        >
          <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
            <path d="M5 20h14v-2H5v2zm7-18L5.33 9h3.84v4h3.66V9h3.84L12 2z"/>
          </svg>
          批量下载
        </button>
        <button
          class="batch-action-btn batch-action-btn--basket"
          :disabled="selectedCount === 0"
          @click="onBatchBasket"
        >
          <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
            <path d="M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49A1.003 1.003 0 0020 4H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z"/>
          </svg>
          加入资料篮
        </button>
        <button
          class="batch-action-btn batch-action-btn--favorite"
          :disabled="selectedCount === 0"
          @click="onBatchFavorite"
        >
          <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
            <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
          </svg>
          收藏
        </button>
      </div>
    </div>

    <div v-if="loading && !resources.length" class="resource-skeleton-list">
      <div v-for="i in 5" :key="i" class="resource-skeleton-card">
        <div class="skeleton-line skeleton-title" />
        <div class="skeleton-line skeleton-meta" />
        <div class="skeleton-line skeleton-footer" />
      </div>
    </div>

    <div v-else-if="!resources.length" class="empty-state">
      <p class="empty-title">暂无匹配的资源</p>
      <p v-if="browseSummary" class="empty-hint">当前筛选：{{ browseSummary }}</p>
      <p v-if="placementHint" class="empty-hint">{{ placementHint }}</p>
      <p v-else class="empty-hint">请切换目录节点、资源类型或放宽筛选条件</p>
      <button
        v-if="showUploadAction"
        type="button"
        class="empty-upload-btn"
        @click="$emit('upload')"
      >
        <span class="empty-upload-icon" aria-hidden="true">⬆</span>
        {{ uploadLabel }}
      </button>
    </div>

    <div v-else class="resource-cards">
      <div
        v-for="item in resources"
        :key="item.id"
        :class="['resource-card', { 'is-selected': selectedIds.has(item.id), 'batch-mode': batchMode }]"
        @click="onCardClick(item)"
      >
        <!-- 批量模式复选框 -->
        <div v-if="batchMode" class="resource-card__checkbox" @click.stop>
          <label class="checkbox-wrap">
            <input
              type="checkbox"
              :checked="selectedIds.has(item.id)"
              @change="toggleSelect(item)"
            />
            <span class="checkbox-visual" />
          </label>
        </div>

        <div class="resource-card__body">
          <div class="resource-header">
            <span
              class="format-badge"
              :class="getFormatInfo(item).className"
              :title="getFormatInfo(item).label"
            >{{ getFormatInfo(item).icon }}</span>
            <span class="premium-tag" v-if="item.fileSizeKb && item.fileSizeKb > 1000">精</span>
            <span class="type-tag">{{ item.type || '教案' }}</span>
            <span class="resource-title">{{ item.title }}</span>
          </div>
          <div class="resource-meta-row">
            <span class="meta-item">
              📁 {{ item.unitName || '' }}
              <template v-if="item.gradeName"> · {{ item.gradeName }}</template>
              <template v-if="item.edition"> · {{ item.edition }}</template>
            </span>
          </div>
          <div class="resource-footer">
            <span class="format-label">{{ getFormatInfo(item).label }}</span>
            <span class="publish-date">{{ formatDate(item.uploadTime) }}</span>
            <span class="download-count">下载 {{ item.downloadCount || 0 }} 次</span>
            <span class="file-size">{{ item.fileSizeKb ? formatSize(item.fileSizeKb) : '' }}</span>
          </div>
        </div>

        <!-- 操作按钮区（非批量模式 hover 显示） -->
        <div v-if="!batchMode" class="resource-card__actions" @click.stop>
          <button
            class="action-btn action-btn--download"
            title="立即下载"
            @click="onDownload(item)"
          >
            <svg class="action-btn__icon" viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M5 20h14v-2H5v2zm7-18L5.33 9h3.84v4h3.66V9h3.84L12 2z"/>
            </svg>
            <span>立即下载</span>
          </button>
          <button
            class="action-btn action-btn--basket"
            title="加入资料篮"
            @click="onAddToBasket(item)"
          >
            <svg class="action-btn__icon" viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49A1.003 1.003 0 0020 4H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z"/>
            </svg>
            <span>+资料篮</span>
          </button>
          <button
            class="action-btn action-btn--favorite"
            :class="{ 'is-favorited': item._favorited }"
            :title="item._favorited ? '取消收藏' : '收藏'"
            @click="onToggleFavorite(item)"
          >
            <svg class="action-btn__icon" viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <AppPagination
      :currentPage="currentPage"
      :totalPages="totalPages"
      :totalCount="total"
      :pageSize="pageSize"
      :visiblePages="visiblePages"
      @update:currentPage="(p: number) => $emit('update:currentPage', p)"
      @update:pageSize="(s: number) => $emit('update:pageSize', s)"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { getFileFormatInfo, inferFormatFromType } from '@/utils/resourceFormat'
import AppPagination from '@/components/shared/AppPagination.vue'
import { ElMessage } from 'element-plus'

const props = withDefaults(
  defineProps<{
    resourceMode: 'single' | 'suite'
    resources: any[]
    total: number
    loading: boolean
    currentPage: number
    pageSize: number
    totalPages: number
    visiblePages: (number | string)[]
    browseSummary?: string
    showUploadAction?: boolean
    uploadLabel?: string
    placementHint?: string
    batchMode: boolean
  }>(),
  {
    showUploadAction: false,
    uploadLabel: '上传资源',
    pageSize: 10,
    batchMode: false,
  },
)

const emit = defineEmits<{
  'open-resource': [item: any]
  'update:currentPage': [value: number]
  'update:pageSize': [value: number]
  'download-resource': [item: any]
  'add-to-basket': [item: any]
  'toggle-favorite': [item: any]
  upload: []
  'batch-download': [items: any[]]
  'batch-add-to-basket': [items: any[]]
  'batch-favorite': [items: any[]]
}>()

// ===== 批量操作 =====
const selectedIds = ref<Set<number | string>>(new Set())

const allSelected = computed(() =>
  props.resources.length > 0 && selectedIds.value.size === props.resources.length,
)

const selectedCount = computed(() => selectedIds.value.size)

// batchMode 关闭时清空选中
watch(
  () => props.batchMode,
  (val) => {
    if (!val) selectedIds.value.clear()
  },
)

function toggleSelect(item: any) {
  const id = item.id
  if (selectedIds.value.has(id)) {
    selectedIds.value.delete(id)
  } else {
    selectedIds.value.add(id)
  }
  selectedIds.value = new Set(selectedIds.value)
}

function toggleSelectAll() {
  if (allSelected.value) {
    selectedIds.value.clear()
  } else {
    selectedIds.value = new Set(props.resources.map((r) => r.id))
  }
}

function onCardClick(item: any) {
  if (props.batchMode) {
    toggleSelect(item)
  } else {
    emit('open-resource', item)
  }
}

function getSelectedItems(): any[] {
  return props.resources.filter((r) => selectedIds.value.has(r.id))
}

function onBatchDownload() {
  const items = getSelectedItems()
  if (!items.length) return
  emit('batch-download', items)
}

function onBatchBasket() {
  const items = getSelectedItems()
  if (!items.length) return
  emit('batch-add-to-basket', items)
  selectedIds.value.clear()
}

function onBatchFavorite() {
  const items = getSelectedItems()
  if (!items.length) return
  emit('batch-favorite', items)
  selectedIds.value.clear()
}

// 翻页时清除选中状态
watch(
  () => [props.currentPage, props.resources],
  () => {
    if (props.batchMode) {
      selectedIds.value.clear()
    }
  },
)

// ===== 原有方法 =====
function getFormatInfo(item: { fileExt?: string; type?: string }) {
  const ext = item.fileExt || inferFormatFromType(item.type)
  return getFileFormatInfo(ext)
}

function formatDate(dt: any): string {
  if (!dt) return ''
  if (typeof dt === 'string') {
    return dt.replace('T', ' ').substring(0, 16)
  }
  try {
    return new Date(dt).toLocaleDateString('zh-CN') + ' ' + new Date(dt).toTimeString().substring(0, 5)
  } catch {
    return String(dt)
  }
}

function formatSize(kb: number): string {
  if (!kb) return ''
  if (kb >= 1024) return (kb / 1024).toFixed(1) + 'MB'
  return kb + 'KB'
}

function onDownload(item: any) {
  emit('download-resource', item)
}

function onAddToBasket(item: any) {
  emit('add-to-basket', item)
}

function onToggleFavorite(item: any) {
  emit('toggle-favorite', item)
}
</script>

<style scoped>
.single-resource-section {
  margin-top: 4px;
  padding: 0 20px 20px;
}

/* ===== 批量工具栏 ===== */
.batch-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0 12px;
  border-bottom: 1px solid #f0eeeb;
  margin-bottom: 12px;
}



.batch-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.batch-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  border: 1px solid var(--border-color, #E8ECF4);
  border-radius: var(--radius-round, 999px);
  background: var(--bg-card, #fff);
  font-size: 12px;
  color: var(--text-regular, #4A4A68);
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
  white-space: nowrap;
}

.batch-action-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.batch-action-btn--download:hover:not(:disabled) {
  border-color: var(--color-primary, #4361EE);
  color: var(--color-primary, #4361EE);
  background: var(--color-primary-bg, #EBF0FF);
}

.batch-action-btn--basket:hover:not(:disabled) {
  border-color: #e6a23c;
  color: #e6a23c;
  background: #fdf6ec;
}

.batch-action-btn--favorite:hover:not(:disabled) {
  border-color: #f56c6c;
  color: #f56c6c;
  background: #fef0f0;
}

.batch-action-btn--select:hover {
  background: #f5f7fa;
  border-color: #c0c4cc;
}

.sep {
  color: var(--border-color, #E8ECF4);
  margin: 0 2px;
}

.selected-hint {
  color: var(--color-primary, #4361EE);
  font-weight: 500;
}

/* ===== 骨架屏 ===== */
.resource-skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.resource-skeleton-card {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
.skeleton-line {
  height: 14px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f2f3f5 25%, #e9ebef 50%, #f2f3f5 75%);
  background-size: 200% 100%;
  animation: shimmer 1.2s infinite;
}
.skeleton-title {
  width: 70%;
  margin-bottom: 10px;
}
.skeleton-meta {
  width: 45%;
  margin-bottom: 8px;
}
.skeleton-footer {
  width: 30%;
}
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ===== 空状态 ===== */
.empty-state {
  text-align: center;
  padding: 40px 16px;
  color: #909399;
  font-size: 14px;
}
.empty-title {
  font-size: 15px;
  color: #606266;
  font-weight: 500;
  margin: 0 0 8px;
}
.empty-hint {
  font-size: 12px;
  color: #c0c4cc;
  margin: 8px 0 0;
}
.empty-upload-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 20px;
  padding: 10px 22px;
  border: none;
  border-radius: 6px;
  background: linear-gradient(135deg, #ff8c42, #ff6b35);
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.35);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.empty-upload-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.45);
}
.empty-upload-icon {
  font-size: 15px;
  line-height: 1;
}

/* ===== 资源卡片 ===== */
.resource-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.resource-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.2s;
}
.resource-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  border-color: #d0d5dd;
}
.resource-card.batch-mode:hover {
  border-color: var(--color-primary, #4361EE);
  box-shadow: 0 0 0 2px rgba(67, 97, 238, 0.12);
}
.resource-card.is-selected {
  border-color: var(--color-primary, #4361EE);
  background: #f8faff;
  box-shadow: 0 0 0 2px rgba(67, 97, 238, 0.12);
}

/* 复选框 */
.resource-card__checkbox {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.checkbox-wrap {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  cursor: pointer;
}
.checkbox-wrap input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}
.checkbox-visual {
  width: 18px;
  height: 18px;
  border: 2px solid #c0c4cc;
  border-radius: 4px;
  background: #fff;
  transition: all 0.15s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}
.checkbox-wrap input:checked + .checkbox-visual {
  background: var(--color-primary, #4361EE);
  border-color: var(--color-primary, #4361EE);
}
.checkbox-wrap input:checked + .checkbox-visual::after {
  content: '';
  width: 5px;
  height: 9px;
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
  margin-top: -1px;
}
.checkbox-wrap:hover .checkbox-visual {
  border-color: var(--color-primary, #4361EE);
}

.resource-card__body {
  flex: 1;
  min-width: 0;
}

.resource-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.format-badge {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 14px;
}
.format-ppt { background: #fff3e0; }
.format-pdf { background: #ffebee; }
.format-word { background: #e3f2fd; }
.format-excel { background: #e8f5e9; }
.format-zip { background: #f3e5f5; }
.format-video { background: #e0f7fa; }
.format-audio { background: #fce4ec; }
.format-default { background: #f5f7fa; }
.premium-tag {
  padding: 2px 6px;
  background: #f56c6c;
  color: #fff;
  font-size: 11px;
  border-radius: 4px;
}
.type-tag {
  padding: 2px 8px;
  background: #ecf5ff;
  color: #409eff;
  font-size: 12px;
  border-radius: 4px;
  flex-shrink: 0;
}
.resource-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  flex: 1;
  line-height: 1.4;
  cursor: pointer;
  transition: color 0.15s ease;
}
.resource-card:hover .resource-title:not(.batch-mode *) {
  color: #409eff;
}
.resource-meta-row {
  margin-bottom: 8px;
}
.meta-item {
  font-size: 13px;
  color: #606266;
}
.resource-footer {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}
.format-label {
  color: #409eff;
  font-weight: 500;
}

/* ===== 操作按钮区（非批量 hover 显示） ===== */
.resource-card__actions {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  opacity: 1;
  transition: opacity 0.2s, transform 0.2s;
}
.resource-card:hover .resource-card__actions {
  transform: translateY(-1px);
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 16px;
  background: #fff;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.15s ease;
  user-select: none;
  line-height: 1;
}
.action-btn:hover {
  background: #f0f6ff;
  border-color: #b3d4ff;
}
.action-btn__icon {
  flex-shrink: 0;
}
.action-btn--download:hover {
  color: #409eff;
  border-color: #409eff;
  background: #ecf5ff;
}
.action-btn--basket:hover {
  color: #e6a23c;
  border-color: #e6a23c;
  background: #fdf6ec;
}
.action-btn--favorite:hover {
  color: #f56c6c;
  border-color: #f56c6c;
  background: #fef0f0;
}
.action-btn--favorite.is-favorited {
  color: #f56c6c;
  border-color: #f56c6c;
  background: #fef0f0;
}
</style>
