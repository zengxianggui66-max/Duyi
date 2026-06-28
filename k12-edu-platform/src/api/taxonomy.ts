/**
 * Phase 5-A：分类维度统一读 API
 */
import { request, unwrapData } from './request'
import type { ApiResult } from './request'

export interface TaxonomyStage {
  id: number
  code: string
  name: string
  icon?: string
  sort?: number
  status?: number
}

export interface TaxonomySubject {
  id: number
  stageId: number
  stageCode?: string
  stageName?: string
  code: string
  name: string
  icon?: string
  sort?: number
  status?: number
}

export interface TaxonomyEdition {
  id: number
  code: string
  name: string
  shortName?: string
  publisher?: string
  yearLabel?: string
  sort?: number
  status?: number
  subjectId?: number
  subjectCode?: string
  subjectName?: string
}

export interface TaxonomyVolume {
  id: number
  code?: string
  name: string
  stageId?: number
  stageCode?: string
  stageName?: string
  gradeId?: number
  gradeCode?: string
  gradeName?: string
  volumeId?: number
  volumeCode?: string
  volumePart?: string
  sort?: number
}

export interface TaxonomyModule {
  id: number
  code?: string
  name: string
  icon?: string
  moduleCategory?: string
  description?: string
  sort?: number
  status?: number
  stageId?: number
  stageCode?: string
  stageName?: string
}

export interface TaxonomyResourceType {
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

export interface TaxonomyGrade {
  id: number
  stageId?: number
  stageCode?: string
  stageName?: string
  code: string
  name: string
  sort?: number
  status?: number
}

export const taxonomyApi = {
  /** 学段列表 */
  getStages(includeDisabled = false) {
    return request
      .get<ApiResult<TaxonomyStage[]>>('/taxonomy/stages', {
        params: { includeDisabled },
        silentError: true,
      })
      .then(unwrapData)
  },

  /** 学科列表（stage 为 code 或中文名，如 primary / 小学） */
  getSubjects(stage?: string, includeDisabled = false) {
    return request
      .get<ApiResult<TaxonomySubject[]>>('/taxonomy/subjects', {
        params: { stage, includeDisabled },
        silentError: true,
      })
      .then(unwrapData)
  },

  /** 教材版本（stage/subject 支持 code 或中文名） */
  getEditions(stage: string | undefined, subject: string, includeDisabled = false) {
    return request
      .get<ApiResult<TaxonomyEdition[]>>('/taxonomy/editions', {
        params: { stage, subject, includeDisabled },
        silentError: true,
      })
      .then(unwrapData)
  },

  /** 教材册别（stage 为 code 或中文名，如 primary / 小学） */
  getVolumes(stage: string, includeDisabled = false) {
    return request
      .get<ApiResult<TaxonomyVolume[]>>('/taxonomy/volumes', {
        params: { stage, includeDisabled },
        silentError: true,
      })
      .then(unwrapData)
  },

  /** 栏目列表（stage 为 code 或中文名） */
  getModules(stage: string, includeDisabled = false) {
    return request
      .get<ApiResult<TaxonomyModule[]>>('/taxonomy/modules', {
        params: { stage, includeDisabled },
        silentError: true,
      })
      .then(unwrapData)
  },

  /** 资源类型（叶子节点，可按 stage/subject/module 缩小分组） */
  getResourceTypes(stage?: string, subject?: string, module?: string, includeDisabled = false) {
    return request
      .get<ApiResult<TaxonomyResourceType[]>>('/taxonomy/resource-types', {
        params: { stage, subject, module, includeDisabled },
        silentError: true,
      })
      .then(unwrapData)
  },

  /** 年级列表（stage 为 code 或中文名，如 primary / 小学） */
  getGrades(stage: string, includeDisabled = false) {
    return request
      .get<ApiResult<TaxonomyGrade[]>>('/taxonomy/grades', {
        params: { stage, includeDisabled },
        silentError: true,
      })
      .then(unwrapData)
  },
}
