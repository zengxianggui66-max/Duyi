/**
 * 智能备课相关 API
 */
import { request } from './request'
import type { ApiResult } from './request'

// ==================== 类型定义 ====================

export interface LessonGenerateParams {
  gradeLevel: string
  subject: string
  topic: string
  grade?: string
  version?: string
  types?: string[]
  basketResourceTitles?: string[]
  basketQuestionIds?: number[]
}

export interface LessonHistoryItem {
  id: number
  gradeLevel: string
  subject: string
  topic: string
  type: string
  content?: string
  createdAt: string
}

// ==================== API 方法 ====================

export const lessonApi = {
  /**
   * AI 生成备课
   */
  generate(data: LessonGenerateParams) {
    return request.post<ApiResult<{ content: string; lessonId: number }>>(
      '/lesson/generate',
      data
    )
  },

  /**
   * 备课历史
   */
  getHistory() {
    return request.get<ApiResult<LessonHistoryItem[]>>('/lesson/history')
  },

  /**
   * 获取备课详情
   */
  getDetail(id: number) {
    return request.get<ApiResult<LessonHistoryItem>>(`/lesson/detail/${id}`)
  },
}
