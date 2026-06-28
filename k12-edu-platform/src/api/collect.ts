/**
 * 收藏相关 API
 */
import { request } from './request'
import type { ApiResult, PageData } from './request'

/** 收藏资源类型，与后端 ResourceTypeConstants 一致 */
export type CollectResourceType = 'resource' | 'primary_chinese'

export interface CollectItem {
  id: number
  resourceId: number
  resourceType?: CollectResourceType
  title: string
  stage?: string
  stageKey?: string
  subject?: string
  subjectKey?: string
  module?: string
  teachingType?: string
  gradeName?: string
  fileExt?: string
  ossUrl?: string
  collectTime: string
}

export interface CollectListParams {
  stageKey?: string
  subject?: string
  teachingType?: string
  current?: number
  size?: number
}

export interface CollectStats {
  total: number
  primaryCount: number
  juniorCount: number
  seniorCount: number
  artCount: number
  danceCount: number
}

export interface CollectSnapshotPayload {
  resourceId: number
  resourceType?: CollectResourceType
  title?: string
  stage?: string
  stageKey?: string
  subject?: string
  subjectKey?: string
  module?: string
  teachingType?: string
  type?: string
  gradeName?: string
  fileExt?: string
  ossUrl?: string
  /** M6：学科页品牌上下文 */
  brandCode?: string
}

export const collectApi = {
  getList() {
    return request.get<ApiResult<CollectItem[]>>('/resource/collect/list')
  },

  getPage(params?: CollectListParams) {
    return request.get<ApiResult<PageData<CollectItem>>>('/resource/collect/page', { params })
  },

  getStats() {
    return request.get<ApiResult<CollectStats>>('/resource/collect/stats', {
      silentError: true,
    })
  },

  collect(payload: CollectSnapshotPayload) {
    return request.post<ApiResult<null>>('/resource/collect', payload)
  },

  uncollect(resourceId: number, resourceType: CollectResourceType = 'resource') {
    return request.delete<ApiResult<null>>(`/resource/collect/${resourceId}`, {
      params: { resourceType },
    })
  },

  checkCollected(resourceId: number, resourceType: CollectResourceType = 'resource') {
    return request.get<ApiResult<boolean>>(`/resource/collect/check/${resourceId}`, {
      params: { resourceType },
    })
  },
}
