<template>
  <div class="platform-page">
    <!-- ===== 顶部导航栏 ===== -->
    <div class="top-nav">
      <div class="top-nav-inner">
        <div class="nav-left-group">
          <div class="site-name">📚 新课堂教育</div>
          <div class="stage-bar">
            <button
                v-for="stage in stages"
                :key="stage.key"
                :class="['stage-btn', { active: currentStage === stage.key }]"
                @click="switchStage(stage.key)"
            >
              {{ stage.name }}
            </button>
          </div>
        </div>
        <div class="nav-right-actions">
          <div class="search-wrapper">
            <input
                type="text"
                v-model="searchKeyword"
                placeholder="输入关键词搜索"
                class="search-input"
                @keyup.enter="handleSearch"
            />
            <button class="search-btn" @click="handleSearch">搜索</button>
          </div>
          <button class="cart-btn" @click="showCart">
            <span class="cart-icon">📦</span> 资料篮
            <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
          </button>
          <button class="upload-btn" @click="goToUpload">
            <span class="upload-icon">⬆️</span> 上传资源
          </button>
        </div>
      </div>
    </div>

    <!-- ===== 主体内容区 ===== -->
    <div class="page-body">
      <!-- 学科筛选栏（横向标签） -->
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
            <span v-if="subject.isNew" class="new-dot"></span>
          </span>
        </div>
      </div>

      <!-- 栏目筛选栏（根据学段动态显示全部栏目，自然换行） -->
      <div class="filter-row column-row" v-if="currentColumns.length">
        <span class="filter-label">栏目：</span>
        <div class="column-tabs">
          <span
              v-for="col in currentColumns"
              :key="col"
              :class="['column-tab', { active: activeColumn === col }]"
              @click="activeColumn = col"
          >{{ col }}</span>
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

      <!-- 高级筛选栏 -->
      <div class="advanced-filter-bar">
        <div class="filter-left">
          <button :class="['filter-mode-btn', { active: resourceMode === 'single' }]" @click="resourceMode = 'single'">找单份</button>
          <button :class="['filter-mode-btn', { active: resourceMode === 'suite' }]" @click="resourceMode = 'suite'">找成套</button>
        </div>
        <div v-if="resourceMode === 'single'" class="filter-middle">
          <div class="sort-buttons">
            <span :class="['sort-btn', { active: sortType === 'comprehensive' }]" @click="sortType = 'comprehensive'">综合</span>
            <span :class="['sort-btn', { active: sortType === 'latest' }]" @click="sortType = 'latest'">最新</span>
            <span :class="['sort-btn', { active: sortType === 'downloads' }]" @click="sortType = 'downloads'">下载量</span>
          </div>
          <div class="checkbox-group">
            <label class="checkbox-label"><input type="checkbox" v-model="filters.premium"> 精品</label>
            <label class="checkbox-label"><input type="checkbox" v-model="filters.free"> 免费</label>
            <label class="checkbox-label"><input type="checkbox" v-model="filters.openClass"> 公开课</label>
            <label class="checkbox-label"><input type="checkbox" v-model="filters.lessonSupport"> 课时配套</label>
          </div>
        </div>
        <div class="filter-right">
          <span class="result-count">共 {{ filteredTotalCount }} 条结果</span>
        </div>
      </div>

      <!-- 两列布局：左侧课程目录 + 右侧资源列表 -->
      <div class="content-two-columns">
        <!-- 左侧课程目录区 -->
        <aside class="course-catalog">
          <!-- 课程信息卡片 -->
          <div class="course-info-card">
            <div class="cover-area">
              <div class="book-cover">
                <span class="cover-icon">📘</span>
                <span v-if="isNewTextbook" class="new-badge">新教材</span>
              </div>
            </div>
            <div class="course-meta">
              <h3 class="course-title">{{ currentStageName }}·{{ currentSubject?.name }}·{{ currentGradeLevelName }}（{{ currentVersionName }}）</h3>
              <div class="version-publisher">版本：{{ currentVersionName }} | 出版社：{{ currentPublisher }}</div>
              <button class="switch-version-btn" @click="toggleVersionModal">切换版本/册别</button>
            </div>
          </div>

          <!-- 新/旧教材快捷切换 -->
          <div class="textbook-switch">
            <button :class="['textbook-btn', { active: isNewTextbook }]" @click="setTextbookVersion(true)">新教材</button>
            <button :class="['textbook-btn', { active: !isNewTextbook }]" @click="setTextbookVersion(false)">旧教材</button>
          </div>

          <!-- 册别快速选择（简化版） -->
          <div class="volume-quick" v-if="volumeList.length > 0">
            <span class="volume-label">册别：</span>
            <select v-model="selectedVolumeId" @change="onVolumeChange" class="volume-select">
              <option v-for="vol in volumeList" :key="vol.id" :value="vol.id">{{ vol.name }} {{ vol.isNew ? '(新)' : '' }}</option>
            </select>
          </div>

          <!-- 单元目录（带折叠展开） -->
          <div class="unit-directory">
            <div class="unit-header">
              <span>目录</span>
              <button class="collapse-all-btn" @click="toggleAllUnits">{{ allUnitsExpanded ? '折叠全部' : '展开全部' }}</button>
            </div>
            <div class="unit-list">
              <div
                  v-for="(unit, idx) in currentUnitList"
                  :key="idx"
                  class="unit-item-wrapper"
              >
                <div class="unit-item" @click="toggleUnitExpand(unit)">
                  <span class="expand-icon">{{ unit.expanded ? '∨' : '⊕' }}</span>
                  <span :class="['unit-name', { active: activeUnit === unit.name }]" @click.stop="activeUnit = unit.name">{{ unit.name }}</span>
                </div>
                <div v-if="unit.expanded && unit.subUnits" class="sub-unit-list">
                  <div v-for="sub in unit.subUnits" :key="sub" class="sub-unit" @click="activeUnit = sub">{{ sub }}</div>
                </div>
              </div>
            </div>
          </div>
        </aside>

        <!-- 右侧资源列表区 -->
        <main class="resource-list-area">
          <!-- 精品成套资源区（仅成套模式显示） -->
          <div v-if="resourceMode === 'suite'" class="suite-section">
            <div class="section-header">
              <h3><span class="star-icon">★</span> 精品成套</h3>
              <span class="section-desc">成套系列资料，整套一键下载</span>
              <span class="more-link" @click="viewMoreSuites">更多 &gt;</span>
            </div>
            <div class="suite-card-grid">
              <div
                  v-for="suite in suitePaginatedSuites"
                  :key="suite.key"
                  class="suite-card"
              >
                <div class="card-left">
                  <div class="card-icon">{{ suite.icon }}</div>
                  <div class="card-info">
                    <div class="card-title">{{ suite.title }}</div>
                    <div class="card-sub">{{ suite.sub }}</div>
                    <div class="card-meta">{{ suite.fileCount }}个文件 | {{ suite.updateTime }}</div>
                  </div>
                </div>
                <div class="card-right">
                  <button class="download-btn">一键下载</button>
                </div>
                <div class="card-tag" v-if="suite.tag">{{ suite.tag }}</div>
              </div>
            </div>

            <!-- 成套资源分页控件 -->
            <div class="pagination" v-if="resourceMode === 'suite' && suiteTotalPages > 1">
              <button class="page-btn" :disabled="suiteCurrentPage === 1" @click="suiteCurrentPage--">上一页</button>
              <button
                  v-for="page in suiteVisiblePages"
                  :key="page"
                  :class="['page-btn', { active: suiteCurrentPage === page }]"
                  @click="suiteCurrentPage = page"
              >{{ page }}</button>
              <button class="page-btn" :disabled="suiteCurrentPage === suiteTotalPages" @click="suiteCurrentPage++">下一页</button>
            </div>
          </div>

          <!-- 单份资源列表区（仅单份模式显示） -->
          <div v-if="resourceMode === 'single'" class="single-resource-section">
            <div class="section-header">
              <h3>单份资源</h3>
              <span class="more-link" @click="viewMoreResources">更多 &gt;</span>
            </div>
            <div class="resource-cards">
              <div
                  v-for="resource in paginatedResources"
                  :key="resource.id"
                  class="resource-card"
              >
                <div class="resource-header">
                  <span class="premium-tag" v-if="resource.isPremium">精</span>
                  <span class="resource-title">{{ resource.title }}</span>
                </div>
                <div class="resource-previews">
                  <div v-for="preview in resource.previews" :key="preview" class="preview-thumb">
                    <span class="thumb-icon">📄</span>
                    <span class="thumb-name">{{ preview }}</span>
                  </div>
                </div>
                <div class="resource-footer">
                  <span class="publish-date">{{ resource.publishDate }}</span>
                  <span class="download-count">下载 {{ resource.downloads }}次</span>
                  <span class="author">{{ resource.author }}</span>
                </div>
              </div>
            </div>

            <!-- 分页控件 -->
            <div class="pagination" v-if="totalPages > 1">
              <button class="page-btn" :disabled="currentPage === 1" @click="currentPage--">上一页</button>
              <button
                  v-for="page in visiblePages"
                  :key="page"
                  :class="['page-btn', { active: currentPage === page }]"
                  @click="currentPage = page"
              >{{ page }}</button>
              <button class="page-btn" :disabled="currentPage === totalPages" @click="currentPage++">下一页</button>
            </div>
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// ========== 学段数据 ==========
const stages = [
  { key: 'primary', name: '小学' },
  { key: 'junior', name: '初中' },
  { key: 'senior', name: '高中' },
  { key: 'art', name: '美术' },
  { key: 'dance', name: '舞蹈' },
]
const currentStage = ref<'primary' | 'junior' | 'senior' | 'art' | 'dance'>('primary')

