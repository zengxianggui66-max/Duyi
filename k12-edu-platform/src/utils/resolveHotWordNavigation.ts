/**
 * 顶栏热门词 / 搜索框跳转解析
 */
import type { RouteLocationRaw } from 'vue-router'
import { STATIC_HOT_WORDS } from '@/config/homeOpsStatic'
import type { HotWordViewModel } from '@/types/homeOps'
import { findHotWordByLabel, resolveHotWordFromNavTarget } from '@/utils/resolveNavTarget'

/**
 * 将用户输入解析为学科浏览路由。
 * 精确匹配运营配置热词时导航；否则返回 null → 走 redirect / 全站搜索。
 */
export function resolveHotWordNavigation(
  input: string,
  words: HotWordViewModel[] = STATIC_HOT_WORDS,
): RouteLocationRaw | null {
  const trimmed = input.trim()
  if (!trimmed) return null

  const configured = findHotWordByLabel(words, trimmed)
  if (configured) {
    return resolveHotWordFromNavTarget(configured)
  }

  return null
}

export function hotWordLabelsFrom(words: HotWordViewModel[] = STATIC_HOT_WORDS): string[] {
  return words.map((w) => w.label)
}
