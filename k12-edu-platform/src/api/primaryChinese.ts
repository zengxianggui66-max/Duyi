/**
 * 小学语文学科资源 API
 */
import { request, type SilentRequestConfig } from './request'
import type { ApiResult, PageData } from './request'
import type {
  PrimaryChineseItem,
  PrimaryChineseParams,
  PrimaryChineseFilterOptions,
  UploadFilterOptions,
  UnitTreeNode,
  ResourceSuite,
  MyUploadStats,
  ResourceRejectInfo,
} from './types'

// ==================== API 方法 ====================

/** 学科资源文件代理 URL（预览 inline / 下载 attachment） */
export function buildPrimaryChineseFileUrl(
  id: number,
  disposition: 'inline' | 'attachment' = 'inline',
): string {
  const base = (import.meta.env.VITE_API_BASE || '/api').replace(/\/$/, '')
  return `${base}/primary-chinese/${id}/file?disposition=${disposition}`
}

export const primaryChineseApi = {
  /**
   * 不分页查询
   */
  getList(
    params?: Omit<PrimaryChineseParams, 'current' | 'size'>,
    config?: SilentRequestConfig,
  ) {
    return request.get<ApiResult<PrimaryChineseItem[]>>(
      '/primary-chinese/list',
      { params, ...config },
    )
  },

  /**
   * 分页查询（主列表使用）
   */
  getPage(params?: PrimaryChineseParams, config?: SilentRequestConfig) {
    return request.get<ApiResult<PageData<PrimaryChineseItem>>>(
      '/primary-chinese/page',
      { params, ...config },
    )
  },

  /** 我的上传统计（需登录） */
  getMyUploadStats(config?: SilentRequestConfig) {
    return request.get<ApiResult<MyUploadStats>>('/primary-chinese/mine/stats', {
      silentError: true,
      ...config,
    })
  },

  /**
   * 资源详情（按ID）
   */
  getDetail(id: number) {
    return request.get<ApiResult<PrimaryChineseItem>>(`/primary-chinese/${id}`)
  },

  /** 上传者查看最新驳回信息（status=2 时） */
  getAuditInfo(id: number, config?: SilentRequestConfig) {
    return request.get<ApiResult<ResourceRejectInfo | null>>(
      `/primary-chinese/mine/reject-info/${id}`,
      { silentError: true, ...config },
    )
  },

  addViewCount(id: number) {
    return request.post<ApiResult<string>>(`/primary-chinese/${id}/view`)
  },

  addDownloadCount(id: number) {
    return request.post<ApiResult<string>>(`/primary-chinese/${id}/download`)
  },

  /**
   * 一次性获取所有筛选项枚举
   */
  getFilterOptions() {
    return request.get<ApiResult<PrimaryChineseFilterOptions>>(
      '/primary-chinese/filter-options',
      { silentError: true },
    )
  },

  /** 上传位置联动筛选项（按学段/学科过滤册别与版本） */
  getUploadFilterOptions(params: {
    stage?: string
    subject?: string
    module?: string
  }, config?: SilentRequestConfig) {
    return request.get<ApiResult<UploadFilterOptions>>(
      '/primary-chinese/upload-filter-options',
      { params, silentError: true, ...config },
    )
  },

  /**
   * 按年级+版本获取单元列表（侧边树数据源）
   */
  getUnitNames(gradeName: string, edition: string) {
    return request.get<ApiResult<string[]>>('/primary-chinese/unit-names', {
      params: { gradeName, edition },
    })
  },

  /**
   * 单元树（单元 + 课文 subUnits）
   */
  getSuites(
    params?: Omit<PrimaryChineseParams, 'current' | 'size'>,
    config?: SilentRequestConfig,
  ) {
    return request.get<ApiResult<ResourceSuite[]>>('/primary-chinese/suites', {
      params,
      ...config,
    })
  },

  getModuleStats(params?: {
    stage?: string
    subject?: string
    gradeName?: string
    edition?: string
  }) {
    return request.get<ApiResult<{ module_name: string; resource_count: number }[]>>(
      '/primary-chinese/module-stats',
      { params },
    )
  },

  getUnitTree(params: {
    volumeKey?: string
    gradeName?: string
    edition?: string
    subject?: string
  }, config?: SilentRequestConfig) {
    return request.get<ApiResult<UnitTreeNode[]>>('/primary-chinese/unit-tree', {
      params,
      ...config,
    })
  },

  getDrafts() {
    return request.get<ApiResult<PrimaryChineseItem[]>>('/primary-chinese/drafts')
  },

  getDraft(id: number) {
    return request.get<ApiResult<PrimaryChineseItem>>(`/primary-chinese/draft/${id}`)
  },

  saveDraft(resource: Partial<PrimaryChineseItem> & { id?: number }) {
    return request.post<ApiResult<PrimaryChineseItem>>('/primary-chinese/draft/save', resource)
  },

  submitDraft(id: number) {
    return request.post<ApiResult<PrimaryChineseItem>>(
      `/primary-chinese/draft/${id}/submit`,
    )
  },

  /** 保存并提交审核（saveDraft + submitDraft 聚合） */
  submitDraftWithPayload(resource: Partial<PrimaryChineseItem> & { id?: number }) {
    return request.post<ApiResult<PrimaryChineseItem>>(
      '/primary-chinese/draft/submit',
      resource,
    )
  },

  deleteDraft(id: number) {
    return request.delete<ApiResult<void>>(`/primary-chinese/draft/${id}`)
  },

  /** 未通过资源复制为新草稿，供重新上传 */
  cloneRejectedToDraft(id: number) {
    return request.post<ApiResult<PrimaryChineseItem>>(`/primary-chinese/rejected/${id}/clone-draft`)
  },

  /** 撤回待审核资源（status: 0 → -1 草稿） */
  withdrawPending(id: number) {
    return request.post<ApiResult<PrimaryChineseItem>>(`/primary-chinese/pending/${id}/withdraw`)
  },

  /**
   * 新增教学资源（同步备课等，与列表查询字段一致）
   */
  save(
    resource: Partial<PrimaryChineseItem> & {
      status?: number
      remark?: string
      allowPreview?: number
    },
  ) {
    return request.post<ApiResult<PrimaryChineseItem>>('/primary-chinese/save', resource)
  },
}
