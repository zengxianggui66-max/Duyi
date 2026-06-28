<template>
  <header class="app-header" :class="{ 'app-header--home': isHome }">
    <div class="header-inner">
      <router-link to="/" class="logo">
        <span class="logo-icon">📚</span>
        <span class="logo-text">新课堂教育</span>
      </router-link>

      <nav class="nav-menu">
        <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">首页</router-link>

        <el-dropdown trigger="hover" popper-class="stage-nav-dropdown" @command="handleStageNav">
          <span
            class="nav-item nav-dropdown"
            :class="{ active: isStageNavActive }"
          >
            学段资源 <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="stage in stageNavList"
                :key="stage.key"
                :command="stage.key"
              >
                {{ stage.name }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-dropdown
          trigger="hover"
          popper-class="feature-channel-mega-dropdown"
          :show-timeout="80"
          :hide-timeout="120"
        >
          <span
            class="nav-item nav-dropdown"
            :class="{ active: isFeatureNavActive }"
          >
            特色频道 <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <FeatureChannelMegaPanel />
          </template>
        </el-dropdown>

        <el-dropdown trigger="hover" @command="handleCoreNav">
          <span class="nav-item nav-dropdown">
            核心功能 <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="lesson">📝 备课中心</el-dropdown-item>
              <el-dropdown-item command="smart-lesson">🤖 智能备课</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <router-link to="/news" class="nav-item" :class="{ active: $route.path.startsWith('/news') }">
          教育资讯
        </router-link>
      </nav>

      <div class="header-search">
        <div ref="searchWrapRef" class="search-box">
          <span class="search-input-icon" aria-hidden="true">
            <el-icon><Search /></el-icon>
          </span>
          <input
            v-model="searchKeyword"
            type="text"
            class="search-input"
            placeholder="搜索试卷、教案、课件..."
            @input="onSearchInput"
            @focus="onSearchFocus"
            @keydown.down.prevent="moveSuggest(1)"
            @keydown.up.prevent="moveSuggest(-1)"
            @keydown.esc="closeSuggest"
            @keyup.enter="handleSearch"
          />
          <button type="button" class="search-btn" aria-label="搜索" @click="handleSearch">
            <el-icon class="search-btn-icon"><Search /></el-icon>
            <span class="search-btn-text">搜索</span>
          </button>
          <div v-if="showSuggest" class="search-suggest-panel">
            <div
              v-for="group in groupedSuggestions"
              :key="group.key"
              class="suggest-group"
            >
              <div class="suggest-group-title">{{ group.label }}</div>
              <button
                v-for="item in group.items"
                :key="`${item.kind}-${item.text}`"
                type="button"
                class="suggest-item"
                :class="{ active: isActiveSuggest(item) }"
                @mousedown.prevent="applySuggestion(item)"
              >
                <span class="suggest-text" v-html="suggestItemHtml(item)"></span>
                <span v-if="item.subtitle" class="suggest-subtitle">{{ item.subtitle }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="header-actions">
        <button type="button" class="action-btn action-btn--outline" @click="goDataBlue">
          <span>📋</span>
          <span>资料篮</span>
          <span v-if="prepBasket.totalCount" class="header-basket-badge">{{ prepBasket.totalCount }}</span>
        </button>
        <button type="button" class="action-btn action-btn--primary" @click="goUpload">
          <span>⬆</span>
          <span>上传资源</span>
        </button>
        <router-link to="/help" class="header-link">帮助中心</router-link>

        <div class="auth-buttons">
          <template v-if="userStore.isLoggedIn">
            <el-dropdown trigger="hover" popper-class="user-nav-dropdown" @command="handleUserNav">
              <div class="user-avatar">
                <el-avatar :size="32" :src="userStore.user?.avatar">
                  {{ userStore.nickname?.charAt(0) }}
                </el-avatar>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="item in userNavItems"
                    :key="item.command"
                    :command="item.command"
                    :divided="item.divided"
                    :class="{ 'user-nav-item--danger': item.danger }"
                  >
                    <span class="user-nav-item">
                      <span class="user-nav-icon">{{ item.icon }}</span>
                      <span class="user-nav-label">{{ item.label }}</span>
                    </span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="login-btn">登录</router-link>
            <router-link to="/register" class="register-btn btn-gradient">注册</router-link>
          </template>
        </div>
      </div>
    </div>

    <div v-if="isHome" class="header-hot-row">
      <div class="header-hot-inner">
        <span class="hot-label">热门：</span>
        <span
          v-for="word in hotWordLabels"
          :key="word"
          class="hot-word"
          @click="quickSearch(word)"
        >{{ word }}</span>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, watchEffect, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore, usePrepBasketStore } from '@/store'
import { searchApi, suggestGroupKey, suggestGroupLabel, resolveSuggestionRoute } from '@/api/search'
import { unwrapData } from '@/api/request'
import type { SearchSuggestItem } from '@/api/search'
import { stages } from '@/config/subjectConfig'
import { loadHomeHotWords } from '@/composables/homeOpsSource'
import type { HotWordViewModel } from '@/types/homeOps'
import { hotWordLabelsFrom, resolveHotWordNavigation } from '@/utils/resolveHotWordNavigation'
import { findExactCatalogEntry, mergeSuggestItems, matchCatalogSuggestions } from '@/config/searchCatalog'
import { reportSearchClick } from '@/utils/searchAnalytics'
import FeatureChannelMegaPanel from '@/components/layout/FeatureChannelMegaPanel.vue'

const stageNavList = stages

interface UserNavItem {
  command: string
  icon: string
  label: string
  divided?: boolean
  danger?: boolean
}

const userNavItems: UserNavItem[] = [
  { command: 'profile', icon: '🏠', label: '个人中心' },
  { command: 'favorites', icon: '⭐', label: '我的收藏' },
  { command: 'downloads', icon: '📥', label: '我的下载' },
  { command: 'views', icon: '👁', label: '我的浏览' },
  { command: 'upload', icon: '📤', label: '我的上传' },
  { command: 'logout', icon: '🚪', label: '退出登录', divided: true, danger: true },
]

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const prepBasket = usePrepBasketStore()
const searchKeyword = ref('')
const hotWords = ref<HotWordViewModel[]>([])
const hotWordLabels = computed(() => hotWordLabelsFrom(hotWords.value))
const suggestions = ref<SearchSuggestItem[]>([])
const groupedSuggestions = computed(() => {
  const groupMap = new Map<string, SearchSuggestItem[]>()
  const order = [
    'subject_stage',
    'resource',
    'feature',
    'prep',
    'news',
    'history',
    'hot',
    'page',
    'keyword',
  ]
  for (const item of suggestions.value) {
    const key = suggestGroupKey(item.kind)
    if (!groupMap.has(key)) groupMap.set(key, [])
    const list = groupMap.get(key)!
    if (list.length < 5) list.push(item)
  }
  const groups: Array<{ key: string; label: string; items: SearchSuggestItem[] }> = []
  for (const key of order) {
    const items = groupMap.get(key) || []
    if (!items.length) continue
    groups.push({ key, label: suggestGroupLabel(key), items })
  }
  return groups
})
const flatSuggestions = computed(() =>
  groupedSuggestions.value.flatMap((group) => group.items).slice(0, 12),
)
const showSuggest = ref(false)
const activeIndex = ref(-1)
const searchWrapRef = ref<HTMLElement | null>(null)
let suggestTimer: ReturnType<typeof setTimeout> | null = null

const isHome = computed(() => route.path === '/')

const isStageNavActive = computed(() => {
  const p = route.path
  return p.startsWith('/grade/') || p.startsWith('/subject/')
})

onMounted(() => {
  prepBasket.fetchBasket()
  void loadHomeHotWords().then((list) => {
    hotWords.value = list
  })
  window.addEventListener('click', handleWindowClick)
})

watchEffect(() => {
  document.documentElement.style.setProperty(
    '--header-height',
    isHome.value ? '104px' : '64px',
  )
})

onUnmounted(() => {
  document.documentElement.style.setProperty('--header-height', '64px')
  window.removeEventListener('click', handleWindowClick)
  if (suggestTimer) clearTimeout(suggestTimer)
})

function navigateBySearchInput(input: string) {
  const target = resolveHotWordNavigation(input, hotWords.value)
  if (target) {
    router.push(target)
    return true
  }
  return false
}

async function handleSearch() {
  let q = searchKeyword.value.trim()
  if (showSuggest.value && activeIndex.value >= 0) {
    const picked = flatSuggestions.value[activeIndex.value]
    if (picked) {
      await applySuggestion(picked)
      return
    }
  }
  if (!q) return
  closeSuggest()
  // 优先走后端 /search/redirect；前端热词仅作少量固定词兜底
  try {
    const redirectRes = await searchApi.redirect(q)
    const redirectData = unwrapData(redirectRes)
    if (redirectData?.directHit && redirectData.target?.detailRoute) {
      reportSearchClick({
        keyword: q,
        docId: redirectData.target.docId,
        resourceId: redirectData.target.resourceId,
        resourceType: redirectData.target.resourceType,
        clickType: 'redirect',
        position: 1,
        detailRoute: redirectData.target.detailRoute,
      })
      const targetRoute = resolveSuggestionRoute({
        text: q,
        kind: 'keyword',
        detailRoute: redirectData.target.detailRoute,
        routeQuery: redirectData.target.routeQuery,
      })
      router.push(targetRoute || redirectData.target.detailRoute)
      return
    }
  } catch {
    // redirect 不可用时回退
  }
  const localHit = findExactCatalogEntry(q)
  if (localHit?.detailRoute) {
    await router.push(resolveSuggestionRoute(localHit) || localHit.detailRoute)
    return
  }
  if (navigateBySearchInput(q)) return
  router.push({ name: 'SearchResult', query: { q, page: '1', sort: 'score' } })
}

async function quickSearch(word: string) {
  searchKeyword.value = word
  closeSuggest()
  await handleSearch()
}

function onSearchInput() {
  requestSuggest(false)
}

function onSearchFocus() {
  requestSuggest(true)
}

function requestSuggest(immediate: boolean) {
  const q = searchKeyword.value.trim()
  if (suggestTimer) clearTimeout(suggestTimer)
  const run = async () => {
    try {
      const res = await searchApi.suggest(q, 12, userStore.user?.id)
      const remote = unwrapData(res) || []
      suggestions.value = q ? mergeSuggestItems(remote, q, 12) : remote.slice(0, 12)
    } catch {
      suggestions.value = q ? matchCatalogSuggestions(q, 12) : []
    }
    const hasData = flatSuggestions.value.length > 0
    activeIndex.value = hasData ? 0 : -1
    showSuggest.value = hasData
  }
  if (immediate) {
    run()
    return
  }
  suggestTimer = setTimeout(run, 220)
}

function moveSuggest(step: 1 | -1) {
  if (!showSuggest.value || !flatSuggestions.value.length) return
  const len = flatSuggestions.value.length
  const next = (activeIndex.value + step + len) % len
  activeIndex.value = next
}

function closeSuggest() {
  showSuggest.value = false
  activeIndex.value = -1
}

async function applySuggestion(item: SearchSuggestItem) {
  if (item.kind === 'hot') {
    const hotRoute = resolveHotWordNavigation(item.text, hotWords.value)
    if (hotRoute) {
      closeSuggest()
      await router.push(hotRoute)
      return
    }
  }
  const targetRoute = resolveSuggestionRoute(item)
  if (targetRoute) {
    closeSuggest()
    reportSearchClick({
      keyword: searchKeyword.value.trim(),
      docId: item.resourceId ? `${item.resourceType}:${item.resourceId}` : undefined,
      resourceId: item.resourceId,
      resourceType: item.resourceType,
      clickType: ['title', 'resourceType', 'page'].includes(item.kind) ? 'result' : 'recommend',
      detailRoute: item.detailRoute,
    })
    await router.push(targetRoute)
    return
  }
  appendKeywordToken(item.text)
  await requestSuggestAfterAppend()
}

function appendKeywordToken(text: string) {
  const token = text.trim()
  if (!token) return
  const tokens = searchKeyword.value.trim().split(/\s+/).filter(Boolean)
  const exists = tokens.some((t) => t.toLowerCase() === token.toLowerCase())
  if (exists) return
  searchKeyword.value = tokens.length ? `${tokens.join(' ')} ${token}` : token
}

async function requestSuggestAfterAppend() {
  const q = searchKeyword.value.trim()
  try {
    const res = await searchApi.suggest(q, 12, userStore.user?.id)
    const remote = unwrapData(res) || []
    suggestions.value = mergeSuggestItems(remote, q, 12)
  } catch {
    suggestions.value = matchCatalogSuggestions(q, 12)
  }
  const hasData = flatSuggestions.value.length > 0
  activeIndex.value = hasData ? 0 : -1
  showSuggest.value = hasData
}

function suggestItemHtml(item: SearchSuggestItem) {
  if (item.highlight) return item.highlight
  return item.text
}

function isActiveSuggest(item: SearchSuggestItem) {
  return flatSuggestions.value[activeIndex.value] === item
}

function handleWindowClick(e: MouseEvent) {
  const target = e.target as Node | null
  if (!target) return
  if (searchWrapRef.value && !searchWrapRef.value.contains(target)) {
    closeSuggest()
  }
}

function goDataBlue() {
  router.push('/lesson/basket')
}

function goUpload() {
  router.push('/upload')
}

function handleStageNav(stageKey: string) {
  // 美术/舞蹈学科 key 与 stage 相同
  const defaults: Record<string, { subject: string; version: string; query?: Record<string, string> }> = {
    preschool: {
      subject: 'chinese',
      version: 'tongbian2024',
      query: { module: '拼音识字', volume: 'k3s2' },
    },
    primary: { subject: 'chinese', version: 'tongbian2024' },
    junior: { subject: 'chinese', version: 'tongbian2024' },
    senior: { subject: 'chinese', version: 'tongbian2024' },
    art: { subject: 'art', version: 'tongbian2024' },
    dance: { subject: 'dance', version: 'tongbian2024' },
  }
  const target = defaults[stageKey] || defaults.primary
  router.push({
    name: 'SubjectDetail',
    params: { stage: stageKey, subject: target.subject, version: target.version },
    query: target.query,
  })
}

function handleCoreNav(cmd: string) {
  const routes: Record<string, string> = {
    upload: '/upload',
    lesson: '/lesson',
    'smart-lesson': '/lesson/smart',
  }
  router.push(routes[cmd] || '/')
}

const isFeatureNavActive = computed(() => {
  const p = route.path
  return p.startsWith('/culture') || p.startsWith('/competition') || p.startsWith('/topic')
})

function handleUserNav(cmd: string) {
  const routes: Record<string, string> = {
    profile: '/profile?tab=home',
    favorites: '/profile?tab=favorites',
    downloads: '/profile?tab=downloads',
    views: '/profile?tab=views',
    upload: '/profile?tab=upload',
  }
  if (cmd === 'logout') {
    userStore.logout()
  } else {
    router.push(routes[cmd] || '/profile?tab=home')
  }
}
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
}

