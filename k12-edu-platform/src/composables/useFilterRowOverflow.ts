import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch, type Ref } from 'vue'

export interface FilterRowOption {
  value: string
  label: string
  icon?: string
  showDot?: boolean
  badge?: number
  /** 顶栏优先栏目（样式加粗） */
  priority?: boolean
}

/**
 * 单行筛选：按容器宽度计算首行可见项，其余通过「更多」在下一行展开（非下拉菜单）。
 * 当前选中若在收起区，自动展开第二行（不把选中项硬塞进首行，避免「更多」被 overflow 裁掉）。
 */
export function useFilterRowOverflow(
  lineRef: Ref<HTMLElement | null>,
  measureRef: Ref<HTMLElement | null>,
  options: Ref<FilterRowOption[]>,
  activeValue: Ref<string>,
  moreReservePx = 84,
) {
  const visibleCount = ref(999)
  const expanded = ref(false)

  let ro: ResizeObserver | null = null

  function countFittedInLine(
    buttons: NodeListOf<HTMLElement>,
    listLength: number,
    maxW: number,
  ): number {
    const gap = 8
    let used = 0
    let count = 0

    for (let i = 0; i < buttons.length; i++) {
      const w = buttons[i].offsetWidth
      const gapBefore = count > 0 ? gap : 0
      const needMoreBtn = i < listLength - 1
      const reserve = needMoreBtn ? moreReservePx : 0
      if (count > 0 && used + gapBefore + w + reserve > maxW) break
      used += gapBefore + w
      count++
    }

    if (count < 1) count = 1
    return Math.min(count, listLength)
  }

  function isActiveInHidden(fitted: number): boolean {
    const list = options.value
    const activeIdx = list.findIndex((o) => o.value === activeValue.value)
    return activeIdx >= 0 && activeIdx >= fitted && fitted < list.length
  }

  function recalculate() {
    const line = lineRef.value
    const measure = measureRef.value
    const list = options.value
    if (!line || !measure || !list.length) {
      visibleCount.value = list.length
      expanded.value = false
      return
    }

    const buttons = measure.querySelectorAll<HTMLElement>('[data-measure-btn]')
    if (!buttons.length) {
      visibleCount.value = list.length
      expanded.value = false
      return
    }

    const fitted = countFittedInLine(buttons, list.length, line.clientWidth)
    visibleCount.value = fitted

    if (isActiveInHidden(fitted)) {
      expanded.value = true
    }
  }

  function scheduleRecalculate() {
    nextTick(() => recalculate())
  }

  onMounted(() => {
    scheduleRecalculate()
    ro = new ResizeObserver(() => scheduleRecalculate())
    if (lineRef.value) ro.observe(lineRef.value)
    window.addEventListener('resize', scheduleRecalculate)
  })

  onBeforeUnmount(() => {
    ro?.disconnect()
    window.removeEventListener('resize', scheduleRecalculate)
  })

  watch(options, scheduleRecalculate, { deep: true })
  watch(activeValue, () => {
    scheduleRecalculate()
    nextTick(() => {
      if (isActiveInHidden(visibleCount.value)) {
        expanded.value = true
      }
    })
  })

  const visibleOptions = computed(() => options.value.slice(0, visibleCount.value))
  const hiddenOptions = computed(() => options.value.slice(visibleCount.value))
  const hasHidden = computed(() => hiddenOptions.value.length > 0)

  function toggleExpand() {
    expanded.value = !expanded.value
  }

  return {
    visibleOptions,
    hiddenOptions,
    hasHidden,
    expanded,
    toggleExpand,
    scheduleRecalculate,
  }
}
