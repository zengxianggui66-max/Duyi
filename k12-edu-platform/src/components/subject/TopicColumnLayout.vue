<template>
  <div class="topic-layout">
    <!-- 调试标记 -->
    <div style="background:purple;color:white;padding:8px;font-size:14px;margin-bottom:12px;border-radius:6px;">
      [DEBUG] TopicColumnLayout 已渲染 | activeColumn={{ activeColumn }} | resources={{ resources.length }} 条
    </div>
    <!-- 左侧主内容区 -->
    <div class="topic-main">
      <!-- 年份 + 版本筛选行（部分栏目显示） -->
      <div class="multi-filter-card" v-if="showExtraFilters">
        <div class="filter-row">
          <span class="filter-label">年份：</span>
          <div class="filter-tags">
            <span
              v-for="y in yearList"
              :key="y"
              :class="['tag', { active: selectedYear === y }]"
              @click="selectedYear = y"
            >{{ y }}</span>
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
        <div class="filter-row extra-row" v-if="showMoreVersions">
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
      </div>

      <!-- 类型 + 模式切换操作栏 -->
      <div class="action-bar">
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

      <!-- 卡片网格（成套模式 or 单份模式统一卡片样式） -->
      <div v-else class="resource-grid">
        <div
          v-for="item in resources"
          :key="item.id"
          class="resource-card"
          @click="$emit('open-resource', item)"
        >
          <!-- 卡片封面 -->
          <div class="card-cover" :style="{ background: getCoverBg(item) }">
            <div class="cover-subject-tag">{{ item.subject || '语文' }}</div>
            <div class="cover-column-tag" v-if="item.module">{{ item.module }}</div>
            <div class="cover-type-tag">{{ item.type || '资料' }}</div>
            <div class="cover-title-preview">{{ item.title?.substring(0, 12) }}</div>
            <span v-if="item.fileSizeKb && item.fileSizeKb > 500" class="cover-premium-badge">精</span>
          </div>

          <!-- 卡片信息 -->
          <div class="card-body">
            <div class="card-title">{{ item.title }}</div>
            <div class="card-meta-row">
              <span class="meta-item">数量：{{ item.fileCount || 1 }}份</span>
              <span class="meta-sep">|</span>
              <span class="meta-item">状态：{{ item.status || '已完结' }}</span>
            </div>
            <div class="card-meta-row">
              <span class="meta-item meta-muted">浏览量：{{ item.viewCount || 0 }}</span>
              <span class="meta-sep">|</span>
              <span class="meta-item meta-muted">下载量：{{ item.downloadCount || 0 }}</span>
            </div>
            <div class="card-meta-row">
              <span class="meta-item meta-muted">更新：{{ formatDate(item.uploadTime) }}</span>
              <span class="meta-author">{{ item.uploader || '教学资源' }}</span>
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
    <div class="topic-sidebar">
      <!-- 精选专题卡片列表 -->
      <div class="sidebar-card">
        <div class="sidebar-title-row">
          <span class="sidebar-title orange">精选专题</span>
          <span class="sidebar-more" @click="$emit('view-more-topics')">更多 &gt;</span>
        </div>
        <div class="topic-card-list">
          <div
            v-for="(topic, idx) in (selectedTopics || defaultTopics)"
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
        </div>

        <!-- 小列表专题链接 -->
        <div class="topic-link-list" v-if="topicLinks && topicLinks.length">
          <div
            v-for="(link, idx) in topicLinks"
            :key="idx"
            class="topic-link"
            @click="$emit('open-resource', link)"
          >
            <span class="topic-dot">{{ idx < 3 ? '🔥' : '·' }}</span>
            <span class="topic-link-text">{{ link.title }}</span>
          </div>
        </div>
        <div class="topic-link-list" v-else>
          <div
            v-for="(link, idx) in defaultTopicLinks"
            :key="idx"
            class="topic-link"
          >
            <span class="topic-dot">{{ idx < 3 ? '🔥' : '·' }}</span>
            <span class="topic-link-text">{{ link }}</span>
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
  selectedVersionName?: string
  selectedTopics?: any[]
  topicLinks?: any[]
}>()

const emit = defineEmits<{
  'update:currentPage': [value: number]
  'update:resourceMode': [value: 'single' | 'suite']
  'update:sortType': [value: string]
  'open-resource': [item: any]
  'view-more': []
  'view-more-topics': []
}>()

// 是否显示年份/版本筛选（专题复习、真题汇编显示）
const showExtraFilters = computed(() =>
  ['专题复习', '真题汇编'].includes(props.activeColumn)
)

// 年份
const currentYear = new Date().getFullYear()
const yearList = ['全部', ...Array.from({ length: 5 }, (_, i) => String(currentYear - i))]
const selectedYear = ref('全部')

