/**
 * 栏目筛选 Composable
 * 按学段展示对应栏目列表；图标优先使用 API，否则使用本地 columnIcons
 * 小学支持 columnOrderByBrand 优先排序（七彩等）
 */
import { ref, computed, watch, type Ref } from 'vue'
import { eduResourceApi } from '../api'
import { unwrapData } from '../api/request'
import {
  columnConfig,
  columnIcons,
  sortColumnsForBrand,
  isPriorityColumn,
  getTopVisibleColumnsForStage,
  groupColumnsForStage,
  type ColumnGroupDefinition,
  type StageKey,
} from '../config/subjectConfig'

export interface ColumnItem {
  name: string
  icon?: string
  /** 顶栏优先展示（加粗样式） */
  priority?: boolean
}

export interface ColumnGroupItem {
  key: string
  label: string
  columns: ColumnItem[]
}

const stageNameMap: Record<string, string> = {
  preschool: '幼儿',
  primary: '小学',
  junior: '初中',
  senior: '高中',
  art: '美术',
  dance: '舞蹈',
}

function resolveStageKey(stage: string): StageKey {
  if (stage in columnConfig) return stage as StageKey
  return 'primary'
}

export function useColumnFilter(
  currentStage: Ref<string>,
  selectedBrandCode?: Ref<string>,
) {
  const apiIconMap = ref<Record<string, string>>({})
  const activeColumn = ref('同步备课')

  async function fetchColumnIcons() {
    try {
      const stageName = stageNameMap[currentStage.value] || '小学'
      const res = await eduResourceApi.getModules({ stageName })
      const list = unwrapData(res) || []
      const map: Record<string, string> = {}
      for (const item of list) {
        if (item?.name && item.icon) {
          map[item.name] = item.icon
        }
      }
      apiIconMap.value = map
    } catch (error) {
      console.error('获取栏目数据失败:', error)
    }
  }

  const currentColumns = computed<ColumnItem[]>(() => {
    const stageKey = resolveStageKey(currentStage.value)
    const baseNames = columnConfig[stageKey] ?? columnConfig.primary
    const brand = selectedBrandCode?.value ?? ''
    const ordered = sortColumnsForBrand(baseNames, brand)
    return ordered.map((name) => ({
      name,
      icon: apiIconMap.value[name] || columnIcons[name] || '',
      priority: isPriorityColumn(name, brand, stageKey),
    }))
  })

  const topVisibleColumns = computed<ColumnItem[]>(() => {
    const stageKey = resolveStageKey(currentStage.value)
    const all = currentColumns.value
    const names = all.map((c) => c.name)
    const topNames = getTopVisibleColumnsForStage(stageKey, names)
    return topNames
      .map((name) => all.find((c) => c.name === name))
      .filter((c): c is ColumnItem => !!c)
  })

  /**
   * 选中回显：
   * 当 activeColumn 不在顶部常驻栏目中时，将其动态插入可见区，避免“已选但不可见”。
   */
  const displayTopVisibleColumns = computed<ColumnItem[]>(() => {
    const top = topVisibleColumns.value
    const active = activeColumn.value
    if (!active) return top
    if (top.some((c) => c.name === active)) return top
    const activeItem = currentColumns.value.find((c) => c.name === active)
    if (!activeItem) return top
    // 插到首位，保证用户第一眼可见当前所选栏目
    return [activeItem, ...top]
  })

  const groupedColumns = computed<ColumnGroupItem[]>(() => {
    const stageKey = resolveStageKey(currentStage.value)
    const all = currentColumns.value
    const allNames = all.map((c) => c.name)
    const topNames = new Set(topVisibleColumns.value.map((c) => c.name))
    const remainingNames = allNames.filter((name) => !topNames.has(name))
    const groups = groupColumnsForStage(stageKey, remainingNames)
    return groups.map((g: ColumnGroupDefinition) => ({
      key: g.key,
      label: g.label,
      columns: g.columns
        .map((name) => all.find((c) => c.name === name))
        .filter((c): c is ColumnItem => !!c),
    }))
  })

  watch(currentStage, () => {
    fetchColumnIcons()
  }, { immediate: true })

  watch(
    () => selectedBrandCode?.value,
    () => {
      fetchColumnIcons()
    },
  )

  watch(currentColumns, (newCols) => {
    if (newCols.length > 0) {
      const columnNames = newCols.map((c) => c.name)
      if (!columnNames.includes(activeColumn.value)) {
        activeColumn.value = columnNames[0]
      }
    }
  })

  return {
    currentColumns,
    topVisibleColumns,
    displayTopVisibleColumns,
    groupedColumns,
    activeColumn,
  }
}
