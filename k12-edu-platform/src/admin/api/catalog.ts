import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'
import type { CatalogNode } from '@/types/browse'

export interface CatalogSchemeItem {
  id: number
  code: string
  name: string
  brandCode?: string
  displayMode?: string
  sort?: number
  /** Phase 6：维度绑定展示 */
  stageId?: number
  stageName?: string
  subjectId?: number
  subjectName?: string
  editionId?: number
  editionName?: string
  volumeId?: number
  volumeName?: string
  status?: number
}

export interface CatalogNodeAdminItem {
  id: number
  schemeId: number
  schemeCode?: string
  schemeName?: string
  parentId?: number
  parentName?: string
  code: string
  name: string
  namePath?: string
  depth?: number
  nodeType: string
  sort?: number
  icon?: string
  meta?: string
  status?: number
  childCount?: number
}

export interface CatalogNodeWrite {
  schemeId: number
  parentId?: number
  code: string
  name: string
  nodeType: string
  sort?: number
  icon?: string
  meta?: string
  status?: number
}

export function listCatalogSchemes() {
  return request.get<ApiResult<CatalogSchemeItem[]>>('/admin/catalog/schemes').then(unwrapData)
}

export function getAdminCatalogTree(params: {
  schemeId?: number
  schemeCode?: string
  volumeKey?: string
  gradeName?: string
  subject?: string
  includeDisabled?: boolean
}) {
  return request.get<ApiResult<CatalogNode[]>>('/admin/catalog/tree', { params }).then(unwrapData)
}

export function listCatalogNodes(params: {
  schemeId: number
  parentId?: number
  volumeKey?: string
  includeDisabled?: boolean
}) {
  return request.get<ApiResult<CatalogNodeAdminItem[]>>('/admin/catalog/nodes', { params }).then(unwrapData)
}

export function createCatalogNode(payload: CatalogNodeWrite) {
  return request.post<ApiResult<CatalogNodeAdminItem>>('/admin/catalog/nodes', payload).then(unwrapData)
}

export function updateCatalogNode(id: number, payload: CatalogNodeWrite) {
  return request.put<ApiResult<CatalogNodeAdminItem>>(`/admin/catalog/nodes/${id}`, payload).then(unwrapData)
}

export function setCatalogNodeStatus(id: number, status: 0 | 1) {
  return request
    .put<ApiResult<void>>(`/admin/catalog/nodes/${id}/status`, null, { params: { status } })
    .then(unwrapData)
}

export function deleteCatalogNode(id: number) {
  return request.delete<ApiResult<void>>(`/admin/catalog/nodes/${id}`).then(unwrapData)
}

export function importUnitCatalogJson(volumeKey: string) {
  return request
    .post<ApiResult<{ volumeKey: string; importedCount: number }>>('/admin/catalog/import-unit-json', null, {
      params: { volumeKey },
    })
    .then(unwrapData)
}
