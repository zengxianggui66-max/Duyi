<template>
  <motion.div class="competition-page" v-loading="loading" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }">
    <section class="competition-hero" :style="{ background: channelInfo.bgColor }">
      <motion.div class="container hero-inner" :initial="{ opacity: 0, y: 12 }" :animate="{ opacity: 1, y: 0 }" :transition="{ duration: 0.35 }">
        <motion.div class="hero-main" :initial="{ opacity: 0, x: -8 }" :animate="{ opacity: 1, x: 0 }" :transition="{ delay: 0.05 }">
          <nav class="breadcrumb" aria-label="面包屑">
            <router-link to="/" class="breadcrumb-link">首页</router-link>
            <span class="breadcrumb-sep">/</span>
            <router-link to="/feature" class="breadcrumb-link">特色资源频道</router-link>
            <span class="breadcrumb-sep">/</span>
            <span class="breadcrumb-current">竞赛专区</span>
          </nav>
          <h1>🏆 {{ channelInfo.name }}</h1>
          <p>{{ channelInfo.desc }}</p>
          <div class="hero-stats">
            <span><b>{{ channelInfo.stats?.total }}</b> 资源</span>
            <span><b>{{ channelInfo.stats?.elite }}</b> 精品</span>
            <span><b>{{ channelInfo.stats?.free }}</b> 免费</span>
          </div>
          <motion.div class="hero-search" :initial="{ opacity: 0, y: 6 }" :animate="{ opacity: 1, y: 0 }" :transition="{ delay: 0.12 }">
            <el-input
              v-model="keyword"
              placeholder="搜索奥数、联赛真题、CSP、作文大赛、考级…"
              size="large"
              clearable
              @keyup.enter="onSearch"
            />
            <el-button type="primary" size="large" @click="onSearch">搜索</el-button>
          </motion.div>
        </motion.div>
        <motion.aside class="hero-actions" :initial="{ opacity: 0, x: 8 }" :animate="{ opacity: 1, x: 0 }" :transition="{ delay: 0.1 }">
          <el-button class="upload-cta" size="large" @click="handleUploadClick">
            <span class="upload-cta-icon">📤</span>
            上传竞赛资源
          </el-button>
          <p class="upload-cta-hint">真题、讲义、教案、PPT、视频、练习题集</p>
        </motion.aside>
      </motion.div>
    </section>

    <motion.div class="container main-layout" :initial="{ opacity: 0, y: 16 }" :animate="{ opacity: 1, y: 0 }" :transition="{ delay: 0.15, duration: 0.35 }">
      <motion.aside class="left-sidebar card" :initial="{ opacity: 0, x: -12 }" :animate="{ opacity: 1, x: 0 }" :transition="{ delay: 0.2 }">
        <h3 class="sidebar-title">竞赛类型</h3>
        <button
          v-for="tab in sidebarTabs"
          :key="tab.key"
          type="button"
          class="sidebar-item"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          <span class="si-icon">{{ tab.icon }}</span>
          <span>{{ tab.name }}</span>
        </button>
      </motion.aside>

      <main class="main-content">
        <motion.div class="filter-bar card" :initial="{ opacity: 0, y: 8 }" :animate="{ opacity: 1, y: 0 }" :transition="{ delay: 0.22 }">
          <div class="filter-group">
            <span class="filter-label">学段</span>
            <el-radio-group v-model="activeGrade" size="small">
              <el-radio-button v-for="g in gradeStageOptions" :key="g.key" :label="g.key">{{ g.name }}</el-radio-button>
            </el-radio-group>
          </div>
          <div class="filter-group">
            <span class="filter-label">学科</span>
            <el-radio-group v-model="activeSubject" size="small">
              <el-radio-button v-for="s in subjectOptions" :key="s.key" :label="s.key">{{ s.name }}</el-radio-button>
            </el-radio-group>
          </div>
          <div class="filter-group">
            <span class="filter-label">资源形态</span>
            <el-radio-group v-model="activeFormat" size="small">
              <el-radio-button v-for="f in formatOptions" :key="f.key" :label="f.key">{{ f.name }}</el-radio-button>
            </el-radio-group>
          </div>
          <div class="filter-group">
            <span class="filter-label">权限</span>
            <el-radio-group v-model="activeLevel" size="small">
              <el-radio-button v-for="l in levelOptions" :key="l.key" :label="l.key">{{ l.name }}</el-radio-button>
            </el-radio-group>
          </div>
        </motion.div>

        <motion.section class="section-card card" :initial="{ opacity: 0, y: 10 }" :animate="{ opacity: 1, y: 0 }" :transition="{ delay: 0.26 }">
          <motion.div class="section-head" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.3 }">
            <h2>🏅 竞赛备考精品包</h2>
            <span class="section-meta">共 {{ packageTotal }} 套</span>
          </motion.div>
          <div v-if="packages.length" class="package-grid">
            <motion.div
              v-for="(pkg, index) in packages"
              :key="pkg.id"
              class="package-card"
              :initial="{ opacity: 0, y: 12 }"
              :animate="{ opacity: 1, y: 0 }"
              :transition="{ delay: 0.06 * index }"
              :whileHover="{ y: -2 }"
              @click="openPackage(pkg)"
            >
              <div class="pkg-cover">{{ pkg.icon || '🏅' }}</div>
              <motion.div class="pkg-body" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.08 + 0.04 * index }">
                <h3>{{ pkg.title }}</h3>
                <p class="pkg-summary">{{ pkg.summary }}</p>
                <div class="pkg-meta">
                  <span>{{ pkg.resourceCount || 0 }} 个资源</span>
                  <span>↓ {{ formatCount(pkg.downloadCount) }}</span>
                </div>
              </motion.div>
            </motion.div>
          </div>
          <el-empty v-else description="暂无精品包" />
        </motion.section>

        <motion.section class="section-card card" :initial="{ opacity: 0, y: 10 }" :animate="{ opacity: 1, y: 0 }" :transition="{ delay: 0.32 }">
          <motion.div class="section-head" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.36 }">
            <h2>📚 竞赛资源库</h2>
            <span class="section-meta">共 {{ total }} 条</span>
          </motion.div>
          <motion.div v-if="resources.length" class="resource-grid" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.38 }">
            <motion.div
              v-for="(item, index) in resources"
              :key="item.id"
              class="resource-card"
              :initial="{ opacity: 0, y: 8 }"
              :animate="{ opacity: 1, y: 0 }"
              :transition="{ delay: 0.03 * index }"
              :whileHover="{ backgroundColor: '#fff7ed' }"
              @click="openResource(item)"
            >
              <span class="res-icon">{{ item.icon || formIcon(item.resourceForm) }}</span>
              <div class="res-body">
                <h4>{{ item.title }}</h4>
                <p>{{ item.summary }}</p>
                <div class="res-meta">
                  <el-tag v-if="item.competitionName" size="small">{{ item.competitionName }}</el-tag>
                  <el-tag size="small" type="warning">{{ formLabel(item.resourceForm) }}</el-tag>
                  <span v-if="item.isFree === 1" class="free-tag">免费</span>
                  <span v-else class="vip-tag">精品</span>
                  <span>↓ {{ formatCount(item.downloadCount) }}</span>
                </div>
              </div>
            </motion.div>
          </motion.div>
          <el-empty v-else description="暂无匹配资源，可尝试调整筛选或上传资料" />
          <div v-if="total > pageSize" class="pagination-wrap">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              background
            />
          </div>
        </motion.section>
      </main>
    </motion.div>

    <CompetitionUploadDialog
      v-model="uploadDialogVisible"
      :categories="categories"
      :grade-stages="gradeStages"
      :resource-forms="resourceForms"
      @success="refreshAll"
    />

    <el-drawer v-model="packageDrawerVisible" :title="currentPackage?.title" size="480px">
      <template v-if="currentPackage">
        <p class="drawer-summary">{{ currentPackage.summary }}</p>
        <div class="drawer-tags">
          <el-tag type="warning">{{ currentPackage.resourceCount || 0 }} 个资源</el-tag>
          <el-tag v-if="currentPackage.isElite" type="danger" effect="plain">精品</el-tag>
        </div>
        <h4>包含资源（{{ packageResources.length }}）</h4>
        <ul class="drawer-list">
          <li v-for="r in packageResources" :key="r.id" @click="openResource(r)">
            {{ r.icon || '📄' }} {{ r.title }}
          </li>
        </ul>
        <el-button type="primary" style="width:100%; margin-top:16px;" @click="downloadPackage(currentPackage.id)">
          下载精品包
        </el-button>
      </template>
    </el-drawer>
  </motion.div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { motion } from 'motion-v'