// 学段对应的学科列表
const subjectDataMap: Record<string, any[]> = {
  primary: [
    { key: 'chinese', name: '语文', isNew: false },
    { key: 'math', name: '数学', isNew: false },
    { key: 'english', name: '英语', isNew: true },
    { key: 'politics', name: '政治（道德与法治）', isNew: false },
    { key: 'science', name: '科学', isNew: false },
    { key: 'music', name: '音乐', isNew: false },
    { key: 'art', name: '美术', isNew: false },
    { key: 'it', name: '信息技术', isNew: false },
    { key: 'psychology', name: '心理健康', isNew: false },
    { key: 'labor', name: '劳技', isNew: false },
    { key: 'calligraphy', name: '书法练习指导', isNew: false },
    { key: 'pe', name: '体育', isNew: false },
    { key: 'comprehensive', name: '综合实践活动', isNew: false },
    { key: 'local', name: '地方、校本课程', isNew: false },
  ],
  junior: [
    { key: 'chinese', name: '语文', isNew: false }, { key: 'math', name: '数学', isNew: false }, { key: 'english', name: '英语', isNew: false },
    { key: 'physics', name: '物理', isNew: false }, { key: 'chemistry', name: '化学', isNew: false }, { key: 'biology', name: '生物', isNew: false },
    { key: 'politics', name: '道德与法治', isNew: false }, { key: 'history', name: '历史', isNew: false }, { key: 'geography', name: '地理', isNew: false }
  ],
  senior: [
    { key: 'chinese', name: '语文', isNew: false }, { key: 'math', name: '数学', isNew: false }, { key: 'english', name: '英语', isNew: false },
    { key: 'physics', name: '物理', isNew: false }, { key: 'chemistry', name: '化学', isNew: false }, { key: 'biology', name: '生物', isNew: false },
    { key: 'politics', name: '政治', isNew: false }, { key: 'history', name: '历史', isNew: false }, { key: 'geography', name: '地理', isNew: false }
  ],
  art: [
    { key: 'drawing', name: '绘画', isNew: false }, { key: 'craft', name: '工艺美术', isNew: false }, { key: 'appreciation', name: '艺术欣赏', isNew: false }
  ],
  dance: [
    { key: 'chineseDance', name: '中国舞', isNew: false }, { key: 'ballet', name: '芭蕾舞', isNew: false }, { key: 'modern', name: '现代舞', isNew: false }
  ],
}

