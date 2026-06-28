<template>
  <div class="platform-page">
    <!-- 学段 + 功能 Tab（搜索/资料篮/上传已并入全站 AppHeader） -->
    <TopNavBar
      :stages="stages"
      :current-stage="currentStage"
      :menu-items="topMenuItems"
      :current-func-key="homeFuncHighlight"
      @switch-stage="onSwitchStage"
      @update:current-func-key="onFuncKeyChange"
    />

    <!-- 内容主体 -->
    <div class="page-body">
      <!-- 首屏：左侧学科导航 + 右侧轮播与快捷功能（底边对齐） -->
      <div class="home-hero-row">
        <div ref="sidebarWrapRef" class="hero-sidebar-wrap">
          <LeftSidebar
            :subjects="currentSubjects"
            :current="currentSubject"
            @select="selectSubject"
          />
        </div>

        <main ref="heroMainRef" class="hero-main">
          <div ref="homePageRef" class="home-page">
            <BannerSection :banners="bannerList" />
            <QuickFunctions :functions="featureCards" />
          </div>

          <Transition name="subject-pop">
            <div
              v-if="currentSubject"
              class="subject-detail-overlay"
              @click.self="closeSubjectDetail"
            >
              <SubjectDetail
                :stage="currentStage"
                :subject="currentSubject"
                :versions="subjectVersions"
                :resource-types="resourceTypes"
                :review-modules="reviewModules"
                :promotion-modules="promotionModules"
                :promotion-section-title="promotionSectionTitle"
                :loading="detailOptionsLoading"
                :selected-version="selectedVersion"
                :selected-resource-type="selectedResourceType"
                :selected-review-module="selectedReviewModule"
                :selected-promotion-module="selectedPromotionModule"
                @close="closeSubjectDetail"
                @select-version="selectVersion"
                @select-resource-type="selectResourceType"
                @select-review-module="selectReviewModule"
                @select-promotion-module="selectPromotionModule"
              />
            </div>
          </Transition>
        </main>
      </div>

      <!-- 教育资源容器 -->
      <div class="edu-resource-container">
        <!-- 同步备课模块 -->
        <SyncResourceModule title="同步备课" @select="openPanelResource" />

        <!-- 试卷专区 -->
        <div id="paper-module">
          <PaperModule
            title="试卷专区"
            :tab-key="paperTabKey"
            :stage-key="paperStageKey"
            :grade-name="paperGradeName"
            @select="openPanelResource"
          />
        </div>

        <!-- 升学专区 -->
        <div id="exam-module">
          <ExamModule
            title="升学专区"
            :exam-type="promotionExamType"
            :topic="promotionTopic"
            :promotion-exam-tabs="promotionExamTypeTabs"
            :promotion-topics-map="promotionTopicsMap"
            @select="openPanelResource"
            @browse-all="onPromotionBrowseAll"
          />
        </div>
      </div>

      <!-- 最新内容 -->
      <LatestContent />
    </div>
  </div>

  <!-- ===== 版本/册别选择弹窗（共享组件） ===== -->
  <VersionVolumeSelector
    :visible="showVersionModal"
    :versions="modalVersionList"
    :volumes="modalVolumeList"
    :selected-version-key="selectedVersion"
    selected-volume-id=""
    @confirm="handleVersionConfirm"
    @close="cancelVersionModal"
  />
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

