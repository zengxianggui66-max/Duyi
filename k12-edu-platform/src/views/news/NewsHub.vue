<template>
  <motion.div
    class="news-hub"
    :initial="{ opacity: 0 }"
    :animate="{ opacity: 1 }"
    :transition="{ duration: 0.3 }"
  >
    <section class="news-hero">
      <motion.div
        class="hero-orb hero-orb-1"
        :animate="{ x: [0, 18, 0], y: [0, -10, 0] }"
        :transition="{ duration: 9, repeat: Infinity, ease: 'easeInOut' }"
      />
      <motion.div
        class="container hero-inner"
        :initial="{ opacity: 0, y: 12 }"
        :animate="{ opacity: 1, y: 0 }"
      >
        <div class="hero-main">
          <h1>📰 教育资讯中心</h1>
        <p>政策解读 · 教学改革 · 教研动态 · 名师讲堂 · 升学备考</p>
        <motion.div
          class="hero-search"
          :initial="{ opacity: 0, y: 8 }"
          :animate="{ opacity: 1, y: 0 }"
          :transition="{ delay: 0.08 }"
        >
          <el-input
            v-model="keyword"
            size="large"
            placeholder="搜索政策、中考、名师、升学…"
            clearable
            @keyup.enter="goSearch"
          />
          <el-button type="primary" size="large" @click="goSearch">搜索</el-button>
        </motion.div>
          <div v-if="hotKeywords.length" class="hot-kw">
            <span class="hot-label">热搜：</span>
            <button
              v-for="kw in hotKeywords"
              :key="kw"
              type="button"
              class="kw-chip"
              @click="searchKeyword(kw)"
            >
              {{ kw }}
            </button>
          </div>
        </div>
        <motion.aside
          class="hero-actions"
          :initial="{ opacity: 0, x: 12 }"
          :animate="{ opacity: 1, x: 0 }"
          :transition="{ delay: 0.1 }"
        >
          <el-button class="upload-cta" size="large" @click="handlePublishClick">
            <span class="upload-cta-icon">✍️</span>
            发布资讯
          </el-button>
          <p class="upload-cta-hint">政策、教研、名师、升学 · 支持图文与封面</p>
        </motion.aside>
      </motion.div>
    </section>

    <motion.div
      class="container page-body"
      :initial="{ opacity: 0, y: 12 }"
      :animate="{ opacity: 1, y: 0 }"
      :transition="{ delay: 0.1 }"
    >
      <!-- 头条 -->
      <section v-if="headlines.length" class="headline-section">
        <h2 class="section-title">🔥 今日头条</h2>
        <div class="headline-grid">
          <router-link
            v-for="(item, i) in headlines"
            :key="item.id"
            :to="`/news/${item.id}`"
            class="headline-card card"
            :class="{ featured: i === 0 }"
          >
            <span class="hl-cat">{{ item.categoryName || getCatName(item.category) }}</span>
            <h3>{{ item.title }}</h3>
            <p>{{ item.summary }}</p>
            <span class="hl-meta">{{ formatDate(item) }} · 👁 {{ item.viewCount || 0 }}</span>
          </router-link>
        </div>
      </section>

      <!-- 五频道 -->
      <section
        v-for="ch in NEWS_CHANNELS"
        :key="ch.key"
        class="channel-section"
      >
        <motion.div class="channel-head">
          <motion.div>
            <h2>{{ ch.icon }} {{ ch.name }}</h2>
            <p>{{ ch.desc }}</p>
          </motion.div>
          <router-link :to="`/news/channel/${ch.key}`" class="more-link">更多 &gt;&gt;</router-link>
        </motion.div>
        <motion.div
          class="article-row"
          :initial="{ opacity: 0 }"
          :whileInView="{ opacity: 1 }"
          :viewport="{ once: true }"
        >
          <router-link
            v-for="item in channelArticles(ch.key)"
            :key="item.id"
            :to="`/news/${item.id}`"
            class="article-mini card"
          >
            <motion.div
              class="mini-cover"
              :style="{ background: ch.gradient }"
              whileHover={{ scale: 1.02 }}
            />
            <motion.div class="mini-body" whileHover={{ x: 4 }}>
              <h4>{{ item.title }}</h4>
              <span class="mini-date">{{ formatDate(item) }}</span>
            </motion.div>
          </router-link>
          <p v-if="!channelArticles(ch.key).length" class="empty-hint">暂无内容，敬请期待</p>
        </motion.div>
      </section>
    </motion.div>
  </motion.div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { motion } from 'motion-v'
import { ElMessage } from 'element-plus'
import { useNewsStore, useUserStore } from '@/store'
import {
  NEWS_CHANNELS,
  NEWS_CATEGORY_MAP,
  formatNewsDate,
} from '@/constants/newsZone'
import type { NewsItem } from '@/api/types'

