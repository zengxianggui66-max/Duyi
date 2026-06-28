import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { primaryChineseApi } from '@/api/primaryChinese'
import type { PrimaryChineseItem, MyUploadStats } from '@/api/types'
import { unwrapData } from '@/api/request'
import { useUserStore } from '@/store'
import {
  MY_RESOURCE_SECTIONS,
  getMyResourceSubjects,
  getMyResourceSubjectNavTitle,
  getMyResourceTypeTabs,
  sectionToApiStage,
  resolveSubjectApiName,
  resolveTypeDisplayName,
  buildMyResourceUploadLocation,
  isSpecialtySection,
  type MyResourceSection,
} from '@/constants/myResources'

export interface MyResourceListItem {
  id: number
  title: string
  type: string
  typeName: string
  format: string
  grade: string
  section: MyResourceSection
  subjectName: string
  module: string
  updateTime: string
  viewCount: number
  downloadCount: number
  isPublic: boolean
  status?: number
  auditStatus?: number
  publishStatus?: number
  ossUrl?: string
  rejectReason?: string
  rejectedAt?: string
  auditorName?: string
}

export type MyResourceStatusFilter = 'all' | 'draft' | 'pending' | 'approved' | 'published' | 'rejected' | 'offline'

export const MY_RESOURCE_STATUS_TABS: { key: MyResourceStatusFilter; label: string }[] = [
  { key: 'all', label: '全部' },
  { key: 'draft', label: '草稿箱' },
  { key: 'pending', label: '待审核' },
  { key: 'approved', label: '已通过' },
  { key: 'rejected', label: '未通过' },
  { key: 'published', label: '已发布' },
  { key: 'offline', label: '已下架' },
]

const STATUS_LABEL: Record<number, string> = {
  [-1]: '草稿',
  0: '待审核',
  1: '已发布',
  2: '未通过',
  3: '已下架',
}

const STATUS_FILTER_TO_CODE: Partial<Record<Exclude<MyResourceStatusFilter, 'all'>, number>> = {
  draft: -1,
  pending: 0,
  published: 1,
  rejected: 2,
  offline: 3,
}

function formatExt(ext?: string): string {
  if (!ext) return '文件'
  return ext.replace(/^\./, '').toUpperCase()
}

function formatDate(raw?: string): string {
  if (!raw) return '-'
  return raw.slice(0, 10)
}

function mapRecord(row: PrimaryChineseItem, section: MyResourceSection): MyResourceListItem {
  return {
    id: row.id,
    title: row.title,
    type: row.type || '',
    typeName: row.type || '资源',
    format: formatExt(row.fileExt),
    grade: row.gradeName || sectionToApiStage(section),
    section,
    subjectName: row.subject || '',
    module: row.module || '',
    updateTime: formatDate(row.uploadTime),
    viewCount: row.viewCount ?? 0,
    downloadCount: row.downloadCount ?? 0,
    isPublic:
      row.auditStatus === 1 && row.publishStatus === 1
        ? true
        : row.status === 1 && row.publishStatus !== 0,
    status: row.status,
    auditStatus: row.auditStatus,
    publishStatus: row.publishStatus,
    ossUrl: row.ossUrl,
  }
}

function formatRejectTime(raw?: string): string {
  if (!raw) return ''
  return raw.replace('T', ' ').slice(0, 16)
}

async function attachRejectReasons(items: MyResourceListItem[]) {
  const rejected = items.filter((r) => r.status === 2)
  if (rejected.length === 0) return
  await Promise.all(
    rejected.map(async (item) => {
      try {
        const res = await primaryChineseApi.getAuditInfo(item.id)
        const info = unwrapData(res)
        if (!info) return
        item.rejectReason = info.reason
        item.rejectedAt = formatRejectTime(info.rejectedAt)
        item.auditorName = info.auditorName
      } catch {
        /* ignore */
      }
    }),
  )
}

