import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'

export interface ResourceStats {
  totalResources?: number
  pendingResources?: number
  approvedResources?: number
  totalDownloads?: number
  totalCollects?: number
}

export function getResourceStats() {
  return request.get<ApiResult<ResourceStats>>('/admin/stats/resources').then(unwrapData)
}
