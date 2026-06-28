import type { RouteLocationRaw } from 'vue-router'
import type { StageKey } from '@/config/subjectConfig'
import { volumeDataMap } from '@/config/volumeData'
import {
  getHomeFuncChannel,
  type HomeFuncChannel,
  type HomeFuncKey,
} from '@/constants/homeFuncChannels'
import { buildSubjectBrowseRoute } from '@/utils/subjectBrowseRoute'
import { normalizeVersionKeyForRoute } from '@/utils/editionAdapter'
import { resolvePromotionFuncKey } from '@/utils/promotionRoute'
import { resolvePromotionTopicBrowse } from '@/constants/promotionTopicBrowse'

export interface FuncBrowseContext {
  /** 首页当前学段；未传则用入口配置学段 */
  stage?: StageKey
  /** 首页当前学科 key；未传则默认 chinese */
  subjectKey?: string
  /** 首页当前教材版本 key；未传则 tongbian2024 */
  versionKey?: string
  /** 升学专区侧栏专题（如 拼音识字）；未传则用入口 defaultTopic */
  topic?: string
}

/** 册别展示名 → volumeDataMap.id */
export function resolveVolumeIdByName(stage: StageKey, volumeName?: string): string | undefined {
  const list = volumeDataMap[stage] || []
  if (!volumeName || !list.length) return list[0]?.id

  const exact = list.find((v) => v.name === volumeName)
  if (exact) return exact.id

  const gradePrefix = volumeName.replace(/[上下]册$/, '')
  const partial = list.find((v) => v.name.startsWith(gradePrefix))
  if (partial) return partial.id

  return undefined
}

export function resolveFuncKey(input: string): HomeFuncKey | null {
  const fromPromotion = resolvePromotionFuncKey(input)
  if (fromPromotion) return fromPromotion
  return null
}

/**
 * 顶栏升学入口 → 学科资源浏览页（栏目 module 对齐 SubjectDetailPage）
 */
export function buildFuncBrowseRoute(
  funcKey: string,
  ctx: FuncBrowseContext = {},
  channelOverride?: HomeFuncChannel,
): RouteLocationRaw | null {
  const key = resolveFuncKey(funcKey)
  if (!key) return null

  const channel = channelOverride ?? getHomeFuncChannel(key)
  const topic = ctx.topic?.trim() || channel.defaultTopic
  const topicTarget = resolvePromotionTopicBrowse(channel.examType, topic)

  const stage = topicTarget?.stageKey ?? channel.browseStageKey ?? channel.stageKey
  const subjectKey = topicTarget?.subjectKey ?? ctx.subjectKey ?? 'chinese'
  const versionKey = normalizeVersionKeyForRoute(
    ctx.versionKey || topicTarget?.versionKey || 'tongbian2024',
  )

  const query: Record<string, string> = {
    module: topicTarget?.browseModule ?? channel.browseModule,
  }

  if (topicTarget?.keyword) {
    query.keyword = topicTarget.keyword
  }

  const volumeName = topicTarget?.volumeName ?? channel.browseDefaultVolume
  const volumeId = resolveVolumeIdByName(stage, volumeName)
  if (volumeId) {
    query.volume = volumeId
  }

  return buildSubjectBrowseRoute({
    stage,
    subjectKey,
    versionKey,
    query,
  })
}

export function getFuncBrowseMeta(funcKey: string): Pick<
  HomeFuncChannel,
  'browseModule' | 'browseStageKey' | 'browseDefaultVolume'
> | null {
  const key = resolveFuncKey(funcKey)
  if (!key) return null
  const ch = getHomeFuncChannel(key)
  return {
    browseModule: ch.browseModule,
    browseStageKey: ch.browseStageKey,
    browseDefaultVolume: ch.browseDefaultVolume,
  }
}
