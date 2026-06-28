<template>
  <div class="profile-page">
    <div class="container profile-container">
      <div class="profile-layout">
        <div class="profile-sidebar card">
          <div class="avatar-section">
            <div v-if="userStore.user?.avatar" class="avatar avatar-img-wrap">
              <img :src="userStore.user.avatar" alt="头像" class="avatar-img" />
            </div>
            <div v-else class="avatar">{{ avatarEmoji }}</div>
            <h3>{{ userStore.nickname }}</h3>
            <p>{{ userStore.user?.email || userStore.user?.phone || '' }}</p>
            <div class="tags-row">
              <el-tag>{{ roleLabel }}</el-tag>
              <el-tag type="warning" effect="plain">{{ memberLevelLabel }}</el-tag>
            </div>
          </div>

          <nav class="profile-nav">
            <a
              class="nav-item"
              :class="{ active: activeNav === 'home' }"
              @click="switchNav('home')"
            >
              🏠 账户首页
            </a>

            <div class="nav-group-label">资源管理</div>
            <a
              v-for="item in resourceNavItems"
              :key="item.key"
              class="nav-item"
              :class="{ active: activeNav === item.key }"
              @click="switchNav(item.key)"
            >
              {{ item.icon }} {{ item.name }}
            </a>

            <div class="nav-group-label">账号信息</div>
            <a
              v-for="item in accountNavItems"
              :key="item.key"
              class="nav-item"
              :class="{ active: activeNav === item.key }"
              @click="switchNav(item.key)"
            >
              {{ item.icon }} {{ item.name }}
            </a>
          </nav>

          <div class="profile-stats">
            <div class="stat">
              <span class="stat-num">{{ sidebarStats.collect }}</span>
              <span class="stat-label">收藏</span>
            </div>
            <div class="stat">
              <span class="stat-num">{{ sidebarStats.download }}</span>
              <span class="stat-label">下载</span>
            </div>
            <div class="stat">
              <span class="stat-num">{{ sidebarStats.upload }}</span>
              <span class="stat-label">上传</span>
            </div>
          </div>
        </div>

        <div class="profile-content">
          <div v-if="activeNav === 'home'" class="content-card card">
            <ProfileHomePanel
              v-if="userStore.isLoggedIn"
              :member-level-label="memberLevelLabel"
              @navigate="switchNav"
            />
            <el-empty v-else description="请先登录" />
          </div>

          <div v-else-if="activeNav === 'favorites'" class="content-card card">
            <h3>⭐ 我的收藏</h3>
            <p class="content-hint">与学科列表收藏按钮同步，按学段、学科与资源类型筛选</p>
            <ProfileFavoritesPanel v-if="userStore.isLoggedIn" />
            <el-empty v-else description="请先登录查看收藏" />
          </div>

          <div v-else-if="activeNav === 'downloads'" class="content-card card">
            <h3>📥 我的下载</h3>
            <p class="content-hint">记录您下载过的教学资源，支持再次下载与删除</p>
            <ProfileDownloadsPanel v-if="userStore.isLoggedIn" />
            <el-empty v-else description="请先登录查看下载" />
          </div>

          <div v-else-if="activeNav === 'views'" class="content-card card">
            <h3>👁 我的浏览</h3>
            <p class="content-hint">浏览资源详情后自动记录，可按学段与学科筛选</p>
            <ProfileViewsPanel v-if="userStore.isLoggedIn" />
            <el-empty v-else description="请先登录查看浏览记录" />
          </div>

          <div v-else-if="activeNav === 'upload'" class="content-card card">
            <h3>📤 我的上传</h3>
            <p class="content-hint">您通过上传页发布的资源，每页 20 条</p>
            <ProfileUploadsPanel v-if="userStore.isLoggedIn" />
            <el-empty v-else description="请先登录查看上传" />
          </div>

          <div v-else-if="activeNav === 'settings'" class="content-card card">
            <ProfileSettingsPanel v-if="userStore.isLoggedIn" />
            <el-empty v-else description="请先登录" />
          </div>

          <div v-else-if="activeNav === 'security'" class="content-card card">
            <AccountSecurity />
          </div>

          <div v-else class="content-card card">
            <el-empty description="请选择左侧菜单" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { memberApi, collectApi, primaryChineseApi, downloadApi } from '@/api'
import { unwrapData } from '@/api/request'
import AccountSecurity from '@/components/user/AccountSecurity.vue'
import ProfileFavoritesPanel from '@/components/user/ProfileFavoritesPanel.vue'
import ProfileUploadsPanel from '@/components/user/ProfileUploadsPanel.vue'
import ProfileDownloadsPanel from '@/components/user/ProfileDownloadsPanel.vue'
import ProfileViewsPanel from '@/components/user/ProfileViewsPanel.vue'
import ProfileSettingsPanel from '@/components/user/ProfileSettingsPanel.vue'
import ProfileHomePanel from '@/components/user/ProfileHomePanel.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const allNavKeys = ['home', 'favorites', 'downloads', 'views', 'upload', 'lesson', 'settings', 'security']
const activeNav = ref((route.query.tab as string) || 'home')

