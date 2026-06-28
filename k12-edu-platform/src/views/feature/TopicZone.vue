<template>
  <motion.div class="topic-page" v-loading="loading" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }">
    <section class="topic-hero" :style="{ background: channelInfo.bgColor }">
      <motion.div
        class="container hero-inner"
        :initial="{ opacity: 0, y: 12 }"
        :animate="{ opacity: 1, y: 0 }"
        :transition="{ duration: 0.35 }"
      >
        <motion.div class="hero-main" :initial="{ opacity: 0, x: -8 }" :animate="{ opacity: 1, x: 0 }">
          <nav class="breadcrumb" aria-label="面包屑">
            <router-link to="/" class="breadcrumb-link">首页</router-link>
            <span class="breadcrumb-sep">/</span>
            <router-link to="/feature" class="breadcrumb-link">特色资源频道</router-link>
            <span class="breadcrumb-sep">/</span>
            <span class="breadcrumb-current">专题资源</span>
          </nav>
          <h1>📚 {{ channelInfo.name }}</h1>
          <p>{{ channelInfo.desc }}</p>
          <motion.p class="hero-hint" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.08 }">
            聚焦成都、绵阳片区 · 寒暑假作业 · 开学备考 · 期中期末 · 升学冲刺 · 时事与跨学科
          </motion.p>
          <motion.div class="hero-stats" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.1 }">
            <span><b>{{ displayStats.total }}</b> 资源</span>
            <span><b>{{ displayStats.elite }}</b> 精品</span>
            <span><b>{{ displayStats.free }}</b> 免费</span>
          </motion.div>
          <motion.div class="hero-search" :initial="{ opacity: 0, y: 6 }" :animate="{ opacity: 1, y: 0 }" :transition="{ delay: 0.12 }">
            <el-input
              v-model="keyword"
              placeholder="搜索暑假作业、期末复习、成都中考、时事热点、PBL…"
              size="large"
              clearable
              @keyup.enter="onSearch"
            />
            <el-button type="primary" size="large" @click="onSearch">搜索</el-button>
          </motion.div>
          <motion.div v-if="hotKeywords.length" class="hot-keywords" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.14 }">
            <span class="hk-label">热搜：</span>
            <button
              v-for="hk in hotKeywords"
              :key="hk.keyword"
              type="button"
              class="hk-chip"
              @click="applyHotKeyword(hk.keyword)"
            >
              {{ hk.keyword }}
            </button>
          </motion.div>
        </motion.div>
        <motion.aside class="hero-actions" :initial="{ opacity: 0, x: 8 }" :animate="{ opacity: 1, x: 0 }" :transition="{ delay: 0.1 }">
          <el-button class="upload-cta" size="large" @click="handleUploadClick">
            <span class="upload-cta-icon">📤</span>
            上传专题资源
          </el-button>
          <p class="upload-cta-hint">作业、试卷、讲义、教案、视频、项目方案</p>
        </motion.aside>
      </motion.div>
    </section>

    <motion.div
      v-if="calendarHint"
      class="calendar-banner-wrap"
      :initial="{ opacity: 0, y: 8 }"
      :animate="{ opacity: 1, y: 0 }"
      :transition="{ delay: 0.14 }"
    >
      <motion.div class="calendar-banner card" :whileHover="{ scale: 1.005 }">
        <span class="cb-icon">{{ calendarHint.icon }}</span>
        <div class="cb-body">
          <strong>{{ calendarHint.schoolYear }} 学年 · {{ calendarHint.title }}</strong>
          <p>{{ calendarHint.desc }}</p>
        </div>
        <el-button type="primary" plain @click="applyCalendarHint">进入推荐专题</el-button>
      </motion.div>
    </motion.div>

    <motion.div
      class="main-layout"
      :initial="{ opacity: 0, y: 16 }"
      :animate="{ opacity: 1, y: 0 }"
      :transition="{ delay: 0.15, duration: 0.35 }"
    >
      <aside class="left-sidebar card">
        <h3 class="sidebar-title">专题场景</h3>
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
      </aside>

      <main class="main-content">
        <motion.div class="filter-bar card" :initial="{ opacity: 0, y: 8 }" :animate="{ opacity: 1, y: 0 }" :transition="{ delay: 0.22 }">
          <motion.div class="region-quick" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.24 }">
            <button
              type="button"
              class="region-card"
              :class="{ active: activeRegion === 'chengdu' }"
              @click="activeRegion = activeRegion === 'chengdu' ? 'all' : 'chengdu'"
            >
              <span class="rc-icon">🐼</span>
              <span class="rc-title">成都片区</span>
              <span class="rc-desc">期末、中考、暑假作业</span>
            </button>
            <button
              type="button"
              class="region-card"
              :class="{ active: activeRegion === 'mianyang' }"
              @click="activeRegion = activeRegion === 'mianyang' ? 'all' : 'mianyang'"
            >
              <span class="rc-icon">🏔️</span>
              <span class="rc-title">绵阳片区</span>
              <span class="rc-desc">中考冲刺、寒假精选</span>
            </button>
          </motion.div>
          <motion.div class="filter-group" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.26 }">
            <span class="filter-label">地域</span>
            <el-radio-group v-model="activeRegion" size="small">
              <el-radio-button v-for="r in regionOptions" :key="r.key" :label="r.key">{{ r.name }}</el-radio-button>
            </el-radio-group>
          </motion.div>
          <motion.div class="filter-group" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.27 }">
            <span class="filter-label">学段</span>
            <el-radio-group v-model="activeGrade" size="small">
              <el-radio-button v-for="g in gradeOptions" :key="g.key" :label="g.key">{{ g.name }}</el-radio-button>
            </el-radio-group>
          </motion.div>
          <motion.div class="filter-group" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.28 }">
            <span class="filter-label">学科</span>
            <el-radio-group v-model="activeSubject" size="small">
              <el-radio-button v-for="s in subjectOptions" :key="s.key" :label="s.key">{{ s.name }}</el-radio-button>
            </el-radio-group>
          </motion.div>
          <motion.div class="filter-group" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.29 }">
            <span class="filter-label">资源形态</span>
            <el-radio-group v-model="activeFormat" size="small">
              <el-radio-button v-for="f in formatOptions" :key="f.key" :label="f.key">{{ f.name }}</el-radio-button>
            </el-radio-group>
          </motion.div>
          <motion.div class="filter-group" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.3 }">
            <span class="filter-label">权限</span>
            <el-radio-group v-model="activeLevel" size="small">
              <el-radio-button v-for="l in levelOptions" :key="l.key" :label="l.key">{{ l.name }}</el-radio-button>
            </el-radio-group>
          </motion.div>
        </motion.div>

        <motion.section class="section-card card" :initial="{ opacity: 0, y: 10 }" :animate="{ opacity: 1, y: 0 }" :transition="{ delay: 0.32 }">
          <motion.div class="section-head" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.34 }">
            <h2>{{ channelInfo.eliteTitle || '🏅 热门专题精选' }}</h2>
            <span class="section-meta">共 {{ albumTotal }} 套 · {{ channelInfo.eliteDesc }}</span>
          </motion.div>
          <motion.div v-if="displayAlbums.length" class="package-grid" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.36 }">
            <motion.div
              v-for="(album, index) in displayAlbums"
              :key="album.id"
              class="package-card"
              :initial="{ opacity: 0, y: 12 }"
              :animate="{ opacity: 1, y: 0 }"
              :transition="{ delay: 0.05 * index }"
              :whileHover="{ y: -2 }"
              @click="openAlbumDrawer(album)"
            >
              <motion.div class="pkg-cover">{{ album.icon || '🏅' }}</motion.div>
              <motion.div class="pkg-body" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.08 + 0.04 * index }">
                <h3>{{ album.title }}</h3>
                <p class="pkg-summary">{{ album.summary || '' }}</p>
                <div class="pkg-meta">
                  <span>{{ album.resourceCount || 0 }} 个资源</span>
                  <span>↓ {{ formatCount(album.downloadCount) }}</span>
                </div>
              </motion.div>
            </motion.div>
          </motion.div>
          <el-empty v-else description="暂无精品专辑" />
        </motion.section>

        <motion.section
          v-if="useDedicatedApi && (hotResources.length || latestResources.length)"
          class="section-card card rank-section"
          :initial="{ opacity: 0, y: 10 }"
          :animate="{ opacity: 1, y: 0 }"
          :transition="{ delay: 0.36 }"
        >
          <div class="rank-columns">
            <div v-if="hotResources.length" class="rank-col">
              <h3>🔥 下载排行</h3>
              <ul class="rank-list">
                <li v-for="(item, idx) in hotResources" :key="item.id" @click="openResource(item)">
                  <span class="rank-no">{{ idx + 1 }}</span>
                  <span class="rank-title">{{ item.title }}</span>
                  <span class="rank-dl">↓{{ formatCount(item.downloadCount) }}</span>
                </li>
              </ul>
            </div>
            <motion.div v-if="latestResources.length" class="rank-col" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.1 }">
              <h3>🆕 最新上架</h3>
              <ul class="rank-list">
                <li v-for="item in latestResources" :key="item.id" @click="openResource(item)">
                  <span class="rank-no new">新</span>
                  <span class="rank-title">{{ item.title }}</span>
                </li>
              </ul>
            </motion.div>
          </div>
        </motion.section>

        <motion.section class="section-card card" :initial="{ opacity: 0, y: 10 }" :animate="{ opacity: 1, y: 0 }" :transition="{ delay: 0.38 }">
          <motion.div class="section-head list-head" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.4 }">
            <h2>📚 专题资源库</h2>
            <div class="list-head-right">
              <span class="section-meta">共 {{ total }} 条</span>
              <el-select v-model="sortBy" size="small" style="width: 130px" @change="currentPage = 1">
                <el-option label="综合排序" value="sort" />
                <el-option label="下载最多" value="downloadCount" />
                <el-option label="最新上传" value="createTime" />
              </el-select>
            </div>
          </motion.div>
          <motion.div v-if="resources.length" class="resource-grid" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.42 }">
            <motion.div
              v-for="(item, index) in resources"
              :key="item.id"
              class="resource-card"
              :initial="{ opacity: 0, y: 8 }"
              :animate="{ opacity: 1, y: 0 }"
              :transition="{ delay: 0.03 * index }"
              :whileHover="{ backgroundColor: '#f5f3ff' }"
              @click="openResource(item)"
            >
              <span class="res-icon">{{ item.icon || typeIcon(item.resourceForm) }}</span>
              <motion.div class="res-body" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.04 * index }">
                <h4>{{ item.title }}</h4>
                <p>{{ item.summary || item.description }}</p>
                <motion.div class="res-meta" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.05 * index }">
                  <el-tag v-if="item.region && item.region !== 'all'" size="small" type="warning">{{ regionLabel(item.region) }}</el-tag>
                  <el-tag v-if="item.gradeStage || item.gradeLevel" size="small">{{ gradeLabel(item.gradeStage || item.gradeLevel) }}</el-tag>
                  <el-tag size="small" type="info">{{ typeLabel(item.resourceForm || item.resourceType) }}</el-tag>
                  <span v-if="item.isFree === 1" class="free-tag">免费</span>
                  <span v-else class="vip-tag">精品</span>
                  <span>↓ {{ formatCount(item.downloadCount) }}</span>
                </motion.div>
              </motion.div>
            </motion.div>
          </motion.div>
          <el-empty v-else description="暂无匹配资源，可调整筛选或上传资料" />
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

    <TopicUploadDialog
      v-model="uploadDialogVisible"
      :categories="categories"
      :regions="regions"
      :grade-stages="gradeStages"
      :resource-forms="resourceForms"
      @success="refreshAll"
    />

    <el-drawer v-model="albumDrawerVisible" :title="currentAlbum?.title" size="480px">
      <template v-if="currentAlbum">
        <p class="drawer-summary">{{ currentAlbum.summary }}</p>
        <div class="drawer-tags">
          <el-tag type="warning">{{ currentAlbum.resourceCount || 0 }} 个资源</el-tag>
          <el-tag v-if="currentAlbum.isElite" type="danger" effect="plain">精品</el-tag>
        </div>
        <h4>包含资源（{{ albumResources.length }}）</h4>
        <ul class="drawer-list">
          <li v-for="r in albumResources" :key="r.id" @click="openResource(r)">
            {{ r.icon || '📚' }} {{ r.title }}
          </li>
        </ul>
        <el-button type="primary" style="width:100%; margin-top:16px;" @click="downloadAlbum(currentAlbum.id)">
          下载精品专辑
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
import TopicUploadDialog from '@/components/topic/TopicUploadDialog.vue'
import { topicApi, type TopicResourceItem, type TopicAlbumItem } from '@/api/topic'
import { useTopicZone } from '@/composables/useTopicZone'
import {
  TOPIC_REGIONS,
  TOPIC_GRADE_STAGES,
  TOPIC_SUBJECTS,
  TOPIC_FORMATS,
  TOPIC_LEVELS,
  TOPIC_REGION_NAMES,
  TOPIC_FORM_NAMES,
} from '@/constants/topicZone'

