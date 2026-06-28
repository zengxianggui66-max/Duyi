import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { downloadApi } from '@/api/download'
import { buildPrimaryChineseFileUrl } from '@/api/primaryChinese'
import { resourceGateway } from '@/api/resourceGateway'
import { unwrapData } from '@/api/request'
import { useUserStore } from '@/store'
import { useFilePreview } from '@/composables/useFilePreview'
import { resolvePreviewAssetUrl } from '@/utils/filePreview'
import type { PrimaryChineseItem } from '@/api/types'
import {
  buildSubjectBackLinkFromQuery,
  buildDetailBreadcrumbFromQuery,
} from '@/composables/useResourceBrowseContext'
import { USE_CATALOG_BROWSE } from '@/config/featureFlags'
import { LESSON_TYPE_ICONS } from '@/composables/useLessonHub'
import { resolveLessonPlanContent, parseTeachingFlowSteps } from '@/utils/lessonPlanContent'
import { getFileFormatInfo } from '@/utils/resourceFormat'

/** M6：分享/收藏链接保留 brand 等浏览上下文 */
function appendBrandToUrl(url: string, brand?: string | string[]): string {
  const code = typeof brand === 'string' ? brand : undefined
  if (!code) return url
  try {
    const u = new URL(url, window.location.origin)
    if (!u.searchParams.has('brand')) {
      u.searchParams.set('brand', code)
    }
    return u.toString()
  } catch {
    const sep = url.includes('?') ? '&' : '?'
    return `${url}${sep}brand=${encodeURIComponent(code)}`
  }
}

