<template>
  <div class="category-page">
    <div class="top-nav">
      <ul class="nav-list">
        <li>
          <router-link to="/theme-class-meeting" class="nav-link">首页</router-link>
        </li>
        <li v-for="nav in topNavItems" :key="nav.key">
          <span
            :class="['nav-link has-arrow', { 'nav-active': currentCategoryKey === nav.key }]"
            @click="switchCategory(nav.key)"
          >{{ nav.label }}</span>
        </li>
      </ul>
    </div>

    <div class="breadcrumb-bar">
      <div class="bc-inner">
        <router-link to="/" class="bc-link">首页</router-link>
        <span class="bc-sep"> / </span>
        <router-link to="/theme-class-meeting" class="bc-link">主题班会</router-link>
        <span class="bc-sep"> / </span>
        <span class="bc-link" @click="resetCategory">{{ currentConfig?.breadCategory }}</span>
        <span class="bc-sep"> / </span>
        <span class="bc-current">{{ currentTheme }}</span>
      </div>
    </div>

    <div class="container">
      <div class="left-sidebar">
        <h3>{{ currentConfig?.sidebarTitle }}</h3>
        <ul>
          <li v-for="cat in currentConfig?.subCategories" :key="cat">
            <a
              :class="['category-link', { active: currentTheme === cat }]"
              @click="setTheme(cat)"
            >{{ cat }}</a>
          </li>
        </ul>
      </div>

      <div class="main-content">
        <div class="filter-bar">
          <div class="filter-tabs">
            <a
              v-for="tab in typeTabs"
              :key="tab.key"
              :class="['filter-tab', { active: currentType === tab.key }]"
              @click="setType(tab.key)"
            >{{ tab.label }}</a>
          </div>
          <div class="sort-links">
            <a
              v-for="s in sortOptions"
              :key="s.key"
              :class="['sort-link', { active: currentSort === s.key }]"
              @click="setSort(s.key)"
            >{{ s.label }}</a>
          </div>
          <div class="filter-options">
            <label><input v-model="premiumOnly" type="checkbox" @change="resetPage"> 精品资源</label>
            <label><input v-model="freeOnly" type="checkbox" @change="resetPage"> 免费资源</label>
          </div>
          <div class="result-count">共 {{ totalCount }} 条结果</div>
        </div>

        <div v-loading="listLoading" class="course-list">
          <div v-if="!listLoading && pageData.length === 0" class="empty-tip">
            暂无相关资源，换个筛选试试~
          </div>
          <div v-for="course in pageData" :key="course.id" class="course-item">
            <div class="course-title" @click="goResourceDetail(course.id)">
              {{ course.title }}
            </div>
            <div class="course-meta">
              <span class="type-tag">{{ course.type }}</span>
              <span>更新 {{ course.date }}</span>
              <span>下载 {{ course.downloads }} 次</span>
              <span>价格 {{ course.price === 0 ? '免费' : course.price + '元' }}</span>
              <span v-if="course.isPremium" class="badge-premium">精品</span>
              <span v-if="course.isFree" class="badge-free">免费</span>
            </div>
          </div>
        </div>

        <div v-if="totalPages > 1" class="pagination-area">
          <div class="pagination">
            <a
              :class="['page-btn', { disabled: currentPage <= 1 }]"
              @click="changePage(currentPage - 1)"
            >上一页</a>
            <a
              v-for="p in pageRange"
              :key="p"
              :class="['page-btn', { active: p === currentPage }]"
              @click="changePage(p)"
            >{{ p }}</a>
            <a
              :class="['page-btn', { disabled: currentPage >= totalPages }]"
              @click="changePage(currentPage + 1)"
            >下一页</a>
          </div>
        </div>
      </div>
    </div>

    <div class="site-footer">
      <h3>{{ currentTheme }}主题班会PPT课件下载 - {{ currentConfig?.breadCategory }} - 新课堂</h3>
      <p>专注 K12 教育信息化，提供海量优质课件/教案/素材，助力教师高效备课。</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { browseApi } from '@/api/browse'
import { unwrapData } from '@/api/request'
import type { PrimaryChineseItem } from '@/api/types'
import {
  THEME_CLASS_MEETING_MODULE,
  CLASS_MEETING_TOP_NAV,
  CLASS_MEETING_NAV_CONFIG,
  resolveClassMeetingCategory,
} from '@/constants/themeClassMeetingNav'

interface Course {
  id: number
  title: string
  type: string
  date: string
  downloads: number
  price: number
  isFree: boolean
  isPremium: boolean
}

const topNavItems = CLASS_MEETING_TOP_NAV
const navConfig = CLASS_MEETING_NAV_CONFIG

const typeTabs = [
  { key: 'all', label: '全部' },
  { key: '课件', label: '课件' },
  { key: '教案', label: '教案' },
  { key: '素材', label: '素材' },
]
const sortOptions = [
  { key: 'default', label: '默认' },
  { key: 'new', label: '最新' },
  { key: 'downloads', label: '下载量' },
]