const router = useRouter()
const userStore = useUserStore()

const uploadDialogVisible = ref(false)
const albumDrawerVisible = ref(false)
const currentAlbum = ref<TopicAlbumItem | null>(null)
const albumResources = ref<TopicResourceItem[]>([])

const {
  loading,
  keyword,
  activeTab,
  activeRegion,
  activeGrade,
  activeSubject,
  activeFormat,
  activeLevel,
  currentPage,
  pageSize,
  albums,
  albumTotal,
  resources,
  total,
  categories,
  regions,
  gradeStages,
  resourceForms,
  channelInfo,
  useDedicatedApi,
  formatCount,
  onSearch,
  openResource,
  refreshAll,
  zoneStats,
  calendarHint,
  hotKeywords,
  hotResources,
  latestResources,
  displayStats,
  sortBy,
  applyCalendarHint,
  applyHotKeyword,
} = useTopicZone()

const displayAlbums = computed(() => albums.value)

const sidebarTabs = computed(() => categories.value)
const regionOptions = TOPIC_REGIONS
const gradeOptions = TOPIC_GRADE_STAGES
const subjectOptions = TOPIC_SUBJECTS
const formatOptions = [
  { key: 'all', name: '不限' },
  ...TOPIC_FORMATS.filter((f) => f.key !== 'all').map((f) => ({ key: f.key, name: f.name })),
]
const levelOptions = TOPIC_LEVELS

