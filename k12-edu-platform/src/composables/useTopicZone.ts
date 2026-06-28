import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useResourceStore } from '@/store'
import {
  topicApi,
  type TopicResourceItem,
  type TopicAlbumItem,
  type TopicZoneStats,
  type TopicCalendarHint,
  type TopicHotKeyword,
} from '@/api/topic'
import { resourceGateway } from '@/api/resourceGateway'
import { channelConfigs, TAB_KEYWORDS } from '@/constants/featureChannels'
import {
  TOPIC_TAB_KEYWORDS,
  TOPIC_GRADE_STAGES,
  TOPIC_FORMATS,
  TOPIC_FORMAT_API_MAP,
  buildTopicListParams,
} from '@/constants/topicZone'

const channelInfo = channelConfigs.zhuanti

const tabKeywords = { ...TAB_KEYWORDS, ...TOPIC_TAB_KEYWORDS }

const USE_TOPIC_API = true

export function useTopicZone() {
  const route = useRoute()
  const router = useRouter()
  const resourceStore = useResourceStore()

  const loading = ref(false)
  const keyword = ref('')
  const activeTab = ref('all')
  const activeRegion = ref('all')
  const activeGrade = ref('all')
  const activeSubject = ref('all')
  const activeFormat = ref('all')
  const activeLevel = ref('all')
  const sortBy = ref<'sort' | 'downloadCount' | 'createTime'>('sort')
  const currentPage = ref(1)
  const pageSize = 12

  const zoneStats = ref<TopicZoneStats | null>(null)
  const calendarHint = ref<TopicCalendarHint | null>(null)
  const hotKeywords = ref<TopicHotKeyword[]>([])
  const hotResources = ref<TopicResourceItem[]>([])
  const latestResources = ref<TopicResourceItem[]>([])

  const mainTabs = channelInfo.mainTabs || []
  const eliteAlbums = channelInfo.eliteAlbums || []

  const resources = ref<TopicResourceItem[]>([])
  const albums = ref<TopicAlbumItem[]>([])
  const total = ref(0)
  const albumTotal = ref(0)
  const useDedicatedApi = ref(USE_TOPIC_API)

  const categories = ref<{ key: string; name: string; icon: string }[]>([])
  const regions = ref<{ key: string; name: string }[]>([])
  const gradeStages = ref<{ key: string; name: string }[]>([])
  const resourceForms = ref<{ key: string; name: string }[]>([])

  function formatCount(count?: number): string {
    if (!count) return '0'
    return count >= 10000 ? `${(count / 10000).toFixed(1)}万` : String(count)
  }

  function applyStaticFilterOptions() {
    categories.value = mainTabs.filter((t) => t.key !== 'elite').map((t) => ({
      key: t.key,
      name: t.name,
      icon: t.icon,
    }))
    gradeStages.value = [...TOPIC_GRADE_STAGES]
    resourceForms.value = TOPIC_FORMATS.filter((f) => f.key !== 'all').map((f) => ({
      key: f.key,
      name: f.name,
    }))
    regions.value = [
      { key: 'all', name: '不限' },
      { key: 'chengdu', name: '成都' },
      { key: 'mianyang', name: '绵阳' },
      { key: 'sichuan', name: '四川其他' },
    ]
  }

  async function loadFilterOptions() {
    if (!useDedicatedApi.value) {
      applyStaticFilterOptions()
      return
    }
    try {
      const res = await topicApi.getFilterOptions()
      const data = res.data.data
      categories.value = data?.categories?.filter((c) => c.key !== 'elite') || []
      regions.value = data?.regions || []
      gradeStages.value = data?.gradeStages || [...TOPIC_GRADE_STAGES]
      resourceForms.value = data?.resourceForms?.filter((f) => f.key !== 'all') || []
    } catch {
      applyStaticFilterOptions()
    }
  }

  async function loadAlbums() {
    if (!useDedicatedApi.value) {
      albums.value = eliteAlbums.map((a, i) => ({
        id: a.id,
        title: a.title,
        summary: a.meta,
        region: 'all',
        gradeStage: 'all',
        category: 'elite',
        icon: a.icon,
        resourceCount: a.resourceCount,
        downloadCount: a.downloadCount,
        isElite: 1,
      }))
      albumTotal.value = albums.value.length
      return
    }
    try {
      const res = await topicApi.listAlbums({
        region: activeRegion.value === 'all' ? undefined : activeRegion.value,
        category: activeTab.value === 'all' || activeTab.value === 'elite' ? undefined : activeTab.value,
        gradeStage: activeGrade.value === 'all' ? undefined : activeGrade.value,
        keyword: keyword.value.trim() || undefined,
        current: 1,
        size: 4,
      })
      albums.value = res.data.data?.records || []
      albumTotal.value = res.data.data?.total || 0
    } catch {
      albums.value = eliteAlbums.map((a) => ({
        id: a.id,
        title: a.title,
        summary: a.meta,
        region: 'all',
        gradeStage: 'all',
        category: 'elite',
        icon: a.icon,
        resourceCount: a.resourceCount,
        downloadCount: a.downloadCount,
        isElite: 1,
      }))
      albumTotal.value = albums.value.length
    }
  }

  async function loadFromDedicatedApi() {
    const { page } = await resourceGateway.listTopicResources({
      category: activeTab.value === 'all' || activeTab.value === 'elite' ? undefined : activeTab.value,
      region: activeRegion.value === 'all' ? undefined : activeRegion.value,
      gradeStage: activeGrade.value === 'all' ? undefined : activeGrade.value,
      subject: activeSubject.value === 'all' ? undefined : activeSubject.value,
      resourceForm:
        activeFormat.value === 'all' ? undefined : TOPIC_FORMAT_API_MAP[activeFormat.value] || activeFormat.value,
      keyword: keyword.value.trim() || undefined,
      isFree: activeLevel.value === 'free' ? 1 : activeLevel.value === 'paid' ? 0 : undefined,
      current: currentPage.value,
      size: pageSize,
      sortField: sortBy.value,
      sortOrder: 'desc',
    })
    resources.value = page?.records || []
    total.value = page?.total || 0
  }

  async function loadFromResourceApi() {
    await resourceStore.searchResources(
      buildTopicListParams({
        tabKey: activeTab.value,
        tabKeywords,
        region: activeRegion.value,
        grade: activeGrade.value,
        subject: activeSubject.value,
        format: activeFormat.value,
        level: activeLevel.value,
        keyword: keyword.value,
        current: currentPage.value,
        size: pageSize,
        sortField: sortBy.value === 'createTime' ? 'createTime' : 'downloadCount',
        sortOrder: 'desc',
      }) as Parameters<typeof resourceStore.searchResources>[0]
    )
    resources.value = (resourceStore.list || []).map((r) => ({
      id: r.id,
      title: r.title,
      summary: r.description,
      category: activeTab.value,
      region: activeRegion.value,
      gradeStage: r.gradeLevel || 'all',
      subject: r.subject,
      resourceForm: r.resourceType || 'material',
      fileFormat: r.fileFormat,
      icon: '📚',
      downloadCount: r.downloadCount,
      viewCount: r.viewCount,
      isFree: 1,
    }))
    total.value = resourceStore.total
  }

  async function ensureDedicatedApi() {
    if (!USE_TOPIC_API) {
      useDedicatedApi.value = false
      return
    }
    try {
      await topicApi.getFilterOptions()
      useDedicatedApi.value = true
    } catch {
      useDedicatedApi.value = false
      ElMessage.warning('专题专库暂不可用，已切换为主站资源检索')
    }
  }

  async function loadResources() {
    if (useDedicatedApi.value) {
      try {
        await loadFromDedicatedApi()
        return
      } catch {
        useDedicatedApi.value = false
      }
    }
    await loadFromResourceApi()
  }

  async function loadP3Data() {
    if (!useDedicatedApi.value) return
    try {
      const region = activeRegion.value === 'all' ? undefined : activeRegion.value
      const [statsRes, hintRes, kwRes, hotRes, latestRes] = await Promise.all([
        topicApi.getStats(),
        topicApi.getCalendarHint(),
        topicApi.getHotKeywords(8),
        topicApi.listHotResources({ limit: 6, region }),
        topicApi.listLatestResources({ limit: 6, region }),
      ])
      zoneStats.value = statsRes.data.data || null
      calendarHint.value = hintRes.data.data || null
      hotKeywords.value = kwRes.data.data || []
      hotResources.value = hotRes.data.data || []
      latestResources.value = latestRes.data.data || []
    } catch {
      zoneStats.value = null
    }
  }

  function applyCalendarHint() {
    if (!calendarHint.value) return
    activeTab.value = calendarHint.value.category
    currentPage.value = 1
    refreshAll()
  }

  function applyHotKeyword(kw: string) {
    keyword.value = kw
    onSearch()
  }

  const displayStats = computed(() => {
    if (zoneStats.value) {
      return {
        total: String(zoneStats.value.total),
        elite: String(zoneStats.value.elite),
        free: String(zoneStats.value.free),
      }
    }
    return channelInfo.stats || { total: '0', elite: '0', free: '0' }
  })

  async function refreshAll() {
    loading.value = true
    try {
      if (useDedicatedApi.value) {
        await Promise.all([loadAlbums(), loadResources(), loadP3Data()])
      } else {
        await loadResources()
        await loadAlbums()
      }
    } catch (e: unknown) {
      const err = e as { message?: string }
      ElMessage.error(err.message || '加载失败')
    } finally {
      loading.value = false
    }
  }

  function onSearch() {
    currentPage.value = 1
    refreshAll()
  }

  function openResource(item: { id: number }) {
    if (useDedicatedApi.value) {
      router.push(`/topic-zone/resource/${item.id}`)
    } else {
      router.push(`/resource/${item.id}/feature`)
    }
  }

  function applyRouteQuery() {
    const q = route.query
    if (typeof q.keyword === 'string' && q.keyword.trim()) {
      keyword.value = q.keyword.trim()
    }
    if (typeof q.tab === 'string' && mainTabs.some((t) => t.key === q.tab)) {
      activeTab.value = q.tab
    }
    if (typeof q.region === 'string' && ['chengdu', 'mianyang', 'sichuan'].includes(q.region)) {
      activeRegion.value = q.region
    }
  }

  watch([activeTab, activeRegion, activeGrade, activeSubject, activeFormat, activeLevel, sortBy], () => {
    currentPage.value = 1
    refreshAll()
  })

  watch(activeRegion, () => {
    if (useDedicatedApi.value) loadP3Data()
  })

  watch(currentPage, () => {
    loadResources()
  })

  watch(
    () => route.query,
    () => {
      applyRouteQuery()
      refreshAll()
    }
  )

  onMounted(async () => {
    applyRouteQuery()
    loading.value = true
    try {
      await ensureDedicatedApi()
      await loadFilterOptions()
      await refreshAll()
    } finally {
      loading.value = false
    }
  })

  return {
    loading,
    keyword,
    activeTab,
    activeRegion,
    activeGrade,
    activeSubject,
    activeFormat,
    activeLevel,
    sortBy,
    currentPage,
    pageSize,
    mainTabs,
    eliteAlbums,
    resources,
    albums,
    total,
    albumTotal,
    categories,
    regions,
    gradeStages,
    resourceForms,
    zoneStats,
    calendarHint,
    hotKeywords,
    hotResources,
    latestResources,
    displayStats,
    channelInfo,
    useDedicatedApi,
    formatCount,
    onSearch,
    openResource,
    refreshAll,
    applyCalendarHint,
    applyHotKeyword,
  }
}
