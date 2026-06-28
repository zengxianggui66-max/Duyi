/**
 * 资源筛选 composable
 * 统一管理资源列表筛选条件
 */
import { ref, computed, watch } from 'vue'

export interface FilterParams {
  keyword?: string
  gradeLevel?: string
  subject?: string
  resourceType?: string
  edition?: string
  module?: string
  sortField?: string
  sortOrder?: 'desc' | 'asc'
}

export interface UseResourceFilterOptions {
  initialFilters?: FilterParams
  onFilterChange?: (filters: FilterParams) => void
}

export function useResourceFilter(options: UseResourceFilterOptions = {}) {
  const { initialFilters = {}, onFilterChange } = options

  // 筛选条件
  const filters = ref<FilterParams>({
    keyword: '',
    gradeLevel: undefined,
    subject: undefined,
    resourceType: undefined,
    edition: undefined,
    module: undefined,
    sortField: 'createTime',
    sortOrder: 'desc',
    ...initialFilters,
  })

  // 是否有筛选条件
  const hasFilters = computed(() => {
    const { keyword, gradeLevel, subject, resourceType, edition, module } = filters.value
    return !!(keyword || gradeLevel || subject || resourceType || edition || module)
  })

  // 筛选条件数量
  const activeFilterCount = computed(() => {
    const { keyword, gradeLevel, subject, resourceType, edition, module } = filters.value
    let count = 0
    if (keyword) count++
    if (gradeLevel) count++
    if (subject) count++
    if (resourceType) count++
    if (edition) count++
    if (module) count++
    return count
  })

  // 更新单个筛选条件
  function setFilter<K extends keyof FilterParams>(key: K, value: FilterParams[K]) {
    filters.value = { ...filters.value, [key]: value }
  }

  // 批量更新筛选条件
  function setFilters(newFilters: Partial<FilterParams>) {
    filters.value = { ...filters.value, ...newFilters }
  }

  // 重置所有筛选条件
  function resetFilters() {
    filters.value = {
      keyword: '',
      gradeLevel: undefined,
      subject: undefined,
      resourceType: undefined,
      edition: undefined,
      module: undefined,
      sortField: 'createTime',
      sortOrder: 'desc',
    }
  }

  // 清除关键词
  function clearKeyword() {
    filters.value.keyword = ''
  }

  // 清除特定筛选条件
  function clearFilter(key: keyof FilterParams) {
    filters.value = { ...filters.value, [key]: undefined }
  }

  // 获取 API 请求参数（排除空值）
  function getQueryParams(): Record<string, string | number | undefined> {
    const params: Record<string, string | number | undefined> = {}
    const { keyword, gradeLevel, subject, resourceType, edition, module, sortField, sortOrder } = filters.value

    if (keyword) params.keyword = keyword
    if (gradeLevel) params.gradeLevel = gradeLevel
    if (subject) params.subject = subject
    if (resourceType) params.resourceType = resourceType
    if (edition) params.edition = edition
    if (module) params.module = module
    if (sortField) params.sortField = sortField
    if (sortOrder) params.sortOrder = sortOrder

    return params
  }

  // 监听筛选条件变化
  if (onFilterChange) {
    watch(filters, onFilterChange, { deep: true })
  }

  return {
    filters,
    hasFilters,
    activeFilterCount,
    setFilter,
    setFilters,
    resetFilters,
    clearKeyword,
    clearFilter,
    getQueryParams,
  }
}