// 组件
import TopNavBar from './components/TopNavBar.vue'
import LeftSidebar from './components/LeftSidebar.vue'
import SubjectDetail from './components/SubjectDetail.vue'
import BannerSection from './components/BannerSection.vue'
import QuickFunctions from './components/QuickFunctions.vue'
import SyncResourceModule from './components/SyncResourceModule.vue'
import PaperModule from './components/PaperModule.vue'
import ExamModule from './components/ExamModule.vue'
import LatestContent from './components/LatestContent.vue'
import VersionVolumeSelector from '@/components/subject/VersionVolumeSelector.vue'
import { type StageKey } from '@/config/subjectConfig'
import { loadSubjects as loadSubjectsFromApi } from '@/composables/taxonomySource'
import { loadHomeBanners, loadHomeQuickEntries } from '@/composables/homeOpsSource'
import type { BannerViewModel, QuickEntryViewModel } from '@/types/homeOps'
import { useHomeSubjectDetailOptions } from '@/composables/useHomeSubjectDetailOptions'
import { normalizeVersionKeyForRoute } from '@/utils/editionAdapter'
import { volumeDataMap } from '@/config/volumeData'
import { buildSubjectBrowseRoute } from '@/utils/subjectBrowseRoute'
import type { ResourceListItem } from '@/components/shared'
import { useHomeFuncChannels } from '@/composables/useHomeFuncChannels'
import { DEFAULT_HOME_FUNC_KEY } from '@/constants/homeFuncChannels'
import { buildFuncBrowseRoute } from '@/utils/funcBrowseRoute'
import { examTypeToFuncKey, resolvePromotionFuncKey } from '@/utils/promotionRoute'

const router = useRouter()
const route = useRoute()

const {
  menuItems: topMenuItems,
  promotionTopicsMap,
  promotionExamTypeTabs,
  getChannel,
} = useHomeFuncChannels()

// ===== 类型定义 =====
interface Subject {
  key: string
  name: string
  isNew?: boolean
}

// ===== 学段切换 =====
const stages = [
  { key: 'preschool', name: '幼儿' },
  { key: 'primary', name: '小学' },
  { key: 'junior', name: '初中' },
  { key: 'senior', name: '高中' },
  { key: 'art', name: '美术' },
  { key: 'dance', name: '舞蹈' },
]

const currentStage = ref<StageKey>('primary')

// 首页顶栏升学入口高亮（仅当带 ?func= 时）
const homeFuncHighlight = computed(() => {
  const func = route.query.func
  if (typeof func !== 'string') return ''
  return resolvePromotionFuncKey(func) ?? ''
})

// 首页三大专区默认：小学
const activeFuncChannel = computed(() => getChannel(DEFAULT_HOME_FUNC_KEY))
const promotionExamType = computed(() => activeFuncChannel.value.examType)
const promotionTopic = computed(() => activeFuncChannel.value.defaultTopic)
// 试卷专区：小学默认期中 + 六年级下册（与升学入口配置解耦）
const paperTabKey = ref('midterm')
const paperStageKey = ref<StageKey>('primary')
const paperGradeName = ref('六年级下册')

// ===== 学科列表（Phase 5-F：taxonomy API） =====
const stageSubjects = ref<Subject[]>([])
const subjectsLoading = ref(false)

async function refreshStageSubjects() {
  subjectsLoading.value = true
  try {
    const list = await loadSubjectsFromApi(currentStage.value)
    stageSubjects.value = list.map((item) => ({
      key: item.key,
      name: item.name,
      isNew: item.isNew,
    }))
  } catch {
    stageSubjects.value = []
  } finally {
    subjectsLoading.value = false
  }
}

const currentSubjects = computed(() => stageSubjects.value)
const currentSubject = ref<Subject | null>(null)

const {
  versions: subjectVersions,
  resourceTypes,
  reviewModules,
  promotionModules,
  promotionSectionTitle,
  loading: detailOptionsLoading,
  loadOptions: loadSubjectDetailOptions,
  clearOptions: clearSubjectDetailOptions,
} = useHomeSubjectDetailOptions()

// ===== 选中状态 =====
const selectedVersion = ref('')
const selectedResourceType = ref('')
const selectedReviewModule = ref('')
const selectedPromotionModule = ref('')

// ===== 版本/册别选择弹窗状态 =====
const showVersionModal = ref(false)
const pendingResourceType = ref('')           // 点击的待跳转资源类型
const pendingSubject = ref<Subject | null>(null) // 待跳转学科

// ===== 学科选择 =====
async function selectSubject(subject: Subject) {
  if (currentSubject.value?.key === subject.key) {
    closeSubjectDetail()
    return
  }
  currentSubject.value = subject
  selectedResourceType.value = ''
  selectedReviewModule.value = ''
  selectedPromotionModule.value = ''
  selectedVersion.value = ''
  await loadSubjectDetailOptions(currentStage.value, subject.key, subject.name)
  selectedVersion.value = subjectVersions.value[0]?.key || ''
}