.header-inner {
  max-width: 1440px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 16px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  text-decoration: none;
}
.logo-icon {
  font-size: 26px;
}
.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #1a5cbf;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
}

.nav-item {
  padding: 7px 12px;
  border-radius: var(--radius-round);
  font-size: 14px;
  color: var(--text-regular);
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 4px;
}
.nav-item:hover,
.nav-item.active {
  color: var(--color-primary);
  background: var(--color-primary-bg);
}

.header-search {
  flex: 1;
  min-width: 0;
  max-width: 460px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 6px;
  min-height: 42px;
  padding: 4px 4px 4px 14px;
  background: linear-gradient(180deg, #ffffff 0%, #f7f9fc 100%);
  border: 1px solid rgba(26, 92, 191, 0.14);
  border-radius: 999px;
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  overflow: visible;
  transition:
    border-color 0.25s ease,
    box-shadow 0.25s ease,
    background 0.25s ease;
  position: relative;
}

.search-box:focus-within {
  border-color: rgba(26, 92, 191, 0.45);
  background: #fff;
  box-shadow:
    0 0 0 4px rgba(26, 92, 191, 0.1),
    0 8px 24px rgba(26, 92, 191, 0.12);
}

.search-input-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  color: #8a94a6;
  font-size: 16px;
  transition: color 0.2s ease;
}

