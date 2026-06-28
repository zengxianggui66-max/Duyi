/**
 * 搜索点击上报（P3，fire-and-forget）
 */
import { searchApi } from '@/api/search'
import type { SearchClickPayload } from '@/api/search'

export function reportSearchClick(payload: SearchClickPayload) {
  if (!payload.keyword?.trim()) return
  void searchApi.recordClick(payload).catch(() => {
    // 静默失败，不影响跳转
  })
}
