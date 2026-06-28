/** 浏览记录详情链接保留的 query 键（排除 reviewTrail 等超长参数） */
const DETAIL_URL_KEEP_KEYS = [
  'from',
  'stage',
  'subject',
  'version',
  'layout',
  'type',
  'brand',
  'volume',
  'module',
  'unit',
  'lesson',
  'mode',
  'keyword',
  'node',
  'reviewScope',
] as const

/** 与数据库 detail_url VARCHAR(255) 对齐，留少量余量 */
export const DETAIL_URL_MAX_LEN = 250

/**
 * 生成用于浏览记录的紧凑详情链接，避免 fullPath 过长导致入库失败
 */
export function buildCompactDetailUrl(
  path: string,
  query: Record<string, string | string[] | undefined>,
): string {
  const params = new URLSearchParams()
  for (const key of DETAIL_URL_KEEP_KEYS) {
    const value = query[key]
    if (typeof value === 'string' && value) {
      params.set(key, value)
    }
  }
  const qs = params.toString()
  const url = qs ? `${path}?${qs}` : path
  return truncateDetailUrl(url)
}

export function truncateDetailUrl(url: string, maxLen = DETAIL_URL_MAX_LEN): string {
  if (!url || url.length <= maxLen) return url
  return url.slice(0, maxLen)
}
