<template>
  <div class="culture-page" v-loading="loading">
    <section class="culture-hero">
      <div class="container hero-inner">
        <div class="hero-main">
          <nav class="breadcrumb" aria-label="面包屑">
            <router-link to="/" class="breadcrumb-link">首页</router-link>
            <span class="breadcrumb-sep">/</span>
            <router-link to="/feature" class="breadcrumb-link">特色资源频道</router-link>
            <span class="breadcrumb-sep">/</span>
            <span class="breadcrumb-current">传统文化研学</span>
          </nav>
          <h1>🏮 巴蜀传统文化研学</h1>
          <p>主题 · 地域（成都/巴蜀）· 研学时长 — 面向教培机构研学实践，不分学段</p>
          <div class="hero-search">
            <el-input
              v-model="keyword"
              placeholder="搜索诗词、非遗、研学线路、民间故事…"
              size="large"
              clearable
              @keyup.enter="onSearch"
            />
            <el-button type="primary" size="large" @click="onSearch">搜索</el-button>
          </div>
        </div>
        <aside class="hero-actions">
          <el-button class="upload-cta" size="large" @click="handleUploadClick">
            <span class="upload-cta-icon">📤</span>
            上传传统文化资源
          </el-button>
          <p class="upload-cta-hint">支持教案、手册、讲解词 PDF/PPT，或提交权威外链</p>
        </aside>
      </div>
    </section>

    <CultureUploadDialog
      v-model="uploadDialogVisible"
      :categories="categories"
      :regions="regions"
      :durations="durations"
      @success="refreshAll"
    />

    <div class="container main-layout">
      <aside class="left-sidebar card">
        <h3 class="sidebar-title">主题分类</h3>
        <button
          v-for="cat in categories"
          :key="cat.key"
          type="button"
          class="sidebar-item"
          :class="{ active: activeCategory === cat.key }"
          @click="activeCategory = cat.key"
        >
          <span class="si-icon">{{ cat.icon }}</span>
          <span>{{ cat.name }}</span>
        </button>
      </aside>

      <main class="main-content">
        <div class="filter-bar card">
          <div class="filter-group">
            <span class="filter-label">地域</span>
            <el-radio-group v-model="activeRegion" size="small">
              <el-radio-button v-for="r in regions" :key="r.key" :label="r.key">{{ r.name }}</el-radio-button>
            </el-radio-group>
          </div>
          <div class="filter-group">
            <span class="filter-label">研学时长</span>
            <el-radio-group v-model="activeDuration" size="small">
              <el-radio-button v-for="d in durations" :key="d.key" :label="d.key">{{ d.name }}</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <!-- 研学线路包 -->
        <section class="section-card card">
          <div class="section-head">
            <h2>🎒 成都·巴蜀研学线路</h2>
            <span class="section-meta">共 {{ packageTotal }} 套</span>
          </div>
          <div v-if="packages.length" class="package-grid">
            <div
              v-for="pkg in packages"
              :key="pkg.id"
              class="package-card"
              @click="openPackage(pkg)"
            >
              <div class="pkg-cover">{{ pkg.icon || '🏮' }}</div>
              <div class="pkg-body">
                <h3>{{ pkg.title }}</h3>
                <p class="pkg-summary">{{ pkg.summary }}</p>
                <div class="pkg-tags">
                  <el-tag size="small" type="warning">{{ pkg.durationLabel || pkg.durationType }}</el-tag>
                  <el-tag size="small">{{ pkg.location }}</el-tag>
                  <el-tag v-if="pkg.isElite" size="small" type="danger" effect="plain">精品</el-tag>
                </div>
                <div class="pkg-meta">
                  <span>{{ pkg.suitableAudience }}</span>
                  <span>↓ {{ formatCount(pkg.downloadCount) }}</span>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无匹配的研学线路" />
        </section>

        <!-- 平台自有资源 -->
        <section class="section-card card">
          <div class="section-head">
            <h2>📦 平台研学资源</h2>
            <span class="section-meta">教案 · 手册 · 讲解词 · 任务单</span>
          </div>
          <div v-if="platformResources.length" class="resource-grid">
            <div
              v-for="item in platformResources"
              :key="item.id"
              class="resource-card"
              @click="openResource(item)"
            >
              <div class="res-icon">{{ item.icon || '📜' }}</div>
              <div class="res-body">
                <h4>{{ item.title }}</h4>
                <p>{{ item.summary }}</p>
                <div class="res-meta">
                  <el-tag size="small">{{ item.durationLabel }}</el-tag>
                  <span>{{ item.location }}</span>
                  <span>↓ {{ formatCount(item.downloadCount) }}</span>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无平台资源" />
        </section>

        <!-- 外链延展 C -->
        <section class="section-card card external-section">
          <div class="section-head">
            <h2>🌐 延展阅读（外链策展）</h2>
            <span class="section-meta">官方与权威来源，点击跳转原站</span>
          </div>
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="外链内容由原网站提供，本平台仅作学习导航与研学参考索引。"
            style="margin-bottom: 16px;"
          />
          <div v-if="externalLinks.length" class="external-list">
            <a
              v-for="link in externalLinks"
              :key="link.id"
              class="external-item"
              :href="link.externalUrl"
              target="_blank"
              rel="noopener noreferrer"
              @click="onExternalClick(link)"
            >
              <span class="ext-icon">{{ link.icon || '🌐' }}</span>
              <div class="ext-body">
                <div class="ext-title">{{ link.title }}</div>
                <div class="ext-source">来源：{{ link.sourceName }}</div>
                <p class="ext-summary">{{ link.summary }}</p>
              </div>
              <span class="ext-arrow">↗</span>
            </a>
          </div>
          <el-empty v-else description="暂无外链资源" />
        </section>
      </main>
    </div>

    <!-- 研学包详情 -->
    <el-drawer v-model="packageDrawerVisible" :title="currentPackage?.title" size="480px">
      <template v-if="currentPackage">
        <p class="drawer-summary">{{ currentPackage.summary }}</p>
        <div class="drawer-tags">
          <el-tag>{{ currentPackage.durationLabel }}</el-tag>
          <el-tag type="success">{{ currentPackage.location }}</el-tag>
        </div>
        <h4>包含资源（{{ packageResources.length }}）</h4>
        <ul class="drawer-list">
          <li v-for="r in packageResources" :key="r.id" @click="openResource(r)">
            {{ r.icon }} {{ r.title }}
          </li>
        </ul>
        <el-button type="primary" style="width:100%; margin-top:16px;" @click="downloadPackage(currentPackage.id)">
          下载研学包
        </el-button>
      </template>
    </el-drawer>

    <!-- 平台资源详情 -->
    <el-dialog v-model="resourceDialogVisible" :title="currentResource?.title" width="520px">
      <template v-if="currentResource">
        <p>{{ currentResource.summary }}</p>
        <div class="dialog-meta">
          <el-tag>{{ categoryName(currentResource.category) }}</el-tag>
          <el-tag type="info">{{ currentResource.durationLabel }}</el-tag>
          <el-tag type="success">{{ currentResource.location }}</el-tag>
        </div>
        <p class="dialog-audience">适用：{{ currentResource.suitableAudience }}</p>
        <template v-if="currentResource.resourceKind === 'platform'">
          <el-button type="primary" @click="downloadResource(currentResource.id)">记录下载</el-button>
        </template>
        <template v-else>
          <el-button type="primary" tag="a" :href="currentResource.externalUrl" target="_blank" rel="noopener">
            前往来源网站
          </el-button>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import CultureUploadDialog from '@/components/culture/CultureUploadDialog.vue'
