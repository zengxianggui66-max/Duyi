import type { HomeFuncKey } from '@/constants/homeFuncChannels'
import { DEFAULT_HOME_FUNC_KEY } from '@/constants/homeFuncChannels'
const ROUTE_TYPE_ALIASES: Record<string, HomeFuncKey> = {
  youxiao: 'youxiao',
  kindergarten_bridge: 'youxiao',
  xiaoshengchu: 'xiaoshengchu',
  primary_promo: 'xiaoshengchu',
  xsc: 'xiaoshengchu',
  zhongkao: 'zhongkao',
  middle: 'zhongkao',
  zk: 'zhongkao',
  gaokao: 'gaokao',
  high: 'gaokao',
  gk: 'gaokao',
  duikou: 'duikou',
  vocational_promo: 'duikou',
}

/** examType → func_key */
export const EXAM_TYPE_TO_FUNC_KEY: Record<string, HomeFuncKey> = {
  kindergarten_bridge: 'youxiao',
  primary_promo: 'xiaoshengchu',
  middle: 'zhongkao',
  high: 'gaokao',
  vocational_promo: 'duikou',
}

export const PROMOTION_LANDING_META: Record<
  HomeFuncKey,
  { title: string; description: string; icon: string }
> = {
  youxiao: {
    icon: '🌱',
    title: '幼小衔接备考专区',
    description: '面向中大班与幼小衔接，覆盖拼音识字、数学启蒙、习惯养成与暑假衔接资料。',
  },
  xiaoshengchu: {
    icon: '🎯',
    title: '小升初备考专区',
    description: '小升初真题、模拟卷、名校招生与分班考试资源，择校冲刺一站式获取。',
  },
  zhongkao: {
    icon: '🏅',
    title: '中考备考专区',
    description: '中考真题、一模二模、三轮复习与专项训练，全科冲刺提分。',
  },
  gaokao: {
    icon: '🏆',
    title: '高考备考专区',
    description: '高考真题、模拟卷、一二三轮复习与作文专项，冲刺理想院校。',
  },
  duikou: {
    icon: '🎓',
    title: '对口升学备考专区',
    description: '对口升学政策解读、真题资料、专业对口与模拟试卷，贯通培养备考指南。',
  },
}

const VALID_FUNC_KEYS = new Set<string>(Object.keys(PROMOTION_LANDING_META))

export function isValidPromotionFuncKey(key: string): key is HomeFuncKey {
  return VALID_FUNC_KEYS.has(key)
}

export function resolvePromotionFuncKey(typeParam: string | undefined | null): HomeFuncKey | null {
  if (!typeParam) return null
  const normalized = typeParam.trim().toLowerCase()
  return ROUTE_TYPE_ALIASES[normalized] ?? (isValidPromotionFuncKey(normalized) ? normalized : null)
}

export function examTypeToFuncKey(examType: string): HomeFuncKey | null {
  return EXAM_TYPE_TO_FUNC_KEY[examType] ?? null
}

export function buildPromotionRoute(
  funcKey: string,
  query?: { topic?: string },
): { path: string; query?: { topic: string } } {
  const route: { path: string; query?: { topic: string } } = {
    path: `/promotion/${funcKey}`,
  }
  if (query?.topic) {
    route.query = { topic: query.topic }
  }
  return route
}

export function applyPromotionPageMeta(funcKey: HomeFuncKey) {
  const meta = PROMOTION_LANDING_META[funcKey] ?? PROMOTION_LANDING_META.gaokao
  const siteName = 'K12教育资源平台'
  document.title = `${meta.title} - ${siteName}`

  let descEl = document.querySelector('meta[name="description"]')
  if (!descEl) {
    descEl = document.createElement('meta')
    descEl.setAttribute('name', 'description')
    document.head.appendChild(descEl)
  }
  descEl.setAttribute('content', meta.description)
}

export function getDefaultPromotionFuncKey(): HomeFuncKey {
  return DEFAULT_HOME_FUNC_KEY
}
