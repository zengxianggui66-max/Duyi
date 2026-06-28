<template>
  <el-drawer
    :model-value="visible"
    :title="suite?.title || '成套资源'"
    direction="rtl"
    size="520px"
    class="suite-detail-drawer"
    @update:model-value="(v: boolean) => !v && $emit('close')"
  >
    <template v-if="suite">
      <div class="drawer-summary">
        <div class="drawer-summary__icon">{{ suite.icon }}</div>
        <div class="drawer-summary__text">
          <p class="drawer-summary__sub">{{ suite.sub }}</p>
          <p class="drawer-summary__meta">
            共 {{ items.length }} 份
            <template v-if="suite.updateTime"> · 更新 {{ suite.updateTime }}</template>
          </p>
        </div>
        <span v-if="suite.tag" class="drawer-summary__tag">{{ suite.tag }}</span>
      </div>

      <div class="drawer-toolbar">
        <label class="select-all">
          <input
            type="checkbox"
            :checked="allSelected"
            @change="toggleSelectAll"
          />
          <span>全选</span>
          <span class="selected-count">已选 {{ selectedIds.size }} 项</span>
        </label>
        <div class="drawer-toolbar__actions">
          <button
            type="button"
            class="toolbar-btn toolbar-btn--primary"
            :disabled="!selectedIds.size || acting"
            @click="onBatchDownload"
          >
            批量下载
          </button>
          <button
            type="button"
            class="toolbar-btn"
            :disabled="!selectedIds.size || acting"
            @click="onBatchBasket"
          >
            加入资料篮
          </button>
        </div>
      </div>

      <div v-if="!items.length" class="drawer-empty">该成套暂无资源明细</div>

      <ul v-else class="item-list">
        <li
          v-for="item in items"
          :key="item.id"
          class="item-row"
          :class="{ 'is-selected': selectedIds.has(item.id) }"
        >
          <label class="item-row__check" @click.stop>
            <input
              type="checkbox"
              :checked="selectedIds.has(item.id)"
              @change="toggleItem(item.id)"
            />
          </label>
          <div class="item-row__body" @click="toggleItem(item.id)">
            <div class="item-row__title">
              <span class="format-badge">{{ formatIcon(item) }}</span>
              <span class="type-tag">{{ item.type || '资源' }}</span>
              <span class="title-text">{{ item.title }}</span>
            </div>
            <div class="item-row__meta">
              <span v-if="item.lessonName">{{ item.lessonName }}</span>
              <span v-if="item.fileSizeKb">{{ formatSize(item.fileSizeKb) }}</span>
              <span>下载 {{ item.downloadCount || 0 }}</span>
            </div>
          </div>
          <div class="item-row__actions">
            <button type="button" class="mini-btn" title="下载" @click="onDownloadOne(item)">↓</button>
            <button type="button" class="mini-btn" title="资料篮" @click="onBasketOne(item)">+</button>
            <button type="button" class="mini-btn" title="查看详情" @click="$emit('open-resource', item)">›</button>
          </div>
        </li>
      </ul>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { PrimaryChineseItem, ResourceSuite } from '@/api/types'
import { useSuiteResourceActions } from '@/composables/useSuiteResourceActions'
import { getFileFormatInfo, inferFormatFromType } from '@/utils/resourceFormat'

const props = defineProps<{
  visible: boolean
  suite: ResourceSuite | null
}>()

defineEmits<{
  close: []
  'open-resource': [item: PrimaryChineseItem]
}>()

const { downloadResource, downloadResources, addResourcesToBasket } = useSuiteResourceActions()

const selectedIds = ref<Set<number>>(new Set())
const acting = ref(false)

const items = computed(() => props.suite?.items || [])

const allSelected = computed(
  () => items.value.length > 0 && selectedIds.value.size === items.value.length,
)

watch(
  () => [props.visible, props.suite?.key] as const,
  ([visible]) => {
    if (visible && items.value.length) {
      selectedIds.value = new Set(items.value.map((i) => i.id))
    } else if (!visible) {
      selectedIds.value = new Set()
    }
  },
)

function getSelectedItems(): PrimaryChineseItem[] {
  return items.value.filter((i) => selectedIds.value.has(i.id))
}

function toggleItem(id: number) {
  const next = new Set(selectedIds.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  selectedIds.value = next
}

function toggleSelectAll() {
  if (allSelected.value) {
    selectedIds.value = new Set()
  } else {
    selectedIds.value = new Set(items.value.map((i) => i.id))
  }
}

function formatIcon(item: PrimaryChineseItem) {
  return getFileFormatInfo(item.fileExt || inferFormatFromType(item.type)).icon
}

function formatSize(kb: number) {
  if (kb >= 1024) return `${(kb / 1024).toFixed(1)}MB`
  return `${kb}KB`
}

function onDownloadOne(item: PrimaryChineseItem) {
  if (!downloadResource(item)) {
    ElMessage.warning('暂无下载链接')
  }
}

async function onBasketOne(item: PrimaryChineseItem) {
  acting.value = true
  try {
    await addResourcesToBasket([item])
  } finally {
    acting.value = false
  }
}

function onBatchDownload() {
  downloadResources(getSelectedItems())
}

async function onBatchBasket() {
  acting.value = true
  try {
    await addResourcesToBasket(getSelectedItems())
  } finally {
    acting.value = false
  }
}
</script>

<style scoped>
.drawer-summary {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 0 0 16px;
  border-bottom: 1px solid #f0eeeb;
  margin-bottom: 16px;
}

.drawer-summary__icon {
  font-size: 36px;
  line-height: 1;
}

.drawer-summary__sub {
  margin: 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

.drawer-summary__meta {
  margin: 6px 0 0;
  font-size: 12px;
  color: #909399;
}

.drawer-summary__tag {
  margin-left: auto;
  flex-shrink: 0;
  padding: 2px 8px;
  background: #fef0f0;
  color: #f56c6c;
  font-size: 11px;
  border-radius: 4px;
}

.drawer-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.select-all {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  user-select: none;
}

.selected-count {
  color: #909399;
  font-size: 12px;
}

.drawer-toolbar__actions {
  display: flex;
  gap: 8px;
}

.toolbar-btn {
  padding: 6px 14px;
  border: 1px solid #dcdfe6;
  border-radius: 16px;
  background: #fff;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
}

.toolbar-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.toolbar-btn--primary {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
}

.toolbar-btn--primary:hover:not(:disabled) {
  background: #66b1ff;
}

.drawer-empty {
  padding: 40px 0;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.item-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.item-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 8px;
  border-radius: 8px;
  border: 1px solid transparent;
}

.item-row:hover,
.item-row.is-selected {
  background: #f5f7fa;
  border-color: #e8ecf4;
}

.item-row__check {
  padding-top: 4px;
  cursor: pointer;
}

.item-row__body {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.item-row__title {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  flex-wrap: wrap;
}

.format-badge {
  flex-shrink: 0;
}

.type-tag {
  flex-shrink: 0;
  font-size: 11px;
  padding: 1px 6px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
}

.title-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.4;
  word-break: break-word;
}

.item-row__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}

.item-row__actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-shrink: 0;
}

.mini-btn {
  width: 28px;
  height: 28px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
}

.mini-btn:hover {
  border-color: #409eff;
  color: #409eff;
}
</style>
