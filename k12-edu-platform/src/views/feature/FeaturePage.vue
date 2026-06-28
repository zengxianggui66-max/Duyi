<template>
  <motion.div
    class="feature-page"
    :initial="{ opacity: 0 }"
    :animate="{ opacity: 1 }"
    :transition="{ duration: 0.3 }"
  >
    <section class="feature-hero">
      <div class="hero-bg">
        <motion.div
          class="hero-orb hero-orb-1"
          :animate="{ x: [0, 20, 0], y: [0, -12, 0] }"
          :transition="{ duration: 8, repeat: Infinity, ease: 'easeInOut' }"
        />
        <motion.div
          class="hero-orb hero-orb-2"
          :animate="{ x: [0, -16, 0], y: [0, 10, 0] }"
          :transition="{ duration: 10, repeat: Infinity, ease: 'easeInOut' }"
        />
      </div>
      <div class="container hero-content">
        <nav class="breadcrumb">
          <router-link to="/" class="breadcrumb-link">首页</router-link>
          <span class="breadcrumb-sep">/</span>
          <span>特色资源频道</span>
        </nav>
        <motion.h1 :initial="{ opacity: 0, y: 12 }" :animate="{ opacity: 1, y: 0 }">特色资源频道</motion.h1>
        <motion.p
          class="hero-desc"
          :initial="{ opacity: 0, y: 8 }"
          :animate="{ opacity: 1, y: 0 }"
          :transition="{ delay: 0.06 }"
        >
          德育与成长 · 文化素养 · 学业提升 — {{ hubStats.channelCount }} 大精品频道，覆盖班会、生涯、国学、竞赛与区域专题
        </motion.p>
        <motion.div
          class="hero-search"
          :initial="{ opacity: 0, y: 8 }"
          :animate="{ opacity: 1, y: 0 }"
          :transition="{ delay: 0.1 }"
        >
          <el-input
            v-model="hubKeyword"
            size="large"
            placeholder="搜索班会、竞赛、暑假作业、国学…"
            clearable
            @keyup.enter="searchHub"
          />
          <el-button type="primary" size="large" @click="searchHub">搜索</el-button>
        </motion.div>
        <motion.div
          class="hero-stats"
          :initial="{ opacity: 0 }"
          :animate="{ opacity: 1 }"
          :transition="{ delay: 0.14 }"
        >
          <motion.div
            v-for="(stat, i) in heroStats"
            :key="stat.label"
            class="hero-stat"
            :initial="{ opacity: 0, y: 10 }"
            :animate="{ opacity: 1, y: 0 }"
            :transition="{ delay: 0.16 + i * 0.05 }"
          >
            <span class="hero-stat-num">{{ stat.value }}</span>
            <span class="hero-stat-label">{{ stat.label }}</span>
          </motion.div>
        </motion.div>
      </div>
    </section>

    <div class="container page-body">
      <section
        v-for="(group, gi) in channelGroups"
        :key="group.key"
        class="hub-section"
      >
        <motion.div
          class="section-head"
          :initial="{ opacity: 0, x: -8 }"
          :animate="{ opacity: 1, x: 0 }"
          :transition="{ delay: 0.08 * gi }"
        >
          <motion.div class="section-head-left" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }">
            <h2>{{ group.label }}</h2>
            <p>{{ group.desc }}</p>
          </motion.div>
        </motion.div>
        <div class="hub-channel-grid" :class="{ 'hub-channel-grid--solo': group.channels.length === 1 }">
          <motion.div
            v-for="(ch, ci) in group.channels"
            :key="ch.navCommand"
            :initial="{ opacity: 0, y: 16 }"
            :animate="{ opacity: 1, y: 0 }"
            :transition="{ delay: 0.06 * (gi + ci) }"
          >
            <router-link
              :to="ch.path"
              class="hub-channel-card card"
              :style="{ '--ch-color': ch.color, '--ch-gradient': ch.gradient }"
            >
              <div class="hub-card-top">
                <span class="hub-card-icon">{{ ch.icon }}</span>
                <span v-if="ch.tier === 'flagship'" class="hub-flagship">旗舰专页</span>
              </div>
              <h3>{{ ch.name }}</h3>
              <p class="hub-card-desc">{{ ch.shortDesc }}</p>
              <div class="hub-card-tags">
                <span v-for="tag in ch.tags.slice(0, 4)" :key="tag" class="hub-tag">{{ tag }}</span>
              </div>
              <div class="hub-card-foot">
                <span class="hub-count">{{ ch.resourceCount }} 资源</span>
                <span class="hub-enter">进入频道 <el-icon><ArrowRight /></el-icon></span>
              </div>
            </router-link>
          </motion.div>
        </div>
      </section>

      <section class="hub-section">
        <router-link
          :to="{ path: spotlight.path, query: spotlight.query }"
          class="hub-spotlight card"
        >
          <span class="hub-spotlight-emoji">{{ spotlight.emoji }}</span>
          <div class="hub-spotlight-body">
            <span class="hub-spotlight-label">教学日历 · 本月推荐</span>
            <strong>{{ spotlight.title }}</strong>
            <p>{{ spotlight.desc }}</p>
          </div>
          <el-button type="primary" plain>立即查看</el-button>
        </router-link>
      </section>

      <section class="hub-section">
        <div class="section-head">
          <div class="section-head-left">
            <h2>精选专题推荐</h2>
            <p>热门专题合集，覆盖教学全场景</p>
          </div>
          <router-link to="/topic-zone" class="view-more">查看更多 →</router-link>
        </div>
        <div class="topic-grid">
          <router-link
            v-for="topic in topics"
            :key="topic.id"
            :to="`/topic-zone?keyword=${encodeURIComponent(topic.title)}`"
            class="topic-card card"
          >
            <div class="topic-cover" :style="{ background: topic.coverColor }">
              <span class="topic-emoji">{{ topic.emoji }}</span>
            </div>
            <div class="topic-info">
              <h4>{{ topic.title }}</h4>
              <p>{{ topic.desc }}</p>
              <motion.div class="topic-meta" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }">
                <span>⬇ {{ topic.downloads }}</span>
                <span>{{ topic.count }} 个资源</span>
              </motion.div>
            </div>
          </router-link>
        </div>
      </section>

      <section class="hub-section">
        <div class="section-head">
          <div class="section-head-left">
            <h2>频道最新资源</h2>
            <p>来自各特色频道的最新上传</p>
          </div>
        </div>
        <div class="resource-grid">
          <div
            v-for="item in latestResources"
            :key="item.id"
            class="resource-card card"
          >
            <motion.div class="resource-cover" :style="{ background: getCoverColor(item.id) }">
              <span class="cover-icon">{{ getResourceIcon(item.resourceType) }}</span>
              <span v-if="item.isFree" class="free-badge">免费</span>
              <span v-else class="vip-badge">会员</span>
            </motion.div>
            <div class="resource-info">
              <h4 class="resource-title">{{ item.title }}</h4>
              <div class="resource-meta">
                <span class="meta-tag channel-tag">{{ getChannelName(item) }}</span>
                <span class="meta-tag">{{ getTypeName(item.resourceType) }}</span>
              </div>
              <motion.div class="resource-bottom" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }">
                <span class="download-count">⬇ {{ formatCount(item.downloadCount || 0) }}</span>
                <span class="resource-author">{{ item.authorName || '匿名' }}</span>
              </motion.div>
            </div>
          </div>
          <div v-if="!latestResources.length && !loading" class="empty-hint">
            <el-empty description="暂无资源，请先导入特色频道种子数据" :image-size="120" />
          </div>
        </div>
      </section>

      <section class="hub-section hub-intro-section">
        <h2 class="intro-title">频道介绍</h2>
        <p class="intro-sub">了解各特色频道的定位与内容方向</p>
        <div class="intro-grid">
          <motion.div
            v-for="(ch, i) in allChannels"
            :key="ch.navCommand"
            class="intro-card"
            :initial="{ opacity: 0, y: 10 }"
            :animate="{ opacity: 1, y: 0 }"
            :transition="{ delay: 0.04 * i }"
          >
            <span class="intro-icon" :style="{ background: ch.gradient }">{{ ch.icon }}</span>
            <h3>{{ ch.name }}</h3>
            <p>{{ ch.intro }}</p>
            <router-link :to="ch.path" class="intro-link">进入频道 →</router-link>
          </motion.div>
        </div>
      </section>
    </div>
  </motion.div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import { motion } from 'motion-v'
