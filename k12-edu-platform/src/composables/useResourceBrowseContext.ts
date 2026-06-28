/**
 * Subject resource browse context: keeps filter state, breadcrumb state, and URL query in sync.
 */
import { watch, type Ref } from 'vue'
import type { RouteLocationRaw } from 'vue-router'
import type { Router, RouteLocationNormalizedLoaded } from 'vue-router'
import { stageNames, subjectDataMap, subjectVersionsMap, type StageKey } from '../config/subjectConfig'
import { seriesNameByCode } from '../config/resourceSeriesConfig'
import { volumeDataMap } from '../config/volumeData'

const ALL_RESOURCE_TYPE = '全部'

export interface BreadcrumbItem {
  name: string
  /** Click target; undefined means the current breadcrumb item is not clickable. */
  route?: RouteLocationRaw
}

export function buildBreadcrumbItems(
  parts: {
    seriesName?: string
    stageName?: string
    subjectName?: string
    gradeLevelName?: string
    versionName?: string
    column?: string
    resourceType?: string
    unitLabel?: string
    /** Review package inner path, for example: 复习课件 / 单元复习 */
    reviewPath?: string[]
  },
  context: {
    stage: string
    subject: string
    version: string
    brand?: string
    volume?: string
    module?: string
    type?: string
    unit?: string
    lesson?: string
    mode?: string
    keyword?: string
    node?: string
    reviewScope?: string
  },
  options?: { keepLastClickable?: boolean },
): BreadcrumbItem[] {
  const names: string[] = [
    parts.seriesName,
    parts.stageName,
    parts.subjectName,
    parts.gradeLevelName,
    parts.versionName,
    parts.column,
    parts.resourceType && parts.resourceType !== ALL_RESOURCE_TYPE ? parts.resourceType : '',
    ...(parts.reviewPath || []),
    parts.unitLabel,
  ].filter((n): n is string => !!n)

  const keepQueryKeys: Record<number, string[]> = {
    0: ['brand'],
    1: ['brand'],
    2: ['brand', 'volume'],
    3: ['brand', 'volume'],
    4: ['brand', 'volume'],
    5: ['brand', 'volume', 'module'],
    6: ['brand', 'volume', 'module', 'type'],
    7: ['brand', 'volume', 'module', 'type', 'reviewScope'],
    8: ['brand', 'volume', 'module', 'type', 'reviewScope'],
    9: ['brand', 'volume', 'module', 'type', 'reviewScope'],
    10: ['brand', 'volume', 'module', 'type', 'reviewScope'],
    11: ['brand', 'volume', 'module', 'type', 'reviewScope'],
  }

  const contextKeys = [
    'brand',
    'volume',
    'module',
    'type',
    'node',
    'unit',
    'lesson',
    'mode',
    'keyword',
    'reviewScope',
  ] as const

  return names.map((name, idx) => {
    const isLast = idx === names.length - 1
    if (isLast && !options?.keepLastClickable) {
      return { name, route: undefined }
    }

    if (isLast && options?.keepLastClickable) {
      const query: Record<string, string> = {}
      for (const k of contextKeys) {
        const v = context[k]
        if (v) query[k] = v
      }
      return {
        name,
        route: {
          name: 'SubjectDetail',
          params: { stage: context.stage, subject: context.subject, version: context.version },
          query,
        },
      }
    }

    const keepKeys = keepQueryKeys[idx] ?? []
    if (idx >= 5) {
      keepKeys.push('node', 'unit', 'lesson', 'mode')
    }
    if (idx > 0 && context.node) keepKeys.push('node', 'unit', 'lesson', 'mode')

    const query: Record<string, string> = {}
    for (const k of keepKeys) {
      const v = context[k as keyof typeof context]
      if (v) query[k] = v
    }

    return {
      name,
      route: {
        name: 'SubjectDetail',
        params: { stage: context.stage, subject: context.subject, version: context.version },
        query,
      },
    }
  })
}

