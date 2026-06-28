import type { RouteLocationRaw } from 'vue-router'
import type { StageKey } from '@/config/subjectConfig'
import type { HotWordViewModel, NavTarget } from '@/types/homeOps'
import { buildSubjectBrowseRoute } from '@/utils/subjectBrowseRoute'
import { resolveVolumeIdByName } from '@/utils/funcBrowseRoute'

function mergeVolumeQuery(
  stage: StageKey,
  query: Record<string, string>,
  volumeName?: string,
): Record<string, string> {
  const next = { ...query }
  if (next.volume) return next
  const volumeId = resolveVolumeIdByName(stage, volumeName)
  if (volumeId) next.volume = volumeId
  return next
}

function queryToRecord(query?: Record<string, string>): Record<string, string> {
  if (!query) return {}
  const out: Record<string, string> = {}
  for (const [key, value] of Object.entries(query)) {
    if (value != null && value !== '') out[key] = value
  }
  return out
}

/** 将 NavTarget 解析为 Vue Router 目标；scroll/external 返回 null，由调用方单独处理 */
export function resolveNavTargetRoute(target: NavTarget): RouteLocationRaw | null {
  if (!target?.type) return null

  switch (target.type) {
    case 'browse': {
      const stage = (target.stageKey ?? 'primary') as StageKey
      const subjectKey = target.subjectKey ?? 'chinese'
      const versionKey = target.versionKey ?? 'tongbian2024'
      const query = mergeVolumeQuery(stage, queryToRecord(target.query), target.volumeName)
      return buildSubjectBrowseRoute({ stage, subjectKey, versionKey, query })
    }
    case 'search': {
      const stage = (target.stageKey ?? 'primary') as StageKey
      const subjectKey = target.subjectKey ?? 'chinese'
      const versionKey = target.versionKey ?? 'tongbian2024'
      const baseQuery = queryToRecord(target.query)
      const query = mergeVolumeQuery(
        stage,
        {
          ...baseQuery,
          module: baseQuery.module ?? '同步备课',
          keyword: target.keyword ?? '',
        },
        target.volumeName,
      )
      return buildSubjectBrowseRoute({ stage, subjectKey, versionKey, query })
    }
    case 'route':
      return target.routePath ? { path: target.routePath } : null
    case 'vip':
      return { path: '/vip' }
    case 'external':
    case 'scroll':
      return null
    default:
      return null
  }
}

export function resolveHotWordFromNavTarget(word: HotWordViewModel): RouteLocationRaw | null {
  return resolveNavTargetRoute(word.navTarget)
}

export function findHotWordByLabel(words: HotWordViewModel[], input: string): HotWordViewModel | undefined {
  const key = input.trim()
  if (!key) return undefined
  return words.find((w) => w.label === key)
}

export function openExternalNavTarget(target: NavTarget) {
  if (target.type !== 'external' || !target.externalUrl) return
  if (target.openInNewTab) {
    window.open(target.externalUrl, '_blank', 'noopener,noreferrer')
  } else {
    window.location.href = target.externalUrl
  }
}

export function scrollToNavTarget(target: NavTarget) {
  if (target.type !== 'scroll' || !target.scrollTarget) return false
  const el = document.getElementById(target.scrollTarget)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
    return true
  }
  return false
}
