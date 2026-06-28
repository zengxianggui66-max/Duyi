<template>
  <aside class="admin-sidebar">
    <div class="admin-sidebar__brand">
      <span class="admin-sidebar__logo">🛡️</span>
      <span class="admin-sidebar__title">新课堂管理端</span>
    </div>
    <el-menu
      :default-active="activePath"
      class="admin-sidebar__menu"
      background-color="#1f2430"
      text-color="#c9cdd8"
      active-text-color="#ffffff"
      router
    >
      <AdminSidebarMenuItem
        v-for="item in menus"
        :key="item.id ?? item.path"
        :item="item"
      />
    </el-menu>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAdminAuthStore } from '@/admin/store/adminAuth'
import AdminSidebarMenuItem from './AdminSidebarMenuItem.vue'

const route = useRoute()
const adminStore = useAdminAuthStore()

const menus = computed(() => adminStore.menus)
const activePath = computed(() => route.path)
</script>

<style scoped>
.admin-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: #1f2430;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.admin-sidebar__brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 56px;
  padding: 0 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.admin-sidebar__logo {
  font-size: 20px;
}

.admin-sidebar__title {
  font-size: 15px;
  font-weight: 700;
}

.admin-sidebar__menu {
  border-right: none;
  flex: 1;
  overflow-x: hidden;
  overflow-y: auto;
}

.admin-sidebar__menu :deep(.el-menu--inline) {
  background-color: rgb(25, 29, 38);
}

.admin-sidebar__menu :deep(.el-sub-menu .el-sub-menu > .el-sub-menu__title) {
  padding-left: 48px !important;
}

.admin-sidebar__menu :deep(.el-sub-menu .el-sub-menu .el-menu-item) {
  padding-left: 56px !important;
}

.admin-sidebar__menu :deep(.admin-sidebar-menu-item--depth-1) {
  padding-left: 40px !important;
}

.admin-sidebar__menu :deep(.admin-sidebar-menu-item--depth-2) {
  padding-left: 48px !important;
}

.admin-sidebar__menu :deep(.admin-sidebar-menu-item--depth-3) {
  padding-left: 56px !important;
}
</style>