const route = useRoute()
const router = useRouter()

function getCategoryKey() {
  return resolveClassMeetingCategory((route.params.category as string) || '爱国教育')
}

const currentCategoryKey = ref(getCategoryKey())
const currentConfig = computed(() => navConfig[currentCategoryKey.value])
const defaultTheme = computed(() => currentConfig.value?.subCategories[0] || '')
const currentTheme = ref((route.query.theme as string) || defaultTheme.value)

const currentType = ref('all')
const currentSort = ref('default')
const premiumOnly = ref(false)
const freeOnly = ref(false)
const currentPage = ref(1)
const pageSize = 10
const listLoading = ref(false)
const totalCount = ref(0)
const pageData = ref<Course[]>([])

function mapRecord(item: PrimaryChineseItem): Course {
  const isFree = item.isFree === 1 || item.isFree === undefined
  return {
    id: item.id,
    title: item.title,
    type: item.displayType || item.type || '课件',
    date: item.uploadTime?.slice(0, 10) || '',
    downloads: item.downloadCount ?? 0,
    price: isFree ? 0 : 8,
    isFree,
    isPremium: !isFree,
  }
}

function resolveSortField() {
  if (currentSort.value === 'downloads') return 'downloadCount'
  if (currentSort.value === 'new') return 'createTime'
  return undefined
}

async function loadCourses() {
  listLoading.value = true
  try {
    const res = await browseApi.getPage(
      {
        module: THEME_CLASS_MEETING_MODULE,
        keyword: currentTheme.value,
        displayType: currentType.value !== 'all' ? currentType.value : undefined,
        current: currentPage.value,
        size: pageSize,
        sortField: resolveSortField(),
        sortOrder: 'desc',
      },
      { silentError: true },
    )
    const page = unwrapData(res)
    let records = (page.records || []).map(mapRecord)
    if (premiumOnly.value) records = records.filter((c) => c.isPremium)
    if (freeOnly.value) records = records.filter((c) => c.isFree)
    pageData.value = records
    totalCount.value = page.total ?? records.length
  } catch {
    pageData.value = []
    totalCount.value = 0
  } finally {
    listLoading.value = false
  }
}

const totalPages = computed(() => Math.max(1, Math.ceil(totalCount.value / pageSize)))

