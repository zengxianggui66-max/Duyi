<template>
  <div class="news-page">
    <section class="news-banner">
      <div class="container">
        <h1>📰 教育资讯</h1>
        <p>教育政策、教学改革、教研动态、升学备考一手掌握</p>
      </div>
    </section>

    <div class="container" style="padding: 24px;">
      <!-- 分类标签 -->
      <div class="category-bar">
        <span
          v-for="cat in NEWS_LIST_CATEGORIES"
          :key="cat.key"
          class="cat-tag"
          :class="{ active: activeCat === cat.key }"
          @click="activeCat = cat.key"
        >{{ cat.icon }} {{ cat.name }}</span>
      </div>

      <!-- 资讯列表 -->
      <div class="news-list">
        <router-link v-for="item in newsList" :key="item.id" :to="`/news/${item.id}`" class="news-item card">
          <div class="news-cover" :style="item.coverUrl ? undefined : { background: item.coverColor }">
            <img v-if="item.coverUrl" :src="item.coverUrl" alt="" class="news-cover-img" />
            <span class="news-cat-badge">{{ item.categoryName }}</span>
            <span class="news-date-badge">{{ item.date }}</span>
          </div>
          <div class="news-body">
            <h3>{{ item.title }}</h3>
            <p>{{ item.summary }}</p>
            <div class="news-footer">
              <span>👁 {{ item.views }} 阅读</span>
              <span>💬 {{ item.comments }} 评论</span>
              <span>{{ item.author }}</span>
            </div>
          </div>
        </router-link>
      </div>

      <div class="pagination-wrap">
        <el-pagination background layout="prev, pager, next" :total="newsStore.total" :page-size="10" :current-page="currentPage" @current-change="handlePageChange" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useNewsStore } from '@/store'
import {
  NEWS_LIST_CATEGORIES,
  coverGradient,
  formatNewsDate,
  getCategoryName,
} from '@/constants/newsZone'

const newsStore = useNewsStore()
const activeCat = ref('all')
const currentPage = ref(1)

async function loadNews() {
  await newsStore.fetchList({
    category: activeCat.value === 'all' ? undefined : activeCat.value,
    current: currentPage.value,
    size: 10,
  })
}

onMounted(() => loadNews())
watch(activeCat, () => { currentPage.value = 1; loadNews() })

const newsList = computed(() => {
  return (newsStore.list || []).map((n: any) => ({
    id: n.id,
    title: n.title,
    summary: n.summary || n.content?.substring(0, 80) || '',
    categoryName: getCategoryName(n.category),
    date: formatNewsDate(n),
    views: formatCount(n.viewCount || 0),
    comments: 0,
    author: n.author || '编辑部',
    coverUrl: n.coverUrl || '',
    coverColor: coverGradient(n.id),
  }))
})

function formatCount(count: number | string): string {
  const num = typeof count === 'string' ? parseInt(count) : count
  if (!num) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  return String(num)
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadNews()
}
</script>

<style scoped>
.news-banner {
  background: linear-gradient(135deg, #1A1A2E, #16213E, #0F3460);
  color: #fff;
  padding: 40px 0;
  text-align: center;
}
.news-banner h1 { font-size: 32px; font-weight: 700; margin-bottom: 8px; }
.news-banner p { opacity: 0.8; }

.category-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}
.cat-tag {
  padding: 8px 20px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-round);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-regular);
}
.cat-tag:hover { border-color: var(--color-primary); color: var(--color-primary); }
.cat-tag.active { background: var(--color-primary); border-color: var(--color-primary); color: #fff; }

.news-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.news-item {
  display: flex;
  border-radius: var(--radius-md);
  overflow: hidden;
  text-decoration: none;
  color: var(--text-primary);
}
.news-cover {
  width: 220px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 16px;
  position: relative;
  overflow: hidden;
}
.news-cover-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.news-cat-badge {
  align-self: flex-start;
  background: rgba(255,255,255,0.9);
  color: var(--color-primary);
  padding: 4px 12px;
  border-radius: var(--radius-round);
  font-size: 12px;
  font-weight: 600;
  position: relative;
  z-index: 1;
}
.news-date-badge {
  color: rgba(255,255,255,0.8);
  font-size: 12px;
  position: relative;
  z-index: 1;
  text-shadow: 0 1px 3px rgba(0,0,0,0.4);
}
.news-body {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}
.news-body h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  line-height: 1.5;
}
.news-body p {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.news-footer {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 12px;
}

.pagination-wrap { display: flex; justify-content: center; margin-top: 32px; }

@media (max-width: 768px) {
  .news-item { flex-direction: column; }
  .news-cover { width: 100%; height: 120px; }
}
</style>
