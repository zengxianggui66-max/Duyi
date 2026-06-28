/**
 * 备课中心 / 资料篮 / 试题库 API
 */
import { request } from './request'
import type { ApiResult, PageData } from './request'

export type BasketItemType = 'resource' | 'question' | 'paper' | 'album'

export interface PrepBasketItem {
  id?: number
  basketId?: number
  itemType: BasketItemType
  refId: number
  title: string
  subtitle?: string
  coverUrl?: string
  metaJson?: string
  sortOrder?: number
  score?: number
  createTime?: string
}

export interface PrepBasketVO {
  basketId: number
  name: string
  items: PrepBasketItem[]
  totalCount: number
  resourceCount: number
  questionCount: number
  paperCount: number
  albumCount: number
}

export interface AddBasketItemPayload {
  itemType: BasketItemType
  refId: number
  title: string
  subtitle?: string
  coverUrl?: string
  metaJson?: string
  score?: number
}

export interface QuestionItem {
  id: number
  stem: string
  questionType: string
  optionsJson?: string
  answer?: string
  analysis?: string
  gradeLevel: string
  subject: string
  difficulty: number
  score: number
  knowledgePoints?: string
  sourceType?: string
  sourceName?: string
  region?: string
  year?: number
  usageCount?: number
}

export interface QuestionListParams {
  keyword?: string
  gradeLevel?: string
  subject?: string
  difficulty?: number
  questionType?: string
  sourceType?: string
  region?: string
  current?: number
  size?: number
}

export const prepApi = {
  getBasket() {
    return request.get<ApiResult<PrepBasketVO>>('/prep/basket')
  },

  addBasketItem(payload: AddBasketItemPayload) {
    return request.post<ApiResult<PrepBasketItem>>('/prep/basket/items', payload)
  },

  removeBasketItem(itemId: number) {
    return request.delete<ApiResult<null>>(`/prep/basket/items/${itemId}`)
  },

  clearBasket() {
    return request.delete<ApiResult<null>>('/prep/basket/clear')
  },

  reorderBasket(orderedItemIds: number[]) {
    return request.put<ApiResult<null>>('/prep/basket/reorder', orderedItemIds)
  },

  checkExists(itemType: BasketItemType, refId: number) {
    return request.get<ApiResult<{ exists: boolean }>>('/prep/basket/exists', {
      params: { itemType, refId },
    })
  },

  getQuestionList(params?: QuestionListParams) {
    return request.get<ApiResult<PageData<QuestionItem>>>('/prep/questions/list', { params })
  },

  getQuestionDetail(id: number) {
    return request.get<ApiResult<QuestionItem>>(`/prep/questions/detail/${id}`)
  },

  mergeBasket(items: AddBasketItemPayload[]) {
    return request.post<ApiResult<{ merged: number }>>('/prep/basket/merge', items)
  },

  previewExam(questionIds?: number[], title?: string) {
    if (questionIds?.length) {
      return request.post<ApiResult<ExamAssemblyVO>>('/prep/exam/preview', {
        questionIds,
        title,
      })
    }
    return request.get<ApiResult<ExamAssemblyVO>>('/prep/exam/preview')
  },

  assembleExam(payload: AssembleExamPayload) {
    return request.post<ApiResult<ExamAssemblyVO>>('/prep/exam/assemble', payload)
  },

  smartGenerateExam(payload: SmartExamPayload) {
    return request.post<ApiResult<ExamAssemblyVO>>('/prep/exam/smart-generate', payload)
  },

  listMyPapers() {
    return request.get<ApiResult<ExamPaperSummary[]>>('/prep/exam/papers')
  },

  getPaperDetail(id: number) {
    return request.get<ApiResult<ExamAssemblyVO>>(`/prep/exam/papers/${id}`)
  },

  exportWord(payload: AssembleExamPayload, answerOnly = false) {
    return request.post(`/prep/exam/export/word`, payload, {
      params: { answerOnly },
      responseType: 'blob',
    })
  },

  getBasketDownloadSummary() {
    return request.get<ApiResult<BasketDownloadSummary>>('/prep/basket/download-summary')
  },

  downloadBasketZip() {
    return request.get('/prep/basket/download-zip', { responseType: 'blob' })
  },
}

export interface BasketDownloadSummary {
  totalItems: number
  downloadableCount: number
  skippedCount: number
  skippedReasons?: string[]
}

export interface ExamAssemblyVO {
  paperId?: number
  title: string
  totalScore: number
  duration: number
  questionCount: number
  sections?: Record<string, unknown>[]
  previewHtml?: string
  answerHtml?: string
}

export interface ExamPaperSummary {
  id: number
  title: string
  gradeLevel: string
  subject: string
  totalScore: number
  duration: number
  questionCount: number
  createTime: string
}

export interface SmartExamPayload {
  title?: string
  gradeLevel: string
  subject: string
  difficulty?: number
  duration?: number
  useBasketQuestions?: boolean
  typeCounts?: { questionType: string; count: number }[]
}

export interface AssembleExamPayload {
  title?: string
  gradeLevel?: string
  subject?: string
  duration?: number
  difficulty?: number
  questionIds?: number[]
}
