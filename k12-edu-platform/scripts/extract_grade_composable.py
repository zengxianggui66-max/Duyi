from pathlib import Path

src = Path(__file__).resolve().parents[1] / 'src/views/grade/GradePage.vue'
lines = src.read_text(encoding='utf-8').splitlines()
script_lines = lines[398:1134]
filtered = []
started = False
for line in script_lines:
    if not started:
        if line.startswith('import '):
            continue
        if 'const route = useRoute()' in line:
            continue
        if 'const router = useRouter()' in line:
            continue
        if 'const resourceStore = useResourceStore()' in line:
            continue
        if line.strip() == '':
            continue
        started = True
    filtered.append(line)

body = '\n'.join(filtered)
if 'selectedVersion' not in body:
    body = body.replace(
        'const activeSort = ref',
        "const selectedVersion = ref('')\nconst selectedCategory = ref('')\nconst selectedPaper = ref('')\nconst selectedKnowledge = ref('')\nconst activeSort = ref",
    )

header = """import { ref, computed, onMounted, watch } from 'vue'
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
import { useResourceStore } from '@/store'
import api from '@/api'

export function useGradePage() {
  const route = useRoute()
  const router = useRouter()
  const resourceStore = useResourceStore()
"""

footer = """
  return {
    route, router, resourceStore, Search,
    gradeLevel, currentSection, isPrimarySection, isJuniorSection, isSeniorSection,
    isArtSection, isDanceSection, currentGradeKeys, currentGradeNames, currentGradeEmojis,
    gradeName, gradeEmoji, gradeDesc, bannerStyle, visibleZoneCards, quickFilterZone,
    isSpecificGrade, activeGrade, selectGrade, apiGradeLevel, gradeSubjects, activeSubjectName,
    topTabs, activeTopTab, searchPlaceholder, activeSubject, searchKeyword, apiSubjects,
    isLoading, currentVersions, currentCategories, currentResourceTypes, currentExamPapers,
    currentKnowledgePoints, selectedVersion, selectedCategory, selectedPaper, selectedKnowledge,
    switchGrade, handleSearch, toggleSubject, goToVersion,
  }
}
"""

out = Path(__file__).resolve().parents[1] / 'src/composables/useGradePage.ts'
out.write_text(header + body + footer, encoding='utf-8')
print(f'Wrote {len(out.read_text(encoding="utf-8").splitlines())} lines')