const TYPE_ICONS: Record<string, string> = {
  courseware: '📊',
  ppt: '📊',
  lesson_plan: '📋',
  exam: '📝',
  video: '🎬',
  document: '📄',
  doc: '📄',
  material: '📑',
  exercise: '✏️',
}

function typeLabel(t?: string) {
  return TOPIC_FORM_NAMES[t || ''] || '资料'
}
function typeIcon(t?: string) {
  return TYPE_ICONS[t || ''] || '📚'
}
function gradeLabel(g?: string) {
  const map: Record<string, string> = { primary: '小学', junior: '初中', senior: '高中' }
  return map[g || ''] || g || ''
}
function regionLabel(r?: string) {
  return TOPIC_REGION_NAMES[r || ''] || r || ''
}

async function openAlbumDrawer(album: TopicAlbumItem) {
  if (!useDedicatedApi.value) {
    keyword.value = album.title
    onSearch()
    return
  }
  currentAlbum.value = album
  albumDrawerVisible.value = true
  albumResources.value = []
  try {
    const res = await topicApi.getAlbum(album.id)
    albumResources.value = res.data.data?.resources || []
  } catch {
    ElMessage.warning('专辑详情加载失败')
  }
}

async function downloadAlbum(id: number) {
  try {
    await topicApi.downloadAlbum(id)
    ElMessage.success('已记录下载')
    refreshAll()
  } catch (e: unknown) {
    const err = e as { message?: string }
    ElMessage.error(err.message || '操作失败')
  }
}