import { useUserStore } from '@/store'
import CompetitionUploadDialog from '@/components/competition/CompetitionUploadDialog.vue'
import { useCompetitionZone } from '@/composables/useCompetitionZone'
import { competitionApi, type CompetitionResourceItem, type CompetitionPackageItem } from '@/api/competition'
import {
  COMPETITION_GRADE_STAGES,
  COMPETITION_SUBJECTS,
  COMPETITION_FORMATS,
  COMPETITION_LEVELS,
} from '@/constants/competitionZone'
const router = useRouter()
const userStore = useUserStore()
const uploadDialogVisible = ref(false)
const packageDrawerVisible = ref(false)
const currentPackage = ref<CompetitionPackageItem | null>(null)
const packageResources = ref<CompetitionResourceItem[]>([])

const {
  loading,
  keyword,
  activeTab,
  activeGrade,
  activeSubject,
  activeFormat,
  activeLevel,
  currentPage,
  pageSize,
  mainTabs,
  resources,
  packages,
  total,
  packageTotal,
  categories,
  gradeStages,
  resourceForms,
  channelInfo,
  formatCount,
  onSearch,
  openResource,
  refreshAll,
} = useCompetitionZone()

const sidebarTabs = computed(() => {
  if (categories.value.length) return categories.value
  return mainTabs.filter((t) => t.key !== 'elite')
})

