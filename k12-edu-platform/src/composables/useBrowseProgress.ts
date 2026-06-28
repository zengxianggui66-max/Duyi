/**
 * 教学进度持久化（localStorage）
 * 记住用户上次浏览的单元/课文位置，按"学段_学科"维度存储
 * 参考 useRecentViews 的 localStorage 模式实现
 */
import { watch } from 'vue'
import type { Ref } from 'vue'

const STORAGE_KEY = 'edu_browse_progress'

export interface BrowseProgress {
  stage: string
  subject: string
  brand?: string
  volume?: string
  version?: string
  /** 目录树节点 ID（新目录树） */
  node?: number | null
  /** 旧目录单元名（兼容 useUnitDirectory） */
  unit?: string
  column?: string
  /** 更新时间戳 */
  updatedAt: number
}

type ProgressMap = Record<string, BrowseProgress>

function loadMap(): ProgressMap {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : {}
  } catch {
    return {}
  }
}

function saveMap(map: ProgressMap) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(map))
  } catch {
    // localStorage 满时静默忽略
  }
}

function makeKey(stage: string, subject: string): string {
  return `${stage}_${subject}`
}

export function useBrowseProgress() {
  /** 获取指定学段+学科的浏览进度 */
  function getProgress(stage: string, subject: string): BrowseProgress | null {
    const map = loadMap()
    return map[makeKey(stage, subject)] || null
  }

  /** 保存当前浏览进度 */
  function saveProgress(progress: BrowseProgress) {
    const map = loadMap()
    map[makeKey(progress.stage, progress.subject)] = {
      ...progress,
      updatedAt: Date.now(),
    }
    saveMap(map)
  }

  /** 在 SubjectDetailPage 中自动监听目录节点/册别变化并保存 */
  function autoSave(options: {
    stage: Ref<string>
    subject: Ref<{ key?: string } | null>
    brand: Ref<string>
    volume: Ref<string>
    version: Ref<string>
    node: Ref<number | null>
    unit: Ref<string>
    column: Ref<string>
  }) {
    watch(
      [options.node, options.volume, options.unit],
      () => {
        if (!options.subject.value?.key) return
        saveProgress({
          stage: options.stage.value,
          subject: options.subject.value.key,
          brand: options.brand.value || undefined,
          volume: options.volume.value || undefined,
          version: options.version.value || undefined,
          node: options.node.value,
          unit: options.unit.value || undefined,
          column: options.column.value || undefined,
          updatedAt: Date.now(),
        })
      },
    )
  }

  return { getProgress, saveProgress, autoSave }
}
