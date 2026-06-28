import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'
import type { NavTarget } from '@/types/homeOps'

export interface AdminHomeBannerItem {
  id: number
  slotCode?: string
  title: string
  subtitle?: string
  ctaText?: string
  icon?: string
  bgColorStart?: string
  bgColorEnd?: string
  imageUrl?: string
  navTarget: NavTarget
  stageKeys?: string[]
  startTime?: string
  endTime?: string
  sort?: number
  status?: number
  remark?: string
}

export interface AdminHomeQuickEntryItem {
  id: number
  entryKey: string
  title: string
  description?: string
  icon?: string
  accentColor?: string
  navTarget: NavTarget
  stageKeys?: string[]
  sort?: number
  status?: number
  remark?: string
}

export interface AdminHomeHotWordItem {
  id: number
  label: string
  actionType: 'browse' | 'search'
  navTarget: NavTarget
  badge?: string
  stageKeys?: string[]
  startTime?: string
  endTime?: string
  sort?: number
  status?: number
  remark?: string
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

export const adminHomeOpsApi = {
  listBanners(includeDisabled = true, slotCode = 'home_hero') {
    return list<AdminHomeBannerItem>('/admin/home/banners', { includeDisabled, slotCode })
  },
  createBanner(payload: Omit<AdminHomeBannerItem, 'id'>) {
    return create<AdminHomeBannerItem>('/admin/home/banners', payload)
  },
  updateBanner(id: number, payload: Omit<AdminHomeBannerItem, 'id'>) {
    return update<AdminHomeBannerItem>('/admin/home/banners', id, payload)
  },
  setBannerStatus(id: number, status: 0 | 1) {
    return setStatus('/admin/home/banners', id, status)
  },
  deleteBanner(id: number) {
    return remove('/admin/home/banners', id)
  },

  listQuickEntries(includeDisabled = true) {
    return list<AdminHomeQuickEntryItem>('/admin/home/quick-entries', { includeDisabled })
  },
  createQuickEntry(payload: Omit<AdminHomeQuickEntryItem, 'id'>) {
    return create<AdminHomeQuickEntryItem>('/admin/home/quick-entries', payload)
  },
  updateQuickEntry(id: number, payload: Omit<AdminHomeQuickEntryItem, 'id'>) {
    return update<AdminHomeQuickEntryItem>('/admin/home/quick-entries', id, payload)
  },
  setQuickEntryStatus(id: number, status: 0 | 1) {
    return setStatus('/admin/home/quick-entries', id, status)
  },
  deleteQuickEntry(id: number) {
    return remove('/admin/home/quick-entries', id)
  },

  listHotWords(includeDisabled = true) {
    return list<AdminHomeHotWordItem>('/admin/home/hot-words', { includeDisabled })
  },
  createHotWord(payload: Omit<AdminHomeHotWordItem, 'id'>) {
    return create<AdminHomeHotWordItem>('/admin/home/hot-words', payload)
  },
  updateHotWord(id: number, payload: Omit<AdminHomeHotWordItem, 'id'>) {
    return update<AdminHomeHotWordItem>('/admin/home/hot-words', id, payload)
  },
  setHotWordStatus(id: number, status: 0 | 1) {
    return setStatus('/admin/home/hot-words', id, status)
  },
  deleteHotWord(id: number) {
    return remove('/admin/home/hot-words', id)
  },
  reorderHotWords(items: { id: number; sort: number }[]) {
    return request.put<ApiResult<void>>('/admin/home/hot-words/reorder', { items }).then(unwrapData)
  },
}
