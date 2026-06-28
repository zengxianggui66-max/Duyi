<template>
  <div class="platform-page">
    <!-- ===== 主体内容区 ===== -->
    <div class="page-body">
      <!-- 学科筛选栏（和截图完全一致） -->
      <div class="filter-row">
        <span class="filter-label">学科：</span>
        <div class="subject-tabs">
          <span
              v-for="subject in currentSubjects"
              :key="subject.key"
              :class="['subject-tab', { active: currentSubject?.key === subject.key }]"
              @click="selectSubject(subject)"
          >
            {{ subject.name }}
          </span>
        </div>
      </div>

      <!-- 栏目筛选栏（和截图完全一致，含"更多"下拉） -->
      <div class="filter-row column-row" v-if="currentColumns.length">
        <span class="filter-label">栏目：</span>
        <div class="column-tabs">
          <span
              v-for="col in visibleColumns"
              :key="col"
              :class="['column-tab', { active: activeColumn === col }]"
              @click="activeColumn = col"
          >{{ col }}</span>
          <span class="column-tab more-tab" @click="toggleMoreColumns">更多 ∨</span>
        </div>
      </div>

      <!-- 资源类型筛选栏（单行按钮，统一逻辑） -->
      <div class="resource-type-bar">
        <div class="type-group">
          <div class="type-buttons">
            <!-- 第一组：基础资源类型按钮 -->
            <button
                v-for="type in resourceTypes"
                :key="`resource-${type}`"
                :class="['type-btn', { active: activeResourceType === type }]"
                @click="selectResourceType(type)"
            >{{ type }}</button>

            <!-- 第二组：教师用书相关按钮（合并到同一行，统一交互逻辑） -->
            <button
                v-for="item in teacherBookTypes"
                :key="`teacher-${item}`"
                :class="['type-btn', { active: activeResourceType === item }]"
                @click="selectResourceType(item)"
            >{{ item }}</button>
          </div>
        </div>
      </div>
      <!-- 高级筛选栏（和截图完全一致：找单份/找成套、排序、精品复选框、结果数） -->
      <div class="advanced-filter-bar">
        <div class="filter-left">
          <button :class="['filter-mode-btn', { active: resourceMode === 'single' }]" @click="resourceMode = 'single'">找单份</button>
          <button :class="['filter-mode-btn', { active: resourceMode === 'suite' }]" @click="resourceMode = 'suite'">找成套</button>
        </div>
        <div class="filter-middle">
          <div class="sort-buttons">
            <span :class="['sort-btn', { active: sortType === 'comprehensive' }]" @click="sortType = 'comprehensive'">综合</span>
            <span :class="['sort-btn', { active: sortType === 'latest' }]" @click="sortType = 'latest'">最新</span>
            <span :class="['sort-btn', { active: sortType === 'downloads' }]" @click="sortType = 'downloads'">下载量</span>
          </div>
          <div class="checkbox-group">
            <label class="checkbox-label"><input type="checkbox" v-model="filters.premium"> 精品</label>
          </div>
        </div>
        <div class="filter-right">
          <span class="result-count">共 {{ filteredTotalCount }} 条结果</span>
        </div>
      </div>

      <!-- 两列布局：左侧课程目录 + 右侧资源列表（和截图完全一致） -->
      <div class="content-two-columns">
        <!-- 左侧课程目录区（和截图一致：课程信息、版本切换、教材切换、单元目录） -->
        <aside class="course-catalog">
          <!-- 课程信息卡片 -->
          <div class="course-info-card">
            <div class="cover-area">
              <div class="book-cover">
                <img src="https://img0.baidu.com/it/u=3984123456,1234567890&fm=253&fmt=auto&app=138&f=JPEG?w=200&h=260" alt="教材封面" class="cover-img">
                <span class="new-badge" v-if="isNewTextbook">新教材</span>
              </div>
            </div>
            <div class="course-meta">
              <h3 class="course-title">语文·一年级下册（…）</h3>
              <div class="version-publisher">版本：统编版（2024） | 出版社：人民教育出版社</div>
              <button class="switch-version-btn" @click="toggleVersionModal">⥂ 切换版本册别</button>
            </div>
          </div>

          <!-- 新/旧教材快捷切换 -->
          <div class="textbook-switch">
            <button :class="['textbook-btn', { active: isNewTextbook }]" @click="setTextbookVersion(true)">新教材</button>
            <button :class="['textbook-btn', { active: !isNewTextbook }]" @click="setTextbookVersion(false)">旧教材</button>
          </div>

          <!-- 单元目录（和截图完全一致的8个单元+本册综合） -->
          <div class="unit-directory">
            <div class="unit-list">
              <div
                  v-for="(unit, idx) in currentUnitList"
                  :key="idx"
                  class="unit-item-wrapper"
              >
                <div class="unit-item" @click="activeUnit = unit.name">
                  <span class="expand-icon">⊕</span>
                  <span class="unit-name" :class="{ active: activeUnit === unit.name }">{{ unit.name }}</span>
                </div>
              </div>
            </div>
          </div>
        </aside>

        <!-- 右侧资源列表区（和截图完全一致的卡片样式） -->
        <main class="resource-list-area">
          <div class="resource-card-grid">
            <div
                v-for="resource in paginatedResources"
                :key="resource.id"
                class="resource-card"
            >
              <div class="card-left">
                <div class="preview-thumb">
                  <span class="thumb-icon">📄</span>
                  <span class="thumb-tags">{{ resource.tags.join(' ') }}</span>
                </div>
              </div>
              <div class="card-right">
                <div class="card-header">
                  <span class="premium-tag" v-if="resource.isPremium">精</span>
                  <span class="card-title">{{ resource.title }}</span>
                </div>
                <div class="card-meta">
                  <span>数量：{{ resource.count }}份</span>
                  <span>状态：{{ resource.status }}</span>
                </div>
                <div class="card-stats">
                  <span>浏览量：{{ resource.views }}</span>
                  <span>下载量：{{ resource.downloads }}</span>
                </div>
                <div class="card-footer">
                  <span class="update-time">更新：{{ resource.updateTime }}</span>
                  <span class="author">作者：{{ resource.author }}</span>
                </div>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>

    <!-- ===== 版本/册别切换弹窗（和截图完全一致） ===== -->
    <div class="version-modal-overlay" v-if="showVersionModal" @click.self="toggleVersionModal">
      <div class="version-modal">
        <!-- 版本选择 -->
        <div class="modal-section">
          <span class="section-label">版本：</span>
          <div class="version-buttons">
            <button
                v-for="ver in subjectVersions"
                :key="ver.key"
                :class="['version-btn', { active: selectedVersion === ver.key }]"
                @click="selectedVersion = ver.key"
            >{{ ver.name }}</button>
          </div>
        </div>

        <!-- 册别选择 -->
        <div class="modal-section">
          <span class="section-label">册别：</span>
          <div class="volume-buttons">
            <button
                v-for="vol in volumeOptions"
                :key="vol.id"
                :class="['volume-btn', { active: selectedVolume === vol.id, new: vol.isNew }]"
                @click="selectedVolume = vol.id"
            >
              {{ vol.name }}
              <span v-if="vol.isNew" class="new-tag">新教材</span>
            </button>
          </div>
        </div>

        <!-- 弹窗按钮 -->
        <div class="modal-footer">
          <button class="modal-btn confirm-btn" @click="confirmVersionChange">确定</button>
          <button class="modal-btn cancel-btn" @click="toggleVersionModal">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

