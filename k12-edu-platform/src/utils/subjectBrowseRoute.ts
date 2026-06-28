import type { RouteLocationRaw } from 'vue-router'
import type { StageKey } from '@/config/subjectConfig'
import { DEFAULT_BRAND_CODE } from '@/config/resourceSeriesConfig'
import { volumeDataMap } from '@/config/volumeData'
import { normalizeVersionKeyForRoute } from '@/utils/editionAdapter'

export function normalizeSubjectKeyForBrowse(stage: StageKey, subjectKey: string): string {
  if (stage === 'art') return 'art'
  if (stage === 'dance') return 'dance'
  return subjectKey
}

/** 首页 / 学段专区 → 学科资源浏览页（七彩课堂 + 默认册别） */
export function buildSubjectBrowseRoute(opts: {
  stage: StageKey
  subjectKey: string
  versionKey: string
  query?: Record<string, string>
}): RouteLocationRaw {
  const query: Record<string, string> = {
    module: '同步备课',
    brand: DEFAULT_BRAND_CODE,
    ...opts.query,
  }
  if (!query.volume) {
    const defaultVolume = volumeDataMap[opts.stage]?.[0]?.id
    if (defaultVolume) query.volume = defaultVolume
  }
  return {
    name: 'SubjectDetail',
    params: {
      stage: opts.stage,
      subject: normalizeSubjectKeyForBrowse(opts.stage, opts.subjectKey),
      version: normalizeVersionKeyForRoute(opts.versionKey),
    },
    query,
  }
}
