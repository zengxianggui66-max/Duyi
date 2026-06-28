<template>
  <div class="version-resource-page">
    <!-- ===================== 顶部标签栏 ===================== -->
    <div class="top-tabs-bar">
      <div class="container">
        <div class="top-tabs">
          <span
            v-for="tab in topTabs"
            :key="tab.key"
            class="top-tab-item"
            :class="{ active: activeTopTab === tab.key }"
            @click="activeTopTab = tab.key"
          >
            {{ tab.name }}
          </span>
        </div>
      </div>
    </div>

    <!-- ===================== 页面标题栏（彩色背景）===================== -->
    <div class="page-header-bar" :style="{ background: headerGradient }">
      <div class="container">
        <!-- 面包屑导航 -->
        <div class="header-breadcrumb">
          <span class="header-breadcrumb-item">首页</span>
          <span class="header-breadcrumb-sep">/</span>
          <span class="header-breadcrumb-item">{{ gradeName }}</span>
          <span class="header-breadcrumb-sep">/</span>
          <span class="header-breadcrumb-item">{{ subjectName }}</span>
          <span class="header-breadcrumb-sep">/</span>
          <span class="header-breadcrumb-item current">{{ versionName }}</span>
        </div>
        <!-- 页面标题 -->
        <h1 class="page-header-title">{{ versionName }} {{ gradeName }} {{ subjectName }}资源</h1>
      </div>
    </div>

    <!-- ===================== 主体区域 ===================== -->
    <div class="container grade-body">
      <!-- 左侧筛选栏 -->
      <aside class="filter-sidebar">
        <!-- 年级筛选 -->
        <div class="filter-section">
          <div class="filter-section-title">年级</div>
          <div class="filter-options">
            <span
              v-for="g in gradeOptions"
              :key="g.key"
              class="filter-option"
              :class="{ active: selectedGrade === g.key }"
              @click="selectedGrade = selectedGrade === g.key ? '' : g.key"
            >
              {{ g.name }}
            </span>
          </div>
        </div>

        <!-- 册次筛选 -->
        <div class="filter-section">
          <div class="filter-section-title">册次</div>
          <div class="filter-options">
            <span
              v-for="v in volumeOptions"
              :key="v.key"
              class="filter-option"
              :class="{ active: selectedVolume === v.key }"
              @click="selectedVolume = selectedVolume === v.key ? '' : v.key"
            >
              {{ v.name }}
            </span>
          </div>
        </div>

        <!-- 类别筛选 -->
        <div class="filter-section">
          <div class="filter-section-title">类别</div>
          <div class="filter-options">
            <span
              v-for="c in categoryOptions"
              :key="c.key"
              class="filter-option"
              :class="{ active: selectedCategory === c.key }"
              @click="selectedCategory = selectedCategory === c.key ? '' : c.key"
            >
              {{ c.name }}
            </span>
          </div>
        </div>

        <!-- 文件格式筛选 -->
        <div class="filter-section">
          <div class="filter-section-title">文件格式</div>
          <div class="filter-options">
            <span
              v-for="f in formatOptions"
              :key="f.key"
              class="filter-option"
              :class="{ active: selectedFormat === f.key }"
              @click="selectedFormat = selectedFormat === f.key ? '' : f.key"
            >
              {{ f.name }}
            </span>
          </div>
        </div>
      </aside>

      <!-- 右侧内容区 -->
      <main class="content-area">
        <!-- 资源排序和数量 -->
        <div class="content-header">
          <div class="result-count">共找到 <strong>{{ filteredResources.length }}</strong> 个资源</div>
          <div class="sort-options">
            <span class="sort-label">排序：</span>
            <select v-model="sortType" class="sort-select">
              <option value="default">默认排序</option>
              <option value="download">下载最多</option>
              <option value="recent">最新上传</option>
              <option value="score">评分最高</option>
            </select>
          </div>
        </div>

        <!-- 资源卡片列表 -->
        <div class="resource-grid">
          <div
            v-for="resource in paginatedResources"
            :key="resource.id"
            class="resource-card"
            @click="goToDetail(resource.id)"
          >
            <div class="resource-icon">{{ resource.icon }}</div>
            <div class="resource-info">
              <div class="resource-title">{{ resource.title }}</div>
              <div class="resource-meta">
                <span class="resource-format">{{ resource.format }}</span>
                <span class="resource-size">{{ resource.size }}</span>
                <span class="resource-download">{{ resource.downloadCount }} 下载</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination">
          <button
            class="page-btn"
            :disabled="currentPage === 1"
            @click="currentPage--"
          >
            ‹ 上一页
          </button>
          <span class="page-info">
            第 {{ currentPage }} / {{ totalPages }} 页
          </span>
          <button
            class="page-btn"
            :disabled="currentPage === totalPages"
            @click="currentPage++"
          >
            下一页 ›
          </button>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

// 路由参数
const route = useRoute()
const router = useRouter()

// 获取路由参数
const gradeKey = computed(() => route.params.grade as string || 'grade1')
const subjectKey = computed(() => route.params.subject as string || 'chinese_p')
const versionKey = computed(() => route.params.version as string || 'tongbian')

