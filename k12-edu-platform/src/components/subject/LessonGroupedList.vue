<template>
  <div v-if="resourceMode === 'single'" class="lesson-grouped-list">
    <div class="list-toolbar">
      <span class="toolbar-label">本课资源</span>
      <span class="result-count-pill">
        共 <em>{{ total }}</em> 条本课资源
      </span>
    </div>

    <div v-if="loading" class="resource-skeleton-list">
      <div v-for="i in 4" :key="i" class="resource-skeleton-card">
        <div class="skeleton-line skeleton-title" />
        <div class="skeleton-line skeleton-meta" />
      </div>
    </div>

    <div v-else-if="!groups.length" class="empty-state">
      <p>暂无本课资源</p>
      <p class="empty-hint">可切换资源类型或调整册别/版本后重试</p>
    </div>

    <!-- 分组模式（显示类型标题）：组数少，普通渲染 -->
    <template v-else-if="showGroupHeaders">
      <section
        v-for="group in groups"
        :key="group.type"
        class="type-group"
      >
        <header class="group-header">
          <span class="group-icon">{{ typeIcon(group.type) }}</span>
          <h3 class="group-title">{{ group.type }}</h3>
          <span class="group-count">{{ group.items.length }} 份</span>
        </header>

        <!-- 组内项目用虚拟列表（当单组超过 30 项时） -->
        <template v-if="group.items.length > 30">
          <div class="resource-cards virtual-cards">
            <VirtualList
              :items="group.items"
              :estimated-item-height="90"
              :item-key="(item: any) => item.id"
              :container-height="Math.min(group.items.length * 90, 540)"
              :overscan="3"
            >
              <template #item="{ item }">
                <div
                  class="resource-card"
                  @click="$emit('open-resource', item)"
                >
                  <div class="resource-header">
                    <span
                      class="format-badge"
                      :class="getFormatInfo(item).className"
                      :title="getFormatInfo(item).label"
                    >{{ getFormatInfo(item).icon }}</span>
                    <span class="type-tag">{{ item.type || group.type }}</span>
                    <span class="resource-title">{{ item.title }}</span>
                  </div>
                  <div class="resource-footer">
                    <span class="format-label">{{ getFormatInfo(item).label }}</span>
                    <span class="publish-date">{{ formatDate(item.uploadTime) }}</span>
                    <span class="download-count">下载 {{ item.downloadCount || 0 }}</span>
                  </div>
                </div>
              </template>
            </VirtualList>
          </div>
        </template>
        <template v-else>
          <div class="resource-cards">
            <div
              v-for="item in group.items"
              :key="item.id"
              class="resource-card"
              @click="$emit('open-resource', item)"
            >
              <div class="resource-header">
                <span
                  class="format-badge"
                  :class="getFormatInfo(item).className"
                  :title="getFormatInfo(item).label"
                >{{ getFormatInfo(item).icon }}</span>
                <span class="type-tag">{{ item.type || group.type }}</span>
                <span class="resource-title">{{ item.title }}</span>
              </div>
              <div class="resource-footer">
                <span class="format-label">{{ getFormatInfo(item).label }}</span>
                <span class="publish-date">{{ formatDate(item.uploadTime) }}</span>
                <span class="download-count">下载 {{ item.downloadCount || 0 }}</span>
              </div>
            </div>
          </div>
        </template>
      </section>
    </template>

    <!-- 单一类型模式（不显示组标题）：使用虚拟列表提升大数据量性能 -->
    <template v-else>
      <div class="resource-cards virtual-cards">
        <VirtualList
          :items="flatItems"
          :estimated-item-height="90"
          :item-key="(item: any) => item.id"
          :container-height="Math.min(flatItems.length * 90, 800)"
          :overscan="5"
        >
          <template #item="{ item }">
            <div
              class="resource-card"
              @click="$emit('open-resource', item)"
            >
              <div class="resource-header">
                <span
                  class="format-badge"
                  :class="getFormatInfo(item).className"
                  :title="getFormatInfo(item).label"
                >{{ getFormatInfo(item).icon }}</span>
                <span class="type-tag">{{ item.type || groups[0]?.type }}</span>
                <span class="resource-title">{{ item.title }}</span>
              </div>
              <div class="resource-footer">
                <span class="format-label">{{ getFormatInfo(item).label }}</span>
                <span class="publish-date">{{ formatDate(item.uploadTime) }}</span>
                <span class="download-count">下载 {{ item.downloadCount || 0 }}</span>
              </div>
            </div>
          </template>
        </VirtualList>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { LESSON_TYPE_ICONS, type LessonResourceGroup } from '@/composables/useLessonHub'
