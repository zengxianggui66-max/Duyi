/**
 * 国学阅读·作文深度融合 API
 * 基础路径：/api/sinology
 */
import { request } from './request'
import type { ApiResult } from './request'
import type {
  SinologyReadingItem,
  UnitBundleVO,
  SinologySchoolItem,
  SinologyFilterEnums,
} from './types'

// ==================== API 方法 ====================

export const sinologyApi = {
  /**
   * 核心接口：按单元获取完整包（国学阅读 + 作文训练 + 学校）
   * GET /api/sinology/unit-bundle?unitId=1
   */
  getUnitBundle(unitId: number) {
    return request.get<ApiResult<UnitBundleVO>>('/sinology/unit-bundle', {
      params: { unitId },
    })
  },

  /**
   * 多维度检索国学阅读素材
   * GET /api/sinology/search?gradeName=三年级&editionName=统编版（2024）&unitName=我上学了
   */
  search(params?: {
    gradeName?: string
    editionName?: string
    unitName?: string
    semesterName?: string
    keyword?: string
  }) {
    return request.get<ApiResult<SinologyReadingItem[]>>('/sinology/search', {
      params,
    })
  },

  /**
   * 国学阅读素材详情
   * GET /api/sinology/{id}
   */
  getDetail(id: number) {
    return request.get<ApiResult<SinologyReadingItem>>(`/sinology/${id}`)
  },

  /**
   * 获取筛选项枚举（版本、单元、学期）
   * GET /api/sinology/filter-enums
   */
  getFilterEnums() {
    return request.get<ApiResult<SinologyFilterEnums>>('/sinology/filter-enums')
  },

  /**
   * 查询学校列表
   * GET /api/sinology/schools?regionPath=成都
   */
  getSchools(params?: {
    regionId?: number
    regionPath?: string
    tag?: string
  }) {
    return request.get<ApiResult<SinologySchoolItem[]>>('/sinology/schools', {
      params,
    })
  },
}
