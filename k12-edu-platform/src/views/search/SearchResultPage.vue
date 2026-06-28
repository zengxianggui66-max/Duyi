<template>

  <div class="search-result-page">

    <div class="container">

      <div class="search-header">

        <h2>全站搜索</h2>

        <p v-if="keyword">

          关键词：<strong>{{ keyword }}</strong>

          <span v-if="!loading" class="result-total">共 {{ total }} 条结果</span>

        </p>

      </div>



      <el-alert

        v-if="queryHint"

        :title="queryHint"

        type="warning"

        show-icon

        :closable="false"

        class="search-hint-alert"

      />



      <div v-if="displayIntents.length" class="intent-row">

        <span class="intent-label">你可能要找：</span>

        <el-tag

          v-for="item in displayIntents"

          :key="`${item.kind}-${item.text}`"

          class="intent-tag"

          effect="plain"

          @click="onIntentClick(item)"

        >

          {{ item.text }}

        </el-tag>

      </div>



      <el-card class="search-filter-card">

        <div class="filter-row">

          <span class="filter-label">内容域</span>

          <el-radio-group v-model="selectedDomain" @change="onFilterChange">

            <el-radio-button

              v-for="item in domainOptions"

              :key="item.key || 'all'"

              :label="item.key"

            >

              {{ item.label }}

              <span v-if="domainCount(item.key)" class="facet-count">{{ domainCount(item.key) }}</span>

            </el-radio-button>

          </el-radio-group>

        </div>



        <div class="filter-row">

          <span class="filter-label">学段</span>

          <el-radio-group v-model="selectedStage" @change="onFilterChange">

            <el-radio-button label="">全部</el-radio-button>

            <el-radio-button

              v-for="item in stageOptions"

              :key="item.key"

              :label="item.key"

            >

              {{ item.name }} {{ item.count }}

            </el-radio-button>

          </el-radio-group>

        </div>



        <div class="filter-row">

          <span class="filter-label">频道</span>

          <el-radio-group v-model="selectedChannel" @change="onFilterChange">

            <el-radio-button label="">全部</el-radio-button>

            <el-radio-button

              v-for="item in channelOptions"

              :key="item.key"

              :label="item.key"

            >

              {{ item.name }} {{ item.count }}

            </el-radio-button>

          </el-radio-group>

        </div>



        <div class="filter-row">

          <span class="filter-label">类型</span>

          <el-radio-group v-model="selectedType" @change="onFilterChange">

            <el-radio-button label="">全部</el-radio-button>

            <el-radio-button

              v-for="item in typeOptions"

              :key="item.key"

              :label="item.key"

            >

              {{ item.name }} {{ item.count }}

            </el-radio-button>

          </el-radio-group>

        </div>



        <div class="filter-row">

          <span class="filter-label">排序</span>

          <el-radio-group v-model="selectedSort" @change="onFilterChange">

            <el-radio-button label="score">综合</el-radio-button>

            <el-radio-button label="newest">最新</el-radio-button>

            <el-radio-button label="download">下载量</el-radio-button>

          </el-radio-group>

        </div>

      </el-card>



      <div v-if="loading" class="loading-wrap">

        <el-skeleton :rows="6" animated />

      </div>



      <div v-else-if="!records.length" class="zero-result-wrap">

        <el-empty description="暂无匹配结果，换个关键词试试" />

        <div class="zero-recommend">

          <p class="zero-recommend-title">推荐入口</p>

          <div class="zero-entry-grid">

            <button

              v-for="entry in displayZeroEntries"

              :key="entry.text"

              type="button"

              class="zero-entry-card"

              @click="onZeroEntryClick(entry)"

            >

              <span class="zero-entry-label">{{ entry.label }}</span>

              <span class="zero-entry-text">{{ entry.text }}</span>

            </button>

          </div>

          <div v-if="recommendations.length" class="zero-recommend-tags">

            <el-tag

              v-for="item in recommendations"

              :key="`${item.kind}-${item.text}`"

              class="zero-recommend-tag"

              :type="recommendTagType(item.kind)"

              effect="plain"

              @click="onRecommendClick(item)"

            >

              {{ item.text }}

            </el-tag>

          </div>

        </div>

      </div>



      <div v-else class="result-list">

        <el-card

          v-for="item in records"

          :key="item.docId"

          class="result-item"

          shadow="hover"

          @click="openResult(item)"

        >

          <div class="result-head">

            <h3 class="result-title" v-html="item.titleHighlight || item.title" />

            <el-tag size="small" :type="resultTagType(item)">{{ resultTypeLabel(item) }}</el-tag>

          </div>

          <p v-if="item.subtitle" class="result-subtitle">{{ item.subtitle }}</p>

          <p class="result-summary" v-html="item.summaryHighlight || item.summary || '暂无摘要'" />

          <div class="result-meta">

            <el-tag v-if="item.contentDomain" size="small" type="warning">

              {{ contentDomainLabel(item.contentDomain) }}

            </el-tag>

            <el-tag v-if="item.stageName" size="small">{{ item.stageName }}</el-tag>

            <el-tag v-if="item.channelName" size="small" type="info">{{ item.channelName }}</el-tag>

            <el-tag v-if="item.teachingType" size="small" type="success">{{ item.teachingType }}</el-tag>

            <span v-if="item.subject">{{ item.subject }}</span>

            <span v-if="item.gradeName">{{ item.gradeName }}</span>

            <span v-if="item.docType === 'resource'">⬇ {{ item.downloadCount || 0 }}</span>

          </div>

        </el-card>

      </div>



      <div v-if="pages > 1" class="pager-wrap">

        <el-pagination

          background

          layout="prev, pager, next"

          :current-page="page"

          :page-size="size"

          :total="total"

          @current-change="onPageChange"

        />

      </div>

    </div>

  </div>