import { useCultureStudy } from '@/composables/useCultureStudy'
import { resourceGateway } from '@/api/resourceGateway'
import { cultureStudyApi, type CultureResourceItem, type CulturePackageItem } from '@/api/cultureStudy'

const router = useRouter()
const userStore = useUserStore()
const uploadDialogVisible = ref(false)

const {
  loading,
  keyword,
  activeCategory,
  activeRegion,
  activeDuration,
  categories,
  regions,
  durations,
  packages,
  platformResources,
  externalLinks,
  packageTotal,
  init,
  refreshAll,
} = useCultureStudy()

const packageDrawerVisible = ref(false)
const resourceDialogVisible = ref(false)
const currentPackage = ref<CulturePackageItem | null>(null)
const packageResources = ref<CultureResourceItem[]>([])
const currentResource = ref<CultureResourceItem | null>(null)

const CATEGORY_NAMES: Record<string, string> = {
  guoxue: '国学经典', shici: '诗歌鉴赏', calligraphy: '书法美术',
  festival: '传统节日', story: '民间故事', customs: '民俗文化',
  bashu: '巴蜀·成都', yanxue: '研学方案',
}

function categoryName(key: string) {
  return CATEGORY_NAMES[key] || key
}

function formatCount(n?: number) {
  if (!n) return '0'
  return n >= 10000 ? `${(n / 10000).toFixed(1)}万` : String(n)
}

