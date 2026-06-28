import type { StageKey } from '@/config/subjectConfig'

/** 升学专区侧栏专题 → 学科资源浏览页落点（与 home_panel_tab_config 对齐） */
export interface PromotionTopicBrowseTarget {
  stageKey: StageKey
  subjectKey: string
  browseModule: string
  volumeName?: string
  versionKey?: string
  keyword?: string
  icon?: string
  description?: string
}

/** 幼小衔接四大专题 */
export const KINDERGARTEN_BRIDGE_TOPIC_BROWSE: Record<string, PromotionTopicBrowseTarget> = {
  拼音识字: {
    stageKey: 'preschool',
    subjectKey: 'chinese',
    browseModule: '拼音识字',
    volumeName: '大班下学期',
    keyword: '拼音',
    icon: '🔤',
    description: '声母韵母、整体认读、识字卡片与拼音练习',
  },
  数学启蒙: {
    stageKey: 'preschool',
    subjectKey: 'math',
    browseModule: '数学启蒙',
    volumeName: '大班下学期',
    versionKey: 'renjiao',
    keyword: '数学启蒙',
    icon: '🔢',
    description: '10 以内加减、数感训练与趣味数学启蒙',
  },
  习惯养成: {
    stageKey: 'preschool',
    subjectKey: 'habit',
    browseModule: '习惯养成',
    volumeName: '大班下学期',
    keyword: '习惯',
    icon: '🌟',
    description: '课堂规则、时间管理、专注力与入学适应',
  },
  暑假衔接: {
    stageKey: 'preschool',
    subjectKey: 'chinese',
    browseModule: '暑假衔接',
    volumeName: '大班下学期',
    keyword: '衔接',
    icon: '☀️',
    description: '暑假综合训练，语文数学衔接一年级',
  },
}

/** 其他升学入口：专题 → 浏览页（可按需扩展） */
export const PROMOTION_TOPIC_BROWSE: Record<string, Record<string, PromotionTopicBrowseTarget>> = {
  kindergarten_bridge: KINDERGARTEN_BRIDGE_TOPIC_BROWSE,
  primary_promo: {
    真题: {
      stageKey: 'primary',
      subjectKey: 'chinese',
      browseModule: '小升初真题',
      volumeName: '六年级下册',
      icon: '📜',
      description: '历年小升初真题与解析',
    },
    模拟试卷: {
      stageKey: 'primary',
      subjectKey: 'chinese',
      browseModule: '小升初模拟',
      volumeName: '六年级下册',
      icon: '📝',
      description: '小升初模拟卷与冲刺练习',
    },
    真题汇编: {
      stageKey: 'primary',
      subjectKey: 'chinese',
      browseModule: '真题汇编',
      volumeName: '六年级下册',
      keyword: '小升初',
      icon: '📚',
      description: '小升初历年真题归类整理',
    },
    暑假衔接: {
      stageKey: 'primary',
      subjectKey: 'chinese',
      browseModule: '暑假',
      volumeName: '六年级下册',
      keyword: '衔接',
      icon: '☀️',
      description: '六升七暑假衔接与预习资料',
    },
  },
  middle: {
    真题: {
      stageKey: 'junior',
      subjectKey: 'chinese',
      browseModule: '中考真题',
      volumeName: '九年级下册',
      icon: '📜',
      description: '中考全科真题汇编',
    },
    一模: {
      stageKey: 'junior',
      subjectKey: 'chinese',
      browseModule: '中考模拟',
      volumeName: '九年级下册',
      keyword: '一模',
      icon: '📋',
      description: '各地一模试卷与讲评',
    },
  },
  high: {
    真题: {
      stageKey: 'senior',
      subjectKey: 'chinese',
      browseModule: '高考真题',
      volumeName: '高三下册',
      icon: '📜',
      description: '高考全科真题与解析',
    },
    高考作文: {
      stageKey: 'senior',
      subjectKey: 'chinese',
      browseModule: '作文',
      volumeName: '高三下册',
      keyword: '高考',
      icon: '✍️',
      description: '高考作文素材、范文与写作指导',
    },
  },
  vocational_promo: {
    政策解读: {
      stageKey: 'junior',
      subjectKey: 'chinese',
      browseModule: '专题复习',
      volumeName: '九年级下册',
      keyword: '对口',
      icon: '📌',
      description: '对口升学政策、招生路径与备考说明',
    },
    真题资料: {
      stageKey: 'junior',
      subjectKey: 'math',
      browseModule: '学业水平',
      volumeName: '九年级下册',
      keyword: '对口',
      icon: '📜',
      description: '对口升学真题、答案与考点解析',
    },
    专业对口: {
      stageKey: 'junior',
      subjectKey: 'chinese',
      browseModule: '专题复习',
      volumeName: '九年级下册',
      keyword: '专业',
      icon: '🧭',
      description: '专业方向、对口类别与职业路径资料',
    },
    模拟试卷: {
      stageKey: 'junior',
      subjectKey: 'math',
      browseModule: '中考模拟',
      volumeName: '九年级下册',
      keyword: '对口',
      icon: '📝',
      description: '对口升学综合模拟卷与训练卷',
    },
  },
}

export function resolvePromotionTopicBrowse(
  examType: string,
  topic: string,
): PromotionTopicBrowseTarget | null {
  const map = PROMOTION_TOPIC_BROWSE[examType]
  if (!map) return null
  return map[topic] ?? null
}

export function listKindergartenBridgeTopics(): string[] {
  return Object.keys(KINDERGARTEN_BRIDGE_TOPIC_BROWSE)
}
