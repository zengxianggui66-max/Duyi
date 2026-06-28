import { ref, watch, onMounted } from 'vue'

import { ElMessage, ElMessageBox } from 'element-plus'

import { downloadApi, type DownloadItem, type DownloadStats } from '@/api/download'

import { buildPrimaryChineseFileUrl } from '@/api/primaryChinese'

import { unwrapData } from '@/api/request'

import { useUserStore } from '@/store'



function formatDate(raw?: string): string {

  if (!raw) return '-'

  return raw.slice(0, 10)

}



function formatFileSize(bytes?: number): string {

  if (bytes === undefined || bytes === null) return '-'

  if (bytes < 1024) return bytes + ' B'

  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'

  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'

}



function formatExt(ext?: string): string {

  if (!ext) return '文件'

  return ext.replace(/^\./, '').toUpperCase()

}



export function useMyDownloads() {

  const userStore = useUserStore()



  const loading = ref(false)

  const items = ref<DownloadItem[]>([])

  const total = ref(0)

  const currentPage = ref(1)

  const pageSize = ref(20)

  const searchKeyword = ref('')

  const stats = ref<DownloadStats>({ total: 0, weekCount: 0, todayCount: 0 })



  async function fetchStats() {

    if (!userStore.isLoggedIn) return

    try {

      const res = await downloadApi.getStats()

      stats.value = unwrapData(res) || { total: 0, weekCount: 0, todayCount: 0 }

    } catch {

      stats.value = { total: 0, weekCount: 0, todayCount: 0 }

    }

  }



  async function fetchList() {

    if (!userStore.isLoggedIn) {

      items.value = []

      total.value = 0

      return

    }

    loading.value = true

    try {

      const res = await downloadApi.getPage({

        current: currentPage.value,

        size: pageSize.value,

        keyword: searchKeyword.value.trim() || undefined,

      })

      const page = unwrapData(res)

      items.value = page?.records || []

      total.value = page?.total ?? 0

    } catch {

      items.value = []

      total.value = 0

      ElMessage.error('加载下载记录失败')

    } finally {

      loading.value = false

    }

  }



  function setSearchKeyword(kw: string) {

    searchKeyword.value = kw

    currentPage.value = 1

    void fetchList()

  }



  function handleView(item: DownloadItem) {

    const query = item.stageKey ? `?stage=${item.stageKey}&from=subject` : ''

    window.open(`/resource/${item.resourceId}${query}`, '_blank')

  }



  function handleDownload(item: DownloadItem) {

    const url = buildPrimaryChineseFileUrl(item.resourceId, 'attachment')

    window.open(url, '_blank')

    ElMessage.success('开始下载')

  }



  async function handleDelete(item: DownloadItem) {

    try {

      await ElMessageBox.confirm(

        `确定删除「${item.resourceTitle}」的下载记录？`,

        '提示',

        { type: 'warning' },

      )

      await downloadApi.remove(item.id)

      ElMessage.success('已删除下载记录')

      await fetchList()

      await fetchStats()

    } catch (e: unknown) {

      if (e !== 'cancel' && e !== 'close') {

        ElMessage.error('操作失败')

      }

    }

  }



  async function handleBatchDelete(ids: number[]) {

    if (!ids.length) return

    try {

      await ElMessageBox.confirm(`确定删除选中的 ${ids.length} 条下载记录？`, '提示', { type: 'warning' })

      await downloadApi.batchRemove(ids)

      ElMessage.success('已删除选中记录')

      await fetchList()

      await fetchStats()

    } catch (e: unknown) {

      if (e !== 'cancel' && e !== 'close') {

        ElMessage.error('操作失败')

      }

    }

  }



  watch([currentPage, pageSize], () => {

    void fetchList()

  })



  watch(

    () => userStore.isLoggedIn,

    () => {

      void fetchStats()

      void fetchList()

    },

  )



  onMounted(() => {

    void fetchStats()

    void fetchList()

  })



  return {

    loading,

    items,

    total,

    currentPage,

    pageSize,

    searchKeyword,

    stats,

    fetchList,

    fetchStats,

    setSearchKeyword,

    handleView,

    handleDownload,

    handleDelete,

    handleBatchDelete,

    formatDate,

    formatFileSize,

    formatExt,

  }

}