function onSearch() {
  refreshAll()
}

function handleUploadClick() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再上传资源')
    router.push({ path: '/login', query: { redirect: '/traditional-culture' } })
    return
  }
  uploadDialogVisible.value = true
}

async function openPackage(pkg: CulturePackageItem) {
  currentPackage.value = pkg
  packageDrawerVisible.value = true
  try {
    const res = await cultureStudyApi.getPackage(pkg.id)
    packageResources.value = res.data.data?.resources || []
  } catch {
    packageResources.value = []
  }
}

async function openResource(item: CultureResourceItem) {
  currentResource.value = item
  resourceDialogVisible.value = true
  try {
    const unifiedDetail = await resourceGateway.getCultureResourceDetail(item.id)
    currentResource.value = {
      ...item,
      ...unifiedDetail,
      id: item.id,
    }
  } catch {
    // ignore detail enrich fallback
  }
  try {
    await resourceGateway.recordBySource('culture_resource', item.id, 'view')
  } catch {
    await cultureStudyApi.viewResource(item.id).catch(() => {})
  }
}

async function downloadResource(id: number) {
  try {
    const result = await resourceGateway.recordBySource('culture_resource', id, 'download')
    if (result?.downloadUrl) {
      window.open(result.downloadUrl, '_blank')
    }
    ElMessage.success('已记录下载')
    refreshAll()
  } catch (e: unknown) {
    try {
      await cultureStudyApi.downloadResource(id)
      ElMessage.success('已记录下载')
      refreshAll()
    } catch (fallbackErr: unknown) {
      const err = fallbackErr as { message?: string }
      ElMessage.error(err.message || '操作失败')
    }
  }
}

async function downloadPackage(id: number) {
  try {
    await cultureStudyApi.downloadPackage(id)
    ElMessage.success('已记录下载')
    refreshAll()
  } catch (e: unknown) {
    const err = e as { message?: string }
    ElMessage.error(err.message || '操作失败')
  }
}

async function onExternalClick(link: CultureResourceItem) {
  try {
    await resourceGateway.recordBySource('culture_resource', link.id, 'view')
  } catch {
    await cultureStudyApi.viewResource(link.id).catch(() => {})
  }
}

onMounted(() => init())
</script>