export function useResourceDetail() {
  const route = useRoute()
  const router = useRouter()
  const userStore = useUserStore()

  // 资源亮点
  const resourceHighlights = computed(() => {
  const type = resource.value?.resourceType
  if (type === 'doc_courseware') {
  return [
  { icon: '✅', title: '流程完整', desc: '包含导入 - 互动 - 升华 - 总结全环节设计' },
  { icon: '🎨', title: '素材丰富', desc: '内置互动问题、情景案例、背景音乐建议' },
  { icon: '🚀', title: '可直接使用', desc: '无需额外修改，下载即可开展班会' },
  { icon: '📖', title: '适配学情', desc: '适配目标学生认知水平，语言通俗易懂' }
  ]
  }
  return [
  { icon: '✅', title: '内容优质', desc: '精心设计的教学资源' },
  { icon: '🚀', title: '即下即用', desc: '无需额外修改，直接使用' },
  { icon: '📖', title: '适配学情', desc: '适配目标学生认知水平' }
  ]
  })

  // ==================== 状态 ====================

  const resource = ref<any>(null)
  const loading = ref(false)
  const isCollected = ref(false)
  const downloading = ref(false)
  const showShareDialog = ref(false)
  const shareUrl = ref('')
  const lessonPlanExpanded = ref(false)
  const isFullPreview = ref(false)
  const selectedLessonType = ref('')
  const {
    loading: previewLoading,
    processing: previewProcessing,
    error: previewError,
    previewInfo: filePreviewInfo,
    loadPreview,
    reset: resetFilePreview,
  } = useFilePreview()

  const pdfUrl = computed(() =>
    resolvePreviewAssetUrl(filePreviewInfo.value?.previewUrl || resource.value?.fileUrl || ''),
  )

  function getFileExt(): string {
    return (resource.value?.fileFormat || '').toLowerCase().replace(/^\./, '')
  }

  function isMediaFormat(ext: string): boolean {
    return ['mp4', 'avi', 'mov', 'webm', 'mp3', 'wav', 'm4a'].includes(ext)
  }

  function isImageFormat(ext: string): boolean {
    return ['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext)
  }

  function getSubjectInlineFileUrl(): string {
    const id = resource.value?.id
    if (isSubjectResource.value && id && isRemoteOssFile.value) {
      return buildPrimaryChineseFileUrl(id, 'inline')
    }
    return ''
  }

  function isWordFormat(ext: string): boolean {
    return ['doc', 'docx'].includes(ext)
  }

  /** 文档预览地址：Word 仅认 PDF 转码结果；禁止 iframe 加载原始 doc/docx */
  const documentPreviewUrl = computed(() => {
    const info = filePreviewInfo.value
    const originalUrl = resource.value?.fileUrl || ''
    const ext = getFileExt()

    if (!originalUrl) return ''

    if (isWordFormat(ext)) {
      if (info?.previewMode === 'pdf' && info.previewUrl) return info.previewUrl
      return ''
    }

    if (info?.previewMode === 'pdf' && info.previewUrl) {
      return info.previewUrl
    }

    if (ext === 'pdf') {
      return info?.previewUrl || getSubjectInlineFileUrl() || originalUrl
    }

    return ''
  })

  const documentPreviewMode = computed(() => {
    const info = filePreviewInfo.value
    const ext = getFileExt()

    if (info?.previewMode === 'processing') return 'processing'

    if (isWordFormat(ext)) {
      if (info?.previewMode === 'pdf' && documentPreviewUrl.value) return 'pdf'
      return 'none'
    }

    if (info?.previewMode === 'pdf' && documentPreviewUrl.value) return 'pdf'
    if (ext === 'pdf' && documentPreviewUrl.value) return 'pdf'
    return 'none'
  })

  const isSubjectResource = computed(() => route.query.from === 'subject')

  const isRemoteOssFile = computed(() => {
    const url = resource.value?.fileUrl || ''
    return url.startsWith('http://') || url.startsWith('https://')
  })

  /** 远程 OSS 音视频/图片走服务端代理；文档类禁止走代理直链（浏览器会触发下载） */
  const playbackUrl = computed(() => {
    const id = resource.value?.id
    const ext = getFileExt()
    if (
      isSubjectResource.value
      && id
      && isRemoteOssFile.value
      && (isMediaFormat(ext) || isImageFormat(ext) || ext === 'pdf')
    ) {
      return buildPrimaryChineseFileUrl(id, 'inline')
    }
    return filePreviewInfo.value?.previewUrl || resource.value?.fileUrl || ''
  })

  // 预览相关
  const previewType = ref<string>('ppt')
  const currentSlide = ref(0)
  const slideCount = ref(1)

  // 音频相关
  const audioPlayer = ref<HTMLAudioElement | null>(null)
  const isPlaying = ref(false)
  const audioProgress = ref(0)
  const currentTime = ref('00:00')
  const audioDuration = ref('00:00')

  // 附加文件列表
  const attachFiles = computed(() => {
    const r = resource.value
    if (!r?.fileUrl) return []

    const extRaw = (r.fileFormat || r.fileExt || 'file').toLowerCase()
    const formatInfo = getFileFormatInfo(extRaw)
    const iconClass =
      ['ppt', 'pptx'].includes(extRaw)
        ? 'icon-ppt'
        : ['zip', 'rar'].includes(extRaw)
          ? 'icon-zip'
          : 'icon-doc'
    const sizeKb = r.fileSizeKb
    let sizeLabel = ''
    if (sizeKb != null && sizeKb > 0) {
      sizeLabel = sizeKb >= 1024 ? `${(sizeKb / 1024).toFixed(1)} MB` : `${sizeKb} KB`
    }

    return [
      {
        name: r.originalFilename || `${r.title}.${extRaw}`,
        desc: formatResourceType(r.resourceType || r.type),
        size: sizeLabel,
        url:
          isSubjectResource.value && r.id && isRemoteOssFile.value
            ? buildPrimaryChineseFileUrl(r.id, 'attachment')
            : r.fileUrl,
        ext: formatInfo.label.toUpperCase(),
        iconClass,
      },
    ]
  })

  interface RelatedResourceItem {
    id: number
    title: string
    icon: string
    downloadCount: number
    type?: string
    lessonName?: string
    unitName?: string
  }

  const relatedResources = ref<RelatedResourceItem[]>([])
  const relatedResourcesLoading = ref(false)
  const recommendResources = ref<RelatedResourceItem[]>([])
  const recommendResourcesLoading = ref(false)

  function getCurrentResourceType(r?: { resourceType?: string; type?: string } | null) {
    return r?.resourceType || r?.type || ''
  }

  function mapToRelatedItem(item: PrimaryChineseItem): RelatedResourceItem {
    return {
      id: item.id,
      title: item.title,
      icon: LESSON_TYPE_ICONS[item.type || ''] || '📎',
      downloadCount: item.downloadCount ?? 0,
      type: item.type,
      lessonName: item.lessonName,
      unitName: item.unitName,
    }
  }

  function buildSubjectBrowseParams(r: Record<string, any>) {
    const catalogNodeId =
      r.catalogNodeId ||
      (route.query.catalogNodeId ? Number(route.query.catalogNodeId) : undefined)
    return {
      stage: r.stage || (route.query.stage === 'primary' ? '小学' : undefined),
      subject: r.subject,
      module: r.module || (route.query.module as string) || undefined,
      gradeName: r.gradeName || r.grade,
      edition: r.version || r.edition,
      brandCode:
        r.brandCode ||
        (typeof route.query.brand === 'string' ? route.query.brand : undefined),
      catalogNodeId: catalogNodeId && catalogNodeId > 0 ? catalogNodeId : undefined,
      unitName: r.unitName || (route.query.unit as string) || undefined,
      lessonName: r.lessonName || (route.query.lesson as string) || undefined,
    }
  }

  async function querySubjectResources(
    params: Record<string, unknown>,
    useCatalogNode: boolean,
  ) {
    const pageParams = {
      ...params,
      current: 1,
      size: 12,
      sortField: 'download_count',
      sortOrder: 'desc' as const,
    }
    const { page } = await resourceGateway.listPrimaryChineseResources(pageParams, { silentError: true })
    return page?.records || []
  }

  const recommendSectionTitle = computed(() => {
    const typeLabel = formatResourceType(getCurrentResourceType(resource.value))
    if (route.query.from === 'subject') {
      const grade = resource.value?.gradeName || resource.value?.grade
      return grade ? `更多${grade}${typeLabel}推荐` : `更多${typeLabel}推荐`
    }
    return `更多${typeLabel}推荐`
  })

  const sameLessonResources = ref<PrimaryChineseItem[]>([])
  const sameLessonLoading = ref(false)

  // ==================== 计算属性 ====================

  const subjectBackLink = computed(() =>
    buildSubjectBackLinkFromQuery(route.query as Record<string, string | string[] | undefined>),
  )

  const showLessonPlanStructured = computed(() => {
    const t = resource.value?.resourceType || resource.value?.type
    return t === '教案'
  })

  const showSameLessonBar = computed(() => {
    if (route.query.from !== 'subject') return false
    return !!(
      route.query.lesson
      || route.query.unit
      || resource.value?.lessonName
      || resource.value?.unitName
    )
  })

  const lessonName = computed(() => (route.query.lesson as string) || resource.value?.lessonName || '')
  const lessonParentUnit = computed(() => (route.query.unit as string) || resource.value?.unitName || '')
  const activeLessonType = computed(
    () => selectedLessonType.value || resource.value?.resourceType || resource.value?.type || '课件',
  )

  const lessonPlanStructured = computed(() => {
    if (!resource.value) {
      return resolveLessonPlanContent({})
    }
    return resolveLessonPlanContent(resource.value)
  })

  watch(
    lessonPlanStructured,
    (data) => {
      lessonPlanExpanded.value = data.hasStructured
    },
    { immediate: true },
  )

  const teachingFlowSteps = computed(() => parseTeachingFlowSteps(lessonPlanStructured.value))

  const sameLessonTypeCounts = computed(() => {
    const map: Record<string, number> = {}
    for (const item of sameLessonResources.value) {
      const t = item.type || '其他'
      map[t] = (map[t] || 0) + 1
    }
    const current = resource.value?.resourceType || resource.value?.type
    if (current) map[current] = (map[current] || 0)
    return map
  })

  const filteredSameLessonResources = computed(() => {
    const selected = selectedLessonType.value
    if (!selected) return sameLessonResources.value
    return sameLessonResources.value.filter((item) => (item.type || '其他') === selected)
  })

  function syncSelectedLessonType() {
    const currentType = resource.value?.resourceType || resource.value?.type || ''
    if (!selectedLessonType.value) {
      selectedLessonType.value = currentType
      return
    }
    if ((sameLessonTypeCounts.value[selectedLessonType.value] || 0) > 0) {
      return
    }
    selectedLessonType.value = currentType || selectedLessonType.value
  }

  watch(
    () => [resource.value?.id, sameLessonResources.value.length],
    () => {
      syncSelectedLessonType()
    },
    { immediate: true },
  )


  const currentChannel = computed(() => {
  if (route.query.from === 'subject') {
    if (lessonName.value && resource.value) {
      const typeLabel = formatResourceType(resource.value.resourceType || resource.value.type)
      return `${lessonName.value} · ${typeLabel}`
    }
    const parts = [
      route.query.stage === 'primary' ? '小学' : route.query.stage,
      route.query.subject,
    ].filter(Boolean)
    return parts.length ? `${parts.join(' · ')} 资源` : '学科资源'
  }
  const type = route.params.type as string
  const channelMap: Record<string, string> = {
  'class-meeting': '主题班会',
  'parent-meeting': '家长会',
  'safety': '安全教育',
  'mental': '心理健康',
  'moral': '品德培养'
  }
  return channelMap[type] || '主题班会'
  })

  const detailBreadcrumbItems = computed(() => {
    const items = buildDetailBreadcrumbFromQuery(
      route.query as Record<string, string | string[] | undefined>,
    )
    if (resource.value?.title) {
      items.push({ name: resource.value.title })
    }
    return items
  })

  const gradeLevelMap: Record<string, string> = {
  preschool: '幼儿',
  primary: '小学',
  junior: '初中',
  senior: '高中',
  art: '美术',
  dance: '舞蹈'
  }

  // ==================== 格式化函数 ====================

  function formatResourceType(type?: string) {
  if (!type) return '课件'
  if (type === '教案' || type === '课件' || type === '练习') return type
  const typeMap: Record<string, string> = {
  paper_handout: '纸质讲义',
  paper_exam: '试卷',
  paper_book: '教辅图书',
  media_micro: '微课视频',
  media_record: '课堂实录',
  media_audio: '音频素材',
  doc_courseware: '课件',
  doc_analysis: '学情分析',
  digital_bank: '题库',
  digital_exam: '在线测试',
  ext_assessment: '入学测评',
  ext_practical: '实践材料',
  }
  return typeMap[type] || type
  }

  function formatMediaType(type?: string) {
  if (!type) return '文档'
  if (type.includes('video')) return '视频'
  if (type.includes('audio')) return '音频'
  if (type.includes('image')) return '图片'
  if (type.includes('ppt')) return 'PPT/Word'
  return '文档'
  }

  /** 大数字格式化：1200 → 1.2k，9800 → 9.8k，12500 → 1.3w */
  function formatCount(n?: number) {
    if (n == null) return '0'
    if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
    if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
    return String(n)
  }

  /** 资源类型 → Element Plus tag type */
  function resourceTypeTagType(type?: string) {
    if (!type) return 'info'
    if (type.includes('courseware') || type.includes('课件')) return 'danger'
    if (type.includes('lesson_plan') || type.includes('教案')) return 'primary'
    if (type.includes('exercise') || type.includes('练习')) return 'success'
    if (type.includes('exam') || type.includes('试卷')) return 'warning'
    return 'info'
  }

  /** 资源类型 → 色号（用于侧边栏色条 / 标签） */
  function resourceTypeColor(type?: string) {
    if (!type) return '#909399'
    if (type.includes('courseware') || type.includes('课件')) return '#f56c6c'
    if (type.includes('lesson_plan') || type.includes('教案')) return '#409eff'
    if (type.includes('exercise') || type.includes('练习')) return '#67c23a'
    if (type.includes('exam') || type.includes('试卷')) return '#e6a23c'
    return '#909399'
  }

  // 幻灯片样式
  function getSlideStyle(index: number) {
  return {}
  }

  // 推荐卡片颜色
  function getRecommendColor(id: number) {
  const colors = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  ]
  return colors[id % colors.length]
  }

  function mapPrimaryChineseToDetail(item: PrimaryChineseItem) {
    const ext = (item.fileExt || '').toLowerCase()
    return {
      id: item.id,
      title: item.title,
      description: item.description || item.remark || item.title,
      fileUrl: item.ossUrl || '',
      coverUrl: '',
      fileFormat: ext,
      contentType: ext,
      grade: item.gradeName,
      gradeName: item.gradeName,
      stage: item.stage,
      subject: item.subject || item.subjectName,
      version: item.edition,
      unitName: item.unitName,
      module: item.module,
      resourceType: item.type,
      type: item.type,
      downloadCount: item.downloadCount ?? 0,
      viewCount: item.viewCount ?? 0,
      uploadTime: item.uploadTime,
      authorName: '平台用户',
      isFree: true,
      fileSizeKb: item.fileSizeKb,
      remark: item.remark,
      lessonPlanJson: item.lessonPlanJson,
      lessonName: item.lessonName,
      catalogNodeId: item.catalogNodeId,
      brandCode: item.brandCode,
      originalFilename: item.originalFilename,
      fileExt: item.fileExt,
    }
  }

  async function fetchRelatedResources() {
    const r = resource.value
    if (!r?.id) {
      relatedResources.value = []
      return
    }

    const currentType = getCurrentResourceType(r)
    relatedResourcesLoading.value = true
    try {
      if (route.query.from === 'subject') {
        const base = buildSubjectBrowseParams(r)
        const records = await querySubjectResources(
          {
            ...base,
            includeSubtree: true,
          },
          true,
        )
        relatedResources.value = records
          .filter(
            (item) =>
              item.id !== r.id &&
              (!currentType || item.type !== currentType),
          )
          .slice(0, 5)
          .map(mapToRelatedItem)
        return
      }

      const { resourceApi } = await import('@/api')
      const res = await resourceApi.getList({
        gradeLevel: r.gradeLevel,
        subject: r.subject,
        size: 12,
      })
      const list = unwrapData(res)?.records || []
      relatedResources.value = list
        .filter(
          (item: { id?: number; resourceType?: string }) =>
            item.id &&
            item.id !== r.id &&
            (!currentType || item.resourceType !== currentType),
        )
        .slice(0, 5)
        .map((item: { id: number; title: string; downloadCount?: number; resourceType?: string }) => ({
          id: item.id,
          title: item.title,
          icon: LESSON_TYPE_ICONS[item.resourceType || ''] || '📎',
          downloadCount: item.downloadCount ?? 0,
          type: item.resourceType,
        }))
    } catch {
      relatedResources.value = []
    } finally {
      relatedResourcesLoading.value = false
    }
  }

  /** 底部推荐：同栏目 + 同册别 + 同类型（可跨课文） */
  async function fetchRecommendResources() {
    const r = resource.value
    if (!r?.id) {
      recommendResources.value = []
      return
    }

    const currentType = getCurrentResourceType(r)
    recommendResourcesLoading.value = true
    try {
      if (route.query.from === 'subject') {
        const base = buildSubjectBrowseParams(r)
        const records = await querySubjectResources(
          {
            stage: base.stage,
            subject: base.subject,
            module: base.module,
            gradeName: base.gradeName,
            edition: base.edition,
            brandCode: base.brandCode,
            type: currentType || undefined,
          },
          false,
        )
        recommendResources.value = records
          .filter((item) => item.id !== r.id && (!currentType || item.type === currentType))
          .slice(0, 4)
          .map(mapToRelatedItem)
        return
      }

      const { resourceApi } = await import('@/api')
      const res = await resourceApi.getList({
        gradeLevel: r.gradeLevel,
        subject: r.subject,
        resourceType: currentType || r.resourceType,
        size: 8,
      })
      const list = unwrapData(res)?.records || []
      recommendResources.value = list
        .filter((item: { id?: number }) => item.id && item.id !== r.id)
        .slice(0, 4)
        .map((item: { id: number; title: string; downloadCount?: number; resourceType?: string }) => ({
          id: item.id,
          title: item.title,
          icon: LESSON_TYPE_ICONS[item.resourceType || ''] || '📎',
          downloadCount: item.downloadCount ?? 0,
          type: item.resourceType,
        }))
    } catch {
      recommendResources.value = []
    } finally {
      recommendResourcesLoading.value = false
    }
  }

  async function fetchSameLessonResources() {
    if (route.query.from !== 'subject') {
      sameLessonResources.value = []
      return
    }
    const lesson = route.query.lesson as string | undefined
    const unit = route.query.unit as string | undefined
    if (!lesson && !unit) {
      sameLessonResources.value = []
      return
    }

    sameLessonLoading.value = true
    try {
      const { records } = await resourceGateway.listPrimaryChineseAll({
        stage: route.query.stage === 'primary' ? '小学' : undefined,
        subject: resource.value?.subject || undefined,
        module: (route.query.module as string) || resource.value?.module || undefined,
        gradeName: resource.value?.grade || undefined,
        edition: resource.value?.version || undefined,
        unitName: unit || resource.value?.unitName || undefined,
        keyword: lesson || undefined,
      }, { silentError: true })
      sameLessonResources.value = records || []
      syncSelectedLessonType()
    } catch {
      sameLessonResources.value = []
    } finally {
      sameLessonLoading.value = false
    }
  }

  function goToSameLessonResource(item: PrimaryChineseItem) {
    if (!item?.id) return
    const query: Record<string, string> = {}
    for (const [k, v] of Object.entries(route.query)) {
      if (typeof v === 'string') query[k] = v
    }
    delete query.layout
    router.push({ name: 'ResourceDetail', params: { id: String(item.id) }, query })
  }

  function handleSelectLessonType(type: string) {
    selectedLessonType.value = type
  }

  function toggleLessonPlanExpanded() {
    lessonPlanExpanded.value = !lessonPlanExpanded.value
  }

  // ==================== 数据获取 ====================

  function queryFrom(): string | undefined {
    const v = route.query.from
    if (Array.isArray(v)) return v[0] ?? undefined
    return v ?? undefined
  }

  function isSubjectBrowseEntry(): boolean {
    return queryFrom() === 'subject'
  }

  async function loadPrimaryDetail(id: number) {
    const detail = await resourceGateway.getPrimaryChineseDetail(id)
    resource.value = mapPrimaryChineseToDetail(detail)
    resourceGateway.recordBySource('primary_chinese', id, 'view').catch(() => {})
    checkCollectStatus(id, 'primary_chinese')
  }

  async function loadGenericDetail(id: number) {
    const { resourceApi } = await import('@/api')
    const res = await resourceApi.getDetail(id)
    resource.value = unwrapData(res)
    checkCollectStatus(id)
  }

  async function fetchDetail(id: number) {
    if (!id || Number.isNaN(id)) {
      resource.value = null
      ElMessage.warning('无效的资源ID')
      return
    }

    loading.value = true
    resource.value = null
    resetFilePreview()

    try {
      if (isSubjectBrowseEntry()) {
        await loadPrimaryDetail(id)
      } else {
        await loadGenericDetail(id)
      }
    } catch (err) {
      if (isSubjectBrowseEntry()) {
        console.warn('资源详情加载失败', err)
        resource.value = null
        ElMessage.error('资源详情加载失败，请稍后重试')
      } else {
        try {
          await loadPrimaryDetail(id)
          await router.replace({
            path: route.path,
            query: { ...route.query, from: 'subject' },
          })
        } catch (fallbackErr) {
          console.warn('资源详情加载失败', fallbackErr)
          resource.value = null
          ElMessage.error('资源详情加载失败，请稍后重试')
        }
      }
    } finally {
      loading.value = false
    }

    if (!resource.value) return

    determinePreviewType()
    shareUrl.value = appendBrandToUrl(
      `${window.location.origin}/resource/${id}${window.location.search || '?from=share'}`,
    )
    resolveResourcePreview().catch(() => {})

    if (isSubjectBrowseEntry()) {
      fetchSameLessonResources().catch(() => {})
    }
    fetchRelatedResources().catch(() => {})
    fetchRecommendResources().catch(() => {})
  }

  /** 收藏状态检查（不阻塞主流程）*/
  let collectCheckTimer: ReturnType<typeof setTimeout> | null = null
  async function checkCollectStatus(id: number, type?: string) {
  try {
    const { collectApi } = await import('@/api')
    const collectRes = await collectApi.checkCollected(id, type as any)
    isCollected.value = unwrapData(collectRes) as boolean
  } catch { /* 未登录忽略 */ }
  }

  // 判断预览类型
  function determinePreviewType() {
  const format = resource.value?.fileFormat?.toLowerCase() || ''
  const contentType = resource.value?.contentType || ''

  if (format === 'pdf' || contentType.includes('pdf')) {
  previewType.value = 'document'
  } else if (['mp4', 'avi', 'mov', 'webm'].includes(format) || contentType.includes('video')) {
  previewType.value = 'video'
  } else if (['mp3', 'wav', 'm4a'].includes(format) || contentType.includes('audio')) {
  previewType.value = 'audio'
  } else if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(format) || contentType.includes('image')) {
  previewType.value = 'image'
  } else if (['ppt', 'pptx'].includes(format) || contentType.includes('ppt')) {
  previewType.value = 'ppt'
  } else if (['doc', 'docx'].includes(format)) {
  previewType.value = 'document'
  } else if (resource.value?.resourceType === '教案' || resource.value?.type === '教案') {
  previewType.value = 'document'
  } else {
  previewType.value = resource.value?.fileUrl ? 'document' : 'ppt'
  }
  }

  /** 阶段 C/D：服务端转码 / PPT 幻灯片预览 */
  async function resolveResourcePreview() {
    const fileUrl = resource.value?.fileUrl
    resetFilePreview()
    if (!fileUrl) return

    const format = resource.value?.fileFormat?.toLowerCase() || ''
    const isMedia = ['mp4', 'avi', 'mov', 'webm', 'mp3', 'wav', 'm4a'].includes(format)
    const isRemoteMedia = isMedia && isRemoteOssFile.value

    // 远程 OSS 音视频由 playbackUrl 代理播放，无需再走预览 API
    if (isRemoteMedia && isSubjectResource.value) {
      return
    }

    await loadPreview(fileUrl)
    const info = filePreviewInfo.value
    if (!info) return

    const isPpt = ['ppt', 'pptx'].includes(format)

    if (info.slideCount && info.slideCount > 0) {
      slideCount.value = info.slideCount
    }

    if (isPpt) {
      previewType.value = 'ppt'
      return
    }

    if (info.previewType === 'video') previewType.value = 'video'
    else if (info.previewType === 'audio') previewType.value = 'audio'
    else if (info.previewType === 'image') previewType.value = 'image'
    else if (isPpt && ['pdf', 'html', 'slides'].includes(info.previewMode || '')) {
      previewType.value = 'ppt'
    } else if (['pdf', 'native'].includes(info.previewMode || '')) {
      previewType.value = 'document'
    } else if (isWordFormat(format) || info.previewMode === 'processing' || info.previewMode === 'none') {
      previewType.value = 'document'
    }
  }

  function setSlideCount(count: number) {
    if (count > 0) slideCount.value = count
  }

  // ==================== 操作函数 ====================

  function handleDownload() {
  const id = resource.value?.id
  const url = resource.value?.fileUrl
  if (!url) {
    ElMessage.warning('暂无下载链接')
    return
  }

  const downloadUrl =
    isSubjectResource.value && id && isRemoteOssFile.value
      ? buildPrimaryChineseFileUrl(id, 'attachment')
      : url

  window.open(downloadUrl, '_blank')
  ElMessage.success('开始下载资源')
  if (id && isSubjectResource.value && !isRemoteOssFile.value) {
    resourceGateway.recordBySource('primary_chinese', id, 'download').catch(() => {})
  }
  if (id && userStore.isLoggedIn) {
    const r = resource.value
    const stageKeyFromRoute = String(route.query.stage || '')
    const stageNameMap: Record<string, string> = {
      preschool: '幼儿',
      primary: '小学',
      junior: '初中',
      senior: '高中',
      art: '美术',
      dance: '舞蹈',
    }
    downloadApi.record({
      resourceId: id,
      resourceType: route.query.from === 'subject' ? 'primary_chinese' : 'resource',
      resourceTitle: r?.title,
      subject: r?.subject,
      stageKey: stageKeyFromRoute || undefined,
      gradeName: r?.gradeName || r?.grade,
      teachingType: r?.type || r?.resourceType,
      fileExt: r?.fileFormat || r?.fileExt,
    }).catch(() => {})
  }
  }

  async function handleCollect() {
  const id = resource.value?.id
  if (!id) return
  const collectType =
    route.query.from === 'subject' ? 'primary_chinese' : 'resource'
  const r = resource.value
  const stageNameMap: Record<string, string> = {
    preschool: '幼儿',
    primary: '小学',
    junior: '初中',
    senior: '高中',
    art: '美术',
    dance: '舞蹈',
  }
  const stageKeyFromRoute = String(route.query.stage || '')
  try {
  const { collectApi } = await import('@/api')
  if (isCollected.value) {
  await collectApi.uncollect(id, collectType)
  isCollected.value = false
  ElMessage.success('已取消收藏')
  } else {
  await collectApi.collect({
    resourceId: id,
    resourceType: collectType,
    title: r?.title,
    stage: r?.stage || stageNameMap[stageKeyFromRoute] || undefined,
    stageKey: stageKeyFromRoute || undefined,
    subject: r?.subject,
    module: r?.module,
    teachingType: r?.type || r?.resourceType,
    gradeName: r?.gradeName,
    fileExt: r?.fileFormat,
    ossUrl: r?.fileUrl,
    brandCode:
      (typeof route.query.brand === 'string' ? route.query.brand : undefined) ||
      r?.brandCode,
  })
  isCollected.value = true
  ElMessage.success('收藏成功，可在个人中心「我的收藏」查看')
  }
  } catch {
  isCollected.value = !isCollected.value
  ElMessage.success(isCollected.value ? '收藏成功' : '已取消收藏')
  }
  }

  async function handleShare() {
  const id = resource.value?.id
  if (!id) return
  const resourceType =
    route.query.from === 'subject' ? 'primary_chinese' : 'resource'
  try {
    const { shareApi } = await import('@/api')
    const res = await shareApi.getShareUrl(id, resourceType)
    const data = unwrapData(res)
    const base =
      data?.shareUrl || `${window.location.origin}/resource/${id}${window.location.search || ''}`
    shareUrl.value = appendBrandToUrl(base, typeof route.query.brand === 'string' ? route.query.brand : undefined)
  } catch {
    const fallback =
      route.query.from === 'subject'
        ? `${window.location.origin}/resource/${id}${window.location.search || '?from=subject'}`
        : `${window.location.origin}/resource/${id}`
    shareUrl.value = appendBrandToUrl(fallback, typeof route.query.brand === 'string' ? route.query.brand : undefined)
  }
  showShareDialog.value = true
  }

  async function handleAddToFolder() {
    if (!resource.value?.id) return
    const { usePrepBasketStore } = await import('@/store/prepBasket')
    const prepBasket = usePrepBasketStore()
    const r = resource.value
    const resourceType =
      route.query.from === 'subject' ? 'primary_chinese' as const : 'resource'
    await prepBasket.addItem({
      itemType: 'resource',
      refId: Number(r.id),
      title: r.title || '未命名资源',
      subtitle: r.subject || r.resourceType || resourceType,
      coverUrl: r.coverUrl || r.thumbnailUrl,
      metaJson: JSON.stringify({
        resourceType,
        gradeLevel: r.gradeLevel,
        fileFormat: r.fileFormat,
        fileUrl: r.fileUrl || r.storagePath,
      }),
    })
  }

  async function copyShareUrl() {
  try {
  await navigator.clipboard.writeText(shareUrl.value)
  const id = resource.value?.id
  if (id) {
    const { shareApi } = await import('@/api')
    const resourceType =
      route.query.from === 'subject' ? 'primary_chinese' : 'resource'
    await shareApi.recordShare({
      resourceId: id,
      resourceType,
      shareType: 'link',
      sharePlatform: 'copy',
    }).catch(() => {})
  }
  ElMessage.success('链接已复制到剪贴板')
  } catch (e) {
  ElMessage.error('复制失败，请手动复制')
  }
  }

  function shareToWechat() {
  ElMessage.success('链接已复制，打开微信粘贴分享')
  }

  function shareToQQ() {
  const title = encodeURIComponent(resource.value?.title || '新课堂教育')
  const url = encodeURIComponent(shareUrl.value)
  window.open(`https://connect.qq.com/widget/shareqq/index.html?url=${url}&title=${title}`, '_blank')
  }

  function shareToWorkWechat() {
  ElMessage.success('链接已复制，打开企业微信粘贴分享')
  }

  // 幻灯片导航
  function prevSlide() {
  if (currentSlide.value > 0) currentSlide.value--
  }

  function nextSlide() {
  if (currentSlide.value < slideCount.value - 1) currentSlide.value++
  }

  function toggleFullPreview() {
  isFullPreview.value = !isFullPreview.value
  }

  // 音频控制
  function toggleAudio() {
  const player = audioPlayer.value
  if (!player) return
  if (isPlaying.value) {
  player.pause()
  isPlaying.value = false
  return
  }
  player.play()
    .then(() => {
      isPlaying.value = true
    })
    .catch(() => {
      isPlaying.value = false
      ElMessage.warning('音频暂无法播放，请稍后重试或下载后查看')
    })
  }

  function onAudioLoaded() {
  if (!audioPlayer.value) return
  const duration = audioPlayer.value.duration
  audioDuration.value = formatTime(duration)
  }

  function onAudioTimeUpdate() {
  if (!audioPlayer.value) return
  const current = audioPlayer.value.currentTime
  const duration = audioPlayer.value.duration
  audioProgress.value = duration > 0 ? (current / duration) * 100 : 0
  currentTime.value = formatTime(current)
  if (duration > 0 && current >= duration) {
    isPlaying.value = false
  }
  }

  function seekAudio(e: MouseEvent) {
  if (!audioPlayer.value) return
  const target = e.currentTarget as HTMLElement
  const rect = target.getBoundingClientRect()
  const percent = (e.clientX - rect.left) / rect.width
  audioPlayer.value.currentTime = percent * audioPlayer.value.duration
  }

  function formatTime(seconds: number): string {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
  }

  // 下载附件
  function downloadFile(file: { url?: string; name?: string }) {
    if (file.url) {
      window.open(file.url, '_blank')
      ElMessage.success('开始下载')
      return
    }
    handleDownload()
  }

  function goToResource(id: number) {
    const query: Record<string, string> = {}
    for (const [k, v] of Object.entries(route.query)) {
      if (typeof v === 'string') query[k] = v
    }
    router.push({ name: 'ResourceDetail', params: { id: String(id) }, query })
  }

  // ==================== 初始化 ====================

  watch(
    () => route.fullPath,
    () => {
      const id = Number(route.params.id)
      if (id) fetchDetail(id)
    },
    { immediate: true },
  )

  return {
    resource, loading, isCollected, downloading, showShareDialog, shareUrl,
    lessonPlanExpanded, isFullPreview, previewType, currentSlide, slideCount,
    audioPlayer, isPlaying, audioProgress, currentTime, audioDuration,
    attachFiles, relatedResources, relatedResourcesLoading,
    recommendResources, recommendResourcesLoading, recommendSectionTitle, resourceHighlights,
    currentChannel, subjectBackLink, gradeLevelMap, pdfUrl, playbackUrl,
    documentPreviewUrl, documentPreviewMode,
    filePreviewInfo, previewLoading, previewProcessing, previewError, setSlideCount,
    showLessonPlanStructured, showSameLessonBar, lessonPlanStructured, teachingFlowSteps,
    lessonName, lessonParentUnit,
    activeLessonType, sameLessonResources, filteredSameLessonResources, sameLessonLoading,
    sameLessonTypeCounts,
    detailBreadcrumbItems,
    formatResourceType, formatMediaType, formatCount, resourceTypeTagType, resourceTypeColor, getSlideStyle, getRecommendColor,
    handleDownload, handleCollect, handleShare, handleAddToFolder,
    copyShareUrl, shareToWechat, shareToQQ, shareToWorkWechat,
    prevSlide, nextSlide, toggleFullPreview, toggleAudio, onAudioLoaded,
    onAudioTimeUpdate, seekAudio, formatTime, downloadFile, goToResource,
    goToSameLessonResource, handleSelectLessonType, toggleLessonPlanExpanded,
  }
}



