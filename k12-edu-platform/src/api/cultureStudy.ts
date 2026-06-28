/**
 * 传统文化 · 巴蜀研学 API
 */
import { request } from './request'
import type { ApiResult } from './request'

export interface CultureResourceItem {
  id: number
  title: string
  summary?: string
  category: string
  region: string
  durationType: string
  durationLabel?: string
  suitableAudience?: string
  location?: string
  resourceKind: 'platform' | 'external'
  fileFormat?: string
  fileUrl?: string
  coverUrl?: string
  icon?: string
  sourceName?: string
  externalUrl?: string
  tags?: string
  downloadCount?: number
  viewCount?: number
  isFree?: number
  isElite?: number
}

export interface CulturePackageItem {
  id: number
  title: string
  summary?: string
  region: string
  durationType: string
  durationLabel?: string
  suitableAudience?: string
  location?: string
  icon?: string
  tags?: string
  resourceCount?: number
  downloadCount?: number
  isFree?: number
  isElite?: number
}

export interface CultureQueryParams {
  category?: string
  region?: string
  durationType?: string
  resourceKind?: string
  keyword?: string
  current?: number
  size?: number
  sortField?: string
  sortOrder?: string
}

export interface CulturePageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

export const cultureStudyApi = {
  getFilterOptions() {
    return request.get<ApiResult<{
      categories: { key: string; name: string; icon: string }[]
      regions: { key: string; name: string }[]
      durations: { key: string; name: string }[]
      resourceKinds: { key: string; name: string }[]
    }>>('/culture/filter-options')
  },

  listResources(params: CultureQueryParams) {
    return request.get<ApiResult<CulturePageResult<CultureResourceItem>>>('/culture/resources/page', { params })
  },

  getResource(id: number) {
    return request.get<ApiResult<CultureResourceItem>>(`/culture/resources/${id}`)
  },

  viewResource(id: number) {
    return request.post<ApiResult>(`/culture/resources/${id}/view`)
  },

  downloadResource(id: number) {
    return request.post<ApiResult>(`/culture/resources/${id}/download`)
  },

  listPackages(params: CultureQueryParams) {
    return request.get<ApiResult<CulturePageResult<CulturePackageItem>>>('/culture/packages/page', { params })
  },

  getPackage(id: number) {
    return request.get<ApiResult<{ package: CulturePackageItem; resources: CultureResourceItem[] }>>(
      `/culture/packages/${id}`
    )
  },

  downloadPackage(id: number) {
    return request.post<ApiResult>(`/culture/packages/${id}/download`)
  },

  /** 上传传统文化研学资源（需登录） */
  uploadResource(formData: FormData) {
    return request.post<ApiResult<CultureResourceItem>>('/culture/resources/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}
