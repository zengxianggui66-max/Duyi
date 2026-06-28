import { ref, computed, watch, type Ref, type ComputedRef } from 'vue'
import { resourceGateway } from '@/api/resourceGateway'
import type { PrimaryChineseItem } from '@/api/types'
import { stageNames, type StageKey } from '@/config/subjectConfig'
import { USE_CATALOG_BROWSE } from '@/config/featureFlags'
import { normalizeEditionLabel } from '@/utils/editionAdapter'

export const LESSON_TYPE_ORDER = [
  '课件',
  '教案',
  '练习',
  '学案',
  '试卷',
  '电子课本',
  '教学反思',
  '音频/朗读',
  '视频',
  '知识点',
  '其他',
] as const

export const LESSON_TYPE_ICONS: Record<string, string> = {
  课件: 'slides',
  教案: 'file-text',
  练习: 'pencil',
  学案: 'notebook',
  试卷: 'clipboard-list',
  视频: 'video',
  '音频/朗读': 'audio-lines',
  知识点: 'list-checks',
  电子课本: 'book-open',
  教学反思: 'message-square-text',
  其他: 'folder',
}

export interface LessonTypeStat {
  type: string
  count: number
}

export interface LessonResourceGroup {
  type: string
  items: PrimaryChineseItem[]
}