// 版本
const versionList = ['全部', '通用', '统编版（2024）']
const moreVersions = ['统编版（五四制）（2024）', '人教版（2001）', '北师大版']
const selectedVersion = ref('全部')
const showMoreVersions = ref(false)

// 过滤
const filterPremium = ref(false)

// 封面颜色映射（按类型）
const coverBgMap: Record<string, string> = {
  '课件': 'linear-gradient(135deg, #667EEA, #764BA2)',
  '试卷': 'linear-gradient(135deg, #F7971E, #FFD200)',
  '教案': 'linear-gradient(135deg, #11998E, #38EF7D)',
  '练习': 'linear-gradient(135deg, #FF416C, #FF4B2B)',
  '视频': 'linear-gradient(135deg, #4776E6, #8E54E9)',
  '音频/朗读': 'linear-gradient(135deg, #0099F7, #F11712)',
  '学案': 'linear-gradient(135deg, #f093fb, #f5576c)',
  '知识点': 'linear-gradient(135deg, #4facfe, #00f2fe)',
}
const topicColors = [
  'linear-gradient(135deg, #4CAF50, #8BC34A)',
  'linear-gradient(135deg, #FF9800, #FFC107)',
  'linear-gradient(135deg, #2196F3, #03A9F4)',
  'linear-gradient(135deg, #9C27B0, #E91E63)',
  'linear-gradient(135deg, #F44336, #FF5722)',
]

function getCoverBg(item: any): string {
  const t = item.type || ''
  return coverBgMap[t] || 'linear-gradient(135deg, #667EEA, #764BA2)'
}

// 默认侧边栏数据（无真实数据时展示）
const defaultTopics = [
  { title: '2026年小学春季下学期各科单元复习合辑', uploadTime: '2026-05-11' },
  { title: '2026小升初各科复习冲刺精选资料', uploadTime: '2026-05-11' },
  { title: '【小学】主题班会', uploadTime: '2026-05-11' },
]
const defaultTopicLinks = [
  '2026春 小学 1-6年级 开学摸底考',
  '2025-2026学期 小学全科 期末复习资…',
  '2025年秋小学各科期中模拟测试卷（…',
  '【新教材】2025-2026学年秋季学…',
  '【新教材】2025-2026学年小学秋…',
]

function formatDate(dt: any): string {
  if (!dt) return ''
  if (typeof dt === 'string') return dt.replace('T', ' ').substring(0, 10)
  try { return new Date(dt).toLocaleDateString('zh-CN') } catch { return String(dt) }
}
</script>

<style scoped>
.topic-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.topic-main {
  flex: 1;
  min-width: 0;
}

