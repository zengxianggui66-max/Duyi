/**
 * 搜索状态管理
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { searchApi } from '@/api'

export const useSearchStore = defineStore('search', () => {
  // 热搜关键词
  const hotKeywords = ref<{ keyword: string; searchCount: number; rank: number }[]>([])

  // 搜索历史
  const searchHistory = ref<string[]>([])

  // 获取热搜关键词
  async function fetchHotKeywords(limit?: number) {
    try {
      const res = await searchApi.getHotKeywords(limit)
      hotKeywords.value = res.data.data || []
    } catch (e) {
      console.error('获取热搜关键词失败:', e)
    }
  }

  // 记录搜索关键词
  async function recordKeyword(keyword: string, userId?: number) {
    try {
      await searchApi.recordKeyword(keyword, userId)
    } catch {
      // 记录失败不影响搜索
    }
  }

  // 获取搜索历史
  async function fetchSearchHistory(userId: number, limit?: number) {
    try {
      const res = await searchApi.getSearchHistory(userId, limit)
      searchHistory.value = res.data.data || []
    } catch (e) {
      console.error('获取搜索历史失败:', e)
    }
  }

  // 清空搜索历史
  async function clearSearchHistory(userId: number) {
    try {
      await searchApi.clearHistory(userId)
      searchHistory.value = []
    } catch (e) {
      console.error('清空搜索历史失败:', e)
    }
  }

  return {
    hotKeywords,
    searchHistory,
    fetchHotKeywords,
    recordKeyword,
    fetchSearchHistory,
    clearSearchHistory,
  }
})