export function useMyResources(options?: { pageSize?: number }) {
  const router = useRouter()
  const route = useRoute()
  const userStore = useUserStore()
  const defaultPageSize = options?.pageSize ?? 20

  const activeSection = ref<MyResourceSection>('primary')
  const activeSubject = ref('')
  const activeType = ref('all')
  const activeStatusFilter = ref<MyResourceStatusFilter>('all')
  const searchKeyword = ref('')
  const currentPage = ref(1)
  const pageSize = ref(defaultPageSize)
  const loading = ref(false)
  const total = ref(0)
  const listItems = ref<MyResourceListItem[]>([])
  const uploadStats = ref<MyUploadStats>({
    total: 0,
    published: 0,
    pending: 0,
    draft: 0,
    rejected: 0,
    offline: 0,
    totalViews: 0,
    totalDownloads: 0,
  })

  const sectionConfig = MY_RESOURCE_SECTIONS
  const statusTabs = MY_RESOURCE_STATUS_TABS
  const currentSubjects = computed(() => getMyResourceSubjects(activeSection.value))
  const subjectNavTitle = computed(() => getMyResourceSubjectNavTitle(activeSection.value))
  const currentResourceTypes = computed(() => getMyResourceTypeTabs(activeSection.value))

  const displayTotal = computed(() => total.value)

  async function fetchStats() {
    if (!userStore.isLoggedIn) return
    try {
      const res = await primaryChineseApi.getMyUploadStats()
      uploadStats.value = unwrapData(res) || uploadStats.value
    } catch {
      /* ignore */
    }
  }

  async function fetchList() {
    if (!userStore.isLoggedIn || !userStore.user?.id) {
      listItems.value = []
      total.value = 0
      return
    }

    loading.value = true
    try {
      const section = activeSection.value
      const params: Record<string, unknown> = {
        stage: sectionToApiStage(section),
        uploaderId: userStore.user.id,
        current: currentPage.value,
        size: pageSize.value,
        sortField: 'upload_time',
        sortOrder: 'desc',
      }
      if (activeStatusFilter.value === 'approved') {
        params.auditStatus = 1
        params.publishStatus = 0
      } else if (activeStatusFilter.value !== 'all') {
        params.status = STATUS_FILTER_TO_CODE[activeStatusFilter.value]
      }
      if (isSpecialtySection(section)) {
        params.subject = section === 'art' ? '美术' : '舞蹈'
      } else if (activeSubject.value) {
        params.subject = resolveSubjectApiName(section, activeSubject.value)
      }
      if (activeType.value !== 'all') {
        params.type = activeType.value
      }
      const kw = searchKeyword.value.trim()
      if (kw) {
        params.keyword = kw
      }
      if (activeSubject.value && isSpecialtySection(section)) {
        params.keyword = resolveSubjectApiName(section, activeSubject.value)
      }

      const res = await primaryChineseApi.getPage(params, { silentError: true })
      const pageData = unwrapData(res)
      const records = (pageData?.records || []) as PrimaryChineseItem[]

      listItems.value = records.map((r) => mapRecord(r, section))
      total.value = pageData?.total ?? records.length
      await attachRejectReasons(listItems.value)
    } catch {
      listItems.value = []
      total.value = 0
      ElMessage.error('加载我的资源失败')
    } finally {
      loading.value = false
    }
  }

  function getTypeCount(typeKey: string): number {
    if (typeKey === 'all') return total.value
    return listItems.value.filter((r) => r.type === typeKey).length
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

  function selectStatusFilter(key: MyResourceStatusFilter) {
    if (activeStatusFilter.value === key) return
    activeStatusFilter.value = key
    currentPage.value = 1
    router.replace({ path: route.path, query: { ...route.query, status: key === 'all' ? undefined : key } })
    void fetchList()
  }

  function setSearchKeyword(kw: string) {
    searchKeyword.value = kw
    currentPage.value = 1
    void fetchList()
  }

  function handleUpload() {
    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录后再上传资源')
      router.push({ name: 'Login', query: { redirect: '/profile?tab=upload' } })
      return
    }
    router.push(
      buildMyResourceUploadLocation({
        section: activeSection.value,
        subjectKey: activeSubject.value || undefined,
        typeKey: activeType.value !== 'all' ? activeType.value : undefined,
      }),
    )
  }

  function handleDownload(item: MyResourceListItem) {
    if (item.ossUrl) {
      window.open(item.ossUrl, '_blank')
      return
    }
    ElMessage.info('暂无可下载链接')
  }

  async function handleSubmitDraft(item: MyResourceListItem) {
    if (item.status !== -1) return
    try {
      await ElMessageBox.confirm(
        `确定将草稿「${item.title}」提交审核吗？提交后不可修改。`,
        '提交审核',
        { confirmButtonText: '提交', cancelButtonText: '取消', type: 'info' },
      )
      await primaryChineseApi.submitDraft(item.id)
      ElMessage.success('已提交审核，请等待管理员审核')
      void fetchStats()
      void fetchList()
    } catch {
      /* cancel or error */
    }
  }

  async function handleWithdraw(item: MyResourceListItem) {
    if (item.status !== 0) return
    try {
      await ElMessageBox.confirm(
        `确定撤回「${item.title}」的审核申请吗？撤回后将变为草稿状态。`,
        '撤回确认',
        { confirmButtonText: '确认撤回', cancelButtonText: '取消', type: 'warning' },
      )
      await primaryChineseApi.withdrawPending(item.id)
      ElMessage.success('已撤回，请修改后重新提交')
      void fetchStats()
      void fetchList()
    } catch {
      /* cancel or error */
    }
  }

  function handleViewFront(item: MyResourceListItem) {
    if (!item.isPublic) {
      ElMessage.info('该资源尚未公开发布')
      return
    }
    const url = router.resolve({ path: `/resource/${item.id}` })
    window.open(url.href, '_blank')
  }

  function handleViewDetail(item: MyResourceListItem) {
    router.push({ path: `/resource/${item.id}` })
  }

  async function handleEdit(item: MyResourceListItem) {
    if (item.status === -1) {
      router.push({ path: '/upload', query: { draftId: String(item.id) } })
      return
    }
    if (item.status === 2) {
      const reason = item.rejectReason?.trim() || '管理员未填写具体原因'
      const auditor = item.auditorName ? `\n审核人：${item.auditorName}` : ''
      const time = item.rejectedAt ? `\n驳回时间：${item.rejectedAt}` : ''
      try {
        await ElMessageBox.confirm(
          `${reason}${auditor}${time}\n\n请根据原因修改后重新上传。`,
          '驳回原因',
          {
            confirmButtonText: '重新上传',
            cancelButtonText: '知道了',
            type: 'warning',
          },
        )
        await handleReupload(item)
      } catch {
        /* cancel */
      }
      return
    }
    ElMessage.info('已提交资源暂不支持在线编辑')
  }

  async function handleReupload(item: MyResourceListItem) {
    try {
      const res = await primaryChineseApi.cloneRejectedToDraft(item.id)
      const draft = unwrapData(res)
      if (!draft?.id) {
        ElMessage.warning('创建草稿失败，请稍后重试')
        return
      }
      ElMessage.success('已复制为新草稿，请修改后重新提交审核')
      router.push({ path: '/upload', query: { draftId: String(draft.id) } })
    } catch {
      ElMessage.error('重新上传失败，请稍后重试')
    }
  }

  async function handleDeleteDraft(item: MyResourceListItem) {
    if (item.status !== -1) return
    try {
      await ElMessageBox.confirm(`确定删除草稿「${item.title}」吗？`, '删除草稿', {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消',
      })
      await primaryChineseApi.deleteDraft(item.id)
      ElMessage.success('草稿已删除')
      void fetchStats()
      void fetchList()
    } catch {
      /* cancel or error */
    }
  }

  function handleMore(command: string, item: MyResourceListItem) {
    if (command === 'share') ElMessage.info('分享功能开发中')
    else if (command === 'delete') void handleDeleteDraft(item)
  }

  function handleExport(_command: string) {
    ElMessage.info('导出功能开发中')
  }

  function statusLabel(status?: number, auditStatus?: number, publishStatus?: number): string {
    return statusLabelByLifecycle(status, auditStatus, publishStatus)
  }

  function statusLabelByLifecycle(status?: number, auditStatus?: number, publishStatus?: number): string {
    if (auditStatus === 1 && publishStatus === 0) return '已通过'
    if (status == null) return ''
    return STATUS_LABEL[status] ?? ''
  }

  function isDraftItem(item: MyResourceListItem): boolean {
    return item.status === -1
  }

  function isRejectedItem(item: MyResourceListItem): boolean {
    return item.status === 2
  }

  function initStatusFromRoute() {
    const q = route.query.status
    if (typeof q !== 'string') return
    const valid: MyResourceStatusFilter[] = [
      'all',
      'draft',
      'pending',
      'approved',
      'published',
      'rejected',
      'offline',
    ]
    if (valid.includes(q as MyResourceStatusFilter)) {
      activeStatusFilter.value = q as MyResourceStatusFilter
    }
  }

  watch([currentPage, pageSize], () => {
    void fetchList()
  })

  watch(activeSubject, () => {
    if (!isSpecialtySection(activeSection.value)) {
      currentPage.value = 1
      void fetchList()
    }
  })

  onMounted(() => {
    initStatusFromRoute()
    void fetchStats()
    void fetchList()
  })

  watch(
    () => route.query.status,
    () => {
      initStatusFromRoute()
      currentPage.value = 1
      void fetchList()
    },
  )

  return {
    activeSection,
    activeSubject,
    activeType,
    activeStatusFilter,
    statusTabs,
    searchKeyword,
    currentPage,
    pageSize,
    loading,
    sectionConfig,
    currentSubjects,
    subjectNavTitle,
    currentResourceTypes,
    listItems,
    displayTotal,
    uploadStats,
    getTypeCount,
    selectSection,
    selectSubject,
    selectType,
    selectStatusFilter,
    setSearchKeyword,
    fetchList,
    fetchStats,
    handleUpload,
    handleSubmitDraft,
    handleWithdraw,
    handleViewFront,
    handleViewDetail,
    handleEdit,
    handleReupload,
    handleDownload,
    handleDeleteDraft,
    handleMore,
    handleExport,
    statusLabel,
    isDraftItem,
    isRejectedItem,
    resolveTypeDisplayName: (key: string) =>
      resolveTypeDisplayName(activeSection.value, key),
    userStore,
  }
}
