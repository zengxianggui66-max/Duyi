import {
  auditResourceMain,
  batchAuditResourceMain,
  batchResourceMain,
  getResourceMainAuditInsights,
  getResourceMainDetail,
  listPendingResourceMain,
  listResourceMain,
  offlineResourceMain,
  publishResourceMain,
  recycleResourceMain,
  restoreResourceMain,
  setRecommendResourceMain,
  setTopResourceMain,
  updateResourceMain,
  type AuditInsights,
  type PageResult,
  type ResourceMainBatchAction,
  type ResourceMainBatchResult,
  type ResourceMainDetail,
  type ResourceMainItem,
  type ResourceMainQuery,
  type ResourceMainUpdate,
} from './resourceMain'

export type AdminPrimaryResource = ResourceMainItem & {
  id?: number
  status?: number
}

export type AdminPrimaryResourceDetail = ResourceMainDetail & {
  id?: number
  status?: number
}

export type PrimaryResourceQuery = ResourceMainQuery
export type PrimaryResourceUpdate = ResourceMainUpdate
export type BatchResourceAction = ResourceMainBatchAction
export type BatchResourceResult = ResourceMainBatchResult
export type { AuditInsights, PageResult }

export const RESOURCE_STATUS = {
  DRAFT: -1,
  PENDING: 0,
  PUBLISHED: 1,
  REJECTED: 2,
  OFFLINE: 3,
  DELETED: 4,
} as const

export const AUDIT_STATUS = {
  DRAFT: -1,
  PENDING: 0,
  APPROVED: 1,
  REJECTED: 2,
  RECHECKING: 3,
} as const

export const PUBLISH_STATUS = {
  UNPUBLISHED: 0,
  PUBLISHED: 1,
  OFFLINE: 2,
  SCHEDULED: 3,
  RECYCLED: 4,
} as const

function withCompatId<T extends ResourceMainItem | ResourceMainDetail>(item: T) {
  return {
    ...item,
    id: item.globalId,
    status: item.legacyStatus,
  }
}

export async function listPrimaryResources(query: PrimaryResourceQuery = {}) {
  const page = await listResourceMain(query)
  return {
    ...page,
    records: page.records.map(withCompatId),
  }
}

export async function listPendingPrimaryResources(query: PrimaryResourceQuery = {}) {
  const page = await listPendingResourceMain(query)
  return {
    ...page,
    records: page.records.map(withCompatId),
  }
}

export function auditPrimaryResource(id: number, status: number, reason?: string) {
  return auditResourceMain(id, status, reason)
}

export function getAuditInsights(id: number) {
  return getResourceMainAuditInsights(id)
}

export function batchAuditResources(
  ids: number[],
  action: 'approve' | 'reject',
  reason?: string,
) {
  return batchAuditResourceMain(ids, action, reason)
}

export async function getPrimaryResource(id: number) {
  return withCompatId(await getResourceMainDetail(id))
}

export function updatePrimaryResource(id: number, dto: PrimaryResourceUpdate) {
  return updateResourceMain(id, dto)
}

export function deletePrimaryResource(id: number) {
  return recycleResourceMain(id)
}

export function publishPrimaryResource(id: number) {
  return publishResourceMain(id)
}

export function offlinePrimaryResource(id: number) {
  return offlineResourceMain(id)
}

export function setRecommendPrimaryResource(id: number, enabled: boolean) {
  return setRecommendResourceMain(id, enabled)
}

export function setTopPrimaryResource(id: number, enabled: boolean, topSort?: number) {
  return setTopResourceMain(id, enabled, topSort)
}

export function restorePrimaryResource(id: number) {
  return restoreResourceMain(id)
}

export function batchPrimaryResources(ids: number[], action: BatchResourceAction) {
  return batchResourceMain(ids, action)
}
