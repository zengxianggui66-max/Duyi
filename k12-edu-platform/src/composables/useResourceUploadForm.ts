import { ref, computed, reactive, onMounted, onUnmounted, watch, type InjectionKey } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Files, Film, Headset } from '@element-plus/icons-vue'
import {
  fileApi,
  primaryChineseApi,
  type PrimaryChineseItem,
  type UploadResult,
  type FilePreviewInfo,
} from '@/api'
import { unwrapData } from '@/api/request'
import {
  type SyncUploadPayload,
} from '@/utils/resourceWriteMapper'
import { subjectDataMap, stageNames, type StageKey } from '@/config/subjectConfig'
import { LESSON_TYPE_ORDER } from '@/composables/useLessonHub'
import {
  subTypesByResourceType,
} from '@/constants/uploadResourceSubTypes'
import { useUploadTaxonomyCascade } from '@/composables/useUploadTaxonomyCascade'
import {
  parseUploadRouteQuery,
} from '@/utils/uploadRoute'
import {
  SYNC_PREP_COLUMN,
  resolveSyncPrepUploadFields,
} from '@/constants/syncPrepColumnFilters'
import {
  formatUploadContextSummary,
  suggestSyncUploadTitle,
  buildSubjectReturnLocation,
  buildPlacementSnapshot,
  suggestSuiteItemTitle,
  type UploadBrowseSnapshot,
} from '@/utils/uploadRoute'
import { getUploadModuleSchema, shouldSkipClassificationStep } from '@/constants/uploadSchema'
import {
  buildUploadRecommendContext,
  getUploadRecommendations,
  applyUploadRecommendation,
  LEGACY_UPLOAD_TEMPLATES,
  type UploadRecommendation,
} from '@/constants/uploadRecommend'
import {
  SYNC_UPLOAD_DEFAULT_STATUS,
} from '@/constants/uploadDefaults'
import {
  RESOURCE_TAG_LABELS,
  type ResourceTagOption,
} from '@/constants/resourceBrowseTags'
import {
  loadBrowseTagOptions,
  loadExamTypeOptions,
} from '@/composables/dictionarySource'
import {
  getFormatPreviewMeta,
  getFileExtension,
  canLocalPreview,
  getPreviewStatusLabel,
  mapServerPreviewKind,
  type FormatPreviewMeta,
  type LocalPreviewType,
  type ServerPreviewKind,
} from '@/utils/previewType'
import { buildLessonPlanJsonFromFlowText } from '@/utils/lessonPlanContent'
import {
  DIFFICULTY_LABELS,
  SCENARIO_LABELS,
  EXAM_TYPE_LABELS,
  GRADE_LEVEL_LABELS,
  RESOURCE_TYPE_LABELS,
} from '@/constants/uploadLabels'
import { persistDraftRecord, finalizeDraftSubmit } from '@/utils/uploadPersist'
import { apiStageToSection } from '@/constants/myResources'
import { RESOURCE_STATUS_DRAFT, RESOURCE_STATUS_PENDING } from '@/constants/uploadDefaults'

export interface UploadFormData {
  title: string
  description: string
  gradeLevel: string
  subject: string
  grade: string
  tags: string[]
  resourceType: string
  subType: string
  version: string
  examTypes: string[]
  scenarios: string[]
  difficulty: string
  isFree: number
  allowPreview: number
  sortWeight: number
  /** 栏目（与列表 module 一致） */
  module: string
  /** 教学资源类型：课件/教案/试卷…（与列表 type 一致） */
  teachingType: string
  gradeName: string
  editionName: string
  unitName: string
  lessonName: string
  /** 目录树节点 id（catalog API，提交时写入资源） */
  catalogNodeId?: number
  /** 可选：教学流程（每行一个环节，详情页侧边栏展示） */
  teachingFlowText: string
}

export type UploadFormContext = ReturnType<typeof useResourceUploadForm>

export const UPLOAD_FORM_KEY: InjectionKey<UploadFormContext> = Symbol('uploadForm')

const SYNC_DEFAULT_DESCRIPTION =
  '本资源为同步备课资料，内容与教材目录对应，适用于课堂教学与课后巩固。'