/* 多维度筛选卡片 */
.multi-filter-card {
  background: #fff;
  border-radius: 10px;
  padding: 14px 20px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
  margin-bottom: 10px;
}
.filter-row {
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 6px 0;
  padding: 6px 0;
  border-bottom: 1px solid #F5F5F5;
}
.filter-row:last-child { border-bottom: none; }
.extra-row { padding-top: 0; }
.filter-label {
  font-size: 13px; color: #666; font-weight: 600;
  white-space: nowrap; min-width: 42px; padding-top: 4px;
}
.ml-24 { margin-left: 24px; }
.filter-tags { display: flex; flex-wrap: wrap; gap: 6px; flex: 1; }
.tag {
  padding: 3px 12px; border-radius: 14px; font-size: 13px; color: #555;
  cursor: pointer; background: #F5F7FA; transition: 0.2s; white-space: nowrap; user-select: none;
}
.tag:hover { background: #E8ECF1; color: #333; }
.tag.active { background: #409EFF; color: #fff; box-shadow: 0 2px 6px rgba(64,158,255,0.3); }
.more-tag {
  padding: 3px 12px; border-radius: 14px; font-size: 13px; color: #409EFF;
  cursor: pointer; background: #EBF5FF; white-space: nowrap;
}

/* 操作栏 */
.action-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
  margin-bottom: 14px;
  flex-wrap: wrap;
}
.mode-btns { display: flex; gap: 6px; }
.mode-btn {
  padding: 5px 14px; border: 1px solid #E0D9D0; border-radius: 16px;
  background: #fff; font-size: 13px; color: #5D4E37; cursor: pointer; transition: 0.2s;
}
.mode-btn.active { background: #409EFF; color: #fff; border-color: #409EFF; }
.sort-btns { display: flex; gap: 4px; }
.sort-item {
  padding: 4px 10px; font-size: 14px; color: #666; cursor: pointer;
  border-radius: 4px; transition: 0.2s;
}
.sort-item.active { color: #409EFF; font-weight: 600; }
.filter-checkboxes { display: flex; align-items: center; gap: 8px; }
.check-label { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #555; cursor: pointer; }
.result-count { margin-left: auto; font-size: 13px; color: #999; white-space: nowrap; }

/* 加载/空状态 */
.loading-state, .empty-state {
  display: flex; align-items: center; justify-content: center;
  padding: 60px; color: #909399; font-size: 14px;
  background: #fff; border-radius: 10px;
}
.loading-spinner {
  display: inline-block; width: 16px; height: 16px;
  border: 2px solid #409EFF; border-top-color: transparent;
  border-radius: 50%; animation: spin 0.8s linear infinite; margin-right: 8px;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 卡片网格 */
.resource-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.resource-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  gap: 0;
}
.resource-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.12);
}

/* 卡片封面（左侧缩略图） */
.card-cover {
  width: 100px;
  min-height: 110px;
  flex-shrink: 0;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-end;
  padding: 8px;
  gap: 3px;
}
.cover-subject-tag {
  position: absolute; top: 6px; left: 6px;
  background: rgba(0,0,0,0.35); color: #fff;
  font-size: 11px; padding: 1px 6px; border-radius: 3px;
}
.cover-column-tag {
  position: absolute; top: 6px; right: 6px;
  background: rgba(255,255,255,0.25); color: #fff;
  font-size: 10px; padding: 1px 5px; border-radius: 3px;
}
.cover-type-tag {
  font-size: 14px; font-weight: 700; color: #FFD700;
  text-shadow: 0 1px 3px rgba(0,0,0,0.3);
}
.cover-title-preview {
  font-size: 11px; color: rgba(255,255,255,0.85); line-height: 1.3;
}
.cover-premium-badge {
  position: absolute; top: 28px; left: 6px;
  background: #F56C6C; color: #fff;
  font-size: 10px; padding: 1px 5px; border-radius: 3px; font-weight: 700;
}

/* 卡片信息 */
.card-body {
  flex: 1; padding: 10px 12px; display: flex; flex-direction: column; gap: 4px;
  min-width: 0;
}
.card-title {
  font-size: 14px; font-weight: 500; color: #333;
  overflow: hidden; text-overflow: ellipsis;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  line-height: 1.4; margin-bottom: 2px;
}
.card-meta-row {
  display: flex; align-items: center; flex-wrap: wrap; gap: 4px;
  font-size: 12px; color: #555;
}
.meta-muted { color: #999; }
.meta-sep { color: #DDD; }
.meta-author { margin-left: auto; font-size: 11px; color: #999; }

/* 分页 */
.pagination { display: flex; justify-content: center; gap: 6px; margin-top: 16px; }
.page-btn {
  padding: 6px 12px; border: 1px solid #DCDFE6; background: #fff;
  border-radius: 4px; cursor: pointer; transition: 0.2s; font-size: 13px;
}
.page-btn:hover:not(:disabled) { color: #409EFF; border-color: #409EFF; }
.page-btn.active { background: #409EFF; color: #fff; border-color: #409EFF; }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* 右侧侧边栏 */
.topic-sidebar { width: 260px; flex-shrink: 0; }
.sidebar-card {
  background: #fff; border-radius: 10px;
  padding: 16px; box-shadow: 0 1px 6px rgba(0,0,0,0.05);
}
.sidebar-title-row {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px;
}
.sidebar-title { font-size: 15px; font-weight: 700; }
.sidebar-title.orange { color: #E8720C; }
.sidebar-more { font-size: 13px; color: #409EFF; cursor: pointer; }

/* 精选专题卡片 */
.topic-card-list { display: flex; flex-direction: column; gap: 12px; margin-bottom: 12px; }
.topic-card { display: flex; gap: 10px; cursor: pointer; }
.topic-card:hover .topic-title { color: #409EFF; }
.topic-thumb {
  width: 64px; height: 48px; border-radius: 6px; overflow: hidden; flex-shrink: 0;
}
.topic-thumb img { width: 100%; height: 100%; object-fit: cover; }
.topic-thumb-placeholder {
  width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;
}
.topic-thumb-placeholder span {
  font-size: 11px; color: #fff; font-weight: 600; text-align: center;
  padding: 2px; line-height: 1.3;
}
.topic-info { flex: 1; min-width: 0; }
.topic-title {
  font-size: 13px; color: #333; line-height: 1.4;
  overflow: hidden; text-overflow: ellipsis;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
}
.topic-date { font-size: 11px; color: #999; margin-top: 4px; }

.topic-link-list {
  border-top: 1px solid #F5F5F5; padding-top: 8px;
  display: flex; flex-direction: column; gap: 7px;
}
.topic-link { display: flex; align-items: center; gap: 6px; cursor: pointer; }
.topic-link:hover .topic-link-text { color: #409EFF; }
.topic-dot { font-size: 13px; flex-shrink: 0; }
.topic-link-text {
  font-size: 13px; color: #555;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

/* 箭头 */
.arrow-down::after { content: ' ▾'; font-size: 10px; }
.arrow-up::after { content: ' ▴'; font-size: 10px; }
</style>
