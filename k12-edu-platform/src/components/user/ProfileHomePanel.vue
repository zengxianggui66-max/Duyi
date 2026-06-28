<template>
  <div class="profile-home-panel">
    <div class="welcome-card card">
      <div class="welcome-main">
        <h3>👋 你好，{{ nickname }}</h3>
        <p class="user-meta">
          用户 ID：{{ userId || '—' }}
          <span class="meta-divider">|</span>
          {{ roleLabel }}
          <span class="meta-divider">|</span>
          {{ memberLevelLabel }}
        </p>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-box" @click="emit('navigate', 'favorites')">
        <span class="stat-num">{{ stats.collect }}</span>
        <span class="stat-label">我的收藏</span>
      </div>
      <div class="stat-box" @click="emit('navigate', 'downloads')">
        <span class="stat-num">{{ stats.download }}</span>
        <span class="stat-label">我的下载</span>
      </div>
      <div class="stat-box" @click="emit('navigate', 'views')">
        <span class="stat-num">{{ stats.view }}</span>
        <span class="stat-label">我的浏览</span>
      </div>
      <div class="stat-box" @click="emit('navigate', 'upload')">
        <span class="stat-num">{{ stats.upload }}</span>
        <span class="stat-label">我的上传</span>
      </div>
    </div>

    <div class="quick-links card">
      <h4>快捷入口</h4>
      <div class="link-grid">
        <button type="button" class="quick-link" @click="emit('navigate', 'favorites')">⭐ 我的收藏</button>
        <button type="button" class="quick-link" @click="emit('navigate', 'downloads')">📥 我的下载</button>
        <button type="button" class="quick-link" @click="emit('navigate', 'views')">👁 我的浏览</button>
        <button type="button" class="quick-link" @click="emit('navigate', 'upload')">📤 我的上传</button>
        <button type="button" class="quick-link" @click="$router.push('/lesson/basket')">📚 我的备课</button>
        <button type="button" class="quick-link" @click="emit('navigate', 'settings')">⚙️ 个人资料</button>
      </div>
    </div>

    <div class="recent-section">
      <div class="recent-card card">
        <div class="recent-header">
          <h4>最近下载</h4>
          <button type="button" class="link-btn" @click="emit('navigate', 'downloads')">查看全部</button>
        </div>
        <ul v-if="recentDownloads.length" class="recent-list">
          <li v-for="item in recentDownloads" :key="item.id" class="recent-item">
            <span class="recent-title">{{ item.resourceTitle }}</span>
            <span class="recent-time">{{ formatDate(item.createTime) }}</span>
          </li>
        </ul>
        <p v-else class="empty-hint">暂无下载记录</p>
      </div>

      <div class="recent-card card">
        <div class="recent-header">
          <h4>最近浏览</h4>
          <button type="button" class="link-btn" @click="emit('navigate', 'views')">查看全部</button>
        </div>
        <ul v-if="recentViews.length" class="recent-list">
          <li v-for="item in recentViews" :key="item.id" class="recent-item">
            <span class="recent-title">{{ item.title }}</span>
            <span class="recent-time">{{ formatViewTime(item.time) }}</span>
          </li>
        </ul>
        <p v-else class="empty-hint">暂无浏览记录</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { collectApi, downloadApi, primaryChineseApi } from '@/api'
import { viewApi } from '@/api/view'
import { unwrapData } from '@/api/request'
import { useUserStore } from '@/store'
import { useRecentViews } from '@/composables/useRecentViews'
import type { DownloadItem } from '@/api/download'

defineProps<{
  memberLevelLabel: string
}>()

const emit = defineEmits<{
  navigate: [tab: string]
}>()

const userStore = useUserStore()
const { formatViewTime } = useRecentViews()

const stats = ref({ collect: 0, download: 0, view: 0, upload: 0 })
const recentDownloads = ref<DownloadItem[]>([])
const recentViews = ref<{ id: number; title: string; time: string }[]>([])

const nickname = computed(() => userStore.nickname || '用户')
const userId = computed(() => userStore.user?.id)
const roleMap: Record<string, string> = {
  admin: '管理员',
  teacher: '教师',
  student: '学生',
  parent: '家长',
  pending: '待分配',
}
const roleLabel = computed(
  () => userStore.user?.roleName || roleMap[userStore.user?.role || ''] || '用户',
)

function formatDate(raw?: string): string {
  if (!raw) return '-'
  return raw.slice(0, 10)
}

async function loadDashboard() {
  if (!userStore.isLoggedIn) return
  try {
    const [cRes, dStatsRes, vStatsRes, uRes, dPageRes, vPageRes] = await Promise.all([
      collectApi.getStats(),
      downloadApi.getStats(),
      viewApi.getStats(),
      primaryChineseApi.getMyUploadStats(),
      downloadApi.getPage({ current: 1, size: 5 }),
      viewApi.getPage({ current: 1, size: 5 }),
    ])
    const c = unwrapData(cRes)
    const dStats = unwrapData(dStatsRes)
    const vStats = unwrapData(vStatsRes)
    const u = unwrapData(uRes)
    const dPage = unwrapData(dPageRes)
    const vPage = unwrapData(vPageRes)

    stats.value = {
      collect: c?.total ?? 0,
      download: dStats?.total ?? 0,
      view: vStats?.total ?? 0,
      upload: u?.total ?? 0,
    }
    recentDownloads.value = dPage?.records || []
    recentViews.value = (vPage?.records || []).map((row) => ({
      id: row.id,
      title: row.title,
      time: row.updateTime || row.createTime || '',
    }))
  } catch {
    stats.value = { collect: 0, download: 0, view: 0, upload: 0 }
  }
}

onMounted(() => {
  void loadDashboard()
})
</script>

<style scoped>
.profile-home-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.welcome-card {
  padding: 20px 24px;
}
.welcome-main h3 {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 700;
}
.user-meta {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
}
.meta-divider {
  margin: 0 8px;
  color: var(--border-light);
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.stat-box {
  padding: 16px;
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.stat-box:hover {
  border-color: var(--color-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.stat-num {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: var(--color-primary);
}
.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
  display: block;
}
.quick-links {
  padding: 20px 24px;
}
.quick-links h4,
.recent-header h4 {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
}
.link-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.quick-link {
  padding: 10px 12px;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  background: #fafafa;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.quick-link:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: var(--color-primary-bg);
}
.recent-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.recent-card {
  padding: 20px 24px;
}
.recent-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.link-btn {
  border: none;
  background: none;
  color: var(--color-primary);
  font-size: 13px;
  cursor: pointer;
}
.recent-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.recent-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-light);
  font-size: 13px;
}
.recent-item:last-child {
  border-bottom: none;
}
.recent-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-regular);
}
.recent-time {
  color: var(--text-secondary);
  flex-shrink: 0;
}
.empty-hint {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
}
@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .recent-section {
    grid-template-columns: 1fr;
  }
  .link-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
