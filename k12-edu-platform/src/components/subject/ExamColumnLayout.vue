<template>
  <!-- 调试标记：如果看到这行文字说明组件已渲染 -->
  <div class="exam-layout" data-layout="exam">
    <div style="background:red;color:white;padding:8px;font-size:14px;margin-bottom:12px;border-radius:6px;">
      [DEBUG] ExamColumnLayout 已渲染 | activeColumn={{ activeColumn }} | resources={{ resources.length }} 条 | total={{ total }}
    </div>
    <!-- 左侧内容区 -->
    <div class="exam-main">
      <!-- 多维度筛选区 -->
      <div class="multi-filter-card">
        <!-- 年级 + 学期 行 -->
        <div class="filter-row" v-if="showGradeFilter">
          <span class="filter-label">年级：</span>
          <div class="filter-tags">
            <span
              v-for="g in gradeList"
              :key="g"
              :class="['tag', { active: selectedGrade === g }]"
              @click="selectedGrade = g"
            >{{ g }}</span>
          </div>
          <span class="filter-label ml-24">学期：</span>
          <div class="filter-tags">
            <span
              v-for="t in termList"
              :key="t"
              :class="['tag', { active: selectedTerm === t }]"
              @click="selectedTerm = t"
            >{{ t }}</span>
          </div>
        </div>

        <!-- 年份 + 版本 行 -->
        <div class="filter-row">
          <span class="filter-label">年份：</span>
          <div class="filter-tags">
            <span
              v-for="y in yearList"
              :key="y"
              :class="['tag', { active: selectedYear === y }]"
              @click="selectedYear = y"
            >{{ y }}</span>
            <span class="more-tag" @click="showMoreYears = !showMoreYears">
              更早 <i :class="showMoreYears ? 'arrow-up' : 'arrow-down'"></i>
            </span>
          </div>
          <span class="filter-label ml-24">版本：</span>
          <div class="filter-tags">
            <span
              v-for="v in versionList"
              :key="v"
              :class="['tag', { active: selectedVersion === v }]"
              @click="selectedVersion = v"
            >{{ v }}</span>
            <span class="more-tag" v-if="moreVersions.length" @click="showMoreVersions = !showMoreVersions">
              更多 <i :class="showMoreVersions ? 'arrow-up' : 'arrow-down'"></i>
            </span>
          </div>
        </div>
        <!-- 更多版本展开 -->
        <div class="filter-row extra-row" v-if="showMoreVersions && moreVersions.length">
          <span class="filter-label"></span>
          <div class="filter-tags">
            <span
              v-for="v in moreVersions"
              :key="v"
              :class="['tag', { active: selectedVersion === v }]"
              @click="selectedVersion = v"
            >{{ v }}</span>
          </div>
        </div>

        <!-- 地区 行 -->
        <div class="filter-row">
          <span class="filter-label">地区：</span>
          <div class="filter-tags">
            <span
              v-for="area in displayAreas"
              :key="area"
              :class="['tag', { active: selectedArea === area }]"
              @click="selectedArea = area"
            >{{ area }}</span>
            <span class="more-tag" v-if="moreAreas.length" @click="showMoreAreas = !showMoreAreas">
              更多 <i :class="showMoreAreas ? 'arrow-up' : 'arrow-down'"></i>
            </span>
          </div>
        </div>
        <!-- 更多地区展开 -->
        <div class="filter-row extra-row" v-if="showMoreAreas && moreAreas.length">
          <span class="filter-label"></span>
          <div class="filter-tags">
            <span
              v-for="area in moreAreas"
              :key="area"
              :class="['tag', { active: selectedArea === area }]"
              @click="selectedArea = area"
            >{{ area }}</span>
          </div>
        </div>
      </div>

      <!-- 类型筛选 + 操作栏 -->
      <div class="action-bar">
        <div class="type-bar">
          <span class="filter-label">类型：</span>
          <div class="type-tags">
            <span
              v-for="t in examTypeList"
              :key="t"
              :class="['type-tag', { active: selectedType === t }]"
              @click="selectedType = t"
            >{{ t }}</span>
          </div>
        </div>
        <div class="action-right">
          <button class="featured-btn">点击找成套资料</button>
        </div>
      </div>

      <!-- 模式切换 + 排序 + 结果计数 -->
      <div class="sort-bar">
        <div class="mode-btns">
          <button :class="['mode-btn', { active: resourceMode === 'single' }]" @click="$emit('update:resourceMode', 'single')">找单份</button>
          <button :class="['mode-btn', { active: resourceMode === 'suite' }]" @click="$emit('update:resourceMode', 'suite')">找成套</button>
        </div>
        <div class="sort-btns">
          <span :class="['sort-item', { active: sortType === 'comprehensive' }]" @click="$emit('update:sortType', 'comprehensive')">综合</span>
          <span :class="['sort-item', { active: sortType === 'latest' }]" @click="$emit('update:sortType', 'latest')">最新</span>
          <span :class="['sort-item', { active: sortType === 'downloads' }]" @click="$emit('update:sortType', 'downloads')">下载量</span>
        </div>
        <div class="filter-checkboxes">
          <label class="check-label"><input type="checkbox" v-model="filterPremium" /> 精品</label>
          <label class="check-label"><input type="checkbox" v-model="filterHasAnswer" /> 有答案</label>
          <label class="check-label"><input type="checkbox" v-model="filterTextOnly" /> 文字版</label>
          <span class="doc-type-btn">文档类型 <i class="arrow-down"></i></span>
        </div>
        <div class="result-count">共 {{ total }} 条结果</div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <span class="loading-spinner"></span> 正在加载资源...
      </div>

      <!-- 空状态 -->
      <div v-else-if="!resources.length" class="empty-state">
        暂无匹配的资源，请尝试调整筛选条件
      </div>

      <!-- 资源列表（条目样式） -->
      <div v-else class="resource-list">
        <div
          v-for="item in resources"
          :key="item.id"
          class="resource-row"
          @click="$emit('open-resource', item)"
        >
          <div class="row-left">
            <span class="doc-icon" :class="getDocIconClass(item)">
              <i class="doc-icon-inner">{{ getDocIconLetter(item) }}</i>
            </span>
          </div>
          <div class="row-body">
            <div class="row-title-line">
              <span v-if="item.fileSizeKb && item.fileSizeKb > 500" class="badge-premium">精</span>
              <span class="row-title">{{ item.title }}</span>
            </div>
            <div class="row-meta">
              <span class="meta-date">{{ formatDate(item.uploadTime) }}</span>
              <span class="meta-sep">|</span>
              <span class="meta-down">{{ item.downloadCount || 0 }}次下载</span>
              <span class="meta-sep">|</span>
              <span class="meta-author">{{ item.uploader || '教学网资源' }}</span>
              <span v-if="item.unitName" class="meta-ref">
                专辑：<a class="ref-link">{{ item.unitName }}</a>
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
        <button class="page-btn" :disabled="currentPage === 1" @click="$emit('update:currentPage', currentPage - 1)">上一页</button>
        <button
          v-for="page in visiblePages"
          :key="page"
          :class="['page-btn', { active: currentPage === page }]"
          @click="$emit('update:currentPage', page)"
        >{{ page }}</button>
        <button class="page-btn" :disabled="currentPage === totalPages" @click="$emit('update:currentPage', currentPage + 1)">下一页</button>
      </div>
    </div>

    <!-- 右侧侧边栏 -->
    <div class="exam-sidebar">
      <!-- 精品成套推荐 -->
      <div class="sidebar-card">
        <div class="sidebar-title orange">精品成套资料推荐</div>
        <div class="featured-list">
          <template v-if="featuredSuites && featuredSuites.length">
            <div
              v-for="(item, idx) in featuredSuites"
              :key="idx"
              class="featured-item"
              @click="$emit('open-resource', item)"
            >
              <span class="featured-rank" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</span>
              <span class="featured-text">{{ item.title }}</span>
              <span v-if="item.fileCount" class="featured-count">【共{{ item.fileCount }}份】</span>
            </div>
          </template>
          <div v-else class="featured-empty">
            <span class="featured-placeholder" v-for="(ph, i) in placeholderFeatured" :key="i">
              <span class="featured-rank" :class="'rank-' + (i + 1)">{{ i + 1 }}</span>
              <span class="featured-text">{{ ph }}</span>
            </span>
          </div>
        </div>
      </div>

      <!-- 精选专题 -->
      <div class="sidebar-card mt-16">
        <div class="sidebar-title-row">
          <span class="sidebar-title orange">精选专题</span>
          <span class="sidebar-more" @click="$emit('view-more-topics')">更多 &gt;</span>
        </div>
        <div class="topic-card-list">
          <template v-if="selectedTopics && selectedTopics.length">
            <div
              v-for="(topic, idx) in selectedTopics"
              :key="idx"
              class="topic-card"
              @click="$emit('open-resource', topic)"
            >
              <div class="topic-thumb">
                <img v-if="topic.thumbUrl" :src="topic.thumbUrl" alt="" />
                <div v-else class="topic-thumb-placeholder" :style="{ background: topicColors[idx % topicColors.length] }">
                  <span>{{ topic.title?.substring(0, 4) }}</span>
                </div>
              </div>
              <div class="topic-info">
                <div class="topic-title">{{ topic.title }}</div>
                <div class="topic-date">{{ formatDate(topic.uploadTime) }}</div>
              </div>
            </div>
          </template>
          <template v-else>
            <div v-for="(pt, pi) in placeholderTopics" :key="'pt-' + pi" class="topic-card">
              <div class="topic-thumb">
                <div class="topic-thumb-placeholder" :style="{ background: topicColors[pi % topicColors.length] }">
                  <span>{{ pt.title?.substring(0, 4) }}</span>
                </div>
              </div>
              <div class="topic-info">
                <div class="topic-title">{{ pt.title }}</div>
                <div class="topic-date">{{ pt.date }}</div>
              </div>
            </div>
          </template>
        </div>

        <!-- 小列表专题 -->
        <div class="topic-link-list" v-if="(topicLinks || []).length">
          <div
            v-for="(link, idx) in (topicLinks || [])"
            :key="idx"
            class="topic-link"
            @click="$emit('open-resource', link)"
          >
            <span class="topic-dot" :class="idx < 3 ? 'hot' : ''">{{ idx < 3 ? '🔥' : '·' }}</span>
            <span class="topic-link-text">{{ link.title }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const props = defineProps<{
  resources: any[]
  total: number
  loading: boolean
  currentPage: number
  totalPages: number
  visiblePages: number[]
  resourceMode: 'single' | 'suite'
  sortType: string
  activeColumn: string
  currentGradeLevelName?: string
  selectedVersionName?: string
  // 侧边栏数据
  featuredSuites?: any[]
  selectedTopics?: any[]
  topicLinks?: any[]
}>()

const emit = defineEmits<{
  'update:currentPage': [value: number]
  'update:resourceMode': [value: 'single' | 'suite']
  'update:sortType': [value: string]
  'update:selectedGrade': [value: string]
  'update:selectedYear': [value: string]
  'update:selectedTerm': [value: string]
  'update:selectedVersion': [value: string]
  'update:selectedArea': [value: string]
  'update:selectedType': [value: string]
  'open-resource': [item: any]
  'view-more': []
  'view-more-topics': []
}>()

// 是否显示年级筛选（月考/期中/期末显示，小升初不显示）
const showGradeFilter = computed(() => {
  return !['小升初真题', '小升初模拟'].includes(props.activeColumn)
})

// 年级列表
const gradeList = ['一年级', '二年级', '三年级', '四年级', '五年级', '六年级']
const selectedGrade = ref('一年级')

// 学期列表
const termList = ['全部', '上学期', '下学期']
const selectedTerm = ref('下学期')

// 年份列表（展示最近6年，更多折叠）
const currentYear = new Date().getFullYear()
const yearList = ['全部', ...Array.from({ length: 5 }, (_, i) => String(currentYear - i))]
const selectedYear = ref('全部')
const showMoreYears = ref(false)

// 版本列表
const mainVersions = ['全部', '通用', '统编版（2024）']
const moreVersions = ['统编版（五四制）（2024）', '人教版（2001）', '北师大版']
const versionList = mainVersions
const selectedVersion = ref('全部')
const showMoreVersions = ref(false)

// 地区
const mainAreas = ['全部', '北京市', '天津市', '河北省', '山西省', '内蒙古', '辽宁省', '吉林省', '黑龙江省', '上海市', '江苏省', '浙江省', '安徽省', '福建省']
const extraAreas = ['广东省', '广西', '海南省', '四川省', '贵州省', '云南省', '西藏', '陕西省', '甘肃省', '青海省', '宁夏', '新疆']
const displayAreas = mainAreas
const moreAreas = extraAreas
const showMoreAreas = ref(false)
const selectedArea = ref('全部')

// 类型
const examTypeList = ['全部', '真题', '真题汇编', '单元测试', '知识点', '专项练习', '模拟卷', '培优拔尖']
const selectedType = ref('全部')

// 过滤选项
const filterPremium = ref(false)
const filterHasAnswer = ref(false)
const filterTextOnly = ref(false)

// 侧边栏颜色
const topicColors = ['#4CAF50', '#FF9800', '#2196F3', '#9C27B0', '#F44336']

// 默认占位数据（无真实数据时展示）
const placeholderFeatured = [
  '2026年春季下学期单元测试卷合集',
  '小学语文期中复习专项训练',
  '期末模拟冲刺试卷（含答案）',
  '同步练习精编·基础版',
  '考点突破培优训练',
]
const placeholderTopics = [
  { title: '2026年春季各科单元复习合辑', date: '2026-05-13' },
  { title: '小升初语文总复习精选资料', date: '2026-05-12' },
  { title: '【精品】期中考试备考资料包', date: '2026-05-11' },
]

// 工具方法
function formatDate(dt: any): string {
  if (!dt) return ''
  if (typeof dt === 'string') return dt.replace('T', ' ').substring(0, 10)
  try { return new Date(dt).toLocaleDateString('zh-CN') } catch { return String(dt) }
}

function getDocIconClass(item: any): string {
  const type = item.type || ''
  if (type.includes('试卷') || type.includes('真题')) return 'icon-pdf'
  if (type.includes('课件')) return 'icon-ppt'
  return 'icon-word'
}

function getDocIconLetter(item: any): string {
  const type = item.type || ''
  if (type.includes('试卷') || type.includes('真题')) return 'PDF'
  if (type.includes('课件')) return 'PPT'
  return 'W'
}
</script>

<style scoped>
.exam-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.exam-main {
  flex: 1;
  min-width: 0;
}

/* 多维度筛选卡片 */
.multi-filter-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
  margin-bottom: 12px;
}

