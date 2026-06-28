from pathlib import Path

p = Path(__file__).resolve().parents[1] / 'src/views/resource/SubjectDetailPage.vue'
text = p.read_text(encoding='utf-8')

if "useSubjectPageState" not in text:
    text = text.replace(
        "import { useApiResources } from '../../composables/useApiResources'",
        "import { useApiResources } from '../../composables/useApiResources'\nimport { useSubjectPageState } from '@/composables/useSubjectPageState'",
    )

old_layout = """// ===== 栏目布局类型判断 =====
// Layout A（考试类）: 开学专区、月考、期中、期末、小升初真题、小升初模拟
const examColumns = ['开学专区', '月考', '期中', '期末', '小升初真题', '小升初模拟']
// Layout B（专题类）: 专题复习、真题汇编、暑假、寒假、作文、阅读、竞赛
const topicColumns = ['专题复习', '真题汇编', '暑假', '寒假', '作文', '阅读', '竞赛']

const columnLayout = computed(() => {
  if (examColumns.includes(activeColumn.value)) return 'exam'
  if (topicColumns.includes(activeColumn.value)) return 'topic'
  return 'default' // 同步备课等保留原有双栏布局
})

"""

if old_layout in text:
    text = text.replace(old_layout, '')

marker = '// ===== 内联考试/专题布局的本地状态'
if marker in text:
    i0 = text.index(marker)
    i2 = text.index('const {\n  apiResources,', i0)
    text = text[:i0] + text[i2:]

anchor = "} = useApiResources("
idx = text.index(anchor)
# find closing paren of useApiResources call
j = text.index(')', idx)
# find end of destructuring
k = text.index(')', text.index('} = useApiResources', idx) + 20)
# simpler: after `) \n\n// 成套资源`
insert_point = text.index('// 成套资源数据', idx)
insert = """
const pageState = useSubjectPageState(activeColumn, apiResources, apiTotal)
const {
  gradeList, selectedGrade, semesterList, selectedSemester, yearList, selectedYear,
  versionDisplayList, versionMoreList, selectedVersion, showVersionMore,
  examTypeList, selectedType, onlyQuality, onlyAnswered, onlyTextVersion,
  regionDisplayList, regionMoreList, selectedRegion, showRegionMore,
  demoTopicListData, suiteRecommendList, topicDisplayTotal, topicCardData,
  sidebarTopics, topicLinks, showGradeFilter,
  getTopicCoverColor, getSubjectLabel, getTypeLabel, getDocIconClass, getDocIconLetter,
  getDocIconClassFull, columnLayout,
} = pageState

"""
if 'pageState = useSubjectPageState' not in text:
    text = text[:insert_point] + insert + text[insert_point:]

p.write_text(text, encoding='utf-8')
print('patched subject')