.search-box:focus-within .search-input-icon {
  color: #1a5cbf;
}

.search-input {
  flex: 1;
  min-width: 0;
  height: 34px;
  padding: 0 8px 0 4px;
  border: none;
  outline: none;
  font-size: 14px;
  color: #1f2937;
  background: transparent;
}

.search-input::placeholder {
  color: #a0a8b8;
}

.search-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex-shrink: 0;
  height: 34px;
  min-width: 78px;
  padding: 0 18px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #1a5cbf 0%, #2d7dd2 52%, #1e88e5 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.04em;
  cursor: pointer;
  white-space: nowrap;
  box-shadow:
    0 4px 14px rgba(26, 92, 191, 0.32),
    inset 0 1px 0 rgba(255, 255, 255, 0.22);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    filter 0.2s ease;
}

.search-btn-icon {
  font-size: 15px;
}

.search-btn-text {
  line-height: 1;
}

.search-btn:hover {
  filter: brightness(1.05);
  box-shadow:
    0 6px 18px rgba(26, 92, 191, 0.38),
    inset 0 1px 0 rgba(255, 255, 255, 0.28);
  transform: translateY(-1px);
}

.search-btn:active {
  transform: translateY(0);
  box-shadow:
    0 2px 8px rgba(26, 92, 191, 0.28),
    inset 0 1px 0 rgba(255, 255, 255, 0.18);
}

