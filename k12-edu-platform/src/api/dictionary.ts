/**
 * Phase 5-D：字典/标签统一读 API
 */
import { request, unwrapData } from './request'
import type { ApiResult } from './request'

export interface DictionaryItem {
  id?: number
  code: string
  name: string
  sort?: number
  status?: number
  examLevel?: string
  extensions?: string
  previewType?: string
  parentId?: number
  level?: number
  tagGroup?: string
  applicableStages?: string[]
  applicableModules?: string[]
}

export const dictionaryApi = {
  listExamScenes(includeDisabled = false) {
    return request
      .get<ApiResult<DictionaryItem[]>>('/dictionary/exam-scenes', { params: { includeDisabled } })
      .then(unwrapData)
  },
  listTeachingScenes(includeDisabled = false) {
    return request
      .get<ApiResult<DictionaryItem[]>>('/dictionary/teaching-scenes', { params: { includeDisabled } })
      .then(unwrapData)
  },
  listFileFormats(includeDisabled = false) {
    return request
      .get<ApiResult<DictionaryItem[]>>('/dictionary/file-formats', { params: { includeDisabled } })
      .then(unwrapData)
  },
  listRegions(parentId?: number, includeDisabled = false) {
    return request
      .get<ApiResult<DictionaryItem[]>>('/dictionary/regions', { params: { parentId, includeDisabled } })
      .then(unwrapData)
  },
  listBrowseTags(stage?: string, module?: string, includeDisabled = false) {
    return request
      .get<ApiResult<DictionaryItem[]>>('/dictionary/browse-tags', {
        params: { stage, module, includeDisabled },
      })
      .then(unwrapData)
  },
}
