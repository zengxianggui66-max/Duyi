/**
 * 专题资源专区 API（方案 B 独立表）
 */
import { request } from './request'
import type { ApiResult, SilentRequestConfig } from './request'

const silentRead: SilentRequestConfig = { silentError: true }

export interface TopicResourceItem {
  id: number
  title: string
  summary?: string
  description?: string
  category: string
  region: string
  gradeStage: string
  gradeLevel?: string
  subject?: string
  resourceType?: string
  resourceForm: string
  topicLabel?: string
  schoolYear?: string
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

export interface TopicAlbumItem {
  id: number
  title: string
  summary?: string
  category: string
  region: string
  gradeStage: string
  icon?: string
  tags?: string
  resourceCount?: number
  downloadCount?: number
  isFree?: number
  isElite?: number
}

export interface TopicQueryParams {
  category?: string
  region?: string
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

export interface TopicZoneStats {
  total: number
  free: number
  elite: number
  albums: number
  byRegion?: { region: string; name: string; count: number }[]
  byCategory?: { category: string; name: string; icon: string; count: number }[]
}

export interface TopicCalendarHint {
  category: string
  title: string
  desc: string
  icon: string
  schoolYear: string
  month: number
}

export interface TopicHotKeyword {
  keyword: string
  rank: number
}

export interface TopicPageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

export const topicApi = {
  getStats() {
    return request.get<ApiResult<TopicZoneStats>>('/topic/stats', silentRead)
  },

  getCalendarHint() {
    return request.get<ApiResult<TopicCalendarHint>>('/topic/calendar-hint', silentRead)
  },

  getHotKeywords(limit = 10) {
    return request.get<ApiResult<TopicHotKeyword[]>>('/topic/hot-keywords', {
      params: { limit },
      ...silentRead,
    })
  },

  listHotResources(params?: { limit?: number; region?: string }) {
    return request.get<ApiResult<TopicResourceItem[]>>('/topic/resources/hot', {
      params,
      ...silentRead,
    })
  },

  listLatestResources(params?: { limit?: number; region?: string }) {
    return request.get<ApiResult<TopicResourceItem[]>>('/topic/resources/latest', {
      params,
      ...silentRead,
    })
  },

  getFilterOptions() {
    return request.get<ApiResult<{
      categories: { key: string; name: string; icon: string }[]
      regions: { key: string; name: string }[]
      gradeStages: { key: string; name: string }[]
      resourceForms: { key: string; name: string }[]
    }>>('/topic/filter-options', silentRead)
  },

  listResources(params: TopicQueryParams) {
    return request.get<ApiResult<TopicPageResult<TopicResourceItem>>>(
      '/topic/resources/page',
      { params, ...silentRead }
    )
  },

  getResource(id: number) {
    return request.get<ApiResult<TopicResourceItem>>(`/topic/resources/${id}`, silentRead)
  },

  recordView(id: number) {
    return request.post<ApiResult>(`/topic/resources/${id}/view`, null, silentRead)
  },

  recordDownload(id: number) {
    return request.post<ApiResult>(`/topic/resources/${id}/download`, null, silentRead)
  },

  listAlbums(params: TopicQueryParams) {
    return request.get<ApiResult<TopicPageResult<TopicAlbumItem>>>(
      '/topic/albums/page',
      { params, ...silentRead }
    )
  },

  getAlbum(id: number) {
    return request.get<ApiResult<{ album: TopicAlbumItem; resources: TopicResourceItem[] }>>(
      `/topic/albums/${id}`,
      silentRead
    )
  },

  downloadAlbum(id: number) {
    return request.post<ApiResult>(`/topic/albums/${id}/download`, null, silentRead)
  },

  uploadResource(formData: FormData) {
    return request.post<ApiResult<TopicResourceItem>>('/topic/resources/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}
