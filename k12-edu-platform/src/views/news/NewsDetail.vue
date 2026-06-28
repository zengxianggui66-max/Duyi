<template>
  <div class="news-detail-page">
    <div class="container detail-layout">
      <router-link to="/news" class="back-link">← 返回资讯中心</router-link>

      <motion.div v-if="loading" class="loading-wrap">加载中…</motion.div>
      <template v-else-if="article">
        <motion.div
          class="main-col"
          :initial="{ opacity: 0, y: 12 }"
          :animate="{ opacity: 1, y: 0 }"
        >
          <PolicyPointsCard :raw="article.policyPoints" />

          <article class="article card">
            <div class="article-actions">
              <el-button
                v-if="collected"
                type="warning"
                plain
                size="small"
                :loading="collectLoading"
                @click="toggleCollect"
              >
                ★ 已收藏
              </el-button>
              <el-button
                v-else
                plain
                size="small"
                :loading="collectLoading"
                @click="toggleCollect"
              >
                ☆ 收藏
              </el-button>
            </div>
            <h1>{{ article.title }}</h1>
            <div class="article-meta">
              <el-tag size="small" effect="plain">{{ categoryLabel }}</el-tag>
              <span>{{ displayDate }}</span>
              <span v-if="article.sourceName">来源：{{ article.sourceName }}</span>
              <span>👁 {{ article.viewCount || 0 }} 阅读</span>
            </div>
            <div v-if="article.coverUrl" class="cover-wrap">
              <img :src="article.coverUrl" :alt="article.title" />
            </div>
            <motion.div class="article-content" v-html="article.content" />
            <div v-if="tags.length" class="article-tags">
              <el-tag v-for="tag in tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
            </div>
          </article>

          <section v-if="relatedArticles.length" class="related-section card">
            <h3>相关阅读</h3>
            <router-link
              v-for="rel in relatedArticles"
              :key="rel.id"
              :to="`/news/${rel.id}`"
              class="related-item"
            >
              <span class="rel-title">{{ rel.title }}</span>
              <span class="rel-meta">{{ formatNewsDate(rel) }}</span>
            </router-link>
          </section>

          <section v-if="relatedLinks.length" class="links-section card">
            <h3>延伸探索</h3>
            <router-link
              v-for="link in relatedLinks"
              :key="link.path"
              :to="link.path"
              class="link-chip"
            >
              {{ link.icon || '🔗' }} {{ link.title }}
            </router-link>
          </section>
        </motion.div>

        <aside v-if="showConsult" class="side-col">
          <ConsultSidebar :article-id="article.id" />
        </aside>
      </template>
      <el-empty v-else description="文章不存在或已下架" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { motion } from 'motion-v'
import { ElMessage } from 'element-plus'
import { newsApi } from '@/api'
import { useUserStore } from '@/store'
import { NEWS_CATEGORY_MAP, formatNewsDate } from '@/constants/newsZone'
import type { NewsItem, RelatedLinkVO } from '@/api/types'
import PolicyPointsCard from './components/PolicyPointsCard.vue'
import ConsultSidebar from './components/ConsultSidebar.vue'

const route = useRoute()
const userStore = useUserStore()

const article = ref<NewsItem | null>(null)
const relatedArticles = ref<NewsItem[]>([])
const relatedLinks = ref<RelatedLinkVO[]>([])
const collected = ref(false)
const loading = ref(false)
const collectLoading = ref(false)

const categoryLabel = computed(
  () => article.value?.categoryName || NEWS_CATEGORY_MAP[article.value?.category || ''] || '教育资讯'
)
const displayDate = computed(() => (article.value ? formatNewsDate(article.value) : ''))
const showConsult = computed(() => article.value?.consultEnabled !== 0)
const tags = computed(() => {
  const t = article.value?.tags
  if (!t) return []
  return t.split(',').map((s) => s.trim()).filter(Boolean)
})

async function fetchDetail(id: number) {
  loading.value = true
  try {
    const res = await newsApi.getDetail(id)
    const vo = res.data.data
    article.value = vo.article
    relatedArticles.value = vo.relatedArticles || []
    relatedLinks.value = vo.relatedLinks || []
    collected.value = !!vo.collected
  } catch {
    article.value = null
    ElMessage.error('获取资讯详情失败')
  } finally {
    loading.value = false
  }
}

async function toggleCollect() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后收藏')
    return
  }
  const id = article.value?.id
  if (!id) return
  collectLoading.value = true
  try {
    if (collected.value) {
      await newsApi.uncollect(id)
      collected.value = false
      ElMessage.success('已取消收藏')
    } else {
      await newsApi.collect(id)
      collected.value = true
      ElMessage.success('收藏成功')
    }
  } catch {
    /* handled globally */
  } finally {
    collectLoading.value = false
  }
}

onMounted(() => {
  const id = Number(route.params.id)
  if (id) fetchDetail(id)
})

watch(
  () => route.params.id,
  (newId) => {
    if (newId) fetchDetail(Number(newId))
  }
)
</script>

<style scoped>
.news-detail-page {
  padding: 24px 0 48px;
  background: var(--bg-page, #f5f7fa);
  min-height: 100%;
}
.detail-layout {
  max-width: 1100px;
  padding: 0 24px;
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
  align-items: start;
}
.back-link {
  grid-column: 1 / -1;
}
.loading-wrap,
.el-empty {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px;
}
.article {
  padding: 32px 40px;
  position: relative;
}
.article-actions {
  position: absolute;
  top: 20px;
  right: 20px;
}
.article h1 {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.4;
  margin: 0 48px 16px 0;
}
.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 20px;
  font-size: 14px;
  color: var(--text-secondary);
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 24px;
}
.cover-wrap img {
  width: 100%;
  border-radius: 8px;
  margin-bottom: 24px;
}
.article-content :deep(h2) {
  font-size: 20px;
  font-weight: 700;
  margin: 28px 0 12px;
  color: var(--color-primary);
}
.article-content :deep(p) {
  font-size: 15px;
  line-height: 1.8;
  margin-bottom: 16px;
}
.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border-light);
}
.related-section,
.links-section {
  margin-top: 20px;
  padding: 20px 24px;
}
.related-section h3,
.links-section h3 {
  font-size: 16px;
  margin: 0 0 16px;
}
.related-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px dashed var(--border-light);
  text-decoration: none;
  color: inherit;
}
.related-item:last-child {
  border-bottom: none;
}
.rel-title {
  flex: 1;
  font-size: 14px;
}
.rel-meta {
  font-size: 12px;
  color: var(--text-secondary);
  flex-shrink: 0;
}
.link-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin: 0 8px 8px 0;
  padding: 8px 14px;
  background: var(--bg-page);
  border-radius: 8px;
  font-size: 13px;
  text-decoration: none;
  color: var(--color-primary);
}

@media (max-width: 900px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
  .side-col {
    order: -1;
  }
}
</style>