import { useResourceStore } from '@/store'
import { topicApi } from '@/api/topic'
import {
  FEATURE_CHANNELS,
  FEATURE_CHANNEL_SPOTLIGHT,
  FEATURE_HUB_STATS,
  getFeatureChannelGroups,
  matchChannelNameByTags,
} from '@/constants/featureChannelRegistry'

const router = useRouter()
const resourceStore = useResourceStore()
const hubKeyword = ref('')
const hubStats = FEATURE_HUB_STATS
const channelGroups = getFeatureChannelGroups()
const allChannels = FEATURE_CHANNELS
const spotlight = FEATURE_CHANNEL_SPOTLIGHT

const loading = computed(() => resourceStore.loading)

const heroStats = [
  { value: hubStats.totalResources, label: '特色资源' },
  { value: `${hubStats.channelCount} 大`, label: '精品频道' },
  { value: `月更 ${hubStats.monthlyNew}`, label: '持续更新' },
]

onMounted(async () => {
  await Promise.all([
    resourceStore.searchResources({
      keyword: '班会育人,生涯规划,传统文化,学科竞赛,寒暑假,期中期末',
      current: 1,
      size: 8,
    }),
    loadTopics(),
  ])
})

interface HubTopic {
  id: number
  emoji: string
  title: string
  desc: string
  downloads: string
  count: number
  coverColor: string
}

