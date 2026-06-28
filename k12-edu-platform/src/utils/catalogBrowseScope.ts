/**
 * 目录节点 → includeSubtree 口径（与后端 CatalogBrowseScope 对齐）
 */
import type { CatalogNode } from '@/types/browse'

/** nodeType → 是否含子树；未列出的 nodeType 走 depth/code/clientHint 兜底 */
export const CATALOG_SUBTREE_BY_NODE_TYPE: Readonly<
  Record<string, boolean | undefined>
> = {
  lesson: false,
  leaf: false,
  unit: true,
  folder: true,
  section: true,
  textbook: true,
  root: true,
}

export function resolveCatalogIncludeSubtree(
  node: Pick<CatalogNode, 'nodeType' | 'depth' | 'code'> | null | undefined,
  clientHint = true,
): boolean {
  if (!node) return clientHint

  const mapped = CATALOG_SUBTREE_BY_NODE_TYPE[node.nodeType]
  if (mapped === false) return false
  if (mapped === true) return true

  if (node.depth === 0) return true
  if (typeof node.code === 'string' && node.code.endsWith('_root')) return true
  return clientHint
}

export function resolveCatalogIncludeSubtreeById(
  findNode: (id: number) => CatalogNode | null | undefined,
  nodeId: number | null | undefined,
  clientHint = true,
): boolean {
  if (nodeId == null || nodeId <= 0) return clientHint
  return resolveCatalogIncludeSubtree(findNode(nodeId), clientHint)
}
