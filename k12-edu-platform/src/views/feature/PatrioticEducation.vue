<template>
  <div class="patriotic-page">
    <!-- 顶部导航 -->
    <div class="top-nav">
      <ul class="nav-list">
        <li><router-link to="/" class="nav-link">首页</router-link></li>
        <li v-for="(item, idx) in topNavItems" :key="idx">
          <span class="nav-link has-arrow">{{ item.name }} ▾</span>
        </li>
      </ul>
    </div>

    <!-- 面包屑 -->
    <div class="breadcrumb">
      <router-link to="/" class="bc-link">首页</router-link>
      <span class="bc-sep"> / </span>
      <router-link to="/theme-class-meeting" class="bc-link">主题班会</router-link>
      <span class="bc-sep"> / </span>
      <span class="bc-link" @click="backToList">爱国教育</span>
      <span class="bc-sep"> / </span>
      <span class="bc-current">{{ currentTheme }}</span>
    </div>

    <!-- 主体容器 -->
    <div class="container">
      <!-- 左侧分类 -->
      <div class="left-sidebar">
        <h3>爱国教育</h3>
        <ul>
          <li v-for="cat in categories" :key="cat">
            <a
              :class="['category-link', { active: currentTheme === cat }]"
              @click="setTheme(cat)"
            >{{ cat }}</a>
          </li>
        </ul>
      </div>

      <!-- 右侧内容区 -->
      <div class="main-content">
        <!-- 列表页 -->
        <div v-if="!showDetail">
          <!-- 筛选栏 -->
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
              <label><input v-model="premiumOnly" type="checkbox" @change="resetPage"> 精品</label>
              <label><input v-model="freeOnly" type="checkbox" @change="resetPage"> 免费</label>
            </div>
            <div class="result-count">共{{ filteredData.length }}条结果</div>
          </div>

          <!-- 课件列表 -->
          <div class="course-list">
            <div
              v-if="pageData.length === 0"
              class="empty-tip"
            >暂无相关课件，请试试其他筛选条件~</div>

            <div
              v-for="course in pageData"
              :key="course.id"
              class="course-item"
            >
              <div class="course-title" @click="showDetailById(course.id)">
                {{ course.title }}
              </div>
              <div class="course-preview">
                <img
                  v-for="(img, i) in course.previewImgs.slice(0, 4)"
                  :key="i"
                  :src="img"
                  class="preview-img"
                  alt="预览图"
                  loading="lazy"
                />
              </div>
              <div class="course-meta">
                <span class="type-tag">{{ course.type }}</span>
                <span>📅 {{ course.date }}</span>
                <span>⬇️ {{ course.downloads }}次下载</span>
                <span>💰 {{ course.price === 0 ? '免费' : course.price + '学贝' }}</span>
                <span>👤 {{ course.author }}</span>
                <span v-if="course.isPremium" class="badge-premium">⭐精品</span>
                <span v-if="course.isFree" class="badge-free">✨免费</span>
              </div>
            </div>
          </div>

          <!-- 分页 -->
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

        <!-- 详情页 -->
        <div v-else class="detail-wrap">
          <h1 class="detail-title">{{ detailCourse?.title }}</h1>
          <div class="preview-wrap">
            <img
              v-for="(img, i) in detailCourse?.detailImgs"
              :key="i"
              :src="img"
              class="preview-item"
              alt="课件预览"
              loading="lazy"
            />
          </div>
          <div class="course-meta" style="margin-bottom: 20px;">
            <span class="type-tag">{{ detailCourse?.type }}</span>
            <span>📅 更新：{{ detailCourse?.date }}</span>
            <span>⬇️ {{ detailCourse?.downloads }}次下载</span>
            <span>💰 {{ detailCourse?.price === 0 ? '免费' : detailCourse?.price + '学贝' }}</span>
            <span>✍️ {{ detailCourse?.author }}</span>
            <span v-if="detailCourse?.isPremium" class="badge-premium">官方精品</span>
            <span v-if="detailCourse?.isFree" class="badge-free">限时免费</span>
          </div>
          <div class="detail-desc">
            <p>📖 课件简介：本课件包含<strong>「{{ detailCourse?.title }}」</strong>完整教学内容，适合中小学爱国主义教育班会使用。内含相关事迹、党史学习、互动问答环节，图文并茂，直接可用。</p>
            <p style="margin-top: 12px;">✨ 教习网提供下载源文件，支持编辑修改。</p>
          </div>
          <button class="back-btn" @click="backToList">← 返回列表</button>
        </div>
      </div>
    </div>

    <!-- 底部说明 -->
    <div class="site-footer">
      <h3>{{ currentTheme }}主题班会PPT课件教案 - 爱国教育 - 教习网</h3>
      <p>教习网专注K12在线教育资源下载服务，优质课件/教案/素材，让教学更轻松。</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'

