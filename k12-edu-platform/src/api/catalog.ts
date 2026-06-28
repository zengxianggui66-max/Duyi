/**
 * 目录树 API（M2）
 */
import { request, type SilentRequestConfig } from './request'
import type { ApiResult } from './request'
import type { CatalogBreadcrumbItem, CatalogNode, CatalogScheme } from '@/types/browse'

export const catalogApi = {
  getSchemes(
    params?: { brandCode?: string; stage?: string; subject?: string },
    config?: SilentRequestConfig,
  ) {
    return request.get<ApiResult<CatalogScheme[]>>('/catalog/schemes', { params, ...config })
  },

  getTree(params: {
    schemeId?: number
    schemeCode?: string
    volumeKey?: string
    gradeName?: string
    edition?: string
    subject?: string
  }, config?: SilentRequestConfig) {
    return request.get<ApiResult<CatalogNode[]>>('/catalog/tree', { params, ...config })
  },

  getBreadcrumb(nodeId: number) {
    return request.get<ApiResult<CatalogBreadcrumbItem[]>>(
      `/catalog/node/${nodeId}/breadcrumb`,
    )
  },
}