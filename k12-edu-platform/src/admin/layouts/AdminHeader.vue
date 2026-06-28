<template>
  <header class="admin-header">
    <div class="admin-header__left">
      <span class="admin-header__hint">管理后台</span>
    </div>
    <div class="admin-header__right">
      <router-link to="/" class="admin-header__link">返回前台</router-link>
      <span class="admin-header__user">{{ displayName }}</span>
      <el-button link type="primary" @click="onLogout">退出</el-button>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminAuthStore } from '@/admin/store/adminAuth'
import { buildLoginRoute } from '@/composables/useAuthLoginFlow'

const router = useRouter()
const adminStore = useAdminAuthStore()

const displayName = computed(() => {
  const u = adminStore.user
  if (!u) return '管理员'
  const roles = u.roles?.length ? u.roles.join(', ') : 'staff'
  return `${u.nickname || u.username} (${roles})`
})

async function onLogout() {
  await adminStore.logout()
  router.replace(buildLoginRoute({ intent: 'admin' }))
}
</script>

<style scoped>
.admin-header {
  height: 56px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e8ecf4;
}

.admin-header__hint {
  font-size: 14px;
  color: #8e8ea0;
}

.admin-header__right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-header__link {
  font-size: 14px;
  color: #4361ee;
  text-decoration: none;
}

.admin-header__user {
  font-size: 14px;
  color: #1a1a2e;
}
</style>
