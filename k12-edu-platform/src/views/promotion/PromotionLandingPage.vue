<template>
  <div class="promotion-page">
    <TopNavBar
      :stages="stages"
      :current-stage="currentStage"
      :menu-items="topMenuItems"
      :current-func-key="currentFuncKey"
      @switch-stage="onSwitchStage"
      @update:current-func-key="onFuncNav"
    />

    <div class="page-body">
      <nav class="breadcrumb" aria-label="面包屑">
        <router-link to="/">首页</router-link>
        <span class="sep">/</span>
        <span>升学备考</span>
        <span class="sep">/</span>
        <span class="current">{{ pageMeta.title.replace('备考专区', '') }}</span>
      </nav>

      <header class="promotion-hero">
        <div class="hero-text">
          <h1>{{ pageMeta.icon }} {{ pageMeta.title }}</h1>
          <p>{{ pageMeta.description }}</p>
        </div>
        <PromotionTopicCards
          v-if="currentFuncKey === 'youxiao'"
          :topics="kindergartenTopics"
          @select="goTopicBrowse"
        />
      </header>

      <div class="promotion-modules">
        <div id="paper-module">
          <PaperModule
            title="试卷专区"
            :tab-key="paperTabKey"
            :stage-key="paperStageKey"
            :grade-name="paperGradeName"
            @select="openPanelResource"
          />
        </div>

        <div id="exam-module">
          <ExamModule
            title="升学专区"
            :exam-type="promotionExamType"
            :topic="activeTopic"
            :promotion-exam-tabs="promotionExamTypeTabs"
            :promotion-topics-map="promotionTopicsMap"
            @select="openPanelResource"
            @update:exam-type="onExamTypeChange"
            @update:topic="onTopicChange"
            @browse-all="onPromotionBrowseAll"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import TopNavBar from '@/views/home/components/TopNavBar.vue'
import PaperModule from '@/views/home/components/PaperModule.vue'
import ExamModule from '@/views/home/components/ExamModule.vue'
import PromotionTopicCards from '@/components/promotion/PromotionTopicCards.vue'
import { useHomeFuncChannels } from '@/composables/useHomeFuncChannels'
import type { StageKey } from '@/config/subjectConfig'
import type { HomeFuncKey } from '@/constants/homeFuncChannels'
import type { ResourceListItem } from '@/components/shared'
import { listKindergartenBridgeTopics } from '@/constants/promotionTopicBrowse'
import { buildFuncBrowseRoute } from '@/utils/funcBrowseRoute'
import {
  applyPromotionPageMeta,
  buildPromotionRoute,
  examTypeToFuncKey,
  getDefaultPromotionFuncKey,
  PROMOTION_LANDING_META,
  resolvePromotionFuncKey,
} from '@/utils/promotionRoute'

const props = defineProps<{
  type: string
}>()

const route = useRoute()
const router = useRouter()

const {
  menuItems: topMenuItems,
  promotionTopicsMap,
  promotionExamTypeTabs,
  getChannel,
} = useHomeFuncChannels()

const stages = [
  { key: 'preschool', name: '幼儿' },
  { key: 'primary', name: '小学' },
  { key: 'junior', name: '初中' },
  { key: 'senior', name: '高中' },
  { key: 'art', name: '美术' },
  { key: 'dance', name: '舞蹈' },
]

const resolvedFuncKey = computed<HomeFuncKey>(() => {
  return resolvePromotionFuncKey(props.type) ?? getDefaultPromotionFuncKey()
})

const currentFuncKey = computed(() => resolvedFuncKey.value)
const activeChannel = computed(() => getChannel(currentFuncKey.value))
const pageMeta = computed(() => PROMOTION_LANDING_META[currentFuncKey.value])

const kindergartenTopics = listKindergartenBridgeTopics()

const promotionExamType = computed(() => activeChannel.value.examType)
const paperTabKey = computed(() => activeChannel.value.paperTab)
const paperStageKey = computed(() => activeChannel.value.stageKey)
const paperGradeName = computed(() => activeChannel.value.paperDefaultGrade)

const activeTopic = computed(() => {
  const q = route.query.topic
  const fromQuery = typeof q === 'string' ? q.trim() : ''
  if (fromQuery) return fromQuery
  return activeChannel.value.defaultTopic
})

const currentStage = ref<StageKey>(activeChannel.value.stageKey)

watch(
  resolvedFuncKey,
  (key) => {
    if (!resolvePromotionFuncKey(props.type)) {
      router.replace(buildPromotionRoute(key))
      return
    }
    currentStage.value = getChannel(key).stageKey
    applyPromotionPageMeta(key)
  },
  { immediate: true },
)

watch(
  () => activeChannel.value.stageKey,
  (stageKey) => {
    currentStage.value = stageKey
  },
)

function onSwitchStage(key: string) {
  currentStage.value = key as StageKey
}

function onFuncNav(key: string) {
  const channel = getChannel(key)
  const browseRoute = buildFuncBrowseRoute(
    key,
    { stage: currentStage.value, topic: channel.defaultTopic },
    channel,
  )
  if (browseRoute) {
    router.push(browseRoute)
    return
  }
  router.push(buildPromotionRoute(key, { topic: activeTopic.value }))
}

function goTopicBrowse(topic: string) {
  const browseRoute = buildFuncBrowseRoute(
    'youxiao',
    { topic },
    getChannel('youxiao'),
  )
  if (browseRoute) {
    router.push(browseRoute)
  }
}

function onPromotionBrowseAll(payload: { examType: string; topic: string }) {
  const funcKey = examTypeToFuncKey(payload.examType)
  if (!funcKey) return
  const browseRoute = buildFuncBrowseRoute(
    funcKey,
    { topic: payload.topic },
    getChannel(funcKey),
  )
  if (browseRoute) {
    router.push(browseRoute)
  }
}

function onExamTypeChange(examType: string) {
  const funcKey = examTypeToFuncKey(examType)
  if (!funcKey || funcKey === currentFuncKey.value) return
  const topics = promotionTopicsMap.value[examType]
  const topic = topics?.[0] || activeTopic.value
  router.push(buildPromotionRoute(funcKey, { topic }))
}

function onTopicChange(topic: string) {
  if (!topic || topic === activeTopic.value) return
  router.replace({
    path: route.path,
    query: { ...route.query, topic },
  })
}

function openPanelResource(item: ResourceListItem) {
  if (item.detailPath) {
    router.push(item.detailPath)
    return
  }
  if (typeof item.id === 'number') {
    router.push(`/resource/${item.id}`)
    return
  }
  ElMessage.warning('该资源暂无可用详情，已过滤虚拟数据跳转')
}
</script>

<style scoped>
.promotion-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.page-body {
  max-width: 1440px;
  margin: 24px auto;
  padding: 0 24px 48px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.breadcrumb {
  font-size: 14px;
  color: #909399;
}

.breadcrumb a {
  color: #409eff;
  text-decoration: none;
}

.breadcrumb a:hover {
  text-decoration: underline;
}

.breadcrumb .sep {
  margin: 0 8px;
}

.breadcrumb .current {
  color: #303133;
}

.promotion-hero {
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f9eb 100%);
  border: 1px solid #d9ecff;
  border-radius: 16px;
  padding: 28px 32px;
}

.hero-text h1 {
  margin: 0 0 10px;
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.hero-text p {
  margin: 0;
  font-size: 15px;
  line-height: 1.7;
  color: #606266;
  max-width: 720px;
}

.promotion-modules {
  display: flex;
  flex-direction: column;
  gap: 32px;
}
</style>
