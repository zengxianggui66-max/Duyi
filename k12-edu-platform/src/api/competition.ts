/**
 * 竞赛专区 API（方案 B 独立表）
 */
import { request } from './request'
import type { ApiResult, SilentRequestConfig } from './request'

const silentRead: SilentRequestConfig = { silentError: true }

export interface CompetitionResourceItem {
  id: number
  title: string
  summary?: string
  category: string
  gradeStage: string
  subject?: string
  resourceForm: string
  competitionName?: string
  fileFormat?: string
  fileUrl?: string
  coverUrl?: string
  icon?: string
  tags?: string
  downloadCount?: number
  viewCount?: number
  isFree?: number
  isElite?: number
}

export interface CompetitionPackageItem {
  id: number
  title: string
  summary?: string
  category: string
  gradeStage: string
  icon?: string
  tags?: string
  resourceCount?: number
  downloadCount?: number
  isFree?: number
  isElite?: number
}

export interface CompetitionQueryParams {
  category?: string
  gradeStage?: string
  subject?: string
  resourceForm?: string
  keyword?: string
  isFree?: number
  current?: number
  size?: number
  sortField?: string
  sortOrder?: string
}

export interface CompetitionPageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

export const competitionApi = {
  getFilterOptions() {
    return request.get<ApiResult<{
      categories: { key: string; name: string; icon: string }[]
      gradeStages: { key: string; name: string }[]
      resourceForms: { key: string; name: string }[]
    }>>('/competition/filter-options', silentRead)
  },

  listResources(params: CompetitionQueryParams) {
    return request.get<ApiResult<CompetitionPageResult<CompetitionResourceItem>>>(
      '/competition/resources/page',
      { params, ...silentRead }
    )
  },

  getResource(id: number) {
    return request.get<ApiResult<CompetitionResourceItem>>(`/competition/resources/${id}`, silentRead)
  },

  recordView(id: number) {
    return request.post<ApiResult>(`/competition/resources/${id}/view`, null, silentRead)
  },

  recordDownload(id: number) {
    return request.post<ApiResult>(`/competition/resources/${id}/download`, null, silentRead)
  },

  listPackages(params: CompetitionQueryParams) {
    return request.get<ApiResult<CompetitionPageResult<CompetitionPackageItem>>>(
      '/competition/packages/page',
      { params, ...silentRead }
    )
  },

  getPackage(id: number) {
    return request.get<ApiResult<{ package: CompetitionPackageItem; resources: CompetitionResourceItem[] }>>(
      `/competition/packages/${id}`,
      silentRead
    )
  },

  downloadPackage(id: number) {
    return request.post<ApiResult>(`/competition/packages/${id}/download`, null, silentRead)
  },

  uploadResource(formData: FormData) {
    return request.post<ApiResult<CompetitionResourceItem>>('/competition/resources/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}
