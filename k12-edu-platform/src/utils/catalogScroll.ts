/** M6：目录树选中节点滚入可视区域 */
export function scrollCatalogNodeIntoView(nodeId: number) {
  if (typeof document === 'undefined' || !nodeId) return
  const el = document.querySelector(`[data-catalog-node-id="${nodeId}"]`)
  el?.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
}
