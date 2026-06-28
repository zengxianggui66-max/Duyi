/**
 * 资源展示类型 Tab ↔ API type/subType（与后端 ResourceDisplayType 对齐）
 */

export interface DisplayTypeQuery {
  type?: string
  subType?: string
}

/** 展示类型 Tab → browse API 查询参数 */
export function mapDisplayTypeToQuery(
  displayType: string,
): DisplayTypeQuery {
  if (!displayType || displayType === '全部') return {}
  if (displayType === '精彩片段') {
    return { type: '视频', subType: '精彩片段' }
  }
  if (displayType === '课文相关图片') {
    return { type: '图片素材', subType: '课文图片' }
  }
  return { type: displayType }
}

/** stats 仅按 type 聚合时，为 Tab 提供兜底 key */
export function displayTypeStatsFallbackKey(
  displayType: string,
): string | undefined {
  if (displayType === '课文相关图片') return '图片素材'
  return undefined
}

/** 从 stats 行解析展示类型 key */
export function statsRowDisplayType(row: {
  displayType?: string
  type?: string
}): string {
  return row.displayType || row.type || '其他'
}
