/**
 * 资料篮状态（登录走 API，未登录走 localStorage）
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { prepApi } from '@/api/prep'
import type { AddBasketItemPayload, PrepBasketItem } from '@/api/prep'
import { downloadBlob } from '@/utils/downloadBlob'
import { useUserStore } from './user'

const LOCAL_KEY = 'prep_basket_local'

function loadLocal(): PrepBasketItem[] {
  try {
    const raw = localStorage.getItem(LOCAL_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

function saveLocal(items: PrepBasketItem[]) {
  localStorage.setItem(LOCAL_KEY, JSON.stringify(items))
}

export const usePrepBasketStore = defineStore('prepBasket', () => {
  const items = ref<PrepBasketItem[]>([])
  const basketId = ref<number | null>(null)
  const loading = ref(false)

  const totalCount = computed(() => items.value.length)
  const questionCount = computed(
    () => items.value.filter((i) => i.itemType === 'question').length
  )
  const resourceCount = computed(
    () => items.value.filter((i) => i.itemType === 'resource').length
  )
  const questionItems = computed(() =>
    items.value.filter((i) => i.itemType === 'question')
  )
  const resourceItems = computed(() =>
    items.value.filter((i) => ['resource', 'paper', 'album'].includes(i.itemType))
  )
  const downloadableItems = computed(() =>
    items.value.filter((i) => i.itemType === 'resource' || i.itemType === 'paper')
  )
  const downloading = ref(false)

  async function fetchBasket() {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      items.value = loadLocal()
      basketId.value = null
      return
    }
    loading.value = true
    try {
      const res = await prepApi.getBasket()
      const data = res.data.data
      items.value = data?.items || []
      basketId.value = data?.basketId ?? null
    } catch (e) {
      console.error('加载资料篮失败', e)
    } finally {
      loading.value = false
    }
  }

  async function addItem(payload: AddBasketItemPayload) {
    const userStore = useUserStore()
    const dup = items.value.some(
      (i) => i.itemType === payload.itemType && i.refId === payload.refId
    )
    if (dup) {
      ElMessage.warning('已在资料篮中')
      return false
    }

    if (!userStore.isLoggedIn) {
      const local: PrepBasketItem = {
        ...payload,
        id: Date.now(),
        sortOrder: items.value.length,
      }
      items.value = [...items.value, local]
      saveLocal(items.value)
      ElMessage.success('已加入资料篮（本地），登录后可云端同步')
      return true
    }

    try {
      const res = await prepApi.addBasketItem(payload)
      const added = res.data.data
      items.value = [...items.value, added]
      ElMessage.success('已加入资料篮')
      return true
    } catch {
      return false
    }
  }

  async function removeItem(itemId: number) {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      items.value = items.value.filter((i) => i.id !== itemId)
      saveLocal(items.value)
      return
    }
    await prepApi.removeBasketItem(itemId)
    items.value = items.value.filter((i) => i.id !== itemId)
    ElMessage.success('已移出资料篮')
  }

  async function reorder(orderedItemIds: number[]) {
    const userStore = useUserStore()
    const idMap = new Map(items.value.map((i) => [i.id!, i]))
    const reordered = orderedItemIds
      .map((id, idx) => {
        const item = idMap.get(id)
        return item ? { ...item, sortOrder: idx } : null
      })
      .filter(Boolean) as PrepBasketItem[]
    const rest = items.value.filter((i) => !orderedItemIds.includes(i.id!))
    items.value = [...reordered, ...rest]

    if (!userStore.isLoggedIn) {
      saveLocal(items.value)
      return
    }
    await prepApi.reorderBasket(orderedItemIds)
  }

  async function mergeLocalOnLogin() {
    const local = loadLocal()
    if (!local.length) return 0
    try {
      const payloads: AddBasketItemPayload[] = local.map((i) => ({
        itemType: i.itemType,
        refId: i.refId,
        title: i.title,
        subtitle: i.subtitle,
        coverUrl: i.coverUrl,
        metaJson: i.metaJson,
        score: i.score,
      }))
      const res = await prepApi.mergeBasket(payloads)
      const merged = res.data.data?.merged ?? 0
      localStorage.removeItem(LOCAL_KEY)
      await fetchBasket()
      if (merged > 0) {
        ElMessage.success(`已合并 ${merged} 项本地资料篮内容`)
      }
      return merged
    } catch (e) {
      console.error('合并资料篮失败', e)
      return 0
    }
  }

  async function downloadAllAsZip() {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录后批量下载')
      return false
    }
    if (!downloadableItems.value.length) {
      ElMessage.warning('资料篮中没有可打包的资源或试卷（试题请使用组卷导出）')
      return false
    }
    downloading.value = true
    try {
      const summaryRes = await prepApi.getBasketDownloadSummary()
      const summary = summaryRes.data.data
      if (summary && summary.downloadableCount === 0) {
        ElMessage.warning('当前条目均暂无文件，请从专题区或资源详情重新加入')
        return false
      }
      const res = await prepApi.downloadBasketZip()
      downloadBlob(res.data as Blob, '资料篮批量下载.zip')
      ElMessage.success(
        summary
          ? `已开始下载，共 ${summary.downloadableCount} 个文件（另含下载说明.txt）`
          : '已开始下载'
      )
      return true
    } catch {
      ElMessage.error('批量下载失败，请稍后重试')
      return false
    } finally {
      downloading.value = false
    }
  }

  async function clearAll() {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      items.value = []
      saveLocal([])
      return
    }
    await prepApi.clearBasket()
    items.value = []
    ElMessage.success('资料篮已清空')
  }

  return {
    items,
    basketId,
    loading,
    totalCount,
    questionCount,
    resourceCount,
    questionItems,
    resourceItems,
    downloadableItems,
    downloading,
    fetchBasket,
    downloadAllAsZip,
    addItem,
    removeItem,
    reorder,
    mergeLocalOnLogin,
    clearAll,
  }
})