const currentSubjects = computed(() => subjectDataMap[currentStage.value] || [])
const currentSubject = ref<any>(null)

// 学段名称映射
const stageNames: Record<string, string> = {
  // 小学
  primary: '小学',
  grade1: '小学一年级',
  grade2: '小学二年级',
  grade3: '小学三年级',
  grade4: '小学四年级',
  grade5: '小学五年级',
  grade6: '小学六年级',
  // 初中
  junior: '初中',
  grade7: '初中七年级',
  grade8: '初中八年级',
  grade9: '初中九年级',
  // 高中
  senior: '高中',
  grade10: '高中一年级',
  grade11: '高中二年级',
  grade12: '高中三年级',
  // 艺体
  art: '美术',
  dance: '舞蹈',
}
const currentStageName = computed(() => stageNames[currentStage.value] || '小学')

// ========== 版本映射 ==========
const subjectVersionsMap: Record<string, any[]> = {
  chinese: [
    { key: 'tongbian2024', name: '统编版（2024）', isNew: true },
    { key: 'tongbian54', name: '统编版（五四制）（2024）', isNew: true },
    { key: 'renjiao', name: '人教部编版（五四制）', isNew: false },
  ],
  default: [{ key: 'default', name: '通用版', isNew: false }]
}

const currentSubjectVersions = computed(() => {
  if (!currentSubject.value) return []
  return subjectVersionsMap[currentSubject.value.key] || subjectVersionsMap.default
})

const selectedVersionKey = ref('tongbian2024')
const selectedVersionName = computed(() => {
  const ver = currentSubjectVersions.value.find(v => v.key === selectedVersionKey.value)
  return ver?.name || '统编版（2024）'
})
const currentVersionName = computed(() => selectedVersionName.value)
const currentPublisher = ref('人民教育出版社')
const isNewTextbook = ref(true)

function selectVersion(ver: any) {
  selectedVersionKey.value = ver.key
  isNewTextbook.value = ver.isNew
}
function setTextbookVersion(isNew: boolean) {
  isNewTextbook.value = isNew
  const ver = currentSubjectVersions.value.find(v => v.isNew === isNew)
  if (ver) selectedVersionKey.value = ver.key
}

// ========== 册别数据映射（学段/年级 -> 册别列表） ==========
const volumeDataMap: Record<string, { id: string; name: string; isNew: boolean }[]> = {
  // 小学学段
  primary: [
    { id: 'y1s1', name: '一年级上册', isNew: true },
    { id: 'y1s2', name: '一年级下册', isNew: true },
    { id: 'y2s1', name: '二年级上册', isNew: true },
    { id: 'y2s2', name: '二年级下册', isNew: true },
    { id: 'y3s1', name: '三年级上册', isNew: true },
    { id: 'y3s2', name: '三年级下册', isNew: true },
    { id: 'y4s1', name: '四年级上册', isNew: false },
    { id: 'y4s2', name: '四年级下册', isNew: false },
    { id: 'y5s1', name: '五年级上册', isNew: false },
    { id: 'y5s2', name: '五年级下册', isNew: false },
    { id: 'y6s1', name: '六年级上册', isNew: false },
    { id: 'y6s2', name: '六年级下册', isNew: false },
  ],
  // 小学具体年级
  grade1: [
    { id: 'y1s1', name: '一年级上册', isNew: true },
    { id: 'y1s2', name: '一年级下册', isNew: true },
  ],
  grade2: [
    { id: 'y2s1', name: '二年级上册', isNew: true },
    { id: 'y2s2', name: '二年级下册', isNew: true },
  ],
  grade3: [
    { id: 'y3s1', name: '三年级上册', isNew: true },
    { id: 'y3s2', name: '三年级下册', isNew: true },
  ],
  grade4: [
    { id: 'y4s1', name: '四年级上册', isNew: false },
    { id: 'y4s2', name: '四年级下册', isNew: false },
  ],
  grade5: [
    { id: 'y5s1', name: '五年级上册', isNew: false },
    { id: 'y5s2', name: '五年级下册', isNew: false },
  ],
  grade6: [
    { id: 'y6s1', name: '六年级上册', isNew: false },
    { id: 'y6s2', name: '六年级下册', isNew: false },
  ],
  // 初中学段
  junior: [
    { id: 'j7s1', name: '七年级上册', isNew: false },
    { id: 'j7s2', name: '七年级下册', isNew: false },
    { id: 'j8s1', name: '八年级上册', isNew: false },
    { id: 'j8s2', name: '八年级下册', isNew: false },
    { id: 'j9s1', name: '九年级上册', isNew: false },
    { id: 'j9s2', name: '九年级下册', isNew: false },
  ],
  // 初中小年级
  grade7: [
    { id: 'j7s1', name: '七年级上册', isNew: false },
    { id: 'j7s2', name: '七年级下册', isNew: false },
  ],
  grade8: [
    { id: 'j8s1', name: '八年级上册', isNew: false },
    { id: 'j8s2', name: '八年级下册', isNew: false },
  ],
  grade9: [
    { id: 'j9s1', name: '九年级上册', isNew: false },
    { id: 'j9s2', name: '九年级下册', isNew: false },
  ],
  // 高中学段
  senior: [
    { id: 's1s1', name: '高一上册', isNew: false },
    { id: 's1s2', name: '高一下册', isNew: false },
    { id: 's2s1', name: '高二上册', isNew: false },
    { id: 's2s2', name: '高二下册', isNew: false },
    { id: 's3s1', name: '高三上册', isNew: false },
    { id: 's3s2', name: '高三下册', isNew: false },
  ],
  // 高中小年级
  grade10: [
    { id: 's1s1', name: '高一上册', isNew: false },
    { id: 's1s2', name: '高一下册', isNew: false },
  ],
  grade11: [
    { id: 's2s1', name: '高二上册', isNew: false },
    { id: 's2s2', name: '高二下册', isNew: false },
  ],
  grade12: [
    { id: 's3s1', name: '高三上册', isNew: false },
    { id: 's3s2', name: '高三下册', isNew: false },
  ],
  // 艺体类
  art: [],
  dance: [],
}

