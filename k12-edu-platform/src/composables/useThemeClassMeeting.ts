import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { browseApi } from '@/api/browse'
import { fetchChannelBootstrap } from '@/api/channel'
import { unwrapData } from '@/api/request'
import type { PrimaryChineseItem } from '@/api/types'
import {
  THEME_CLASS_MEETING_MODULE,
  CLASS_MEETING_GRADE_TABS,
  CLASS_MEETING_LATEST_TABS,
  CLASS_MEETING_SIDEBAR,
  CLASS_MEETING_THEME_GROUPS,
  THEME_TO_CATEGORY,
  SIDEBAR_TO_CATEGORY,
} from '@/constants/themeClassMeetingNav'

const CARD_BG_COLORS = [
  'linear-gradient(135deg, #667eea, #764ba2)',
  'linear-gradient(135deg, #f093fb, #f5576c)',
  'linear-gradient(135deg, #4facfe, #00f2fe)',
  'linear-gradient(135deg, #43e97b, #38f9d7)',
]

const HOT_THEME_CLASSES = ['card-1', 'card-2', 'card-3', 'card-4']

const FALLBACK_MENU = [
  { name: '首页', active: true },
  { name: '爱国教育', dropdownItems: ['学雷锋', '爱国主义', '革命传统', '勿忘国耻'] },
  {
    name: '安全教育',
    dropdownItems: ['用电安全', '食品安全', '消防安全', '交通安全', '校园安全', '网络安全', '生命健康', '游泳安全', '自然灾害', '疫情防控'],
    colClass: 'double-col',
  },
  {
    name: '德育教育',
    dropdownItems: ['感恩父母', '感恩老师', '学会感恩', '诚实守信', '文明礼仪', '人际交往', '遵纪守法', '团结友爱', '责任与担当'],
    colClass: 'double-col',
  },
  {
    name: '环保教育',
    dropdownItems: ['绿色家园', '节能减排', '节约用水', '变废为宝'],
    colClass: 'double-col',
  },
  {
    name: '心理健康',
    dropdownItems: ['自我认识', '青春期', '轻松考试', '快乐生活', '情绪管理', '冲刺高考'],
    colClass: 'double-col',
  },
  {
    name: '励志教育',
    dropdownItems: ['自信篇', '励志篇', '奋斗篇', '梦想篇'],
    colClass: 'double-col',
  },
  {
    name: '节日主题',
    dropdownItems: ['元旦', '春节', '元宵节', '妇女节', '清明节', '劳动节', '五四青年节', '端午节', '母亲节', '父亲节', '儿童节', '教师节', '国庆节', '重阳节', '中秋节', '植树节', '圣诞节'],
    colClass: 'triple-col',
  },
  {
    name: '更多主题',
    dropdownItems: ['家校共育', '开学第一天', '期中考试家长会', '期末考试家长会', '班级文化', '学习方法', '珍惜时间', '其他'],
    colClass: 'double-col',
  },
]

export interface MenuItem {
  name: string
  active?: boolean
  dropdownItems?: string[]
  colClass?: string
}

export interface DownloadCard {
  id: number
  imgText: string
  title: string
  duration: string
  downloads: number
}

export interface LatestItem {
  id: number
  title: string
  date: string
}

export interface HotTheme {
  name: string
  class: string
  keyword?: string
}

function mapDownloadItem(item: PrimaryChineseItem, idx: number): DownloadCard {
  return {
    id: item.id,
    imgText: item.title.slice(0, 10),
    title: item.title,
    duration: item.fileExt?.toUpperCase() || item.type || 'PPT',
    downloads: item.downloadCount ?? 0,
  }
}

function mapLatestItem(item: PrimaryChineseItem): LatestItem {
  const date = item.uploadTime?.slice(0, 10) || ''
  return { id: item.id, title: item.title, date }
}

function getCardBg(idx: number) {
  return CARD_BG_COLORS[idx % CARD_BG_COLORS.length]
}