// 顶部标签
const topTabs = [
  { key: 'sync', name: '同步' },
  { key: 'knowledge', name: '知识点' },
  { key: 'homework', name: '作业' },
  { key: 'paper', name: '试卷' },
  { key: 'composition', name: '作文' },
  { key: 'premium', name: '精品' },
  { key: 'school', name: '名校' },
  { key: 'book', name: '教辅' },
  { key: 'training', name: '研修' },
  { key: 'prepare', name: '备课' },
  { key: 'compose', name: '组卷' },
]
const activeTopTab = ref('sync')

// 筛选状态
const selectedGrade = ref('')
const selectedVolume = ref('')
const selectedCategory = ref('')
const selectedFormat = ref('')
const sortType = ref('default')
const currentPage = ref(1)
const pageSize = 20

// 配置数据
const gradeConfig: Record<string, { name: string; color: string }> = {
  grade1: { name: '一年级', color: '#435EEA' },
  grade2: { name: '二年级', color: '#435EEA' },
  grade3: { name: '三年级', color: '#435EEA' },
  grade4: { name: '四年级', color: '#435EEA' },
  grade5: { name: '五年级', color: '#435EEA' },
  grade6: { name: '六年级', color: '#435EEA' },
  grade7: { name: '初一', color: '#00B894' },
  grade8: { name: '初二', color: '#00B894' },
  grade9: { name: '初三', color: '#00B894' },
  grade10: { name: '高一', color: '#6C5CE7' },
  grade11: { name: '高二', color: '#6C5CE7' },
  grade12: { name: '高三', color: '#6C5CE7' },
}

const subjectConfig: Record<string, { name: string; icon: string }> = {
  chinese_p: { name: '语文', icon: '📖' },
  math_p: { name: '数学', icon: '📐' },
  english_p: { name: '英语', icon: '🔤' },
  science: { name: '科学', icon: '🔬' },
  morality: { name: '道德与法治', icon: '⚖️' },
  art_p: { name: '美术', icon: '🎨' },
  music: { name: '音乐', icon: '🎵' },
  pe: { name: '体育', icon: '⚽' },
}

const versionConfig: Record<string, string> = {
  tongbian: '统编版',
  tongbian2016: '统编版(2016)',
  tongbian54: '统编版(五四制)',
  tongbian542018: '统编版(五四制)(2018)',
  pep: '人教版',
  bsd: '北师大版',
  suke: '苏教版',
}

const gradeName = computed(() => gradeConfig[gradeKey.value]?.name || '一年级')
const subjectName = computed(() => subjectConfig[subjectKey.value]?.name || '语文')
const versionName = computed(() => versionConfig[versionKey.value] || '统编版')

const headerGradient = computed(() => {
  const color = gradeConfig[gradeKey.value]?.color || '#435EEA'
  return `linear-gradient(135deg, ${color}, ${color}dd)`
})

// 筛选选项
const gradeOptions = computed(() => {
  // 获取当前学段的所有年级
  const section = gradeKey.value.replace(/[0-9]/g, '')
  const gradeNum = parseInt(gradeKey.value.replace(/[a-z_]/g, ''))
  const options: Array<{ key: string; name: string }> = []
  for (let i = 1; i <= 6; i++) {
    const key = `grade${i}`
    options.push({ key, name: `${i}年级` })
  }
  return options
})

const volumeOptions = [
  { key: 'volume1', name: '上册' },
  { key: 'volume2', name: '下册' },
  { key: 'full', name: '全册' },
]

const categoryOptions = [
  { key: 'courseware', name: '课件' },
  { key: 'lesson_plan', name: '教案' },
  { key: 'study_plan', name: '学案' },
  { key: 'homework', name: '作业' },
  { key: 'test_paper', name: '试卷' },
  { key: 'question_set', name: '题集' },
  { key: 'material', name: '素材' },
  { key: 'prep_package', name: '备课包' },
]

const formatOptions = [
  { key: 'ppt', name: 'PPT' },
  { key: 'word', name: 'Word' },
  { key: 'pdf', name: 'PDF' },
  { key: 'video', name: '视频' },
  { key: 'audio', name: '音频' },
  { key: 'image', name: '图片' },
]

// 模拟资源数据
const allResources = ref([
  { id: 1, title: '第一单元 沁园春·长沙 课件', icon: '📄', format: 'PPT', size: '12.5MB', downloadCount: 2345, grade: 'grade1', volume: 'volume1', category: 'courseware' },
  { id: 2, title: '第二单元 雨巷 教案', icon: '📝', format: 'Word', size: '2.3MB', downloadCount: 1892, grade: 'grade1', volume: 'volume1', category: 'lesson_plan' },
  { id: 3, title: '第三单元 再别康桥 学案', icon: '📋', format: 'Word', size: '1.8MB', downloadCount: 1456, grade: 'grade1', volume: 'volume1', category: 'study_plan' },
  { id: 4, title: '第一单元测试卷', icon: '📃', format: 'Word', size: '1.2MB', downloadCount: 3567, grade: 'grade1', volume: 'volume1', category: 'test_paper' },
  { id: 5, title: '古诗词鉴赏练习题', icon: '📚', format: 'PDF', size: '3.4MB', downloadCount: 2890, grade: 'grade1', volume: 'volume1', category: 'question_set' },
  { id: 6, title: '期中模拟试卷', icon: '📃', format: 'Word', size: '2.1MB', downloadCount: 4231, grade: 'grade1', volume: 'volume1', category: 'test_paper' },
  { id: 7, title: '写作指导课件', icon: '📄', format: 'PPT', size: '8.9MB', downloadCount: 1678, grade: 'grade1', volume: 'volume1', category: 'courseware' },
  { id: 8, title: '文言文知识点总结', icon: '📝', format: 'Word', size: '1.5MB', downloadCount: 3456, grade: 'grade1', volume: 'volume1', category: 'homework' },
])

