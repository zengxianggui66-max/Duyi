/** 主题班会专区 browse 模块名 */
export const THEME_CLASS_MEETING_MODULE = '主题班会'

export const CLASS_MEETING_GRADE_TABS = [
  { key: 'primary', name: '小学主题班会', gradeName: '小学' },
  { key: 'junior', name: '初中主题班会', gradeName: '初中' },
  { key: 'senior', name: '高中主题班会', gradeName: '高中' },
] as const

export const CLASS_MEETING_LATEST_TABS = [
  { key: 'courseware', name: '主题班会课件', displayType: '课件' },
  { key: 'lessonplan', name: '主题班会教案', displayType: '教案' },
  { key: 'material', name: '主题班会素材', displayType: '素材' },
] as const

export interface ClassMeetingCategoryConfig {
  sidebarTitle: string
  breadCategory: string
  subCategories: string[]
}

export const CLASS_MEETING_TOP_NAV = [
  { key: '爱国教育', label: '爱国教育' },
  { key: '安全教育', label: '安全教育' },
  { key: '德育教育', label: '德育教育' },
  { key: '环保教育', label: '环保教育' },
  { key: '心理健康', label: '心理健康' },
  { key: '励志教育', label: '励志教育' },
  { key: '节日主题', label: '节日主题' },
  { key: '更多主题', label: '更多主题' },
] as const

export const CLASS_MEETING_NAV_CONFIG: Record<string, ClassMeetingCategoryConfig> = {
  爱国教育: {
    sidebarTitle: '爱国教育',
    breadCategory: '爱国教育',
    subCategories: ['学雷锋', '爱国主义', '革命传统', '勿忘国耻'],
  },
  安全教育: {
    sidebarTitle: '安全教育',
    breadCategory: '安全教育',
    subCategories: ['用电安全', '食品安全', '消防安全', '交通安全', '校园安全', '网络安全', '生命健康', '游泳安全', '自然灾害', '疫情防控'],
  },
  德育教育: {
    sidebarTitle: '思想品德',
    breadCategory: '思想品德',
    subCategories: ['诚实守信', '文明礼仪', '养成好习惯', '人际交往', '遵纪守法', '团结友爱', '责任与担当', '传统美德'],
  },
  环保教育: {
    sidebarTitle: '环保教育',
    breadCategory: '环保教育',
    subCategories: ['绿色家园', '节能减排', '保护水资源', '变废为宝'],
  },
  心理健康: {
    sidebarTitle: '心理健康',
    breadCategory: '心理健康',
    subCategories: ['自我认识', '青春期', '轻松考试', '快乐生活', '情绪管理', '冲刺高考'],
  },
  励志教育: {
    sidebarTitle: '励志教育',
    breadCategory: '励志教育',
    subCategories: ['自信篇', '励志篇', '奋斗篇', '梦想篇'],
  },
  节日主题: {
    sidebarTitle: '节日主题',
    breadCategory: '节日主题',
    subCategories: ['元旦', '春节', '元宵节', '妇女节', '清明节', '劳动节', '五四青年节', '端午节', '母亲节', '父亲节', '儿童节', '教师节', '国庆节', '重阳节', '中秋节', '植树节', '圣诞节'],
  },
  更多主题: {
    sidebarTitle: '更多主题',
    breadCategory: '更多主题',
    subCategories: ['家校共育', '开学第一天', '期中考试家长会', '期末考试家长会', '班级文化', '学习方法', '珍惜时间', '其他'],
  },
}