// ===================== 类型定义 =====================
interface Course {
  id: number
  theme: string
  title: string
  type: string
  date: string
  downloads: number
  price: number
  isFree: boolean
  isPremium: boolean
  author: string
  previewImgs: string[]
  detailImgs: string[]
}

// ===================== 顶部导航 =====================
const topNavItems = [
  { name: '爱国教育' }, { name: '安全教育' }, { name: '德育教育' },
  { name: '环保教育' }, { name: '心理健康' }, { name: '励志教育' },
  { name: '节日主题' }, { name: '更多主题' },
]

// ===================== 分类 =====================
const categories = ['学雷锋', '爱国主义', '革命传统', '勿忘国耻']

// ===================== 筛选 =====================
const typeTabs = [
  { key: 'all', label: '全部' },
  { key: '课件', label: '课件' },
  { key: '教案', label: '教案' },
  { key: '素材', label: '素材' },
]
const sortOptions = [
  { key: 'default', label: '综合' },
  { key: 'new', label: '最新' },
  { key: 'downloads', label: '下载量' },
]

// ===================== 状态 =====================
const route = useRoute()
const currentTheme = ref((route.query.theme as string) || '学雷锋')
const currentType = ref('all')
const currentSort = ref('default')
const premiumOnly = ref(false)
const freeOnly = ref(false)
const currentPage = ref(1)
const pageSize = 5

const showDetail = ref(false)
const detailCourse = ref<Course | null>(null)

// ===================== 数据源 =====================
// 可信化：关闭本地 mock 展示，待后端真实接口接入后替换为 API 加载。
const allCourses = ref<Course[]>([])

// ===================== 计算属性 =====================
const filteredData = computed(() => {
  let result = allCourses.value.filter(c => c.theme === currentTheme.value)
  if (currentType.value !== 'all') result = result.filter(c => c.type === currentType.value)
  if (premiumOnly.value) result = result.filter(c => c.isPremium)
  if (freeOnly.value) result = result.filter(c => c.isFree)
  if (currentSort.value === 'new') result = [...result].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
  else if (currentSort.value === 'downloads') result = [...result].sort((a, b) => b.downloads - a.downloads)
  else result = [...result].sort((a, b) => b.id - a.id)
  return result
})

const totalPages = computed(() => Math.ceil(filteredData.value.length / pageSize))

const pageData = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredData.value.slice(start, start + pageSize)
})

