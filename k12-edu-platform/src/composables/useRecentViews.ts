/**
 * 最近浏览记录：本地缓存 + 登录后同步服务端
 */
import { ref, watch } from 'vue'
import { viewApi } from '@/api/view'
import { useUserStore } from '@/store'
import { truncateDetailUrl } from '@/utils/detailUrl'

const STORAGE_KEY = 'edu_recent_views'
const MAX_ITEMS = 20

export interface RecentViewItem {
  id: number
  title: string
  subject?: string
  stageKey?: string
  stage?: string
  gradeName?: string
  teachingType?: string
  fileExt?: string
  ossUrl?: string
  time: string
  url?: string
}

function loadFromStorage(): RecentViewItem[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

function saveToStorage(list: RecentViewItem[]) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
  } catch {
    // localStorage 满时静默忽略
  }
}

const viewList = ref<RecentViewItem[]>(loadFromStorage())

watch(viewList, (val) => {
  saveToStorage(val)
}, { deep: true })

export function useRecentViews() {
  function addView(item: {
    id: number
    title: string
    subject?: string
    stageKey?: string
    stage?: string
    gradeName?: string
    teachingType?: string
    fileExt?: string
    ossUrl?: string
    url?: string
  }) {
    if (!item.id) return

    const filtered = viewList.value.filter((v) => v.id !== item.id)
    const newItem: RecentViewItem = {
      id: item.id,
      title: item.title,
      subject: item.subject,
      stageKey: item.stageKey,
      stage: item.stage,
      gradeName: item.gradeName,
      teachingType: item.teachingType,
      fileExt: item.fileExt,
      ossUrl: item.ossUrl,
      time: new Date().toISOString(),
      url: item.url,
    }
    viewList.value = [newItem, ...filtered].slice(0, MAX_ITEMS)

    const userStore = useUserStore()
    if (userStore.isLoggedIn) {
      const detailUrl = truncateDetailUrl(item.url || '')
      viewApi.upsert({
        resourceId: item.id,
        resourceType: 'primary_chinese',
        title: item.title,
        subject: item.subject,
        stageKey: item.stageKey,
        stage: item.stage,
        gradeName: item.gradeName,
        teachingType: item.teachingType,
        fileExt: item.fileExt,
        ossUrl: item.ossUrl,
        url: detailUrl,
        detailUrl,
      }).catch(() => {})
    }
  }

  function getViews() {
    return viewList
  }

  function clearViews() {
    viewList.value = []
  }

  function formatViewTime(isoString: string): string {
    if (!isoString) return ''
    const now = new Date()
    const date = new Date(isoString)
    const diffMs = now.getTime() - date.getTime()
    const diffMinutes = Math.floor(diffMs / 60000)
    const diffHours = Math.floor(diffMinutes / 60)
    const diffDays = Math.floor(diffHours / 24)

    if (diffMinutes < 1) return '刚刚'
    if (diffMinutes < 60) return `${diffMinutes} 分钟前`
    if (diffHours < 24) return `${diffHours} 小时前`
    if (diffDays === 1) return '昨天'
    if (diffDays < 7) return `${diffDays} 天前`
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    return `${y}-${m}-${d}`
  }

  return {
    addView,
    getViews,
    clearViews,
    formatViewTime,
  }
}