</template>



<script setup lang="ts">

import { computed, ref, watch } from 'vue'

import { useRoute, useRouter } from 'vue-router'

import {

  searchApi,

  SEARCH_CONTENT_DOMAIN_OPTIONS,

  contentDomainLabel,

  docTypeLabel,

  resolveResultRoute,

  resolveSuggestionRoute,

} from '@/api/search'

import { unwrapData } from '@/api/request'

import type {

  SearchResultItem,

  SearchFacetBucket,

  SearchSuggestItem,

  SearchContentDomain,

  SearchSuggestKind,

} from '@/api/search'

import { reportSearchClick } from '@/utils/searchAnalytics'
import { buildZeroEntries, matchSubjectIntents } from '@/config/searchCatalog'



const route = useRoute()

const router = useRouter()



const loading = ref(false)

const records = ref<SearchResultItem[]>([])

const total = ref(0)

const page = ref(1)

const size = ref(20)

const pages = ref(0)



const stageOptions = ref<SearchFacetBucket[]>([])

const channelOptions = ref<SearchFacetBucket[]>([])

const typeOptions = ref<SearchFacetBucket[]>([])

const domainFacetOptions = ref<SearchFacetBucket[]>([])

const queryHint = ref('')

const recommendations = ref<SearchSuggestItem[]>([])

const intents = ref<SearchSuggestItem[]>([])

const displayIntents = computed(() => {
  if (intents.value.length) return intents.value
  return matchSubjectIntents(keyword.value)
})

const displayZeroEntries = computed(() => buildZeroEntries(keyword.value))



const selectedDomain = ref<SearchContentDomain>('')

const selectedStage = ref('')

const selectedChannel = ref('')

const selectedType = ref('')

const selectedSort = ref<'score' | 'newest' | 'download'>('score')



const domainOptions = SEARCH_CONTENT_DOMAIN_OPTIONS

const keyword = computed(() => String(route.query.q || '').trim())



function domainCount(key: SearchContentDomain) {

  if (!key) return 0

  const found = domainFacetOptions.value.find((item) => item.key === key)

  return found?.count || 0

}



