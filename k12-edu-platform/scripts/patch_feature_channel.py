from pathlib import Path

p = Path(__file__).resolve().parents[1] / 'src/views/feature/FeatureChannel.vue'
text = p.read_text(encoding='utf-8')
start = text.index('          <!-- 资源列表区 -->')
end = text.index('        </main>', start)
replacement = """          <FeatureResourceList
            :loading="loading"
            :total="total"
            :resources="resources"
            :stats="stats"
            v-model:sort-by="sortBy"
            :current-page="currentPage"
            :default-desc="channelInfo.defaultDesc"
            :get-cover-color="getCoverColor"
            :get-resource-icon="getResourceIcon"
            :get-type-name="getTypeName"
            :format-count="formatCount"
            @sort-change="loadData"
            @page-change="onPageChange"
            @open="goToDetail"
          />

"""
text = text[:start] + replacement + text[end:]
s0 = text.index('<script setup lang="ts">')
s1 = text.index('</script>', s0)
new_script = """<script setup lang="ts">
import FeatureFilterSidebar from '@/components/feature/FeatureFilterSidebar.vue'
import FeatureResourceList from '@/components/feature/FeatureResourceList.vue'
import { useFeatureChannel, banhuiThemes, hotDownloadTabs } from '@/composables/useFeatureChannel'

const {
  channelType, channelInfo, loading, total, resources, mainTabs, activeMainTab,
  eliteAlbums, stats, gradeFilters, levelFilters, formatFilters, subjectFilters,
  selectedGrade, selectedLevel, selectedFormat, selectedSubject, sortBy, currentPage,
  activeHotTab, hotDownloadItems, latestUploads, switchMainTab, loadData, goToDetail,
  getCoverColor, getResourceIcon, getTypeName, formatCount,
} = useFeatureChannel()

function onPageChange(p: number) {
  currentPage.value = p
  loadData()
}
</script>"""
text = text[:s0] + new_script + text[s1 + len('</script>'):]
p.write_text(text, encoding='utf-8')
print('patched', p.stat().st_size)