.filter-row {
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 6px 0;
  padding: 8px 0;
  border-bottom: 1px solid #F5F5F5;
}
.filter-row:last-child { border-bottom: none; }
.extra-row { padding-top: 0; }

.filter-label {
  font-size: 13px;
  color: #666;
  font-weight: 600;
  white-space: nowrap;
  min-width: 42px;
  padding-top: 4px;
}
.ml-24 { margin-left: 24px; }

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  flex: 1;
}

.tag {
  padding: 3px 12px;
  border-radius: 14px;
  font-size: 13px;
  color: #555;
  cursor: pointer;
  transition: 0.2s;
  background: #F5F7FA;
  white-space: nowrap;
  user-select: none;
}
.tag:hover { background: #E8ECF1; color: #333; }
.tag.active {
  background: #409EFF;
  color: #fff;
  box-shadow: 0 2px 6px rgba(64,158,255,0.3);
}

.more-tag {
  padding: 3px 12px;
  border-radius: 14px;
  font-size: 13px;
  color: #409EFF;
  cursor: pointer;
  background: #EBF5FF;
  white-space: nowrap;
  user-select: none;
}
.more-tag:hover { background: #D6EEFF; }

/* 类型栏 + 操作栏 */
.action-bar {
  background: #fff;
  border-radius: 10px;
  padding: 12px 20px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
  margin-bottom: 10px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}
.type-bar {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex: 1;
}
.type-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.type-tag {
  padding: 4px 12px;
  border-radius: 14px;
  font-size: 13px;
  color: #555;
  cursor: pointer;
  background: #F5F7FA;
  transition: 0.2s;
  user-select: none;
}
.type-tag:hover { background: #E8ECF1; }
.type-tag.active {
  background: #409EFF;
  color: #fff;
}
.action-right {}
.featured-btn {
  padding: 6px 14px;
  background: linear-gradient(135deg, #FF8C00, #FFA940);
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(255,140,0,0.3);
  white-space: nowrap;
}
.featured-btn:hover { opacity: 0.9; }

/* 排序栏 */
.sort-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 20px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
  margin-bottom: 10px;
  flex-wrap: wrap;
}
.mode-btns { display: flex; gap: 6px; }
.mode-btn {
  padding: 5px 14px;
  border: 1px solid #E0D9D0;
  border-radius: 16px;
  background: #fff;
  font-size: 13px;
  color: #5D4E37;
  cursor: pointer;
  transition: 0.2s;
}
.mode-btn.active { background: #409EFF; color: #fff; border-color: #409EFF; }
.sort-btns { display: flex; gap: 4px; }
.sort-item {
  padding: 4px 10px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  border-radius: 4px;
  transition: 0.2s;
}
.sort-item.active { color: #409EFF; font-weight: 600; }
.filter-checkboxes { display: flex; align-items: center; gap: 10px; }
.check-label { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #555; cursor: pointer; }
.check-label input { cursor: pointer; }
.doc-type-btn {
  padding: 4px 10px;
  border: 1px solid #DDD;
  border-radius: 4px;
  font-size: 13px;
  color: #555;
  cursor: pointer;
  background: #fff;
}
.result-count { margin-left: auto; font-size: 13px; color: #999; white-space: nowrap; }

/* 加载/空状态 */
.loading-state, .empty-state {
  display: flex; align-items: center; justify-content: center;
  padding: 40px; color: #909399; font-size: 14px;
  background: #fff; border-radius: 10px;
}
.loading-spinner {
  display: inline-block; width: 16px; height: 16px;
  border: 2px solid #409EFF; border-top-color: transparent;
  border-radius: 50%; animation: spin 0.8s linear infinite; margin-right: 8px;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 资源列表（条目样式） */
.resource-list { display: flex; flex-direction: column; gap: 0; }
.resource-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 20px;
  background: #fff;
  border-bottom: 1px solid #F5F5F5;
  cursor: pointer;
  transition: background 0.15s;
}
.resource-row:first-child { border-radius: 10px 10px 0 0; }
.resource-row:last-child { border-radius: 0 0 10px 10px; border-bottom: none; }
.resource-row:only-child { border-radius: 10px; }
.resource-row:hover { background: #F9FBFF; }

.row-left { padding-top: 2px; }
.doc-icon {
  display: inline-flex; align-items: center; justify-content: center;
  width: 32px; height: 32px; border-radius: 6px; font-size: 10px; font-weight: 700;
}
.icon-word { background: #2B5797; }
.icon-pdf { background: #C0392B; }
.icon-ppt { background: #D35400; }
.doc-icon-inner { color: #fff; font-size: 10px; letter-spacing: -0.5px; }

.row-body { flex: 1; min-width: 0; }
.row-title-line { display: flex; align-items: center; gap: 6px; margin-bottom: 6px; }
.badge-premium {
  padding: 1px 5px; background: #F56C6C; color: #fff;
  font-size: 11px; border-radius: 3px; flex-shrink: 0;
}
.row-title {
  font-size: 15px; font-weight: 500; color: #333;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.row-title:hover { color: #409EFF; }
.row-meta { display: flex; align-items: center; flex-wrap: wrap; gap: 4px; font-size: 12px; color: #999; }
.meta-sep { color: #DDD; }
.ref-link { color: #409EFF; cursor: pointer; }
.ref-link:hover { text-decoration: underline; }

/* 分页 */
.pagination { display: flex; justify-content: center; gap: 6px; margin-top: 16px; }
.page-btn {
  padding: 6px 12px; border: 1px solid #DCDFE6;
  background: #fff; border-radius: 4px; cursor: pointer; transition: 0.2s; font-size: 13px;
}
.page-btn:hover:not(:disabled) { color: #409EFF; border-color: #409EFF; }
.page-btn.active { background: #409EFF; color: #fff; border-color: #409EFF; }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* 右侧侧边栏 */
.exam-sidebar { width: 260px; flex-shrink: 0; }
.sidebar-card {
  background: #fff; border-radius: 10px;
  padding: 16px; box-shadow: 0 1px 6px rgba(0,0,0,0.05);
}
.mt-16 { margin-top: 16px; }
.sidebar-title { font-size: 15px; font-weight: 700; margin-bottom: 12px; }
.sidebar-title.orange { color: #E8720C; }
.sidebar-title-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.sidebar-more { font-size: 13px; color: #409EFF; cursor: pointer; }

/* 精品成套推荐 */
.featured-list { display: flex; flex-direction: column; gap: 10px; }
.featured-item {
  display: flex; align-items: flex-start; gap: 8px;
  cursor: pointer; padding: 4px 0; transition: 0.15s;
}
.featured-item:hover .featured-text { color: #409EFF; }
.featured-rank {
  display: inline-flex; align-items: center; justify-content: center;
  width: 18px; height: 18px; border-radius: 4px;
  font-size: 12px; font-weight: 700; color: #fff; flex-shrink: 0; margin-top: 1px;
}
.rank-1 { background: #E8720C; }
.rank-2 { background: #F59A23; }
.rank-3 { background: #F7C948; }
.featured-rank:not(.rank-1):not(.rank-2):not(.rank-3) { background: #CCC; color: #666; }
.featured-text { font-size: 13px; color: #333; line-height: 1.4; flex: 1; }
.featured-count { font-size: 12px; color: #409EFF; white-space: nowrap; }
.featured-empty { display: flex; flex-direction: column; gap: 10px; }
.featured-placeholder {
  display: flex; align-items: flex-start; gap: 8px;
  padding: 4px 0;
}
.featured-placeholder .featured-text { color: #999; }

/* 精选专题 */
.topic-card-list { display: flex; flex-direction: column; gap: 12px; margin-bottom: 12px; }
.topic-card { display: flex; gap: 10px; cursor: pointer; }
.topic-card:hover .topic-title { color: #409EFF; }
.topic-thumb {
  width: 60px; height: 46px; border-radius: 6px; overflow: hidden; flex-shrink: 0;
}
.topic-thumb img { width: 100%; height: 100%; object-fit: cover; }
.topic-thumb-placeholder {
  width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;
}
.topic-thumb-placeholder span { font-size: 11px; color: #fff; font-weight: 600; text-align: center; padding: 2px; line-height: 1.3; }
.topic-info { flex: 1; min-width: 0; }
.topic-title { font-size: 13px; color: #333; line-height: 1.4; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.topic-date { font-size: 11px; color: #999; margin-top: 4px; }

.topic-link-list { border-top: 1px solid #F5F5F5; padding-top: 8px; display: flex; flex-direction: column; gap: 6px; }
.topic-link { display: flex; align-items: center; gap: 6px; cursor: pointer; }
.topic-link:hover .topic-link-text { color: #409EFF; }
.topic-dot { font-size: 13px; flex-shrink: 0; }
.topic-link-text { font-size: 13px; color: #555; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* 箭头 */
.arrow-down::after { content: ' ▾'; font-size: 10px; }
.arrow-up::after { content: ' ▴'; font-size: 10px; }
</style>


