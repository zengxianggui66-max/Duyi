import { ref, computed, watch } from 'vue'
import type { Ref, ComputedRef } from 'vue'
import {
  EXAM_COLUMNS,
  TOPIC_LAYOUT_COLUMNS,
  getExamTypeList,
  getRegionLists,
  resolveExamBrowseParams,
  usesSichuanRegions,
} from '@/constants/examColumnFilters'
import {
  getDefaultGradeForStage,
  getGradeListForStage,
  parseGradeSemesterFromVolumeName,
} from '@/config/volumeData'

export type SuppressDemoRef = Ref<boolean> | ComputedRef<boolean>

const examColumns: string[] = [...EXAM_COLUMNS]
const topicColumns: string[] = [...TOPIC_LAYOUT_COLUMNS]
/** 国学阅读·作文深度融合 */
const readingWritingColumns = ['国学阅读', '作文']
/** 七彩包：教师工作包 / 期中·期末复习包 → 分类列表布局 */
const packListColumns = ['教师工作包', '期中复习', '期末复习']

export function useSubjectPageState(
  activeColumn: Ref<string>,
  apiResources: Ref<any[]>,
  apiTotal: Ref<number>,
  currentSubject?: Ref<{ name?: string } | string | null>,
  suppressDemo?: SuppressDemoRef,
  searchKeyword?: Ref<string>,
  currentStage?: Ref<string>,
  currentGradeLevelName?: Ref<string>,
) {
  const _suppressDemo = computed(() => !!suppressDemo?.value)
  const columnLayout = computed(() => {
    if (examColumns.includes(activeColumn.value)) return 'exam'
    if (readingWritingColumns.includes(activeColumn.value)) return 'reading_writing'
    if (topicColumns.includes(activeColumn.value)) return 'topic'
    if (packListColumns.includes(activeColumn.value)) return 'category_list'
    return 'default'
  })

  const layoutUsesExamFilters = computed(
    () => columnLayout.value === 'exam' || columnLayout.value === 'topic',
  )

  // ===== 内联考试/专题布局的本地状态 =====
  const currentYear = new Date().getFullYear()
  const stageKey = computed(() => currentStage?.value || 'primary')
  const gradeList = computed(() => getGradeListForStage(stageKey.value))
  const selectedGrade = ref(getDefaultGradeForStage(stageKey.value))
  const semesterList = ['全部', '上学期', '下学期']
  const selectedSemester = ref('下学期')
  const yearList = ['全部', ...Array.from({ length: 6 }, (_, i) => String(currentYear - i)), '更早']
  const selectedYear = ref('全部')

  const versionAllList = [
    '全部',
    '通用',
    '统编版（2024）',
    '统编版（五四制）（2024）',
    '人教版（2024）',
    '北师大版（2024）',
    '苏教版（2024）',
    '部编版（2023）',
    '人教版（2023）',
  ]
  const versionDisplayList = ['全部', '通用', '统编版（2024）', '统编版（五四制）（2024）']
  const versionMoreList = computed(() => versionAllList.filter((v) => !versionDisplayList.includes(v)))
  const selectedVersion = ref('全部')
  const showVersionMore = ref(false)

  const examTypeList = computed(() => getExamTypeList(activeColumn.value as any))
  const selectedType = ref('全部')

  const regionConfig = computed(() => getRegionLists(activeColumn.value as any))
  const regionDisplayList = computed(() => regionConfig.value.display)
  const regionMoreList = computed(() => regionConfig.value.more)
  const showExamRegionFilter = computed(() => layoutUsesExamFilters.value)
  const selectedRegion = ref('全部')
  const showRegionMore = ref(false)

  const onlyQuality = ref(false)
  const onlyAnswered = ref(false)
  const onlyTextVersion = ref(false)

  function syncGradeSemesterFromVolume() {
    const parsed = parseGradeSemesterFromVolumeName(currentGradeLevelName?.value || '')
    if (!parsed) return
    if (gradeList.value.includes(parsed.grade)) {
      selectedGrade.value = parsed.grade
    }
    if (semesterList.includes(parsed.semester)) {
      selectedSemester.value = parsed.semester
    }
  }

  function resetGradeForStage(stage: string) {
    const defaultGrade = getDefaultGradeForStage(stage)
    if (gradeList.value.includes(defaultGrade)) {
      selectedGrade.value = defaultGrade
    } else if (gradeList.value.length) {
      selectedGrade.value = gradeList.value[0]
    }
    selectedSemester.value = stage === 'senior' ? '全部' : '下学期'
  }

  function resetExamFiltersForColumn(column: string) {
    selectedType.value = '全部'
    const { defaultRegion } = getRegionLists(column)
    selectedRegion.value = defaultRegion
    showRegionMore.value = false
    showVersionMore.value = false
  }

  watch(
    stageKey,
    (stage) => {
      resetGradeForStage(stage)
      syncGradeSemesterFromVolume()
    },
    { immediate: true },
  )

  watch(
    () => currentGradeLevelName?.value,
    () => {
      syncGradeSemesterFromVolume()
    },
    { immediate: true },
  )

  watch(
    activeColumn,
    (col) => {
      if (examColumns.includes(col) || topicColumns.includes(col)) {
        resetExamFiltersForColumn(col)
      }
    },
    { immediate: true },
  )

  const examBrowseParams = computed(() =>
    resolveExamBrowseParams({
      column: activeColumn.value as any,
      selectedType: selectedType.value,
      selectedRegion: selectedRegion.value,
      selectedVersion: selectedVersion.value,
      searchKeyword: searchKeyword?.value,
      layoutUsesExamFilters: layoutUsesExamFilters.value,
    }),
  )

  const topicDisplayTotal = computed(() => {
    if (apiTotal.value > 0) return apiTotal.value
    if (apiResources.value.length) return apiResources.value.length
    return 0
  })

  const topicCardData = ref([
    {
      title:
        '部编版五年级语文上册单元+期中+期末检测卷【（原卷版+解析版）A3+（答案解析课件）PPT】+同步练',
      subjectTag: '语文',
      typeTag: '课件 练习 试卷',
      count: 26,
      unit: '份',
      status: '已完结',
      views: 1879,
      downloads: 7,
      date: '2026-01-25',
      author: '文海拾贝',
      featured: true,
    },
    {
      title: '（含答案）部编版小升初语文模拟卷（全 5 套）',
      subjectTag: '语文',
      typeTag: '小升初模拟\n试卷',
      count: 5,
      unit: '套',
      status: '已完结',
      views: 514,
      downloads: 1,
      date: '2025-05-20',
      author: '中小学各科教研',
    },
    {
      title:
        '2025小升初语文考前模拟 （基础卷+提高卷+压轴卷）（A3A4版+答案）2024-2025统编版',
      subjectTag: '语文',
      typeTag: '冲刺卷模拟卷\n试卷',
      count: 12,
      unit: '份',
      status: '已完结',
      views: 1532,
      downloads: 12,
      date: '2025-05-15',
      author: '鹏城九九李老头',
    },
    {
      title:
        '2025小升初语文模考试基础卷+提高卷+压轴卷（A3A4原卷+答案与解释）2024-2025统编版',
      subjectTag: '语文',
      typeTag: '小升初模拟\n试卷',
      count: 39,
      unit: '份',
      status: '已完结',
      views: 4221,
      downloads: 59,
      date: '2025-04-28',
      author: '鹏城九九李老头',
    },
    {
      title:
        '2025小升初语文模拟试卷 基础卷+拔高卷（原卷+答案解析）2024-2025学年六年级 统编版',
      subjectTag: '语文',
      typeTag: '小升初模拟\n试卷',
      count: 3,
      unit: '份',
      status: '连载中',
      views: 722,
      downloads: 8,
      date: '2025-03-12',
      author: '鹏城九九李老头',
    },
    {
      title:
        '六年级下册（小升初语文）期末考试卷 模拟试卷 冲刺试卷（原卷+答案）2024统编版全国通用',
      subjectTag: '语文',
      typeTag: '期末冲刺卷',
      count: 25,
      unit: '份',
      status: '已完结',
      views: 5821,
      downloads: 123,
      date: '2024-06-05',
      author: '鹏城九九李老头',
      featured: true,
    },
  ])

  const topicCoverColors = [
    'linear-gradient(135deg,#FFF8E1,#FFE082)',
    'linear-gradient(135deg,#FFF3E0,#FFCC80)',
    'linear-gradient(135deg,#FCE4EC,#F48FB1)',
  ]
  function getTopicCoverColor(idx: number): string {
    return topicCoverColors[idx % topicCoverColors.length]
  }
  function getSubjectLabel(): string {
    const sub = currentSubject?.value
    if (sub && typeof sub === 'object' && 'name' in sub) return (sub as { name?: string }).name || '语文'
    return '语文'
  }
  function getTypeLabel(item: any): string {
    const t = item.type || ''
    if (t.includes('课件')) return '课件 练习 试卷'
    if (t.includes('模拟')) return '小升初模拟\n试卷'
    if (t.includes('期末') || t.includes('期中')) return '期末冲刺卷'
    if (t.includes('单元')) return '单元练习'
    return '试卷'
  }

  const showGradeFilter = computed(() => {
    return !['小升初真题', '小升初模拟'].includes(activeColumn.value)
  })

  const showSemesterFilter = computed(() => {
    return showGradeFilter.value && stageKey.value !== 'senior'
  })

  function getDocIconClass(item: any): string {
    const type = item.type || ''
    if (type.includes('试卷') || type.includes('真题')) return 'icon-pdf-box'
    if (type.includes('课件')) return 'icon-ppt-box'
    return 'icon-word-box'
  }
  function getDocIconLetter(item: any): string {
    const type = item.type || ''
    if (type.includes('试卷') || type.includes('真题')) return 'PDF'
    if (type.includes('课件')) return 'PPT'
    return 'W'
  }

  function getDocIconClassFull(item: any): string {
    const ext = (item.fileExt || item.file_ext || '').toLowerCase()
    if (['ppt', 'pptx'].includes(ext)) return 'icon-ppt-box'
    if (['pdf'].includes(ext)) return 'icon-pdf-box'
    return 'icon-word-box'
  }

  return {
    gradeList,
    selectedGrade,
    semesterList,
    selectedSemester,
    yearList,
    selectedYear,
    versionAllList,
    versionDisplayList,
    versionMoreList,
    selectedVersion,
    showVersionMore,
    examTypeList,
    selectedType,
    onlyQuality,
    onlyAnswered,
    onlyTextVersion,
    regionDisplayList,
    regionMoreList,
    selectedRegion,
    showRegionMore,
    showExamRegionFilter,
    examBrowseParams,
    layoutUsesExamFilters,
    usesSichuanRegions: computed(() => usesSichuanRegions(activeColumn.value)),
    topicDisplayTotal,
    topicCardData,
    _suppressDemo,
    topicCoverColors,
    showGradeFilter,
    showSemesterFilter,
    getTopicCoverColor,
    getSubjectLabel,
    getTypeLabel,
    getDocIconClass,
    getDocIconLetter,
    getDocIconClassFull,
    columnLayout,
    examColumns,
    topicColumns,
    readingWritingColumns,
  }
}