// ========== 学科数据（和截图一致） ==========
const currentSubjects = ref([
  { key: 'chinese', name: '语文' },
  { key: 'math', name: '数学' },
  { key: 'english', name: '英语' },
  { key: 'politics', name: '政治 (道德与法治)' },
  { key: 'science', name: '科学' },
  { key: 'music', name: '音乐' },
  { key: 'art', name: '美术' },
  { key: 'it', name: '信息技术' },
  { key: 'psychology', name: '心理健康' },
  { key: 'labor', name: '劳技' },
  { key: 'calligraphy', name: '书法练习指导' },
  { key: 'pe', name: '体育' },
  { key: 'comprehensive', name: '综合实践活动' },
  { key: 'local', name: '地方、校本课程' },
])
const currentSubject = ref<any>(currentSubjects.value[0])
const currentColumns = ref(['同步备课', '开学专区', '月考', '期中', '期末', '小升初真题', '小升初模拟', '专题复习', '真题汇编', '暑假', '寒假', '作文', '阅读', '竞赛'])
const visibleColumns = computed(() => currentColumns.value.slice(0, 13))
const showAllColumns = ref(false)
function toggleMoreColumns() {
  showAllColumns.value = !showAllColumns.value
}
const activeColumn = ref('同步备课')
function selectSubject(subject: any) {
  currentSubject.value = subject
}

