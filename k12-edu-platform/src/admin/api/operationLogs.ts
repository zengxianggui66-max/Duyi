import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'

export interface OperationLog {
  id: number
  userId: number
  username?: string
  module?: string
  action?: string
  permission?: string
  requestUri?: string
  requestMethod?: string
  status?: number
  errorMsg?: string
  durationMs?: number
  createTime?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export function listOperationLogs(params: {
  current?: number
  size?: number
  module?: string
  username?: string
}) {
  return request
    .get<ApiResult<PageResult<OperationLog>>>('/admin/operation-logs', { params })
    .then(unwrapData)
}