export function useResourceUploadForm() {
  const router = useRouter()
  const route = useRoute()

  const currentStep = ref(0)
  const placementConfirmed = ref(false)
  const basicFormRef = ref<FormInstance>()
  const submitting = ref(false)
  const uploadFile = ref<File | null>(null)
  /** 成套模式下的多文件列表 */
  const uploadFiles = ref<File[]>([])
  const previewUrl = ref('')
  const formatMeta = ref<FormatPreviewMeta | null>(null)
  const docxPreviewHtml = ref('')
  const previewLoading = ref(false)
  const previewError = ref('')
  /** 云端上传/预览失败提示（不遮挡本地预览） */
  const serverPreviewWarning = ref('')
  const tempUploadResult = ref<UploadResult | null>(null)
  const serverPreviewUrl = ref('')
  const serverPreviewType = ref('')
  const serverPreviewMode = ref('')
  const serverPreviewProvider = ref('')
  const serverPreviewMessage = ref('')
  const serverPreviewInfo = ref<FilePreviewInfo | null>(null)
  const serverPreviewReady = ref(false)
  let fileSelectGeneration = 0
  const browseSnapshot = ref<UploadBrowseSnapshot>(parseUploadRouteQuery({}))
  const draftId = ref<number | null>(null)
  const draftSavedFile = ref<{ name: string; url: string; ext?: string; sizeKb?: number } | null>(
    null,
  )
  const activeRecommendationId = ref('')
  const browseRecommendationAutoApplied = ref(false)
  const descriptionUserEdited = ref(false)

  const formData = reactive<UploadFormData>({
    title: '',
    description: '',
    gradeLevel: '',
    subject: '',
    grade: '',
    tags: [] as string[],
    resourceType: '',
    subType: '',
    version: '',
    examTypes: [],
    scenarios: ['classroom'],
    difficulty: 'basic',
    isFree: 0,
    allowPreview: 1,
    sortWeight: 50,
    module: '同步备课',
    teachingType: '课件',
    gradeName: '',
    editionName: '',
    unitName: '',
    lessonName: '',
    catalogNodeId: undefined,
    teachingFlowText: '',
  })

  const taxonomy = useUploadTaxonomyCascade(formData, {
    browseSnapshot,
    onCascadeChange: () => {
      syncPlacementFromForm()
    },
  })

  const {
    stageOptions,
    currentSubjects,
    currentGrades,
    moduleOptions,
    editionOptions,
    gradeOptions,
    resourceTypeOptions,
    currentTextbookVersions,
    optionsLoading,
    treeLoading,
    unitTree,
    selectedUnit,
    lessonOptions,
    canLoadCatalogTree,
    handleGradeLevelChange,
    handleSubjectChange,
    handleModuleChange,
    handleCatalogChange,
    handleUnitChange,
    handleLessonChange,
    loadPlacementFilters,
    loadUnitTree,
    initCascade,
    subjectLabel,
    usingOfflineOptions,
  } = taxonomy

  const basicRules: FormRules = {
    title: [
      { required: true, message: '请输入资源名称', trigger: 'blur' },
      { min: 2, max: 100, message: '名称长度在 2 到 100 个字符', trigger: 'blur' },
    ],
    description: [
      { required: true, message: '请输入资源简介', trigger: 'blur' },
      { min: 10, message: '简介至少 10 个字符', trigger: 'blur' },
    ],
    gradeLevel: [{ required: true, message: '请选择学段', trigger: 'change' }],
    subject: [{ required: true, message: '请选择学科', trigger: 'change' }],
  }

  const isSyncUpload = computed(() => formData.module === '同步备课')

  const showTeachingFlowField = computed(
    () => isSyncUpload.value && ['教案', '课件'].includes(formData.teachingType),
  )

  const fromBrowse = computed(() => browseSnapshot.value.fromBrowse)

  /** 从学科页进入：两步向导（上传 + 预览） */
  const isBrowseWizard = computed(() => browseSnapshot.value.fromBrowse)

  /** 直达上传：五步向导（位置 + 基本 + 文件 + 分类 + 预览） */
  const isDirectWizard = computed(() => !isBrowseWizard.value)

  const uploadModuleSchema = computed(() => getUploadModuleSchema(formData.module || '同步备课'))

  const isSuiteMode = computed(() => browseSnapshot.value.resourceMode === 'suite')

  const showClassificationStep = computed(
    () => isDirectWizard.value && !shouldSkipClassificationStep(formData.module),
  )

  const wizardMaxStep = computed(() => {
    if (isBrowseWizard.value) return 1
    if (showClassificationStep.value) return 4
    return 3
  })

  const isDirectPreviewStep = computed(
    () =>
      isDirectWizard.value &&
      currentStep.value === (showClassificationStep.value ? 4 : 3),
  )

  const isDraftEditMode = computed(() => draftId.value != null && draftId.value > 0)

  const hasUploadFiles = computed(() => {
    if (isSuiteMode.value) return uploadFiles.value.length > 0
    if (uploadFile.value) return true
    if (draftSavedFile.value?.url) return true
    return false
  })

  const placementReady = computed(() => {
    if (isBrowseWizard.value) return true
    if (!formData.gradeLevel || !formData.subject || !formData.module) return false
    const schema = uploadModuleSchema.value
    if (!formData.teachingType) return false
    if (schema.showSyncCatalog) {
      return !!(formData.gradeName && formData.editionName)
    }
    return true
  })

  const submitButtonLabel = computed(() => {
    if (usePrimaryChinesePath.value) return '提交审核'
    return '提交审核'
  })

  const usePrimaryChinesePath = computed(
    () =>
      browseSnapshot.value.fromBrowse ||
      (isSyncUpload.value && placementConfirmed.value),
  )

  const resourceModeLabel = computed(() =>
    browseSnapshot.value.resourceMode === 'suite' ? '找成套' : '找单份',
  )

  const returnLocation = computed(() => buildSubjectReturnLocation(browseSnapshot.value))

  const contextSummary = computed(() =>
    formatUploadContextSummary(browseSnapshot.value, subjectLabel.value),
  )

  const uploadChannel = computed((): '' | 'competition' | 'topic' => {
    const ch = route.query.channel
    if (ch === 'competition' || ch === 'topic') return ch
    return ''
  })

  const recommendContext = computed(() =>
    buildUploadRecommendContext(browseSnapshot.value, formData, uploadChannel.value),
  )

  const uploadRecommendations = computed(() =>
    getUploadRecommendations(recommendContext.value, subjectLabel.value),
  )

  const showUploadRecommendations = computed(
    () =>
      uploadRecommendations.value.length > 0 &&
      !!(formData.gradeLevel && formData.subject),
  )

  const recommendContextLine = computed(() => {
    const ctx = recommendContext.value
    const parts = [
      gradeLevelLabel.value,
      subjectLabel.value,
      ctx.gradeName,
      ctx.editionName,
      ctx.module,
      ctx.lessonName
        ? ctx.unitName
          ? `${ctx.unitName} · ${ctx.lessonName}`
          : ctx.lessonName
        : ctx.unitName,
    ]
    return parts.filter(Boolean).join(' · ')
  })

  const recommendAutoHint = computed(() => {
    if (!browseSnapshot.value.fromBrowse || !browseRecommendationAutoApplied.value) return ''
    const primary = uploadRecommendations.value[0]
    if (primary?.matchBrowse) {
      return '已根据浏览位置自动填充简介与标签'
    }
    return '已根据栏目推荐默认填充'
  })

  const syncTeachingTypes = computed(() =>
    LESSON_TYPE_ORDER.filter((t) => t !== '其他'),
  )

  const currentSubTypes = computed(
    () => subTypesByResourceType[formData.resourceType as keyof typeof subTypesByResourceType] || [],
  )
  const currentExamTypes = ref<{ label: string; value: string }[]>([])
  const uploadResourceTagOptions = ref<ResourceTagOption[]>([])

  async function refreshDictionaryOptions() {
    const stage = formData.gradeLevel
    uploadResourceTagOptions.value = await loadBrowseTagOptions(stage, formData.module)
    currentExamTypes.value = await loadExamTypeOptions(stage)
  }

  const detectedFormat = computed(() => {
    if (!uploadFile.value) return ''
    return uploadFile.value.name.split('.').pop()?.toLowerCase() || ''
  })

  const localPreviewType = computed<LocalPreviewType>(
    () => formatMeta.value?.localPreviewType ?? 'none',
  )

  const hasLocalPreviewContent = computed(() => {
    if (!canLocalPreview(formatMeta.value)) return false
    if (localPreviewType.value === 'docx') return !!docxPreviewHtml.value.trim()
    return !!previewUrl.value
  })

  const hasServerPreview = computed(
    () => serverPreviewReady.value && !!serverPreviewUrl.value,
  )

  const isPreviewable = computed(
    () => hasLocalPreviewContent.value || hasServerPreview.value,
  )

  const previewStatusLabel = computed(() =>
    getPreviewStatusLabel(formatMeta.value, {
      hasLocal: hasLocalPreviewContent.value,
      hasServer: hasServerPreview.value,
      serverProvider: serverPreviewProvider.value,
      previewMode: serverPreviewMode.value,
    }),
  )

  const activePreviewKind = computed((): ServerPreviewKind => {
    if (hasLocalPreviewContent.value) {
      return localPreviewType.value
    }
    if (hasServerPreview.value) {
      return mapServerPreviewKind(
        serverPreviewType.value,
        detectedFormat.value,
        serverPreviewMode.value,
      )
    }
    return 'none'
  })

  const isImageFile = computed(() => activePreviewKind.value === 'image')

  const canNext = computed(() => {
    if (isBrowseWizard.value) {
      if (currentStep.value === 0) {
        return !!(
          formData.title &&
          formData.description &&
          formData.gradeLevel &&
          formData.subject &&
          formData.teachingType &&
          hasUploadFiles.value &&
          formData.gradeName &&
          formData.editionName
        )
      }
      return true
    }
    if (currentStep.value === 0) return placementReady.value
    if (currentStep.value === 1) {
      return !!(formData.title && formData.description && formData.gradeLevel && formData.subject)
    }
    if (currentStep.value === 2) return hasUploadFiles.value
    if (currentStep.value === 3 && isSyncUpload.value) {
      return !!formData.teachingType
    }
    return true
  })

  const canSubmit = computed(() => {
    if (usingOfflineOptions.value) return false
    const base = !!(
      formData.title &&
      formData.description &&
      formData.gradeLevel &&
      formData.subject &&
      hasUploadFiles.value &&
      formData.teachingType
    )
    if (usePrimaryChinesePath.value) {
      return base && !!formData.gradeName && !!formData.editionName && !!formData.module
    }
    return base && !!formData.resourceType
  })

  const gradeLevelLabel = computed(() => {
    const fromApi = stageOptions.value.find((s) => s.key === formData.gradeLevel)?.name
    if (fromApi) return fromApi
    if (formData.gradeLevel && stageNames[formData.gradeLevel as StageKey]) {
      return stageNames[formData.gradeLevel as StageKey]
    }
    return GRADE_LEVEL_LABELS[formData.gradeLevel] || ''
  })

  const gradeLabel = computed(() => {
    if (formData.gradeName) return formData.gradeName
    return currentGrades.value.find((g) => g.value === formData.grade)?.label || ''
  })

  const resourceTypeLabel = computed(() => {
    if (isSyncUpload.value) return formData.teachingType || '未选择'
    return RESOURCE_TYPE_LABELS[formData.resourceType] || ''
  })

  const subTypeLabel = computed(() => {
    return currentSubTypes.value.find((s) => s.value === formData.subType)?.label || '未选择'
  })

  const difficultyLabel = computed(() => {
    return DIFFICULTY_LABELS[formData.difficulty] || ''
  })

  const scenariosLabel = computed(() => {
    return formData.scenarios.map((s) => SCENARIO_LABELS[s]).filter(Boolean).join('、') || '未选择'
  })

  const examTypesLabel = computed(() => {
    const labelMap = Object.fromEntries(currentExamTypes.value.map((t) => [t.value, t.label]))
    return formData.examTypes.map((e) => labelMap[e] || EXAM_TYPE_LABELS[e]).filter(Boolean).join('、') || '未选择'
  })

  const tagsLabel = computed(() =>
    formData.tags.map((t) => RESOURCE_TAG_LABELS[t] || t).filter(Boolean).join('、') || '无标签',
  )

  watch(
    () => [formData.gradeLevel, formData.module] as const,
    () => {
      void refreshDictionaryOptions()
    },
    { immediate: true },
  )

  watch(uploadResourceTagOptions, (options) => {
    const allowed = new Set(options.map((o) => o.value))
    formData.tags = formData.tags.filter((t) => allowed.has(t))
  })

  function applyBrowseSnapshot(snapshot: UploadBrowseSnapshot) {
    browseSnapshot.value = snapshot
    if (!snapshot.fromBrowse) return

    if (snapshot.stageKey) formData.gradeLevel = snapshot.stageKey
    if (snapshot.subjectKey) formData.subject = snapshot.subjectKey
    if (snapshot.module) formData.module = snapshot.module
    if (snapshot.teachingType) {
      const t = snapshot.teachingType
      const invalidCatalogLabel = t.includes('（统编版）') || t.includes('（人教版）')
      formData.teachingType = invalidCatalogLabel
        ? getUploadModuleSchema(snapshot.module || '同步备课').defaultTeachingType
        : t
    }
    if (snapshot.gradeName) formData.gradeName = snapshot.gradeName
    if (snapshot.editionName) formData.editionName = snapshot.editionName
    if (snapshot.unitName) formData.unitName = snapshot.unitName
    if (snapshot.lessonName) formData.lessonName = snapshot.lessonName
    if (snapshot.versionKey) formData.version = snapshot.versionKey
    if (snapshot.catalogNodeId && snapshot.catalogNodeId > 0) {
      formData.catalogNodeId = snapshot.catalogNodeId
    }

    if (snapshot.module === SYNC_PREP_COLUMN && snapshot.teachingType) {
      const syncFields = resolveSyncPrepUploadFields(snapshot.teachingType)
      if (syncFields) {
        formData.teachingType = syncFields.apiType
        if (syncFields.subType) formData.subType = syncFields.subType
        if (syncFields.resourceType) formData.resourceType = syncFields.resourceType
      }
    }

    if (!formData.description || formData.description.length < 10) {
      formData.description = SYNC_DEFAULT_DESCRIPTION
    }
    if (snapshot.module === '同步备课' && !formData.tags.includes('sync')) {
      formData.tags = [...formData.tags, 'sync']
    }

    syncTitleFromBrowseContext()

    tryAutoApplyBrowseRecommendation()
  }

  function syncPlacementFromForm(): UploadBrowseSnapshot {
    const snap = buildPlacementSnapshot({
      stageKey: (formData.gradeLevel || '') as StageKey | '',
      subjectKey: formData.subject,
      versionKey: formData.version || browseSnapshot.value.versionKey,
      module: formData.module,
      teachingType: formData.teachingType,
      gradeName: formData.gradeName,
      editionName: formData.editionName,
      unitName: formData.unitName,
      lessonName: formData.lessonName,
      resourceMode: browseSnapshot.value.resourceMode,
      catalogNodeId: formData.catalogNodeId,
    })
    browseSnapshot.value = snap
    if (snap.module === '同步备课' && !formData.tags.includes('sync')) {
      formData.tags = [...formData.tags, 'sync']
    }
    return snap
  }

  function confirmPlacementStep() {
    if (!placementReady.value) return false
    const snap = syncPlacementFromForm()
    placementConfirmed.value = true
    if (snap.module === '同步备课') {
      syncTitleFromBrowseContext()
    }
    return true
  }

  function syncTitleFromBrowseContext() {
    if (!browseSnapshot.value.fromBrowse && !placementConfirmed.value) return
    if (formData.module !== '同步备课') return
    const subjectName =
      subjectDataMap[browseSnapshot.value.stageKey as StageKey]?.find(
        (s) => s.key === browseSnapshot.value.subjectKey,
      )?.name || subjectLabel.value
    const snap: UploadBrowseSnapshot = {
      ...browseSnapshot.value,
      teachingType: formData.teachingType || browseSnapshot.value.teachingType,
    }
    const title = suggestSyncUploadTitle(snap, subjectName)
    if (title) formData.title = title
  }

  function findRecommendationById(id: string): UploadRecommendation | undefined {
    return uploadRecommendations.value.find((r) => r.id === id)
  }

  /** 浏览进入：自动应用与当前 type 一致的首条推荐（仅增强简介/标签，类型已由路由带入） */
  function tryAutoApplyBrowseRecommendation() {
    if (!browseSnapshot.value.fromBrowse || browseRecommendationAutoApplied.value) return
    const recs = uploadRecommendations.value
    if (!recs.length) return

    const primary = recs[0]
    const canOverwriteDesc =
      !descriptionUserEdited.value &&
      (formData.description === SYNC_DEFAULT_DESCRIPTION || formData.description.length < 30)

    const browseTabType =
      browseSnapshot.value.teachingType && browseSnapshot.value.teachingType !== '全部'
        ? browseSnapshot.value.teachingType
        : formData.teachingType
    const syncFields =
      browseSnapshot.value.module === SYNC_PREP_COLUMN
        ? resolveSyncPrepUploadFields(browseTabType)
        : null
    const appliedType = syncFields?.apiType || browseTabType

    applyUploadRecommendation(formData, primary, {
      subjectName: subjectLabel.value,
      context: { ...recommendContext.value, teachingType: browseTabType },
      updateTitle: false,
      preserveDescription: !canOverwriteDesc,
      preserveTeachingType: true,
      silent: true,
    })
    formData.teachingType = appliedType
    if (syncFields?.subType) formData.subType = syncFields.subType
    if (syncFields?.resourceType) formData.resourceType = syncFields.resourceType

    activeRecommendationId.value = primary.id
    browseRecommendationAutoApplied.value = true
  }

  function applyRecommendationById(id: string, options?: { silent?: boolean }) {
    const rec = findRecommendationById(id)
    if (!rec) return
    applyUploadRecommendation(formData, rec, {
      subjectName: subjectLabel.value,
      context: { ...recommendContext.value, teachingType: rec.teachingType },
      updateTitle: true,
      preserveTeachingType: false,
    })
    if (isBrowseWizard.value) {
      browseSnapshot.value = {
        ...browseSnapshot.value,
        teachingType: rec.teachingType,
      }
      syncTitleFromBrowseContext()
    }
    activeRecommendationId.value = id
    if (!options?.silent) {
      ElMessage.success(`已应用推荐：${rec.teachingType}`)
    }
  }

  function applyLegacyTemplateById(id: string) {
    const tpl = LEGACY_UPLOAD_TEMPLATES.find((t) => t.id === id)
    if (!tpl) return
    Object.assign(formData, tpl.patch)
    activeRecommendationId.value = ''
    ElMessage.success(`已应用：${tpl.label}`)
  }

  function applyCompetitionChannelPreset() {
    if (route.query.channel !== 'competition') return
    formData.module = '竞赛'
    if (!formData.tags.includes('competition')) {
      formData.tags = [...formData.tags.filter((t) => t !== 'sync'), 'competition']
    }
    if (!formData.scenarios.includes('competition')) {
      formData.scenarios = [...formData.scenarios, 'competition']
    }
    if (!formData.examTypes.includes('competition')) {
      formData.examTypes = [...formData.examTypes, 'competition']
    }
    formData.difficulty = 'competition'
    if (!formData.description || formData.description.length < 10) {
      formData.description =
        '本资源为竞赛专区资料，含真题、模拟、讲义或辅导内容，适用于学科竞赛、奥数、考级与考前冲刺。'
    }
  }

  function applyTopicChannelPreset() {
    if (route.query.channel !== 'topic') return
    formData.module = '专题资源'
    if (!formData.tags.includes('topic')) {
      formData.tags = [...formData.tags.filter((t) => t !== 'sync'), 'topic']
    }
    if (!formData.scenarios.includes('topic')) {
      formData.scenarios = [...formData.scenarios, 'topic']
    }
    if (!formData.examTypes.includes('topic')) {
      formData.examTypes = [...formData.examTypes, 'topic']
    }
    formData.difficulty = 'topic'
    if (!formData.description || formData.description.length < 10) {
      formData.description =
        '本资源为专题资源，适用于寒暑假作业、开学备考、期中期末复习、升学冲刺、时事热点或跨学科项目式学习。'
    }
  }

  function applyDirectRoutePrefill(raw: Record<string, string | string[] | undefined>) {
    const str = (k: string) => {
      const v = raw[k]
      return typeof v === 'string' ? v : ''
    }
    if (str('stage')) formData.gradeLevel = str('stage')
    if (str('subject')) formData.subject = str('subject')
    if (str('module')) formData.module = str('module')
    if (str('type')) {
      const t = str('type')
      const invalidCatalogLabel = t.includes('（统编版）') || t.includes('（人教版）')
      formData.teachingType = invalidCatalogLabel
        ? getUploadModuleSchema(formData.module || '同步备课').defaultTeachingType
        : t
    }
    if (str('gradeName')) formData.gradeName = str('gradeName')
    if (str('edition')) formData.editionName = str('edition')
    if (str('unit')) {
      const u = str('unit')
      formData.unitName = u.includes('（统编版）') && !u.includes('单元') ? '' : u
    }
    if (str('lesson')) formData.lessonName = str('lesson')
    if (formData.gradeLevel && formData.subject && formData.module) {
      syncPlacementFromForm()
      if (formData.gradeName && formData.editionName) {
        placementConfirmed.value = true
      }
    }
  }

  function initFromRoute() {
    const draftParam = route.query.draftId
    const id = typeof draftParam === 'string' ? Number(draftParam) : NaN
    if (Number.isFinite(id) && id > 0) {
      void loadDraft(id)
      return
    }
    draftSavedFile.value = null
    const snapshot = parseUploadRouteQuery(route.query as Record<string, string | string[] | undefined>)
    applyBrowseSnapshot(snapshot)
    if (!snapshot.fromBrowse) {
      applyDirectRoutePrefill(route.query as Record<string, string | string[] | undefined>)
    }
    applyCompetitionChannelPreset()
    applyTopicChannelPreset()
  }

  async function loadDraft(id: number) {
    try {
      const res = await primaryChineseApi.getDraft(id)
      const draft = unwrapData(res)
      if (!draft) return
      draftId.value = draft.id
      draftSavedFile.value = null
      currentStep.value = 0
      formData.title = draft.title || ''
      formData.description = draft.description || draft.remark || ''
      formData.module = draft.module || '同步备课'
      formData.teachingType = draft.type || '课件'
      formData.gradeName = draft.gradeName || ''
      formData.editionName = draft.edition || ''
      formData.unitName = draft.unitName || ''
      formData.lessonName = draft.lessonName || ''
      formData.allowPreview = draft.allowPreview ?? 1
      formData.isFree = draft.isFree ?? 0
      formData.sortWeight = draft.sort ?? formData.sortWeight
      const section = apiStageToSection(draft.stage)
      if (section) formData.gradeLevel = section
      if (draft.subject && section) {
        const subjects = subjectDataMap[section as StageKey] || []
        const matched = subjects.find((s) => s.name === draft.subject)
        if (matched) formData.subject = matched.key
      }
      browseSnapshot.value = buildPlacementSnapshot({
        stageKey: (formData.gradeLevel || '') as StageKey | '',
        subjectKey: formData.subject,
        module: formData.module,
        teachingType: formData.teachingType,
        gradeName: formData.gradeName,
        editionName: formData.editionName,
        unitName: formData.unitName,
        lessonName: formData.lessonName,
        resourceMode: 'single',
      })
      const schema = getUploadModuleSchema(formData.module)
      const placementComplete =
        !!formData.gradeLevel &&
        !!formData.subject &&
        !!formData.module &&
        !!formData.teachingType &&
        (!schema.showSyncCatalog || (!!formData.gradeName && !!formData.editionName))
      placementConfirmed.value = placementComplete
      if (draft.ossUrl) {
        draftSavedFile.value = {
          name: draft.originalFilename || draft.title || '已保存的文件',
          url: draft.ossUrl,
          ext: draft.fileExt,
          sizeKb: draft.fileSizeKb,
        }
        tempUploadResult.value = {
          fileUrl: draft.ossUrl,
          fileName: draft.originalFilename || draft.title || '草稿文件',
          fileSize: (draft.fileSizeKb || 0) * 1024,
          fileFormat: draft.fileExt || '',
          filePath: draft.ossObjectKey || '',
          contentType: '',
          isPreviewable: false,
          previewType: '',
          uploadRecordId: 0,
          ossBucket: draft.ossBucket,
          ossObjectKey: draft.ossObjectKey,
        }
      }
      router.replace({ path: '/upload', query: { draftId: String(draft.id) } })
      ElMessage.success('已恢复草稿，请继续完善并提交审核')
    } catch {
      ElMessage.warning('草稿加载失败，将使用空白表单')
    }
  }

  function resolvePreviewStatusForPayload(): number | undefined {
    const upload = tempUploadResult.value
    if (upload?.isPreviewable) return 1
    if (formData.allowPreview === 0) return 0
    if (upload?.fileUrl || draftSavedFile.value?.url) return 2
    return undefined
  }

  function validateForSubmitClient(): string | null {
    if (!formData.title?.trim()) return '请填写资源标题'
    if (!formData.description?.trim()) return '请填写资源简介'
    if (!formData.gradeLevel) return '请选择学段'
    if (!formData.subject) return '请选择学科'
    if (!formData.teachingType) return '请选择资源类型'
    if (!hasUploadFiles.value && !draftSavedFile.value?.url && !tempUploadResult.value?.fileUrl) {
      return '请先上传文件'
    }
    if (usePrimaryChinesePath.value) {
      if (!formData.gradeName) return '请选择教材册别'
      if (!formData.editionName) return '请选择教材版本'
      if (!formData.module) return '请选择栏目'
      const schema = getUploadModuleSchema(formData.module || '同步备课')
      if (schema.showSyncCatalog) {
        const catalogId =
          (formData.catalogNodeId && formData.catalogNodeId > 0
            ? formData.catalogNodeId
            : undefined) ??
          (browseSnapshot.value.catalogNodeId && browseSnapshot.value.catalogNodeId > 0
            ? browseSnapshot.value.catalogNodeId
            : undefined)
        if (!catalogId) return '请选择目录位置（单元/课文）'
      }
    } else if (!formData.resourceType) {
      return '请选择资源类型'
    }
    return null
  }

  function buildSyncPayload(
    fileUrl: string,
    fileSizeKb: number,
    ext: string,
    status: number = SYNC_UPLOAD_DEFAULT_STATUS,
    options?: { file?: File; title?: string },
  ): SyncUploadPayload {
    const snap = browseSnapshot.value
    const file = options?.file ?? uploadFile.value
    return {
      id: draftId.value ?? undefined,
      stage: gradeLevelLabel.value || '小学',
      subject: subjectLabel.value || snap.subjectKey,
      module: formData.module || '同步备课',
      type: formData.teachingType,
      gradeName: formData.gradeName,
      edition: formData.editionName,
      brandCode: snap.brandCode || undefined,
      catalogNodeId:
        (formData.catalogNodeId && formData.catalogNodeId > 0
          ? formData.catalogNodeId
          : undefined) ??
        (snap.catalogNodeId && snap.catalogNodeId > 0 ? snap.catalogNodeId : undefined),
      subType: formData.subType || undefined,
      unitName: snap.unitNameForApi || formData.unitName || undefined,
      lessonName: snap.lessonName || formData.lessonName || undefined,
      title: options?.title || formData.title,
      originalFilename: file?.name,
      fileExt: ext,
      ossUrl: fileUrl || undefined,
      ossBucket: tempUploadResult.value?.ossBucket,
      ossObjectKey: tempUploadResult.value?.ossObjectKey || tempUploadResult.value?.filePath,
      fileSizeKb: fileUrl ? fileSizeKb : undefined,
      status,
      description: formData.description,
      allowPreview: formData.allowPreview,
      previewStatus: resolvePreviewStatusForPayload(),
      isFree: formData.isFree,
      sort: formData.sortWeight,
      lessonPlanJson: buildLessonPlanJsonFromFlowText(formData.teachingFlowText),
    }
  }

  function handleResourceTypeChange() {
    formData.subType = ''
  }


  function resetServerPreviewState() {
    serverPreviewUrl.value = ''
    serverPreviewType.value = ''
    serverPreviewMode.value = ''
    serverPreviewProvider.value = ''
    serverPreviewMessage.value = ''
    serverPreviewInfo.value = null
    serverPreviewReady.value = false
  }

  async function cleanupTempUpload() {
    const path = tempUploadResult.value?.filePath
    tempUploadResult.value = null
    resetServerPreviewState()
    if (path) {
      try {
        await fileApi.delete(path)
      } catch {
        /* 临时文件可能已被清理 */
      }
    }
  }

  function clearLocalPreviewResources() {
    if (previewUrl.value) {
      URL.revokeObjectURL(previewUrl.value)
      previewUrl.value = ''
    }
    docxPreviewHtml.value = ''
  }

  async function clearPreviewResources() {
    clearLocalPreviewResources()
    previewError.value = ''
    serverPreviewWarning.value = ''
    previewLoading.value = false
    await cleanupTempUpload()
  }

  async function buildLocalPreview(file: File, meta: FormatPreviewMeta) {
    if (!canLocalPreview(meta)) return

    if (meta.localPreviewType === 'docx') {
      try {
        const arrayBuffer = await file.arrayBuffer()
        const { default: mammoth } = await import('mammoth')
        const result = await mammoth.convertToHtml({ arrayBuffer })
        docxPreviewHtml.value = result.value
        if (!docxPreviewHtml.value.trim()) {
          previewError.value = '未能解析 Word 文档内容，请检查文件是否损坏'
        }
      } catch {
        previewError.value = 'Word 本地预览失败，已尝试云端预览'
      }
      return
    }

    previewUrl.value = URL.createObjectURL(file)
  }

  async function uploadTempFileForPreview(file: File, generation: number) {
    const fd = new FormData()
    fd.append('file', file)
    const uploadRes = await fileApi.upload(fd)
    if (generation !== fileSelectGeneration) return

    const uploaded = unwrapData(uploadRes)
    tempUploadResult.value = uploaded

    if (!uploaded.isPreviewable) {
      serverPreviewReady.value = false
      return
    }

    serverPreviewType.value = uploaded.previewType || formatMeta.value?.serverPreviewType || ''

    try {
      const infoRes = await fileApi.getPreviewInfo(uploaded.fileUrl)
      const info = unwrapData(infoRes)
      if (generation !== fileSelectGeneration) return
      if (info?.previewUrl) {
        serverPreviewInfo.value = info
        serverPreviewUrl.value = info.previewUrl
        serverPreviewType.value = info.previewType || serverPreviewType.value
        serverPreviewMode.value = info.previewMode || ''
        serverPreviewProvider.value = info.provider || ''
        serverPreviewMessage.value = info.message || ''
        serverPreviewReady.value = true
        return
      }
    } catch {
      /* 回退旧 preview 接口 */
    }

    let accessibleUrl = uploaded.fileUrl || ''
    try {
      const previewRes = await fileApi.getPreviewUrl(uploaded.fileUrl)
      const resolved = unwrapData(previewRes)
      if (resolved) accessibleUrl = resolved
    } catch {
      /* 使用 upload 返回的 fileUrl */
    }

    if (generation !== fileSelectGeneration) return

    serverPreviewUrl.value = accessibleUrl
    serverPreviewMode.value = 'native'
    serverPreviewProvider.value = 'native'
    serverPreviewInfo.value = null
    serverPreviewReady.value = !!accessibleUrl
  }

  async function setUploadFiles(files: File[]) {
    uploadFiles.value = files
    if (!files.length) {
      await setUploadFile(null)
      return
    }
    await setUploadFile(files[0])
  }

  async function removeSuiteFile(index: number) {
    const next = uploadFiles.value.filter((_, i) => i !== index)
    await setUploadFiles(next)
  }

  async function setUploadFile(file: File | null) {
    fileSelectGeneration += 1
    const generation = fileSelectGeneration

    await clearPreviewResources()
    uploadFile.value = file
    if (!isSuiteMode.value) {
      uploadFiles.value = file ? [file] : []
    }
    formatMeta.value = null

    if (!file) return

    const ext = getFileExtension(file.name)
    formatMeta.value = getFormatPreviewMeta(ext)

    if (!formatMeta.value) {
      previewError.value = `不支持上传 .${ext} 格式`
      return
    }

    if (file.size > formatMeta.value.maxSizeMb * 1024 * 1024) {
      previewError.value = `文件超过 ${formatMeta.value.maxSizeMb}MB 限制`
      return
    }

    try {
      const checkRes = await fileApi.checkFormat(file.name)
      const supported = unwrapData(checkRes)
      if (supported === false) {
        previewError.value = '服务器不支持该文件格式'
        return
      }
    } catch {
      /* 离线或接口不可用时沿用本地格式表 */
    }

    const meta = formatMeta.value
    previewLoading.value = true
    previewError.value = ''
    serverPreviewWarning.value = ''

    try {
      await Promise.all([
        canLocalPreview(meta) ? buildLocalPreview(file, meta) : Promise.resolve(),
        meta.isPreviewable ? uploadTempFileForPreview(file, generation) : Promise.resolve(),
      ])

      if (generation !== fileSelectGeneration) return

      const hasLocal =
        canLocalPreview(meta) &&
        (meta.localPreviewType === 'docx'
          ? !!docxPreviewHtml.value.trim()
          : !!previewUrl.value)
      const hasServer = serverPreviewReady.value && !!serverPreviewUrl.value

      if (!hasLocal && !hasServer && meta.isPreviewable) {
        previewError.value = '云端预览加载失败，请检查网络后重试'
      } else if (!hasLocal && !meta.isPreviewable) {
        previewError.value = '该格式暂不支持预览'
      }
    } catch (e: unknown) {
      if (generation === fileSelectGeneration) {
        const msg = e instanceof Error ? e.message : '文件上传失败'
        const hasLocalNow =
          canLocalPreview(meta) &&
          (meta.localPreviewType === 'docx'
            ? !!docxPreviewHtml.value.trim()
            : !!previewUrl.value)
        if (hasLocalNow) {
          serverPreviewWarning.value = msg.includes('不支持')
            ? msg
            : `云端上传失败：${msg}（已显示本地预览，提交前请确认服务可用）`
          previewError.value = ''
        } else {
          previewError.value = msg.includes('不支持') ? msg : `云端上传失败：${msg}`
        }
      }
    } finally {
      if (generation === fileSelectGeneration) {
        previewLoading.value = false
      }
    }
  }

  function tempUploadMatchesCurrentFile(): boolean {
    const temp = tempUploadResult.value
    const file = uploadFile.value
    if (!temp || !file) return false
    return temp.fileName === file.name && temp.fileSize === file.size
  }

  async function ensureFileUploaded(target?: File): Promise<UploadResult> {
    const file = target ?? uploadFile.value!
    if (!target && tempUploadMatchesCurrentFile() && tempUploadResult.value?.fileUrl) {
      return tempUploadResult.value
    }
    const fd = new FormData()
    fd.append('file', file)
    const uploadRes = await fileApi.upload(fd)
    const uploaded = unwrapData(uploadRes)
    if (!target) {
      tempUploadResult.value = uploaded
    }
    return uploaded
  }

  function formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`
  }

  function getFileIcon(format: string) {
    const videoFormats = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv', 'webm']
    const audioFormats = ['mp3', 'wav', 'flac', 'aac', 'ogg', 'wma']
    if (videoFormats.includes(format)) return Film
    if (audioFormats.includes(format)) return Headset
    return Files
  }

  function handlePrev() {
    if (currentStep.value <= 0) return
    if (currentStep.value === 4 && showClassificationStep.value) {
      currentStep.value = 3
      return
    }
    if (currentStep.value === 3 && !showClassificationStep.value && isDirectWizard.value) {
      currentStep.value = 2
      return
    }
    currentStep.value--
  }

  async function handleNext() {
    if (isDirectWizard.value && currentStep.value === 0) {
      if (!confirmPlacementStep()) {
        ElMessage.warning('请完整选择上传位置（同步备课需填写册别、版本与资源类型）')
        return
      }
    } else if (isBrowseWizard.value && currentStep.value === 0) {
      try {
        await basicFormRef.value?.validate()
      } catch {
        ElMessage.warning('请完善必填信息')
        return
      }
    } else if (isDirectWizard.value && currentStep.value === 1) {
      try {
        await basicFormRef.value?.validate()
      } catch {
        ElMessage.warning('请完善必填信息')
        return
      }
    }
    const fileStep = isBrowseWizard.value ? 0 : 2
    if (!hasUploadFiles.value && currentStep.value === fileStep) {
      ElMessage.warning(isSuiteMode.value ? '请先添加成套文件' : '请先上传文件')
      return
    }
    if (currentStep.value >= wizardMaxStep.value) return
    if (currentStep.value === 2 && isDirectWizard.value && !showClassificationStep.value) {
      currentStep.value = 3
      return
    }
    currentStep.value++
  }

  async function handleSaveDraft() {
    submitting.value = true
    try {
      let fileUrl = tempUploadResult.value?.fileUrl || draftSavedFile.value?.url || ''
      let fileSizeKb = 0
      let ext = (detectedFormat.value || '').toLowerCase()
      if (uploadFile.value && !fileUrl) {
        try {
          const uploaded = await ensureFileUploaded()
          fileUrl = uploaded.fileUrl || ''
          fileSizeKb = Math.max(1, Math.ceil((uploaded.fileSize || uploadFile.value.size) / 1024))
          ext = (uploaded.fileFormat || ext).toLowerCase()
        } catch {
          // 草稿允许暂不上传文件
        }
      } else if (tempUploadResult.value) {
        fileSizeKb = Math.max(1, Math.ceil((tempUploadResult.value.fileSize || 0) / 1024))
      }
      const payload = buildSyncPayload(fileUrl, fileSizeKb, ext, RESOURCE_STATUS_DRAFT)
      const saved = await persistDraftRecord(payload)
      if (saved?.id) {
        draftId.value = saved.id
        router.replace({
          query: { ...route.query, draftId: String(saved.id) },
        })
      }
      ElMessage.success('草稿已保存，可继续编辑或前往草稿箱查看')
    } catch (error: unknown) {
      const msg = error instanceof Error ? error.message : '草稿保存失败'
      ElMessage.error(msg)
    } finally {
      submitting.value = false
    }
  }

  function navigateAfterSubmit() {
    router.push({ path: '/my-resources', query: { status: 'pending' } })
  }

  function navigateAfterSaveDraft() {
    router.push({ path: '/my-resources', query: { status: 'draft' } })
  }

  async function handleCancel() {
    await clearPreviewResources()
    const back = returnLocation.value
    if (back) router.push(back)
    else router.push('/')
  }

  async function submitOneSyncResource(file?: File, titleOverride?: string) {
    const files =
      file != null
        ? [file]
        : isSuiteMode.value
          ? uploadFiles.value
          : uploadFile.value
            ? [uploadFile.value]
            : []

    let fileUrl = tempUploadResult.value?.fileUrl || draftSavedFile.value?.url || ''
    let fileSizeKb = draftSavedFile.value?.sizeKb || 1
    let ext = (draftSavedFile.value?.ext || detectedFormat.value || '').toLowerCase()
    let activeFile: File | undefined = files[0]

    if (files.length === 1) {
      const f = files[0]
      const uploaded = await ensureFileUploaded(f)
      fileUrl = uploaded.fileUrl || ''
      fileSizeKb = Math.max(1, Math.ceil((uploaded.fileSize || f.size) / 1024))
      ext = (uploaded.fileFormat || getFileExtension(f.name) || '').toLowerCase()
      activeFile = f
    } else if (!fileUrl) {
      throw new Error('请先上传文件')
    }

    const payload = buildSyncPayload(fileUrl, fileSizeKb, ext, RESOURCE_STATUS_DRAFT, {
      file: activeFile,
      title: titleOverride,
    })
    const saved = await persistDraftRecord(payload)
    if (!saved?.id) throw new Error('保存草稿失败')
    await finalizeDraftSubmit({ ...payload, id: saved.id }, saved.id, RESOURCE_STATUS_PENDING)
  }

  async function submitSyncResource() {
    const files = isSuiteMode.value ? uploadFiles.value : uploadFile.value ? [uploadFile.value] : []

    if (files.length > 1) {
      let saved = 0
      for (let i = 0; i < files.length; i++) {
        const file = files[i]
        const title = suggestSuiteItemTitle(formData.title, file.name, i + 1)
        await submitOneSyncResource(file, title)
        saved++
      }
      if (saved > 1) {
        ElMessage.success(`已提交审核 ${saved} 份资源，审核通过后将展示在列表中`)
      }
    } else {
      await submitOneSyncResource()
    }

    tempUploadResult.value = null
    draftSavedFile.value = null
    draftId.value = null
  }

  async function submitLegacyResource() {
    const fd = new FormData()
    fd.append('file', uploadFile.value!)
    fd.append('title', formData.title)
    fd.append('description', formData.description)
    fd.append('gradeLevel', formData.gradeLevel)
    fd.append('subject', formData.subject)
    fd.append('grade', formData.grade)
    fd.append('resourceType', formData.resourceType)
    fd.append('subType', formData.subType)
    fd.append('version', formData.version)
    const tagExportMap: Record<string, string> = {
      ...RESOURCE_TAG_LABELS,
      competition: '学科竞赛',
      topic: '专题资源',
      review: '复习',
    }
    const exportedTags = formData.tags.map((t) => tagExportMap[t] || t)
    if (route.query.channel === 'competition' && !exportedTags.includes('学科竞赛')) {
      exportedTags.push('学科竞赛')
    }
    if (route.query.channel === 'topic' && !exportedTags.includes('专题资源')) {
      exportedTags.push('专题资源')
    }
    fd.append('tags', exportedTags.join(','))
    fd.append('examType', formData.examTypes.join(','))
    fd.append('isFree', String(formData.isFree))
    fd.append('difficulty', formData.difficulty)
    fd.append('sortWeight', String(formData.sortWeight))

    await fileApi.uploadResource(fd)
  }

  async function handleSubmit() {
    if (isDirectWizard.value && isSyncUpload.value && !placementConfirmed.value) {
      ElMessage.warning('请先完成「选择上传位置」步骤')
      currentStep.value = 0
      return
    }
    if (!fromBrowse.value && (!formData.gradeLevel || !formData.subject)) {
      ElMessage.warning('请从学科页选择上传位置，或先填写学段与学科')
      return
    }
    if (!canSubmit.value) {
      if (usingOfflineOptions.value) {
        ElMessage.warning('分类服务离线中，当前仅可保存草稿，请稍后重试提交审核')
      } else {
        ElMessage.warning('请完善所有必填信息')
      }
      return
    }
    const submitErr = validateForSubmitClient()
    if (submitErr) {
      ElMessage.warning(submitErr)
      return
    }
    if (!hasUploadFiles.value) {
      ElMessage.warning(isSuiteMode.value ? '请先添加成套文件' : '请先上传文件')
      return
    }

    submitting.value = true
    try {
      if (usePrimaryChinesePath.value) {
        const suiteCount = isSuiteMode.value ? uploadFiles.value.length : 1
        await submitSyncResource()
        if (suiteCount <= 1) {
          ElMessage.success('已提交审核，可在「我的资源 - 待审核」查看进度')
        }
      } else {
        await submitLegacyResource()
        ElMessage.success('资源上传成功！')
      }
      navigateAfterSubmit()
    } catch (error: unknown) {
      const msg = error instanceof Error ? error.message : '提交失败，请重试'
      ElMessage.error(msg)
    } finally {
      submitting.value = false
    }
  }

  onMounted(() => {
    initFromRoute()
    void initCascade().then(() => {
      if (!formData.module) formData.module = '同步备课'
      if (!formData.teachingType) formData.teachingType = '课件'
    })
  })

  onUnmounted(() => {
    void clearPreviewResources()
  })

  watch(
    () => route.query,
    () => {
      browseRecommendationAutoApplied.value = false
      initFromRoute()
    },
  )

  watch(
    () => formData.description,
    (val, oldVal) => {
      if (oldVal !== undefined && val !== SYNC_DEFAULT_DESCRIPTION) {
        descriptionUserEdited.value = true
      }
    },
  )

  watch(isSuiteMode, async (suite, wasSuite) => {
    if (suite === wasSuite) return
    await setUploadFiles([])
  })

  watch(
    () => [formData.module, formData.gradeLevel, formData.lessonName] as const,
    () => {
      if (!browseSnapshot.value.fromBrowse && uploadRecommendations.value.length) {
        const stillActive = uploadRecommendations.value.some(
          (r) => r.id === activeRecommendationId.value,
        )
        if (!stillActive) activeRecommendationId.value = ''
      }
    },
  )

  return {
    currentStep,
    basicFormRef,
    submitting,
    uploadFile,
    uploadFiles,
    isSuiteMode,
    hasUploadFiles,
    showClassificationStep,
    isDirectPreviewStep,
    uploadModuleSchema,
    previewUrl,
    formatMeta,
    localPreviewType,
    docxPreviewHtml,
    previewLoading,
    previewError,
    serverPreviewWarning,
    previewStatusLabel,
    tempUploadResult,
    serverPreviewUrl,
    serverPreviewType,
    serverPreviewMode,
    serverPreviewProvider,
    serverPreviewMessage,
    serverPreviewInfo,
    serverPreviewReady,
    hasLocalPreviewContent,
    hasServerPreview,
    activePreviewKind,
    formData,
    activeRecommendationId,
    uploadRecommendations,
    showUploadRecommendations,
    recommendContextLine,
    recommendAutoHint,
    browseSnapshot,
    draftId,
    isDraftEditMode,
    draftSavedFile,
    basicRules,
    fromBrowse,
    isBrowseWizard,
    isDirectWizard,
    placementConfirmed,
    placementReady,
    syncPlacementFromForm,
    wizardMaxStep,
    submitButtonLabel,
    usePrimaryChinesePath,
    resourceModeLabel,
    isSyncUpload,
    showTeachingFlowField,
    contextSummary,
    syncTeachingTypes,
    stageOptions,
    currentSubjects,
    currentGrades,
    moduleOptions,
    editionOptions,
    gradeOptions,
    resourceTypeOptions,
    optionsLoading,
    treeLoading,
    unitTree,
    selectedUnit,
    lessonOptions,
    canLoadCatalogTree,
    currentSubTypes,
    currentTextbookVersions,
    currentExamTypes,
    detectedFormat,
    isPreviewable,
    isImageFile,
    canNext,
    canSubmit,
    gradeLevelLabel,
    subjectLabel,
    gradeLabel,
    resourceTypeLabel,
    subTypeLabel,
    difficultyLabel,
    scenariosLabel,
    examTypesLabel,
    tagsLabel,
    uploadResourceTagOptions,
    handleGradeLevelChange,
    handleSubjectChange,
    handleModuleChange,
    handleCatalogChange,
    handleUnitChange,
    handleLessonChange,
    usingOfflineOptions,
    handleResourceTypeChange,
    applyRecommendationById,
    applyLegacyTemplateById,
    setUploadFile,
    setUploadFiles,
    removeSuiteFile,
    formatFileSize,
    getFileIcon,
    handlePrev,
    handleNext,
    handleSaveDraft,
    handleCancel,
    handleSubmit,
  }
}

