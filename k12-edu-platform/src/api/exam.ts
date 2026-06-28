/**
 * 智能组卷相关 API
 */
import { request } from './request'
import type { ApiResult } from './request'

// ==================== 类型定义 ====================

export interface ExamGenerateParams {
  gradeLevel: string
  subject: string
  difficulty: number // 1-5
  questionTypes: string[]
  totalScore?: number
  questionCount?: number
}

export interface ExamHistoryItem {
  id: number
  title: string
  gradeLevel: string
  subject: string
  difficulty: number
  totalScore: number
  questionCount: number
  createdAt: string
}

export interface ExamDetail {
  id: number
  title: string
  content: string
  answer: string
  analysis: string
  gradeLevel: string
  subject: string
  difficulty: number
  totalScore: number
}

// ==================== API 方法 ====================

export const examApi = {
  /**
   * AI 生成试卷
   */
  generate(data: ExamGenerateParams) {
    return request.post<ApiResult<{ examId: number; content: string }>>(
      '/exam/generate',
      data
    )
  },

  /**
   * 组卷历史
   */
  getHistory() {
    return request.get<ApiResult<ExamHistoryItem[]>>('/exam/history')
  },

  /**
   * 试卷详情
   */
  getDetail(id: number) {
    return request.get<ApiResult<ExamDetail>>(`/exam/detail/${id}`)
  },
}