// ========== 资源类型筛选栏（和截图完全一致） ==========
const resourceTypes = ref(['全部', '课件', '教案', '练习', '试卷', '学案', '电子课本', '教学反思', '音频/朗读', '视频', '知识点'])
const activeResourceType = ref('全部')

const teacherBookTypes = ref(['教师用书', '课堂实录', '讲义', '说课稿', '教学计划', '预习', '教学总结', '逐字稿', '公开课'])
const activeTeacherBook = ref('教师用书')

// 选择资源类型
function selectResourceType(type: string) {
  activeResourceType.value = type
}

// ========== 高级筛选栏（和截图完全一致） ==========
const resourceMode = ref<'single' | 'suite'>('suite')
const sortType = ref<'comprehensive' | 'latest' | 'downloads'>('comprehensive')
const filters = ref({
  premium: false
})

// ========== 左侧课程目录数据（和截图一致） ==========
const isNewTextbook = ref(true)
const currentPublisher = ref('人民教育出版社')

function setTextbookVersion(isNew: boolean) {
  isNewTextbook.value = isNew
}

// 单元列表（和截图完全一致）
const currentUnitList = ref([
  { name: '第一单元·识字' },
  { name: '第二单元·阅读' },
  { name: '第三单元·阅读' },
  { name: '第四单元·阅读' },
  { name: '第五单元·识字' },
  { name: '第六单元·阅读' },
  { name: '第七单元·阅读' },
  { name: '第八单元·阅读' },
  { name: '本册综合' }
])
const activeUnit = ref('第一单元·识字')

// ========== 版本/册别切换弹窗数据 ==========
const showVersionModal = ref(false)
const subjectVersions = ref([
  { key: 'tongbian2024', name: '统编版（2024）' },
  { key: 'tongbian54', name: '统编版（五四制）（2024）' },
  { key: 'renjiao', name: '人教部编版（五四制）' },
  { key: 'teacheredu', name: '特教-人教版' }
])
const selectedVersion = ref('tongbian2024')

const volumeOptions = ref([
  { id: 'y1s1-new', name: '一年级上册（2024）', isNew: true },
  { id: 'y1s2-new', name: '一年级下册（2024）', isNew: true },
  { id: 'y2s1-new', name: '二年级上册（2024）', isNew: true },
  { id: 'y2s2-new', name: '二年级下册（2024）', isNew: true },
  { id: 'y3s1-new', name: '三年级上册（2024）', isNew: true },
  { id: 'y3s2-new', name: '三年级下册（2024）', isNew: true },
  { id: 'y1s1-old', name: '一年级上册', isNew: false },
  { id: 'y1s2-old', name: '一年级下册', isNew: false },
  { id: 'y2s1-old', name: '二年级上册', isNew: false },
  { id: 'y2s2-old', name: '二年级下册', isNew: false },
  { id: 'y3s1-old', name: '三年级上册', isNew: false },
  { id: 'y3s2-old', name: '三年级下册', isNew: false },
  { id: 'y4s1-old', name: '四年级上册', isNew: false },
  { id: 'y4s2-old', name: '四年级下册', isNew: false },
  { id: 'y5s1-old', name: '五年级上册', isNew: false },
  { id: 'y5s2-old', name: '五年级下册', isNew: false },
  { id: 'y6s1-old', name: '六年级上册', isNew: false },
  { id: 'y6s2-old', name: '六年级下册', isNew: false },
])
const selectedVolume = ref('y1s2-new')

function toggleVersionModal() {
  showVersionModal.value = !showVersionModal.value
}
function confirmVersionChange() {
  toggleVersionModal()
}

// ========== 右侧资源数据（和截图完全一致的卡片数据） ==========
const allResources = ref([
  {
    id: 1,
    title: '【开学摸底考】（统编版·2024）2024-2025学年一年级语文下册开学摸底考试卷',
    tags: ['语文', '试卷'],
    isPremium: true,
    count: 3,
    status: '已完结',
    views: 930,
    downloads: 14,
    updateTime: '2026-01-24',
    author: '备课教研—乐老师'
  },
  {
    id: 2,
    title: '【核心素养-任务型】2025年春季统编版语文一年级下册课件+教案+音视频素材',
    tags: ['语文', '课件', '教案', '视频'],
    isPremium: true,
    count: 53,
    status: '已完结',
    views: 10836,
    downloads: 192,
    updateTime: '2026-03-01',
    author: '教研工坊'
  },
  {
    id: 3,
    title: '【新课标-核心素养】2025年春季统编版语文一年级下册课件+教案+音视频素材',
    tags: ['语文', '课件', '教案', '视频'],
    isPremium: true,
    count: 52,
    status: '已完结',
    views: 23473,
    downloads: 1066,
    updateTime: '2026-02-25',
    author: '教研工坊'
  },
  {
    id: 4,
    title: '【新课改-任务型】人教部编版语文一年级下册 全册课件+教案+音视频素材',
    tags: ['语文', '课件', '教案', '视频'],
    isPremium: true,
    count: 45,
    status: '已完结',
    views: 6472,
    downloads: 116,
    updateTime: '2026-02-25',
    author: '教研工坊'
  }
])

