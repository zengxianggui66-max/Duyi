<template>
  <div class="catalog-sidebar">
    <div class="catalog-header">
      <span class="catalog-title">目录</span>
      <span v-if="displayModeLabel" class="catalog-mode-tag">{{ displayModeLabel }}</span>
      <button type="button" class="collapse-all-btn" @click="$emit('toggle-all')">
        {{ allExpanded ? '折叠全部' : '展开全部' }}
      </button>
    </div>

    <div v-if="loading" class="catalog-skeleton">
      <div v-for="i in 8" :key="i" class="catalog-skeleton-row" :style="{ width: skeletonWidth(i) }" />
    </div>
    <div v-else-if="!nodes.length" class="catalog-empty">暂无目录，请切换册别或品牌</div>
    <div v-else class="catalog-tree">
      <CatalogTreeNode
        v-for="node in nodes"
        :key="node.id"
        :node="node"
        :depth="0"
        :active-node-id="activeNodeId"
        :is-expanded="isExpanded"
        @select="$emit('select', $event)"
        @toggle-expand="$emit('toggle-expand', $event)"
        @prefetch="$emit('prefetch', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { CatalogNode, DisplayMode } from '@/types/browse'
import CatalogTreeNode from './CatalogTreeNode.vue'

const props = defineProps<{
  nodes: CatalogNode[]
  activeNodeId: number | null
  loading?: boolean
  displayMode?: DisplayMode
  allExpanded?: boolean
  isExpanded: (nodeId: number) => boolean
}>()

defineEmits<{
  select: [nodeId: number]
  'toggle-expand': [nodeId: number]
  'toggle-all': []
  prefetch: [nodeId: number]
}>()

function skeletonWidth(i: number): string {
  const widths = ['72%', '58%', '85%', '64%', '78%', '55%', '90%', '62%']
  return widths[(i - 1) % widths.length]
}

const displayModeLabel = computed(() => {
  switch (props.displayMode) {
    case 'category_list':
      return '分类'
    case 'unit_matrix':
      return '单元'
    case 'lesson_hub':
      return '教材'
    default:
      return ''
  }
})
</script>

<script lang="ts">
export default { name: 'CatalogSidebar' }
</script>

<style scoped>
.catalog-sidebar {
  border-top: 1px solid #f0eeeb;
  padding-top: 12px;
}

.catalog-header {
  display: flex;
  align-items: center;
  gap: var(--space-sm, 8px);
  margin-bottom: 10px;
  font-size: var(--fs-body, 14px);
}

.catalog-title {
  font-weight: 600;
  color: var(--text-primary, #1A1A2E);
}

.catalog-mode-tag {
  font-size: var(--fs-caption, 12px);
  color: var(--color-brand-accent, #F5C542);
  background: #fff8e6;
  padding: 2px 8px;
  border-radius: 4px;
}

.collapse-all-btn {
  margin-left: auto;
  font-size: var(--fs-caption, 12px);
  color: var(--text-regular, #4A4A68);
  background: none;
  border: none;
  cursor: pointer;
  transition: color var(--transition-fast, 0.15s ease);
}

.collapse-all-btn:hover {
  color: var(--color-brand-accent, #F5C542);
}

.catalog-empty {
  padding: var(--space-md, 16px) var(--space-sm, 8px);
  font-size: var(--fs-body, 14px);
  color: var(--text-placeholder, #B0B0C0);
  text-align: center;
}

.catalog-skeleton {
  padding: var(--space-sm, 8px) 4px;
}

.catalog-skeleton-row {
  height: 14px;
  margin: 10px 8px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: catalog-shimmer 1.2s ease-in-out infinite;
}

@keyframes catalog-shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.catalog-tree {
  overflow-y: auto;
}
</style>
