/**
 * 资源状态管理
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { resourceApi } from '@/api'
import type { ResourceItem, ResourceListParams, ResourceStats } from '@/api/types'

export const useResourceStore = defineStore('resource', () => {
  // 资源列表状态
  const list = ref<ResourceItem[]>([])
  const total = ref(0)
  const current = ref(1)
  const loading = ref(false)

  // 热门/推荐资源
  const hotResources = ref<ResourceItem[]>([])
  const recommendResources = ref<ResourceItem[]>([])

  // 平台统计
  const stats = ref<ResourceStats>({
    totalResources: 0,
    todayResources: 0,
    totalUsers: 0,
    totalDownloads: 0
  })

  // 获取资源列表
  async function fetchList(params?: ResourceListParams) {
    loading.value = true
    try {
      const res = await resourceApi.getList(params)
      const data = res.data.data
      list.value = data?.records || []
      total.value = data?.total || 0
      current.value = data?.current || 1
    } catch (e) {
      console.error('获取资源列表失败:', e)
    } finally {
      loading.value = false
    }
  }

  // 搜索资源（委托给 fetchList，语义别名）
  async function searchResources(params?: Omit<ResourceListParams, 'resourceType'>) {
    return fetchList(params as ResourceListParams)
  }

  // 获取热门资源
  async function fetchHot(gradeLevel?: string) {
    try {
      const res = await resourceApi.getHot(gradeLevel)
      hotResources.value = res.data.data || []
    } catch (e) {
      console.error('获取热门资源失败:', e)
    }
  }

  // 获取推荐资源
  async function fetchRecommend() {
    try {
      const res = await resourceApi.getRecommend()
      recommendResources.value = res.data.data || []
    } catch (e) {
      console.error('获取推荐资源失败:', e)
    }
  }

  // 获取平台统计
  async function fetchStats() {
    try {
      const res = await resourceApi.getStats()
      stats.value = res.data.data || {}
    } catch (e) {
      console.error('获取统计失败:', e)
    }
  }

  return {
    list,
    total,
    current,
    loading,
    hotResources,
    recommendResources,
    stats,
    fetchList,
    searchResources,
    fetchHot,
    fetchRecommend,
    fetchStats,
  }
})