// 筛选和分页逻辑
const filteredResources = computed(() => {
  let res = [...allResources.value]
  if (filters.value.premium) res = res.filter(r => r.isPremium)
  if (sortType.value === 'latest') res.sort((a, b) => new Date(b.updateTime).getTime() - new Date(a.updateTime).getTime())
  if (sortType.value === 'downloads') res.sort((a, b) => b.downloads - a.downloads)
  return res
})

const filteredTotalCount = computed(() => filteredResources.value.length)
const paginatedResources = computed(() => filteredResources.value)

// 监听筛选变化重置页码（如果有分页的话）
watch([filteredResources], () => {
  // 这里可以重置页码
})
</script>

<style scoped>
/* 基础样式 */
.platform-page {
  min-height: 100vh;
  background: #F5F7FA;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* ===== 主体内容区 ===== */
.page-body {
  max-width: 1440px;
  margin: 20px auto;
  padding: 0 24px;
}

/* 统一筛选行样式 */
.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  padding: 10px 20px;
  border-radius: 20px;
  margin-bottom: 12px;
  border: 1px solid #EEF2F6;
}
.filter-label {
  font-weight: 600;
  color: #2C3E50;
  font-size: 14px;
  flex-shrink: 0;
  min-width: 40px;
}
.subject-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.subject-tab {
  cursor: pointer;
  font-size: 14px;
  padding: 6px 16px;
  border-radius: 30px;
  color: #333;
  background: #F5F7FA;
  transition: 0.2s;
}
.subject-tab:hover {
  background: #E8F4FF;
}
.subject-tab.active {
  background: #409EFF;
  color: #fff;
  font-weight: 500;
}

/* 栏目筛选栏 */
.column-row {
  margin-bottom: 12px;
}
.column-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.column-tab {
  padding: 6px 16px;
  border-radius: 30px;
  cursor: pointer;
  background: #fff;
  border: 1px solid #DCDFE6;
  font-size: 13px;
  color: #333;
  transition: 0.2s;
}
.column-tab:hover {
  border-color: #409EFF;
  color: #409EFF;
}
.column-tab.active {
  background: #409EFF;
  color: #fff;
  border-color: #409EFF;
}
.more-tab {
  color: #909399;
}

/* 资源类型筛选栏 */
.resource-type-bar {
  background: #fff;
  border-radius: 20px;
  padding: 10px 20px;
  margin-bottom: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  border: 1px solid #EEF2F6;
}
.type-group {
  display: flex;
  align-items: center;
  gap: 8px;
}
.type-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.type-btn {
  background: #F5F7FA;
  border: none;
  padding: 4px 12px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 12px;
  color: #606266;
  transition: 0.15s;
}
.type-btn:hover {
  background: #E8F4FF;
  color: #409EFF;
}
.type-btn.active {
  background: #409EFF;
  color: white;
}

/* 高级筛选栏 */
.advanced-filter-bar {
  background: #fff;
  border-radius: 20px;
  padding: 10px 20px;
  margin-bottom: 24px;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #EEF2F6;
}
.filter-left {
  display: flex;
  gap: 12px;
}
.filter-mode-btn {
  padding: 6px 18px;
  border-radius: 30px;
  border: 1px solid #DCDFE6;
  background: white;
  cursor: pointer;
  transition: 0.15s;
}
.filter-mode-btn.active {
  background: #E67E22;
  color: white;
  border-color: #E67E22;
}
.filter-middle {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}
.sort-buttons {
  display: flex;
  gap: 12px;
}
.sort-btn {
  cursor: pointer;
  padding: 4px 8px;
  font-size: 13px;
  color: #606266;
  transition: 0.15s;
}
.sort-btn.active {
  color: #409EFF;
  font-weight: 600;
}
.checkbox-group {
  display: flex;
  gap: 16px;
}
.checkbox-label {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  font-size: 13px;
  color: #606266;
}
.filter-right .result-count {
  color: #606266;
  font-size: 13px;
}

