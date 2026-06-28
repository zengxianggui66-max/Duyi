<template>
  <component
    :is="tag"
    class="resource-list-row"
    :class="[`theme-${theme}`, { clickable }]"
    @click="onClick"
  >
    <span class="dot">&bull;</span>
    <span class="name">{{ item.title }}</span>
    <span v-if="item.date" class="date">{{ item.date }}</span>
  </component>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ResourceListItem, ModuleTheme } from './types'

const props = withDefaults(
  defineProps<{
    item: ResourceListItem
    theme?: ModuleTheme
    tag?: 'li' | 'div'
    clickable?: boolean
  }>(),
  {
    theme: 'orange',
    tag: 'li',
    clickable: true,
  }
)

const emit = defineEmits<{
  click: [item: ResourceListItem]
}>()

const tag = computed(() => (props.tag === 'div' ? 'div' : 'li'))

function onClick() {
  if (props.clickable) {
    emit('click', props.item)
  }
}
</script>

<style scoped>
.resource-list-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  min-width: 0;
  list-style: none;
}

.resource-list-row.clickable {
  cursor: pointer;
}

.theme-orange {
  --row-hover: #ff6b00;
}

.theme-blue {
  --row-hover: #1890ff;
}

.theme-green {
  --row-hover: #52c41a;
}

.resource-list-row.clickable:hover .name {
  color: var(--row-hover);
}

.dot {
  color: #ccc;
  font-size: 12px;
  flex-shrink: 0;
  line-height: 1.6;
}

.name {
  flex: 1;
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
}

.date {
  font-size: 13px;
  color: #bbb;
  flex-shrink: 0;
  white-space: nowrap;
  margin-left: 8px;
}
</style>