export interface ResourceBrowseQuery {
  brand?: string
  node?: string
  module?: string
  type?: string
  unit?: string
  lesson?: string
  mode?: 'single' | 'suite'
  volume?: string
  keyword?: string
  page?: string
  /** Final review scope: unit | special */
  reviewScope?: string
  /** Final review breadcrumb labels, separated by |. */
  reviewTrail?: string
}

export interface UseResourceBrowseContextOptions {
  route: RouteLocationNormalizedLoaded
  router: Router
  currentStage: Ref<StageKey>
  currentSubject: Ref<{ key: string; name: string } | null>
  selectedVersionKey: Ref<string>
  selectedVolumeId: Ref<string>
  activeColumn: Ref<string>
  activeResourceType: Ref<string>
  activeUnit: Ref<string>
  searchKeyword?: Ref<string>
  resourceMode: Ref<'single' | 'suite'>
  selectedBrandCode: Ref<string>
  activeCatalogNodeId?: Ref<number | null>
  resolveParentUnitName: (unitName: string) => string
  /** Final review scope. */
  reviewScope?: Ref<string | undefined>
  /** Final review inner breadcrumb labels. */
  getReviewPathLabels?: () => string[]
}

export function consumeUrlSyncFlag(): boolean {
  return (globalThis as any).__urlSyncInProgress === true
    ? ((globalThis as any).__urlSyncInProgress = false, true)
    : false
}

export function normalizeVersionKey(key: string) {
  if (key === 'tongbian') return 'tongbian2024'
  return key
}

function resolveSubjectName(stage: string, subjectKey: string): string {
  const subjects = subjectDataMap[stage as StageKey] || []
  return subjects.find((s) => s.key === subjectKey)?.name || subjectKey
}

function resolveVersionName(subjectKey: string, versionKey: string): string {
  const normalized = normalizeVersionKey(versionKey)
  const versions = subjectVersionsMap[subjectKey] || subjectVersionsMap.default
  return versions.find((v) => v.key === normalized)?.name || versionKey
}

function resolveVolumeName(stage: string, volumeId: string): string {
  const volumes = volumeDataMap[stage] || []
  return volumes.find((v) => v.id === volumeId)?.name || ''
}

function pickQueryString(
  query: Record<string, string | string[] | undefined>,
  key: string,
): string | undefined {
  const v = query[key]
  return typeof v === 'string' && v ? v : undefined
}

export function buildDetailBreadcrumbFromQuery(
  query: Record<string, string | string[] | undefined>,
): BreadcrumbItem[] {
  if (query.from !== 'subject') return []

  const stage = pickQueryString(query, 'stage') || 'primary'
  const subject = pickQueryString(query, 'subject') || ''
  const version = normalizeVersionKey(pickQueryString(query, 'version') || 'tongbian2024')
  const brand = pickQueryString(query, 'brand')
  const volume = pickQueryString(query, 'volume')
  const module = pickQueryString(query, 'module')
  const type = pickQueryString(query, 'type')
  const unit = pickQueryString(query, 'unit')
  const lesson = pickQueryString(query, 'lesson')
  const mode = query.mode === 'single' || query.mode === 'suite' ? query.mode : undefined
  const keyword = pickQueryString(query, 'keyword')
  const node = pickQueryString(query, 'node')
  const reviewScope = pickQueryString(query, 'reviewScope')
  const reviewTrailRaw = pickQueryString(query, 'reviewTrail')
  const reviewPath = reviewTrailRaw ? reviewTrailRaw.split('|').filter(Boolean) : undefined

  const unitLabel =
    lesson && unit && unit !== lesson ? `${unit} / ${lesson}` : unit || lesson || ''

  const context = {
    stage,
    subject,
    version,
    brand,
    volume,
    module,
    type: type && type !== ALL_RESOURCE_TYPE ? type : undefined,
    unit: undefined as string | undefined,
    lesson: undefined as string | undefined,
    mode,
    keyword,
    node,
    reviewScope,
  }
  if (lesson && unit && unit !== lesson) {
    context.unit = unit
    context.lesson = lesson
  } else if (unit) {
    context.unit = unit
  } else if (lesson) {
    context.unit = lesson
  }

  return buildBreadcrumbItems(
    {
      seriesName: brand ? seriesNameByCode(brand) : undefined,
      stageName: stageNames[stage as StageKey] || stage,
      subjectName: resolveSubjectName(stage, subject),
      gradeLevelName: volume ? resolveVolumeName(stage, volume) : undefined,
      versionName: resolveVersionName(subject, version),
      column: module,
      resourceType: type && type !== ALL_RESOURCE_TYPE ? type : undefined,
      reviewPath,
      unitLabel: unitLabel || undefined,
    },
    context,
    { keepLastClickable: true },
  )
}

