/**
 * 成套资源：单份/批量下载与资料篮（内部员工版，无会员逻辑）
 */
import { ElMessage } from 'element-plus'
import { prepApi, primaryChineseApi } from '@/api'
import type { PrimaryChineseItem } from '@/api/types'
import { usePrepBasketStore } from '@/store/prepBasket'

export function useSuiteResourceActions() {
  const prepBasket = usePrepBasketStore()

  function downloadResource(item: PrimaryChineseItem): boolean {
    if (!item?.id) return false
    if (item.ossUrl) {
      window.open(item.ossUrl, '_blank')
      primaryChineseApi.getDetail(item.id).catch(() => {})
      return true
    }
    return false
  }

  function downloadResources(items: PrimaryChineseItem[]) {
    const list = items.filter((i) => i?.id)
    if (!list.length) {
      ElMessage.warning('没有可下载的资源')
      return
    }
    let ok = 0
    let fail = 0
    for (const item of list) {
      if (downloadResource(item)) ok++
      else fail++
    }
    if (ok > 0 && fail === 0) {
      ElMessage.success(`已开始下载 ${ok} 个文件`)
    } else if (ok > 0) {
      ElMessage.warning(`已开始下载 ${ok} 个，${fail} 个暂无下载链接`)
    } else {
      ElMessage.warning('所选资源均暂无下载链接，请联系管理员')
    }
  }

  async function addResourcesToBasket(items: PrimaryChineseItem[]) {
    const list = items.filter((i) => i?.id)
    if (!list.length) return

    let count = 0
    for (const item of list) {
      try {
        const checkRes = await prepApi.checkExists('resource', item.id)
        if (checkRes?.data?.data?.exists) continue
        await prepApi.addBasketItem({
          itemType: 'resource',
          refId: item.id,
          title: item.title || '',
          subtitle: item.type || '',
        })
        count++
      } catch {
        // 单个失败继续下一个
      }
    }

    if (count > 0) {
      await prepBasket.fetchBasket()
      ElMessage.success(`已将 ${count} 个资源加入资料篮`)
    } else {
      ElMessage.info('所选资源均已在资料篮中或添加失败')
    }
  }

  return {
    downloadResource,
    downloadResources,
    addResourcesToBasket,
  }
}
