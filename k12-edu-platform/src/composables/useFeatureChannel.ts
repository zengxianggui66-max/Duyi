import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useResourceStore } from '@/store'
import { browseApi } from '@/api/browse'
import { unwrapData } from '@/api/request'
import type { PrimaryChineseItem } from '@/api/types'
import { channelConfigs, TAB_KEYWORDS } from '@/constants/featureChannels'
import { fetchChannelBootstrap } from '@/api/channel'
import { USE_HOME_OPS_API } from '@/config/featureFlags'
import {
  COMPETITION_CHANNEL,
  COMPETITION_GRADE_STAGES,
  COMPETITION_SUBJECTS,
  COMPETITION_FORMATS,
  COMPETITION_LEVELS,
  buildCompetitionListParams,
} from '@/constants/competitionZone'
import { THEME_CLASS_MEETING_MODULE } from '@/constants/themeClassMeetingNav'
import {
  isCanonicalListChannel,
  buildBanhuiBrowseParams,
  buildZhuantiTopicParams,
  fetchBanhuiResourcePage,
  fetchZhuantiResourcePage,
  fetchZhuantiSideLists,
  topicItemToPrimaryShape,
  resolveChannelDetailPath,
  CHANNEL_ZHUANTI,
} from '@/composables/useChannelResourceList'
import type { TopicResourceItem } from '@/api/topic'

interface HotDownloadItem {
  id: number
  title: string
  meta: string
  icon: string
  downloadCount: number
  isFree: boolean
}

interface LatestUploadItem {
  id: number
  title: string
  tag: string
  format: string
  pageCount: number
  uploadTime: string
  downloadCount: number
  isFree: boolean
  icon: string
}

export const banhuiThemes = [
  { key: 'meeting', name: '主题班会', icon: '📝', count: 86 },
  { key: 'parent', name: '家长会', icon: '👨‍👩‍👧', count: 42 },
  { key: 'safety', name: '安全教育', icon: '🛡️', count: 68 },
  { key: 'mental', name: '心理健康', icon: '💚', count: 55 },
  { key: 'moral', name: '品德培养', icon: '🌟', count: 63 },
  { key: 'deyu', name: '德育教育', icon: '📖', count: 71 },
  { key: 'habit', name: '行为习惯', icon: '✨', count: 38 },
  { key: 'festival', name: '节日主题', icon: '🎉', count: 94 },
  { key: 'term', name: '开学/毕业', icon: '🎓', count: 47 },
  { key: 'class', name: '班级管理', icon: '👥', count: 32 },
  { key: 'law', name: '法治教育', icon: '⚖️', count: 29 },
  { key: 'elite', name: '精品专辑', icon: '🏅', count: 18 },
]

export const hotDownloadTabs = [
  { key: 'week', name: '本周热门' },
  { key: 'month', name: '本月热门' },
  { key: 'all', name: '总榜' },
]

const coverColors = [
  'linear-gradient(135deg, #667eea, #764ba2)',
  'linear-gradient(135deg, #f093fb, #f5576c)',
  'linear-gradient(135deg, #4facfe, #00f2fe)',
  'linear-gradient(135deg, #43e97b, #38f9d7)',
  'linear-gradient(135deg, #fa709a, #fee140)',
  'linear-gradient(135deg, #a18cd1, #fbc2eb)',
]