// 动态册别列表
const volumeList = computed(() => volumeDataMap[currentStage.value] || [])

const selectedVolumeId = ref('')

// 根据学段自动设置默认册别
function setDefaultVolumeByStage() {
  const list = volumeList.value
  if (list.length > 0) {
    selectedVolumeId.value = list[0].id
  } else {
    selectedVolumeId.value = ''
  }
}
const currentGradeLevelName = computed(() => {
  const vol = volumeList.value.find(v => v.id === selectedVolumeId.value)
  return vol?.name || ''
})
function selectVolume(vol: any) {
  selectedVolumeId.value = vol.id
}
function onVolumeChange() {
  initUnitList()
}

// ========== 单元列表（带展开折叠） ==========
// 小学语文单元
const chineseUnitRaw = [
  { name: '第一单元·识字', subUnits: ['识字1', '识字2', '语文园地一'] },
  { name: '第二单元·阅读', subUnits: ['课文1', '课文2', '语文园地二'] },
  { name: '第三单元·阅读', subUnits: ['课文3', '课文4', '语文园地三'] },
  { name: '第四单元·阅读', subUnits: ['课文5', '课文6', '语文园地四'] },
  { name: '第五单元·识字', subUnits: ['识字5', '识字6', '语文园地五'] },
  { name: '第六单元·阅读', subUnits: ['课文7', '课文8', '语文园地六'] },
  { name: '第七单元·阅读', subUnits: ['课文9', '课文10', '语文园地七'] },
  { name: '第八单元·阅读', subUnits: ['课文11', '课文12', '语文园地八'] },
  { name: '语文园地一', subUnits: [] }, { name: '口语交际', subUnits: [] }, { name: '快乐读书吧', subUnits: [] }, { name: '单元整体教学', subUnits: [] }
]
// 初中语文单元
const juniorChineseUnitRaw = [
  { name: '第一单元·群星闪耀', subUnits: ['课文1 邓稼先', '课文2 说和做', '课文3 回忆鲁迅先生', '写作 写出人物精神'] },
  { name: '第二单元·家国情怀', subUnits: ['课文5 黄河颂', '课文6 老山界', '课文7 土地的誓言', '写作 学习抒情'] },
  { name: '第三单元·凡人小事', subUnits: ['课文9 阿长与<山海经>', '课文10 老王', '课文11 台阶', '写作 抓住细节'] },
  { name: '第四单元·修身正己', subUnits: ['课文13 叶圣陶先生二三事', '课文14 驿路梨花', '课文15 最苦与最乐', '写作 怎样选材'] },
  { name: '第五单元·哲理之思', subUnits: ['课文17 紫藤萝瀑布', '课文18 一棵小桃树', '课文19 外国诗二首', '写作 文从字顺'] },
  { name: '第六单元·探险科幻', subUnits: ['课文21 伟大的悲剧', '课文22 太空一日', '课文23 带上她的眼睛', '写作 语言简明'] },
]
// 高中语文单元
const seniorChineseUnitRaw = [
  { name: '第一单元·青春的价值', subUnits: ['沁园春·长沙', '立论', '这个过程真好', '单元学习任务'] },
  { name: '第二单元·劳动光荣', subUnits: ['红烛', '致云雀', '鹊桥仙·纤云弄巧', '单元学习任务'] },
  { name: '第三单元·诗文意境', subUnits: ['短歌行', '归园田居', '梦游天姥吟留别', '单元学习任务'] },
  { name: '第四单元·家乡文化', subUnits: ['调查家乡文化生活', '参与家乡文化建设', '单元学习任务'] },
  { name: '第五单元·乡土中国', subUnits: ['整本书阅读《乡土中国》', '单元学习任务'] },
  { name: '第六单元·学习之道', subUnits: ['劝学', '师说', '拿来主义', '单元学习任务'] },
  { name: '第七单元·自然情怀', subUnits: ['故都的秋', '荷塘月色', '我与地坛', '单元学习任务'] },
  { name: '第八单元·词语积累', subUnits: ['词语积累与词语解释', '单元学习任务'] },
]
const defaultUnitRaw = [{ name: '单元列表准备中', subUnits: [] }]

