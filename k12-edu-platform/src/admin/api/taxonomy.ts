import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'

export interface TaxonomyStageItem {
  id: number
  code: string
  name: string
  icon?: string
  sort?: number
  status?: number
}

export interface TaxonomySubjectItem {
  id: number
  stageId: number
  stageCode?: string
  stageName?: string
  code: string
  name: string
  icon?: string
  sort?: number
  status?: number
  editionIds?: number[]
  moduleIds?: number[]
  resourceTypeIds?: number[]
}

export interface TaxonomyEditionItem {
  id: number
  code: string
  name: string
  shortName?: string
  /** Phase 6: 绑定学段 */
  stageId?: number
  /** Phase 6: 绑定学科 */
  subjectId?: number
  publisher?: string
  yearLabel?: string
  sort?: number
  status?: number
}

export interface TaxonomyGradeItem {
  id: number
  stageId: number
  code?: string
  name: string
  sort?: number
  status?: number
}

export interface TaxonomyVolumeItem {
  id: number
  code: string
  name: string
  sort?: number
  /** Phase 6: 状态 */
  status?: number
  /** Phase 6: 绑定学段 */
  stageId?: number
  /** Phase 6: 绑定学科 */
  subjectId?: number
  /** Phase 6: 绑定版本 */
  editionId?: number
}

export interface TaxonomyModuleItem {
  id: number
  code: string
  name: string
  icon?: string
  moduleCategory?: string
  description?: string
  sort?: number
  status?: number
  stageIds?: number[]
  stageNames?: string[]
}

export interface TaxonomyResourceTypeItem {
  id: number
  parentId?: number
  code?: string
  name: string
  icon?: string
  groupCode?: string
  groupName?: string
  allowPreview?: number
  sort?: number
  status?: number
}

type StatusPayload = { status?: number }

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