const pageRange = computed(() => {
  const total = totalPages.value
  const cur = currentPage.value
  const start = Math.max(1, cur - 2)
  const end = Math.min(total, start + 4)
  const pages: number[] = []
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

// ===================== 操作 =====================
function setTheme(theme: string) {
  currentTheme.value = theme
  currentType.value = 'all'
  currentSort.value = 'default'
  premiumOnly.value = false
  freeOnly.value = false
  currentPage.value = 1
  showDetail.value = false
}

function setType(type: string) {
  currentType.value = type
  currentPage.value = 1
}

function setSort(sort: string) {
  currentSort.value = sort
  currentPage.value = 1
}

function resetPage() {
  currentPage.value = 1
}

function changePage(p: number) {
  if (p < 1 || p > totalPages.value) return
  currentPage.value = p
  window.scrollTo({ top: 400, behavior: 'smooth' })
}

function showDetailById(id: number) {
  const course = allCourses.value.find(c => c.id === id)
  if (!course) return
  detailCourse.value = course
  showDetail.value = true
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function backToList() {
  showDetail.value = false
  detailCourse.value = null
}

// 监听路由 query 变化（从其他页面带 theme 参数跳转）
watch(() => route.query.theme, (val) => {
  if (val) setTheme(val as string)
})
</script>

<style scoped>
/* ===================== 基础 ===================== */
.patriotic-page {
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* ===================== 顶部导航 ===================== */
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
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.has-arrow {
  cursor: default;
}

/* ===================== 面包屑 ===================== */
.breadcrumb {
  background-color: #fff;
  padding: 12px 20px;
  font-size: 14px;
  color: #666;
  border-bottom: 1px solid #e9ecef;
  max-width: 1200px;
  margin: 0 auto;
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
}

.bc-current {
  color: #333;
}

/* ===================== 主容器 ===================== */
.container {
  width: 1200px;
  margin: 24px auto;
  display: flex;
  gap: 24px;
}

/* ===================== 左侧分类 ===================== */
.left-sidebar {
  width: 200px;
  background-color: #fff;
  border-radius: 12px;
  padding: 16px 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  align-self: start;
  flex-shrink: 0;
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
  text-decoration: none;
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

/* ===================== 右侧主内容 ===================== */
.main-content {
  flex: 1;
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  min-width: 0;
}

/* ===================== 筛选栏 ===================== */
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
  text-decoration: none;
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
  text-decoration: none;
  color: #5a6874;
  cursor: pointer;
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

/* ===================== 课件列表 ===================== */
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
}

.course-title:hover {
  color: #1976D2;
}

.course-preview {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.preview-img {
  width: 140px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
  background-color: #f1f5f9;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s;
}

.preview-img:hover {
  transform: scale(1.02);
}

.course-meta {
  font-size: 13px;
  color: #7e8c9e;
  display: flex;
  gap: 20px;
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

/* ===================== 分页 ===================== */
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
  text-decoration: none;
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

/* ===================== 详情页 ===================== */
.detail-wrap {
  padding: 4px 0;
}

.detail-title {
  font-size: 24px;
  color: #0f2b3d;
  margin-bottom: 20px;
  font-weight: 700;
}

.preview-wrap {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.preview-item {
  width: 180px;
  height: 130px;
  border-radius: 10px;
  object-fit: cover;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.detail-desc {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  margin-top: 10px;
  color: #2d3e50;
  font-size: 14px;
  line-height: 1.7;
}

.back-btn {
  display: inline-block;
  margin-top: 20px;
  background: #f0f4f9;
  padding: 8px 18px;
  border-radius: 30px;
  font-size: 14px;
  cursor: pointer;
  color: #1976D2;
  border: none;
  transition: background 0.2s;
}

.back-btn:hover {
  background: #e3eef9;
}

/* ===================== 底部 ===================== */
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

/* ===================== 响应式 ===================== */
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
  }

  .left-sidebar ul {
    display: flex;
    flex-wrap: wrap;
  }

  .category-link {
    padding: 8px 16px;
    border-right: none;
  }

  .category-link.active {
    border-right: none;
    border-bottom: 2px solid #1976D2;
  }
}

@media (max-width: 600px) {
  .course-preview {
    overflow-x: auto;
  }

  .preview-img {
    flex-shrink: 0;
  }

  .filter-bar {
    flex-wrap: wrap;
  }

  .result-count {
    margin-left: 0;
  }
}
</style>