const resourceNavItems = [
  { key: 'favorites', name: '我的收藏', icon: '⭐' },
  { key: 'downloads', name: '我的下载', icon: '📥' },
  { key: 'views', name: '我的浏览', icon: '👁' },
  { key: 'upload', name: '我的上传', icon: '📤' },
  { key: 'lesson', name: '我的备课', icon: '📚' },
]

const accountNavItems = [
  { key: 'settings', name: '个人资料', icon: '⚙️' },
  { key: 'security', name: '账号安全', icon: '🔐' },
]

const sidebarStats = ref({ collect: 0, download: 0, upload: 0 })
const memberInfo = ref<{ memberLevel?: string; level?: string } | null>(null)

const roleMap: Record<string, string> = {
  admin: '管理员',
  teacher: '教师',
  student: '学生',
  parent: '家长',
  pending: '待分配',
}
const roleLabel = computed(() => userStore.user?.roleName || roleMap[userStore.user?.role || ''] || '用户')
const avatarEmoji = computed(() => {
  const r = userStore.user?.role
  if (r === 'admin') return '🛡️'
  if (r === 'teacher') return '👨‍🏫'
  if (r === 'student') return '🎓'
  return '👨‍👧'
})
const memberLevelLabel = computed(() => {
  const lv = String(memberInfo.value?.memberLevel ?? memberInfo.value?.level ?? '')
  if (lv === 'premium' || lv === '1') return '高级会员'
  if (lv === 'basic') return '基础会员'
  return '免费用户'
})

function switchNav(key: string) {
  if (key === 'lesson') {
    router.push('/lesson/basket')
    return
  }
  activeNav.value = key
  router.replace({ query: { tab: key } })
}

async function loadSidebarStats() {
  if (!userStore.isLoggedIn) {
    sidebarStats.value = { collect: 0, download: 0, upload: 0 }
    return
  }
  try {
    const [cRes, dRes, uRes] = await Promise.all([
      collectApi.getStats(),
      downloadApi.getStats(),
      primaryChineseApi.getMyUploadStats(),
    ])
    const c = unwrapData(cRes)
    const d = unwrapData(dRes)
    const u = unwrapData(uRes)
    sidebarStats.value = {
      collect: c?.total ?? 0,
      download: d?.total ?? 0,
      upload: u?.total ?? 0,
    }
  } catch {
    sidebarStats.value = { collect: 0, download: 0, upload: 0 }
  }
}

watch(
  () => route.query.tab,
  (tab) => {
    if (typeof tab === 'string' && allNavKeys.includes(tab)) {
      activeNav.value = tab
    }
  },
)

watch(
  () => userStore.isLoggedIn,
  () => {
    void loadSidebarStats()
  },
)

onMounted(async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await memberApi.getInfo()
    memberInfo.value = res.data?.data ?? res.data
  } catch {
    /* ignore */
  }
  await loadSidebarStats()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: var(--bg-body);
}
.profile-container {
  padding: 32px 24px;
  max-width: 1200px;
}
.profile-layout {
  display: flex;
  gap: 24px;
}
.profile-sidebar {
  width: 260px;
  flex-shrink: 0;
}
.avatar-section {
  text-align: center;
  padding: 24px 0 20px;
  border-bottom: 1px solid var(--border-light);
}
.avatar {
  font-size: 64px;
  margin-bottom: 8px;
}
.avatar-img-wrap {
  width: 72px;
  height: 72px;
  margin: 0 auto 8px;
  border-radius: 50%;
  overflow: hidden;
  font-size: 0;
}
.avatar-img-wrap .avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-section h3 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 4px;
}
.avatar-section p {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.tags-row {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
}
.profile-nav {
  padding: 12px 0;
}
.nav-group-label {
  padding: 12px 16px 4px;
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 600;
}
.nav-item {
  display: block;
  padding: 10px 16px;
  font-size: 14px;
  color: var(--text-regular);
  cursor: pointer;
  border-radius: var(--radius-sm);
  margin: 2px 8px;
  transition: all 0.2s;
}
.nav-item:hover {
  background: var(--color-primary-bg);
  color: var(--color-primary);
}
.nav-item.active {
  background: var(--color-primary);
  color: #fff;
  font-weight: 600;
}
.profile-stats {
  display: flex;
  justify-content: space-around;
  padding: 16px 0;
  border-top: 1px solid var(--border-light);
}
.stat {
  text-align: center;
}
.stat-num {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary);
}
.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
}
.profile-content {
  flex: 1;
  min-width: 0;
}
.content-card {
  padding: 24px;
}
.content-card h3 {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 8px;
}
.content-hint {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 16px;
}
@media (max-width: 768px) {
  .profile-layout {
    flex-direction: column;
  }
  .profile-sidebar {
    width: 100%;
  }
}
</style>

