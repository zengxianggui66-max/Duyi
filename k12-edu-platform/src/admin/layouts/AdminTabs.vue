<template>
  <div v-if="tabs.length" class="admin-tabs">
    <router-link
      v-for="tab in tabs"
      :key="tab.path"
      :to="tab.path"
      class="admin-tabs__item"
      :class="{ 'admin-tabs__item--active': route.path === tab.path }"
    >
      {{ tab.title }}
    </router-link>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

const route = useRoute()
const adminStore = useAdminAuthStore()

const tabs = computed(() =>
  adminStore.menus.map((m) => ({ path: m.path, title: m.title }))
)
</script>

<style scoped>
.admin-tabs {
  display: flex;
  gap: 4px;
  padding: 8px 20px 0;
  background: #fff;
  border-bottom: 1px solid #e8ecf4;
  overflow-x: auto;
}

.admin-tabs__item {
  padding: 8px 14px;
  font-size: 13px;
  color: #8e8ea0;
  text-decoration: none;
  border-radius: 8px 8px 0 0;
  white-space: nowrap;
}

.admin-tabs__item--active {
  color: #4361ee;
  background: #f0f2f8;
  font-weight: 600;
}
</style>
