import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'

export interface OpsChannelStats {
  total?: string
  elite?: string
  free?: string
}

export interface OpsChannelUi {
  showGradeFilter?: boolean
  showSubjectFilter?: boolean
  eliteTitle?: string
  eliteDesc?: string
}

export interface OpsChannelItem {
  id: number
  code: string
  name: string
  icon?: string
  description?: string
  bgGradient?: string
  routePath?: string
  stats?: OpsChannelStats
  ui?: OpsChannelUi
  sort?: number
  status?: number
  remark?: string
}

export interface OpsChannelTabItem {
  id: number
  channelCode: string
  tabKey: string
  tabName: string
  icon?: string
  searchKeyword?: string
  sort?: number
  status?: number
}

export interface OpsChannelAlbumItem {
  id: number
  channelCode: string
  title: string
  icon?: string
  meta?: string
  resourceCount?: number
  downloadCount?: number
  coverGradient?: string
  linkPath?: string
  sort?: number
  status?: number
}

export const adminOpsChannelApi = {
  list(includeDisabled = true) {
    return request
      .get<ApiResult<OpsChannelItem[]>>('/admin/operation/channels', { params: { includeDisabled } })
      .then(unwrapData)
  },
  update(id: number, payload: {
    name: string
    icon?: string
    description?: string
    bgGradient?: string
    routePath?: string
    stats?: OpsChannelStats
    ui?: OpsChannelUi
    sort?: number
    status?: number
  }) {
    return request.put<ApiResult<OpsChannelItem>>(`/admin/operation/channels/${id}`, payload).then(unwrapData)
  },
  setStatus(id: number, status: 0 | 1) {
    return request
      .put<ApiResult<void>>(`/admin/operation/channels/${id}/status`, null, { params: { status } })
      .then(unwrapData)
  },
  listTabs(code: string, includeDisabled = true) {
    return request
      .get<ApiResult<OpsChannelTabItem[]>>(`/admin/operation/channels/${code}/tabs`, { params: { includeDisabled } })
      .then(unwrapData)
  },
  updateTab(tabId: number, payload: {
    tabKey: string
    tabName: string
    icon?: string
    searchKeyword?: string
    sort?: number
    status?: number
  }) {
    return request
      .put<ApiResult<OpsChannelTabItem>>(`/admin/operation/channels/tabs/${tabId}`, payload)
      .then(unwrapData)
  },
  listAlbums(code: string, includeDisabled = true) {
    return request
      .get<ApiResult<OpsChannelAlbumItem[]>>(`/admin/operation/channels/${code}/albums`, { params: { includeDisabled } })
      .then(unwrapData)
  },
  createAlbum(code: string, payload: {
    title: string
    icon?: string
    meta?: string
    resourceCount?: number
    downloadCount?: number
    coverGradient?: string
    linkPath?: string
    sort?: number
    status?: number
  }) {
    return request
      .post<ApiResult<OpsChannelAlbumItem>>(`/admin/operation/channels/${code}/albums`, payload)
      .then(unwrapData)
  },
  updateAlbum(albumId: number, payload: {
    title: string
    icon?: string
    meta?: string
    resourceCount?: number
    downloadCount?: number
    coverGradient?: string
    linkPath?: string
    sort?: number
    status?: number
  }) {
    return request
      .put<ApiResult<OpsChannelAlbumItem>>(`/admin/operation/channels/albums/${albumId}`, payload)
      .then(unwrapData)
  },
  deleteAlbum(albumId: number) {
    return request.delete<ApiResult<void>>(`/admin/operation/channels/albums/${albumId}`).then(unwrapData)
  },
}
