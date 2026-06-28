import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { cultureStudyApi, type CultureResourceItem, type CulturePackageItem } from '@/api/cultureStudy'
import { resourceGateway } from '@/api/resourceGateway'

export function useCultureStudy() {
  const loading = ref(false)
  const keyword = ref('')

  const activeCategory = ref('all')
  const activeRegion = ref('all')
  const activeDuration = ref('all')
  const activeKind = ref('all')

  const categories = ref<{ key: string; name: string; icon: string }[]>([])
  const regions = ref<{ key: string; name: string }[]>([])
  const durations = ref<{ key: string; name: string }[]>([])
  const resourceKinds = ref<{ key: string; name: string }[]>([])

  const packages = ref<CulturePackageItem[]>([])
  const platformResources = ref<CultureResourceItem[]>([])
  const externalLinks = ref<CultureResourceItem[]>([])

  const packageTotal = ref(0)
  const platformTotal = ref(0)
  const externalTotal = ref(0)

  async function loadFilterOptions() {
    const res = await cultureStudyApi.getFilterOptions()
    const data = res.data.data
    categories.value = data?.categories || []
    regions.value = data?.regions || []
    durations.value = data?.durations || []
    resourceKinds.value = data?.resourceKinds || []
  }

  function buildParams(extra: { resourceKind?: string; current?: number; size?: number }) {
    return {
      category: activeCategory.value === 'all' ? undefined : activeCategory.value,
      region: activeRegion.value === 'all' ? undefined : activeRegion.value,
      durationType: activeDuration.value === 'all' ? undefined : activeDuration.value,
      resourceKind: extra.resourceKind === 'all' ? undefined : extra.resourceKind,
      keyword: keyword.value.trim() || undefined,
      current: extra.current ?? 1,
      size: extra.size ?? 12,
      sortField: 'sort',
      sortOrder: 'desc',
    }
  }

  async function loadPackages() {
    const res = await cultureStudyApi.listPackages(buildParams({ current: 1, size: 8 }))
    packages.value = res.data.data?.records || []
    packageTotal.value = res.data.data?.total || 0
  }

  async function loadPlatformResources() {
    const { page } = await resourceGateway.listCultureResources(
      buildParams({ resourceKind: 'platform', current: 1, size: 12 })
    )
    platformResources.value = page?.records || []
    platformTotal.value = page?.total || 0
  }

  async function loadExternalLinks() {
    const { page } = await resourceGateway.listCultureResources(
      buildParams({ resourceKind: 'external', current: 1, size: 8 })
    )
    externalLinks.value = page?.records || []
    externalTotal.value = page?.total || 0
  }

  async function refreshAll() {
    loading.value = true
    try {
      await Promise.all([loadPackages(), loadPlatformResources(), loadExternalLinks()])
    } catch (e: unknown) {
      const err = e as { message?: string }
      ElMessage.error(err.message || '加载失败，请确认已执行数据库脚本 12_culture_study.sql')
    } finally {
      loading.value = false
    }
  }

  async function init() {
    loading.value = true
    try {
      await loadFilterOptions()
      await refreshAll()
    } finally {
      loading.value = false
    }
  }

  watch([activeCategory, activeRegion, activeDuration, activeKind], () => {
    refreshAll()
  })

  return {
    loading,
    keyword,
    activeCategory,
    activeRegion,
    activeDuration,
    activeKind,
    categories,
    regions,
    durations,
    resourceKinds,
    packages,
    platformResources,
    externalLinks,
    packageTotal,
    platformTotal,
    externalTotal,
    init,
    refreshAll,
    loadPackages,
    loadPlatformResources,
    loadExternalLinks,
  }
}
