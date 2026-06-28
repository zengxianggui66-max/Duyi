/**
 * API 资源加载 Composable
 * 管理后端 API 资源查询、分页、自动刷新
 *
 * 特性：
 * - 防抖：300ms 延迟避免频繁请求
 * - 自动刷新：监听参数变化自动重新加载
 * - 分页：完整的分页状态和导航
 */
import { ref, computed, watch, onUnmounted, type Ref, type ComputedRef } from 'vue'
import { ElMessage } from 'element-plus'
import { resourceGateway } from '../api'
import { USE_CATALOG_BROWSE } from '../config/featureFlags'
import type { ExamBrowseQuery } from '@/constants/examColumnFilters'
import {
  SYNC_PREP_COLUMN,
  mapSyncPrepTypeToQuery,
} from '@/constants/syncPrepColumnFilters'
import { normalizeEditionLabel } from '@/utils/editionAdapter'
import { stripBrowseStatsParams } from '@/utils/sortResourceTypes'
import { stageNames, type StageKey } from '@/config/subjectConfig'
import { usePagination } from './usePagination'

// ===== 真实 API =====

interface ApiResourceItem {
  id: number
  title: string
  unitName?: string
  gradeName?: string
  edition?: string
  ossUrl?: string
  type?: string
  fileSizeKb?: number
  downloadCount?: number
  uploadTime?: string
  [key: string]: any
}

export interface TabCacheEntry {
  column: string
  resources: ApiResourceItem[]
  total: number
  currentPage: number
  pageSize: number
  scrollTop: number
}

// 全局 Tab 缓存（切换栏目时保留分页状态）
const tabCache = new Map<string, TabCacheEntry>()

// ===== Composable 主逻辑 =====

let composableInstanceCounter = 0
let lastMainResourceErrorAt = 0

function showMainResourceError() {
  const now = Date.now()
  if (now - lastMainResourceErrorAt < 2000) return
  lastMainResourceErrorAt = now
  ElMessage.error('资源列表加载失败，请稍后重试')
}

