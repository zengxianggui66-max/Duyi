import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { viewApi, type ViewItem } from '@/api/view'
import { unwrapData } from '@/api/request'
import { useUserStore } from '@/store'
import {
  MY_RESOURCE_SECTIONS,
  getMyResourceSubjects,
  type MyResourceSection,
} from '@/constants/myResources'

export interface ViewListItem {
  id: number
  resourceId: number
  title: string
  subject?: string
  stageKey?: string
  stage?: string
  gradeName?: string
  teachingType?: string
  fileExt?: string
  ossUrl?: string
  detailUrl?: string
  time: string
}

function mapRecord(row: ViewItem): ViewListItem {
  return {
    id: row.id,
    resourceId: row.resourceId,
    title: row.title,
    subject: row.subject,
    stageKey: row.stageKey,
    stage: row.stage,
    gradeName: row.gradeName,
    teachingType: row.teachingType,
    fileExt: row.fileExt,
    ossUrl: row.ossUrl,
    detailUrl: row.detailUrl,
    time: row.updateTime || row.createTime || '',
  }
}

function resolveSubjectName(section: MyResourceSection | 'all', subjectKey: string): string {
  if (!subjectKey || section === 'all') return subjectKey
  const list = getMyResourceSubjects(section as MyResourceSection)
  return list.find((s) => s.key === subjectKey)?.name || subjectKey
}

export function useMyViews() {
  const userStore = useUserStore()
  const loading = ref(false)
  const activeSection = ref<MyResourceSection | 'all'>('all')
  const activeSubject = ref('')
  const activeType = ref('all')
  const searchKeyword = ref('')
  const sortOrder = ref<'desc' | 'asc'>('desc')
  const currentPage = ref(1)
  const pageSize = ref(20)
  const total = ref(0)
  const items = ref<ViewListItem[]>([])
  const viewStats = ref({ total: 0, weekCount: 0, todayCount: 0 })

  const sectionConfig = MY_RESOURCE_SECTIONS

  const currentSubjects = computed(() => {
    if (activeSection.value === 'all') return []
    return getMyResourceSubjects(activeSection.value as MyResourceSection)
  })

  const currentTypes = computed(() => {
    const types = new Set<string>()
    items.value.forEach((item) => {
      if (item.teachingType) types.add(item.teachingType)
    })
    return Array.from(types)
  })

  async function fetchStats() {
    if (!userStore.isLoggedIn) {
      viewStats.value = { total: 0, weekCount: 0, todayCount: 0 }
      return
    }
    try {
      const res = await viewApi.getStats()
      viewStats.value = unwrapData(res) || { total: 0, weekCount: 0, todayCount: 0 }
    } catch {
      viewStats.value = { total: 0, weekCount: 0, todayCount: 0 }
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
      const subject = resolveSubjectName(activeSection.value, activeSubject.value)
      const res = await viewApi.getPage({
        current: currentPage.value,
        size: pageSize.value,
        keyword: searchKeyword.value || undefined,
        stageKey: activeSection.value === 'all' ? undefined : activeSection.value,
        subject: subject || undefined,
        teachingType: activeType.value === 'all' ? undefined : activeType.value,
      })
      const page = unwrapData(res)
      let records = (page?.records || []).map(mapRecord)
      if (sortOrder.value === 'asc') {
        records = [...records].reverse()
      }
      items.value = records
      total.value = page?.total ?? 0
    } catch {
      items.value = []
      total.value = 0
      ElMessage.error('加载浏览记录失败')
    } finally {
      loading.value = false
    }
  }

  function selectSection(key: string) {
    activeSection.value = key as MyResourceSection | 'all'
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

  function setSearchKeyword(kw: string) {
    searchKeyword.value = kw
    currentPage.value = 1
    void fetchList()
  }

  function setSortOrder(order: 'desc' | 'asc') {
    sortOrder.value = order
    void fetchList()
  }

  async function removeItem(id: number) {
    try {
      await viewApi.remove(id)
      ElMessage.success('已删除浏览记录')
      await fetchList()
      await fetchStats()
    } catch {
      ElMessage.error('删除失败')
    }
  }

  async function clearAll() {
    try {
      await viewApi.clearAll()
      ElMessage.success('已清空所有浏览记录')
      await fetchList()
      await fetchStats()
    } catch {
      ElMessage.error('操作失败')
    }
  }

  function fileIconClass(item: ViewListItem): string {
    const ext = (item.fileExt || '').toLowerCase()
    if (['ppt', 'pptx'].includes(ext)) return 'icon-ppt'
    if (['doc', 'docx'].includes(ext)) return 'icon-word'
    if (['pdf'].includes(ext)) return 'icon-pdf'
    if (['mp4', 'avi', 'mov', 'wmv'].includes(ext)) return 'icon-video'
    if (['xls', 'xlsx'].includes(ext)) return 'icon-excel'
    return 'icon-default'
  }

  function fileIconText(item: ViewListItem): string {
    const ext = (item.fileExt || '').toLowerCase()
    if (['ppt', 'pptx'].includes(ext)) return 'P'
    if (['doc', 'docx'].includes(ext)) return 'W'
    if (['pdf'].includes(ext)) return 'PDF'
    if (['mp4', 'avi', 'mov', 'wmv'].includes(ext)) return '▶'
    if (['xls', 'xlsx'].includes(ext)) return 'E'
    return '📄'
  }

  function teachingTypeTagType(type?: string): string {
    const map: Record<string, string> = {
      课件: 'warning',
      教案: 'primary',
      试卷: 'danger',
      练习: 'success',
      学案: 'info',
      视频: '',
    }
    return map[type || ''] || ''
  }

  function formatViewTime(isoString: string): string {
    if (!isoString) return ''
    const now = new Date()
    const date = new Date(isoString)
    const diffMs = now.getTime() - date.getTime()
    const diffMinutes = Math.floor(diffMs / 60000)
    const diffHours = Math.floor(diffMinutes / 60)
    const diffDays = Math.floor(diffHours / 24)

    if (diffMinutes < 1) return '刚刚'
    if (diffMinutes < 60) return `${diffMinutes} 分钟前`
    if (diffHours < 24) return `${diffHours} 小时前`
    if (diffDays === 1) return '昨天'
    if (diffDays < 7) return `${diffDays} 天前`
    return isoString.slice(0, 10)
  }

  function handleView(item: ViewListItem) {
    if (item.detailUrl) {
      window.open(item.detailUrl, '_blank')
      return
    }
    window.open(`/resource/detail/${item.resourceId}`, '_blank')
  }

  function handleDownload(item: ViewListItem) {
    if (item.ossUrl) {
      window.open(item.ossUrl, '_blank')
    } else {
      window.open(`/api/resource/download/${item.resourceId}`, '_blank')
    }
  }

  watch([currentPage, pageSize], () => {
    void fetchList()
  })

  watch(
    () => userStore.isLoggedIn,
    () => {
      void fetchStats()
      void fetchList()
    },
  )

  onMounted(() => {
    void fetchStats()
    void fetchList()
  })

  return {
    loading,
    activeSection,
    activeSubject,
    activeType,
    currentPage,
    pageSize,
    total,
    items,
    viewStats,
    sectionConfig,
    currentSubjects,
    currentTypes,
    searchKeyword,
    sortOrder,
    selectSection,
    selectSubject,
    selectType,
    setSearchKeyword,
    setSortOrder,
    removeItem,
    clearAll,
    formatViewTime,
    fileIconClass,
    fileIconText,
    teachingTypeTagType,
    handleView,
    handleDownload,
  }
}
