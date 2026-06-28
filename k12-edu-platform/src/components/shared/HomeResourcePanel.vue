<template>
  <section class="home-resource-panel" :class="`theme-${theme}`">
    <ModuleTopTabs
      :title="title"
      :tabs="tabs"
      :model-value="tabModel"
      :theme="theme"
      @update:model-value="$emit('update:tabModel', $event)"
    />

    <div class="content-wrap">
      <aside v-if="$slots.sidebar" class="panel-sidebar">
        <slot name="sidebar" />
      </aside>

      <div class="panel-main">
        <slot>
          <ResourceListGrid
            :items="items"
            :theme="theme"
            :layout="listLayout"
            :max-items="maxItems"
            @select="$emit('select', $event)"
          />
        </slot>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import ModuleTopTabs from './ModuleTopTabs.vue'
import ResourceListGrid from './ResourceListGrid.vue'
import type { ModuleTab, ResourceListItem, ModuleTheme } from './types'

withDefaults(
  defineProps<{
    title: string
    tabs: ModuleTab[]
    tabModel: string
    theme?: ModuleTheme
    items?: ResourceListItem[]
    listLayout?: 'grid' | 'split'
    maxItems?: number
  }>(),
  {
    theme: 'orange',
    items: () => [],
    listLayout: 'grid',
    maxItems: 14,
  }
)

defineEmits<{
  'update:tabModel': [key: string]
  select: [item: ResourceListItem]
}>()
</script>

<style scoped>
.home-resource-panel {
  --panel-primary: #ff6b00;
  --panel-sidebar-bg: linear-gradient(135deg, #fff5eb 0%, #fff9f3 40%, #fff 100%);
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.theme-blue {
  --panel-primary: #1890ff;
  --panel-sidebar-bg: linear-gradient(135deg, #e6f4ff 0%, #f0f8ff 50%, #fff 100%);
}

.theme-green {
  --panel-primary: #52c41a;
  --panel-sidebar-bg: linear-gradient(135deg, #f0fff0 0%, #f6ffed 50%, #fff 100%);
}

.content-wrap {
  display: flex;
  align-items: stretch;
  min-height: 360px;
}

.panel-sidebar {
  flex: 0 0 300px;
  width: 300px;
  padding: 20px 16px;
  background: var(--panel-sidebar-bg);
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel-main {
  flex: 1;
  min-width: 0;
  padding: 20px 24px;
}

@media (max-width: 1024px) {
  .content-wrap {
    flex-direction: column;
  }

  .panel-sidebar {
    width: 100%;
    flex: none;
    border-right: none;
    border-bottom: 1px solid #f0f0f0;
  }
}
</style>
