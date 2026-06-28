/**
 * 分享相关 API
 */
import { request } from './request'
import type { ApiResult } from './request'
import type { CollectResourceType } from './collect'

// ==================== 类型定义 ====================

export interface ShareRecord {
  id: number
  resourceId: number
  resourceType?: CollectResourceType
  title: string
  shareUrl: string
  shareType: string
  sharePlatform?: string
  shareTime: string
}

// ==================== API 方法 ====================

export const shareApi = {
  recordShare(data: {
    resourceId: number
    resourceType?: CollectResourceType
    shareType: string
    sharePlatform: string
  }) {
    return request.post<ApiResult<{ shareId: number; shareUrl: string }>>(
      '/resource/share/record',
      data,
    )
  },

  getShareUrl(resourceId: number, resourceType: CollectResourceType = 'resource') {
    return request.get<
      ApiResult<{
        shareUrl: string
        title: string
        description: string
      }>
    >(`/resource/share/url/${resourceId}`, {
      params: { resourceType },
    })
  },

  getShareCount(resourceId: number, resourceType: CollectResourceType = 'resource') {
    return request.get<ApiResult<{ count: number }>>(
      `/resource/share/count/${resourceId}`,
      { params: { resourceType } },
    )
  },

  myShares() {
    return request.get<ApiResult<ShareRecord[]>>('/resource/share/my')
  },
}
