from pathlib import Path

p = Path(__file__).resolve().parents[1] / 'src/views/grade/GradePage.vue'
text = p.read_text(encoding='utf-8')

# Replace nav bar section
nav_start = text.index('    <!-- ===================== 年级导航栏 ===================== -->')
nav_end = text.index('    <!-- ===================== 页面标题栏', nav_start)
nav_new = """    <GradeNavBar
      :grade-keys="currentGradeKeys"
      :grade-names="currentGradeNames"
      :grade-emojis="currentGradeEmojis"
      :grade-level="gradeLevel"
      v-model:search-keyword="searchKeyword"
      :search-placeholder="searchPlaceholder"
      @switch-grade="switchGrade"
      @search="handleSearch"
    />

"""
text = text[:nav_start] + nav_new + text[nav_end:]

# Replace sidebar
sb_start = text.index('      <!-- 左侧学科导航（使用 API 数据） -->')
sb_end = text.index('      <!-- 右侧内容区 -->', sb_start)
sb_new = """      <GradeSubjectSidebar
        :subjects="apiSubjects"
        :active-subject="activeSubject"
        @select="toggleSubject"
      />

"""
text = text[:sb_start] + sb_new + text[sb_end:]

s0 = text.index('<script setup lang="ts">')
s1 = text.index('</script>', s0)
new_script = """<script setup lang="ts">
import GradeNavBar from '@/components/grade/GradeNavBar.vue'
import GradeSubjectSidebar from '@/components/grade/GradeSubjectSidebar.vue'
import { useGradePage } from '@/composables/useGradePage'

const {
  gradeLevel, currentGradeKeys, currentGradeNames, currentGradeEmojis,
  gradeName, gradeEmoji, bannerStyle, searchKeyword, searchPlaceholder,
  apiSubjects, activeSubject, isArtSection, isDanceSection, isSpecificGrade,
  topTabs, activeTopTab, isLoading, currentVersions, currentCategories,
  currentResourceTypes, currentExamPapers, currentKnowledgePoints,
  selectedVersion, selectedCategory, selectedPaper, selectedKnowledge,
  switchGrade, handleSearch, toggleSubject, goToVersion, selectGrade, activeGrade,
  visibleZoneCards, quickFilterZone, displayResources, formatCount, getCoverColor,
} = useGradePage()
</script>"""
text = text[:s0] + new_script + text[s1 + len('</script>'):]
p.write_text(text, encoding='utf-8')
print('patched grade page')
