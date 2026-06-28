/**
 * 教育资讯状态
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { newsApi } from '@/api'
import type { NewsItem, NewsListParams, NewsHomeVO, NewsDetailVO } from '@/api/types'

export const useNewsStore = defineStore('news', () => {
  const list = ref<NewsItem[]>([])
  const total = ref(0)
  const loading = ref(false)
  const home = ref<NewsHomeVO | null>(null)

  async function fetchHome() {
    loading.value = true
    try {
      const res = await newsApi.getHome()
      home.value = res.data.data
      return home.value
    } catch (e) {
      console.error('获取资讯首页失败:', e)
      return null
    } finally {
      loading.value = false
    }
  }

  async function fetchList(params?: NewsListParams) {
    loading.value = true
    try {
      const res = await newsApi.getList(params)
      const data = res.data.data
      list.value = data?.records || []
      total.value = data?.total || 0
    } catch (e) {
      console.error('获取资讯列表失败:', e)
    } finally {
      loading.value = false
    }
  }

  async function fetchDetail(id: number): Promise<NewsDetailVO | null> {
    try {
      const res = await newsApi.getDetail(id)
      return res.data.data
    } catch (e) {
      console.error('获取资讯详情失败:', e)
      return null
    }
  }

  return { list, total, loading, home, fetchHome, fetchList, fetchDetail }
})
