/**
 * Phase 2 Step 3：上传页分类维度级联（taxonomy + catalog 统一读源）
 * 供 useResourceUploadForm、UploadStepPlacement、ResourceUploadDialog 共用
 */
import { ref, computed, watch, type Ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { UnitTreeNode } from '@/api/types'
import type { CatalogNode } from '@/types/browse'
import {
  loadStages,
  loadSubjects,
  loadGrades,
  loadModules,
  loadEditions,
  loadEditionGroups,
  loadVolumes,
  loadResourceTypes,
  fallbackTextbookVersionGroups,
  resetTaxonomyOfflineFallback,
  taxonomyUsingOfflineFallback,
  type TaxonomyEditionGroup,
} from '@/composables/taxonomySource'
import {
  catalogNodesToUnitTree,
  findCatalogNodeId,
  loadUploadCatalogTree,
  type UploadCatalogTreeParams,
} from '@/composables/catalogSource'
import type { UploadBrowseSnapshot } from '@/utils/uploadRoute'
import { findTextbookRootNode } from '@/utils/sortResourceTypes'

export interface UploadTaxonomyFormSlice {
  gradeLevel: string
  subject: string
  grade: string
  module: string
  editionName: string
  gradeName: string
  unitName: string
  lessonName: string
  teachingType: string
  version: string
  examTypes?: string[]
  catalogNodeId?: number
}

export interface TextbookVersionGroup {
  label: string
  options: { label: string; value: string }[]
}

function mapEditionGroupsToVersionGroups(groups: TaxonomyEditionGroup[]): TextbookVersionGroup[] {
  if (!groups.length) return []
  return groups.map((g) => ({
    label: g.publisher,
    options: g.editions.map((e) => ({ label: e.name, value: e.code })),
  }))
}

function fallbackTextbookVersions(stageKey: string): TextbookVersionGroup[] {
  return fallbackTextbookVersionGroups(stageKey)
}

export function useUploadTaxonomyCascade(
  form: UploadTaxonomyFormSlice,
  options?: {
    browseSnapshot?: Ref<UploadBrowseSnapshot>
    onCascadeChange?: () => void
  },
) {
  const stageOptions = ref<{ key: string; name: string }[]>([])
  const currentSubjects = ref<{ label: string; value: string }[]>([])
  const currentGrades = ref<{ label: string; value: string }[]>([])
  const moduleOptions = ref<string[]>([])
  const editionOptions = ref<string[]>([])
  const gradeOptions = ref<string[]>([])
  const resourceTypeOptions = ref<string[]>([])
  const currentTextbookVersions = ref<TextbookVersionGroup[]>([])

  const optionsLoading = ref(false)
  const treeLoading = ref(false)
  const catalogTree = ref<CatalogNode[]>([])
  const unitTree = ref<UnitTreeNode[]>([])
  const selectedUnit = ref('')
  let offlineToastShown = false

  const usingOfflineOptions = computed(() => taxonomyUsingOfflineFallback.value)

  function notifyOfflineIfNeeded() {
    if (taxonomyUsingOfflineFallback.value && !offlineToastShown) {
      offlineToastShown = true
      ElMessage.warning(
        '分类服务暂不可用，当前使用离线默认选项；提交审核已禁用，仍可保存草稿',
      )
    }
  }

  const subjectLabel = computed(() => {
    return currentSubjects.value.find((s) => s.value === form.subject)?.label || form.subject || ''
  })

  const lessonOptions = computed(() => {
    if (!selectedUnit.value) return []
    const node = unitTree.value.find((u) => u.name === selectedUnit.value)
    return node?.subUnits || []
  })

  const canLoadCatalogTree = computed(
    () => !!form.gradeName && !!form.editionName && !!form.subject,
  )

  function syncCatalogNodeId(nodeId?: number) {
    const id = nodeId && nodeId > 0 ? nodeId : 0
    form.catalogNodeId = id > 0 ? id : undefined
    if (options?.browseSnapshot) {
      options.browseSnapshot.value = {
        ...options.browseSnapshot.value,
        catalogNodeId: id,
      }
    }
  }

  function clearCatalogFields() {
    selectedUnit.value = ''
    form.gradeName = ''
    form.editionName = ''
    form.unitName = ''
    form.lessonName = ''
    syncCatalogNodeId(0)
    catalogTree.value = []
    unitTree.value = []
    editionOptions.value = []
    gradeOptions.value = []
  }

  function clearUnitAndLessonOnly() {
    selectedUnit.value = ''
    form.unitName = ''
    form.lessonName = ''
    syncCatalogNodeId(0)
    unitTree.value = []
    catalogTree.value = []
  }

  function clearSubjectDependentFields() {
    clearCatalogFields()
    form.version = ''
  }

  async function refreshStageOptions() {
    stageOptions.value = await loadStages()
  }

  async function loadSubjectsAndGrades(stageKey?: string) {
    const stage = stageKey ?? form.gradeLevel
    if (!stage) {
      currentSubjects.value = []
      currentGrades.value = []
      moduleOptions.value = []
      return
    }
    const [subjects, grades, modules] = await Promise.all([
      loadSubjects(stage),
      loadGrades(stage),
      loadModules(stage),
    ])
    currentSubjects.value = subjects.map((s) => ({ label: s.name, value: s.key }))
    currentGrades.value = grades.map((g) => ({ label: g.name, value: g.code }))
    moduleOptions.value = modules.map((m) => m.name).filter(Boolean)
    if (form.subject && !currentSubjects.value.some((s) => s.value === form.subject)) {
      form.subject = ''
    }
    if (form.grade && !currentGrades.value.some((g) => g.value === form.grade)) {
      form.grade = ''
    }
    if (form.module && !moduleOptions.value.includes(form.module)) {
      moduleOptions.value = [...moduleOptions.value, form.module]
    }
    notifyOfflineIfNeeded()
  }

  async function loadEditionAndResourceTypes() {
    if (!form.gradeLevel || !form.subject) {
      editionOptions.value = []
      resourceTypeOptions.value = []
      currentTextbookVersions.value = form.gradeLevel
        ? fallbackTextbookVersions(form.gradeLevel)
        : []
      return
    }
    const [editions, types, groups] = await Promise.all([
      loadEditions(form.gradeLevel, form.subject),
      loadResourceTypes(form.gradeLevel, form.subject, form.module || undefined),
      loadEditionGroups(form.gradeLevel, form.subject),
    ])
    editionOptions.value = editions.map((e) => e.name).filter(Boolean)
    resourceTypeOptions.value = types.map((t) => t.name).filter(Boolean)
    const mappedGroups = mapEditionGroupsToVersionGroups(groups)
    currentTextbookVersions.value = mappedGroups.length
      ? mappedGroups
      : fallbackTextbookVersions(form.gradeLevel)
    if (form.editionName && !editionOptions.value.includes(form.editionName)) {
      form.editionName = ''
    }
  }

  async function loadVolumeNames() {
    if (!form.gradeLevel) {
      gradeOptions.value = []
      return
    }
    const volumes = await loadVolumes(form.gradeLevel)
    gradeOptions.value = volumes.map((v) => v.name).filter(Boolean)
    if (form.gradeName && !gradeOptions.value.includes(form.gradeName)) {
      form.gradeName = ''
    }
  }

  async function loadPlacementFilters() {
    if (!form.gradeLevel || !form.subject) {
      editionOptions.value = []
      if (form.gradeLevel) {
        await Promise.all([loadVolumeNames(), loadSubjectsAndGrades()])
      } else {
        gradeOptions.value = []
        moduleOptions.value = []
      }
      return
    }
    optionsLoading.value = true
    try {
      const [volumes, modules, types, editions, groups] = await Promise.all([
        loadVolumes(form.gradeLevel),
        loadModules(form.gradeLevel),
        loadResourceTypes(form.gradeLevel, form.subject, form.module || undefined),
        loadEditions(form.gradeLevel, form.subject),
        loadEditionGroups(form.gradeLevel, form.subject),
      ])
      gradeOptions.value = volumes.map((v) => v.name).filter(Boolean)
      moduleOptions.value = modules.map((m) => m.name).filter(Boolean)
      resourceTypeOptions.value = types.map((t) => t.name).filter(Boolean)
      editionOptions.value = editions.map((e) => e.name).filter(Boolean)
      const mappedGroups = mapEditionGroupsToVersionGroups(groups)
      currentTextbookVersions.value = mappedGroups.length
        ? mappedGroups
        : fallbackTextbookVersions(form.gradeLevel)
      if (!gradeOptions.value.length || !editionOptions.value.length) {
        ElMessage.warning('教材目录选项为空，请检查分类配置或稍后重试')
      }
      if (form.gradeName && !gradeOptions.value.includes(form.gradeName)) {
        form.gradeName = ''
      }
      if (form.editionName && !editionOptions.value.includes(form.editionName)) {
        form.editionName = ''
      }
      notifyOfflineIfNeeded()
    } catch {
      gradeOptions.value = []
      editionOptions.value = []
      ElMessage.warning('加载教材目录选项失败，请刷新页面或稍后重试')
    } finally {
      optionsLoading.value = false
    }
  }

  function clearTextbookRootUnitSelection() {
    const root = findTextbookRootNode(catalogTree.value)
    const rootName = root?.name?.trim()
    if (!rootName) return
    const picked = (selectedUnit.value || form.unitName || '').trim()
    if (picked === rootName) {
      selectedUnit.value = ''
      form.unitName = ''
      form.lessonName = ''
      syncCatalogNodeId(0)
    }
  }

  async function loadUnitTree() {
    if (!canLoadCatalogTree.value) {
      catalogTree.value = []
      unitTree.value = []
      return
    }
    treeLoading.value = true
    try {
      const params: UploadCatalogTreeParams = {
        gradeName: form.gradeName,
        edition: form.editionName,
        subject: subjectLabel.value,
      }
      const tree = await loadUploadCatalogTree(params)
      catalogTree.value = tree
      unitTree.value = catalogNodesToUnitTree(tree)
      if (form.unitName && !selectedUnit.value) {
        selectedUnit.value = form.unitName
      }
      clearTextbookRootUnitSelection()
      resolveCatalogNodeFromSelection()
    } catch {
      catalogTree.value = []
      unitTree.value = []
    } finally {
      treeLoading.value = false
    }
  }

  function resolveCatalogNodeFromSelection() {
    if (!catalogTree.value.length) return
    const nodeId = findCatalogNodeId(
      catalogTree.value,
      selectedUnit.value || form.unitName,
      form.lessonName || undefined,
    )
    if (nodeId) syncCatalogNodeId(nodeId)
  }

  async function handleGradeLevelChange() {
    form.subject = ''
    form.grade = ''
    form.module = ''
    form.version = ''
    form.examTypes = []
    clearCatalogFields()
    await Promise.all([loadSubjectsAndGrades(), loadVolumeNames()])
    options?.onCascadeChange?.()
  }

  async function handleSubjectChange() {
    clearSubjectDependentFields()
    await loadPlacementFilters()
    void loadUnitTree()
    options?.onCascadeChange?.()
  }

  async function handleModuleChange(showSyncCatalog?: boolean) {
    if (showSyncCatalog === false) {
      clearCatalogFields()
    } else if (showSyncCatalog) {
      clearUnitAndLessonOnly()
    }
    await loadPlacementFilters()
    if (showSyncCatalog && canLoadCatalogTree.value) {
      void loadUnitTree()
    }
    options?.onCascadeChange?.()
  }

  function handleCatalogChange() {
    selectedUnit.value = ''
    form.unitName = ''
    form.lessonName = ''
    syncCatalogNodeId(0)
    void loadUnitTree()
    options?.onCascadeChange?.()
  }

  function handleUnitChange(unit: string) {
    form.unitName = unit || ''
    form.lessonName = ''
    selectedUnit.value = unit || ''
    resolveCatalogNodeFromSelection()
    options?.onCascadeChange?.()
  }

  function handleLessonChange() {
    resolveCatalogNodeFromSelection()
    options?.onCascadeChange?.()
  }

  async function initCascade() {
    resetTaxonomyOfflineFallback()
    offlineToastShown = false
    await refreshStageOptions()
    if (form.gradeLevel) {
      await loadSubjectsAndGrades()
      await loadVolumeNames()
      if (form.subject) {
        await loadPlacementFilters()
      }
      if (form.unitName) selectedUnit.value = form.unitName
      if (canLoadCatalogTree.value) await loadUnitTree()
    }
    notifyOfflineIfNeeded()
  }

  watch(
    () => form.gradeLevel,
    (stage, prev) => {
      if (!stage || stage === prev) return
      void loadSubjectsAndGrades(stage)
    },
  )

  watch(
    () => [form.gradeName, form.editionName, form.subject] as const,
    () => {
      if (canLoadCatalogTree.value) void loadUnitTree()
    },
  )

  return {
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
    catalogTree,
    unitTree,
    selectedUnit,
    subjectLabel,
    lessonOptions,
    canLoadCatalogTree,
    usingOfflineOptions,
    refreshStageOptions,
    loadSubjectsAndGrades,
    loadPlacementFilters,
    loadUnitTree,
    handleGradeLevelChange,
    handleSubjectChange,
    handleModuleChange,
    handleCatalogChange,
    handleUnitChange,
    handleLessonChange,
    initCascade,
    syncCatalogNodeId,
  }
}