.search-suggest-panel {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  z-index: 20;
  overflow: auto;
  max-height: 420px;
  padding: 6px 0;
}

.suggest-group + .suggest-group {
  margin-top: 4px;
}

.suggest-group-title {
  color: #909399;
  font-size: 12px;
  padding: 6px 12px;
}

.suggest-item {
  width: 100%;
  border: none;
  background: #fff;
  text-align: left;
  padding: 8px 12px;
  display: block;
  cursor: pointer;
}

.suggest-subtitle {
  display: block;
  margin-top: 2px;
  color: #909399;
  font-size: 11px;
  line-height: 1.4;
}

.suggest-item + .suggest-item {
  border-top: 1px solid #f0f2f5;
}

.suggest-item:hover,
.suggest-item.active {
  background: #f5f9ff;
}

.suggest-text {
  color: #303133;
  font-size: 13px;
}
.suggest-text :deep(em) {
  color: #1a5cbf;
  font-style: normal;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  margin-left: auto;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 34px;
  padding: 0 12px;
  border-radius: 18px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}
.action-btn--outline {
  border: 1px solid #1a5cbf;
  background: #fff;
  color: #1a5cbf;
  position: relative;
}
.header-basket-badge {
  margin-left: 4px;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 999px;
  line-height: 1.4;
}
.action-btn--outline:hover {
  background: #ecf5ff;
}
.action-btn--primary {
  border: none;
  background: #22c55e;
  color: #fff;
}
.action-btn--primary:hover {
  background: #16a34a;
}

