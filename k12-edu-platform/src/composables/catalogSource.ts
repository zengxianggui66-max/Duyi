/**
 * Phase 2：上传/浏览目录树统一读源（catalog API + 缓存 + 单元树兼容转换）
 *
 * 约定：API 优先 → 空则返回 []（调用方禁用控件并提示）
 */
import { catalogApi } from '@/api/catalog'
import { unwrapData } from '@/api/request'
import type { UnitTreeNode } from '@/api/types'
import type { CatalogNode } from '@/types/browse'
import { findTextbookRootNode } from '@/utils/sortResourceTypes'

const CACHE_TTL_MS = 5 * 60 * 1000

type CacheEntry<T> = { at: number; data: T }

const catalogTreeCache = new Map<string, CacheEntry<CatalogNode[]>>()

function isFresh<T>(entry: CacheEntry<T> | undefined): entry is CacheEntry<T> {
  return !!entry && Date.now() - entry.at < CACHE_TTL_MS
}

function cacheKey(params: UploadCatalogTreeParams): string {
  return [
    params.schemeCode || 'textbook_unit',
    params.gradeName || '',
    params.edition || '',
    params.subject || '',
    params.volumeKey || '',
    params.schemeId ?? '',
  ].join('|')
}

export interface UploadCatalogTreeParams {
  schemeCode?: string
  schemeId?: number
  volumeKey?: string
  gradeName: string
  edition: string
  subject: string
}

/** 上传页目录树（原始 CatalogNode[]） */
export async function loadUploadCatalogTree(
  params: UploadCatalogTreeParams,
): Promise<CatalogNode[]> {
  if (!params.gradeName || !params.edition || !params.subject) {
    return []
  }
  const key = cacheKey(params)
  const cached = catalogTreeCache.get(key)
  if (isFresh(cached)) return cached!.data
  try {
    const tree = await catalogApi
      .getTree({
        schemeCode: params.schemeCode || 'textbook_unit',
        schemeId: params.schemeId,
        volumeKey: params.volumeKey,
        gradeName: params.gradeName,
        edition: params.edition,
        subject: params.subject,
      })
      .then(unwrapData)
    const data = tree || []
    if (data.length) {
      catalogTreeCache.set(key, { at: Date.now(), data })
    }
    return data
  } catch {
    return []
  }
}

/** 上传页单元树（兼容 primary-chinese unit-tree 结构） */
export async function loadUploadUnitTree(params: UploadCatalogTreeParams): Promise<UnitTreeNode[]> {
  const tree = await loadUploadCatalogTree(params)
  return catalogNodesToUnitTree(tree)
}

function unitNodesFromCatalogTree(nodes: CatalogNode[]): CatalogNode[] {
  if (!nodes?.length) return []
  const root = findTextbookRootNode(nodes)
  if (root?.children?.length) {
    return root.children.filter(
      (n) =>
        n.nodeType === 'unit' ||
        n.nodeType === 'section' ||
        (n.children?.length ?? 0) > 0 ||
        (n.subUnits?.length ?? 0) > 0,
    )
  }
  return nodes.filter(
    (n) =>
      n.nodeType === 'unit' ||
      (n.subUnits?.length ?? 0) > 0 ||
      (n.children?.length ?? 0) > 0,
  )
}

/** 上传页单元树（兼容 primary-chinese unit-tree 结构；跳过整册课本根 folder） */
export function catalogNodesToUnitTree(nodes: CatalogNode[]): UnitTreeNode[] {
  return unitNodesFromCatalogTree(nodes).map((n) => ({
    name: n.name,
    subUnits: n.subUnits?.length
      ? [...n.subUnits]
      : (n.children || [])
          .filter((c) => c.nodeType === 'lesson')
          .map((c) => c.name),
  }))
}

/** 按单元/课文名解析目录节点 id（用于上传落位 catalogNodeId） */
export function findCatalogNodeId(
  nodes: CatalogNode[],
  unitName: string,
  lessonName?: string,
): number | undefined {
  if (!unitName?.trim()) return undefined
  const trimmedUnit = unitName.trim()
  const trimmedLesson = lessonName?.trim()

  for (const unit of unitNodesFromCatalogTree(nodes)) {
    if (unit.name !== trimmedUnit) continue
    if (trimmedLesson && unit.children?.length) {
      const lesson = unit.children.find(
        (c) => c.nodeType === 'lesson' && c.name === trimmedLesson,
      )
      if (lesson?.id) return lesson.id
    }
    if (unit.id > 0) return unit.id
  }

  const root = findTextbookRootNode(nodes)
  if (root && root.name === trimmedUnit && root.id > 0) {
    return root.id
  }
  return undefined
}

export function invalidateCatalogCache() {
  catalogTreeCache.clear()
}