export function buildSubjectBackLinkFromQuery(
  query: Record<string, string | string[] | undefined>,
): RouteLocationRaw | null {
  const from = query.from
  if (from !== 'subject') return null
  const stage = query.stage as string
  const subject = query.subject as string
  const version = query.version as string
  if (!stage || !subject || !version) return null

  const backQuery: Record<string, string> = {}
  const keys = ['brand', 'node', 'module', 'type', 'unit', 'lesson', 'mode', 'volume', 'keyword', 'reviewScope', 'reviewTrail'] as const
  for (const k of keys) {
    const v = query[k]
    if (typeof v === 'string' && v) backQuery[k] = v
  }

  return {
    name: 'SubjectDetail',
    params: { stage, subject, version: normalizeVersionKey(version) },
    query: backQuery,
  }
}

export function useResourceBrowseContext(opts: UseResourceBrowseContextOptions) {
  const {
    route,
    router,
    currentStage,
    currentSubject,
    selectedVersionKey,
    selectedVolumeId,
    activeColumn,
    activeResourceType,
    activeUnit,
    searchKeyword,
    resourceMode,
    selectedBrandCode,
    activeCatalogNodeId,
    resolveParentUnitName,
    reviewScope,
    getReviewPathLabels,
  } = opts

  function initFromRoute() {
    const { stage, subject, version } = route.params as {
      stage?: string
      subject?: string
      version?: string
    }
    if (stage) {
      currentStage.value = stage as StageKey
    }
    if (version) {
      selectedVersionKey.value = normalizeVersionKey(version)
    }

    const q = route.query as ResourceBrowseQuery
    if (typeof q.brand === 'string') selectedBrandCode.value = q.brand
    if (q.module) activeColumn.value = q.module
    if (q.type) activeResourceType.value = q.type
    if (q.volume) selectedVolumeId.value = q.volume
    if (q.mode === 'single' || q.mode === 'suite') resourceMode.value = q.mode
    if (typeof q.reviewScope === 'string' && q.reviewScope && reviewScope) {
      reviewScope.value = q.reviewScope
    }
    if (q.lesson) activeUnit.value = q.lesson
    else if (q.unit) activeUnit.value = q.unit
    if (typeof q.keyword === 'string' && searchKeyword) {
      searchKeyword.value = q.keyword
    }

    return { stage, subject, version }
  }

  function applySubjectFromRoute(
    subjects: Array<{ key: string; name: string }>,
    subjectKey?: string,
  ) {
    const key = subjectKey || (route.params.subject as string | undefined)
    if (!key) return
    const found = subjects.find((s) => s.key === key)
    if (found) currentSubject.value = found
  }

  function buildBrowseQuery(): ResourceBrowseQuery {
    const unit = activeUnit.value
    const parent = resolveParentUnitName(unit)
    const isLesson = !!unit && parent !== unit

    const query: ResourceBrowseQuery = {}
    if (selectedBrandCode.value !== undefined && selectedBrandCode.value !== null) {
      query.brand = selectedBrandCode.value
    }
    if (activeCatalogNodeId?.value != null && activeCatalogNodeId.value > 0) {
      query.node = String(activeCatalogNodeId.value)
    }
    if (activeColumn.value) query.module = activeColumn.value
    if (activeResourceType.value && activeResourceType.value !== ALL_RESOURCE_TYPE) {
      query.type = activeResourceType.value
    }
    if (selectedVolumeId.value) query.volume = selectedVolumeId.value
    if (searchKeyword?.value) query.keyword = searchKeyword.value
    if (resourceMode.value) query.mode = resourceMode.value
    if (isLesson) {
      query.unit = parent
      query.lesson = unit
    } else if (unit) {
      query.unit = unit
    }
    if (reviewScope?.value) query.reviewScope = reviewScope.value
    const trail = getReviewPathLabels?.()
    if (trail?.length) query.reviewTrail = trail.join('|')
    return query
  }

  let syncTimer: ReturnType<typeof setTimeout> | null = null

  function isSameRouteQuery(nextQuery: Record<string, string>): boolean {
    const currentQuery = route.query
    const currentKeys = Object.keys(currentQuery).filter((key) => currentQuery[key] !== undefined)
    const nextKeys = Object.keys(nextQuery)
    if (currentKeys.length !== nextKeys.length) return false
    return nextKeys.every((key) => currentQuery[key] === nextQuery[key])
  }

  function syncToUrl() {
    if (syncTimer) clearTimeout(syncTimer)
    syncTimer = setTimeout(() => {
      if (route.name !== 'SubjectDetail' || !currentSubject.value?.key) return
      const query = buildBrowseQuery() as Record<string, string>
      const params = {
        stage: currentStage.value,
        subject: currentSubject.value.key,
        version: selectedVersionKey.value,
      }
      if (
        route.params.stage === params.stage &&
        route.params.subject === params.subject &&
        route.params.version === params.version &&
        isSameRouteQuery(query)
      ) {
        return
      }
      ;(globalThis as any).__urlSyncInProgress = true
      router
        .replace({
          name: 'SubjectDetail',
          params,
          query,
        })
        .finally(() => {
          queueMicrotask(() => {
            ;(globalThis as any).__urlSyncInProgress = false
          })
        })
    }, 200)
  }

  function disposeSync() {
    if (syncTimer) {
      clearTimeout(syncTimer)
      syncTimer = null
    }
  }

  function buildDetailLocation(item: { id: number; type?: string }): RouteLocationRaw {
    const query: Record<string, string> = {
      from: 'subject',
      stage: currentStage.value,
      subject: currentSubject.value?.key || '',
      version: selectedVersionKey.value,
      ...buildBrowseQuery(),
    }
    return {
      name: 'ResourceDetail',
      params: { id: String(item.id) },
      query,
    }
  }

  function navigateToDetail(item: { id: number }) {
    if (!item?.id) return
    router.push(buildDetailLocation(item))
  }

  function buildSubjectBackLink(query: Record<string, string | string[] | undefined>): RouteLocationRaw | null {
    return buildSubjectBackLinkFromQuery(query)
  }

  function formatBrowseSummary(parts: {
    stageName?: string
    subjectName?: string
    gradeLevelName?: string
    versionName?: string
    column?: string
    resourceType?: string
    unitLabel?: string
    seriesName?: string
  }) {
    return [
      parts.seriesName,
      parts.stageName,
      parts.subjectName,
      parts.gradeLevelName,
      parts.versionName,
      parts.column,
      parts.resourceType && parts.resourceType !== ALL_RESOURCE_TYPE ? parts.resourceType : '',
      parts.unitLabel,
    ]
      .filter(Boolean)
      .join(' / ')
  }

  function setupUrlSync(watchSources: () => unknown[]) {
    const unwatch = watch(watchSources, () => syncToUrl(), { deep: true })
    return () => {
      unwatch()
      disposeSync()
    }
  }

  return {
    initFromRoute,
    applySubjectFromRoute,
    buildBrowseQuery,
    syncToUrl,
    buildDetailLocation,
    navigateToDetail,
    buildSubjectBackLink,
    formatBrowseSummary,
    buildBreadcrumbItems,
    setupUrlSync,
    normalizeVersionKey,
    disposeSync,
  }
}
