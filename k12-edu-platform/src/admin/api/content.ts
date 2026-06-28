import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export interface ContentQuery {
  keyword?: string
  category?: string
  region?: string
  gradeStage?: string
  status?: number
  includeDisabled?: boolean
  current?: number
  size?: number
}

export interface ContentPackageItem {
  id: number
  title: string
  summary?: string
  category?: string
  region?: string
  gradeStage?: string
  durationType?: string
  durationLabel?: string
  suitableAudience?: string
  location?: string
  coverUrl?: string
  icon?: string
  tags?: string
  resourceCount?: number
  downloadCount?: number
  isFree?: number
  isElite?: number
  status?: number
  sort?: number
}

export interface ContentPackageWrite {
  title: string
  summary?: string
  category?: string
  region?: string
  gradeStage?: string
  durationType?: string
  durationLabel?: string
  suitableAudience?: string
  location?: string
  coverUrl?: string
  icon?: string
  tags?: string
  isFree?: number
  isElite?: number
  sort?: number
  status?: number
}

export interface AdminArticleItem {
  id: number
  title: string
  summary?: string
  content?: string
  coverUrl?: string
  category?: string
  categoryName?: string
  author?: string
  status?: number
  viewCount?: number
  publishTime?: string
  tags?: string
  isTop?: number
  topOrder?: number
  isFeatured?: number
}

export interface ArticleQuery {
  keyword?: string
  category?: string
  status?: number
  current?: number
  size?: number
}

export interface ArticleWrite {
  title: string
  summary?: string
  content?: string
  coverUrl?: string
  category: string
  author?: string
  tags?: string
  gradeLevels?: string
  regions?: string
  status?: number
  isTop?: number
  topOrder?: number
  isFeatured?: number
}

const topicBase = '/admin/topic'
const cultureBase = '/admin/culture'
const competitionBase = '/admin/competition'
const articlesBase = '/admin/articles'

function pageGet<T>(path: string, params?: ContentQuery | ArticleQuery) {
  return request
    .get<ApiResult<PageResult<T>>>(path, { params: params as Record<string, unknown> })
    .then(unwrapData)
}

export const adminContentApi = {
  listTopicAlbums: (params?: ContentQuery) => pageGet<ContentPackageItem>(`${topicBase}/albums`, params),
  createTopicAlbum: (body: ContentPackageWrite) =>
    request.post<ApiResult<ContentPackageItem>>(`${topicBase}/albums`, body).then(unwrapData),
  updateTopicAlbum: (id: number, body: ContentPackageWrite) =>
    request.put<ApiResult<ContentPackageItem>>(`${topicBase}/albums/${id}`, body).then(unwrapData),
  setTopicAlbumStatus: (id: number, status: number) =>
    request.put<ApiResult<void>>(`${topicBase}/albums/${id}/status`, null, { params: { status } }).then(() => undefined),
  deleteTopicAlbum: (id: number) =>
    request.delete<ApiResult<void>>(`${topicBase}/albums/${id}`).then(() => undefined),

  listCulturePackages: (params?: ContentQuery) => pageGet<ContentPackageItem>(`${cultureBase}/packages`, params),
  createCulturePackage: (body: ContentPackageWrite) =>
    request.post<ApiResult<ContentPackageItem>>(`${cultureBase}/packages`, body).then(unwrapData),
  updateCulturePackage: (id: number, body: ContentPackageWrite) =>
    request.put<ApiResult<ContentPackageItem>>(`${cultureBase}/packages/${id}`, body).then(unwrapData),
  setCulturePackageStatus: (id: number, status: number) =>
    request.put<ApiResult<void>>(`${cultureBase}/packages/${id}/status`, null, { params: { status } }).then(() => undefined),
  deleteCulturePackage: (id: number) =>
    request.delete<ApiResult<void>>(`${cultureBase}/packages/${id}`).then(() => undefined),

  listCompetitionPackages: (params?: ContentQuery) =>
    pageGet<ContentPackageItem>(`${competitionBase}/packages`, params),
  createCompetitionPackage: (body: ContentPackageWrite) =>
    request.post<ApiResult<ContentPackageItem>>(`${competitionBase}/packages`, body).then(unwrapData),
  updateCompetitionPackage: (id: number, body: ContentPackageWrite) =>
    request.put<ApiResult<ContentPackageItem>>(`${competitionBase}/packages/${id}`, body).then(unwrapData),
  setCompetitionPackageStatus: (id: number, status: number) =>
    request.put<ApiResult<void>>(`${competitionBase}/packages/${id}/status`, null, { params: { status } }).then(() => undefined),
  deleteCompetitionPackage: (id: number) =>
    request.delete<ApiResult<void>>(`${competitionBase}/packages/${id}`).then(() => undefined),

  listArticles: (params?: ArticleQuery) => pageGet<AdminArticleItem>(articlesBase, params),
  getArticle: (id: number) =>
    request.get<ApiResult<AdminArticleItem>>(`${articlesBase}/${id}`).then(unwrapData),
  createArticle: (body: ArticleWrite) =>
    request.post<ApiResult<number>>(articlesBase, body).then(unwrapData),
  updateArticle: (id: number, body: ArticleWrite) =>
    request.put<ApiResult<void>>(`${articlesBase}/${id}`, body).then(unwrapData),
  setArticleStatus: (id: number, status: number) =>
    request.put<ApiResult<void>>(`${articlesBase}/${id}/status`, null, { params: { status } }).then(() => undefined),
  deleteArticle: (id: number) =>
    request.delete<ApiResult<void>>(`${articlesBase}/${id}`).then(() => undefined),
}
