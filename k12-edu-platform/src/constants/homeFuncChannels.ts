import type { StageKey } from '@/config/subjectConfig'

/**
 * 首页顶栏功能入口本地兜底配置。
 * 运行时优先使用 GET /api/home/func-channels 返回的数据（见 useHomeFuncChannels）。
 */

/** 首页顶栏功能入口 key */
export type HomeFuncKey = 'youxiao' | 'xiaoshengchu' | 'zhongkao' | 'gaokao' | 'duikou'

/** 升学专区后端 examType */
export type PromotionExamType =
  | 'kindergarten_bridge'
  | 'primary_promo'
  | 'middle'
  | 'high'
  | 'vocational_promo'
  | string

export interface HomeFuncChannel {
  key: HomeFuncKey
  name: string
  examType: PromotionExamType
  defaultTopic: string
  stageKey: StageKey
  /** 试卷专区 Tab（entrance=升学） */
  paperTab: string
  /** 试卷专区默认年级册别 */
  paperDefaultGrade: string
  scrollTarget: 'exam-module'
  /** 资源浏览页栏目（edu_module.name） */
  browseModule: string
  /** 资源浏览页学段（可与 stageKey 相同） */
  browseStageKey: StageKey
  /** 资源浏览页默认册别展示名，如「六年级下册」 */
  browseDefaultVolume: string
}

/** 升学专区各 examType 侧栏专题（与 home_panel_tab_config.filter_key 对齐） */
export const PROMOTION_TOPICS_MAP: Record<string, string[]> = {
  kindergarten_bridge: ['拼音识字', '数学启蒙', '习惯养成', '暑假衔接'],
  primary_promo: [
    '真题', '模拟试卷', '名校招生', '专项训练',
    '真题汇编', '重点校卷', '分班考试', '暑假衔接',
  ],
  middle: [
    '一模', '二模', '真题', '一轮复习',
    '二轮专题', '三轮冲刺', '模拟试卷', '真题汇编', '中考作文',
  ],
  high: [
    '一模', '二模', '三模', '真题',
    '一轮复习', '二轮专题', '三轮冲刺', '模拟试卷', '真题汇编', '高考作文',
  ],
  vocational_promo: ['政策解读', '真题资料', '专业对口', '模拟试卷'],
}

/** 升学专区顶栏 Tab（与首页 func-bar 对齐） */
export const PROMOTION_EXAM_TYPE_TABS = [
  { key: 'kindergarten_bridge', label: '幼小衔接' },
  { key: 'primary_promo', label: '小升初' },
  { key: 'middle', label: '中考' },
  { key: 'high', label: '高考' },
  { key: 'vocational_promo', label: '对口升学' },
] as const

export const HOME_FUNC_CHANNELS: Record<HomeFuncKey, HomeFuncChannel> = {
  youxiao: {
    key: 'youxiao',
    name: '幼小衔接',
    examType: 'kindergarten_bridge',
    defaultTopic: '拼音识字',
    stageKey: 'preschool',
    paperTab: 'opening',
    paperDefaultGrade: '大班下学期',
    scrollTarget: 'exam-module',
    browseModule: '拼音识字',
    browseStageKey: 'preschool',
    browseDefaultVolume: '大班下学期',
  },
  xiaoshengchu: {
    key: 'xiaoshengchu',
    name: '小升初',
    examType: 'primary_promo',
    defaultTopic: '真题',
    stageKey: 'primary',
    paperTab: 'entrance',
    paperDefaultGrade: '六年级下册',
    scrollTarget: 'exam-module',
    browseModule: '小升初真题',
    browseStageKey: 'primary',
    browseDefaultVolume: '六年级下册',
  },
  zhongkao: {
    key: 'zhongkao',
    name: '中考',
    examType: 'middle',
    defaultTopic: '真题',
    stageKey: 'junior',
    paperTab: 'entrance',
    paperDefaultGrade: '九年级下册',
    scrollTarget: 'exam-module',
    browseModule: '中考真题',
    browseStageKey: 'junior',
    browseDefaultVolume: '九年级下册',
  },
  gaokao: {
    key: 'gaokao',
    name: '高考',
    examType: 'high',
    defaultTopic: '真题',
    stageKey: 'senior',
    paperTab: 'entrance',
    paperDefaultGrade: '高三下册',
    scrollTarget: 'exam-module',
    browseModule: '高考真题',
    browseStageKey: 'senior',
    browseDefaultVolume: '选择性必修二',
  },
  duikou: {
    key: 'duikou',
    name: '对口升学',
    examType: 'vocational_promo',
    defaultTopic: '政策解读',
    stageKey: 'junior',
    paperTab: 'entrance',
    paperDefaultGrade: '九年级下册',
    scrollTarget: 'exam-module',
    browseModule: '学业水平',
    browseStageKey: 'junior',
    browseDefaultVolume: '九年级下册',
  },
}

export const HOME_FUNC_MENU_ITEMS = Object.values(HOME_FUNC_CHANNELS).map((ch) => ({
  key: ch.key,
  name: ch.name,
}))

/** 首页默认升学入口（幼小衔接 → 幼儿） */
export const DEFAULT_HOME_FUNC_KEY: HomeFuncKey = 'youxiao'

export function getHomeFuncChannel(key: string): HomeFuncChannel {
  return HOME_FUNC_CHANNELS[key as HomeFuncKey] ?? HOME_FUNC_CHANNELS[DEFAULT_HOME_FUNC_KEY]
}
