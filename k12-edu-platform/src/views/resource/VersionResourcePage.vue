<template>
  <div class="version-resource-page">
    <!-- ===================== 顶部标签栏（学科网风格）===================== -->
    <section class="top-tab-bar">
      <div class="container">
        <div class="tab-items">
          <span
            v-for="tab in tabs"
            :key="tab.key"
            class="tab-item"
            :class="{ active: activeTab === tab.key }"
            @click="switchTab(tab.key)"
          >
            {{ tab.name }}
            <sup v-if="moduleStatMap[tab.name]" class="tab-count">{{ moduleStatMap[tab.name] }}</sup>
          </span>
        </div>
      </div>
    </section>

    <!-- ===================== 页面标题栏 ===================== -->
    <section class="page-header-bar" :style="{ background: gradeColor }">
      <div class="container">
        <div class="header-breadcrumb">
          <span class="header-breadcrumb-item" @click="goHome">首页</span>
          <span class="header-breadcrumb-sep">/</span>
          <span class="header-breadcrumb-item" @click="goGrade">{{ gradeName }}</span>
          <span class="header-breadcrumb-sep">/</span>
          <span class="header-breadcrumb-item" @click="goSubject">{{ subjectName }}</span>
          <span class="header-breadcrumb-sep">/</span>
          <span class="header-breadcrumb-item current">{{ versionName }}</span>
        </div>
        <h1 class="page-header-title">📚 {{ versionName }} {{ gradeName }} {{ subjectName }}资源</h1>
      </div>
    </section>

    <!-- ===================== 主体区域 ===================== -->
    <div class="container page-body">
      <!-- 横向筛选栏 -->
      <div class="filter-bar">
        <!-- 册次 -->
        <div class="filter-row">
          <span class="filter-label">册次:</span>
          <div class="filter-tags">
            <span
              v-for="vol in volumeOptions"
              :key="vol.key"
              class="filter-tag"
              :class="{ active: selectedVolume === vol.key }"
              @click="selectVolume(vol.key)"
            >
              {{ vol.name }}
            </span>
          </div>
        </div>

        <!-- 类型 -->
        <div class="filter-row">
          <span class="filter-label">类型:</span>
          <div class="filter-tags">
            <span
              v-for="type in typeOptions"
              :key="type.key"
              class="filter-tag"
              :class="{ active: selectedType === type.key }"
              @click="selectType(type.key)"
            >
              {{ type.name }}
            </span>
          </div>
        </div>

        <!-- 关键词搜索 -->
        <div class="filter-row">
          <span class="filter-label">搜索:</span>
          <div class="search-input-wrap">
            <input
              v-model="keyword"
              class="search-input"
              placeholder="输入关键词搜索..."
              @keydown.enter="doSearch"
            />
            <button class="search-btn" @click="doSearch">搜索</button>
          </div>
        </div>
      </div>

      <!-- 资源列表 -->
      <main class="resource-list">
        <!-- 列表头部 -->
        <div class="list-header">
          <span class="result-count">共找到 <strong>{{ apiTotal }}</strong> 个资源</span>
          <div class="sort-dropdown">
            <el-dropdown @command="handleSort">
              <span class="sort-trigger">
                排序: {{ currentSortName }} <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="s in sortOptions" :key="s.key" :command="s.key">
                    {{ s.name }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <span>加载中...</span>
        </div>

        <!-- 资源卡片列表 -->
        <div v-else-if="apiResources.length > 0" class="resource-cards">
          <div
            v-for="resource in apiResources"
            :key="resource.id"
            class="resource-card"
            @click="handlePreview(resource)"
          >
            <div class="resource-icon" :class="getFileIconClass(resource.fileExt)">
              {{ getFileIconLetter(resource.fileExt) }}
            </div>
            <div class="resource-info">
              <div class="resource-title">{{ resource.title }}</div>
              <div class="resource-meta">
                <span v-if="resource.fileExt" class="resource-format">{{ resource.fileExt.toUpperCase() }}</span>
                <span v-if="resource.fileSizeKb" class="resource-size">{{ formatSize(resource.fileSizeKb) }}</span>
                <span class="resource-downloads">📥 {{ resource.downloadCount || 0 }}</span>
                <span class="resource-date">{{ formatDate(resource.uploadTime) }}</span>
                <span v-if="resource.unitName" class="resource-unit">{{ resource.unitName }}</span>
              </div>
            </div>
            <div class="resource-actions" @click.stop>
              <el-button
                type="primary"
                size="small"
                @click="handleDownload(resource)"
              >下载</el-button>
              <el-button size="small" @click="handleCollect(resource)">收藏</el-button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-icon">📂</div>
          <div class="empty-text">暂无资源</div>
          <div class="empty-hint">请尝试其他筛选条件</div>
        </div>

        <!-- 分页 -->
        <div v-if="apiTotal > 0" class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="apiTotal"
            layout="prev, pager, next, total"
            background
            @current-change="handlePageChange"
          />
        </div>
      </main>
    </div>

    <!-- ===================== 资源详情弹窗 ===================== -->
    <el-dialog
      v-model="previewVisible"
      :title="currentResource?.title"
      width="660px"
      class="resource-preview-dialog"
    >
      <div v-if="currentResource" class="preview-content">
        <div class="preview-meta-grid">
          <div class="preview-meta-item" v-if="currentResource.gradeName">
            <span class="meta-label">年级:</span>
            <span class="meta-value">{{ currentResource.gradeName }}</span>
          </div>
          <div class="preview-meta-item" v-if="currentResource.editionName">
            <span class="meta-label">版本:</span>
            <span class="meta-value">{{ currentResource.editionName }}</span>
          </div>
          <div class="preview-meta-item" v-if="currentResource.moduleName">
            <span class="meta-label">栏目:</span>
            <span class="meta-value">{{ currentResource.moduleName }}</span>
          </div>
          <div class="preview-meta-item" v-if="currentResource.resourceTypeName">
            <span class="meta-label">类型:</span>
            <span class="meta-value">{{ currentResource.resourceTypeName }}</span>
          </div>
          <div class="preview-meta-item" v-if="currentResource.unitName">
            <span class="meta-label">单元:</span>
            <span class="meta-value">{{ currentResource.unitName }}</span>
          </div>
          <div class="preview-meta-item" v-if="currentResource.fileExt">
            <span class="meta-label">格式:</span>
            <span class="meta-value">{{ (currentResource.fileExt || '').toUpperCase() }}</span>
          </div>
          <div class="preview-meta-item" v-if="currentResource.fileSizeKb">
            <span class="meta-label">大小:</span>
            <span class="meta-value">{{ formatSize(currentResource.fileSizeKb) }}</span>
          </div>
          <div class="preview-meta-item">
            <span class="meta-label">下载:</span>
            <span class="meta-value">{{ currentResource.downloadCount || 0 }} 次</span>
          </div>
          <div class="preview-meta-item">
            <span class="meta-label">浏览:</span>
            <span class="meta-value">{{ currentResource.viewCount || 0 }} 次</span>
          </div>
          <div class="preview-meta-item" v-if="currentResource.uploadTime">
            <span class="meta-label">上传:</span>
            <span class="meta-value">{{ formatDate(currentResource.uploadTime) }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="previewVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleDownload(currentResource!)">
            📥 下载资源
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, Loading } from '@element-plus/icons-vue'
import { resourceGateway } from '@/api/resourceGateway'

const route = useRoute()
const router = useRouter()

// 路由参数
const grade = computed(() => route.params.grade as string)
const subject = computed(() => route.params.subject as string)
const version = computed(() => route.params.version as string)

// ===== 名称映射 =====
const gradeNameMap: Record<string, string> = {
  grade1: '一年级', grade2: '二年级', grade3: '三年级',
  grade4: '四年级', grade5: '五年级', grade6: '六年级',
  grade7: '初一', grade8: '初二', grade9: '初三',
  grade10: '高一', grade11: '高二', grade12: '高三',
}
const subjectNameMap: Record<string, string> = {
  chinese_p: '语文', math_p: '数学', english_p: '英语',
  science: '科学', morality: '道德与法治',
  chinese: '语文', math: '数学', english: '英语',
}
const versionNameMap: Record<string, string> = {
  tongbian: '统编版', tongbian2024: '统编版(2024)',
  pep: '人教版', bsd: '北师大版', suke: '苏教版',
}

const gradeName = computed(() => gradeNameMap[grade.value] || grade.value)
const subjectName = computed(() => subjectNameMap[subject.value] || subject.value)
const versionName = computed(() => versionNameMap[version.value] || version.value)

// ===== 顶部 Tab（栏目） =====
const tabs = [
  { key: '同步备课', name: '同步备课' },
  { key: '单元测试', name: '单元测试' },
  { key: '期中专区', name: '期中' },
  { key: '期末专区', name: '期末' },
  { key: '专题复习', name: '专题复习' },
  { key: '真题汇编', name: '真题汇编' },
  { key: '知识点',   name: '知识点' },
  { key: '作文专区', name: '作文' },
  { key: '阅读理解', name: '阅读' },
  { key: '竞赛专区', name: '竞赛' },
]
const activeTab = ref('同步备课')
// 各栏目资源数量统计
const moduleStatMap = ref<Record<string, number>>({})

// ===== 筛选状态 =====
const selectedVolume = ref('') // ''=全部, '上册', '下册', '全册'
const selectedType = ref('')   // ''=全部 或 类型名称
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(15)

const volumeOptions = [
  { key: '', name: '全部' },
  { key: '上册', name: '上册' },
  { key: '下册', name: '下册' },
  { key: '全册', name: '全册' },
]

const typeOptions = [
  { key: '', name: '不限' },
  { key: '课件', name: '课件' },
  { key: '教案', name: '教案' },
  { key: '学案', name: '学案' },
  { key: '练习', name: '练习' },
  { key: '试卷', name: '试卷' },
  { key: '教师用书', name: '教师用书' },
  { key: '知识点', name: '知识点' },
  { key: '视频', name: '视频' },
  { key: '素材', name: '素材' },
]

// ===== 排序 =====
const currentSortKey = ref('download_count')
const sortOptions = [
  { key: 'download_count', name: '下载量' },
  { key: 'upload_time',    name: '更新时间' },
  { key: 'view_count',     name: '浏览量' },
  { key: 'file_size_kb',   name: '文件大小' },
]
const currentSortName = computed(() =>
  sortOptions.find(s => s.key === currentSortKey.value)?.name || '下载量'
)

// ===== API 状态 =====
const apiResources = ref<any[]>([])
const apiTotal = ref(0)
const loading = ref(false)

// 资源详情弹窗
const previewVisible = ref(false)
const currentResource = ref<any>(null)

// ===== 数据加载 =====

/** 构造 API 查询参数（映射前端状态到后端参数）*/
function buildParams() {
  // gradeName 映射：路由参数 grade1 → "一年级"；若有册次 → "一年级上册"
  let gradeFull = gradeName.value
  if (selectedVolume.value) {
    gradeFull = gradeName.value + selectedVolume.value
  }

  return {
    stage:    '小学',
    subject:  subjectName.value,
    edition:  versionName.value,
    module:   activeTab.value !== '同步备课' ? activeTab.value : undefined,
    type:     selectedType.value || undefined,
    // 如果册次选择了具体的，尝试用 gradeName 全称（如 "一年级上册"）
    gradeName: gradeFull || undefined,
    keyword:  keyword.value || undefined,
    current:  currentPage.value,
    size:     pageSize.value,
    sortField: currentSortKey.value as 'createTime' | 'downloadCount' | 'viewCount' | 'rating' | 'collectCount',
    sortOrder: 'desc' as const,
  }
}

async function fetchResources() {
  loading.value = true
  try {
    const params = buildParams()
    const { page } = await resourceGateway.listPrimaryChineseResources(params)
    apiResources.value = page?.records || []
    apiTotal.value = page?.total || 0
  } catch (e) {
    console.error('加载资源失败', e)
    apiResources.value = []
    apiTotal.value = 0
  } finally {
    loading.value = false
  }
}

/** 加载各栏目资源数量（用于 Tab 上显示数字角标） */
async function fetchModuleStats() {
  try {
    const params = {
      stage:   '小学',
      subject: subjectName.value,
      edition: versionName.value,
      gradeName: gradeName.value,
    }
    const { modules } = await resourceGateway.getPrimaryChineseModuleStats(params)
    const map: Record<string, number> = {}
    for (const item of modules) {
      const m = item.module
      if (m) map[m] = item.count || 0
    }
    moduleStatMap.value = map
  } catch (e) {
    // 静默失败
  }
}

// ===== 事件处理 =====

function switchTab(tabKey: string) {
  activeTab.value = tabKey
  currentPage.value = 1
  fetchResources()
}

function selectVolume(key: string) {
  selectedVolume.value = key
  currentPage.value = 1
  fetchResources()
}

function selectType(key: string) {
  selectedType.value = key
  currentPage.value = 1
  fetchResources()
}

function doSearch() {
  currentPage.value = 1
  fetchResources()
}

function handleSort(sortKey: string) {
  currentSortKey.value = sortKey
  currentPage.value = 1
  fetchResources()
}

function handlePageChange(page: number) {
  currentPage.value = page
  fetchResources()
}

function handlePreview(resource: any) {
  currentResource.value = resource
  previewVisible.value = true
  // 记录浏览次数
  if (resource.id) {
    resourceGateway.recordViewBySource('primary_chinese', resource.id).catch(() => {})
  }
}

function handleDownload(resource: any) {
  if (!resource) return
  if (resource.ossUrl) {
    window.open(resource.ossUrl, '_blank')
    ElMessage.success(`开始下载: ${resource.title}`)
  } else {
    ElMessage.warning('暂无下载链接，请联系管理员')
  }
}

function handleCollect(resource: any) {
  ElMessage.success(`已收藏: ${resource.title}`)
}

// ===== 工具函数 =====

function formatSize(kb?: number): string {
  if (!kb) return '-'
  if (kb < 1024) return `${kb}KB`
  return `${(kb / 1024).toFixed(1)}MB`
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '-'
  return dateStr.substring(0, 10)
}

function getFileIconClass(ext?: string): string {
  if (!ext) return 'icon-word'
  const lower = ext.toLowerCase()
  if (['ppt', 'pptx'].includes(lower)) return 'icon-ppt'
  if (['pdf'].includes(lower)) return 'icon-pdf'
  if (['zip', 'rar'].includes(lower)) return 'icon-zip'
  if (['mp4', 'avi', 'mkv'].includes(lower)) return 'icon-video'
  return 'icon-word'
}

function getFileIconLetter(ext?: string): string {
  if (!ext) return 'W'
  const lower = ext.toLowerCase()
  if (['ppt', 'pptx'].includes(lower)) return 'P'
  if (['pdf'].includes(lower)) return 'PDF'
  if (['zip', 'rar'].includes(lower)) return 'ZIP'
  if (['mp4', 'avi'].includes(lower)) return '▶'
  return 'W'
}

// ===== 导航 =====
const gradeColor = computed(() => {
  const colors: Record<string, string> = {
    grade1: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    grade2: 'linear-gradient(135deg, #43E97B 0%, #38F9D7 100%)',
    grade3: 'linear-gradient(135deg, #FA709A 0%, #FEE140 100%)',
    grade4: 'linear-gradient(135deg, #30CFD0 0%, #330867 100%)',
    grade5: 'linear-gradient(135deg, #F093FB 0%, #F5576C 100%)',
    grade6: 'linear-gradient(135deg, #4776E6 0%, #8E54E9 100%)',
    grade7: 'linear-gradient(135deg, #00B4DB 0%, #0083B0 100%)',
    grade8: 'linear-gradient(135deg, #56CCF2 0%, #2F80ED 100%)',
    grade9: 'linear-gradient(135deg, #A18CD1 0%, #FBC2EB 100%)',
    grade10: 'linear-gradient(135deg, #5EE7DF 0%, #B490CA 100%)',
    grade11: 'linear-gradient(135deg, #D4FC79 0%, #96E6A1 100%)',
    grade12: 'linear-gradient(135deg, #FDCBF1 0%, #E6DEE9 100%)',
  }
  return colors[grade.value] || '#435EEA'
})

function goHome() { router.push('/') }
function goGrade() { router.push(`/grade/${grade.value}`) }
function goSubject() { router.push(`/grade/${grade.value}`) }

// ===== 生命周期 =====
onMounted(() => {
  fetchResources()
  fetchModuleStats()
})
</script>

<style scoped>
.version-resource-page {
  min-height: 100vh;
  background: #f5f7fa;
}

/* ===================== 顶部标签栏 ===================== */
.top-tab-bar {
  background: #fff;
  border-bottom: 1px solid #E4E7ED;
  padding: 12px 0;
  position: sticky;
  top: 64px;
  z-index: 100;
}
.tab-items {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.tab-item {
  position: relative;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: all 0.2s;
}
.tab-item:hover {
  color: var(--color-primary, #409eff);
  background: #ecf5ff;
}
.tab-item.active {
  color: #fff;
  background: var(--color-primary, #409eff);
}
.tab-count {
  position: absolute;
  top: 2px;
  right: 2px;
  background: #f56c6c;
  color: #fff;
  font-size: 10px;
  padding: 0 4px;
  border-radius: 8px;
  line-height: 1.4;
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
  margin-bottom: 12px;
  font-size: 14px;
}
.header-breadcrumb-item {
  opacity: 0.9;
  cursor: pointer;
  transition: opacity 0.2s;
}
.header-breadcrumb-item:hover {
  opacity: 1;
  text-decoration: underline;
}
.header-breadcrumb-item.current { font-weight: 600; }
.header-breadcrumb-sep { opacity: 0.6; }
.page-header-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  line-height: 1.3;
}

/* ===================== 主体区域 ===================== */
.page-body { padding: 20px 0; }

/* 横向筛选栏 */
.filter-bar {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 16px;
}
.filter-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}
.filter-row:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}
.filter-label {
  width: 56px;
  font-size: 14px;
  color: #909399;
  flex-shrink: 0;
}
.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.filter-tag {
  padding: 4px 12px;
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}
.filter-tag:hover {
  color: var(--color-primary, #409eff);
  background: #ecf5ff;
}
.filter-tag.active {
  color: #fff;
  background: var(--color-primary, #409eff);
}
.search-input-wrap {
  display: flex;
  gap: 8px;
  flex: 1;
}
.search-input {
  flex: 1;
  max-width: 400px;
  height: 32px;
  padding: 0 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}
.search-input:focus { border-color: var(--color-primary, #409eff); }
.search-btn {
  padding: 0 16px;
  height: 32px;
  background: var(--color-primary, #409eff);
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: opacity 0.2s;
}
.search-btn:hover { opacity: 0.9; }

/* 资源列表 */
.resource-list {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  min-height: 500px;
}
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #EBEEF5;
}
.result-count {
  font-size: 14px;
  color: #909399;
}
.sort-trigger {
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}
.sort-trigger:hover { color: var(--color-primary, #409eff); }

/* 加载状态 */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 60px 20px;
  color: #909399;
  font-size: 14px;
}
.loading-icon {
  animation: spin 1s linear infinite;
  font-size: 20px;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 资源卡片 */
.resource-cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.resource-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #EBEEF5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.resource-card:hover {
  border-color: var(--color-primary, #409eff);
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
}
.resource-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
  color: #fff;
}
.resource-icon.icon-ppt  { background: #D35400; }
.resource-icon.icon-pdf  { background: #C0392B; }
.resource-icon.icon-zip  { background: #8E44AD; }
.resource-icon.icon-video { background: #16A085; }
.resource-icon.icon-word { background: #2B5797; }

.resource-info { flex: 1; min-width: 0; }
.resource-title {
  font-size: 15px;
  color: #303133;
  margin-bottom: 6px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.resource-meta {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #909399;
  flex-wrap: wrap;
}
.resource-format {
  background: #f0f9eb;
  color: #67c23a;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.resource-downloads { color: #409eff; }
.resource-unit {
  background: #f4f4f5;
  color: #606266;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.resource-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}
.empty-icon { font-size: 48px; margin-bottom: 16px; }
.empty-text { font-size: 16px; color: #303133; margin-bottom: 8px; }
.empty-hint { font-size: 14px; color: #909399; }

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 详情弹窗 */
.preview-content { padding: 8px 0; }
.preview-meta-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 24px;
}
.preview-meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}
.meta-label { color: #909399; min-width: 40px; }
.meta-value { color: #303133; font-weight: 500; }
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* 响应式 */
@media (max-width: 768px) {
  .preview-meta-grid { grid-template-columns: 1fr; }
  .resource-meta { gap: 8px; }
}
</style>

