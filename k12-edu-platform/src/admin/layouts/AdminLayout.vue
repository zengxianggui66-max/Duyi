<template>
  <div class="admin-layout">
    <AdminSidebar />
    <div class="admin-layout__main">
      <AdminHeader />
      <AdminTabs />
      <div class="admin-layout__content">
        <AdminBreadcrumb />
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import AdminSidebar from './AdminSidebar.vue'
import AdminHeader from './AdminHeader.vue'
import AdminBreadcrumb from './AdminBreadcrumb.vue'
import AdminTabs from './AdminTabs.vue'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

const adminStore = useAdminAuthStore()

/** SQL 菜单变更后无需重新登录，进入布局时刷新侧栏树 */
onMounted(() => {
  if (adminStore.loaded) {
    void adminStore.refreshMenus()
  }
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f0f2f8;
}

.admin-layout__main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.admin-layout__content {
  flex: 1;
  padding: 16px 20px 24px;
  overflow: auto;
}
</style>
