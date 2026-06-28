<template>
  <div class="news-channel-page">
    <section class="channel-banner" :style="{ background: bannerGradient }">
      <div class="container">
        <nav class="breadcrumb">
          <router-link to="/news">教育资讯</router-link>
          <span>/</span>
          <span>{{ channelTitle }}</span>
        </nav>
        <h1>{{ channelIcon }} {{ channelTitle }}</h1>
        <p v-if="channelDef">{{ channelDef.desc }}</p>
      </div>
    </section>

    <div class="container filters-wrap">
      <motion.div class="filter-row">
        <el-select v-model="gradeLevel" placeholder="学段" style="width: 120px" @change="reload">
          <el-option
            v-for="g in GRADE_FILTER_OPTIONS"
            :key="g.key"
            :label="g.label"
            :value="g.key"
          />
        </el-select>
        <el-select v-model="region" placeholder="地区" style="width: 120px" @change="reload">
          <el-option
            v-for="r in REGION_FILTER_OPTIONS"
            :key="r.key"
            :label="r.label"
            :value="r.key"
          />
        </el-select>
        <el-select v-model="sort" style="width: 120px" @change="reload">
          <el-option label="最新发布" value="publishTime" />
          <el-option label="最多阅读" value="hot" />
        </el-select>
        <el-input
          v-model="searchKw"
          placeholder="关键词搜索"
          clearable
          style="flex: 1; max-width: 320px"
          @keyup.enter="reload"
        />
        <el-button type="primary" @click="reload">筛选</el-button>
      </motion.div>

      <div v-loading="newsStore.loading" class="news-list">
        <router-link
          v-for="item in newsList"
          :key="item.id"
          :to="`/news/${item.id}`"
          class="news-item card"
        >
          <div class="news-cover" :style="{ background: item.coverColor }">
            <span class="news-cat-badge">{{ item.categoryName }}</span>
            <span class="news-date-badge">{{ item.date }}</span>
          </div>
          <div class="news-body">
            <h3>{{ item.title }}</h3>
            <p>{{ item.summary }}</p>
            <motion.div class="news-footer" whileHover={{ opacity: 0.85 }}>
              <span>👁 {{ item.views }} 阅读</span>
              <span>{{ item.author }}</span>
            </motion.div>
          </div>
        </router-link>
        <el-empty v-if="!newsStore.loading && !newsList.length" description="暂无相关资讯" />
      </div>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="newsStore.total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { motion } from 'motion-v'
import { newsApi } from '@/api'
import { useNewsStore } from '@/store'
import {
  getChannelByKey,
  NEWS_CATEGORY_MAP,
  GRADE_FILTER_OPTIONS,
  REGION_FILTER_OPTIONS,
  formatNewsDate,
  coverGradient,
} from '@/constants/newsZone'

const route = useRoute()
const newsStore = useNewsStore()

const gradeLevel = ref('all')
const region = ref('all')
const sort = ref<'publishTime' | 'hot'>('publishTime')
const searchKw = ref('')
const currentPage = ref(1)
const pageSize = 10

const channelCode = computed(() => String(route.params.code || 'all'))
const channelDef = computed(() => getChannelByKey(channelCode.value))
const channelTitle = computed(() => {
  if (channelCode.value === 'all') return searchKw.value ? `搜索：${searchKw.value}` : '全部资讯'
  return channelDef.value?.name || NEWS_CATEGORY_MAP[channelCode.value] || '资讯频道'
})
const channelIcon = computed(() => channelDef.value?.icon || '📰')
const bannerGradient = computed(
  () => channelDef.value?.gradient || 'linear-gradient(135deg, #1A1A2E, #16213E)'
)

const newsList = computed(() =>
  (newsStore.list || []).map((n) => ({
    id: n.id,
    title: n.title,
    summary: n.summary || '',
    categoryName: n.categoryName || NEWS_CATEGORY_MAP[n.category || ''] || '资讯',
    date: formatNewsDate(n),
    views: formatCount(n.viewCount || 0),
    author: n.author || '编辑部',
    coverColor: coverGradient(n.id),
  }))
)

function formatCount(count: number): string {
  if (count >= 10000) return (count / 10000).toFixed(1) + '万'
  return String(count)
}

async function loadList() {
  const q = (route.query.q as string) || searchKw.value.trim()
  if (q) searchKw.value = q

  if (q) {
    await fetchSearch(q)
    return
  }

  await newsStore.fetchList({
    category: channelCode.value === 'all' ? undefined : channelCode.value,
    gradeLevel: gradeLevel.value === 'all' ? undefined : gradeLevel.value,
    region: region.value === 'all' ? undefined : region.value,
    sort: sort.value,
    current: currentPage.value,
    size: pageSize,
  })
}

async function fetchSearch(keyword: string) {
  newsStore.loading = true
  try {
    const res = await newsApi.search(keyword, currentPage.value, pageSize)
    const data = res.data.data
    newsStore.$patch({
      list: data?.records || [],
      total: data?.total || 0,
    })
  } finally {
    newsStore.loading = false
  }
}

function reload() {
  currentPage.value = 1
  loadList()
}

function onPageChange(p: number) {
  currentPage.value = p
  loadList()
}

watch(
  () => [route.params.code, route.query.q],
  () => {
    currentPage.value = 1
    if (route.query.q) searchKw.value = String(route.query.q)
    loadList()
  }
)

onMounted(() => {
  if (route.query.q) searchKw.value = String(route.query.q)
  loadList()
})
</script>

<style scoped>
.news-channel-page {
  min-height: 100%;
  background: var(--bg-page, #f5f7fa);
}
.channel-banner {
  color: #fff;
  padding: 32px 0;
}
.breadcrumb {
  font-size: 13px;
  margin-bottom: 12px;
  opacity: 0.85;
}
.breadcrumb a {
  color: #fff;
  text-decoration: none;
}
.channel-banner h1 {
  font-size: 28px;
  margin: 0 0 8px;
}
.filters-wrap {
  padding: 24px;
}
.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
  align-items: center;
}
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
}
.news-cat-badge {
  align-self: flex-start;
  background: rgba(255, 255, 255, 0.9);
  color: var(--color-primary);
  padding: 4px 12px;
  border-radius: var(--radius-round);
  font-size: 12px;
  font-weight: 600;
}
.news-date-badge {
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
}
.news-body {
  padding: 20px;
  flex: 1;
}
.news-body h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}
.news-body p {
  font-size: 14px;
  color: var(--text-secondary);
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
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 768px) {
  .news-item {
    flex-direction: column;
  }
  .news-cover {
    width: 100%;
    height: 120px;
  }
}
</style>