const topics = ref<HubTopic[]>([])

const TOPIC_COVER_COLORS = [
  'linear-gradient(135deg, #f093fb, #f5576c)',
  'linear-gradient(135deg, #4facfe, #00f2fe)',
  'linear-gradient(135deg, #43e97b, #38f9d7)',
  'linear-gradient(135deg, #667eea, #764ba2)',
  'linear-gradient(135deg, #fa709a, #fee140)',
  'linear-gradient(135deg, #a18cd1, #fbc2eb)',
]

async function loadTopics() {
  try {
    const res = await topicApi.listAlbums({
      current: 1,
      size: 6,
      sortField: 'downloadCount',
      sortOrder: 'desc',
    })
    const records = res.data.data?.records || []
    topics.value = records.map((album, i) => ({
      id: album.id,
      emoji: album.icon || '📚',
      title: album.title,
      desc: album.summary || album.category || '精选专题专辑',
      downloads: formatTopicDownloads(album.downloadCount ?? 0),
      count: album.resourceCount ?? 0,
      coverColor: TOPIC_COVER_COLORS[i % TOPIC_COVER_COLORS.length],
    }))
  } catch {
    topics.value = []
  }
}

function formatTopicDownloads(count: number) {
  if (count >= 10000) return `${(count / 10000).toFixed(1)}万`
  return String(count)
}

function searchHub() {
  const q = hubKeyword.value.trim()
  if (!q) return
  router.push({ path: '/topic-zone', query: { keyword: q } })
}
const latestResources = computed(() =>
  (resourceStore.list || []).slice(0, 8).map((r: Record<string, unknown>) => ({
    id: r.id as number,
    title: r.title as string,
    resourceType: r.resourceType as string,
    downloadCount: r.downloadCount as number,
    authorName: r.authorName as string,
    isFree: r.isFree !== false && r.isFree !== 0,
    tags: (r.tags as string) || '',
  })),
)

const coverColors = [
  'linear-gradient(135deg, #667eea, #764ba2)',
  'linear-gradient(135deg, #f093fb, #f5576c)',
  'linear-gradient(135deg, #4facfe, #00f2fe)',
  'linear-gradient(135deg, #43e97b, #38f9d7)',
  'linear-gradient(135deg, #fa709a, #fee140)',
  'linear-gradient(135deg, #a18cd1, #fbc2eb)',
  'linear-gradient(135deg, #ffecd2, #fcb69f)',
  'linear-gradient(135deg, #89f7fe, #66a6ff)',
]

