import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'

export interface AdminResource {
  id: number
  title: string
  subject?: string
  resourceType?: string
  authorName?: string
  createTime?: string
  status?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export interface ResourceQuery {
  current?: number
  size?: number
  keyword?: string
  status?: number
}

export function listPendingResources(query: ResourceQuery = {}) {
  return request
    .get<ApiResult<PageResult<AdminResource>>>('/admin/resource/pending', { params: query })
    .then(unwrapData)
}

export function auditResource(id: number, status: number, reason?: string) {
  return request.post<ApiResult<void>>(`/admin/resource/${id}/audit`, null, {
    params: { status, reason },
  })
}

export function listResources(query: ResourceQuery = {}) {
  return request
    .get<ApiResult<PageResult<AdminResource>>>('/admin/resource/list', { params: query })
    .then(unwrapData)
}

export function deleteResource(id: number) {
  return request.delete<ApiResult<void>>(`/admin/resource/${id}`)
}
