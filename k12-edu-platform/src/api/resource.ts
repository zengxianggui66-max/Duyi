/**
 * 资源相关 API
 * 包含资源列表、详情、搜索、统计等
 */
import { request } from './request'
import type { ApiResult, PageData } from './request'
import type { ResourceItem, ResourceListParams, ResourceStats } from './types'

// ==================== API 方法 ====================

export const resourceApi = {
  /**
   * 资源列表（分页+筛选+排序）
   */
  getList(params?: ResourceListParams) {
    return request.get<ApiResult<PageData<ResourceItem>>>('/resource/list', {
      params,
    })
  },

  /**
   * 搜索资源（分页+筛选+排序）
   */
  search(params?: ResourceListParams) {
    return request.get<ApiResult<PageData<ResourceItem>>>('/resource/list', {
      params,
    })
  },

  /**
   * 热门资源
   */
  getHot(gradeLevel?: string) {
    return request.get<ApiResult<ResourceItem[]>>('/resource/hot', {
      params: { gradeLevel: gradeLevel || 'all' },
    })
  },

  /**
   * 推荐资源
   */
  getRecommend() {
    return request.get<ApiResult<ResourceItem[]>>('/resource/recommend')
  },

  /**
   * 资源详情
   */
  getDetail(id: number) {
    return request.get<ApiResult<ResourceItem>>(`/resource/detail/${id}`)
  },

  /**
   * 平台统计
   */
  getStats() {
    return request.get<ApiResult<ResourceStats>>('/resource/stats')
  },

  /**
   * 获取下载信息
   */
  getDownloadUrl(id: number) {
    return request.get<ApiResult<{ url: string }>>(
      `/resource/download-url/${id}`
    )
  },

  /**
   * 获取预览信息
   */
  getPreview(id: number) {
    return request.get<ApiResult<{ previewUrl: string; type: string }>>(
      `/resource/preview/${id}`
    )
  },
}
