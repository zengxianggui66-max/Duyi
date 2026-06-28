/**
 * 资源类型 Tab：「全部」固定首位，其余按数量降序（0 在后，同数量保持原顺序）
 */
import type { CatalogNode } from '@/types/browse'
import type { PrimaryChineseParams } from '@/api/types'

export function sortResourceTypeNames(
  names: string[],
  counts: Record<string, number>,
  enabled = true,
): string[] {
  if (!enabled || names.length === 0) return [...names]

  const indexed = names.map((name, index) => ({
    name,
    index,
    count: counts[name] ?? 0,
  }))

  const allIdx = indexed.findIndex((x) => x.name === '全部')
  const allEntry = allIdx >= 0 ? indexed.splice(allIdx, 1)[0] : null

  indexed.sort((a, b) => {
    if (b.count !== a.count) return b.count - a.count
    return a.index - b.index
  })

  const sorted = indexed.map((x) => x.name)
  return allEntry ? [allEntry.name, ...sorted] : sorted
}

export function stripBrowseStatsParams<T extends Record<string, unknown>>(
  params: T,
): Omit<T, 'type' | 'subType' | 'current' | 'size' | 'sortField' | 'sortOrder' | 'keyword'> {
  const {
    type: _type,
    subType: _subType,
    current: _current,
    size: _size,
    sortField: _sortField,
    sortOrder: _sortOrder,
    keyword: _keyword,
    ...rest
  } = params
  return rest as Omit<
    T,
    'type' | 'subType' | 'current' | 'size' | 'sortField' | 'sortOrder' | 'keyword'
  >
}

export function hasBrowseStatsScope(params: Record<string, unknown>): boolean {
  const nodeId = params.catalogNodeId
  if (typeof nodeId === 'number' && nodeId > 0) return true
  return !!(
    params.gradeName ||
    params.subject ||
    params.module ||
    params.stage
  )
}

/** 目录树中整册课本根节点（如「语文（统编版）」） */
export function findTextbookRootNode(nodes: CatalogNode[]): CatalogNode | null {
  if (!nodes?.length) return null
  return (
    nodes.find((n) => n.depth === 0) ??
    nodes.find((n) => typeof n.code === 'string' && n.code.endsWith('_root')) ??
    nodes[0] ??
    null
  )
}

/** 从 stats map 解析 Tab 角标（含同步备课别名） */
export function resolveTabTypeCount(
  map: Record<string, number>,
  tabName: string,
  fallbackKey?: (tab: string) => string | undefined,
): number | undefined {
  if (!tabName) return undefined
  if (tabName === '全部') return map['全部']
  if (map[tabName] != null) return map[tabName]
  const fb = fallbackKey?.(tabName)
  if (fb && map[fb] != null) return map[fb]
  return undefined
}

/** 开发态：校验「全部」与各类型之和是否一致 */
export function warnTypeCountMismatch(
  map: Record<string, number>,
  label: string,
): void {
  if (!import.meta.env.DEV) return
  const total = map['全部']
  if (total == null) return
  const sum = Object.entries(map)
    .filter(([k]) => k !== '全部')
    .reduce((s, [, v]) => s + (v || 0), 0)
  if (sum !== total) {
    console.warn(
      `[browse stats] ${label}: 全部(${total}) ≠ 各类型之和(${sum})`,
      map,
    )
  }
}

/**
 * 整册课本维度的类型统计参数：固定根节点 + 子树，不随单元/课文选中变化。
 */
export function composeTextbookStatsParams(input: {
  stage?: string
  subject?: string
  module?: string
  gradeName?: string
  edition?: string
  brandCode?: string
  catalogTree?: CatalogNode[]
}): PrimaryChineseParams {
  const { catalogTree, ...dimension } = input
  const root = findTextbookRootNode(catalogTree ?? [])
  if (root?.id) {
    return {
      ...dimension,
      catalogNodeId: root.id,
      includeSubtree: true,
    }
  }
  return dimension
}