interface UnitItem {
  name: string
  subUnits: string[]
  expanded: boolean
}

const currentUnitListRaw = ref<UnitItem[]>([])
const allUnitsExpanded = ref(false)

function initUnitList() {
  let raw: Array<{ name: string; subUnits: string[] }> = defaultUnitRaw
  if (currentSubject.value?.key === 'chinese') {
    if (currentStage.value === 'primary') {
      raw = chineseUnitRaw
    } else if (currentStage.value === 'junior') {
      raw = juniorChineseUnitRaw
    } else if (currentStage.value === 'senior') {
      raw = seniorChineseUnitRaw
    } else {
      raw = defaultUnitRaw
    }
  }
  // 非语文学科，使用通用目录
  currentUnitListRaw.value = raw.map(u => ({ ...u, expanded: allUnitsExpanded.value }))
}
function toggleUnitExpand(unit: UnitItem) {
  unit.expanded = !unit.expanded
}
function toggleAllUnits() {
  allUnitsExpanded.value = !allUnitsExpanded.value
  currentUnitListRaw.value.forEach(u => u.expanded = allUnitsExpanded.value)
}
const currentUnitList = computed(() => currentUnitListRaw.value)
const activeUnit = ref('第一单元·识字')

// ========== 筛选栏数据 ==========
// ========== 根据学段动态获取栏目列表 ==========
const primaryColumns = ref([
  '同步备课', '开学专区', '月考', '期中', '期末', '小升初真题', '小升初模拟',
  '专题复习', '真题汇编', '暑假', '寒假', '作文', '阅读', '竞赛'
])

const juniorColumns = ref([
  '同步备课', '开学专区', '月考', '期中', '期末', '学业水平',
  '一轮复习', '二轮专题', '三轮冲刺', '中考模拟', '中考真题',
  '真题汇编', '暑假', '寒假', '作文', '阅读', '竞赛', '纯素材'
])

const seniorColumns = ref([
  '同步备课', '开学专区', '月考', '期中', '期末', '学业水平',
  '一轮复习', '二轮专题', '三轮冲刺', '高考模拟', '高考真题',
  '真题汇编', '寒假', '暑假', '作文', '阅读', '竞赛', '纯素材'
])

// 美术、舞蹈默认使用小学栏目
const artColumns = ref([...primaryColumns.value])
const danceColumns = ref([...primaryColumns.value])

const currentColumns = computed(() => {
  switch (currentStage.value) {
    case 'primary': return primaryColumns.value
    case 'junior':  return juniorColumns.value
    case 'senior':  return seniorColumns.value
    case 'art':     return artColumns.value
    case 'dance':   return danceColumns.value
    default:        return primaryColumns.value
  }
})

// 当前激活的栏目（学段切换时重置为第一个）
const activeColumn = ref('同步备课')

// 监听学段切换，重置 activeColumn
watch(currentColumns, (newCols) => {
  if (newCols.length > 0 && !newCols.includes(activeColumn.value)) {
    activeColumn.value = newCols[0]
  }
})

// 修改1: 资源类型列表与截图完全一致，将"音频朗读"改为"音频/朗读"
const resourceTypes = ref(['全部', '课件', '教案', '练习', '试卷', '学案', '电子课本', '教学反思', '音频/朗读', '视频', '知识点'])
const activeResourceType = ref('全部')

// 修改2: 教师用书列表与截图完全一致，移除第一个"教师用书"按钮，并改为截图中的8个选项
const teacherBookTypes = ref(['课堂实录', '讲义', '说课稿', '教学设计', '预习', '教学总结', '逐字稿', '公开课'])
// 修改3: 默认高亮教师用书列表的第一个选项
const activeTeacherBook = ref(teacherBookTypes.value[0])

// 选择资源类型（当选择非"课件"时，清空教师用书筛选）
function selectResourceType(type: string) {
  activeResourceType.value = type
  if (type !== '课件') {
    activeTeacherBook.value = ''  // 清空教师用书筛选
  }
}

const resourceMode = ref<'single' | 'suite'>('single')
const sortType = ref<'comprehensive' | 'latest' | 'downloads'>('comprehensive')
const filters = ref({
  premium: false,
  free: false,
  openClass: false,
  lessonSupport: false
})