function syncFilterFromQuery() {

  selectedDomain.value = (String(route.query.domain || '') as SearchContentDomain) || ''

  selectedStage.value = String(route.query.stage || '')

  selectedChannel.value = String(route.query.channel || '')

  selectedType.value = String(route.query.type || '')

  const sort = String(route.query.sort || 'score')

  selectedSort.value = ['score', 'newest', 'download'].includes(sort)

    ? (sort as 'score' | 'newest' | 'download')

    : 'score'

  page.value = Number(route.query.page || 1) || 1

}



async function loadSearchResult() {

  if (!keyword.value) {

    records.value = []

    total.value = 0

    pages.value = 0

    recommendations.value = []

    intents.value = []

    return

  }

  loading.value = true

  try {

    const res = await searchApi.searchAll({

      q: keyword.value,

      page: page.value,

      size: size.value,

      domain: selectedDomain.value || undefined,

      stage: selectedStage.value || undefined,

      channel: selectedChannel.value || undefined,

      type: selectedType.value || undefined,

      sort: selectedSort.value,

    })

    const data = unwrapData(res)

    records.value = data.records || []

    total.value = data.total || 0

    pages.value = data.pages || 0

    page.value = data.page || page.value

    size.value = data.size || size.value

    queryHint.value = data.queryHint || ''

    recommendations.value = data.recommendations || []

    intents.value = data.intents?.length ? data.intents : matchSubjectIntents(keyword.value)

    stageOptions.value = data.facets?.stages || []

    channelOptions.value = data.facets?.channels || []

    typeOptions.value = data.facets?.types || []

    domainFacetOptions.value = data.facets?.domains || []

  } catch {
    records.value = []
    total.value = 0
    pages.value = 0
    intents.value = matchSubjectIntents(keyword.value)
    recommendations.value = []
  } finally {

    loading.value = false

  }

}



function pushQuery(next: Record<string, string | number | undefined>) {

  const query: Record<string, string> = {}

  const merged = {

    q: keyword.value,

    domain: selectedDomain.value || undefined,

    stage: selectedStage.value || undefined,

    channel: selectedChannel.value || undefined,

    type: selectedType.value || undefined,

    sort: selectedSort.value,

    page: page.value,

    ...next,

  }

  for (const [k, v] of Object.entries(merged)) {

    if (v !== undefined && v !== null && String(v) !== '') {

      query[k] = String(v)

    }

  }

  router.push({ name: 'SearchResult', query })

}



function onFilterChange() {

  page.value = 1

  pushQuery({ page: 1 })

}



function onPageChange(p: number) {

  page.value = p

  pushQuery({ page: p })

}



function resultTypeLabel(item: SearchResultItem) {

  if (item.teachingType) return item.teachingType

  if (item.channelName && item.docType === 'channel') return item.channelName

  return docTypeLabel(item.docType, item.channelName || '资源')

}



function resultTagType(item: SearchResultItem): 'success' | 'warning' | 'info' | 'primary' | 'danger' | undefined {

  const domain = item.contentDomain || ''

  if (domain === 'feature') return 'success'

  if (domain === 'prep') return 'warning'

  if (domain === 'news') return 'info'

  return 'primary'

}



function recommendTagType(kind: SearchSuggestKind): 'success' | 'warning' | 'info' | 'primary' {

  if (kind === 'channel' || kind === 'feature') return 'success'

  if (kind === 'prep') return 'warning'

  if (kind === 'news') return 'info'

  return 'primary'

}



function openResult(item: SearchResultItem) {

  reportSearchClick({

    keyword: keyword.value,

    docId: item.docId,

    resourceId: item.resourceId,

    resourceType: item.resourceType,

    clickType: 'result',

    position: records.value.findIndex((row) => row.docId === item.docId) + 1 || undefined,

    detailRoute: item.detailRoute,

  })

  const target = resolveResultRoute(item)

  if (target) {

    router.push(target)

  }

}



function onRecommendClick(item: SearchSuggestItem) {

  reportSearchClick({

    keyword: keyword.value,

    clickType: 'recommend',

    detailRoute: item.detailRoute,

  })

  const target = resolveSuggestionRoute(item)

  if (target) {

    router.push(target)

    return

  }

  router.push({ name: 'SearchResult', query: { q: item.text, page: '1', sort: 'score' } })

}



