import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'

export interface AdminFuncChannelItem {
  id: number
  funcKey: string
  name: string
  examType: string
  defaultTopic: string
  stageKey: string
  paperTab: string
  paperDefaultGrade: string
  scrollTarget?: string
  examTabLabel?: string
  browseModule?: string
  browseStageKey?: string
  browseDefaultVolume?: string
  sort?: number
  status?: number
}

export const adminFuncChannelApi = {
  list(includeDisabled = true) {
    return request
      .get<ApiResult<AdminFuncChannelItem[]>>('/admin/home/func-channels', { params: { includeDisabled } })
      .then(unwrapData)
  },
  update(id: number, payload: Omit<AdminFuncChannelItem, 'id' | 'funcKey'>) {
    return request
      .put<ApiResult<AdminFuncChannelItem>>(`/admin/home/func-channels/${id}`, payload)
      .then(unwrapData)
  },
  setStatus(id: number, status: 0 | 1) {
    return request
      .put<ApiResult<void>>(`/admin/home/func-channels/${id}/status`, null, { params: { status } })
      .then(unwrapData)
  },
}