.header-link {
  padding: 6px 10px;
  border-radius: var(--radius-round);
  font-size: 13px;
  color: var(--text-regular);
  text-decoration: none;
  white-space: nowrap;
}
.header-link:hover {
  color: var(--color-primary);
  background: var(--color-primary-bg);
}

.auth-buttons {
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-avatar {
  cursor: pointer;
}

.login-btn {
  padding: 6px 12px;
  font-size: 13px;
  color: var(--text-regular);
  text-decoration: none;
}
.register-btn {
  font-size: 13px;
  padding: 6px 14px;
}

.header-hot-row {
  border-top: 1px solid var(--border-light);
  background: #fafbfd;
}

.header-hot-inner {
  max-width: 1440px;
  margin: 0 auto;
  padding: 6px 24px 10px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.hot-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.hot-word {
  font-size: 12px;
  color: #1a5cbf;
  cursor: pointer;
  padding: 2px 10px;
  border-radius: 12px;
  background: rgba(26, 92, 191, 0.08);
}
.hot-word:hover {
  background: rgba(26, 92, 191, 0.15);
}

@media (max-width: 1200px) {
  .nav-menu {
    display: none;
  }
  .header-search {
    max-width: 300px;
  }
  .search-btn {
    min-width: 40px;
    padding: 0 12px;
  }
  .search-btn-text {
    display: none;
  }
  .action-btn span:last-child {
    display: none;
  }
}
</style>

<style>
/* 学段资源下拉（挂载到 body，需全局样式） */
.stage-nav-dropdown.el-popper {
  min-width: 120px;
}

.stage-nav-dropdown .el-dropdown-menu {
  min-width: 120px;
  padding: 6px 0;
}

.stage-nav-dropdown .el-dropdown-menu__item {
  min-width: 128px;
  padding: 10px 24px;
  font-size: 14px;
  letter-spacing: 0.12em;
  text-align: center;
  justify-content: center;
}

/* 特色频道 Mega 面板 */
.feature-channel-mega-dropdown.el-popper {
  padding: 0 !important;
  border: 1px solid var(--border-light, #e8ecf4) !important;
  border-radius: 16px !important;
  box-shadow: 0 16px 48px rgba(26, 46, 94, 0.14) !important;
  overflow: hidden;
}

.feature-channel-mega-dropdown .el-popper__arrow::before {
  border-color: var(--border-light, #e8ecf4) !important;
}

.feature-channel-mega-dropdown .el-dropdown-menu {
  display: none;
}

/* 用户头像下拉（与个人中心侧栏对齐） */
.user-nav-dropdown.el-popper {
  min-width: 168px;
  border-radius: 10px !important;
  border: 1px solid var(--border-light, #e8ecf4) !important;
  box-shadow: 0 8px 24px rgba(26, 46, 94, 0.1) !important;
}

.user-nav-dropdown .el-dropdown-menu {
  min-width: 168px;
  padding: 6px;
}

.user-nav-dropdown .el-dropdown-menu__item {
  padding: 0;
  line-height: normal;
  border-radius: 6px;
}

.user-nav-dropdown .el-dropdown-menu__item .user-nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 12px;
  font-size: 14px;
  color: var(--text-regular, #606266);
}

.user-nav-dropdown .el-dropdown-menu__item:hover .user-nav-item {
  color: var(--color-primary, #409eff);
}

.user-nav-dropdown .user-nav-icon {
  width: 20px;
  text-align: center;
  flex-shrink: 0;
  font-size: 15px;
  line-height: 1;
}

.user-nav-dropdown .user-nav-label {
  flex: 1;
  font-weight: 500;
}

.user-nav-dropdown .el-dropdown-menu__item.user-nav-item--danger .user-nav-item {
  color: #e53935;
}

.user-nav-dropdown .el-dropdown-menu__item.user-nav-item--danger:hover .user-nav-item {
  color: #c62828;
}
</style>

