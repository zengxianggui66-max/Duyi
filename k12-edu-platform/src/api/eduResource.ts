import { request } from './request'
import type { ApiResult, PageData } from './request'

export interface DimensionItem {
  id: number
  name: string
  icon?: string
}

export interface FilterOptions {
  stages: DimensionItem[]
  subjects: DimensionItem[]
  editions: DimensionItem[]
  grades: DimensionItem[]
  semesters: DimensionItem[]
  volumes: DimensionItem[]
  modules: DimensionItem[]
  resourceTypes: DimensionItem[]
  units: DimensionItem[]
}

export interface EduResourceItem {
  id: number
  title: string
  description?: string
  originalFilename?: string
  fileExt?: string
  ossBucket?: string
  ossObjectKey?: string
  ossUrl?: string
  fileSizeKb?: number
  downloadCount?: number
  viewCount?: number
  collectCount?: number
  /** 旧兼容字段：业务判断优先使用 auditStatus/publishStatus/isDeleted。 */
  status?: number
  auditStatus?: number
  publishStatus?: number
  isDeleted?: number
  uploaderId?: number
  uploadTime?: string
  updateTime?: string
  sort?: number
  remark?: string
  stageName?: string
  subjectName?: string
  editionName?: string
  gradeName?: string
  semesterName?: string
  volumeName?: string
  moduleName?: string
  resourceTypeName?: string
  unitName?: string
  lessonName?: string
  catalogNodeId?: number
}

export interface EduResourceParams {
  stageName?: string
  subjectName?: string
  editionName?: string
  gradeName?: string
  semesterName?: string
  volumeName?: string
  moduleName?: string
  resourceTypeName?: string
  /** 兼容字段：目录资源匹配优先传 catalogNodeId。 */
  unitName?: string
  lessonName?: string
  catalogNodeId?: number
  keyword?: string
  fileExt?: string
  status?: number
  auditStatus?: number
  publishStatus?: number
  isDeleted?: number
  current?: number
  size?: number
  sortField?: string
  sortOrder?: 'desc' | 'asc'
}

export const eduResourceApi = {
  getPage(params?: EduResourceParams) {
    return request.get<ApiResult<PageData<EduResourceItem>>>('/edu-resource/page', {
      params,
    })
  },

  getList(params?: Omit<EduResourceParams, 'current' | 'size'>) {
    return request.get<ApiResult<EduResourceItem[]>>('/edu-resource/list', {
      params,
    })
  },

  getDetail(id: number) {
    return request.get<ApiResult<EduResourceItem>>(`/edu-resource/${id}`)
  },

  addDownloadCount(id: number) {
    return request.post<ApiResult>(`/edu-resource/${id}/download`)
  },

  addViewCount(id: number) {
    return request.post<ApiResult>(`/edu-resource/${id}/view`)
  },

  getFilterOptions(stageName?: string, subjectName?: string) {
    return request.get<ApiResult<FilterOptions>>('/edu-resource/filter-options', {
      params: { stageName, subjectName },
      silentError: true,
    })
  },

  getStatsByDimension(params?: { dimension: string; parentValue?: string }) {
    return request.get<ApiResult<Array<{ value: string; label: string; count: number }>>>(
      '/edu-resource/stats-by-dimension',
      {
        params,
        silentError: true,
      },
    )
  },

  getModules(params?: { stageName?: string; subjectName?: string }) {
    return request.get<ApiResult<DimensionItem[]>>('/edu-resource/modules', {
      params,
      silentError: true,
    })
  },

  getUnits(params?: {
    gradeName?: string
    editionName?: string
    volumeName?: string
    subjectName?: string
  }) {
    return request.get<ApiResult<DimensionItem[]>>('/edu-resource/units', {
      params,
      silentError: true,
    })
  },

  getResourceTypes(params?: {
    stageName?: string
    subjectName?: string
    moduleName?: string
  }) {
    return request.get<ApiResult<DimensionItem[]>>('/edu-resource/resource-types', {
      params,
      silentError: true,
    })
  },
}
