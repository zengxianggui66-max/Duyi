import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import {
  subjectConfig, gradeLevelNames, gradeLevels,
  getSubjectsByGrade, getResourceTypesByGrade, getVersionsByGrade, getExamTypesByGrade,
  primaryGradeKeys, primaryGradeNames, primaryGradeEmojis, primaryGradeColors, primaryGradeDescs,
  primaryZoneCards, isPrimaryGrade, isLowGrade,
  juniorGradeKeys, juniorGradeNames, juniorGradeEmojis, juniorGradeColors, juniorGradeDescs,
  seniorGradeKeys, seniorGradeNames, seniorGradeEmojis, seniorGradeColors, seniorGradeDescs,
  juniorZoneCards, seniorZoneCards,
} from '@/constants'
import { useResourceStore, useSearchStore } from '@/store'
import { eduResourceApi } from '@/api'
import { formatCount } from '@/constants/utils'
import { getResourceIcon, getSubjectName, getTypeName, getVersionName, getExamTypeName, getCoverColor } from '@/utils/resourceDisplay'

export function useGradePage() {
  const route = useRoute()
  const router = useRouter()
  const resourceStore = useResourceStore()
  const searchStore = useSearchStore()

  // ===================== 年级/学段计算 =====================
  const gradeLevel = computed(() => (route.params.level as string) || 'primary')

  /** 当前学段（primary=小学, junior=初中, senior=高中, art=美术, dance=舞蹈） */
  const currentSection = computed(() => {
  const level = gradeLevel.value
  // 检查是否是具体年级
  if (isPrimaryGrade(level)) return 'primary'
  if (juniorGradeKeys.includes(level as any)) return 'junior'
  if (seniorGradeKeys.includes(level as any)) return 'senior'
  // 检查是否是学段名称
  if (level === 'primary' || level === 'junior' || level === 'senior' || level === 'art' || level === 'dance') return level
  return 'primary'
  })

  /** 是否是小学学段（小学专区或小学年级页） */
  const isPrimarySection = computed(() => currentSection.value === 'primary')
  /** 是否是初中学段（初中专区或初中年级页） */
  const isJuniorSection = computed(() => currentSection.value === 'junior')
  /** 是否是高中学段（高中专区或高中年级页） */
  const isSeniorSection = computed(() => currentSection.value === 'senior')
  /** 是否是美术专区 */
  const isArtSection = computed(() => currentSection.value === 'art')
  /** 是否是舞蹈专区 */
  const isDanceSection = computed(() => currentSection.value === 'dance')

  /** 当前学段配置（合并keys/names/emojis/colors/descs避免重复条件分支） */
  const sectionConfig = computed(() => {
    if (isPrimarySection.value) return { keys: primaryGradeKeys, names: primaryGradeNames, emojis: primaryGradeEmojis, colors: primaryGradeColors, descs: primaryGradeDescs }
    if (isJuniorSection.value) return { keys: juniorGradeKeys, names: juniorGradeNames, emojis: juniorGradeEmojis, colors: juniorGradeColors, descs: juniorGradeDescs }
    if (isSeniorSection.value) return { keys: seniorGradeKeys, names: seniorGradeNames, emojis: seniorGradeEmojis, colors: seniorGradeColors, descs: seniorGradeDescs }
    return { keys: primaryGradeKeys, names: primaryGradeNames, emojis: primaryGradeEmojis, colors: primaryGradeColors, descs: primaryGradeDescs }
  })

  const currentGradeKeys = computed(() => sectionConfig.value.keys)
  const currentGradeNames = computed(() => sectionConfig.value.names)
  const currentGradeEmojis = computed(() => sectionConfig.value.emojis)
  const currentGradeColors = computed(() => sectionConfig.value.colors)
  const currentGradeDescs = computed(() => sectionConfig.value.descs)

  /** 显示名称：grade1 → 一年级；primary → 小学 */
  const gradeName = computed(() => {
  if (isArtSection.value) return '美术'
  if (isDanceSection.value) return '舞蹈'
  if (isPrimarySection.value) return primaryGradeNames[gradeLevel.value] || '小学'
  if (isJuniorSection.value) return juniorGradeNames[gradeLevel.value] || '初中'
  if (isSeniorSection.value) return seniorGradeNames[gradeLevel.value] || '高中'
  return gradeLevelNames[gradeLevel.value] || '小学'
  })

  const gradeEmoji = computed(() => {
  if (isArtSection.value) return '🎨'
  if (isDanceSection.value) return '💃'
  if (isPrimarySection.value) return primaryGradeEmojis[gradeLevel.value] || '🌈'
  if (isJuniorSection.value) return juniorGradeEmojis[gradeLevel.value] || '📘'
  if (isSeniorSection.value) return seniorGradeEmojis[gradeLevel.value] || '🎓'
  return currentGradeEmojis.value[gradeLevel.value] || '📚'
  })

  const gradeDesc = computed(() => {
  if (isPrimarySection.value) return primaryGradeDescs[gradeLevel.value] || ''
  if (isJuniorSection.value) return juniorGradeDescs[gradeLevel.value] || ''
  if (isSeniorSection.value) return seniorGradeDescs[gradeLevel.value] || ''
  return currentGradeDescs.value[gradeLevel.value] || ''
  })

  /** Banner 动态背景 */
  const bannerStyle = computed(() => {
  if (isPrimarySection.value) {
    return { background: primaryGradeColors[gradeLevel.value] || 'linear-gradient(135deg,#FF6B6B,#ee5a24)' }
  }
  if (isJuniorSection.value) {
    return { background: juniorGradeColors[gradeLevel.value] || 'linear-gradient(135deg, #4ECDC4, #44bd9e)' }
  }
  if (isSeniorSection.value) {
    return { background: seniorGradeColors[gradeLevel.value] || 'linear-gradient(135deg, #5B7CF9, #4a6cf7)' }
  }
  const colorMap: Record<string, string> = {
    primary: 'linear-gradient(135deg, #FF6B6B, #ee5a24)',
    junior:  'linear-gradient(135deg, #4ECDC4, #44bd9e)',
    senior:  'linear-gradient(135deg, #45B7D1, #6C5CE7)',
    art:     'linear-gradient(135deg, #FF9A9E, #FECFEF)',
    dance:   'linear-gradient(135deg, #A18CD1, #FBC2EB)',
  }
  return { background: colorMap[gradeLevel.value] || colorMap.primary }
})

// ===================== 专区卡片（低/高年级适配） =====================
const visibleZoneCards = computed(() => {
  if (isPrimarySection.value) {
    const isLow = isLowGrade(gradeLevel.value)
    return isLow
      ? primaryZoneCards.filter(c => c.grades !== 'high')
      : primaryZoneCards
  }
  if (isJuniorSection.value) return juniorZoneCards
  if (isSeniorSection.value) return seniorZoneCards
  return []
})

/** 点击专区卡片 → 快速关键词搜索 */
function quickFilterZone(zoneKey: string) {
  const keywordMap: Record<string, string> = {
    class_meeting: '班会', tradition: '传统文化', art_sports: '艺体',
    science_innovation: 'STEAM', career: '生涯规划', upgrade: '小升初',
    class_meeting_j: '班会', zhongkao: '中考备考', tradition_j: '传统文化',
    art_sports_j: '艺体', competition_j: '学科竞赛', career_j: '生涯规划',
    class_meeting_s: '班会', gaokao: '高考备考', tradition_s: '传统文化',
    art_sports_s: '艺体', competition_s: '学科竞赛', career_s: '生涯规划',
  }
  searchKeyword.value = keywordMap[zoneKey] || ''
  handleSearch()
}

// ===================== 年级选择 =====================
/** 是否选择了具体年级（非学段级别） */
const isSpecificGrade = computed(() => {
  return activeGrade.value !== ''
})

/** 当前选中的年级（用于学段页显示年级选择） */
const activeGrade = ref('')

/** 选择年级 */
function selectGrade(key: string) {
  activeGrade.value = key
  // 重置学科和册别选择
  activeSubject.value = ''
  selectedVersion.value = ''
  selectedCategory.value = ''
  selectedPaper.value = ''
  selectedKnowledge.value = ''
}

// ===================== 学科 =====================
/** 年级页统一使用对应学段的学科列表 */
const apiGradeLevel = computed(() => {
  if (isPrimarySection.value) return 'primary'
  if (isJuniorSection.value) return 'junior'
  if (isSeniorSection.value) return 'senior'
  if (isArtSection.value) return 'art'
  if (isDanceSection.value) return 'dance'
  return gradeLevel.value
})
const gradeSubjects = computed(() => getSubjectsByGrade(apiGradeLevel.value))

/** 传给后端的学段中文名（小学/初中/高中），不要用具体年级名 */
const apiStageName = computed(() => {
  if (isPrimarySection.value) return '小学'
  if (isJuniorSection.value) return '初中'
  if (isSeniorSection.value) return '高中'
  if (isArtSection.value) return '美术'
  if (isDanceSection.value) return '舞蹈'
  return '小学'
})

/** 侧边栏学科：API 优先，失败时回退本地配置 */
const sidebarSubjects = computed(() => {
  if (apiSubjects.value.length > 0) {
    return apiSubjects.value.map((item) => {
      const localSub = gradeSubjects.value.find((s) => s.name === item.name)
      return { id: item.id, name: item.name, key: localSub?.key || String(item.id) }
    })
  }
  return gradeSubjects.value.map((s, i) => ({ id: i + 1, name: s.name, key: s.key }))
})

const activeSubjectName = computed(() => {
  if (!activeSubject.value) return ''
  const configMatch = gradeSubjects.value.find((s) => s.key === activeSubject.value)
  if (configMatch) return configMatch.name
  const apiMatch = apiSubjects.value.find((s) => String(s.id) === activeSubject.value)
  return apiMatch?.name || ''
})

// ===================== 顶部标签 =====================
// 普通学段标签
const normalTopTabs = [
  { key: 'sync', name: '同步' },
  { key: 'upgrade', name: '小升初' },
  { key: 'knowledge', name: '知识点' },
  { key: 'homework', name: '作业' },
  { key: 'paper', name: '试卷' },
  { key: 'composition', name: '作文' },
  { key: 'quality', name: '精品' },
  { key: 'school', name: '名校' },
  { key: 'teaching', name: '教辅' },
  { key: 'research', name: '研修' },
  { key: 'prepare', name: '备课' },
  { key: 'compose', name: '组卷' },
]
// 美术专区标签
const artTopTabs = [
  { key: 'exam', name: '考级备考' },
  { key: 'creative', name: '儿童创意' },
  { key: 'basics', name: '少儿基础' },
  { key: 'sketch', name: '素描' },
  { key: 'color', name: '色彩' },
  { key: 'cartoon', name: '动漫' },
  { key: 'painting', name: '国画' },
  { key: 'calligraphy', name: '书法' },
  { key: 'handcraft', name: '手工' },
  { key: 'art_exam', name: '艺考' },
]
// 舞蹈专区标签
const danceTopTabs = [
  { key: 'exam', name: '考级备考' },
  { key: 'chinese', name: '中国舞' },
  { key: 'folk', name: '民族舞' },
  { key: 'classical', name: '古典舞' },
  { key: 'latin', name: '拉丁舞' },
  { key: 'ballroom', name: '摩登舞' },
  { key: 'street', name: '街舞' },
  { key: 'ballet', name: '芭蕾' },
  { key: 'dance_exam', name: '艺考' },
]

const topTabs = computed(() => {
  if (isArtSection.value) return artTopTabs
  if (isDanceSection.value) return danceTopTabs
  return normalTopTabs
})
const activeTopTab = ref('sync')

watch(
  () => currentSection.value,
  (section) => {
    activeTopTab.value = section === 'art' || section === 'dance' ? 'exam' : 'sync'
  },
  { immediate: true },
)

// ===================== 搜索框占位符 =====================
const searchPlaceholder = computed(() => {
  if (isPrimarySection.value) return `搜索${gradeName.value}资源...`
  return '搜索资源...'
})

// ===================== 筛选状态 =====================
const activeSubject = ref('')
const activeResourceType = ref('')
const activeVersion = ref('')
const activeExamType = ref('')
const searchKeyword = ref('')
  const activeSort = ref('downloadCount')
  const currentPage = ref(1)
  const selectedVersion = ref('')
  const selectedCategory = ref('')
  const selectedPaper = ref('')
  const selectedKnowledge = ref('')
  const showClassifyBar = ref(false)
  const selectedQuickTag = ref('')
  const activeTab = ref('')

// ===================== API 动态数据 =====================
// 从后端 API 获取的动态数据
const apiSubjects = ref<{ id: number; name: string }[]>([])
const apiModules = ref<{ id: number; name: string; icon?: string }[]>([])
const apiResourceTypes = ref<{ id: number; name: string; icon?: string }[]>([])
const apiEditions = ref<{ id: number; name: string }[]>([])
const apiGrades = ref<{ id: number; name: string }[]>([])
const isLoading = ref(false)

// 加载学段筛选数据
async function loadGradeFilters() {
  const stageName = apiStageName.value
  if (!stageName) return

  isLoading.value = true
  try {
    const res = await eduResourceApi.getFilterOptions(stageName)
    if (res.data.code === 200 && res.data.data) {
      const data = res.data.data
      apiSubjects.value = data.subjects || []
      apiModules.value = data.modules || []
      apiResourceTypes.value = data.resourceTypes || []
      apiEditions.value = data.editions || []
      apiGrades.value = data.grades || []
    }
  } catch (error) {
    console.error('加载筛选数据失败:', error)
    apiSubjects.value = []
    apiModules.value = []
    apiResourceTypes.value = []
    apiEditions.value = []
    apiGrades.value = []
  } finally {
    isLoading.value = false
  }
}

// 监听学段变化，自动加载数据
watch(() => gradeLevel.value, () => {
  loadGradeFilters()
}, { immediate: true })

// 监听学科变化，重新加载栏目和版本
watch(() => activeSubject.value, () => {
  if (activeSubject.value) {
    const stageName = apiStageName.value
    if (stageName) {
      eduResourceApi.getFilterOptions(stageName, activeSubjectName.value).then((res) => {
        if (res.data.code === 200 && res.data.data) {
          const data = res.data.data
          apiModules.value = data.modules || []
          apiEditions.value = data.editions || []
        }
      }).catch(() => {})
    }
  }
})

// 根据 API 数据获取版本列表，同时从本地配置补充 key（用于导航）
const currentVersions = computed(() => {
  const allLocalVersions = getVersionsByGrade(apiGradeLevel.value)
  if (apiEditions.value.length > 0) {
    return apiEditions.value.map((item) => {
      const localMatch = allLocalVersions.find((v) => v.name === item.name)
      return { key: localMatch?.key || String(item.id), name: item.name }
    })
  }
  return allLocalVersions.map((v) => ({ key: v.key, name: v.name }))
})

// 获取栏目列表（使用 API 数据，包含 icon）
const currentCategories = computed(() => {
  return apiModules.value.map(item => ({ key: String(item.id), name: item.name, icon: item.icon }))
})

// 获取资源类型列表（使用 API 数据，包含 icon）
const currentResourceTypes = computed(() => {
  return apiResourceTypes.value.map(item => ({ key: String(item.id), name: item.name, icon: item.icon }))
})

// 获取试卷列表（使用 API 数据，栏目中筛选）
const currentExamPapers = computed(() => {
  return apiModules.value.map(item => ({ key: String(item.id), name: item.name }))
})

// 获取知识点列表（使用 API 数据）
const currentKnowledgePoints = computed(() => {
  return apiModules.value.map(item => ({ key: String(item.id), name: item.name }))
})

// 面包屑导航数据
const breadcrumbItems = computed(() => {
  const items = [
    { name: '学科网', clickable: true, action: 'home' },
  ]

  // 根据学段添加学段
  if (isPrimarySection.value) {
    items.push({ name: '小学', clickable: true, action: 'section' })
  } else if (isJuniorSection.value) {
    items.push({ name: '初中', clickable: true, action: 'section' })
  } else if (isSeniorSection.value) {
    items.push({ name: '高中', clickable: true, action: 'section' })
  } else if (isArtSection.value) {
    items.push({ name: '美术', clickable: true, action: 'section' })
  } else if (isDanceSection.value) {
    items.push({ name: '舞蹈', clickable: true, action: 'section' })
  }

  // 添加当前学科
  if (activeSubject.value) {
    const subjectName = gradeSubjects.value.find(s => s.key === activeSubject.value)?.name || ''
    items.push({ name: subjectName, clickable: true, action: 'subject' })
  }

  // 添加当前标签页
  const tabName = topTabs.value.find((t) => t.key === activeTopTab.value)?.name || '同步'
  items.push({ name: `教材${tabName}`, clickable: true, action: 'tab' })

  // 添加当前版本
  if (selectedVersion.value && currentVersions.value.length > 0) {
    const versionName = currentVersions.value.find(v => v.key === selectedVersion.value)?.name || ''
    if (versionName) {
      items.push({ name: versionName, clickable: true, action: 'version' })
    }
  }

  // 添加当前年级册次
  items.push({ name: `${gradeName.value}${getGradeSemester()}`, clickable: false, action: '' })

  return items
})

// 获取年级册次
function getGradeSemester(): string {
  return '上册' // 默认显示上册，后续可扩展
}

// 面包屑点击处理
function handleBreadcrumbClick(crumb: any) {
  switch (crumb.action) {
    case 'home':
      // 返回首页
      break
    case 'section':
      // 返回学段页
      break
    case 'subject':
      // 返回学科列表
      activeSubject.value = ''
      selectedVersion.value = ''
      break
    case 'tab':
      // 返回标签页
      break
    case 'version':
      // 返回版本选择
      selectedVersion.value = ''
      break
  }
}

const quickTags = computed(() => {
  const map: Record<string, any[]> = {
    knowledge: [
      { key: 'chinese', name: '语文' },
      { key: 'math', name: '数学' },
      { key: 'english', name: '英语' },
      { key: 'physics', name: '物理' },
      { key: 'chemistry', name: '化学' },
    ],
    homework: [
      { key: 'daily', name: '每日作业' },
      { key: 'weekend', name: '周末作业' },
      { key: 'vacation', name: '假期作业' },
    ],
    paper: [
      { key: 'select', name: '选题' },
      { key: 'download', name: '下载' },
      { key: 'collect', name: '收藏' },
    ],
    composition: [
      { key: 'primary', name: '小学作文' },
      { key: 'junior', name: '初中作文' },
      { key: 'senior', name: '高中作文' },
    ],
    quality: [
      { key: 'hot', name: '热门推荐' },
      { key: 'new', name: '最新上传' },
      { key: 'vip', name: '会员专享' },
    ],
    school: [
      { key: 'beijing', name: '北京名校' },
      { key: 'shanghai', name: '上海名校' },
      { key: 'guangzhou', name: '广州名校' },
    ],
    book: [
      { key: 'textbook', name: '教材' },
      { key: 'workbook', name: '练习册' },
      { key: 'guide', name: '教参' },
    ],
  }
  return map[activeTab.value] || []
})

// ===================== 排序 =====================
const sortOptions = [
  { label: '🔥 下载最多', value: 'downloadCount', field: 'downloadCount', order: 'desc' },
  { label: '🆕 最新上传', value: 'createTime',    field: 'createTime',    order: 'desc' },
  { label: '👁 浏览最多', value: 'viewCount',      field: 'viewCount',     order: 'desc' },
  { label: '⭐ 好评优先', value: 'rating',          field: 'rating',        order: 'desc' },
  { label: '📥 收藏最多', value: 'collectCount',    field: 'collectCount',  order: 'desc' },
]

function getSortParams() {
  const sort = sortOptions.find(s => s.value === activeSort.value)
  return { sortField: (sort?.field || 'downloadCount') as 'createTime' | 'downloadCount' | 'viewCount' | 'rating' | 'collectCount', sortOrder: (sort?.order || 'desc') as 'desc' | 'asc' }
}

// ===================== 年级切换 =====================
function switchGrade(key: string) {
  router.push(`/grade/${key}`)
}

// ===================== 资源类型分组 =====================
const resourceTypeGroups = computed(() => {
  const level = apiGradeLevel.value
  const items = getResourceTypesByGrade(level)
  if (['primary', 'junior', 'senior'].includes(level)) {
    const paper   = items.filter(i => i.key.startsWith('paper_'))
    const media   = items.filter(i => i.key.startsWith('media_'))
    const doc     = items.filter(i => i.key.startsWith('doc_'))
    const digital = items.filter(i => i.key.startsWith('digital_'))
    const ext     = items.filter(i => i.key.startsWith('ext_'))
    return [
      paper.length   ? { label: '📄 纸质类',     items: paper.map(i => ({ label: i.name, value: i.key })) }   : null,
      media.length   ? { label: '🎬 音视频类',   items: media.map(i => ({ label: i.name, value: i.key })) }   : null,
      doc.length     ? { label: '📊 文档类',     items: doc.map(i => ({ label: i.name, value: i.key })) }     : null,
      digital.length ? { label: '💻 数字化工具', items: digital.map(i => ({ label: i.name, value: i.key })) } : null,
      ext.length     ? { label: '📦 拓展资源',   items: ext.map(i => ({ label: i.name, value: i.key })) }     : null,
    ].filter(Boolean) as { label: string; items: { label: string; value: string }[] }[]
  }
  if (level === 'art') {
    const paper   = items.filter(i => ['art_textbook','art_copybook','art_template','art_workbook','art_form','art_exam'].some(p => i.key.startsWith(p)))
    const media   = items.filter(i => i.key.startsWith('art_video'))
    const doc     = items.filter(i => i.key.startsWith('art_doc'))
    const digital = items.filter(i => i.key.startsWith('art_digital'))
    const ext     = items.filter(i => i.key.startsWith('art_ext'))
    return [
      paper.length   ? { label: '📄 纸质类',     items: paper.map(i => ({ label: i.name, value: i.key })) }   : null,
      media.length   ? { label: '🎬 视频/素材',  items: media.map(i => ({ label: i.name, value: i.key })) }   : null,
      doc.length     ? { label: '📊 文档类',     items: doc.map(i => ({ label: i.name, value: i.key })) }     : null,
      digital.length ? { label: '💻 数字化工具', items: digital.map(i => ({ label: i.name, value: i.key })) } : null,
      ext.length     ? { label: '📦 拓展资源',   items: ext.map(i => ({ label: i.name, value: i.key })) }     : null,
    ].filter(Boolean) as { label: string; items: { label: string; value: string }[] }[]
  }
  if (level === 'dance') {
    const paper   = items.filter(i => i.key.startsWith('dance_paper'))
    const media   = items.filter(i => i.key.startsWith('dance_video'))
    const doc     = items.filter(i => i.key.startsWith('dance_doc'))
    const digital = items.filter(i => i.key.startsWith('dance_digital'))
    const ext     = items.filter(i => i.key.startsWith('dance_ext'))
    return [
      paper.length   ? { label: '📄 纸质类',     items: paper.map(i => ({ label: i.name, value: i.key })) }   : null,
      media.length   ? { label: '🎵 音乐/视频',  items: media.map(i => ({ label: i.name, value: i.key })) }   : null,
      doc.length     ? { label: '📊 文档类',     items: doc.map(i => ({ label: i.name, value: i.key })) }     : null,
      digital.length ? { label: '💻 数字化工具', items: digital.map(i => ({ label: i.name, value: i.key })) } : null,
      ext.length     ? { label: '📦 拓展资源',   items: ext.map(i => ({ label: i.name, value: i.key })) }     : null,
    ].filter(Boolean) as { label: string; items: { label: string; value: string }[] }[]
  }
  return []
})

// ===================== 教材版本分组 =====================
const textVersionGroups = computed(() => {
  const level = apiGradeLevel.value
  const items = getVersionsByGrade(level)
  if (['primary', 'junior', 'senior'].includes(level)) {
    const domesticKeys = ['pep_primary','pep_junior','pep_senior','bsd_primary','bsd_junior','suke','suke_junior','waiyan_primary','waiyan_junior','waiyan_senior','hujiao','xiangjiao','xiangjiao_senior','lujiao','lujiao_senior']
    const intlKeys = ['oxford_discover','oxford_discover_j','power_up','get_smart','think','think_senior','unlock','unlock_senior','treasures','national_geo','ielts_toefl']
    const domestic = items.filter(i => domesticKeys.includes(i.key))
    const intl = items.filter(i => intlKeys.includes(i.key))
    const custom = items.filter(i => !domesticKeys.includes(i.key) && !intlKeys.includes(i.key))
    return [
      domestic.length ? { label: '🏫 国内课标版', items: domestic.map(i => ({ label: i.name, value: i.key })) } : null,
      intl.length     ? { label: '🌍 国际原版',   items: intl.map(i => ({ label: i.name, value: i.key })) }     : null,
      custom.length   ? { label: '🎯 机构定制',   items: custom.map(i => ({ label: i.name, value: i.key })) }   : null,
    ].filter(Boolean) as { label: string; items: { label: string; value: string }[] }[]
  }
  return []
})

// ===================== 备考类型分组 =====================
const examTypeGroups = computed(() => {
  const level = apiGradeLevel.value
  const items = getExamTypesByGrade(level)
  if (['primary','junior','senior'].includes(level)) {
    const sync     = items.filter(i => i.key.startsWith('sync_'))
    const entrance = items.filter(i => i.key.startsWith('entrance_'))
    const cert     = items.filter(i => i.key.startsWith('cert_'))
    return [
      sync.length     ? { label: '📝 校内同步', items: sync.map(i => ({ label: i.name, value: i.key })) }     : null,
      entrance.length ? { label: '🎓 升学备考', items: entrance.map(i => ({ label: i.name, value: i.key })) } : null,
      cert.length     ? { label: '🏆 能力认证', items: cert.map(i => ({ label: i.name, value: i.key })) }     : null,
    ].filter(Boolean) as { label: string; items: { label: string; value: string }[] }[]
  }
  if (level === 'art') {
    const exam   = items.filter(i => i.key.includes('level') || i.key.includes('exam_prep'))
    const talent = items.filter(i => i.key.includes('talent') || i.key.includes('quality'))
    return [
      exam.length   ? { label: '🏅 测评/考级', items: exam.map(i => ({ label: i.name, value: i.key })) }   : null,
      talent.length ? { label: '⭐ 艺术特长', items: talent.map(i => ({ label: i.name, value: i.key })) } : null,
    ].filter(Boolean) as { label: string; items: { label: string; value: string }[] }[]
  }
  if (level === 'dance') {
    const exam        = items.filter(i => i.key.includes('level') || i.key.includes('exam_basics'))
    const talent      = items.filter(i => i.key.includes('talent'))
    const competition = items.filter(i => i.key.includes('competition'))
    return [
      exam.length        ? { label: '🏅 测评/考级', items: exam.map(i => ({ label: i.name, value: i.key })) }        : null,
      talent.length      ? { label: '⭐ 艺术特长', items: talent.map(i => ({ label: i.name, value: i.key })) }      : null,
      competition.length ? { label: '🏆 比赛备赛', items: competition.map(i => ({ label: i.name, value: i.key })) } : null,
    ].filter(Boolean) as { label: string; items: { label: string; value: string }[] }[]
  }
  return []
})

// ===================== 数据加载 =====================
async function loadResources() {
  const sortParams = getSortParams()
  await resourceStore.fetchList({
    gradeLevel: apiGradeLevel.value,
    // 小学年级页额外传 gradeNo（如 1/2/3/4/5/6），后端可按此筛选
    gradeNo: isPrimarySection.value ? gradeLevel.value.replace('grade', '') : undefined,
    subject: activeSubject.value || undefined,
    resourceType: activeResourceType.value || undefined,
    textbookVersion: activeVersion.value || undefined,
    examType: activeExamType.value || undefined,
    keyword: searchKeyword.value || undefined,
    current: currentPage.value,
    size: 12,
    ...sortParams,
  })
}

onMounted(() => loadResources())

watch(
  [gradeLevel, activeSubject, activeResourceType, activeVersion, activeExamType, activeSort],
  () => { currentPage.value = 1; loadResources() }
)

// ===================== 资源展示映射 =====================
const displayResources = computed(() =>
  (resourceStore.list || []).map((r: any) => ({
    id: r.id,
    title: r.title || r.resourceName,
    icon: getResourceIcon(r.resourceType),
    subject: getSubjectName(r.subject),
    type: getTypeName(r.resourceType),
    version: getVersionName(r.textbookVersion),
    examType: getExamTypeName(r.examType),
    downloads: formatCount(r.downloadCount || 0),
    views: formatCount(r.viewCount || 0),
    pageCount: Math.floor(Math.random() * 80) + 10,
    author: r.author || '匿名',
    isFree: r.isFree !== false,
    coverColor: getCoverColor(r.id),
  }))
)

function toggleSubject(key: string) {
  if (activeSubject.value === key) {
    activeSubject.value = ''
    showClassifyBar.value = false
  } else {
    activeSubject.value = key
    showClassifyBar.value = true
  }
}

function resetFilters() {
  activeSubject.value = ''
  activeResourceType.value = ''
  activeVersion.value = ''
  activeExamType.value = ''
  searchKeyword.value = ''
}

function handleSearch() {
  currentPage.value = 1
  if (searchKeyword.value.trim()) searchStore.recordKeyword(searchKeyword.value.trim())
  loadResources()
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadResources()
}

// 跳转到版本资源页面
function goToVersion(versionKey: string, _versionName: string) {
  let subjectKey = activeSubject.value
  if (!subjectKey) {
    const firstSubject = gradeSubjects.value[0]
    if (firstSubject) subjectKey = firstSubject.key
  }

  const stage = apiGradeLevel.value
  // 美术/舞蹈学科详情页路由仅支持 art / dance 学科 key
  if (stage === 'art') subjectKey = 'art'
  if (stage === 'dance') subjectKey = 'dance'

  if (subjectKey) {
    router.push({
      name: 'SubjectDetail',
      params: { stage, subject: subjectKey, version: versionKey },
      query: { module: '同步备课' },
    })
  }
}
  return {
    route, router, resourceStore, Search,
    gradeLevel, currentSection, isPrimarySection, isJuniorSection, isSeniorSection,
    isArtSection, isDanceSection, currentGradeKeys, currentGradeNames, currentGradeEmojis,
    currentGradeColors, currentGradeDescs,
    gradeName, gradeEmoji, gradeDesc, bannerStyle, visibleZoneCards, quickFilterZone,
    isSpecificGrade, activeGrade, selectGrade, apiGradeLevel, gradeSubjects, activeSubjectName,
    topTabs, activeTopTab, searchPlaceholder, activeSubject, activeResourceType, searchKeyword,
    apiSubjects, sidebarSubjects, isLoading, currentVersions, currentCategories, currentResourceTypes,
    currentExamPapers, currentKnowledgePoints, selectedVersion, selectedCategory, selectedPaper,
    selectedKnowledge,     switchGrade, handleSearch, toggleSubject, goToVersion, handlePageChange,
    displayResources, formatCount, getCoverColor, getResourceIcon, getTypeName, loadResources,
    showClassifyBar, resourceTypeGroups, textVersionGroups, examTypeGroups, quickTags,
    breadcrumbItems, handleBreadcrumbClick, sortOptions, activeSort, currentPage,
    selectedQuickTag, activeTab,
  }
}