function handleUploadClick() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再上传资源')
    router.push({ path: '/login', query: { redirect: '/topic-zone' } })
    return
  }
  if (useDedicatedApi.value) {
    uploadDialogVisible.value = true
  } else {
    router.push({ path: '/upload', query: { channel: 'topic' } })
  }
}
</script>

<style scoped>
.topic-page { min-height: 100vh; background: var(--bg-page, #f5f6f8); }
.topic-hero { color: #fff; padding: 32px 0 40px; }
.hero-inner {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
  flex-wrap: wrap;
}
.hero-main { flex: 1; min-width: 280px; }
.hero-main h1 { font-size: 28px; font-weight: 700; margin: 8px 0; }
.hero-main p { opacity: 0.92; margin-bottom: 8px; max-width: 640px; }
.hero-hint { font-size: 13px; opacity: 0.85; margin-bottom: 12px; }
.hero-stats { display: flex; gap: 20px; margin-bottom: 16px; font-size: 14px; }
.hero-stats b { font-size: 18px; margin-right: 4px; }
.hero-search { display: flex; gap: 8px; max-width: 520px; }
.hot-keywords {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  max-width: 640px;
}
.hk-label { font-size: 13px; opacity: 0.9; }
.hk-chip {
  border: 1px solid rgba(255, 255, 255, 0.45);
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s;
}
.hk-chip:hover { background: rgba(255, 255, 255, 0.3); }

.topic-page :deep(.calendar-banner-wrap) {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 16px;
  box-sizing: border-box;
}
.calendar-banner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
  border: 1px solid #ddd6fe;
  border-radius: 12px;
}
.cb-icon { font-size: 36px; flex-shrink: 0; }
.cb-body { flex: 1; min-width: 0; }
.cb-body strong { display: block; font-size: 15px; color: #5b21b6; margin-bottom: 4px; }
.cb-body p { font-size: 13px; color: var(--text-secondary); margin: 0; }

.rank-section { padding: 16px 20px; }
.rank-columns { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.rank-col h3 { font-size: 15px; font-weight: 700; margin-bottom: 12px; }
.rank-list { list-style: none; padding: 0; margin: 0; }
.rank-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
}
.rank-list li:hover { background: #f5f3ff; }
.rank-no {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: #ede9fe;
  color: #6d28d9;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.rank-no.new { background: #dcfce7; color: #15803d; font-size: 10px; }
.rank-title { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rank-dl { font-size: 11px; color: var(--text-secondary); flex-shrink: 0; }

.list-head { align-items: center; }
.list-head-right { display: flex; align-items: center; gap: 12px; }
.hero-actions { text-align: right; }
.upload-cta {
  background: rgba(255, 255, 255, 0.95) !important;
  color: #6d28d9 !important;
  border: none !important;
  font-weight: 600;
}
.upload-cta-hint { font-size: 12px; opacity: 0.85; margin-top: 8px; max-width: 200px; margin-left: auto; }
.breadcrumb { font-size: 13px; margin-bottom: 4px; }
.breadcrumb-link { color: rgba(255, 255, 255, 0.85); text-decoration: none; }
.breadcrumb-sep { margin: 0 6px; opacity: 0.6; }
.breadcrumb-current { opacity: 0.95; }

.topic-page :deep(.main-layout) {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto 40px;
  padding: 0 24px 24px;
  box-sizing: border-box;
}
.topic-page :deep(.left-sidebar) {
  flex: 0 0 220px;
  width: 220px;
  max-width: 220px;
  padding: 16px 12px;
  position: sticky;
  top: 80px;
  align-self: flex-start;
  z-index: 1;
}
.topic-page :deep(.left-sidebar.card:hover) {
  transform: none;
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
.sidebar-item:hover { background: #f5f3ff; }
.sidebar-item.active {
  background: linear-gradient(135deg, #8b5cf6, #6d28d9);
  color: #fff;
  font-weight: 600;
}
.si-icon { font-size: 18px; }

.topic-page :deep(.main-content) {
  flex: 1 1 0;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.topic-page :deep(.filter-bar) { padding: 18px 22px; }
.topic-page :deep(.filter-group) {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 10px 16px;
  margin-bottom: 16px;
}
.topic-page :deep(.filter-group:last-child) { margin-bottom: 0; }
.topic-page :deep(.filter-label) {
  flex: 0 0 72px;
  margin-bottom: 0;
  line-height: 28px;
}
.topic-page :deep(.filter-group .el-radio-group) {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.topic-page :deep(.filter-group .el-radio-button__inner) {
  border-radius: 8px !important;
}
.region-quick { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 16px; }
.region-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 12px 14px;
  border: 2px solid #e9e5ff;
  border-radius: 10px;
  background: #faf9ff;
  cursor: pointer;
  text-align: left;
  transition: border-color 0.2s, background 0.2s;
}
.region-card:hover { border-color: #c4b5fd; }
.region-card.active {
  border-color: #8b5cf6;
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
}
.rc-icon { font-size: 24px; margin-bottom: 4px; }
.rc-title { font-size: 15px; font-weight: 700; color: #5b21b6; }
.rc-desc { font-size: 12px; color: var(--text-secondary); margin-top: 2px; }
.filter-label { font-size: 13px; font-weight: 600; color: var(--text-secondary); }

.section-card { padding: 20px 24px; }
.section-head { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 16px; flex-wrap: wrap; gap: 8px; }
.section-head h2 { font-size: 18px; font-weight: 700; }
.section-meta { font-size: 13px; color: var(--text-secondary); }

.package-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.package-card {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: linear-gradient(135deg, #f5f3ff 0%, #fff 100%);
  border: 1px solid #ddd6fe;
  border-radius: 12px;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}
.package-card:hover { box-shadow: 0 8px 24px rgba(139, 92, 246, 0.2); transform: translateY(-2px); }
.pkg-cover {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
}
.pkg-body h3 { font-size: 15px; font-weight: 700; margin-bottom: 6px; }
.pkg-summary { font-size: 13px; color: var(--text-secondary); margin-bottom: 8px; }
.pkg-meta { font-size: 12px; color: var(--text-secondary); display: flex; justify-content: space-between; }

.resource-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.resource-card {
  display: flex;
  gap: 12px;
  padding: 14px;
  background: var(--bg-body, #f9fafb);
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}
.res-icon { font-size: 28px; flex-shrink: 0; }
.res-body h4 { font-size: 14px; font-weight: 600; margin-bottom: 4px; }
.res-body p {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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
.drawer-list li:hover { background: #f5f3ff; }

@media (max-width: 900px) {
  .topic-page :deep(.main-layout) { flex-direction: column; }
  .topic-page :deep(.left-sidebar) {
    flex: 1 1 auto;
    width: 100%;
    max-width: none;
    position: static;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  .topic-page :deep(.filter-group) { flex-direction: column; }
  .topic-page :deep(.filter-label) { flex: none; }
  .sidebar-item { width: auto; }
  .package-grid, .resource-grid, .region-quick, .rank-columns { grid-template-columns: 1fr; }
  .calendar-banner { flex-direction: column; align-items: flex-start; }
  .hero-inner { flex-direction: column; }
  .hero-actions { text-align: left; }
}
</style>