export function useThemeClassMeeting() {
  const router = useRouter()
  const searchKeyword = ref('')
  const activeMenuIdx = ref(-1)
  const activeSidebar = ref('安全教育')
  const menuItems = ref<MenuItem[]>([...FALLBACK_MENU])
  const hotThemes = ref<HotTheme[]>([
    { name: '安全教育', class: 'card-1', keyword: '安全教育' },
    { name: '期末考试家长会', class: 'card-2', keyword: '期末考试家长会' },
    { name: '端午节主题班会', class: 'card-3', keyword: '端午节' },
    { name: '食品安全', class: 'card-4', keyword: '食品安全' },
  ])
  const bannerTitle = ref('安全教育主题班会')
  const downloadTabs = CLASS_MEETING_GRADE_TABS.map(({ key, name }) => ({ key, name }))
  const currentDownloadTab = ref('primary')
  const downloadLists = ref<Record<string, DownloadCard[]>>({})
  const latestTabs = CLASS_MEETING_LATEST_TABS.map(({ key, name }) => ({ key, name }))
  const currentLatestTab = ref('courseware')
  const latestLists = ref<Record<string, LatestItem[]>>({})
  const listLoading = ref(false)
  const tabKeywords = ref<Record<string, string>>({})

  const sidebarItems = CLASS_MEETING_SIDEBAR
  const themeGroups = CLASS_MEETING_THEME_GROUPS

  const currentDownloadList = computed(
    () => downloadLists.value[currentDownloadTab.value] || [],
  )
  const currentLatestList = computed(
    () => latestLists.value[currentLatestTab.value] || [],
  )

  function setActiveMenu(idx: number) {
    menuItems.value.forEach((item, i) => {
      item.active = i === idx
    })
  }

  function navigateToTheme(theme: string) {
    const category = THEME_TO_CATEGORY[theme]
    if (category) {
      router.push({ name: 'ClassMeetingCategory', params: { category }, query: { theme } })
    }
  }

  function handleSearch() {
    const q = searchKeyword.value.trim()
    if (!q) return
    router.push({
      name: 'ClassMeetingCategory',
      params: { category: '更多主题' },
      query: { theme: q },
    })
  }

  function handleDropdownClick(sub: string) {
    activeMenuIdx.value = -1
    navigateToTheme(sub)
  }

  function handleSidebarClick(name: string) {
    activeSidebar.value = name
    const category = SIDEBAR_TO_CATEGORY[name] || name
    router.push({ name: 'ClassMeetingCategory', params: { category } })
  }

  function handleThemeClick(name: string, keyword?: string) {
    navigateToTheme(keyword || name)
  }

  function handleBannerClick() {
    navigateToTheme('安全教育')
  }

  function handleDownloadCard(item: DownloadCard) {
    router.push(`/resource/${item.id}`)
  }

  function handleResourceClick(item: LatestItem) {
    router.push(`/resource/${item.id}`)
  }

  function handleTopicLink(link: string) {
    navigateToTheme(link)
  }

  async function loadChannelConfig() {
    try {
      const data = await fetchChannelBootstrap('banhui')
      if (!data) return

      tabKeywords.value = data.tabKeywords ?? {}

      const tabs = (data.mainTabs ?? []).filter((t) => t.key !== 'all' && t.key !== 'elite')
      if (tabs.length) {
        menuItems.value = [
          { name: '首页', active: true },
          ...tabs.map((t) => ({
            name: t.name,
            dropdownItems: tabKeywords.value[t.key]
              ? [tabKeywords.value[t.key]]
              : undefined,
          })),
        ]
      }

      const albums = data.eliteAlbums ?? []
      if (albums.length) {
        hotThemes.value = albums.slice(0, 4).map((a, i) => ({
          name: a.title,
          class: HOT_THEME_CLASSES[i % HOT_THEME_CLASSES.length],
          keyword: a.title,
        }))
        bannerTitle.value = albums[0]?.title || bannerTitle.value
      }
    } catch {
      /* 保留静态 fallback */
    }
  }

  async function loadDownloadList(tabKey: string) {
    const tab = CLASS_MEETING_GRADE_TABS.find((t) => t.key === tabKey)
    if (!tab) return
    listLoading.value = true
    try {
      const res = await browseApi.getPage(
        {
          module: THEME_CLASS_MEETING_MODULE,
          gradeName: tab.gradeName,
          current: 1,
          size: 4,
          sortField: 'downloadCount',
          sortOrder: 'desc',
        },
        { silentError: true },
      )
      const page = unwrapData(res)
      downloadLists.value[tabKey] = (page.records || []).map(mapDownloadItem)
    } catch {
      downloadLists.value[tabKey] = []
    } finally {
      listLoading.value = false
    }
  }

  async function loadLatestList(tabKey: string) {
    const tab = CLASS_MEETING_LATEST_TABS.find((t) => t.key === tabKey)
    if (!tab) return
    try {
      const res = await browseApi.getPage(
        {
          module: THEME_CLASS_MEETING_MODULE,
          displayType: tab.displayType,
          current: 1,
          size: 12,
          sortField: 'createTime',
          sortOrder: 'desc',
        },
        { silentError: true },
      )
      const page = unwrapData(res)
      latestLists.value[tabKey] = (page.records || []).map(mapLatestItem)
    } catch {
      latestLists.value[tabKey] = []
    }
  }

  watch(currentDownloadTab, (key) => {
    if (!downloadLists.value[key]?.length) loadDownloadList(key)
  })

  watch(currentLatestTab, (key) => {
    if (!latestLists.value[key]?.length) loadLatestList(key)
  })

  onMounted(async () => {
    await loadChannelConfig()
    await Promise.all([
      loadDownloadList(currentDownloadTab.value),
      loadLatestList(currentLatestTab.value),
    ])
  })

  return {
    searchKeyword,
    activeMenuIdx,
    menuItems,
    sidebarItems,
    activeSidebar,
    hotThemes,
    bannerTitle,
    downloadTabs,
    currentDownloadTab,
    currentDownloadList,
    latestTabs,
    currentLatestTab,
    currentLatestList,
    themeGroups,
    listLoading,
    setActiveMenu,
    handleSearch,
    handleDropdownClick,
    handleSidebarClick,
    handleThemeClick,
    handleBannerClick,
    handleDownloadCard,
    handleResourceClick,
    handleTopicLink,
    getCardBg,
  }
}
