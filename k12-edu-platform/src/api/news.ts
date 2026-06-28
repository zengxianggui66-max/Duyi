/**
 * 教育资讯 API（P0–P2）
 */
import { request } from './request'
import type { ApiResult, PageData } from './request'
import type {
  NewsItem,
  NewsListParams,
  NewsHomeVO,
  NewsDetailVO,
  ConsultLeadPayload,
  ArticleCreatePayload,
} from './types'

export type { NewsItem, NewsListParams, NewsHomeVO, NewsDetailVO, ConsultLeadPayload }

export const newsApi = {
  getHome() {
    return request.get<ApiResult<NewsHomeVO>>('/news/home')
  },

  getList(params?: NewsListParams) {
    return request.get<ApiResult<PageData<NewsItem>>>('/news/list', { params })
  },

  search(keyword: string, current = 1, size = 10) {
    return request.get<ApiResult<PageData<NewsItem>>>('/news/search', {
      params: { keyword, current, size },
    })
  },

  getHotKeywords() {
    return request.get<ApiResult<string[]>>('/news/hot-keywords')
  },

  getDetail(id: number) {
    return request.get<ApiResult<NewsDetailVO>>(`/news/detail/${id}`)
  },

  collect(id: number) {
    return request.post<ApiResult<null>>(`/news/${id}/collect`)
  },

  uncollect(id: number) {
    return request.delete<ApiResult<null>>(`/news/${id}/collect`)
  },

  isCollected(id: number) {
    return request.get<ApiResult<{ collected: boolean }>>(`/news/${id}/collected`)
  },

  submitConsult(payload: ConsultLeadPayload) {
    return request.post<ApiResult<null>>('/news/consult/lead', payload)
  },

  createArticle(payload: ArticleCreatePayload) {
    return request.post<ApiResult<number>>('/news', payload)
  },
}
