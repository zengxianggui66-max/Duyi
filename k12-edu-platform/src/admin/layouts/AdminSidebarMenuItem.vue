<template>
  <el-sub-menu
    v-if="item.children?.length"
    :index="menuIndex"
    :teleported="false"
    popper-class="admin-sidebar-submenu-popper"
  >
    <template #title>
      <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
      <span>{{ item.title }}</span>
    </template>
    <AdminSidebarMenuItem
      v-for="child in item.children"
      :key="child.id ?? child.path"
      :item="child"
      :depth="depth + 1"
    />
  </el-sub-menu>
  <el-menu-item v-else :index="item.path" :class="itemClass">
    <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
    <span>{{ item.title }}</span>
  </el-menu-item>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { AdminMenu } from '@/admin/api/auth'
import AdminSidebarMenuItem from './AdminSidebarMenuItem.vue'

const props = withDefaults(
  defineProps<{
    item: AdminMenu
    depth?: number
  }>(),
  { depth: 0 },
)

const menuIndex = computed(() => String(props.item.id ?? props.item.path))

const itemClass = computed(() => {
  if (props.depth <= 0) return undefined
  return `admin-sidebar-menu-item--depth-${Math.min(props.depth, 3)}`
})
</script>