export const THEME_TO_CATEGORY: Record<string, string> = {
  学雷锋: '爱国教育', 爱国主义: '爱国教育', 革命传统: '爱国教育', 勿忘国耻: '爱国教育',
  用电安全: '安全教育', 食品安全: '安全教育', 消防安全: '安全教育', 交通安全: '安全教育',
  校园安全: '安全教育', 网络安全: '安全教育', 生命健康: '安全教育', 游泳安全: '安全教育',
  自然灾害: '安全教育', 疫情防控: '安全教育',
  感恩父母: '德育教育', 感恩老师: '德育教育', 学会感恩: '德育教育',
  诚实守信: '德育教育', 文明礼仪: '德育教育', 人际交往: '德育教育',
  遵纪守法: '德育教育', 团结友爱: '德育教育', 责任与担当: '德育教育',
  养成好习惯: '德育教育', 传统美德: '德育教育',
  绿色家园: '环保教育', 节能减排: '环保教育', 节约用水: '环保教育', 保护水资源: '环保教育', 变废为宝: '环保教育',
  自我认识: '心理健康', 青春期: '心理健康', 轻松考试: '心理健康',
  快乐生活: '心理健康', 情绪管理: '心理健康', 冲刺高考: '心理健康',
  自信篇: '励志教育', 励志篇: '励志教育', 奋斗篇: '励志教育', 梦想篇: '励志教育',
  元旦: '节日主题', 春节: '节日主题', 元宵节: '节日主题', 妇女节: '节日主题',
  清明节: '节日主题', 劳动节: '节日主题', 五四青年节: '节日主题', 端午节: '节日主题',
  母亲节: '节日主题', 父亲节: '节日主题', 儿童节: '节日主题', 教师节: '节日主题',
  国庆节: '节日主题', 重阳节: '节日主题', 中秋节: '节日主题', 植树节: '节日主题', 圣诞节: '节日主题',
  家校共育: '更多主题', 开学第一天: '更多主题', 期中考试家长会: '更多主题',
  期末考试家长会: '更多主题', 班级文化: '更多主题', 学习方法: '更多主题',
  珍惜时间: '更多主题', 其他: '更多主题',
}

export const SIDEBAR_TO_CATEGORY: Record<string, string> = {
  爱国教育: '爱国教育',
  安全教育: '安全教育',
  感恩教育: '德育教育',
  心理健康: '心理健康',
  环保教育: '环保教育',
  思想品德: '德育教育',
  励志教育: '励志教育',
  家长会: '更多主题',
  节日主题: '节日主题',
  更多主题: '更多主题',
}

export const CLASS_MEETING_SIDEBAR = [
  { icon: '📌', name: '爱国教育' },
  { icon: '✅', name: '安全教育' },
  { icon: '💖', name: '感恩教育' },
  { icon: '🧠', name: '心理健康' },
  { icon: '🌱', name: '环保教育' },
  { icon: '📜', name: '思想品德' },
  { icon: '💪', name: '励志教育' },
  { icon: '👨‍👩‍👧‍👦', name: '家长会' },
  { icon: '🎊', name: '节日主题' },
  { icon: '…', name: '更多主题' },
]

export const CLASS_MEETING_THEME_GROUPS = [
  { title: '爱国教育', links: ['学雷锋', '爱国主义', '革命传统', '勿忘国耻'] },
  { title: '安全教育', links: ['用电安全', '食品安全', '消防安全', '交通安全', '校园安全', '网络安全', '生命健康', '游泳安全', '自然灾害', '疫情防控'] },
  { title: '感恩教育', links: ['感恩父母', '感恩老师', '学会感恩'] },
  { title: '心理健康', links: ['自我认识', '青春期', '轻松考试', '快乐生活', '情绪管理', '冲刺高考'] },
  { title: '环保教育', links: ['绿色家园', '节能减排', '保护水资源', '变废为宝'] },
  { title: '思想品德', links: ['诚实守信', '文明礼仪', '养成好习惯', '人际交往', '遵纪守法', '团结友爱', '责任与担当', '传统美德'] },
  { title: '励志教育', links: ['自信篇', '励志篇', '奋斗篇', '梦想篇'] },
  { title: '家长会', links: ['家校共育', '开学第一天', '期中考试家长会', '期末考试家长会'] },
  { title: '节日主题', links: ['元旦', '春节', '元宵节', '妇女节', '清明节', '劳动节', '五四青年节', '端午节', '母亲节', '父亲节', '儿童节', '教师节', '国庆节', '重阳节', '中秋节', '植树节', '圣诞节'] },
  { title: '更多主题', links: ['班级文化', '学习方法', '珍惜时间', '其他'] },
]

export const CLASS_MEETING_CATEGORY_ALIAS: Record<string, string> = {
  patriotic: '爱国教育',
  safety: '安全教育',
  morality: '德育教育',
  environmental: '环保教育',
  psychology: '心理健康',
  inspiration: '励志教育',
  festival: '节日主题',
  more: '更多主题',
}

export function resolveClassMeetingCategory(param: string) {
  return CLASS_MEETING_CATEGORY_ALIAS[param] || param
}