function getCoverColor(id: number) {
  return coverColors[(id - 1) % coverColors.length]
}

function getResourceIcon(type?: string) {
  const map: Record<string, string> = {
    courseware: '📊',
    lessonplan: '📋',
    lesson_plan: '📋',
    study_guide: '📑',
    exam: '📝',
    video: '🎬',
    material: '📂',
  }
  return map[type || ''] || '📄'
}

function getTypeName(type?: string) {
  const map: Record<string, string> = {
    courseware: '课件',
    lesson_plan: '教案',
    lessonplan: '教案',
    study_guide: '导学案',
    exam: '试卷',
    video: '视频',
    material: '素材',
  }
  return map[type || ''] || type || '资源'
}

function getChannelName(item: { tags?: string }) {
  return matchChannelNameByTags(item.tags || '')
}

function formatCount(count: number | string): string {
  const num = typeof count === 'string' ? parseInt(count, 10) : count
  if (!num) return '0'
  if (num >= 10000) return `${(num / 10000).toFixed(1)}万`
  return String(num)
}
</script>

<style scoped>
.feature-page {
  min-height: 100vh;
  background: var(--bg-page, #f5f7fa);
}

.feature-hero {
  position: relative;
  padding: 48px 0 56px;
  color: #fff;
  overflow: hidden;
  background: linear-gradient(135deg, #1e3a8a 0%, #4361ee 45%, #7c3aed 100%);
}

.hero-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.hero-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.35;
}

.hero-orb-1 {
  width: 320px;
  height: 320px;
  background: #60a5fa;
  top: -80px;
  right: 10%;
}

.hero-orb-2 {
  width: 260px;
  height: 260px;
  background: #c4b5fd;
  bottom: -60px;
  left: 5%;
}

.hero-content {
  position: relative;
  z-index: 1;
}

.breadcrumb {
  font-size: 13px;
  margin-bottom: 12px;
  opacity: 0.9;
}

.breadcrumb-link {
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
}

.breadcrumb-link:hover {
  color: #fff;
}

.breadcrumb-sep {
  margin: 0 8px;
  opacity: 0.6;
}

.feature-hero h1 {
  font-size: 38px;
  font-weight: 800;
  margin: 0 0 12px;
  letter-spacing: -0.02em;
}

.hero-desc {
  font-size: 16px;
  line-height: 1.7;
  opacity: 0.92;
  max-width: 720px;
  margin: 0 0 24px;
}

.hero-search {
  display: flex;
  gap: 10px;
  max-width: 520px;
  margin-bottom: 32px;
}

.hero-search :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.hero-stats {
  display: flex;
  gap: 40px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.hero-stat-num {
  display: block;
  font-size: 28px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}

.hero-stat-label {
  font-size: 13px;
  opacity: 0.85;
}

.page-body {
  padding-bottom: 48px;
}

.hub-section {
  padding: 36px 0 8px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
  gap: 16px;
}

.section-head-left h2 {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 6px;
  color: var(--text-primary);
}

.section-head-left p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.hub-channel-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.hub-channel-grid--solo {
  grid-template-columns: 1fr;
  max-width: 50%;
}

.hub-channel-card {
  display: block;
  padding: 24px 26px;
  text-decoration: none;
  color: var(--text-primary);
  border-left: 4px solid var(--ch-color);
  transition: transform 0.25s, box-shadow 0.25s;
  overflow: hidden;
  position: relative;
}

.hub-channel-card::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 120px;
  height: 120px;
  background: var(--ch-gradient);
  opacity: 0.08;
  border-radius: 0 0 0 100%;
}

.hub-channel-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 40px rgba(67, 97, 238, 0.12);
}

.hub-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.hub-card-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: var(--ch-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.hub-flagship {
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 20px;
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  color: #b45309;
  font-weight: 600;
}

.hub-channel-card h3 {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 8px;
}

.hub-card-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0 0 14px;
}

.hub-card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
}