const pageRange = computed(() => {
  const total = totalPages.value
  const cur = currentPage.value
  const start = Math.max(1, cur - 2)
  const end = Math.min(total, start + 4)
  const pages: number[] = []
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

function switchCategory(key: string) {
  currentCategoryKey.value = key
  const firstTheme = navConfig[key]?.subCategories[0] || ''
  currentTheme.value = firstTheme
  resetFilters()
  router.replace({ params: { category: key }, query: { theme: firstTheme } })
}

function setTheme(theme: string) {
  currentTheme.value = theme
  resetFilters()
  router.replace({ query: { ...route.query, theme } })
}

function resetCategory() {
  setTheme(currentConfig.value?.subCategories[0] || '')
}

function resetFilters() {
  currentType.value = 'all'
  currentSort.value = 'default'
  premiumOnly.value = false
  freeOnly.value = false
  currentPage.value = 1
}

function setType(type: string) {
  currentType.value = type
  currentPage.value = 1
  loadCourses()
}

function setSort(sort: string) {
  currentSort.value = sort
  currentPage.value = 1
  loadCourses()
}

function resetPage() {
  currentPage.value = 1
  loadCourses()
}

function changePage(p: number) {
  if (p < 1 || p > totalPages.value) return
  currentPage.value = p
  loadCourses()
  window.scrollTo({ top: 400, behavior: 'smooth' })
}

function goResourceDetail(id: number) {
  router.push(`/resource/${id}`)
}

watch(
  () => route.params.category,
  (val) => {
    if (val) {
      currentCategoryKey.value = resolveClassMeetingCategory(val as string)
    }
  },
)

watch(
  () => route.query.theme,
  (val) => {
    if (val && val !== currentTheme.value) {
      currentTheme.value = val as string
      resetFilters()
      loadCourses()
    }
  },
)

watch(currentTheme, () => {
  currentPage.value = 1
  loadCourses()
})

onMounted(loadCourses)
</script>

<style scoped>
.category-page {
  background-color: #f5f7fa;
  min-height: 100vh;
}

.top-nav {
  background-color: #1976D2;
  height: 50px;
  line-height: 50px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.nav-list {
  display: flex;
  list-style: none;
  padding-left: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.nav-link {
  color: #fff;
  text-decoration: none;
  padding: 0 20px;
  display: inline-block;
  font-size: 15px;
  transition: background 0.2s;
  cursor: pointer;
  line-height: 50px;
}

.nav-link:hover,
.nav-link.nav-active {
  background-color: rgba(255, 255, 255, 0.25);
  font-weight: 600;
}

.breadcrumb-bar {
  background-color: #fff;
  border-bottom: 1px solid #e9ecef;
}

.bc-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 12px 20px;
  font-size: 14px;
  color: #666;
}

.bc-link {
  color: #666;
  text-decoration: none;
  cursor: pointer;
}

.bc-link:hover {
  color: #1976D2;
}

.bc-sep {
  margin: 0 4px;
  color: #bbb;
}

.bc-current {
  color: #333;
}

.container {
  width: 1200px;
  margin: 24px auto;
  display: flex;
  gap: 24px;
}

.left-sidebar {
  width: 200px;
  flex-shrink: 0;
  background-color: #fff;
  border-radius: 12px;
  padding: 16px 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  align-self: start;
}

.left-sidebar h3 {
  font-size: 16px;
  padding: 0 20px 12px;
  color: #1e2a3a;
  font-weight: 600;
  border-bottom: 1px solid #edf2f7;
  margin: 0;
}

.left-sidebar ul {
  list-style: none;
  margin-top: 8px;
  padding: 0;
}

.category-link {
  display: block;
  padding: 10px 20px;
  color: #4a5568;
  font-size: 14px;
  transition: all 0.2s;
  cursor: pointer;
}

.category-link.active {
  background-color: #e3f2fd;
  color: #1976D2;
  border-right: 3px solid #1976D2;
  font-weight: 500;
}

.category-link:not(.active):hover {
  background-color: #f8fafc;
  color: #1976D2;
}

.main-content {
  flex: 1;
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  min-width: 0;
}

.filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 20px;
  border-bottom: 1px solid #edf2f7;
  padding-bottom: 12px;
}

.filter-tabs {
  display: flex;
  gap: 24px;
}

.filter-tab {
  color: #5a6874;
  font-size: 15px;
  padding: 4px 0;
  transition: 0.2s;
  cursor: pointer;
}

.filter-tab.active {
  color: #1976D2;
  font-weight: 600;
  border-bottom: 2px solid #1976D2;
}

.sort-links {
  display: flex;
  gap: 18px;
  font-size: 14px;
}

.sort-link {
  color: #5a6874;
  cursor: pointer;
  transition: color 0.2s;
}

.sort-link.active {
  color: #1976D2;
  font-weight: 500;
}

.filter-options {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #4a5568;
}

.filter-options label {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
}

.result-count {
  margin-left: auto;
  font-size: 14px;
  color: #6c7a8a;
}

.course-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 24px;
}

.empty-tip {
  text-align: center;
  padding: 40px;
  color: #8a9bb0;
  font-size: 14px;
}

.course-item {
  border-bottom: 1px solid #edf2f7;
  padding-bottom: 18px;
}

.course-item:last-child {
  border-bottom: none;
}

.course-title {
  font-size: 17px;
  font-weight: 600;
  color: #1e2a3a;
  margin-bottom: 12px;
  cursor: pointer;
  transition: color 0.2s;
  display: inline-block;
  line-height: 1.5;
}

.course-title:hover {
  color: #1976D2;
}

.course-meta {
  font-size: 13px;
  color: #7e8c9e;
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.type-tag {
  background: #eef2ff;
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 12px;
  color: #1976D2;
}

.badge-free {
  background-color: #e6f7e6;
  color: #2e7d32;
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 12px;
}

.badge-premium {
  background-color: #fff3e0;
  color: #ed6c02;
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 12px;
}

.pagination-area {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 12px;
  border-top: 1px solid #edf2f7;
}

.pagination {
  display: flex;
  gap: 6px;
  align-items: center;
  flex-wrap: wrap;
}

.page-btn {
  display: inline-block;
  padding: 6px 12px;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  color: #2c3e50;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  user-select: none;
}

.page-btn.active {
  background-color: #1976D2;
  color: #fff;
  border-color: #1976D2;
}

.page-btn:not(.active):not(.disabled):hover {
  background-color: #f1f5f9;
  border-color: #b9c8f0;
}

.page-btn.disabled {
  color: #bbb;
  cursor: not-allowed;
  pointer-events: none;
}

.site-footer {
  width: 1200px;
  margin: 0 auto 30px;
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.site-footer h3 {
  font-size: 18px;
  color: #1f3a4b;
  margin-bottom: 8px;
}

.site-footer p {
  font-size: 14px;
  color: #6c7a8a;
}

@media (max-width: 1220px) {
  .container,
  .site-footer {
    width: 96%;
  }
}

@media (max-width: 900px) {
  .container {
    flex-direction: column;
  }

  .left-sidebar {
    width: 100%;
    padding: 12px 0;
  }

  .left-sidebar ul {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    padding: 0 12px;
  }

  .category-link {
    padding: 6px 12px;
    border-radius: 6px;
  }

  .category-link.active {
    border-right: none;
    border-bottom: 2px solid #1976D2;
  }

  .nav-list {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
}

@media (max-width: 600px) {
  .filter-bar {
    gap: 10px;
  }

  .result-count {
    margin-left: 0;
  }
}
</style>
