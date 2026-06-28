/**
 * Phase 3L-β：banhui / zhuanti 与 canonical 页同源列表 API
 */
import { browseApi } from '@/api/browse'
import { resourceGateway } from '@/api/resourceGateway'
import { topicApi, type TopicResourceItem } from '@/api/topic'
import { unwrapData } from '@/api/request'
import type { PrimaryChineseItem } from '@/api/types'
import { THEME_CLASS_MEETING_MODULE } from '@/constants/themeClassMeetingNav'
import { TOPIC_FORMAT_API_MAP } from '@/constants/topicZone'

export const CHANNEL_BANHUI = 'banhui'
export const CHANNEL_ZHUANTI = 'zhuanti'

export function isCanonicalListChannel(code: string) {
  return code === CHANNEL_BANHUI || code === CHANNEL_ZHUANTI
}

export function resolveChannelTabKeyword(tabKey: string, tabKeywords: Record<string, string>) {
  if (!tabKey || tabKey === 'all' || tabKey === 'elite') return ''
  return tabKeywords[tabKey] || ''
}

const GRADE_NAME_MAP: Record<string, string> = {
  primary: '小学',
  junior: '初中',
  senior: '高中',
}

export interface ChannelListQuery {
  tabKey: string
  tabKeywords: Record<string, string>
  grade?: string
  subject?: string
  format?: string
  level?: string
  keyword?: string
  current?: number
  size?: number
  sortField?: string
  sortOrder?: 'desc' | 'asc'
}

export function buildBanhuiBrowseParams(opts: ChannelListQuery) {
  const tabKw = resolveChannelTabKeyword(opts.tabKey, opts.tabKeywords)
  const keyword = opts.keyword?.trim() || tabKw || undefined
  return {
    module: THEME_CLASS_MEETING_MODULE,
    keyword,
    gradeName: opts.grade && opts.grade !== 'all' ? GRADE_NAME_MAP[opts.grade] : undefined,
    current: opts.current ?? 1,
    size: opts.size ?? 12,
    sortField: opts.sortField,
    sortOrder: opts.sortOrder ?? 'desc',
  }
}

export function buildZhuantiTopicParams(opts: ChannelListQuery) {
  const category =
    opts.tabKey === 'all' || opts.tabKey === 'elite' ? undefined : opts.tabKey
  let keyword = opts.keyword?.trim()
  if (!keyword && opts.tabKey !== 'all' && opts.tabKeywords[opts.tabKey]) {
    keyword = opts.tabKeywords[opts.tabKey].split(/\s+/)[0]
  }
  let isFree: number | undefined
  if (opts.level === 'free') isFree = 1
  else if (opts.level === 'elite' || opts.level === 'vip') isFree = 0

  return {
    category,
    gradeStage: opts.grade && opts.grade !== 'all' ? opts.grade : undefined,
    subject: opts.subject && opts.subject !== 'all' ? opts.subject : undefined,
    resourceForm:
      opts.format && opts.format !== 'all'
        ? TOPIC_FORMAT_API_MAP[opts.format] || opts.format
        : undefined,
    isFree,
    keyword: keyword || undefined,
    current: opts.current ?? 1,
    size: opts.size ?? 12,
    sortField: opts.sortField ?? 'createTime',
    sortOrder: opts.sortOrder ?? 'desc',
  }
}

export async function fetchBanhuiResourcePage(params: ReturnType<typeof buildBanhuiBrowseParams>) {
  const res = await browseApi.getPage(params, { silentError: true })
  const page = unwrapData(res)
  return {
    records: (page.records || []) as PrimaryChineseItem[],
    total: page.total ?? 0,
  }
}

export async function fetchZhuantiResourcePage(params: ReturnType<typeof buildZhuantiTopicParams>) {
  const { page } = await resourceGateway.listTopicResources(params)
  return {
    records: page?.records || [],
    total: page?.total ?? 0,
  }
}

export async function fetchZhuantiSideLists(limit = 5) {
  const [hotRes, latestRes] = await Promise.all([
    topicApi.listHotResources({ limit }),
    topicApi.listLatestResources({ limit }),
  ])
  return {
    hot: hotRes.data?.data || [],
    latest: latestRes.data?.data || [],
  }
}

export function topicItemToPrimaryShape(item: TopicResourceItem): PrimaryChineseItem {
  return {
    id: item.id,
    title: item.title,
    type: item.resourceForm,
    displayType: item.resourceForm,
    module: '专题资源',
    subjectName: item.subject,
    gradeName: item.gradeStage,
    fileExt: item.fileFormat,
    downloadCount: item.downloadCount ?? 0,
    viewCount: item.viewCount ?? 0,
    isFree: item.isFree ?? 1,
    uploadTime: '',
  } as PrimaryChineseItem
}

export function resolveChannelDetailPath(channelCode: string, id: number) {
  if (channelCode === CHANNEL_ZHUANTI) return `/topic-zone/resource/${id}`
  return `/resource/${id}`
}
