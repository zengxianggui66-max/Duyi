/**
 * P0 全站搜索静态目录（与后端 SearchSiteCatalog 对齐）
 * 后端不可用时作为 suggest / intents / redirect 兜底
 */
import type { SearchSuggestItem } from '@/api/search'

interface CatalogEntry {
  text: string
  kind: SearchSuggestItem['kind']
  subtitle?: string
  detailRoute?: string
  routeQuery?: Record<string, string>
  contentDomain?: SearchSuggestItem['contentDomain']
  aliases?: string[]
}

const SUBJECT_ENTRIES: CatalogEntry[] = [
  { text: '小学语文', kind: 'subject', subtitle: '小学 / 语文 / 同步资源', detailRoute: '/subject/primary/chinese/tongbian2024', contentDomain: 'stage_resource', aliases: ['语文', '小学'] },
  { text: '初中语文', kind: 'subject', subtitle: '初中 / 语文 / 同步资源', detailRoute: '/subject/junior/chinese/tongbian2024', contentDomain: 'stage_resource', aliases: ['语文', '初中'] },
  { text: '高中语文', kind: 'subject', subtitle: '高中 / 语文 / 同步资源', detailRoute: '/subject/senior/chinese/tongbian2024', contentDomain: 'stage_resource', aliases: ['语文', '高中'] },
  { text: '小学数学', kind: 'subject', subtitle: '小学 / 数学 / 同步资源', detailRoute: '/subject/primary/math/tongbian2024', contentDomain: 'stage_resource', aliases: ['数学'] },
  { text: '初中数学', kind: 'subject', subtitle: '初中 / 数学 / 同步资源', detailRoute: '/subject/junior/math/tongbian2024', contentDomain: 'stage_resource', aliases: ['数学'] },
  { text: '高中数学', kind: 'subject', subtitle: '高中 / 数学 / 同步资源', detailRoute: '/subject/senior/math/tongbian2024', contentDomain: 'stage_resource', aliases: ['数学'] },
]

const FEATURE_ENTRIES: CatalogEntry[] = [
  { text: '主题班会', kind: 'channel', subtitle: '主题班会 / 德育', detailRoute: '/theme-class-meeting', contentDomain: 'feature', aliases: ['班会', '德育'] },
  { text: '生涯规划', kind: 'feature', subtitle: '生涯规划 / 升学指导', detailRoute: '/feature/shengya', contentDomain: 'feature', aliases: ['生涯', '升学', '选科'] },
  { text: '传统文化', kind: 'feature', subtitle: '传统文化 / 国学', detailRoute: '/culture', contentDomain: 'feature', aliases: ['国学', '诗词'] },
  { text: '竞赛专区', kind: 'feature', subtitle: '竞赛专区 / 奥赛', detailRoute: '/competition', contentDomain: 'feature', aliases: ['竞赛', '奥赛'] },
]

const PREP_ENTRIES: CatalogEntry[] = [
  { text: '备课中心', kind: 'prep', subtitle: '教案 / 课件 / 学案', detailRoute: '/lesson', contentDomain: 'prep', aliases: ['备课', '教案', '课件'] },
  { text: '智能备课', kind: 'prep', subtitle: 'AI 辅助备课', detailRoute: '/lesson/smart', contentDomain: 'prep', aliases: ['智能备课'] },
  { text: '组卷', kind: 'prep', subtitle: '试卷 / 组卷', detailRoute: '/lesson/assemble', contentDomain: 'prep', aliases: ['组卷', '试卷'] },
  { text: '试卷列表', kind: 'prep', subtitle: '备课专区 / 试卷', detailRoute: '/lesson/papers', contentDomain: 'prep', aliases: ['我的试卷'] },
  { text: '资料篮', kind: 'prep', subtitle: '备课专区 / 资源篮', detailRoute: '/lesson/basket', contentDomain: 'prep', aliases: ['资源篮'] },
]

const NEWS_ENTRIES: CatalogEntry[] = [
  { text: '教育局通知', kind: 'news', subtitle: '教育资讯 / 通知', detailRoute: '/news/list?keyword=通知', contentDomain: 'news', aliases: ['通知', '教育局'] },
  { text: '政策资讯', kind: 'news', subtitle: '教育资讯 / 政策', detailRoute: '/news/channel/policy', contentDomain: 'news', aliases: ['政策', '资讯'] },
  { text: '教育政策', kind: 'news', subtitle: '教育资讯 / 政策', detailRoute: '/news/channel/policy', contentDomain: 'news', aliases: ['教育政策'] },
  { text: '教研动态', kind: 'news', subtitle: '教育资讯 / 教研', detailRoute: '/news/channel/research', contentDomain: 'news', aliases: ['教研'] },
  { text: '升学备考', kind: 'news', subtitle: '教育资讯 / 升学', detailRoute: '/news/channel/exam', contentDomain: 'news', aliases: ['升学', '中高考'] },
  { text: '名师讲堂', kind: 'news', subtitle: '教育资讯 / 名师', detailRoute: '/news/channel/teacher', contentDomain: 'news', aliases: ['名师'] },
  { text: '教学改革', kind: 'news', subtitle: '教育资讯 / 改革', detailRoute: '/news/channel/reform', contentDomain: 'news', aliases: ['新课标'] },
]