export function useLessonHub(
  currentStage: Ref<string>,
  currentSubject: Ref<{ name?: string } | null>,
  currentGradeLevelName: Ref<string>,
  selectedVersionName: Ref<string>,
  activeColumn: Ref<string>,
  activeUnit: Ref<string>,
  activeResourceType: Ref<string>,
  searchKeyword: Ref<string>,
  sortType: Ref<string>,
  resolveParentUnitName: (unitName: string) => string,
  brandCode: Ref<string | undefined> | ComputedRef<string | undefined>,
  catalogNodeId?: Ref<number | null> | ComputedRef<number | null>,
) {
  const lessonAllResources = ref<PrimaryChineseItem[]>([])
  const lessonLoading = ref(false)

  const isLessonHubActive = computed(() => {
    // 目录浏览模式下（无论是真实节点还是兼容回落节点），统一走右侧主列表区，
    // 避免回落节点（负数 id）误进入 lesson-hub 导致筛选口径不一致。
    if (USE_CATALOG_BROWSE && catalogNodeId?.value != null) {
      return false
    }
    const unit = activeUnit.value
    if (!unit) return false
    const parent = resolveParentUnitName(unit)
    return parent !== unit
  })

  const lessonName = computed(() => (isLessonHubActive.value ? activeUnit.value : ''))

  const parentUnitName = computed(() => {
    if (!isLessonHubActive.value) return activeUnit.value || ''
    return resolveParentUnitName(activeUnit.value)
  })

  function buildLessonListParams() {
    const nodeId = catalogNodeId?.value
    if (USE_CATALOG_BROWSE && nodeId != null && nodeId > 0) {
      return {
        stage: stageNames[currentStage.value as StageKey] || undefined,
        subject: currentSubject.value?.name || undefined,
        module: activeColumn.value || undefined,
        gradeName: currentGradeLevelName.value || undefined,
        edition: selectedVersionName.value || undefined,
        keyword: searchKeyword.value?.trim() || undefined,
        brandCode: brandCode.value || undefined,
        catalogNodeId: nodeId,
        includeSubtree: false,
      }
    }

    const unit = activeUnit.value
    const parent = resolveParentUnitName(unit)
    const isLeaf = !!unit && parent !== unit

    const normalizedEdition = selectedVersionName.value
      ? normalizeEditionLabel(selectedVersionName.value)
      : undefined

    return {
      stage: stageNames[currentStage.value as StageKey] || undefined,
      subject: currentSubject.value?.name || undefined,
      module: activeColumn.value || undefined,
      gradeName: currentGradeLevelName.value || undefined,
      edition: normalizedEdition || undefined,
      unitName: parent || undefined,
      lessonName: isLeaf ? unit : undefined,
      keyword: searchKeyword.value?.trim() || undefined,
      brandCode: brandCode.value || undefined,
    }
  }

  function sortItems(items: PrimaryChineseItem[]) {
    const list = [...items]
    if (sortType.value === 'latest') {
      list.sort((a, b) => (b.uploadTime || '').localeCompare(a.uploadTime || ''))
    } else if (sortType.value === 'downloads') {
      list.sort((a, b) => (b.downloadCount || 0) - (a.downloadCount || 0))
    }
    return list
  }

  async function fetchLessonResources() {
    if (!isLessonHubActive.value) {
      lessonAllResources.value = []
      return
    }
    lessonLoading.value = true
    try {
      const params = buildLessonListParams()
      const { records } = await resourceGateway.listPrimaryChineseAll(
        { ...params, current: 1, size: 500 },
        { silentError: true },
      )
      lessonAllResources.value = sortItems(records)
    } catch {
      lessonAllResources.value = []
    } finally {
      lessonLoading.value = false
    }
  }

  const typeStats = computed<LessonTypeStat[]>(() => {
    const map = new Map<string, number>()
    for (const item of lessonAllResources.value) {
      const type = item.type || '其他'
      map.set(type, (map.get(type) || 0) + 1)
    }
    const stats: LessonTypeStat[] = [{ type: '全部', count: lessonAllResources.value.length }]
    for (const type of LESSON_TYPE_ORDER) {
      if (map.has(type)) stats.push({ type, count: map.get(type)! })
    }
    for (const [type, count] of map) {
      if (type !== '全部' && !LESSON_TYPE_ORDER.includes(type as typeof LESSON_TYPE_ORDER[number])) {
        stats.push({ type, count })
      }
    }
    return stats
  })

  const groupedResources = computed<LessonResourceGroup[]>(() => {
    const selected = activeResourceType.value
    const all = lessonAllResources.value

    if (selected !== '全部') {
      const items = all.filter((item) => (item.type || '其他') === selected)
      return items.length ? [{ type: selected, items }] : []
    }

    const map = new Map<string, PrimaryChineseItem[]>()
    for (const item of all) {
      const type = item.type || '其他'
      if (!map.has(type)) map.set(type, [])
      map.get(type)!.push(item)
    }

    const groups: LessonResourceGroup[] = []
    for (const type of LESSON_TYPE_ORDER) {
      if (map.has(type)) groups.push({ type, items: map.get(type)! })
    }
    for (const [type, items] of map) {
      if (!LESSON_TYPE_ORDER.includes(type as typeof LESSON_TYPE_ORDER[number])) {
        groups.push({ type, items })
      }
    }
    return groups
  })

  const displayTotal = computed(() => {
    if (activeResourceType.value === '全部') {
      return lessonAllResources.value.length
    }
    return groupedResources.value.reduce((sum, group) => sum + group.items.length, 0)
  })

  const lessonSuiteHint = computed(() => {
    const types = groupedResources.value.filter((group) => group.items.length > 0)
    if (types.length < 2) return null
    const typeNames = types.map((group) => group.type).join('、')
    return {
      title: `${lessonName.value} · 本课配套资源`,
      sub: `含 ${typeNames} 等共 ${lessonAllResources.value.length} 份`,
      fileCount: lessonAllResources.value.length,
    }
  })

  watch(
    () => [
      isLessonHubActive.value,
      activeUnit.value,
      currentGradeLevelName.value,
      selectedVersionName.value,
      activeColumn.value,
      (currentSubject.value as { key?: string })?.key,
      searchKeyword.value,
      sortType.value,
      brandCode.value,
      catalogNodeId?.value,
    ],
    () => {
      if (isLessonHubActive.value) {
        fetchLessonResources()
      } else {
        lessonAllResources.value = []
      }
    },
    { deep: true },
  )

  watch(activeResourceType, () => {
    if (isLessonHubActive.value && lessonAllResources.value.length === 0) {
      fetchLessonResources()
    }
  })

  return {
    isLessonHubActive,
    lessonName,
    parentUnitName,
    lessonAllResources,
    lessonLoading,
    typeStats,
    groupedResources,
    displayTotal,
    lessonSuiteHint,
    fetchLessonResources,
  }
}

export function useLessonHubGate(isLessonHubActive: ComputedRef<boolean>) {
  return computed(() => !isLessonHubActive.value)
}
