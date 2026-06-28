<template>
  <AdminPageShell title="用户与行为" desc="用户增长与资源互动简版（不含 DAU/留存模型）">
    <div class="toolbar">
      <el-radio-group v-model="userDays" size="small" @change="load">
        <el-radio-button :value="30">近 30 天</el-radio-button>
        <el-radio-button :value="90">近 90 天</el-radio-button>
      </el-radio-group>
      <el-button size="small" :loading="loading" @click="load">刷新</el-button>
      <el-button v-if="hasUserView" size="small" @click="$router.push('/admin/users')">用户列表</el-button>
    </div>

    <el-row v-loading="loading" :gutter="16" class="cards">
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover">
          <div class="card-label">用户总数</div>
          <div class="card-value">{{ users?.totalUsers ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover">
          <div class="card-label">近 {{ userDays }} 天新增</div>
          <div class="card-value">{{ users?.newUsersInPeriod ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover">
          <div class="card-label">近 7 天互动（浏览/下载/收藏）</div>
          <div class="card-value card-value--sm">{{ actionSummary }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never">
          <template #header>注册趋势</template>
          <TrendBars :points="users?.registrationTrend ?? []" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never">
          <template #header>互动趋势（近 7 天）</template>
          <el-table :data="dashboard?.actionTrend ?? []" size="small" stripe max-height="280">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="views" label="浏览" width="80" />
            <el-table-column prop="downloads" label="下载" width="80" />
            <el-table-column prop="collects" label="收藏" width="80" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AdminPageShell from '@/admin/components/AdminPageShell.vue'
import TrendBars from '@/admin/components/TrendBars.vue'
import { getAnalyticsDashboard, getAnalyticsUsers, type AnalyticsDashboard, type AnalyticsUser } from '@/admin/api/analytics'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

const loading = ref(false)
const userDays = ref(30)
const users = ref<AnalyticsUser | null>(null)
const dashboard = ref<AnalyticsDashboard | null>(null)
const adminStore = useAdminAuthStore()
const hasUserView = computed(() => adminStore.hasPermission('admin:user:view'))

const actionSummary = computed(() => {
  const rows = dashboard.value?.actionTrend ?? []
  const views = rows.reduce((s, r) => s + r.views, 0)
  const downloads = rows.reduce((s, r) => s + r.downloads, 0)
  const collects = rows.reduce((s, r) => s + r.collects, 0)
  return `${views} / ${downloads} / ${collects}`
})

async function load() {
  loading.value = true
  try {
    const [u, d] = await Promise.all([getAnalyticsUsers(userDays.value), getAnalyticsDashboard(7)])
    users.value = u
    dashboard.value = d
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.cards {
  margin-bottom: 16px;
}
.card-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}
.card-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}
.card-value--sm {
  font-size: 18px;
}
</style>