/* 两列布局 */
.content-two-columns {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 32px;
}

/* 左侧课程目录 */
.course-catalog {
  background: #fff;
  border-radius: 20px;
  padding: 16px;
  border: 1px solid #EEF2F6;
  align-self: start;
}
.course-info-card {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}
.book-cover {
  width: 70px;
  height: 90px;
  background: #F5E6D3;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  flex-shrink: 0;
  overflow: hidden;
}
.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.new-badge {
  position: absolute;
  top: -6px;
  left: -10px;
  background: #F56C6C;
  color: #fff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 20px;
}
.course-meta {
  flex: 1;
}
.course-title {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 6px;
  line-height: 1.4;
}
.version-publisher {
  font-size: 12px;
  color: #8C9AA8;
  margin-bottom: 6px;
}
.switch-version-btn {
  font-size: 12px;
  background: #F5F7FA;
  border: 1px solid #E4E9F0;
  border-radius: 20px;
  padding: 4px 12px;
  cursor: pointer;
  transition: 0.15s;
  color: #409EFF;
}
.textbook-switch {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.textbook-btn {
  flex: 1;
  padding: 6px;
  border-radius: 30px;
  border: 1px solid #DCDFE6;
  background: white;
  cursor: pointer;
  font-size: 13px;
  transition: 0.15s;
}
.textbook-btn.active {
  background: #409EFF;
  color: white;
}
.unit-directory {
  margin-top: 8px;
}
.unit-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.unit-item {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 6px 8px;
  border-radius: 12px;
  transition: 0.15s;
}
.unit-item:hover {
  background: #F5F7FA;
}
.expand-icon {
  font-size: 14px;
  width: 20px;
  color: #909399;
}
.unit-name {
  font-size: 13px;
  color: #2C3E50;
}
.unit-name.active {
  color: #409EFF;
  font-weight: 600;
}

/* 右侧资源列表 */
.resource-list-area {
  min-width: 0;
}
.resource-card-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}
.resource-card {
  padding: 16px;
  border: 1px solid #EEF2F6;
  border-radius: 20px;
  transition: 0.2s;
  display: flex;
  gap: 16px;
}
.resource-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 14px rgba(0,0,0,0.05);
}
.preview-thumb {
  width: 120px;
  height: 120px;
  background: #F5F7FA;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.thumb-icon {
  font-size: 24px;
  margin-bottom: 4px;
}
.thumb-tags {
  font-size: 12px;
  color: #606266;
  text-align: center;
}
.card-right {
  flex: 1;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.premium-tag {
  background: #E67E22;
  color: #fff;
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 12px;
  flex-shrink: 0;
}
.card-title {
  font-weight: 600;
  font-size: 15px;
  line-height: 1.4;
}
.card-meta, .card-stats, .card-footer {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #8C9AA8;
  margin-bottom: 6px;
}
.card-footer {
  margin-top: 8px;
}

/* 版本切换弹窗 */
.version-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.version-modal {
  background: white;
  border-radius: 12px;
  padding: 24px;
  width: 600px;
  max-width: 90%;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}
.modal-section {
  margin-bottom: 24px;
}
.section-label {
  font-weight: 600;
  color: #2C3E50;
  font-size: 14px;
  margin-bottom: 12px;
  display: block;
}
.version-buttons, .volume-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.version-btn, .volume-btn {
  padding: 8px 16px;
  border: 1px solid #DCDFE6;
  background: white;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  transition: 0.15s;
}
.version-btn.active, .volume-btn.active {
  background: #409EFF;
  color: white;
  border-color: #409EFF;
}
.new-tag {
  background: #F56C6C;
  color: white;
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 10px;
  margin-left: 4px;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}
.modal-btn {
  padding: 8px 24px;
  border-radius: 20px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  transition: 0.15s;
}
.confirm-btn {
  background: #409EFF;
  color: white;
}
.cancel-btn {
  background: white;
  border: 1px solid #DCDFE6;
  color: #606266;
}

/* 响应式 */
@media (max-width: 860px) {
  .content-two-columns {
    grid-template-columns: 1fr;
  }
  .resource-card-grid {
    grid-template-columns: 1fr;
  }
  .advanced-filter-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
}
</style>