const gradeStageOptions = COMPETITION_GRADE_STAGES
const subjectOptions = COMPETITION_SUBJECTS
const formatOptions = [
  { key: 'all', name: '不限' },
  ...COMPETITION_FORMATS.filter((f) => f.key !== 'all').map((f) => ({ key: f.key, name: f.name })),
]
const levelOptions = COMPETITION_LEVELS

const FORM_LABELS: Record<string, string> = {
  exam: '真题', mock: '模拟', lecture: '讲义', lesson_plan: '教案',
  ppt: 'PPT', video: '视频', doc: '文档', exercise: '练习',
}
const FORM_ICONS: Record<string, string> = {
  exam: '📝', mock: '📋', lecture: '📑', lesson_plan: '📖',
  ppt: '📊', video: '🎬', doc: '📄', exercise: '✏️',
}

function formLabel(key?: string) {
  return FORM_LABELS[key || ''] || '资料'
}
function formIcon(key?: string) {
  return FORM_ICONS[key || ''] || '🏆'
}

function handleUploadClick() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再上传资源')
    router.push({ path: '/login', query: { redirect: '/competition-zone' } })
    return
  }
  uploadDialogVisible.value = true
}

async function openPackage(pkg: CompetitionPackageItem) {
  currentPackage.value = pkg
  packageDrawerVisible.value = true
  packageResources.value = []
  try {
    const res = await competitionApi.getPackage(pkg.id)
    packageResources.value = res.data.data?.resources || []
  } catch {
    ElMessage.warning('精品包详情加载失败')
  }
}

async function downloadPackage(id: number) {
  try {
    await competitionApi.downloadPackage(id)
    ElMessage.success('已记录下载')
    refreshAll()
  } catch (e: unknown) {
    const err = e as { message?: string }
    ElMessage.error(err.message || '操作失败')
  }
}
</script>