const RESOURCE_TYPE_ENTRIES: CatalogEntry[] = [
  { text: '课件', kind: 'resourceType', subtitle: 'PPT / 课件资源', detailRoute: '/search/result?q=课件&type=课件', contentDomain: 'stage_resource', aliases: ['ppt'] },
  { text: '教案', kind: 'resourceType', subtitle: '教学设计 / 教案', detailRoute: '/search/result?q=教案&type=教案', contentDomain: 'stage_resource', aliases: ['教学设计'] },
  { text: '试卷', kind: 'resourceType', subtitle: '试卷 / 真题 / 模拟', detailRoute: '/search/result?q=试卷&type=试卷', contentDomain: 'stage_resource', aliases: ['真题', '模拟卷'] },
]

const ALL_ENTRIES = [
  ...SUBJECT_ENTRIES,
  ...FEATURE_ENTRIES,
  ...PREP_ENTRIES,
  ...NEWS_ENTRIES,
  ...RESOURCE_TYPE_ENTRIES,
]

function toSuggest(entry: CatalogEntry): SearchSuggestItem {
  return {
    text: entry.text,
    kind: entry.kind,
    subtitle: entry.subtitle,
    detailRoute: entry.detailRoute,
    routeQuery: entry.routeQuery,
    contentDomain: entry.contentDomain,
  }
}

function matched(keyword: string, entry: CatalogEntry): boolean {
  const kw = keyword.trim().toLowerCase()
  if (!kw) return false
  const text = entry.text.toLowerCase()
  if (text.includes(kw) || kw.includes(text)) return true
  return (entry.aliases || []).some((alias) => {
    const a = alias.toLowerCase()
    return kw.includes(a) || a.includes(kw) || text.includes(kw)
  })
}

/** 学科意图：语文 → 小/初/高语文 */
export function matchSubjectIntents(keyword: string, limit = 3): SearchSuggestItem[] {
  const kw = keyword.trim()
  if (!kw) return []
  const subjects = ['语文', '数学', '英语', '物理', '化学', '生物']
  const hitSubject = subjects.find((s) => kw.includes(s))
  if (!hitSubject) {
    return SUBJECT_ENTRIES.filter((e) => matched(kw, e)).slice(0, limit).map(toSuggest)
  }
  return SUBJECT_ENTRIES.filter((e) => e.text.includes(hitSubject)).slice(0, limit).map(toSuggest)
}

/** 联想下拉：合并后端结果与本地目录 */
export function matchCatalogSuggestions(keyword: string, limit = 12): SearchSuggestItem[] {
  const kw = keyword.trim()
  if (!kw) return []
  const out: SearchSuggestItem[] = []
  const seen = new Set<string>()
  const groups = [
    matchSubjectIntents(kw, 3),
    FEATURE_ENTRIES.filter((e) => matched(kw, e)).map(toSuggest),
    PREP_ENTRIES.filter((e) => matched(kw, e)).map(toSuggest),
    NEWS_ENTRIES.filter((e) => matched(kw, e)).map(toSuggest),
    RESOURCE_TYPE_ENTRIES.filter((e) => matched(kw, e)).map(toSuggest),
  ]
  for (const group of groups) {
    for (const item of group) {
      const key = item.text.toLowerCase()
      if (seen.has(key) || out.length >= limit) continue
      seen.add(key)
      out.push(item)
    }
  }
  return out.slice(0, limit)
}

/** 精确热词直达 */
export function findExactCatalogEntry(keyword: string): SearchSuggestItem | null {
  const kw = keyword.trim()
  if (!kw) return null
  const hit = ALL_ENTRIES.find((e) => e.text === kw)
  return hit ? toSuggest(hit) : null
}

/** 零结果推荐：按关键词优先展示相关入口 */
export function buildZeroEntries(keyword: string) {
  const matchedItems = matchCatalogSuggestions(keyword, 6)
  if (matchedItems.length) {
    return matchedItems.map((item) => ({
      label: item.contentDomain === 'feature' ? '特色频道'
        : item.contentDomain === 'prep' ? '备课专区'
        : item.contentDomain === 'news' ? '教育资讯'
        : '学段资源',
      text: item.text,
      detailRoute: item.detailRoute || `/search/result?q=${encodeURIComponent(item.text)}`,
    }))
  }
  return [
    { label: '学段资源', text: '小学语文', detailRoute: '/subject/primary/chinese/tongbian2024' },
    { label: '特色频道', text: '主题班会', detailRoute: '/theme-class-meeting' },
    { label: '备课专区', text: '备课中心', detailRoute: '/lesson' },
    { label: '教育资讯', text: '政策资讯', detailRoute: '/news/list?keyword=政策' },
  ]
}

/** 合并后端 suggest，本地目录优先补全 P0 项 */
export function mergeSuggestItems(remote: SearchSuggestItem[], keyword: string, limit = 12): SearchSuggestItem[] {
  const local = matchCatalogSuggestions(keyword, limit)
  const seen = new Set<string>()
  const out: SearchSuggestItem[] = []
  for (const item of [...local, ...remote]) {
    const key = `${item.kind}-${item.text}`.toLowerCase()
    if (seen.has(key) || out.length >= limit) continue
    if (!item.detailRoute && !item.routeQuery) continue
    seen.add(key)
    out.push(item)
  }
  return out
}
