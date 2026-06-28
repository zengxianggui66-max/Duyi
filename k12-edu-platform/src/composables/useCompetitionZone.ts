import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useResourceStore } from '@/store'
import { competitionApi, type CompetitionResourceItem, type CompetitionPackageItem } from '@/api/competition'
import { resourceGateway } from '@/api/resourceGateway'
import { channelConfigs, TAB_KEYWORDS } from '@/constants/featureChannels'
import {
  COMPETITION_GRADE_STAGES,
  COMPETITION_SUBJECTS,
  COMPETITION_FORMATS,
  COMPETITION_LEVELS,
  buildCompetitionListParams,
} from '@/constants/competitionZone'

const channelInfo = channelConfigs.jingsai

/** 是否使用竞赛独立表 API（P3）；失败时回退主站 resource */
const USE_COMPETITION_API = true

export function useCompetitionZone() {
  const router = useRouter()
  const resourceStore = useResourceStore()

  const loading = ref(false)
  const keyword = ref('')
  const activeTab = ref('all')
  const activeGrade = ref('all')
  const activeSubject = ref('all')
  const activeFormat = ref('all')
  const activeLevel = ref('all')
  const currentPage = ref(1)
  const pageSize = 12

  const mainTabs = channelInfo.mainTabs || []
  const eliteAlbums = channelInfo.eliteAlbums || []

  const resources = ref<CompetitionResourceItem[]>([])
  const packages = ref<CompetitionPackageItem[]>([])
  const total = ref(0)
  const packageTotal = ref(0)
  const useDedicatedApi = ref(USE_COMPETITION_API)

  const categories = ref<{ key: string; name: string; icon: string }[]>([])
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
    gradeStages.value = [...COMPETITION_GRADE_STAGES]
    resourceForms.value = [...COMPETITION_FORMATS]
  }

  async function loadFilterOptions() {
    if (!useDedicatedApi.value) {
      applyStaticFilterOptions()
      return
    }
    try {
      const res = await competitionApi.getFilterOptions()
      const data = res.data.data
      categories.value = data?.categories || []
      gradeStages.value = data?.gradeStages || [...COMPETITION_GRADE_STAGES]
      resourceForms.value = data?.resourceForms || []
    } catch {
      applyStaticFilterOptions()
    }
  }

  async function loadPackages() {
    if (!useDedicatedApi.value) return
    try {
      const res = await competitionApi.listPackages({
        gradeStage: activeGrade.value === 'all' ? undefined : activeGrade.value,
        category: activeTab.value === 'all' || activeTab.value === 'elite' ? undefined : activeTab.value,
        keyword: keyword.value.trim() || undefined,
        current: 1,
        size: 4,
      })
      packages.value = res.data.data?.records || []
      packageTotal.value = res.data.data?.total || 0
    } catch {
      packages.value = eliteAlbums.map((a, i) => ({
        id: a.id,
        title: a.title,
        summary: a.meta,
        gradeStage: 'all',
        category: 'elite',
        icon: a.icon,
        resourceCount: a.resourceCount,
        downloadCount: a.downloadCount,
        isElite: 1,
        sort: i,
      }))
      packageTotal.value = packages.value.length
    }
  }

  async function loadFromDedicatedApi() {
    const { page } = await resourceGateway.listCompetitionResources({
      category: activeTab.value === 'all' || activeTab.value === 'elite' ? undefined : activeTab.value,
      gradeStage: activeGrade.value === 'all' ? undefined : activeGrade.value,
      subject:
        activeSubject.value === 'all'
          ? undefined
          : activeSubject.value === 'info'
            ? 'info'
            : activeSubject.value,
      resourceForm: activeFormat.value === 'all' ? undefined : activeFormat.value,
      keyword: keyword.value.trim() || undefined,
      isFree: activeLevel.value === 'free' ? 1 : activeLevel.value === 'paid' ? 0 : undefined,
      current: currentPage.value,
      size: pageSize,
      sortField: 'sort',
      sortOrder: 'desc',
    })
    resources.value = page?.records || []
    total.value = page?.total || 0
  }

  async function loadFromResourceApi() {
    await resourceStore.searchResources(
      buildCompetitionListParams({
        tabKey: activeTab.value,
        tabKeywords: TAB_KEYWORDS,
        grade: activeGrade.value,
        subject: activeSubject.value,
        format: activeFormat.value,
        level: activeLevel.value,
        keyword: keyword.value,
        current: currentPage.value,
        size: pageSize,
        sortField: 'downloadCount',
        sortOrder: 'desc',
      }) as Parameters<typeof resourceStore.searchResources>[0]
    )
    resources.value = (resourceStore.list || []).map((r) => ({
      id: r.id,
      title: r.title,
      summary: r.description,
      category: activeTab.value,
      gradeStage: r.gradeLevel || 'all',
      subject: r.subject,
      resourceForm: r.resourceType || 'document',
      fileFormat: r.fileFormat,
      icon: '📄',
      downloadCount: r.downloadCount,
      viewCount: r.viewCount,
      isFree: r.downloadCount !== undefined ? 1 : 1,
    }))
    total.value = resourceStore.total
  }

  async function ensureDedicatedApi() {
    if (!USE_COMPETITION_API) {
      useDedicatedApi.value = false
      return
    }
    try {
      await competitionApi.getFilterOptions()
      useDedicatedApi.value = true
    } catch {
      useDedicatedApi.value = false
      ElMessage.warning('竞赛专库暂不可用，已切换为主站资源检索')
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

  async function refreshAll() {
    loading.value = true
    try {
      if (useDedicatedApi.value) {
        await Promise.all([loadPackages(), loadResources()])
      } else {
        await loadResources()
        await loadPackages()
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
      router.push(`/competition-zone/resource/${item.id}`)
    } else {
      router.push(`/resource/${item.id}/feature`)
    }
  }

  const stats = computed(() => channelInfo.stats)

  watch([activeTab, activeGrade, activeSubject, activeFormat, activeLevel], () => {
    currentPage.value = 1
    refreshAll()
  })

  watch(currentPage, () => {
    loadResources()
  })

  onMounted(async () => {
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
    activeGrade,
    activeSubject,
    activeFormat,
    activeLevel,
    currentPage,
    pageSize,
    mainTabs,
    eliteAlbums,
    resources,
    packages,
    total,
    packageTotal,
    categories,
    gradeStages,
    resourceForms,
    channelInfo,
    stats,
    useDedicatedApi,
    formatCount,
    onSearch,
    openResource,
    refreshAll,
  }
}