function closeSubjectDetail() {
  currentSubject.value = null
  clearSubjectDetailOptions()
  selectedVersion.value = ''
  selectedResourceType.value = ''
  selectedReviewModule.value = ''
  selectedPromotionModule.value = ''
}

async function navigateSubjectBrowse(
  subject: Subject,
  query: Record<string, string>,
  versionKey?: string,
) {
  closeSubjectDetail()
  const vk =
    versionKey ||
    selectedVersion.value ||
    normalizeVersionKeyForRoute(subjectVersions.value[0]?.key || 'tongbian2024')
  await router.push(
    buildSubjectBrowseRoute({
      stage: currentStage.value,
      subjectKey: subject.key,
      versionKey: vk,
      query,
    }),
  )
}

// 弹窗内版本列表（来自 subject-nav API）
const modalVersionList = computed(() => {
  if (!pendingSubject.value) return []
  if (currentSubject.value?.key === pendingSubject.value.key) {
    return subjectVersions.value.map((v) => ({
      id: v.id,
      key: v.key,
      name: v.name,
      isNew: v.isNew,
    }))
  }
  return subjectVersions.value.map((v) => ({
    id: v.id,
    key: v.key,
    name: v.name,
    isNew: v.isNew,
  }))
})

// 弹窗内册别列表（根据当前学段从 volumeDataMap 获取）
const modalVolumeList = computed(() => volumeDataMap[currentStage.value] || [])

function openVersionModal(typeName: string, subject: Subject) {
  pendingResourceType.value = typeName
  pendingSubject.value = subject
  showVersionModal.value = true
}

function selectResourceType(typeName: string, subject: Subject) {
  // 打开版本/册别选择弹窗，确认后再跳转
  openVersionModal(typeName, subject)
}

async function selectVersion(version: { key: string; editionName?: string }, subject: Subject) {
  const versionKey = normalizeVersionKeyForRoute(version.key)
  selectedVersion.value = versionKey
  selectedResourceType.value = ''
  selectedReviewModule.value = ''
  selectedPromotionModule.value = ''
  const query: Record<string, string> = { module: '同步备课' }
  if (version.editionName) {
    query.edition = version.editionName
  }
  await navigateSubjectBrowse(subject, query, versionKey)
}

async function selectReviewModule(moduleName: string, subject: Subject) {
  selectedReviewModule.value = moduleName
  selectedResourceType.value = ''
  selectedPromotionModule.value = ''
  await navigateSubjectBrowse(subject, { module: moduleName })
}

async function selectPromotionModule(moduleName: string, subject: Subject) {
  selectedPromotionModule.value = moduleName
  selectedResourceType.value = ''
  selectedReviewModule.value = ''
  await navigateSubjectBrowse(subject, { module: moduleName })
}

async function handleVersionConfirm(versionKey: string, volumeId: string) {
  if (!pendingSubject.value || !pendingResourceType.value) return
  const subject = pendingSubject.value
  const resourceType = pendingResourceType.value
  showVersionModal.value = false
  pendingResourceType.value = ''
  pendingSubject.value = null
  const normalizedKey = normalizeVersionKeyForRoute(versionKey)
  await navigateSubjectBrowse(
    subject,
    { type: resourceType, volume: volumeId },
    normalizedKey,
  )
}

function cancelVersionModal() {
  showVersionModal.value = false
  pendingResourceType.value = ''
  pendingSubject.value = null
}

// ===== 切换学段 =====
function switchStage(key: StageKey) {
  currentStage.value = key
  closeSubjectDetail()
  void refreshStageSubjects()
  void refreshHomeOps()
}

function onSwitchStage(key: string) {
  switchStage(key as StageKey)
}

function onFuncKeyChange(key: string) {
  const channel = getChannel(key)
  const route = buildFuncBrowseRoute(
    key,
    {
      stage: currentStage.value,
      subjectKey: currentSubject.value?.key,
      versionKey:
        selectedVersion.value ||
        subjectVersions.value[0]?.key ||
        undefined,
      topic: channel.defaultTopic,
    },
    channel,
  )
  if (route) {
    router.push(route)
    return
  }
  ElMessage.warning('暂未配置该升学入口')
}

