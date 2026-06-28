/**
 * 首页三大专区 API
 */
import { request } from './request'
import type { ApiResult } from './request'

export interface HomePanelItem {
  id?: number
  title: string
  date?: string
  source?: string
  detailPath?: string
}

export interface HomePanelListResult {
  items: HomePanelItem[]
  limit: number
}

export interface HomeFuncChannelDto {
  key: string
  name: string
  examType: string
  defaultTopic: string
  stageKey: string
  paperTab: string
  paperDefaultGrade: string
  scrollTarget: string
  browseModule?: string
  browseStageKey?: string
  browseDefaultVolume?: string
  topics: string[]
}

export interface PromotionExamTabDto {
  key: string
  label: string
}

export interface HomeFuncChannelsResult {
  channels: HomeFuncChannelDto[]
  promotionExamTabs: PromotionExamTabDto[]
  promotionTopicsMap: Record<string, string[]>
}

const HOME_LIMIT = 18

export const homePanelApi = {
  getFuncChannels() {
    return request.get<ApiResult<HomeFuncChannelsResult>>('/home/func-channels')
  },

  getSyncPrep(params: {
    stageKey: string
    subjectName?: string
    tabKey: string
    limit?: number
  }) {
    return request.get<ApiResult<HomePanelListResult>>('/home/panels/sync-prep', {
      params: { ...params, limit: params.limit ?? HOME_LIMIT },
    })
  },

  getPaperZone(params: {
    stageKey: string
    gradeName?: string
    tabKey: string
    limit?: number
  }) {
    return request.get<ApiResult<HomePanelListResult>>('/home/panels/paper-zone', {
      params: { ...params, limit: params.limit ?? HOME_LIMIT },
    })
  },

  getPromotion(params: {
    examType: string
    topic?: string
    limit?: number
  }) {
    return request.get<ApiResult<HomePanelListResult>>('/home/panels/promotion', {
      params: { ...params, limit: params.limit ?? HOME_LIMIT },
    })
  },
}

export { HOME_LIMIT }
