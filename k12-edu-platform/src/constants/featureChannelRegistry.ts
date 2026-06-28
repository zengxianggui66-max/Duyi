/** 特色频道统一注册表 — 导航、总览页、页脚共用 */

export type FeatureChannelGroup = 'moral' | 'culture' | 'academic'

export interface FeatureChannelMeta {
  /** 路由/导航 command */
  navCommand: string
  /** channelConfigs 键（FeatureChannel 通用页） */
  configKey: string
  name: string
  shortDesc: string
  intro: string
  icon: string
  color: string
  gradient: string
  path: string
  group: FeatureChannelGroup
  tier: 'flagship' | 'standard'
  tags: string[]
  resourceCount: string
  /** 用于资源列表识别来源频道 */
  tagKeywords: string[]
}

export const FEATURE_CHANNEL_GROUP_LABELS: Record<FeatureChannelGroup, { label: string; desc: string }> = {
  moral: { label: '德育与成长', desc: '班会育人 · 生涯引领' },
  culture: { label: '文化素养', desc: '国学经典 · 巴蜀研学' },
  academic: { label: '学业提升', desc: '竞赛冲刺 · 专题备考' },
}

export const FEATURE_CHANNEL_SPOTLIGHT = {
  title: '期中考试复习冲刺包',
  desc: '各学科期中复习资料汇总，含模拟试卷',
  path: '/topic-zone',
  query: { keyword: '期中复习' },
  emoji: '🎯',
}

export const FEATURE_CHANNELS: FeatureChannelMeta[] = [
  {
    navCommand: 'theme-meeting',
    configKey: 'banhui',
    name: '主题班会',
    shortDesc: '班会育人、安全教育、心理健康，全学段班主任必备',
    intro: '为班主任提供丰富的主题班会与育人资源，涵盖安全教育、心理健康、爱国教育、品德培养、家长会等，支持小学/初中/高中全学段一键筛选下载。',
    icon: '🎯',
    color: '#409eff',
    gradient: 'linear-gradient(135deg, #409eff, #2563eb)',
    path: '/theme-class-meeting',
    group: 'moral',
    tier: 'flagship',
    tags: ['主题班会', '班会育人', '家长会', '安全教育'],
    resourceCount: '2.5万+',
    tagKeywords: ['班会育人', '主题班会'],
  },
  {
    navCommand: 'shengya',
    configKey: 'shengya',
    name: '生涯规划',
    shortDesc: '升学指导、职业认知、选课与志愿填报',
    intro: '帮助学生认识自我、探索职业世界，提供从初中到高中全阶段的生涯规划资源，含选课指导、升学路径分析、职业认知体验等。',
    icon: '🧭',
    color: '#14b8a6',
    gradient: 'linear-gradient(135deg, #4facfe, #00f2fe)',
    path: '/feature/shengya',
    group: 'moral',
    tier: 'standard',
    tags: ['升学指导', '职业规划', '选课指导', '志愿填报'],
    resourceCount: '8600+',
    tagKeywords: ['生涯规划', '职业规划'],
  },
  {
    navCommand: 'chuantong',
    configKey: 'chuantong',
    name: '传统文化',
    shortDesc: '国学经典、诗词书法、巴蜀研学与民俗非遗',
    intro: '精选国学经典诵读材料、传统节日文化课件、诗词鉴赏教案、书法入门教程，将传统文化融入课堂教学。',
    icon: '🏮',
    color: '#d97706',
    gradient: 'linear-gradient(135deg, #f5af19, #f12711)',
    path: '/traditional-culture',
    group: 'culture',
    tier: 'flagship',
    tags: ['国学', '诗词', '成都研学', '民俗'],
    resourceCount: '2.5万+',
    tagKeywords: ['传统文化', '国学'],
  },
  {
    navCommand: 'jingsai',
    configKey: 'jingsai',
    name: '竞赛专区',
    shortDesc: '五大学科竞赛、奥数、作文与信息学备考',
    intro: '覆盖数学、物理、化学、生物、信息学五大学科竞赛，提供真题解析、模拟训练、竞赛辅导等全方位备考资源。',
    icon: '🏆',
    color: '#ea580c',
    gradient: 'linear-gradient(135deg, #F59E0B, #EF4444)',
    path: '/competition-zone',
    group: 'academic',
    tier: 'flagship',
    tags: ['数学竞赛', '物理竞赛', '信息学', '奥数'],
    resourceCount: '1.5万+',
    tagKeywords: ['学科竞赛', '竞赛'],
  },
  {
    navCommand: 'zhuanti',
    configKey: 'zhuanti',
    name: '专题资源',
    shortDesc: '成都·绵阳：寒暑假、期中期末、升学冲刺与 PBL',
    intro: '聚焦成都、绵阳片区教学日历，覆盖寒暑假作业、开学备考、期中期末、升学冲刺、时事热点与跨学科项目式学习。',
    icon: '📚',
    color: '#7c3aed',
    gradient: 'linear-gradient(135deg, #8B5CF6, #6D28D9)',
    path: '/topic',
    group: 'academic',
    tier: 'flagship',
    tags: ['暑假作业', '期中复习', '成都', '绵阳', '升学冲刺'],
    resourceCount: '680+',
    tagKeywords: ['寒暑假', '期中期末', '专题资源'],
  },
]

const GROUP_ORDER: FeatureChannelGroup[] = ['moral', 'culture', 'academic']

export function getFeatureChannelGroups() {
  return GROUP_ORDER.map((key) => ({
    key,
    ...FEATURE_CHANNEL_GROUP_LABELS[key],
    channels: FEATURE_CHANNELS.filter((c) => c.group === key),
  })).filter((g) => g.channels.length > 0)
}

export function findFeatureChannelByCommand(command: string): FeatureChannelMeta | undefined {
  return FEATURE_CHANNELS.find((c) => c.navCommand === command)
}

export function resolveFeatureChannelPath(command: string): string {
  if (command === 'overview') return '/feature'
  const ch = findFeatureChannelByCommand(command)
  if (ch) return ch.path
  return `/feature/${command}`
}

export function matchChannelNameByTags(tags: string): string {
  const t = tags || ''
  for (const ch of FEATURE_CHANNELS) {
    if (ch.tagKeywords.some((kw) => t.includes(kw))) return ch.name
  }
  return '特色频道'
}

export const FEATURE_HUB_STATS = {
  totalResources: '7.5万+',
  channelCount: String(FEATURE_CHANNELS.length),
  monthlyNew: '500+',
}