// ========== 模拟成套资源数据 ==========
const suitePackages = ref([
  { key: 'courseware', icon: '📚', title: '成套装课件', sub: '2026统编版语文一年级下册PPT课件(电子新教材)', tag: '配套资源', fileCount: 24, updateTime: '2026-05-10' },
  { key: 'lessonplan', icon: '📖', title: '成套装教案', sub: '2026统编版一年级下册语文教案全册(新教材)', tag: '配套资源', fileCount: 18, updateTime: '2026-05-08' },
  { key: 'exercise', icon: '✏️', title: '成套装练习', sub: '2026统编版语文一年级下册同步练习可打印2026', tag: '配套资源', fileCount: 32, updateTime: '2026-05-05' },
  { key: 'guide', icon: '📘', title: '成套装学案', sub: '2026统编版语文一年级下册导学案及答案(新教材)', tag: '配套资源', fileCount: 15, updateTime: '2026-05-01' },
  { key: 'paper', icon: '📝', title: '成套装试卷', sub: '2026统编版语文一年级下册同步试卷2026', tag: '配套资源', fileCount: 12, updateTime: '2026-04-28' },
  { key: 'video', icon: '🎬', title: '成套装视频', sub: '2026统编版语文一年级下册教学视频全套', tag: '配套资源', fileCount: 20, updateTime: '2026-04-25' },
  { key: 'audio', icon: '🎧', title: '成套装音频', sub: '2026统编版语文一年级下册朗读音频MP3', tag: '配套资源', fileCount: 8, updateTime: '2026-04-22' },
  { key: 'test', icon: '📋', title: '成套装测试卷', sub: '2026统编版语文一年级下册单元测试卷含答案', tag: '配套资源', fileCount: 16, updateTime: '2026-04-20' },
  { key: 'summary', icon: '📑', title: '成套装教学总结', sub: '2026统编版语文一年级下册教学反思与总结', tag: '配套资源', fileCount: 10, updateTime: '2026-04-18' },
])
// ========== 成套资源分页状态 ==========
const suiteCurrentPage = ref(1)
const suitePageSize = ref(3)

const suitePaginatedSuites = computed(() => {
  const start = (suiteCurrentPage.value - 1) * suitePageSize.value
  return suitePackages.value.slice(start, start + suitePageSize.value)
})

const suiteTotalPages = computed(() =>
    Math.ceil(suitePackages.value.length / suitePageSize.value)
)

const suiteVisiblePages = computed(() => {
  const total = suiteTotalPages.value
  const current = suiteCurrentPage.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  let start = Math.max(1, current - 3)
  let end = Math.min(total, current + 3)
  if (end - start < 6) {
    if (start === 1) end = start + 6
    else if (end === total) start = end - 6
  }
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

// ========== 模拟单份资源数据 ==========
const allResources = ref([
  { id: 1, title: '【新教材核心素养】部编版语文一下识字8《人之初》优质课件+教案', isPremium: true, previews: ['课件预览1', '教案预览'], publishDate: '2026-05-12', downloads: 342, author: '教研室' },
  { id: 2, title: '部编版语文一年级下册《静夜思》公开课视频+逐字稿', isPremium: false, previews: ['视频截图', '逐字稿'], publishDate: '2026-05-10', downloads: 189, author: '名师工作室' },
  { id: 3, title: '小学语文一下第一单元 识字1《春夏秋冬》同步练习（含答案）', isPremium: false, previews: ['练习卷'], publishDate: '2026-05-08', downloads: 276, author: '学科网' },
  { id: 4, title: '【精品】统编版语文一年级下册单元整体教学设计（全册）', isPremium: true, previews: ['教学设计1', '教学设计2'], publishDate: '2026-05-05', downloads: 521, author: '特级教师' },
  { id: 5, title: '语文一下《荷叶圆圆》课件+动画素材+朗读音频', isPremium: false, previews: ['PPT', '音频'], publishDate: '2026-05-03', downloads: 198, author: '教育集团' },
  { id: 6, title: '部编版语文一年级下册期中复习知识点汇总（核心素养）', isPremium: false, previews: ['知识点清单'], publishDate: '2026-04-30', downloads: 467, author: '教研组' },
  { id: 7, title: '【公开课】《要下雨了》课堂实录+教学反思+课件', isPremium: true, previews: ['实录', '课件'], publishDate: '2026-04-28', downloads: 312, author: '公开课' },
  { id: 8, title: '语文一下第五单元《动物儿歌》教案+作业设计', isPremium: false, previews: ['教案', '作业'], publishDate: '2026-04-25', downloads: 154, author: '备课组' },
])

// 筛选和分页逻辑
const currentPage = ref(1)
const pageSize = 6

const filteredResources = computed(() => {
  let res = [...allResources.value]
  if (resourceMode.value === 'single') {
    if (activeResourceType.value !== '全部') {
      // 如果选择了"课件"且有教师用书子选项，进一步筛选
      if (activeResourceType.value === '课件' && activeTeacherBook.value) {
        res = res.filter(r => r.title.includes(activeResourceType.value) && r.title.includes(activeTeacherBook.value))
      } else {
        res = res.filter(r => r.title.includes(activeResourceType.value))
      }
    }
    if (filters.value.premium) res = res.filter(r => r.isPremium)
    if (filters.value.free) res = res.filter(r => r.title.includes('免费'))
    if (filters.value.openClass) res = res.filter(r => r.title.includes('公开课'))
    if (filters.value.lessonSupport) res = res.filter(r => r.title.includes('课时'))
    if (sortType.value === 'latest') res.sort((a, b) => new Date(b.publishDate).getTime() - new Date(a.publishDate).getTime())
    if (sortType.value === 'downloads') res.sort((a, b) => b.downloads - a.downloads)
  } else {
    res = []
  }
  return res
})

const filteredTotalCount = computed(() => resourceMode.value === 'single' ? filteredResources.value.length : suitePackages.value.length)
const paginatedResources = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredResources.value.slice(start, start + pageSize)
})
const totalPages = computed(() => Math.ceil(filteredResources.value.length / pageSize))

const visiblePages = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  let start = Math.max(1, current - 3)
  let end = Math.min(total, current + 3)
  if (end - start < 6) {
    if (start === 1) end = start + 6
    else if (end === total) start = end - 6
  }
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

