/**
 * 统一资源浏览 API（M3）
 */
import { request, type SilentRequestConfig } from './request'
import type { ApiResult, PageData } from './request'
import type { PrimaryChineseItem, PrimaryChineseParams, ResourceSuite } from './types'

export interface BrowseTypeStat {
  /** 展示类型（与 Tab 名一致，优先使用） */
  displayType?: string
  /** 兼容旧字段 */
  type: string
  count: number
}

export interface BrowseStatsResult {
  types: BrowseTypeStat[]
  total: number
  /** 各类型计数之和，用于与 total 对账 */
  typeSum?: number
}

export interface BrowseModuleStat {
  module: string
  count: number
}

export const browseApi = {
  getPage(params?: PrimaryChineseParams, config?: SilentRequestConfig) {
    return request.get<ApiResult<PageData<PrimaryChineseItem>>>('/resources/browse', {
      params,
      ...config,
    })
  },

  getStats(
    params?: Omit<PrimaryChineseParams, 'current' | 'size'>,
    config?: SilentRequestConfig,
  ) {
    return request.get<ApiResult<BrowseStatsResult>>('/resources/browse/stats', {
      params,
      ...config,
    })
  },

  getModuleStats(
    params?: Omit<PrimaryChineseParams, 'current' | 'size'>,
    config?: SilentRequestConfig,
  ) {
    return request.get<ApiResult<BrowseModuleStat[]>>('/resources/browse/module-stats', {
      params,
      ...config,
    })
  },

  getSuites(
    params?: Omit<PrimaryChineseParams, 'current' | 'size'>,
    config?: SilentRequestConfig,
  ) {
    return request.get<ApiResult<ResourceSuite[]>>('/resources/browse/suites', {
      params,
      ...config,
    })
  },
}