.hub-tag {
  padding: 3px 10px;
  font-size: 11px;
  border-radius: 20px;
  background: var(--color-primary-bg, #ebf0ff);
  color: var(--color-primary, #4361ee);
}

.hub-card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 14px;
  border-top: 1px solid var(--border-light);
}

.hub-count {
  font-size: 14px;
  font-weight: 700;
  color: var(--ch-color);
}

.hub-enter {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  color: var(--ch-color);
}

.hub-spotlight {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 22px 28px;
  text-decoration: none;
  color: inherit;
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 50%, #fff 100%);
  border: 1px solid #ddd6fe;
  transition: box-shadow 0.25s;
}

.hub-spotlight:hover {
  box-shadow: 0 12px 32px rgba(124, 58, 237, 0.15);
}

.hub-spotlight-emoji {
  font-size: 48px;
  flex-shrink: 0;
}

.hub-spotlight-body {
  flex: 1;
  min-width: 0;
}

.hub-spotlight-label {
  display: block;
  font-size: 12px;
  color: #7c3aed;
  font-weight: 600;
  margin-bottom: 4px;
}

.hub-spotlight-body strong {
  display: block;
  font-size: 18px;
  color: #5b21b6;
  margin-bottom: 4px;
}

.hub-spotlight-body p {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

.view-more {
  color: var(--color-primary);
  font-size: 14px;
  white-space: nowrap;
  text-decoration: none;
  font-weight: 600;
}

.topic-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.topic-card {
  border-radius: var(--radius-md);
  overflow: hidden;
  text-decoration: none;
  color: var(--text-primary);
  transition: transform 0.3s, box-shadow 0.3s;
}

.topic-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.topic-cover {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.topic-emoji {
  font-size: 52px;
}

.topic-info {
  padding: 16px;
}

.topic-info h4 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 6px;
}

.topic-info p {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 10px;
  line-height: 1.5;
}

.topic-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-secondary);
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.resource-card {
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: transform 0.25s, box-shadow 0.25s;
}

.resource-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.resource-cover {
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.cover-icon {
  font-size: 44px;
  color: rgba(255, 255, 255, 0.9);
}

.free-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #10b981;
  color: #fff;
  padding: 2px 10px;
  border-radius: var(--radius-round);
  font-size: 11px;
  font-weight: 600;
}

.vip-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: linear-gradient(135deg, #f59e0b, #ef4444);
  color: #fff;
  padding: 2px 10px;
  border-radius: var(--radius-round);
  font-size: 11px;
  font-weight: 600;
}

.resource-info {
  padding: 14px;
}

.resource-title {
  font-size: 14px;
  font-weight: 600;
  line-height: 1.5;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.resource-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.meta-tag {
  padding: 2px 8px;
  background: var(--color-primary-bg);
  color: var(--color-primary);
  border-radius: var(--radius-round);
  font-size: 11px;
}

.channel-tag {
  background: #f0f0ff;
  color: #7c3aed;
}

.resource-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--text-secondary);
}

.empty-hint {
  grid-column: 1 / -1;
  padding: 40px 0;
}

.hub-intro-section {
  padding-bottom: 32px;
}

.intro-title {
  text-align: center;
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 8px;
}

.intro-sub {
  text-align: center;
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 28px;
}

.intro-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.intro-card {
  padding: 24px 20px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  background: #fff;
  transition: border-color 0.25s, box-shadow 0.25s;
}

.intro-card:hover {
  border-color: var(--color-primary);
  box-shadow: 0 8px 24px rgba(67, 97, 238, 0.08);
}

.intro-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-bottom: 12px;
}

.intro-card h3 {
  font-size: 16px;
  font-weight: 700;
  margin: 0 0 8px;
}

.intro-card p {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 0 0 12px;
}

.intro-link {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary);
  text-decoration: none;
}

@media (max-width: 1100px) {
  .hub-channel-grid--solo {
    max-width: none;
  }
  .intro-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .resource-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .feature-hero h1 {
    font-size: 28px;
  }
  .hero-stats {
    flex-wrap: wrap;
    gap: 24px;
  }
  .hub-channel-grid {
    grid-template-columns: 1fr;
  }
  .topic-grid {
    grid-template-columns: 1fr;
  }
  .hub-spotlight {
    flex-direction: column;
    align-items: flex-start;
  }
  .intro-grid {
    grid-template-columns: 1fr;
  }
}
</style>
