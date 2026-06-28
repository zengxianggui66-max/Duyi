import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'

export interface TagTeachingSceneItem {
  id: number
  code: string
  name: string
  sort?: number
  status?: number
}

export interface TagBrowseTagItem {
  id: number
  code: string
  name: string
  tagGroup?: string
  applicableStages?: string | null
  applicableModules?: string | null
  sort?: number
  status?: number
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

export function parseJsonList(raw?: string | null): string[] {
  if (!raw) return []
  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed.map(String) : []
  } catch {
    return []
  }
}

export function stringifyJsonList(list?: string[]): string | null {
  if (!list || list.length === 0) return null
  return JSON.stringify(list)
}

export const adminTagApi = {
  listTeachingScenes(includeDisabled = true) {
    return list<TagTeachingSceneItem>('/admin/tags/teaching-scenes', { includeDisabled })
  },
  createTeachingScene(payload: Omit<TagTeachingSceneItem, 'id'>) {
    return create<TagTeachingSceneItem>('/admin/tags/teaching-scenes', payload)
  },
  updateTeachingScene(id: number, payload: Omit<TagTeachingSceneItem, 'id'>) {
    return update<TagTeachingSceneItem>('/admin/tags/teaching-scenes', id, payload)
  },
  setTeachingSceneStatus(id: number, status: 0 | 1) {
    return setStatus('/admin/tags/teaching-scenes', id, status)
  },
  deleteTeachingScene(id: number) {
    return remove('/admin/tags/teaching-scenes', id)
  },

  listBrowseTags(includeDisabled = true) {
    return list<TagBrowseTagItem>('/admin/tags/browse-tags', { includeDisabled })
  },
  createBrowseTag(payload: Record<string, unknown>) {
    return create<TagBrowseTagItem>('/admin/tags/browse-tags', payload)
  },
  updateBrowseTag(id: number, payload: Record<string, unknown>) {
    return update<TagBrowseTagItem>('/admin/tags/browse-tags', id, payload)
  },
  setBrowseTagStatus(id: number, status: 0 | 1) {
    return setStatus('/admin/tags/browse-tags', id, status)
  },
  deleteBrowseTag(id: number) {
    return remove('/admin/tags/browse-tags', id)
  },
}
