/**
 * 浏览记录相关 API
 */
import { request } from './request'
import type { ApiResult, PageData } from './request'

export interface ViewItem {
  id: number
  userId: number
  resourceId: number
  resourceType?: string
  title: string
  subject?: string
  stageKey?: string
  stage?: string
  gradeName?: string
  teachingType?: string
  fileExt?: string
  ossUrl?: string
  detailUrl?: string
  createTime?: string
  updateTime?: string
}

export interface ViewListParams {
  current?: number
  size?: number
  keyword?: string
  stageKey?: string
  subject?: string
  teachingType?: string
}

export interface ViewStats {
  total: number
  weekCount: number
  todayCount: number
}

export interface UpsertViewPayload {
  resourceId: number
  resourceType?: string
  title?: string
  subject?: string
  stageKey?: string
  stage?: string
  gradeName?: string
  teachingType?: string
  fileExt?: string
  ossUrl?: string
  url?: string
  detailUrl?: string
}

export const viewApi = {
  getPage(params?: ViewListParams) {
    return request.get<ApiResult<PageData<ViewItem>>>('/resource/view/page', { params })
  },

  getStats() {
    return request.get<ApiResult<ViewStats>>('/resource/view/stats')
  },

  upsert(payload: UpsertViewPayload) {
    return request.post<ApiResult<null>>('/resource/view', payload)
  },

  remove(id: number) {
    return request.delete<ApiResult<null>>(`/resource/view/${id}`)
  },

  clearAll() {
    return request.delete<ApiResult<null>>('/resource/view')
  },

  batchRemove(ids: number[]) {
    return request.delete<ApiResult<null>>('/resource/view/batch', { data: { ids } })
  },
}
