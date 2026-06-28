<template>
  <div class="tree-node-wrap">
    <div
      :class="['tree-row', { active: activeNodeId === node.id }]"
      :style="{ paddingLeft: depth * 14 + 8 + 'px' }"
      :data-catalog-node-id="node.id"
      @mouseenter="onHover"
    >
      <span
        v-if="hasChildren"
        class="expand-icon"
        @click.stop="$emit('toggle-expand', node.id)"
      >{{ expanded ? '∨' : '⊕' }}</span>
      <span v-else class="expand-icon expand-icon--placeholder" aria-hidden="true"></span>
      <span class="node-name" @click.stop="$emit('select', node.id)">{{ node.name }}</span>
      <span v-if="node.nodeType === 'leaf'" class="node-type-tag">资源</span>
    </div>
    <div v-if="expanded && hasChildren" class="tree-children">
      <CatalogTreeNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :depth="depth + 1"
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
import type { CatalogNode } from '@/types/browse'

const props = defineProps<{
  node: CatalogNode
  depth: number
  activeNodeId: number | null
  isExpanded: (nodeId: number) => boolean
}>()

const emit = defineEmits<{
  select: [nodeId: number]
  'toggle-expand': [nodeId: number]
  prefetch: [nodeId: number]
}>()

const hasChildren = computed(() => (props.node.children?.length ?? 0) > 0)
const expanded = computed(() => props.isExpanded(props.node.id))

let hoverTimer: ReturnType<typeof setTimeout> | null = null

function onHover() {
  if (hoverTimer) clearTimeout(hoverTimer)
  hoverTimer = setTimeout(() => {
    emit('prefetch', props.node.id)
  }, 200)
}
</script>

<script lang="ts">
export default { name: 'CatalogTreeNode' }
</script>

<style scoped>
.tree-row {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 8px 8px 0;
  cursor: pointer;
  border-radius: 6px;
  font-size: 14px;
  color: var(--text-regular, #4A4A68);
  line-height: 1.4;
  transition: background var(--transition-fast, 0.15s ease);
}

.tree-row:hover {
  background: #f5f5f0;
}

.tree-row.active {
  background: linear-gradient(90deg, #faf3d6 0%, #fffbe8 100%);
  color: #b8860b;
  font-weight: 500;
}

.tree-children {
  overflow: hidden;
  transition: max-height var(--transition-collapse, 0.25s ease),
              opacity var(--transition-collapse, 0.25s ease);
}

.expand-icon {
  flex-shrink: 0;
  width: 18px;
  text-align: center;
  font-size: 12px;
  color: #999;
  cursor: pointer;
  transition: transform var(--transition-fast, 0.15s ease);
}

.expand-icon--placeholder {
  visibility: hidden;
}

.node-name {
  flex: 1;
  min-width: 0;
  word-break: break-word;
}

.node-type-tag {
  flex-shrink: 0;
  font-size: 11px;
  color: #888;
  background: #f5f5f5;
  padding: 1px 6px;
  border-radius: 3px;
}
</style>
