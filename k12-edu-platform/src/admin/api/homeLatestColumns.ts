import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'

export interface HomeLatestRule {
  stageKey?: string
  subjectName?: string
  moduleNames?: string[]
  excludeModuleNames?: string[]
  resourceTypeNames?: string[]
  titleKeyword?: string
  limit?: number
  orderBy?: string
}

export interface HomeLatestColumnItem {
  id: number
  columnKey: string
  title: string
  morePath: string
  dataSource: 'rule' | 'manual' | 'api'
  rule?: HomeLatestRule | null
  sort?: number
  status?: number
  remark?: string
}

export interface HomeLatestListItem {
  id?: number
  title: string
  date?: string
  itemType?: 'resource' | 'article' | 'link'
  resourceId?: number
  resourceSource?: string
  articleId?: number
  linkPath?: string
}

export interface HomeLatestColumnWithItems {
  key: string
  title: string
  morePath: string
  dataSource: 'rule' | 'manual' | 'api'
  items: HomeLatestListItem[]
}

export function joinCsvList(list?: string[] | null) {
  return list?.length ? list.join(', ') : ''
}

export function splitCsvList(text: string): string[] {
  return text
    .split(/[,，]/)
    .map((s) => s.trim())
    .filter(Boolean)
}

function list<T>(path: string, params?: Record<string, unknown>) {
  return request.get<ApiResult<T[]>>(path, { params }).then(unwrapData)
}

export const adminHomeLatestApi = {
  listColumns(includeDisabled = true) {
    return list<HomeLatestColumnItem>('/admin/home/latest-columns', { includeDisabled })
  },
  updateColumn(id: number, payload: {
    title: string
    morePath: string
    dataSource: string
    rule?: HomeLatestRule | null
    sort?: number
    status?: number
    remark?: string
  }) {
    return request.put<ApiResult<HomeLatestColumnItem>>(`/admin/home/latest-columns/${id}`, payload).then(unwrapData)
  },
  setColumnStatus(id: number, status: 0 | 1) {
    return request
      .put<ApiResult<void>>(`/admin/home/latest-columns/${id}/status`, null, { params: { status } })
      .then(unwrapData)
  },
  preview(id: number, stageKey?: string) {
    return list<HomeLatestListItem>(`/admin/home/latest-columns/${id}/preview`, stageKey ? { stageKey } : undefined)
  },
  listManualItems(columnId: number, includeDisabled = true) {
    return list<HomeLatestListItem>(`/admin/home/latest-columns/${columnId}/items`, { includeDisabled })
  },
  createManualItem(columnId: number, payload: {
    title: string
    itemDate?: string
    resourceId?: number
    resourceSource?: string
    linkPath?: string
    articleId?: number
    sort?: number
    status?: number
  }) {
    return request
      .post<ApiResult<HomeLatestListItem>>(`/admin/home/latest-columns/${columnId}/items`, payload)
      .then(unwrapData)
  },
  updateManualItem(itemId: number, payload: {
    title: string
    itemDate?: string
    resourceId?: number
    resourceSource?: string
    linkPath?: string
    articleId?: number
    sort?: number
    status?: number
  }) {
    return request
      .put<ApiResult<HomeLatestListItem>>(`/admin/home/latest-columns/items/${itemId}`, payload)
      .then(unwrapData)
  },
  deleteManualItem(itemId: number) {
    return request.delete<ApiResult<void>>(`/admin/home/latest-columns/items/${itemId}`).then(unwrapData)
  },
}