const filteredResources = computed(() => {
  let result = [...allResources.value]
  
  // 筛选年级
  if (selectedGrade.value) {
    result = result.filter(r => r.grade === selectedGrade.value)
  }
  
  // 筛选册次
  if (selectedVolume.value) {
    result = result.filter(r => r.volume === selectedVolume.value)
  }
  
  // 筛选类别
  if (selectedCategory.value) {
    result = result.filter(r => r.category === selectedCategory.value)
  }
  
  // 排序
  if (sortType.value === 'download') {
    result.sort((a, b) => b.downloadCount - a.downloadCount)
  }
  
  return result
})

const totalPages = computed(() => Math.ceil(filteredResources.value.length / pageSize))

const paginatedResources = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredResources.value.slice(start, start + pageSize)
})

// 跳转详情
function goToDetail(id: number) {
  router.push(`/resource/${id}`)
}

// 重置分页
function resetPagination() {
  currentPage.value = 1
}

// 监听筛选变化
watch([selectedGrade, selectedVolume, selectedCategory, selectedFormat, sortType], () => {
  resetPagination()
})

onMounted(() => {
  // 初始化
})
</script>

<style scoped>
.version-resource-page {
  min-height: 100vh;
  background: #f5f7fa;
}

/* ===================== 顶部标签栏 ===================== */
.top-tabs-bar {
  background: #fff;
  border-bottom: 1px solid #E4E7ED;
  position: sticky;
  top: 64px;
  z-index: 100;
}
.top-tabs {
  display: flex;
  gap: 0;
  overflow-x: auto;
}
.top-tab-item {
  padding: 12px 20px;
  font-size: 15px;
  color: #606266;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
  white-space: nowrap;
}
.top-tab-item:hover {
  color: var(--color-primary);
}
.top-tab-item.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
  font-weight: 600;
}

/* ===================== 页面标题栏 ===================== */
.page-header-bar {
  color: #fff;
  padding: 24px 0;
}
.header-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
}
.header-breadcrumb-item {
  opacity: 0.9;
  cursor: pointer;
}
.header-breadcrumb-item:hover {
  text-decoration: underline;
}
.header-breadcrumb-item.current {
  font-weight: 600;
}
.header-breadcrumb-sep {
  opacity: 0.6;
}
.page-header-title {
  font-size: 26px;
  font-weight: 700;
  margin: 0;
}

/* ===================== 主体区域 ===================== */
.grade-body {
  display: flex;
  gap: 20px;
  padding: 20px 0;
}

/* 左侧筛选栏 */
.filter-sidebar {
  width: 200px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  height: fit-content;
  position: sticky;
  top: 140px;
}
.filter-section {
  margin-bottom: 20px;
}
.filter-section:last-child {
  margin-bottom: 0;
}
.filter-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}
.filter-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.filter-option {
  font-size: 13px;
  color: #606266;
  padding: 6px 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}
.filter-option:hover {
  background: #f5f7fa;
  color: var(--color-primary);
}
.filter-option.active {
  background: var(--color-primary-bg);
  color: var(--color-primary);
  font-weight: 500;
}

/* 右侧内容区 */
.content-area {
  flex: 1;
  min-width: 0;
}
.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
}
.result-count {
  font-size: 14px;
  color: #606266;
}
.result-count strong {
  color: var(--color-primary);
}
.sort-options {
  display: flex;
  align-items: center;
  gap: 8px;
}
.sort-label {
  font-size: 14px;
  color: #909399;
}
.sort-select {
  padding: 6px 12px;
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
  background: #fff;
  cursor: pointer;
}

/* 资源卡片列表 */
.resource-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.resource-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.resource-card:hover {
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}
.resource-icon {
  font-size: 32px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 8px;
}
.resource-info {
  flex: 1;
  min-width: 0;
}
.resource-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.resource-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}
.resource-format {
  background: #ecf5ff;
  color: #409EFF;
  padding: 2px 8px;
  border-radius: 4px;
}
.resource-download {
  color: #67C23A;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 24px 0;
}
.page-btn {
  padding: 8px 16px;
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  background: #fff;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.page-btn:hover:not(:disabled) {
  color: var(--color-primary);
  border-color: var(--color-primary);
}
.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.page-info {
  font-size: 14px;
  color: #606266;
}
</style>

