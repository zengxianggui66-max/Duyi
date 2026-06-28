import type { BannerViewModel, HotWordViewModel, NavTarget, QuickEntryViewModel } from '@/types/homeOps'

/** 静态兜底：与 sql/69 种子一致 */
export const STATIC_BANNERS: BannerViewModel[] = [
  {
    title: '开通即享全站资源',
    description: '海量优质资源，持续更新，下载无限制',
    cta: '立即开通',
    icon: '💎',
    color1: '#667EEA',
    color2: '#764BA2',
    navTarget: { type: 'vip' },
  },
  {
    title: '2024秋季同步备课精选',
    description: '汇聚优质教学资源，助力新学期教学',
    cta: '立即查看',
    icon: '📚',
    color1: '#FF6B6B',
    color2: '#FF8E53',
    navTarget: { type: 'route', routePath: '/lesson' },
  },
  {
    title: '生涯规划',
    description: '职业探索、志愿填报，助力学生规划未来',
    cta: '立即查看',
    icon: '🧭',
    color1: '#14B8A6',
    color2: '#0D9488',
    navTarget: { type: 'route', routePath: '/topic' },
  },
]

export const STATIC_QUICK_ENTRIES: QuickEntryViewModel[] = [
  {
    key: 'courseware',
    title: '课件资源',
    description: '海量课件、一键下载',
    icon: '📚',
    color: '#4facfe',
    navTarget: {
      type: 'browse',
      stageKey: 'primary',
      subjectKey: 'chinese',
      versionKey: 'tongbian2024',
      query: { module: '同步备课' },
    },
  },
  {
    key: 'prepare',
    title: '备课中心',
    description: '教案学案、同步资源',
    icon: '📋',
    color: '#43e97b',
    navTarget: { type: 'route', routePath: '/lesson/smart' },
  },
  {
    key: 'theme-meeting',
    title: '主题班会',
    description: '班会育人、心理健康',
    icon: '🎯',
    color: '#409eff',
    navTarget: { type: 'route', routePath: '/topic' },
  },
  {
    key: 'exam',
    title: '智能组卷',
    description: '智能组卷、精准出题',
    icon: '📝',
    color: '#f093fb',
    navTarget: { type: 'route', routePath: '/exam/smart' },
  },
  {
    key: 'resource',
    title: '教育资源',
    description: '全站资源、分类检索',
    icon: '📦',
    color: '#fa709a',
    navTarget: {
      type: 'browse',
      stageKey: 'primary',
      subjectKey: 'chinese',
      versionKey: 'tongbian2024',
      query: { module: '同步备课' },
    },
  },
  {
    key: 'career',
    title: '生涯规划',
    description: '职业探索、志愿填报',
    icon: '🧭',
    color: '#14b8a6',
    navTarget: { type: 'route', routePath: '/topic' },
  },
  {
    key: 'culture',
    title: '传统文化',
    description: '巴蜀研学·成都',
    icon: '🏮',
    color: '#d97706',
    navTarget: { type: 'route', routePath: '/culture' },
  },
  {
    key: 'competition',
    title: '竞赛专区',
    description: '学科竞赛、奥数',
    icon: '🏅',
    color: '#f39c12',
    navTarget: { type: 'route', routePath: '/competition' },
  },
  {
    key: 'topic',
    title: '专题资源',
    description: '寒暑假·升学·成都绵阳',
    icon: '📚',
    color: '#8b5cf6',
    navTarget: { type: 'route', routePath: '/topic' },
  },
]

export const STATIC_HOT_WORDS: HotWordViewModel[] = [
  {
    label: '一年级语文',
    actionType: 'browse',
    navTarget: {
      type: 'browse',
      stageKey: 'primary',
      subjectKey: 'chinese',
      versionKey: 'tongbian2024',
      volumeName: '一年级上册',
      query: { module: '同步备课' },
    },
  },
  {
    label: '期中试卷',
    actionType: 'browse',
    navTarget: {
      type: 'browse',
      stageKey: 'primary',
      subjectKey: 'chinese',
      versionKey: 'tongbian2024',
      volumeName: '一年级上册',
      query: { module: '期中' },
    },
  },
  {
    label: '教案模板',
    actionType: 'search',
    navTarget: {
      type: 'search',
      stageKey: 'primary',
      subjectKey: 'chinese',
      versionKey: 'tongbian2024',
      volumeName: '一年级上册',
      keyword: '教案',
      searchEngine: 'mysql',
      query: { module: '同步备课' },
    },
  },
  {
    label: '中考复习',
    actionType: 'browse',
    navTarget: {
      type: 'browse',
      stageKey: 'junior',
      subjectKey: 'chinese',
      versionKey: 'tongbian2024',
      query: { module: '一轮复习' },
    },
  },
]

export function navTargetFromLegacyPath(path: string): NavTarget {
  return { type: 'route', routePath: path }
}