export function useFeatureChannel() {
  const route = useRoute()
  const router = useRouter()
  const resourceStore = useResourceStore()

  const channelType = computed(() => (route.params.type as string) || 'banhui')
  const isCompetitionChannel = computed(() => channelType.value === COMPETITION_CHANNEL)
  const isCanonicalChannel = computed(() => isCanonicalListChannel(channelType.value))
  const apiChannelInfo = ref<Record<string, unknown> | null>(null)
  const apiTabKeywords = ref<Record<string, string>>({})

  const channelList = ref<PrimaryChineseItem[]>([])
  const channelTotal = ref(0)
  const channelLoading = ref(false)

  const channelInfo = computed(() => {
    const staticConfig = channelConfigs[channelType.value] || channelConfigs.banhui
    if (!apiChannelInfo.value) return staticConfig
    return { ...staticConfig, ...apiChannelInfo.value }
  })
  const effectiveTabKeywords = computed(() => ({
    ...TAB_KEYWORDS,
    ...apiTabKeywords.value,
  }))
  const loading = computed(() =>
    isCanonicalChannel.value ? channelLoading.value : resourceStore.loading,
  )
  const total = computed(() =>
    isCanonicalChannel.value ? channelTotal.value : resourceStore.total,
  )
  const resources = computed(() =>
    isCanonicalChannel.value ? channelList.value : resourceStore.list,
  )

  const mainTabs = computed(() => channelInfo.value.mainTabs || [])
  const activeMainTab = ref('all')
  const eliteAlbums = computed(() => channelInfo.value.eliteAlbums || [])
  const stats = computed(() => ({
    elite: channelInfo.value.stats?.elite || 0,
    free: channelInfo.value.stats?.free || 0,
  }))

  const gradeFilters = computed(() =>
    isCompetitionChannel.value
      ? [...COMPETITION_GRADE_STAGES]
      : [
          { key: 'all', name: '不限' }, { key: 'primary', name: '小学' },
          { key: 'junior', name: '初中' }, { key: 'senior', name: '高中' },
        ]
  )
  const levelFilters = computed(() =>
    isCompetitionChannel.value
      ? COMPETITION_LEVELS.map(({ key, name }) => ({ key, name }))
      : [
          { key: 'all', name: '不限' }, { key: 'free', name: '免费' },
          { key: 'elite', name: '精品' }, { key: 'vip', name: '特供' },
        ]
  )
  const formatFilters = computed(() =>
    isCompetitionChannel.value
      ? COMPETITION_FORMATS.map(({ key, name }) => ({ key, name }))
      : [
          { key: 'all', name: '不限' }, { key: 'ppt', name: 'PPT' },
          { key: 'word', name: 'Word' }, { key: 'pdf', name: 'PDF' }, { key: 'video', name: '视频' },
        ]
  )
  const subjectFilters = computed(() =>
    isCompetitionChannel.value
      ? COMPETITION_SUBJECTS.map(({ key, name }) => ({ key, name }))
      : [
          { key: 'all', name: '不限' }, { key: 'math', name: '数学' },
          { key: 'physics', name: '物理' }, { key: 'chemistry', name: '化学' },
          { key: 'info', name: '信息学' }, { key: 'writing', name: '语文' }, { key: 'english', name: '英语' },
        ]
  )

  const selectedGrade = ref('all')
  const selectedLevel = ref('all')
  const selectedFormat = ref('all')
  const selectedSubject = ref('all')
  const sortBy = ref('newest')
  const currentPage = ref(1)
  const searchKeyword = ref('')

  const activeHotTab = ref('week')
  const hotDownloadItems = ref<HotDownloadItem[]>([])
  const latestUploads = ref<LatestUploadItem[]>([])

  function resolveBrowseKeyword() {
    if (activeMainTab.value !== 'all') {
      return effectiveTabKeywords.value[activeMainTab.value] || ''
    }
    return ''
  }

  function mapHotItem(item: PrimaryChineseItem): HotDownloadItem {
    const isFree = item.isFree === 1 || item.isFree === undefined
    return {
      id: item.id,
      title: item.title,
      meta: `${item.gradeName || '全学段'} · ${item.displayType || item.type || '课件'}`,
      icon: getResourceIcon(item.type),
      downloadCount: item.downloadCount ?? 0,
      isFree,
    }
  }

  function mapLatestItem(item: PrimaryChineseItem): LatestUploadItem {
    const isFree = item.isFree === 1 || item.isFree === undefined
    return {
      id: item.id,
      title: item.title,
      tag: item.module || item.subjectName || '特色资源',
      format: item.fileExt?.toUpperCase() || item.displayType || 'PPT',
      pageCount: item.fileSizeKb ? Math.max(1, Math.round(item.fileSizeKb / 30)) : 0,
      uploadTime: item.uploadTime?.slice(0, 10) || '最近',
      downloadCount: item.downloadCount ?? 0,
      isFree,
      icon: getResourceIcon(item.type),
    }
  }

  function buildListQuery(sortField: string) {
    return {
      tabKey: activeMainTab.value,
      tabKeywords: effectiveTabKeywords.value,
      grade: selectedGrade.value,
      subject: selectedSubject.value,
      format: selectedFormat.value,
      level: selectedLevel.value,
      keyword: searchKeyword.value,
      current: currentPage.value,
      size: 12,
      sortField,
      sortOrder: 'desc' as const,
    }
  }

  function mapTopicHotItem(item: TopicResourceItem): HotDownloadItem {
    const shaped = topicItemToPrimaryShape(item)
    return mapHotItem(shaped)
  }

  function mapTopicLatestItem(item: TopicResourceItem): LatestUploadItem {
    const shaped = topicItemToPrimaryShape(item)
    return mapLatestItem(shaped)
  }

  async function loadHotDownloads() {
    if (isCompetitionChannel.value) {
      await resourceStore.searchResources(
        buildCompetitionListParams({
          tabKey: activeMainTab.value,
          tabKeywords: effectiveTabKeywords.value,
          grade: selectedGrade.value,
          subject: selectedSubject.value,
          format: selectedFormat.value,
          level: selectedLevel.value,
          keyword: searchKeyword.value,
          current: 1,
          size: 5,
          sortField: 'downloadCount',
          sortOrder: 'desc',
        }) as Parameters<typeof resourceStore.searchResources>[0],
      )
      hotDownloadItems.value = resourceStore.list.slice(0, 5).map(mapHotItem)
      return
    }

    if (channelType.value === CHANNEL_ZHUANTI) {
      try {
        const { hot } = await fetchZhuantiSideLists(5)
        hotDownloadItems.value = hot.map(mapTopicHotItem)
      } catch {
        hotDownloadItems.value = []
      }
      return
    }

    try {
      const keyword = resolveBrowseKeyword()
      const params: Parameters<typeof browseApi.getPage>[0] = {
        keyword: keyword || undefined,
        current: 1,
        size: 5,
        sortField: 'downloadCount',
        sortOrder: 'desc',
      }
      if (channelType.value === 'banhui') {
        params.module = THEME_CLASS_MEETING_MODULE
      }
      const res = await browseApi.getPage(params, { silentError: true })
      const page = unwrapData(res)
      hotDownloadItems.value = (page.records || []).map(mapHotItem)
    } catch {
      hotDownloadItems.value = []
    }
  }

  async function loadLatestUploads() {
    if (isCompetitionChannel.value) {
      await resourceStore.searchResources(
        buildCompetitionListParams({
          tabKey: activeMainTab.value,
          tabKeywords: effectiveTabKeywords.value,
          grade: selectedGrade.value,
          subject: selectedSubject.value,
          format: selectedFormat.value,
          level: selectedLevel.value,
          keyword: searchKeyword.value,
          current: 1,
          size: 5,
          sortField: 'createTime',
          sortOrder: 'desc',
        }) as Parameters<typeof resourceStore.searchResources>[0],
      )
      latestUploads.value = resourceStore.list.slice(0, 5).map(mapLatestItem)
      return
    }

    if (channelType.value === CHANNEL_ZHUANTI) {
      try {
        const { latest } = await fetchZhuantiSideLists(5)
        latestUploads.value = latest.map(mapTopicLatestItem)
      } catch {
        latestUploads.value = []
      }
      return
    }

    try {
      const keyword = resolveBrowseKeyword()
      const params: Parameters<typeof browseApi.getPage>[0] = {
        keyword: keyword || undefined,
        current: 1,
        size: 5,
        sortField: 'createTime',
        sortOrder: 'desc',
      }
      if (channelType.value === 'banhui') {
        params.module = THEME_CLASS_MEETING_MODULE
      }
      const res = await browseApi.getPage(params, { silentError: true })
      const page = unwrapData(res)
      latestUploads.value = (page.records || []).map(mapLatestItem)
    } catch {
      latestUploads.value = []
    }
  }

  async function loadSideLists() {
    await Promise.all([loadHotDownloads(), loadLatestUploads()])
  }

  function switchMainTab(key: string) {
    activeMainTab.value = key
    currentPage.value = 1
    loadData()
  }

  function resolveSortField() {
    if (sortBy.value === 'downloads') return 'downloadCount'
    if (sortBy.value === 'rating') return 'rating'
    if (sortBy.value === 'hot') return 'viewCount'
    return 'createTime'
  }

  async function loadData() {
    const sortField = resolveSortField()

    if (isCompetitionChannel.value) {
      await resourceStore.searchResources(
        buildCompetitionListParams({
          tabKey: activeMainTab.value,
          tabKeywords: effectiveTabKeywords.value,
          grade: selectedGrade.value,
          subject: selectedSubject.value,
          format: selectedFormat.value,
          level: selectedLevel.value,
          keyword: searchKeyword.value,
          current: currentPage.value,
          size: 12,
          sortField,
          sortOrder: 'desc',
        }) as Parameters<typeof resourceStore.searchResources>[0]
      )
      return
    }

    if (channelType.value === 'banhui') {
      channelLoading.value = true
      try {
        const page = await fetchBanhuiResourcePage(
          buildBanhuiBrowseParams(buildListQuery(sortField)),
        )
        channelList.value = page.records
        channelTotal.value = page.total
      } catch {
        channelList.value = []
        channelTotal.value = 0
      } finally {
        channelLoading.value = false
      }
      return
    }

    if (channelType.value === CHANNEL_ZHUANTI) {
      channelLoading.value = true
      try {
        const page = await fetchZhuantiResourcePage(
          buildZhuantiTopicParams(buildListQuery(sortField)),
        )
        channelList.value = page.records.map(topicItemToPrimaryShape)
        channelTotal.value = page.total
      } catch {
        channelList.value = []
        channelTotal.value = 0
      } finally {
        channelLoading.value = false
      }
      return
    }

    let keyword = ''
    if (activeMainTab.value !== 'all') {
      keyword = effectiveTabKeywords.value[activeMainTab.value] || ''
    }

    await resourceStore.searchResources({
      keyword,
      current: currentPage.value,
      size: 12,
      sortField: sortField as 'createTime' | 'downloadCount' | 'viewCount' | 'rating',
      sortOrder: 'desc',
      gradeLevel: selectedGrade.value !== 'all' ? selectedGrade.value : undefined,
      subject: selectedSubject.value !== 'all' ? selectedSubject.value : undefined,
    })
  }

  function goToDetail(item: { id: number }) {
    if (isCanonicalChannel.value) {
      router.push(resolveChannelDetailPath(channelType.value, item.id))
      return
    }
    router.push(`/resource/${item.id}/feature`)
  }

  function getCoverColor(id: number) {
    return coverColors[(id - 1) % coverColors.length]
  }

  function getResourceIcon(type?: string) {
    const map: Record<string, string> = {
      courseware: '📊', lesson_plan: '📋', exam: '📝', video: '🎬', document: '📄', material: '📑',
    }
    return map[type || ''] || '📄'
  }

  function getTypeName(type?: string) {
    const map: Record<string, string> = {
      courseware: '课件', lesson_plan: '教案', exam: '试卷', video: '视频', document: '文档', material: '讲义',
    }
    return map[type || ''] || '课件'
  }

  function formatCount(count: number): string {
    if (!count) return '0'
    return count >= 10000 ? `${(count / 10000).toFixed(1)}万` : String(count)
  }

  async function loadChannelConfig() {
    apiChannelInfo.value = null
    apiTabKeywords.value = {}
    if (!USE_HOME_OPS_API) return
    try {
      const data = await fetchChannelBootstrap(channelType.value)
      if (!data) return
      apiChannelInfo.value = {
        name: data.name,
        icon: data.icon,
        desc: data.desc,
        bgColor: data.bgColor,
        stats: data.stats,
        showGradeFilter: data.showGradeFilter,
        showSubjectFilter: data.showSubjectFilter,
        eliteTitle: data.eliteTitle,
        eliteDesc: data.eliteDesc,
        mainTabs: data.mainTabs?.map((t) => ({ key: t.key, name: t.name, icon: t.icon })) ?? [],
        eliteAlbums: data.eliteAlbums?.map((a) => ({
          id: a.id,
          title: a.title,
          icon: a.icon,
          meta: a.meta,
          resourceCount: a.resourceCount,
          downloadCount: a.downloadCount,
          coverColor: a.coverColor,
        })) ?? [],
      }
      apiTabKeywords.value = data.tabKeywords ?? {}
    } catch {
      /* 保留静态 channelConfigs */
    }
  }

  watch(
    () => route.params.type,
    () => {
      activeMainTab.value = 'all'
      selectedGrade.value = 'all'
      selectedLevel.value = 'all'
      selectedFormat.value = 'all'
      selectedSubject.value = 'all'
      searchKeyword.value = ''
      currentPage.value = 1
      loadChannelConfig().then(() => Promise.all([loadData(), loadSideLists()]))
    },
    { immediate: true }
  )

  watch([selectedGrade, selectedLevel, selectedFormat, selectedSubject, sortBy, currentPage], () => {
    loadData()
  })

  watch(activeHotTab, () => {
    loadHotDownloads()
  })

  watch(activeMainTab, () => {
    loadSideLists()
  })

  return {
    channelType,
    isCompetitionChannel,
    channelInfo,
    loading,
    total,
    resources,
    mainTabs,
    activeMainTab,
    eliteAlbums,
    stats,
    gradeFilters,
    levelFilters,
    formatFilters,
    subjectFilters,
    selectedGrade,
    selectedLevel,
    selectedFormat,
    selectedSubject,
    sortBy,
    currentPage,
    searchKeyword,
    activeHotTab,
    hotDownloadItems,
    latestUploads,
    switchMainTab,
    loadData,
    goToDetail,
    getCoverColor,
    getResourceIcon,
    getTypeName,
    formatCount,
  }
}