<style scoped>
.culture-page {
  min-height: calc(100vh - 60px);
  background: var(--bg-body, #f5f6fa);
}
.culture-hero {
  background: linear-gradient(135deg, #7c2d12 0%, #b45309 40%, #d97706 100%);
  color: #fff;
  padding: 32px 0 36px;
}
.hero-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 32px;
}
.hero-main { flex: 1; min-width: 0; }
.breadcrumb {
  margin-bottom: 10px;
  font-size: 13px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
.breadcrumb-link {
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  transition: color 0.2s;
}
.breadcrumb-link:hover { color: #fff; }
.breadcrumb-sep { margin: 0 8px; opacity: 0.55; }
.breadcrumb-current { color: rgba(255, 255, 255, 0.95); font-weight: 500; }
.hero-main h1 { font-size: 28px; font-weight: 800; margin-bottom: 8px; }
.hero-main p { opacity: 0.9; margin-bottom: 20px; font-size: 15px; }
.hero-search { display: flex; gap: 12px; max-width: 560px; }
.hero-search .el-input { flex: 1; }
.hero-actions {
  flex-shrink: 0;
  text-align: center;
  padding-bottom: 4px;
}
.upload-cta {
  height: 48px;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  border: none;
  background: #fff !important;
  color: #b45309 !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}
.upload-cta:hover {
  background: #fffbeb !important;
  color: #92400e !important;
  transform: translateY(-1px);
}
.upload-cta-icon { margin-right: 6px; }
.upload-cta-hint {
  margin-top: 10px;
  font-size: 12px;
  opacity: 0.85;
  max-width: 200px;
  line-height: 1.5;
}

.main-layout {
  display: flex;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  align-items: flex-start;
}
.left-sidebar {
  width: 200px;
  flex-shrink: 0;
  padding: 16px 12px;
  position: sticky;
  top: 80px;
}
.sidebar-title {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 12px;
  padding: 0 8px;
  color: var(--text-secondary);
}
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
.sidebar-item:hover { background: #fef3c7; }
.sidebar-item.active {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: #fff;
  font-weight: 600;
}
.si-icon { font-size: 18px; }

.main-content { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 20px; }
.filter-bar { padding: 16px 20px; }
.filter-group { margin-bottom: 12px; }
.filter-group:last-child { margin-bottom: 0; }
.filter-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.section-card { padding: 20px 24px; }
.section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16px;
}
.section-head h2 { font-size: 18px; font-weight: 700; }
.section-meta { font-size: 13px; color: var(--text-secondary); }

.package-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
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
.package-card:hover {
  box-shadow: 0 8px 24px rgba(217, 119, 6, 0.15);
  transform: translateY(-2px);
}
.pkg-cover {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: #fef3c7;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
}
.pkg-body h3 { font-size: 15px; font-weight: 700; margin-bottom: 6px; }
.pkg-summary {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.pkg-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 8px; }
.pkg-meta {
  font-size: 12px;
  color: var(--text-secondary);
  display: flex;
  justify-content: space-between;
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.resource-card {
  display: flex;
  gap: 12px;
  padding: 14px;
  background: var(--bg-body, #f9fafb);
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}
.resource-card:hover { background: #fef3c7; }
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
.res-meta {
  font-size: 11px;
  color: var(--text-secondary);
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.external-list { display: flex; flex-direction: column; gap: 10px; }
.external-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 10px;
  text-decoration: none;
  color: inherit;
  transition: background 0.2s;
}
.external-item:hover { background: #e0f2fe; }
.ext-icon { font-size: 24px; }
.ext-body { flex: 1; min-width: 0; }
.ext-title { font-weight: 600; font-size: 14px; margin-bottom: 4px; }
.ext-source { font-size: 12px; color: #0369a1; margin-bottom: 4px; }
.ext-summary { font-size: 12px; color: var(--text-secondary); margin: 0; }
.ext-arrow { color: #0369a1; font-size: 18px; }

.drawer-summary { color: var(--text-secondary); margin-bottom: 12px; }
.drawer-tags { display: flex; gap: 8px; margin-bottom: 16px; }
.drawer-list { list-style: none; padding: 0; margin: 0; }
.drawer-list li {
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}
.drawer-list li:hover { background: #fef3c7; }
.dialog-meta { display: flex; flex-wrap: wrap; gap: 8px; margin: 12px 0; }
.dialog-audience { font-size: 13px; color: var(--text-secondary); margin-bottom: 16px; }

@media (max-width: 900px) {
  .hero-inner { flex-direction: column; align-items: stretch; }
  .hero-actions { text-align: left; }
  .upload-cta-hint { max-width: none; }
  .main-layout { flex-direction: column; }
  .left-sidebar { width: 100%; position: static; display: flex; flex-wrap: wrap; gap: 8px; }
  .sidebar-item { width: auto; }
  .package-grid, .resource-grid { grid-template-columns: 1fr; }
}
</style>
