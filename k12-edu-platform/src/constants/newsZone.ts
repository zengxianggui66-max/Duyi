/** 教育资讯频道配置 */

export interface NewsChannelDef {
  key: string
  name: string
  icon: string
  desc: string
  gradient: string
}

export const NEWS_CHANNELS: NewsChannelDef[] = [
  {
    key: 'policy',
    name: '教育政策',
    icon: '🏛️',
    desc: '国家与地方教育政策、招生规定解读',
    gradient: 'linear-gradient(135deg, #1e3a5f, #2563eb)',
  },
  {
    key: 'reform',
    name: '教学改革',
    icon: '🔄',
    desc: '新课标、课堂改革与教学创新实践',
    gradient: 'linear-gradient(135deg, #065f46, #10b981)',
  },
  {
    key: 'research',
    name: '教研动态',
    icon: '🔬',
    desc: '教研活动、课题成果与学科前沿',
    gradient: 'linear-gradient(135deg, #5b21b6, #8b5cf6)',
  },
  {
    key: 'teacher',
    name: '名师讲堂',
    icon: '👨‍🏫',
    desc: '名师课例、教学智慧与成长故事',
    gradient: 'linear-gradient(135deg, #9a3412, #f97316)',
  },
  {
    key: 'exam',
    name: '升学备考',
    icon: '🎓',
    desc: '中高考政策、备考策略与志愿填报',
    gradient: 'linear-gradient(135deg, #be123c, #f43f5e)',
  },
]

export const NEWS_CATEGORY_MAP: Record<string, string> = {
  policy: '教育政策',
  reform: '教学改革',
  research: '教研动态',
  teacher: '名师讲堂',
  exam: '升学备考',
}

export const GRADE_FILTER_OPTIONS = [
  { key: 'all', label: '全部学段' },
  { key: 'primary', label: '小学' },
  { key: 'junior', label: '初中' },
  { key: 'senior', label: '高中' },
]

export const REGION_FILTER_OPTIONS = [
  { key: 'all', label: '全国' },
  { key: 'sichuan', label: '四川' },
  { key: 'chengdu', label: '成都' },
  { key: 'mianyang', label: '绵阳' },
]

export const NEWS_CATEGORY_OPTIONS = NEWS_CHANNELS.map((c) => ({
  value: c.key,
  label: c.name,
  icon: c.icon,
}))

export const NEWS_LIST_CATEGORIES = [
  { key: 'all', name: '全部', icon: '📰' },
  ...NEWS_CHANNELS.map((c) => ({ key: c.key, name: c.name, icon: c.icon })),
]

export function getChannelByKey(key: string): NewsChannelDef | undefined {
  return NEWS_CHANNELS.find((c) => c.key === key)
}

export function getCategoryName(cat?: string): string {
  return NEWS_CATEGORY_MAP[cat || ''] || cat || '资讯'
}

export function formatNewsDate(item: { publishTime?: string; createTime?: string }) {
  const raw = item.publishTime || item.createTime || ''
  return raw ? String(raw).substring(0, 10) : ''
}

export function coverGradient(id: number): string {
  const colors = [
    'linear-gradient(135deg, #667eea, #764ba2)',
    'linear-gradient(135deg, #f093fb, #f5576c)',
    'linear-gradient(135deg, #4facfe, #00f2fe)',
    'linear-gradient(135deg, #43e97b, #38f9d7)',
    'linear-gradient(135deg, #fa709a, #fee140)',
    'linear-gradient(135deg, #a18cd1, #fbc2eb)',
  ]
  return colors[id % colors.length]
}
