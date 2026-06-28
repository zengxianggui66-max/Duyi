/**
 * 分页 composable
 * 统一管理分页状态和计算
 */
import { ref, computed } from 'vue'

export interface PaginationOptions {
  total?: number
  pageSize?: number
  currentPage?: number
  maxVisiblePages?: number
}

export function usePagination(options: PaginationOptions = {}) {
  const { pageSize: initialPageSize = 12, currentPage: initialPage = 1, maxVisiblePages: maxVisible = 7 } = options

  const total = ref(options.total || 0)
  const pageSize = ref(initialPageSize)
  const currentPage = ref(initialPage)

  // 计算总页数
  const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1)

  // 计算可见页码范围
  const visiblePages = computed(() => {
    const pages: (number | string)[] = []
    const tp = totalPages.value
    const cp = currentPage.value

    if (tp <= maxVisible) {
      // 总页数小于等于最大可见页数，显示所有
      for (let i = 1; i <= tp; i++) pages.push(i)
    } else {
      // 总页数大于最大可见页数，需要省略
      const half = Math.floor(maxVisible / 2)
      let start = Math.max(1, cp - half)
      let end = Math.min(tp, cp + half)

      // 调整范围，确保显示足够多的页码
      if (end - start < maxVisible - 1) {
        if (start === 1) {
          end = Math.min(tp, start + maxVisible - 1)
        } else if (end === tp) {
          start = Math.max(1, end - maxVisible + 1)
        }
      }

      // 添加首页省略号
      if (start > 1) {
        pages.push(1)
        if (start > 2) pages.push('...')
      }

      // 添加中间页码
      for (let i = start; i <= end; i++) {
        if (!pages.includes(i)) pages.push(i)
      }

      // 添加末页省略号
      if (end < tp) {
        if (end < tp - 1) pages.push('...')
        pages.push(tp)
      }
    }

    return pages
  })

  // 是否显示上一页
  const hasPrev = computed(() => currentPage.value > 1)

  // 是否显示下一页
  const hasNext = computed(() => currentPage.value < totalPages.value)

  // 是否为首页
  const isFirstPage = computed(() => currentPage.value === 1)

  // 是否为末页
  const isLastPage = computed(() => currentPage.value === totalPages.value)

  // 跳转到指定页
  function goToPage(page: number) {
    if (page < 1 || page > totalPages.value || page === currentPage.value) return
    currentPage.value = page
  }

  // 下一页
  function nextPage() {
    goToPage(currentPage.value + 1)
  }

  // 上一页
  function prevPage() {
    goToPage(currentPage.value - 1)
  }

  // 首页
  function firstPage() {
    goToPage(1)
  }

  // 末页
  function lastPage() {
    goToPage(totalPages.value)
  }

  // 重置分页
  function reset() {
    currentPage.value = 1
  }

  // 设置总数
  function setTotal(newTotal: number) {
    total.value = newTotal
    // 如果当前页超出范围，重置到末页
    if (currentPage.value > totalPages.value) {
      currentPage.value = totalPages.value
    }
  }

  // 设置每页条数
  function setPageSize(size: number) {
    pageSize.value = size
    reset()
  }

  return {
    total,
    pageSize,
    currentPage,
    totalPages,
    visiblePages,
    hasPrev,
    hasNext,
    isFirstPage,
    isLastPage,
    goToPage,
    nextPage,
    prevPage,
    firstPage,
    lastPage,
    reset,
    setTotal,
    setPageSize,
  }
}
