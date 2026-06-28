<template>
  <div class="resource-list-grid" :class="[`layout-${layout}`, `theme-${theme}`]">
    <template v-if="layout === 'split'">
      <ul class="resource-column">
        <ResourceListRow
          v-for="(item, index) in leftItems"
          :key="'l-' + index"
          :item="item"
          :theme="theme"
          @click="$emit('select', item)"
        />
      </ul>
      <ul class="resource-column">
        <ResourceListRow
          v-for="(item, index) in rightItems"
          :key="'r-' + index"
          :item="item"
          :theme="theme"
          @click="$emit('select', item)"
        />
      </ul>
    </template>
    <template v-else>
      <ResourceListRow
        v-for="(item, index) in items"
        :key="index"
        :item="item"
        :theme="theme"
        tag="div"
        @click="$emit('select', item)"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ResourceListRow from './ResourceListRow.vue'
import type { ResourceListItem, ModuleTheme } from './types'

const props = withDefaults(
  defineProps<{
    items: ResourceListItem[]
    theme?: ModuleTheme
    layout?: 'grid' | 'split'
    maxItems?: number
  }>(),
  {
    theme: 'orange',
    layout: 'grid',
    maxItems: 14,
  }
)

defineEmits<{
  select: [item: ResourceListItem]
}>()

const displayItems = computed(() => props.items.slice(0, props.maxItems))

const leftItems = computed(() => {
  const half = Math.ceil(displayItems.value.length / 2)
  return displayItems.value.slice(0, half)
})

const rightItems = computed(() => {
  const half = Math.ceil(displayItems.value.length / 2)
  return displayItems.value.slice(half)
})
</script>

<style scoped>
.resource-list-grid.layout-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  column-gap: 40px;
  row-gap: 14px;
}

.resource-list-grid.layout-split {
  display: flex;
  gap: 48px;
}

.resource-column {
  flex: 1;
  min-width: 0;
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

@media (max-width: 1024px) {
  .resource-list-grid.layout-grid,
  .resource-list-grid.layout-split {
    display: flex;
    flex-direction: column;
    gap: 14px;
  }
}
</style>
