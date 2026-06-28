<template>
  <section class="latest-content">
    <h2 class="section-main-title">&mdash; {{ sectionTitle }} &mdash;</h2>

    <div class="cards-row">
      <article
        v-for="column in columns"
        :key="column.key"
        class="content-card"
      >
        <header class="card-header">
          <div class="card-title-wrap">
            <span class="card-icon" aria-hidden="true" />
            <h3 class="card-title">{{ column.title }}</h3>
          </div>
          <a class="more-link" href="javascript:;" @click.prevent="goMore(column)">更多 &gt;&gt;</a>
        </header>

        <ul class="item-list">
          <ResourceListRow
            v-for="(item, index) in column.items"
            :key="`${column.key}-${item.id ?? index}`"
            :item="item"
            theme="blue"
            @click="onItemClick(column, item)"
          />
        </ul>
      </article>
    </div>

    <PaginationDots v-model="currentPage" :count="pageCount" />
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ResourceListRow, PaginationDots } from '@/components/shared'
import { newsApi } from '@/api'
import { fetchHomeLatestColumns } from '@/api/homeOps'
import { USE_HOME_OPS_API } from '@/config/featureFlags'
import { formatNewsDate } from '@/constants/newsZone'

interface ListItem {
  id?: number
  title: string
  date: string
}

interface Column {
  key: string
  title: string
  morePath: string
  dataSource?: string
  items: ListItem[]
}

const props = withDefaults(
  defineProps<{
    title?: string
    columns?: Column[]
    stageKey?: string
  }>(),
  { title: '最新内容', stageKey: 'primary' }
)

const router = useRouter()
const sectionTitle = props.title
const currentPage = ref(0)
const pageCount = 3

const defaultColumns: Column[] = [
  {
    key: 'material',
    title: '最新资料',
    morePath: '/resource/sync',
    items: [
      { title: '2025年福建省泉州市初三一模试卷及答案（全科）', date: '2026-05-16' },
      { title: '2025年浙江省杭州市初三一模试卷及答案（全科）', date: '2026-05-16' },
      { title: '2025年广东省广州市初三一模试卷及答案（全科）', date: '2026-05-16' },
      { title: '2025年江苏省南京市初三一模试卷及答案（全科）', date: '2026-05-16' },
      { title: '2025年山东省济南市初三一模试卷及答案（全科）', date: '2026-05-16' },
      { title: '2025年湖北省武汉市初三一模试卷及答案（全科）', date: '2026-05-16' },
      { title: '2025年四川省成都市初三一模试卷及答案（全科）', date: '2026-05-16' },
      { title: '2025年河南省郑州市初三一模试卷及答案（全科）', date: '2026-05-16' },
    ],
  },
  {
    key: 'topic',
    title: '最新专题',
    morePath: '/topic',
    items: [
      { title: '文言二则PPT课件', date: '2026-05-16' },
      { title: '夜色ppt', date: '2026-05-16' },
      { title: 'Natural disasters ppt', date: '2026-05-16' },
      { title: '营养午餐ppt', date: '2026-05-15' },
      { title: '《草原》精品课件', date: '2026-05-15' },
      { title: '《星空》教学设计专题', date: '2026-05-15' },
      { title: '中秋节主题班会PPT', date: '2026-05-14' },
      { title: '环保主题教育专题资源', date: '2026-05-14' },
    ],
  },
  {
    key: 'news',
    title: '最新资讯',
    morePath: '/news',
    dataSource: 'api',
    items: [
      { title: '一年级上册过生日教案第一课时教案', date: '2026-05-12' },
      { title: '【上传定价参考标准】平台资源上传说明', date: '2026-05-10' },
      { title: '2026年春季学习资源更新公告', date: '2026-04-28' },
      { title: '智能备课功能全面升级通知', date: '2026-04-20' },
      { title: '小学语文新教材同步资源上线', date: '2026-04-15' },
      { title: '初中数学期中复习指导专题', date: '2026-03-28' },
      { title: '高中英语阅读精准练习指南', date: '2026-03-15' },
      { title: '平台维护公告（3月二十九日）', date: '2026-02-27' },
    ],
  },
]

const columns = ref<Column[]>(props.columns?.length ? [...props.columns] : [...defaultColumns])

function mapApiColumns(apiCols: Awaited<ReturnType<typeof fetchHomeLatestColumns>>): Column[] {
  return apiCols.map((col) => ({
    key: col.key,
    title: col.title,
    morePath: col.morePath,
    dataSource: col.dataSource,
    items: (col.items ?? []).map((item) => ({
      id: item.id ?? item.resourceId ?? item.articleId,
      title: item.title,
      date: item.date ?? '',
    })),
  }))
}

async function loadFromApi() {
  if (!USE_HOME_OPS_API || props.columns?.length) return
  try {
    const apiCols = await fetchHomeLatestColumns(props.stageKey)
    if (apiCols.length) {
      columns.value = mapApiColumns(apiCols)
      await loadNewsColumn()
    }
  } catch {
    await loadNewsColumn()
  }
}

async function loadNewsColumn() {
  const newsCol = columns.value.find((c) => c.key === 'news' || c.dataSource === 'api')
  if (!newsCol) return
  try {
    const res = await newsApi.getHome()
    const home = res.data.data
    const items: ListItem[] = []
    if (home?.headlines?.length) {
      home.headlines.slice(0, 4).forEach((a) => {
        items.push({ id: a.id, title: a.title, date: formatNewsDate(a) })
      })
    }
    const channelOrder = ['policy', 'exam', 'teacher', 'research', 'reform']
    for (const key of channelOrder) {
      if (items.length >= 8) break
      const list = home?.channels?.[key] || []
      for (const a of list) {
        if (items.length >= 8) break
        if (!items.some((i) => i.title === a.title)) {
          items.push({ id: a.id, title: a.title, date: formatNewsDate(a) })
        }
      }
    }
    if (items.length) {
      const idx = columns.value.findIndex((c) => c.key === newsCol.key)
      if (idx >= 0) {
        columns.value[idx] = { ...columns.value[idx], items }
      }
    }
  } catch {
    /* 保留默认或 API 返回的静态兜底 */
  }
}

onMounted(() => {
  if (props.columns?.length) {
    loadNewsColumn()
  } else {
    loadFromApi()
  }
})

function goMore(column: Column) {
  router.push(column.morePath)
}

function onItemClick(column: Column, item: ListItem) {
  if ((column.key === 'news' || column.dataSource === 'api') && item.id) {
    router.push(`/news/${item.id}`)
    return
  }
  if (item.id) {
    router.push(`/resource/${item.id}`)
  }
}
</script>

<style scoped>
.latest-content {
  margin-top: 8px;
  padding-bottom: 24px;
}

.section-main-title {
  text-align: center;
  font-size: 22px;
  font-weight: 700;
  color: #222;
  margin: 0 0 28px;
  letter-spacing: 2px;
}

.cards-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.content-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
  padding: 20px 20px 16px;
  min-width: 0;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  gap: 12px;
}

.card-title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.card-icon {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff9a44 0%, #4facfe 100%);
  box-shadow: 0 2px 6px rgba(79, 172, 254, 0.35);
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: #222;
  margin: 0;
  white-space: nowrap;
}

.more-link {
  flex-shrink: 0;
  font-size: 13px;
  color: #bbb;
  text-decoration: none;
  transition: color 0.2s;
}

.more-link:hover {
  color: #409eff;
}

.item-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

@media (max-width: 1200px) {
  .cards-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .section-main-title {
    font-size: 18px;
  }

  .content-card {
    padding: 16px;
  }
}
</style>