export function useApiResources(
  currentStage: Ref<string>,
  currentSubject: Ref<any>,
  currentGradeLevelName: Ref<string>,
  selectedVersionName: Ref<string>,
  activeColumn: Ref<string>,
  activeResourceType: Ref<string>,
  activeUnit: Ref<string>,
  searchKeyword: Ref<string>,
  sortType: Ref<string>,
  resolveParentUnitName: (unitName: string) => string,
  brandCode: Ref<string | undefined> | ComputedRef<string | undefined>,
  catalogNodeId?: Ref<number | null> | ComputedRef<number | null>,
  enabled?: Ref<boolean> | ComputedRef<boolean>,
  /** 考试/专题布局：类型、地区、版本筛选（覆盖 activeResourceType） */
  examBrowseParams?: ComputedRef<ExamBrowseQuery | null>,
  layoutUsesExamFilters?: ComputedRef<boolean>,
  /** 文档类型筛选（仅开学专区等专题布局使用） */
  docType?: Ref<string>,
  /** 目录浏览：是否包含子树（课文节点建议 false，与 stats 一致） */
  resolveIncludeSubtree?: () => boolean,
) {
  // 唯一实例 ID（用于竞态控制）
  const instanceId = `useApiResources_${++composableInstanceCounter}`
  let currentSerial = 0

  // 闭包内的防抖定时器（避免跨实例干扰）
  let debounceTimer: ReturnType<typeof setTimeout> | null = null
  function clearTimer() {
    if (debounceTimer) {
      clearTimeout(debounceTimer)
      debounceTimer = null
    }
  }
  function runDebounced(fn: () => void, delay: number = 300) {
    clearTimer()
    debounceTimer = setTimeout(fn, delay)
  }

  const apiFetchEnabled = computed(() => enabled?.value !== false)
  const pagination = usePagination({ pageSize: 10 })
  const { currentPage: apiCurrentPage, pageSize: apiPageSize, total: apiTotal, setTotal, goToPage, setPageSize } = pagination
  const apiResources = ref<ApiResourceItem[]>([])
  const apiLoading = ref(false)

  // 保存滚动位置（外部读取/设置）
  let savedScrollTop = 0
  function saveScrollTop(top: number) { savedScrollTop = top }
  function getSavedScrollTop() { return savedScrollTop }

  /** 生成 Tab 缓存 key（含目录节点，避免切换单元时复用错误列表/总数） */
  function tabCacheKey(): string {
    const node = catalogNodeId?.value ?? 'none'
    const subtree = resolveIncludeSubtree?.() ?? true
    return [
      currentStage.value,
      (currentSubject.value as any)?.key,
      currentGradeLevelName.value,
      selectedVersionName.value,
      activeColumn.value,
      activeResourceType.value,
      searchKeyword.value,
      sortType.value,
      brandCode.value,
      node,
      subtree,
      docType?.value,
    ].join('|')
  }

  /**
   * 保存当前 Tab 状态到缓存
   */
  function saveTabState() {
    const key = tabCacheKey()
    tabCache.set(key, {
      column: activeColumn.value,
      resources: [...apiResources.value],
      total: apiTotal.value,
      currentPage: apiCurrentPage.value,
      pageSize: apiPageSize.value,
      scrollTop: savedScrollTop,
    })
  }

  /**
   * 恢复 Tab 状态（返回 true 表示命中缓存）
   */
  function restoreTabState(): boolean {
    const key = tabCacheKey()
    const cached = tabCache.get(key)
    if (!cached || cached.column !== activeColumn.value) return false
    apiResources.value = cached.resources
    setTotal(cached.total)
    goToPage(cached.currentPage)
    if (cached.pageSize !== apiPageSize.value) {
      setPageSize(cached.pageSize)
    }
    savedScrollTop = cached.scrollTop
    return true
  }

  /**
   * 构建 API 查询参数
   */
  function buildApiParams(): Record<string, unknown> {
    const nodeId = catalogNodeId?.value
    const useNodeBrowse = USE_CATALOG_BROWSE && nodeId != null && nodeId > 0
    const useExamFilters = layoutUsesExamFilters?.value === true
    const exam = useExamFilters ? examBrowseParams?.value : null

    let type: string | undefined
    let subType: string | undefined
    let keyword: string | undefined
    let edition: string | undefined
    let fileExt: string | undefined

    if (exam) {
      type = exam.type
      subType = exam.subType
      keyword = exam.keyword
      edition = exam.edition
    } else {
      keyword = searchKeyword.value?.trim() || undefined
      if (activeColumn.value === SYNC_PREP_COLUMN) {
        const q = mapSyncPrepTypeToQuery(activeResourceType.value)
        type = q.type
        subType = q.subType
      } else {
        type =
          activeResourceType.value === '全部' ? undefined : activeResourceType.value
      }
      edition = selectedVersionName.value || undefined
    }

    // 文档类型筛选
    if (docType?.value && docType.value !== '全部') {
      fileExt = docType.value
    }

    const normalizedEdition = edition ? normalizeEditionLabel(edition) : undefined

    const base = {
      stage: stageNames[currentStage.value as StageKey] || undefined,
      subject: (currentSubject.value as { name?: string })?.name || undefined,
      module: activeColumn.value || undefined,
      type,
      subType,
      gradeName: currentGradeLevelName.value || undefined,
      edition:
        exam?.edition ??
        (useExamFilters ? undefined : normalizedEdition || undefined),
      keyword,
      fileExt,
      brandCode: brandCode.value || undefined,
      current: apiCurrentPage.value,
      size: apiPageSize.value,
      sortField: 'upload_time',
      sortOrder: 'desc' as const,
    }

    if (useNodeBrowse) {
      const includeSubtree = resolveIncludeSubtree?.() ?? true
      return {
        ...base,
        catalogNodeId: nodeId,
        includeSubtree,
      }
    }

    const unit = activeUnit.value
    const parentUnit = resolveParentUnitName(unit)
    const isLessonLeaf = !!unit && parentUnit !== unit
    return {
      ...base,
      unitName: parentUnit || undefined,
      lessonName: isLessonLeaf ? unit : undefined,
    }
  }

  /**
   * 请求资源列表（带防抖 + 竞态控制）
   */
  function fetchApiResources() {
    if (!currentStage.value || !apiFetchEnabled.value) return

    // 保存当前 Tab 状态（切换前保存旧状态）
    saveTabState()

    // 尝试从缓存恢复
    if (restoreTabState()) {
      // 已有缓存，后台静默刷新
      runDebounced(async () => {
        const serial = ++currentSerial
        apiLoading.value = false
        try {
          const params = buildApiParams()
          const silent = { silentError: true } as const
          const { page } = await resourceGateway.listPrimaryChineseResources(params, silent)
          // 竞态检查：如 serial 已过期则丢弃响应
          if (serial !== currentSerial) return
          apiResources.value = page?.records || []
          setTotal(page?.total || 0)
        } catch (_e) {
          // 静默失败，保留缓存数据
        }
      })
      return
    }

    runDebounced(async () => {
      const serial = ++currentSerial
      const hadData = apiResources.value.length > 0
      if (!hadData) {
        apiLoading.value = true
      }
      try {
        const params = buildApiParams()
        const silent = { silentError: true } as const
        const { page } = await resourceGateway.listPrimaryChineseResources(params, silent)
        // 竞态检查：如 serial 已过期则丢弃响应
        if (serial !== currentSerial) return
        apiResources.value = page?.records || []
        setTotal(page?.total || 0)
      } catch (_e) {
        if (!hadData) {
          apiResources.value = []
          setTotal(0)
          showMainResourceError()
        }
      } finally {
        if (serial === currentSerial) {
          apiLoading.value = false
        }
      }
    })
  }

  // 监听关键参数变化，自动重新加载资源（带防抖）
  watch(
    () => [
      currentStage.value,
      (currentSubject.value as { key?: string })?.key,
      currentGradeLevelName.value,
      selectedVersionName.value,
      activeColumn.value,
      activeResourceType.value,
      activeUnit.value,
      searchKeyword.value,
      sortType.value,
      brandCode.value,
      catalogNodeId?.value,
      layoutUsesExamFilters?.value,
      examBrowseParams?.value,
      docType?.value,
    ],
    () => {
      if (!apiFetchEnabled.value) return
      goToPage(1)
      fetchApiResources()
    },
    { deep: true }
  )

  /** 切换每页条数 */
  function setApiPageSize(size: number) {
    setPageSize(size)
    fetchApiResources()
  }

  // 组件卸载时清理防抖定时器
  onUnmounted(() => {
    clearTimer()
  })

  /** 类型角标统计参数：与列表同 scope，但不含类型/分页/排序筛选 */
  function buildStatsParams(): Record<string, unknown> {
    return stripBrowseStatsParams(buildApiParams())
  }

  return {
    apiCurrentPage,
    apiPageSize,
    apiTotal,
    apiResources,
    apiLoading,
    apiTotalPages: pagination.totalPages,
    apiVisiblePages: pagination.visiblePages,
    fetchApiResources,
    goApiPage: (page: number) => { goToPage(page); fetchApiResources() },
    setApiPageSize,
    buildApiParams,
    buildStatsParams,
    saveScrollTop,
    getSavedScrollTop,
  }
}
