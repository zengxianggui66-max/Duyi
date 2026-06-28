<template>
  <nav v-if="items.length" class="browse-breadcrumb" aria-label="当前筛选路径">
    <span class="breadcrumb-label">当前筛选</span>
    <ol class="breadcrumb-list">
      <li
        v-for="(item, idx) in items"
        :key="`${idx}-${item.name}`"
        class="breadcrumb-item"
      >
        <span v-if="idx > 0" class="path-sep" aria-hidden="true">／</span>
        <router-link
          v-if="item.route"
          :to="item.route"
          class="path-link"
        >
          {{ item.name }}
        </router-link>
        <span v-else class="path-part">{{ item.name }}</span>
      </li>
    </ol>
  </nav>
</template>

<script setup lang="ts">
import type { RouteLocationRaw } from 'vue-router'

export interface BreadcrumbItem {
  name: string
  /** 点击后跳转的路由；为 undefined 表示当前项，不可点击 */
  route?: RouteLocationRaw
}

defineProps<{
  items: BreadcrumbItem[]
}>()
</script>

<style scoped>
.browse-breadcrumb {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  gap: var(--space-sm, 8px);
  margin-bottom: 12px;
  padding: 10px var(--space-md, 16px);
  background: var(--bg-card, #fff);
  border: 1px solid var(--border-light, #F0F2F8);
  border-radius: var(--radius-sm, 6px);
  overflow: hidden;
}

.breadcrumb-label {
  flex-shrink: 0;
  font-size: var(--fs-caption, 12px);
  font-weight: 500;
  color: var(--text-secondary, #8E8EA0);
}

.breadcrumb-list {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  gap: 0;
  margin: 0;
  padding: 0;
  list-style: none;
  min-width: 0;
  overflow: hidden;
}

.breadcrumb-item {
  display: inline-flex;
  align-items: center;
  font-size: var(--fs-caption, 12px);
  line-height: 1.4;
  white-space: nowrap;
}

.path-sep {
  color: var(--text-placeholder, #B0B0C0);
  margin: 0 4px;
}

.path-part {
  color: var(--text-regular, #4A4A68);
}

.path-link {
  color: var(--color-primary, #4361EE);
  text-decoration: none;
  cursor: pointer;
  transition: color var(--transition-fast, 0.15s ease);
}

.path-link:hover {
  color: var(--color-primary-light, #6B83F2);
  text-decoration: underline;
}

.path-link:active {
  color: var(--color-primary-dark, #3250D3);
}

.breadcrumb-item:last-child .path-part,
.breadcrumb-item:last-child .path-link {
  color: var(--text-primary, #1A1A2E);
  font-weight: 500;
}

.breadcrumb-item:last-child .path-link {
  cursor: default;
  text-decoration: none;
  pointer-events: none;
}
</style>
