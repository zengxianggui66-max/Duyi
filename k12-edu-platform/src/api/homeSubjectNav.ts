/**
 * Phase 5-F: home subject detail overlay API
 */
import { request, unwrapData } from './request'
import type { ApiResult } from './request'

export interface HomeSubjectNavSubject {
  id?: number
  code: string
  name: string
  icon?: string
  stageCode?: string
  stageName?: string
}

export interface HomeSubjectNavEditionItem {
  id?: number
  code?: string
  name: string
  isNew?: boolean
  sort?: number
}

export interface HomeSubjectNavResourceTypeItem {
  id?: number
  code?: string
  name: string
  sort?: number
}

export interface HomeSubjectNavModuleItem {
  id?: number
  code?: string
  name: string
  icon?: string
  sort?: number
}

export interface HomeSubjectNavModuleSection {
  label?: string
  title?: string
  modules: HomeSubjectNavModuleItem[]
}

export interface HomeSubjectNavPayload {
  subject?: HomeSubjectNavSubject
  syncPrep?: {
    editions?: HomeSubjectNavEditionItem[]
    resourceTypes?: HomeSubjectNavResourceTypeItem[]
  }
  reviewPrep?: HomeSubjectNavModuleSection
  promotionPrep?: HomeSubjectNavModuleSection
}

export const homeSubjectNavApi = {
  getNav(stage: string, subject: string) {
    return request
      .get<ApiResult<HomeSubjectNavPayload>>('/home/subject-nav', {
        params: { stage, subject },
        silentError: true,
      })
      .then(unwrapData)
  },
}
