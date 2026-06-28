/**
 * 期末复习包配置：专项知识点、栏目常量
 */
import type { CatalogNode } from '@/types/browse'

export const FINAL_REVIEW_MODULE = '期末复习'

export const REVIEW_KIND = {
  courseware: 'courseware',
  exercise: 'exercise',
  exam: 'exam',
} as const

export const REVIEW_SCOPE = {
  unit: 'unit',
  special: 'special',
} as const

export type ReviewKind = (typeof REVIEW_KIND)[keyof typeof REVIEW_KIND]
export type ReviewScope = (typeof REVIEW_SCOPE)[keyof typeof REVIEW_SCOPE]

export interface ReviewNodeMeta {
  reviewKind?: ReviewKind | string
  reviewScope?: ReviewScope | string
  knowledgePoint?: string
  canonicalUnit?: string
  filterProfile?: string
  defaultModule?: string
  packSection?: boolean
  volumeKey?: string
}

/** 与 SQL 种子数据对齐的专项知识点 */
export const FINAL_REVIEW_KNOWLEDGE_POINTS: Record<string, string[]> = {
  default: ['拼音与识字', '词语与句子', '阅读与鉴赏', '口语交际', '习作', '古诗与积累'],
  y5s1: ['字音字形', '词语运用', '句子与修辞', '阅读与鉴赏', '习作', '口语交际', '古诗与文言文'],
  y6s1: ['字音字形', '词语运用', '句子与修辞', '阅读与鉴赏', '习作', '口语交际', '古诗与文言文'],
}

export function getKnowledgePoints(volumeKey: string): string[] {
  return FINAL_REVIEW_KNOWLEDGE_POINTS[volumeKey] ?? FINAL_REVIEW_KNOWLEDGE_POINTS.default
}

export function readReviewMeta(node?: CatalogNode | null): ReviewNodeMeta {
  return (node?.meta as ReviewNodeMeta) || {}
}

export function findNodeById(nodes: CatalogNode[], id: number | null): CatalogNode | null {
  if (id == null) return null
  for (const n of nodes) {
    if (n.id === id) return n
    if (n.children?.length) {
      const found = findNodeById(n.children, id)
      if (found) return found
    }
  }
  return null
}

export function findPathToNode(
  nodes: CatalogNode[],
  targetId: number,
  path: CatalogNode[] = [],
): CatalogNode[] {
  for (const n of nodes) {
    const next = [...path, n]
    if (n.id === targetId) return next
    if (n.children?.length) {
      const found = findPathToNode(n.children, targetId, next)
      if (found.length) return found
    }
  }
  return []
}

export function findParentNode(
  nodes: CatalogNode[],
  childId: number,
  parent: CatalogNode | null = null,
): CatalogNode | null {
  for (const n of nodes) {
    if (n.id === childId) return parent
    if (n.children?.length) {
      const found = findParentNode(n.children, childId, n)
      if (found) return found
    }
  }
  return null
}

/** 从当前节点向上查找带 reviewKind 的祖先 */
export function findReviewKindAncestor(
  nodes: CatalogNode[],
  nodeId: number | null,
): CatalogNode | null {
  if (nodeId == null) return null
  let current = findNodeById(nodes, nodeId)
  while (current) {
    const meta = readReviewMeta(current)
    if (meta.reviewKind) return current
    const parent = findParentNode(nodes, current.id)
    current = parent
  }
  return null
}

/** 期末复习包内面包屑路径（不含教材单元） */
export function buildFinalReviewPathLabels(
  nodes: CatalogNode[],
  nodeId: number | null,
): string[] {
  if (nodeId == null) return []
  const path = findPathToNode(nodes, nodeId)
  const packIdx = path.findIndex((n) => n.name === FINAL_REVIEW_MODULE)
  if (packIdx < 0) return []
  return path.slice(packIdx + 1).map((n) => n.name)
}

/** 在树中按名称查找节点（期末复习包内） */
export function findReviewNodeByNames(
  nodes: CatalogNode[],
  names: string[],
): CatalogNode | null {
  let level = nodes
  let found: CatalogNode | null = null
  for (const name of names) {
    found = level.find((n) => n.name === name) || null
    if (!found) return null
    level = found.children || []
  }
  return found
}

/** 查找复习课件下某 scope 的第一个可选叶子 */
export function findFirstCoursewareLeaf(
  nodes: CatalogNode[],
  volumeKey: string,
  scope: ReviewScope,
): CatalogNode | null {
  const pack = findReviewNodeByNames(nodes, [FINAL_REVIEW_MODULE, '复习课件', scope === 'unit' ? '单元复习' : '专项复习'])
  if (!pack?.children?.length) return pack
  const leaf = pack.children.find((c) => c.nodeType === 'leaf') || pack.children[0]
  return leaf
}

/** 同步备课跳转：根据 canonicalUnit 在教材树中找单元节点 */
export function findSyncPrepUnitNode(
  nodes: CatalogNode[],
  canonicalUnit: string,
): CatalogNode | null {
  for (const n of nodes) {
    if (n.name === FINAL_REVIEW_MODULE || readReviewMeta(n).packSection) continue
    if (n.name === canonicalUnit) return n
    if (n.children?.length) {
      const inChild = findSyncPrepUnitNode(n.children, canonicalUnit)
      if (inChild) return inChild
    }
  }
  return null
}