watch([filteredResources, resourceMode], () => {
  currentPage.value = 1
})

// 监听成套资源变化或模式切换时重置页码
watch([suitePackages, resourceMode], () => {
  suiteCurrentPage.value = 1
})

// ========== 其他交互 ==========
const searchKeyword = ref('')
const cartCount = ref(3)
function handleSearch() {
  console.log('搜索:', searchKeyword.value)
}
function showCart() {
  console.log('打开资料篮')
}
function goToUpload() {
  router.push('/upload')
}
function toggleVersionModal() {
  console.log('切换版本册别')
}
function viewMoreSuites() {
  console.log('查看更多成套')
}
function viewMoreResources() {
  console.log('查看更多单份资源')
}

// 学段切换
function switchStage(key: string) {
  currentStage.value = key as any
  setDefaultVolumeByStage()           // 重置册别
  const subjects = subjectDataMap[currentStage.value] || []
  if (subjects.length) {
    currentSubject.value = subjects[0]
    initSubjectDefaults(currentSubject.value)
  } else {
    currentSubject.value = null
  }
  initUnitList()                      // 重新生成目录
  // 重置激活栏目为当前学段第一个栏目
  if (currentColumns.value.length) {
    activeColumn.value = currentColumns.value[0]
  }
}

// 学科选择
function selectSubject(subject: any) {
  if (currentSubject.value?.key === subject.key) return
  currentSubject.value = subject
  initSubjectDefaults(subject)
  initUnitList()
}

function initSubjectDefaults(subject: any) {
  if (subject.key === 'chinese') {
    selectedVersionKey.value = 'tongbian2024'
    // 册别由 switchStage 中的 setDefaultVolumeByStage() 设置，此处不再硬编码
    activeUnit.value = currentUnitList.value[0]?.name || ''
    isNewTextbook.value = true
  } else {
    selectedVersionKey.value = 'default'
    activeUnit.value = currentUnitList.value[0]?.name || ''
  }
  initUnitList()
}

onMounted(() => {
  // 从 URL 参数初始化
  const urlStage = route.params.stage as string
  const urlSubject = route.params.subject as string
  const urlVersion = route.params.version as string

  if (urlStage) {
    // 设置学段/年级
    currentStage.value = urlStage as any

    // 将具体年级映射回学段，以获取学科列表
    const stageToSection: Record<string, string> = {
      primary: 'primary', grade1: 'primary', grade2: 'primary', grade3: 'primary',
      grade4: 'primary', grade5: 'primary', grade6: 'primary',
      junior: 'junior', grade7: 'junior', grade8: 'junior', grade9: 'junior',
      senior: 'senior', grade10: 'senior', grade11: 'senior', grade12: 'senior',
      art: 'art', dance: 'dance',
    }
    const section = stageToSection[urlStage] || 'primary'

    // 设置册别
    setDefaultVolumeByStage()

    // 初始化学科
    if (urlSubject) {
      const subjects = subjectDataMap[section] || []
      const targetSubject = subjects.find(s => s.key === urlSubject)
      if (targetSubject) {
        currentSubject.value = targetSubject
        initSubjectDefaults(targetSubject)
      }
    }

    // 设置版本
    if (urlVersion) {
      selectedVersionKey.value = urlVersion
      // 根据版本判断是否是新教材
      const ver = currentSubjectVersions.value.find(v => v.key === urlVersion)
      if (ver) {
        isNewTextbook.value = ver.isNew
      }
    }

    initUnitList()
  } else {
    // 默认初始化
    switchStage('primary')
  }
})
</script>

