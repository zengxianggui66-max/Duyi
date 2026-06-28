import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { collectApi, type CollectItem } from '@/api/collect'
import { unwrapData } from '@/api/request'
import { useUserStore } from '@/store'
import {
  MY_RESOURCE_SECTIONS,
  getMyResourceSubjects,
  getMyResourceSubjectNavTitle,
  getMyResourceTypeTabs,
  resolveSubjectApiName,
  resolveTypeDisplayName,
  isSpecialtySection,
  type MyResourceSection,
} from '@/constants/myResources'

function formatDate(raw?: string): string {
  if (!raw) return '-'
  return raw.slice(0, 10)
}

function formatExt(ext?: string): string {
  if (!ext) return '文件'
  return ext.replace(/^\./, '').toUpperCase()
}

export function useMyCollections() {
  const userStore = useUserStore()

  const activeSection = ref<MyResourceSection>('primary')
  const activeSubject = ref('')
  const activeType = ref('all')
  const currentPage = ref(1)
  const pageSize = ref(20)
  const loading = ref(false)
  const total = ref(0)
  const items = ref<CollectItem[]>([])
  const collectStats = ref({ total: 0, primaryCount: 0, juniorCount: 0, seniorCount: 0, artCount: 0, danceCount: 0 })
  const searchKeyword = ref('')
  const sortOrder = ref<'desc' | 'asc'>('desc')

  const sectionConfig = MY_RESOURCE_SECTIONS
  const currentSubjects = computed(() => getMyResourceSubjects(activeSection.value))
  const subjectNavTitle = computed(() => getMyResourceSubjectNavTitle(activeSection.value))
  const currentResourceTypes = computed(() => getMyResourceTypeTabs(activeSection.value))

  async function fetchStats() {
    if (!userStore.isLoggedIn) return
    try {
      const res = await collectApi.getStats()
      collectStats.value = unwrapData(res) || { total: 0, primaryCount: 0, juniorCount: 0, seniorCount: 0, artCount: 0, danceCount: 0 }
    } catch {
      collectStats.value = { total: 0, primaryCount: 0, juniorCount: 0, seniorCount: 0, artCount: 0, danceCount: 0 }
    }
  }

  async function fetchList() {
    if (!userStore.isLoggedIn) {
      items.value = []
      total.value = 0
      return
    }
    loading.value = true
    try {
      const params: Record<string, unknown> = {
        stageKey: activeSection.value,
        current: currentPage.value,
        size: pageSize.value,
      }
      if (activeSubject.value) {
        params.subject = resolveSubjectApiName(activeSection.value, activeSubject.value)
      }
      if (activeType.value !== 'all') {
        params.teachingType = activeType.value
      }
      if (searchKeyword.value) {
        params.keyword = searchKeyword.value
      }
      params.sort = sortOrder.value
      const res = await collectApi.getPage(params)
      const page = unwrapData(res)
      items.value = page?.records || []
      total.value = page?.total ?? 0
    } catch {
      items.value = []
      total.value = 0
      ElMessage.error('加载收藏失败')
    } finally {
      loading.value = false
    }
  }

  function selectSection(key: MyResourceSection) {
    if (activeSection.value === key) return
    activeSection.value = key
    activeSubject.value = ''
    activeType.value = 'all'
    currentPage.value = 1
    void fetchList()
  }

  function selectSubject(key: string) {
    activeSubject.value = key
    activeType.value = 'all'
    currentPage.value = 1
    void fetchList()
  }

  function selectType(key: string) {
    activeType.value = key
    currentPage.value = 1
    void fetchList()
  }

  function getTypeCount(typeKey: string): number {
    if (typeKey === 'all') return total.value
    return items.value.filter((r) => (r.teachingType || '') === typeKey).length
  }

  async function handleUncollect(item: CollectItem) {
    try {
      await ElMessageBox.confirm(`确定取消收藏「${item.title}」？`, '提示', { type: 'warning' })
      await collectApi.uncollect(
        item.resourceId,
        (item.resourceType as 'resource' | 'primary_chinese') || 'primary_chinese',
      )
      ElMessage.success('已取消收藏')
      await fetchList()
      await fetchStats()
    } catch (e: unknown) {
      if (e !== 'cancel' && e !== 'close') {
        ElMessage.error('操作失败')
      }
    }
  }

  function handleDownload(item: CollectItem) {
    if (item.ossUrl) {
      window.open(item.ossUrl, '_blank')
    } else {
      ElMessage.info('暂无可下载链接')
    }
  }

  function setSearchKeyword(keyword: string) {
    searchKeyword.value = keyword
    currentPage.value = 1
    void fetchList()
  }

  function setSortOrder(order: 'desc' | 'asc') {
    sortOrder.value = order
    currentPage.value = 1
    void fetchList()
  }

  watch([currentPage, pageSize], () => {
    void fetchList()
  })

  onMounted(() => {
    void fetchStats()
    void fetchList()
  })

  return {
    activeSection,
    activeSubject,
    activeType,
    currentPage,
    pageSize,
    loading,
    total,
    items,
    collectStats,
    sectionConfig,
    currentSubjects,
    subjectNavTitle,
    currentResourceTypes,
    searchKeyword,
    sortOrder,
    selectSection,
    selectSubject,
    selectType,
    getTypeCount,
    fetchList,
    fetchStats,
    handleUncollect,
    handleDownload,
    setSearchKeyword,
    setSortOrder,
    formatDate,
    formatExt,
    resolveTypeDisplayName: (key: string) =>
      resolveTypeDisplayName(activeSection.value, key),
    userStore,
  }
}

