import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'

export interface HomePanelTabItem {
  id: number
  panelCode: string
  tabKey: string
  filterKey?: string
  tabLabel?: string
  moduleNames?: string[]
  excludeModuleNames?: string[]
  resourceTypeNames?: string[]
  titleKeyword?: string
  queryMode: string
  sort?: number
  status?: number
}

export interface HomePanelFeaturedItem {
  id: number
  panelCode: string
  tabKey: string
  filterKey?: string
  stageKey?: string
  subjectName?: string
  gradeName?: string
  resourceId: number
  resourceSource: string
  resourceTitle?: string
  sort?: number
  expireTime?: string
  status?: number
}

export interface HomePanelPreviewItem {
  id: number
  title: string
  date?: string
  source?: string
}

export interface HomePanelPreviewResult {
  items: HomePanelPreviewItem[]
  limit?: number
}

function list<T>(path: string, params?: Record<string, unknown>) {
  return request.get<ApiResult<T[]>>(path, { params }).then(unwrapData)
}

function create<T>(path: string, payload: unknown) {
  return request.post<ApiResult<T>>(path, payload).then(unwrapData)
}

function update<T>(path: string, id: number, payload: unknown) {
  return request.put<ApiResult<T>>(`${path}/${id}`, payload).then(unwrapData)
}

function setStatus(path: string, id: number, status: 0 | 1) {
  return request.put<ApiResult<void>>(`${path}/${id}/status`, null, { params: { status } }).then(unwrapData)
}

function remove(path: string, id: number) {
  return request.delete<ApiResult<void>>(`${path}/${id}`).then(unwrapData)
}

export const adminHomePanelApi = {
  listTabs(panelCode?: string, includeDisabled = true) {
    return list<HomePanelTabItem>('/admin/home/panels/tabs', { panelCode, includeDisabled })
  },
  updateTab(id: number, payload: Partial<HomePanelTabItem> & { queryMode: string }) {
    return update<HomePanelTabItem>('/admin/home/panels/tabs', id, payload)
  },
  setTabStatus(id: number, status: 0 | 1) {
    return setStatus('/admin/home/panels/tabs', id, status)
  },

  listFeatured(panelCode?: string, tabKey?: string, filterKey?: string, includeDisabled = true) {
    return list<HomePanelFeaturedItem>('/admin/home/panels/featured', {
      panelCode,
      tabKey,
      filterKey,
      includeDisabled,
    })
  },
  createFeatured(payload: Omit<HomePanelFeaturedItem, 'id' | 'resourceTitle'>) {
    return create<HomePanelFeaturedItem>('/admin/home/panels/featured', payload)
  },
  updateFeatured(id: number, payload: Omit<HomePanelFeaturedItem, 'id' | 'resourceTitle'>) {
    return update<HomePanelFeaturedItem>('/admin/home/panels/featured', id, payload)
  },
  setFeaturedStatus(id: number, status: 0 | 1) {
    return setStatus('/admin/home/panels/featured', id, status)
  },
  deleteFeatured(id: number) {
    return remove('/admin/home/panels/featured', id)
  },

  preview(params: {
    panelCode: string
    tabKey: string
    filterKey?: string
    stageKey?: string
    subjectName?: string
    gradeName?: string
    limit?: number
  }) {
    return request
      .get<ApiResult<HomePanelPreviewResult>>('/admin/home/panels/preview', { params })
      .then(unwrapData)
  },
}

export function splitCsvList(input: string): string[] {
  return input
    .split(/[,，]/)
    .map((s) => s.trim())
    .filter(Boolean)
}

export function joinCsvList(list?: string[]): string {
  return list?.join(', ') ?? ''
}

export const PANEL_CODE_OPTIONS = [
  { value: 'sync_prep', label: '同步备课' },
  { value: 'paper_zone', label: '试卷专区' },
  { value: 'promotion', label: '升学专区' },
]

export const RESOURCE_SOURCE_OPTIONS = [
  { value: 'oss_primary_chinese', label: 'OSS 宽表' },
  { value: 'edu_resource', label: 'edu_resource 主表' },
  { value: 'edu_resource_suite', label: '成套资源' },
]
