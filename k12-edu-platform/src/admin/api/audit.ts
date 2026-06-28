import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'
import type { PageResult } from './resources'

export interface AuditLogItem {
  id: number
  resourceId: number
  resourceTitle?: string
  stage?: string
  subject?: string
  auditorId: number
  auditorName?: string
  action: 'approve' | 'approve_publish' | 'approve_audit' | 'reject' | string
  actionLabel?: string
  beforeStatus?: number
  beforeStatusLabel?: string
  afterStatus?: number
  afterStatusLabel?: string
  reason?: string
  createdAt?: string
}

export interface RejectReasonItem {
  id: number
  content: string
  category?: number
  categoryLabel?: string
  sort: number
  status: number
  createdAt?: string
  updatedAt?: string
}

export interface AuditLogQuery {
  current?: number
  size?: number
  resourceId?: number
  auditorId?: number
  action?: string
  keyword?: string
  startTime?: string
  endTime?: string
}

export interface RejectReasonWrite {
  content: string
  category?: number
  sort?: number
  status?: number
}

export function listAuditLogs(query: AuditLogQuery = {}) {
  return request
    .get<ApiResult<PageResult<AuditLogItem>>>('/admin/audit/logs', { params: query })
    .then(unwrapData)
}

export function listRejectReasons(includeDisabled = false) {
  return request
    .get<ApiResult<RejectReasonItem[]>>('/admin/audit/reject-reasons', {
      params: { includeDisabled },
    })
    .then(unwrapData)
}

/** 按分类分组返回，用于 el-optgroup */
export function listRejectReasonsByCategory(includeDisabled = false) {
  return request
    .get<ApiResult<Record<string, RejectReasonItem[]>>>(
      '/admin/audit/reject-reasons/by-category',
      { params: { includeDisabled } },
    )
    .then(unwrapData)
}

export function createRejectReason(payload: RejectReasonWrite) {
  return request
    .post<ApiResult<RejectReasonItem>>('/admin/audit/reject-reasons', payload)
    .then(unwrapData)
}

export function updateRejectReason(id: number, payload: RejectReasonWrite) {
  return request
    .put<ApiResult<RejectReasonItem>>(`/admin/audit/reject-reasons/${id}`, payload)
    .then(unwrapData)
}

/** 启用/禁用模板：status 1=启用 0=禁用 */
export function setRejectReasonStatus(id: number, status: 0 | 1) {
  return request
    .put<ApiResult<void>>(`/admin/audit/reject-reasons/${id}/status`, null, {
      params: { status },
    })
    .then(unwrapData)
}

/** 物理删除模板（不可恢复） */
export function deleteRejectReason(id: number) {
  return request.delete<ApiResult<void>>(`/admin/audit/reject-reasons/${id}`).then(unwrapData)
}