export const adminTaxonomyApi = {
  listStages(includeDisabled = true) {
    return list<TaxonomyStageItem>('/admin/taxonomy/stages', { includeDisabled })
  },
  createStage(payload: Omit<TaxonomyStageItem, 'id'>) {
    return create<TaxonomyStageItem>('/admin/taxonomy/stages', payload)
  },
  updateStage(id: number, payload: Omit<TaxonomyStageItem, 'id'>) {
    return update<TaxonomyStageItem>('/admin/taxonomy/stages', id, payload)
  },
  setStageStatus(id: number, status: 0 | 1) {
    return setStatus('/admin/taxonomy/stages', id, status)
  },
  deleteStage(id: number) {
    return remove('/admin/taxonomy/stages', id)
  },

  listSubjects(stageId?: number, includeDisabled = true) {
    return list<TaxonomySubjectItem>('/admin/taxonomy/subjects', { stageId, includeDisabled })
  },
  createSubject(payload: Partial<TaxonomySubjectItem> & { stageId: number; code: string; name: string }) {
    return create<TaxonomySubjectItem>('/admin/taxonomy/subjects', payload)
  },
  updateSubject(id: number, payload: Partial<TaxonomySubjectItem> & { stageId: number; code: string; name: string }) {
    return update<TaxonomySubjectItem>('/admin/taxonomy/subjects', id, payload)
  },
  setSubjectStatus(id: number, status: 0 | 1) {
    return setStatus('/admin/taxonomy/subjects', id, status)
  },
  deleteSubject(id: number) {
    return remove('/admin/taxonomy/subjects', id)
  },

  /** Phase 6：支持 stageId/subjectId 筛选 */
  listEditions(stageId?: number, subjectId?: number, includeDisabled = true) {
    return list<TaxonomyEditionItem>('/admin/taxonomy/editions', { stageId, subjectId, includeDisabled })
  },
  createEdition(payload: Omit<TaxonomyEditionItem, 'id'>) {
    return create<TaxonomyEditionItem>('/admin/taxonomy/editions', payload)
  },
  updateEdition(id: number, payload: Omit<TaxonomyEditionItem, 'id'>) {
    return update<TaxonomyEditionItem>('/admin/taxonomy/editions', id, payload)
  },
  setEditionStatus(id: number, status: 0 | 1) {
    return setStatus('/admin/taxonomy/editions', id, status)
  },
  deleteEdition(id: number) {
    return remove('/admin/taxonomy/editions', id)
  },

  listGrades(stageId?: number, includeDisabled = true) {
    return list<TaxonomyGradeItem>('/admin/taxonomy/grades', { stageId, includeDisabled })
  },
  createGrade(payload: Partial<TaxonomyGradeItem> & { stageId: number; name: string }) {
    return create<TaxonomyGradeItem>('/admin/taxonomy/grades', payload)
  },
  updateGrade(id: number, payload: Partial<TaxonomyGradeItem> & { stageId: number; name: string }) {
    return update<TaxonomyGradeItem>('/admin/taxonomy/grades', id, payload)
  },
  setGradeStatus(id: number, status: 0 | 1) {
    return setStatus('/admin/taxonomy/grades', id, status)
  },
  deleteGrade(id: number) {
    return remove('/admin/taxonomy/grades', id)
  },

  /** Phase 6：支持多维筛选 */
  listVolumes(stageId?: number, subjectId?: number, editionId?: number, includeDisabled = true) {
    return list<TaxonomyVolumeItem>('/admin/taxonomy/volumes', { stageId, subjectId, editionId, includeDisabled })
  },
  createVolume(payload: Omit<TaxonomyVolumeItem, 'id'>) {
    return create<TaxonomyVolumeItem>('/admin/taxonomy/volumes', payload)
  },
  updateVolume(id: number, payload: Omit<TaxonomyVolumeItem, 'id'>) {
    return update<TaxonomyVolumeItem>('/admin/taxonomy/volumes', id, payload)
  },
  /** Phase 6：册别状态开关 */
  setVolumeStatus(id: number, status: 0 | 1) {
    return setStatus('/admin/taxonomy/volumes', id, status)
  },
  deleteVolume(id: number) {
    return remove('/admin/taxonomy/volumes', id)
  },

  listModules(stageId?: number, includeDisabled = true) {
    return list<TaxonomyModuleItem>('/admin/taxonomy/modules', { stageId, includeDisabled })
  },
  createModule(payload: Partial<TaxonomyModuleItem> & { code: string; name: string }) {
    return create<TaxonomyModuleItem>('/admin/taxonomy/modules', payload)
  },
  updateModule(id: number, payload: Partial<TaxonomyModuleItem> & { code: string; name: string }) {
    return update<TaxonomyModuleItem>('/admin/taxonomy/modules', id, payload)
  },
  setModuleStatus(id: number, status: 0 | 1) {
    return setStatus('/admin/taxonomy/modules', id, status)
  },
  deleteModule(id: number) {
    return remove('/admin/taxonomy/modules', id)
  },

  listResourceTypes(parentId?: number, includeDisabled = true) {
    return list<TaxonomyResourceTypeItem>('/admin/taxonomy/resource-types', { parentId, includeDisabled })
  },
  createResourceType(payload: Partial<TaxonomyResourceTypeItem> & { parentId: number; code: string; name: string }) {
    return create<TaxonomyResourceTypeItem>('/admin/taxonomy/resource-types', payload)
  },
  updateResourceType(
    id: number,
    payload: Partial<TaxonomyResourceTypeItem> & { parentId: number; code: string; name: string },
  ) {
    return update<TaxonomyResourceTypeItem>('/admin/taxonomy/resource-types', id, payload)
  },
  setResourceTypeStatus(id: number, status: 0 | 1) {
    return setStatus('/admin/taxonomy/resource-types', id, status)
  },
  deleteResourceType(id: number) {
    return remove('/admin/taxonomy/resource-types', id)
  },
}

export type { StatusPayload }