function onIntentClick(item: SearchSuggestItem) {

  onRecommendClick(item)

}



function onZeroEntryClick(entry: { text: string; detailRoute: string }) {

  reportSearchClick({

    keyword: keyword.value,

    clickType: 'recommend',

    detailRoute: entry.detailRoute,

  })

  router.push(entry.detailRoute)

}



watch(

  () => route.query,

  () => {

    syncFilterFromQuery()

    void loadSearchResult()

  },

  { immediate: true, deep: true },

)

</script>



<style scoped>

.search-result-page {

  min-height: 100vh;

  background: #f5f7fa;

  padding: 20px 0;

}



.container {

  max-width: 1200px;

  margin: 0 auto;

  padding: 0 20px;

}



.search-header {

  margin-bottom: 12px;

}



.search-header h2 {

  margin: 0 0 8px;

}



.result-total {

  margin-left: 8px;

  color: #606266;

}



.intent-row {

  display: flex;

  align-items: center;

  flex-wrap: wrap;

  gap: 8px;

  margin-bottom: 12px;

  padding: 10px 14px;

  background: #fff;

  border-radius: 10px;

  border: 1px solid #ebeef5;

}



.intent-label {

  color: #606266;

  font-size: 13px;

}



.intent-tag {

  cursor: pointer;

}



.search-filter-card {

  margin-bottom: 16px;

}



.search-hint-alert {

  margin-bottom: 16px;

}



.filter-row {

  display: flex;

  align-items: center;

  gap: 10px;

  margin-bottom: 10px;

  flex-wrap: wrap;

}



.filter-row:last-child {

  margin-bottom: 0;

}



.filter-label {

  width: 56px;

  color: #606266;

  flex-shrink: 0;

}



.facet-count {

  margin-left: 2px;

  opacity: 0.75;

}



.loading-wrap {

  background: #fff;

  border-radius: 12px;

  padding: 16px;

}



.result-list {

  display: flex;

  flex-direction: column;

  gap: 12px;

}



.result-item {

  cursor: pointer;

}



.result-head {

  display: flex;

  align-items: flex-start;

  justify-content: space-between;

  gap: 12px;

  margin-bottom: 6px;

}



.result-title {

  margin: 0;

  font-size: 18px;

  color: #1a5cbf;

  flex: 1;

}



.result-subtitle {

  margin: 0 0 6px;

  color: #909399;

  font-size: 12px;

}



.result-summary {

  margin: 0 0 10px;

  color: #606266;

  line-height: 1.7;

}



.result-meta {

  display: flex;

  align-items: center;

  gap: 10px;

  color: #909399;

  font-size: 12px;

  flex-wrap: wrap;

}



.pager-wrap {

  margin-top: 16px;

  display: flex;

  justify-content: center;

}



.zero-result-wrap {

  background: #fff;

  border-radius: 12px;

  padding: 16px;

}



.zero-recommend {

  margin-top: 8px;

}



.zero-recommend-title {

  margin: 0 0 12px;

  color: #606266;

  font-size: 14px;

  text-align: center;

}



.zero-entry-grid {

  display: grid;

  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));

  gap: 10px;

  margin-bottom: 16px;

}



.zero-entry-card {

  border: 1px solid #ebeef5;

  border-radius: 10px;

  background: #fafbfd;

  padding: 12px;

  text-align: left;

  cursor: pointer;

  transition: border-color 0.2s, background 0.2s;

}



.zero-entry-card:hover {

  border-color: #1a5cbf;

  background: #f5f9ff;

}



.zero-entry-label {

  display: block;

  color: #909399;

  font-size: 11px;

  margin-bottom: 4px;

}



.zero-entry-text {

  display: block;

  color: #303133;

  font-size: 14px;

  font-weight: 600;

}



.zero-recommend-tags {

  display: flex;

  flex-wrap: wrap;

  gap: 8px;

  justify-content: center;

}



.zero-recommend-tag {

  cursor: pointer;

}



:deep(em) {

  color: #f56c6c;

  font-style: normal;

}

</style>

