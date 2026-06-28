import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'

export interface DictionaryExamSceneItem {
  id: number
  code: string
  name: string
  examLevel?: string
  sort?: number
  status?: number
}

export interface DictionaryFileFormatItem {
  id: number
  code: string
  name: string
  extensions: string
  mimeTypes?: string
  previewType?: string
  sort?: number
  status?: number
}

export interface DictionaryRegionItem {
  id: number
  parentId: number
  code: string
  name: string
  level?: number
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

export const adminDictionaryApi = {
  listExamScenes(includeDisabled = true) {
    return list<DictionaryExamSceneItem>('/admin/dictionaries/exam-scenes', { includeDisabled })
  },
  createExamScene(payload: Omit<DictionaryExamSceneItem, 'id'>) {
    return create<DictionaryExamSceneItem>('/admin/dictionaries/exam-scenes', payload)
  },
  updateExamScene(id: number, payload: Omit<DictionaryExamSceneItem, 'id'>) {
    return update<DictionaryExamSceneItem>('/admin/dictionaries/exam-scenes', id, payload)
  },
  setExamSceneStatus(id: number, status: 0 | 1) {
    return setStatus('/admin/dictionaries/exam-scenes', id, status)
  },
  deleteExamScene(id: number) {
    return remove('/admin/dictionaries/exam-scenes', id)
  },

  listFileFormats(includeDisabled = true) {
    return list<DictionaryFileFormatItem>('/admin/dictionaries/file-formats', { includeDisabled })
  },
  createFileFormat(payload: Omit<DictionaryFileFormatItem, 'id'>) {
    return create<DictionaryFileFormatItem>('/admin/dictionaries/file-formats', payload)
  },
  updateFileFormat(id: number, payload: Omit<DictionaryFileFormatItem, 'id'>) {
    return update<DictionaryFileFormatItem>('/admin/dictionaries/file-formats', id, payload)
  },
  setFileFormatStatus(id: number, status: 0 | 1) {
    return setStatus('/admin/dictionaries/file-formats', id, status)
  },
  deleteFileFormat(id: number) {
    return remove('/admin/dictionaries/file-formats', id)
  },

  listRegions(parentId?: number, includeDisabled = true) {
    return list<DictionaryRegionItem>('/admin/dictionaries/regions', { parentId, includeDisabled })
  },
  createRegion(payload: Omit<DictionaryRegionItem, 'id'>) {
    return create<DictionaryRegionItem>('/admin/dictionaries/regions', payload)
  },
  updateRegion(id: number, payload: Omit<DictionaryRegionItem, 'id'>) {
    return update<DictionaryRegionItem>('/admin/dictionaries/regions', id, payload)
  },
  setRegionStatus(id: number, status: 0 | 1) {
    return setStatus('/admin/dictionaries/regions', id, status)
  },
  deleteRegion(id: number) {
    return remove('/admin/dictionaries/regions', id)
  },
}