const router = useRouter()
const newsStore = useNewsStore()
const userStore = useUserStore()
const keyword = ref('')

function handlePublishClick() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再发布资讯')
    router.push({ path: '/login', query: { redirect: '/news/publish' } })
    return
  }
  router.push('/news/publish')
}

const headlines = computed(() => newsStore.home?.headlines || [])
const hotKeywords = computed(() => newsStore.home?.hotKeywords || [])

function channelArticles(key: string): NewsItem[] {
  return newsStore.home?.channels?.[key] || []
}

function getCatName(cat?: string) {
  return (cat && NEWS_CATEGORY_MAP[cat]) || '教育资讯'
}

function formatDate(item: NewsItem) {
  return formatNewsDate(item)
}

function goSearch() {
  const q = keyword.value.trim()
  if (!q) return
  router.push({ path: '/news/channel/all', query: { q } })
}

function searchKeyword(kw: string) {
  keyword.value = kw
  goSearch()
}

onMounted(() => newsStore.fetchHome())
</script>

<style scoped>
.news-hub {
  min-height: 100%;
  background: var(--bg-page, #f5f7fa);
}
.news-hero {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0f172a, #1e3a5f, #0f3460);
  color: #fff;
  padding: 48px 0 40px;
}
.hero-inner {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 32px;
}
.hero-main {
  flex: 1;
  min-width: 0;
  text-align: left;
}
.hero-actions {
  flex-shrink: 0;
  text-align: right;
  padding-top: 8px;
}
.upload-cta {
  background: rgba(255, 255, 255, 0.95) !important;
  color: #1d4ed8 !important;
  border: none !important;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.25);
}
.upload-cta:hover {
  background: #fff !important;
  transform: translateY(-1px);
}
.upload-cta-icon {
  margin-right: 6px;
}
.upload-cta-hint {
  font-size: 12px;
  opacity: 0.85;
  margin: 10px 0 0;
  max-width: 200px;
  margin-left: auto;
  line-height: 1.5;
}
.hero-orb {
  position: absolute;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.25), transparent 70%);
  top: -80px;
  right: 10%;
  pointer-events: none;
}
.hero-inner h1 {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px;
}
.hero-inner > p {
  opacity: 0.85;
  margin-bottom: 24px;
}
.hero-search {
  display: flex;
  gap: 12px;
  max-width: 560px;
  margin: 0 0 16px;
}
.hot-kw {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  gap: 8px;
  align-items: center;
}
.hot-label {
  font-size: 13px;
  opacity: 0.7;
}
.kw-chip {
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  cursor: pointer;
}
.kw-chip:hover {
  background: rgba(255, 255, 255, 0.2);
}
.page-body {
  padding: 32px 24px 48px;
}
.section-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 16px;
}
.headline-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr;
  gap: 16px;
  margin-bottom: 40px;
}
.headline-card {
  padding: 20px;
  text-decoration: none;
  color: inherit;
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition: box-shadow 0.2s, transform 0.2s;
}
.headline-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}
.headline-card.featured {
  grid-row: span 2;
}
.hl-cat {
  font-size: 12px;
  color: var(--color-primary);
  font-weight: 600;
}
.headline-card h3 {
  font-size: 16px;
  font-weight: 600;
  line-height: 1.5;
  margin: 0;
}
.headline-card.featured h3 {
  font-size: 20px;
}
.headline-card p {
  font-size: 13px;
  color: var(--text-secondary);
  flex: 1;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.hl-meta {
  font-size: 12px;
  color: var(--text-secondary);
}
.channel-section {
  margin-bottom: 36px;
}
.channel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 16px;
}
.channel-head h2 {
  font-size: 20px;
  margin: 0 0 4px;
}
.channel-head p {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}
.more-link {
  font-size: 14px;
  color: var(--color-primary);
  text-decoration: none;
}
.article-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.article-mini {
  display: flex;
  gap: 12px;
  padding: 12px;
  text-decoration: none;
  color: inherit;
}
.mini-cover {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  flex-shrink: 0;
}
.mini-body h4 {
  font-size: 14px;
  font-weight: 600;
  line-height: 1.5;
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.mini-date {
  font-size: 12px;
  color: var(--text-secondary);
}
.empty-hint {
  grid-column: 1 / -1;
  text-align: center;
  color: var(--text-secondary);
  padding: 24px;
}

@media (max-width: 768px) {
  .hero-inner {
    flex-direction: column;
  }
  .hero-actions {
    text-align: left;
    width: 100%;
    padding-top: 0;
  }
  .upload-cta {
    width: 100%;
  }
  .upload-cta-hint {
    margin-left: 0;
    max-width: none;
  }
}

@media (max-width: 992px) {
  .headline-grid {
    grid-template-columns: 1fr;
  }
  .headline-card.featured {
    grid-row: auto;
  }
  .article-row {
    grid-template-columns: 1fr;
  }
}
</style>