function onPromotionBrowseAll(payload: { examType: string; topic: string }) {
  const funcKey = examTypeToFuncKey(payload.examType)
  if (!funcKey) return
  const channel = getChannel(funcKey)
  const browseRoute = buildFuncBrowseRoute(
    funcKey,
    { topic: payload.topic },
    channel,
  )
  if (browseRoute) {
    router.push(browseRoute)
  }
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

// ===== 轮播图数据 =====
// ===== 轮播 & 快捷功能（Phase 7-A：home ops API） =====
const bannerList = ref<BannerViewModel[]>([])
const featureCards = ref<QuickEntryViewModel[]>([])

async function refreshHomeOps() {
  const stage = currentStage.value
  const [banners, entries] = await Promise.all([
    loadHomeBanners(stage),
    loadHomeQuickEntries(stage),
  ])
  bannerList.value = banners
  featureCards.value = entries
}

// ===== 侧栏高度与右侧首页内容（轮播+快捷功能）底边对齐 =====
const sidebarWrapRef = ref<HTMLElement | null>(null)
const homePageRef = ref<HTMLElement | null>(null)
const heroMainRef = ref<HTMLElement | null>(null)

let resizeObserver: ResizeObserver | null = null

function syncSidebarHeight() {
  const wrap = sidebarWrapRef.value
  const target = homePageRef.value
  const heroMain = heroMainRef.value
  if (!wrap || !target) return
  const h = target.offsetHeight
  wrap.style.height = `${h}px`
  if (heroMain) heroMain.style.height = `${h}px`
}

function setupResizeObserver() {
  resizeObserver?.disconnect()
  resizeObserver = new ResizeObserver(() => syncSidebarHeight())
  if (homePageRef.value) {
    resizeObserver.observe(homePageRef.value)
  }
}

onMounted(() => {
  void refreshStageSubjects()
  void refreshHomeOps()
  const func = route.query.func
  if (typeof func === 'string' && resolvePromotionFuncKey(func)) {
    const channel = getChannel(func)
    const browseRoute = buildFuncBrowseRoute(
      func,
      {
        stage: currentStage.value,
        subjectKey: currentSubject.value?.key,
        versionKey: selectedVersion.value || undefined,
        topic: channel.defaultTopic,
      },
      channel,
    )
    if (browseRoute) {
      router.replace(browseRoute)
      return
    }
  }
  nextTick(() => {
    setupResizeObserver()
    syncSidebarHeight()
  })
  window.addEventListener('resize', syncSidebarHeight)
})

onUnmounted(() => {
  resizeObserver?.disconnect()
  window.removeEventListener('resize', syncSidebarHeight)
})

watch(currentStage, () => {
  nextTick(() => {
    setupResizeObserver()
    syncSidebarHeight()
  })
})

</script>

<style scoped>
.platform-page {
  min-height: 100vh;
  background: #F5F7FA;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', sans-serif;
}

*,
*::before,
*::after {
  box-sizing: border-box;
}

.page-body {
  max-width: 1440px;
  margin: 24px auto;
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* 左侧学科栏与右侧轮播+快捷功能底边对齐 */
.home-hero-row {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.hero-sidebar-wrap {
  flex: 0 0 168px;
  width: 168px;
}

.hero-sidebar-wrap :deep(.left-sidebar) {
  height: 100%;
}

.hero-main {
  flex: 1;
  min-width: 0;
  position: relative;
  overflow: hidden;
}

.subject-detail-overlay {
  position: absolute;
  inset: 0;
  z-index: 20;
  display: flex;
  align-items: stretch;
  padding: 8px;
  box-sizing: border-box;
  background: rgba(255, 255, 255, 0.52);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-radius: 16px;
}

.subject-detail-overlay :deep(.subject-detail) {
  width: 100%;
  height: 100%;
  max-height: 100%;
  min-height: 0;
  margin-bottom: 0;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(31, 47, 58, 0.14);
  border: 1px solid rgba(67, 97, 238, 0.14);
}

.subject-pop-enter-active,
.subject-pop-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.subject-pop-enter-from,
.subject-pop-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

.edu-resource-container {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.home-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

</style>
