/**
 * 资源品牌/系列 API
 */
import { request } from './request'
import type { ApiResult } from './request'

export interface ResourceBrandItem {
  id: number
  code: string
  name: string
  publisher?: string
  logoUrl?: string
  sort?: number
}

export const brandApi = {
  list(params?: { stage?: string; subject?: string }) {
    return request.get<ApiResult<ResourceBrandItem[]>>('/brands', {
      params,
      silentError: true,
    })
  },
}
