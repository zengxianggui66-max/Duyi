from pathlib import Path

src = Path(__file__).resolve().parents[1] / 'src/views/resource/SubjectDetailPage.vue'
lines = src.read_text(encoding='utf-8').splitlines()
# exam/topic state block: lines 531-645 (1-indexed) -> 530-644
block = '\n'.join('  ' + l for l in lines[530:645])

header = """import { ref, computed } from 'vue'
import type { Ref, ComputedRef } from 'vue'

export function useSubjectPageState(activeColumn: Ref<string>, apiResources: Ref<any[]>, apiTotal: Ref<number>) {
"""

footer = """
  return {
    gradeList, selectedGrade, semesterList, selectedSemester, yearList, selectedYear,
    versionAllList, versionDisplayList, versionMoreList, selectedVersion, showVersionMore,
    examTypeList, selectedType, onlyQuality, onlyAnswered, onlyTextVersion,
    regionAllList, regionDisplayList, regionMoreList, selectedRegion, showRegionMore,
    demoTopicListData, suiteRecommendList, topicDisplayTotal, topicCardData,
    sidebarTopics, topicLinks, topicCoverColors, showGradeFilter,
    getTopicCoverColor, getSubjectLabel, getTypeLabel, getDocIconClass, getDocIconLetter,
    getDocIconClassFull, columnLayout, examColumns, topicColumns,
  }
}
"""

out = Path(__file__).resolve().parents[1] / 'src/composables/useSubjectPageState.ts'
out.write_text(header + block + footer, encoding='utf-8')
print('lines', len(out.read_text(encoding='utf-8').splitlines()))
