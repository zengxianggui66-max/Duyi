import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'

export interface ResourceMainItem {
  globalId: number
  sourceType: string
  sourceId: number
  title: string
  stage?: string
  subject?: string
  module?: string
  type?: string
  gradeName?: string
  edition?: string
  brandCode?: string
  subType?: string
  unitName?: string
  lessonName?: string
  fileExt?: string
  ossUrl?: string
  fileSizeKb?: number
  legacyStatus?: number
  auditStatus?: number
  publishStatus?: number
  auditStatusLabel?: string
  shelfStatusLabel?: string
  uploaderId?: number
  uploadTime?: string
  isRecommend?: number
  isTop?: number
  isFree?: number
  downloadCount?: number
  viewCount?: number
  allowPreview?: number
  previewStatus?: number
  fileSafetyStatus?: number | string
  catalogNodeId?: number
}

export interface ResourceMainDetail extends ResourceMainItem {
  originalFilename?: string
  updateTime?: string
  topSort?: number
  sort?: number
  remark?: string
  catalogPath?: string
}

export interface ResourceMainQuery {
  sourceType?: string
  stage?: string
  subject?: string
  module?: string
  type?: string
  gradeName?: string
  edition?: string
  keyword?: string
  auditStatus?: number
  publishStatus?: number
  status?: number
  isRecommend?: number
  isTop?: number
  isFree?: number
  uploaderId?: number
  current?: number
  size?: number
  sortField?: string
  sortOrder?: string
}

export interface ResourceMainUpdate {
  title?: string
  module?: string
  type?: string
  gradeName?: string
  edition?: string
  unitName?: string
  lessonName?: string
  remark?: string
  isFree?: number
  sort?: number
  catalogNodeId?: number
  catalogPath?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages?: number
}

export type ResourceMainBatchAction =
  | 'publish'
  | 'offline'
  | 'recommend'
  | 'unrecommend'
  | 'top'
  | 'untop'
  | 'recycle'
  | 'restore'

export interface ResourceMainBatchResult {
  successCount: number
  skipCount: number
  skipReasons?: string[]
  message?: string
}

export interface AuditInsights {
  resourceId: number
  duplicateHints: {
    id: number
    title?: string
    status?: number
    statusLabel?: string
    matchType?: string
  }[]
  sensitiveWords: string[]
  fileSafetyStatus: 'safe' | 'pending' | 'risk' | 'unknown'
  fileSafetyMessage?: string
}

const basePath = '/admin/resource-main'

export function listResourceMain(params: ResourceMainQuery = {}) {
  return request
    .get<ApiResult<PageResult<ResourceMainItem>>>(basePath, { params })
    .then(unwrapData)
}

export function listPendingResourceMain(params: ResourceMainQuery = {}) {
  return request
    .get<ApiResult<PageResult<ResourceMainItem>>>(`${basePath}/pending`, { params })
    .then(unwrapData)
}

export function getResourceMainDetail(globalId: number) {
  return request
    .get<ApiResult<ResourceMainDetail>>(`${basePath}/${globalId}`)
    .then(unwrapData)
}

export function updateResourceMain(globalId: number, dto: ResourceMainUpdate) {
  return request.put<ApiResult<void>>(`${basePath}/${globalId}`, dto)
}

export function auditResourceMain(globalId: number, status: number, reason?: string) {
  return request.post<ApiResult<void>>(`${basePath}/${globalId}/audit`, null, {
    params: { status, reason },
  })
}

export function batchAuditResourceMain(
  ids: number[],
  action: 'approve' | 'reject',
  reason?: string,
) {
  return request
    .post<ApiResult<ResourceMainBatchResult>>(`${basePath}/batch-audit`, { ids, action, reason })
    .then(unwrapData)
}

export function getResourceMainAuditInsights(globalId: number) {
  return request
    .get<ApiResult<AuditInsights>>(`${basePath}/${globalId}/audit-insights`)
    .then(unwrapData)
}

export function publishResourceMain(globalId: number) {
  return request.post<ApiResult<void>>(`${basePath}/${globalId}/publish`)
}

export function offlineResourceMain(globalId: number) {
  return request.post<ApiResult<void>>(`${basePath}/${globalId}/offline`)
}

export function recycleResourceMain(globalId: number) {
  return request.delete<ApiResult<void>>(`${basePath}/${globalId}`)
}

export function restoreResourceMain(globalId: number) {
  return request.post<ApiResult<void>>(`${basePath}/${globalId}/restore`)
}

export function setRecommendResourceMain(globalId: number, enabled: boolean) {
  return request.post<ApiResult<void>>(`${basePath}/${globalId}/recommend`, null, {
    params: { enabled },
  })
}

export function setTopResourceMain(globalId: number, enabled: boolean, topSort?: number) {
  return request.post<ApiResult<void>>(`${basePath}/${globalId}/top`, null, {
    params: { enabled, topSort },
  })
}

export function batchResourceMain(ids: number[], action: ResourceMainBatchAction) {
  return request
    .post<ApiResult<ResourceMainBatchResult>>(`${basePath}/batch`, { ids, action })
    .then(unwrapData)
}

export function getResourceMainStats() {
  return request.get<ApiResult<Record<string, unknown>>>(`${basePath}/stats`).then(unwrapData)
}

export interface LegacyApiUsageRow {
  apiPath: string
  statDate?: string
  hitCount?: number
  lastHitTime?: string
  sampleQuery?: string
}

export function getLegacyApiUsage(days = 7) {
  return request
    .get<ApiResult<LegacyApiUsageRow[]>>(`${basePath}/legacy-api-usage`, { params: { days } })
    .then(unwrapData)
}