<style scoped>
.competition-page { min-height: 100vh; background: var(--bg-page, #f5f6f8); }
.competition-hero { color: #fff; padding: 32px 0 40px; }
.hero-inner {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
  flex-wrap: wrap;
}
.hero-main { flex: 1; min-width: 280px; }
.hero-main h1 { font-size: 28px; font-weight: 700; margin: 8px 0; }
.hero-main p { opacity: 0.92; margin-bottom: 12px; max-width: 640px; }
.hero-stats { display: flex; gap: 20px; margin-bottom: 16px; font-size: 14px; }
.hero-stats b { font-size: 18px; margin-right: 4px; }
.hero-search { display: flex; gap: 8px; max-width: 520px; }
.hero-actions { text-align: right; }
.upload-cta {
  background: rgba(255,255,255,0.95) !important;
  color: #b45309 !important;
  border: none !important;
  font-weight: 600;
}
.upload-cta-hint { font-size: 12px; opacity: 0.85; margin-top: 8px; max-width: 200px; margin-left: auto; }
.breadcrumb { font-size: 13px; margin-bottom: 4px; }
.breadcrumb-link { color: rgba(255,255,255,0.85); text-decoration: none; }
.breadcrumb-sep { margin: 0 6px; opacity: 0.6; }
.breadcrumb-current { opacity: 0.95; }

.main-layout {
  display: flex;
  gap: 24px;
  max-width: 1200px;
  margin: -24px auto 40px;
  padding: 0 24px 24px;
  align-items: flex-start;
}
.left-sidebar {
  width: 200px;
  flex-shrink: 0;
  padding: 16px 12px;
  position: sticky;
  top: 80px;
}
.sidebar-title { font-size: 14px; font-weight: 700; margin-bottom: 12px; padding: 0 8px; color: var(--text-secondary); }
.sidebar-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 10px 12px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  text-align: left;
  color: var(--text-regular);
  transition: background 0.2s;
}
.sidebar-item:hover { background: #fff7ed; }
.sidebar-item.active {
  background: linear-gradient(135deg, #f59e0b, #ef4444);
  color: #fff;
  font-weight: 600;
}
.si-icon { font-size: 18px; }

.main-content { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 20px; }
.filter-bar { padding: 16px 20px; }
.filter-group { margin-bottom: 12px; }
.filter-group:last-child { margin-bottom: 0; }
.filter-label { display: block; font-size: 13px; font-weight: 600; color: var(--text-secondary); margin-bottom: 8px; }

.section-card { padding: 20px 24px; }
.section-head { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 16px; }
.section-head h2 { font-size: 18px; font-weight: 700; }
.section-meta { font-size: 13px; color: var(--text-secondary); }

.package-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.package-card {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: linear-gradient(135deg, #fffbeb 0%, #fff 100%);
  border: 1px solid #fde68a;
  border-radius: 12px;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}
.package-card:hover { box-shadow: 0 8px 24px rgba(245, 158, 11, 0.2); transform: translateY(-2px); }
.pkg-cover {
  width: 56px; height: 56px; border-radius: 12px; background: #fef3c7;
  display: flex; align-items: center; justify-content: center; font-size: 28px; flex-shrink: 0;
}
.pkg-body h3 { font-size: 15px; font-weight: 700; margin-bottom: 6px; }
.pkg-summary {
  font-size: 13px; color: var(--text-secondary); margin-bottom: 8px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.pkg-meta { font-size: 12px; color: var(--text-secondary); display: flex; justify-content: space-between; }

.resource-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.resource-card {
  display: flex; gap: 12px; padding: 14px; background: var(--bg-body, #f9fafb);
  border-radius: 10px; cursor: pointer; transition: background 0.2s;
}
.resource-card:hover { background: #fff7ed; }
.res-icon { font-size: 28px; flex-shrink: 0; }
.res-body h4 { font-size: 14px; font-weight: 600; margin-bottom: 4px; }
.res-body p {
  font-size: 12px; color: var(--text-secondary); margin-bottom: 8px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.res-meta { font-size: 11px; color: var(--text-secondary); display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.free-tag { color: #10b981; font-weight: 600; }
.vip-tag { color: #8b5cf6; font-weight: 600; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; }

.drawer-summary { color: var(--text-secondary); margin-bottom: 12px; line-height: 1.6; }
.drawer-tags { display: flex; gap: 8px; margin-bottom: 16px; }
.drawer-list { list-style: none; padding: 0; margin: 0 0 16px; }
.drawer-list li {
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}
.drawer-list li:hover { background: #fff7ed; }

@media (max-width: 900px) {
  .main-layout { flex-direction: column; margin-top: 0; }
  .left-sidebar { width: 100%; position: static; display: flex; flex-wrap: wrap; gap: 8px; }
  .sidebar-item { width: auto; }
  .package-grid, .resource-grid { grid-template-columns: 1fr; }
  .hero-inner { flex-direction: column; }
  .hero-actions { text-align: left; }
}
</style>