import { getFileFormatInfo, inferFormatFromType } from '@/utils/resourceFormat'
import VirtualList from '@/components/shared/VirtualList.vue'

const props = defineProps<{
  resourceMode: 'single' | 'suite'
  groups: LessonResourceGroup[]
  total: number
  loading: boolean
  activeType: string
}>()

defineEmits<{
  'open-resource': [item: { id: number }]
}>()

const showGroupHeaders = computed(() => props.activeType === '全部')

/** 单一类型模式：扁平化所有项目 */
const flatItems = computed(() => {
  return props.groups.flatMap((g) => g.items)
})

function typeIcon(type: string) {
  return LESSON_TYPE_ICONS[type] || '📎'
}

function getFormatInfo(item: { fileExt?: string; type?: string }) {
  const ext = item.fileExt || inferFormatFromType(item.type)
  return getFileFormatInfo(ext)
}

function formatDate(dt?: string) {
  if (!dt) return ''
  return dt.replace('T', ' ').substring(0, 16)
}
</script>

<style scoped>
.lesson-grouped-list {
  margin-top: 4px;
  padding: 0 20px 16px;
}

.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  padding: 11px 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #f4f8ff 100%);
  border: 1px solid #eef2f6;
  border-radius: 10px;
}

.toolbar-label {
  flex-shrink: 0;
  padding-left: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  letter-spacing: 0.02em;
  border-left: 3px solid #409eff;
}

.result-count-pill {
  flex-shrink: 0;
  margin-left: auto;
  padding: 5px 14px;
  font-size: 13px;
  color: #606266;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 20px;
  line-height: 1.4;
  letter-spacing: 0.04em;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.result-count-pill em {
  font-style: normal;
  font-weight: 600;
  font-size: 14px;
  color: #409eff;
  margin: 0 2px;
}
.type-group {
  margin-bottom: 20px;
}
.group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f2f6;
}
.group-icon {
  font-size: 18px;
}
.group-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  flex: 1;
}
.group-count {
  font-size: 12px;
  color: #909399;
}
.resource-cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.virtual-cards {
  gap: 0;
}
.resource-card {
  padding: 14px 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #eef2f6;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.resource-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #d9ecff;
}
.resource-header {
  display: flex;
  align-items: flex-start;
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
.type-tag {
  flex-shrink: 0;
  padding: 2px 8px;
  background: #ecf5ff;
  color: #409eff;
  font-size: 12px;
  border-radius: 4px;
}
.resource-title {
  font-size: 14px;
  line-height: 1.5;
  color: #303133;
  font-weight: 500;
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
}
.empty-state {
  text-align: center;
  padding: 40px;
  color: #909399;
}
.empty-hint {
  font-size: 12px;
  margin-top: 8px;
}
.resource-skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.resource-skeleton-card {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #eef2f6;
}
.skeleton-line {
  height: 14px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f2f3f5 25%, #e9ebef 50%, #f2f3f5 75%);
  background-size: 200% 100%;
  animation: shimmer 1.2s infinite;
}
.skeleton-title { width: 75%; margin-bottom: 8px; }
.skeleton-meta { width: 40%; }
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>