<style scoped>
/* 基础样式 */
.platform-page {
  min-height: 100vh;
  background: #F5F7FA;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* ===== 顶部导航栏 ===== */
.top-nav {
  background: #FFF8E7;
  border-bottom: 1px solid #F0E5CF;
  position: sticky;
  top: 0;
  z-index: 20;
}
.top-nav-inner {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 24px;
  height: 68px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
}
.nav-left-group {
  display: flex;
  align-items: center;
  gap: 32px;
}
.site-name {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #E67E22, #F39C12);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.stage-bar {
  display: flex;
  gap: 6px;
  background: rgba(255, 245, 225, 0.8);
  padding: 4px;
  border-radius: 48px;
}
.stage-btn {
  padding: 8px 20px;
  border: none;
  background: transparent;
  border-radius: 40px;
  font-size: 15px;
  font-weight: 500;
  color: #5D4E37;
  cursor: pointer;
  transition: 0.2s;
}
.stage-btn.active {
  background: #fff;
  color: #409EFF;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}
.search-wrapper {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 48px;
  border: 1px solid #F0E0C8;
  overflow: hidden;
  flex-shrink: 0;
}
.nav-right-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: nowrap;
}
.search-input {
  padding: 8px 16px;
  border: none;
  outline: none;
  width: 200px;
}
.search-btn {
  background: #409EFF;
  border: none;
  padding: 8px 18px;
  color: white;
  cursor: pointer;
  font-weight: 500;
}
.cart-btn {
  background: #fff;
  border: 1px solid #F0E0C8;
  border-radius: 48px;
  padding: 8px 18px;
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.cart-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background: #F56C6C;
  color: white;
  border-radius: 20px;
  padding: 0px 6px;
  font-size: 12px;
}
.upload-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #52c41a, #389e0d);
  border: none;
  border-radius: 40px;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.25);
}
.upload-btn:hover {
  background: linear-gradient(135deg, #73d13d, #52c41a);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.35);
}
.upload-icon {
  font-size: 16px;
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
.new-dot {
  width: 6px;
  height: 6px;
  background: #F56C6C;
  border-radius: 50%;
  display: inline-block;
  margin-left: 4px;
  vertical-align: middle;
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
.column-tab.more {
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
  gap: 24px;
  border: 1px solid #EEF2F6;
}
.type-group {
  display: flex;
  align-items: center;
  gap: 8px;
}
.type-label {
  font-weight: 600;
  color: #2C3E50;
  font-size: 13px;
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
/* 教师用书子筛选器缩进 */
.sub-filter-group {
  margin-top: 8px;
  margin-left: 20px;
  padding-left: 12px;
  border-left: 2px dashed #DCDFE6;
}
.sub-btn {
  font-size: 11px;
  padding: 3px 10px;
  background: #FAFAFA;
  border: 1px dashed #DCDFE6;
}
.sub-btn:hover {
  background: #F0F9FF;
  border-color: #409EFF;
}
.sub-btn.active {
  background: #F0F9FF;
  border: 1px solid #409EFF;
  color: #409EFF;
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
}
.new-badge {
  position: absolute;
  top: -6px;
  right: -10px;
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
.volume-quick {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.volume-label {
  font-weight: 600;
  font-size: 13px;
  color: #2C3E50;
  flex-shrink: 0;
}
.volume-select {
  padding: 6px 12px;
  border-radius: 20px;
  border: 1px solid #DCDFE6;
  flex: 1;
  font-size: 13px;
  cursor: pointer;
}
.unit-directory {
  margin-top: 8px;
}
.unit-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-weight: bold;
  font-size: 14px;
  color: #2C3E50;
}
.collapse-all-btn {
  font-size: 12px;
  background: transparent;
  border: 1px solid #DCDFE6;
  border-radius: 20px;
  padding: 2px 10px;
  cursor: pointer;
  color: #909399;
}
.unit-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.unit-item-wrapper {
  margin-bottom: 4px;
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
.sub-unit-list {
  padding-left: 32px;
  margin-top: 4px;
}
.sub-unit {
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
  border-radius: 8px;
  color: #606266;
  transition: 0.15s;
}
.sub-unit:hover {
  background: #F0F5FF;
}

/* 右侧资源列表 */
.resource-list-area {
  min-width: 0;
}
.suite-section, .single-resource-section {
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 24px;
}
.section-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 20px;
}
.section-header h3 {
  margin: 0;
  font-size: 20px;
  color: #1F2F3A;
}
.star-icon {
  color: #E67E22;
}
.section-desc {
  font-size: 13px;
  color: #909399;
  flex: 1;
}
.more-link {
  color: #8C9AA8;
  cursor: pointer;
  font-size: 13px;
  transition: 0.15s;
  flex-shrink: 0;
}
.more-link:hover {
  color: #409EFF;
}
.suite-card-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.suite-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #FAFCFF;
  border-radius: 16px;
  padding: 16px;
  border: 1px solid #EDEDED;
  position: relative;
  transition: 0.2s;
}
.suite-card:hover {
  border-color: #409EFF;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}
.card-left {
  display: flex;
  gap: 16px;
  align-items: center;
}
.card-icon {
  font-size: 40px;
}
.card-info .card-title {
  font-weight: 700;
  font-size: 15px;
  margin-bottom: 4px;
}
.card-sub {
  font-size: 12px;
  color: #8C9AA8;
  margin-bottom: 4px;
}
.card-meta {
  font-size: 12px;
  color: #8C9AA8;
}
.download-btn {
  background: #409EFF;
  color: #fff;
  border: none;
  padding: 8px 20px;
  border-radius: 30px;
  cursor: pointer;
  font-size: 13px;
  transition: 0.15s;
  flex-shrink: 0;
}
.download-btn:hover {
  background: #66b1ff;
}
.card-tag {
  position: absolute;
  top: 8px;
  right: 12px;
  background: #F0F2F6;
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 20px;
}
.resource-cards {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.resource-card {
  padding: 16px;
  border: 1px solid #EEF2F6;
  border-radius: 20px;
  transition: 0.2s;
}
.resource-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 14px rgba(0,0,0,0.05);
}
.resource-header {
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
.resource-title {
  font-weight: 600;
  font-size: 15px;
}
.resource-previews {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.preview-thumb {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #F8F9FC;
  padding: 6px 12px;
  border-radius: 30px;
  font-size: 12px;
  color: #606266;
}
.thumb-icon {
  font-size: 14px;
}
.resource-footer {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #8C9AA8;
}
.publish-date {
  flex-shrink: 0;
}
.download-count {
  color: #409EFF;
  flex-shrink: 0;
}
.author {
  margin-left: auto;
  flex-shrink: 0;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 24px;
}
.page-btn {
  padding: 6px 12px;
  border-radius: 30px;
  border: 1px solid #DCDFE6;
  background: white;
  cursor: pointer;
  font-size: 13px;
  transition: 0.15s;
  color: #606266;
}
.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.page-btn.active {
  background: #409EFF;
  color: white;
  border-color: #409EFF;
}

/* 响应式 */
@media (max-width: 768px) {
  .nav-right-actions {
    flex-wrap: wrap;
  }
}
@media (max-width: 860px) {
  .content-two-columns {
    grid-template-columns: 1fr;
  }
  .advanced-filter-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  .filter-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .subject-tabs, .column-tabs {
    gap: 6px;
  }
  .filter-middle {
    flex-direction: column;
    gap: 12px;
  }
}
</style>

