<template>
  <HomeResourcePanel
    :title="title"
    :tabs="tabList"
    v-model:tab-model="currentTab"
    theme="orange"
    :items="displayItems"
    :max-items="18"
    list-layout="grid"
    @select="$emit('select', $event)"
  >
    <template #sidebar>
      <div class="sidebar-header">
        <h2 class="sidebar-title">{{ sidebarTitle }}</h2>
        <p class="sidebar-desc">{{ sidebarDesc }}</p>
      </div>

      <FilterChipGroup
        :chips="stageChips"
        v-model="currentStage"
        theme="orange"
        variant="stage"
      />

      <FilterChipGroup
        :chips="subjectChips"
        v-model="currentSubject"
        theme="orange"
        :columns="3"
        variant="grid"
      />
    </template>
  </HomeResourcePanel>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import {
  HomeResourcePanel,
  FilterChipGroup,
  type ModuleTab,
  type ResourceListItem,
} from '@/components/shared'
import { useSyncPrepPanel } from '@/composables/useHomePanels'

const props = withDefaults(
  defineProps<{
    title?: string
    tabs?: ModuleTab[]
    items?: ResourceListItem[]
  }>(),
  { title: '\u540c\u6b65\u5907\u8bfe' }
)

defineEmits<{
  select: [item: ResourceListItem]
}>()

const sidebarDesc = '\u7cbe\u54c1\u6210\u5957\u7cfb\u5217\u8d44\u6599\uff0c\u4e00\u952e\u6253\u5305\u4e0b\u8f7d'

const tabList = computed(() =>
  props.tabs?.length
    ? props.tabs
    : [
        { key: 'courseware', label: '\u8bfe\u4ef6' },
        { key: 'lesson_plan', label: '\u6559\u6848' },
        { key: 'exercise', label: '\u540c\u6b65\u7ec3\u4e60' },
        { key: 'study_guide', label: '\u5b66\u6848' },
        { key: 'knowledge', label: '\u77e5\u8bc6\u70b9' },
        { key: 'collection', label: '\u5408\u96c6' },
      ]
)

const stageChips = [
  { key: 'primary', label: '\u5c0f\u5b66' },
  { key: 'junior', label: '\u521d\u4e2d' },
  { key: 'senior', label: '\u9ad8\u4e2d' },
  { key: 'art', label: '\u7f8e\u672f' },
  { key: 'dance', label: '\u821e\u8e48' },
]

const subjectsByStage: Record<string, string[]> = {
  primary: [
    '\u8bed\u6587', '\u6570\u5b66', '\u82f1\u8bed', '\u79d1\u5b66', '\u9053\u5fb7\u4e0e\u6cd5\u6cbb',
    '\u97f3\u4e50', '\u7f8e\u672f', '\u4fe1\u606f\u6280\u672f', '\u5fc3\u7406\u5065\u5eb7', '\u52b3\u6280',
    '\u4e66\u6cd5\u7ec3\u4e60\u6307\u5bfc', '\u4f53\u80b2', '\u7efc\u5408\u5b9e\u8df5\u6d3b\u52a8', '\u5730\u65b9\u3001\u6821\u672c\u8bfe\u7a0b',
  ],
  junior: [
    '\u8bed\u6587', '\u6570\u5b66', '\u82f1\u8bed', '\u7269\u7406', '\u5316\u5b66', '\u751f\u7269',
    '\u5386\u53f2', '\u5730\u7406', '\u9053\u5fb7\u4e0e\u6cd5\u6cbb', '\u79d1\u5b66', '\u97f3\u4e50',
    '\u7f8e\u672f', '\u4f53\u80b2', '\u4fe1\u606f\u6280\u672f', '\u5fc3\u7406\u5065\u5eb7',
  ],
  senior: [
    '\u8bed\u6587', '\u6570\u5b66', '\u82f1\u8bed', '\u653f\u6cbb', '\u5386\u53f2', '\u5730\u7406',
    '\u7269\u7406', '\u5316\u5b66', '\u751f\u7269', '\u4fe1\u606f\u6280\u672f', '\u901a\u7528\u6280\u672f',
    '\u5fc3\u7406\u5065\u5eb7', '\u52b3\u52a8\u6280\u672f', '\u4f53\u80b2', '\u97f3\u4e50', '\u7f8e\u672f',
  ],
  art: ['\u7ed8\u753b', '\u7d20\u63cf', '\u8272\u5f69', '\u56fd\u753b', '\u4e66\u6cd5', '\u624b\u5de5', '\u827a\u8003\u7f8e\u672f'],
  dance: ['\u4e2d\u56fd\u821e', '\u6c11\u65cf\u821e', '\u53e4\u5178\u821e', '\u62c9\u4e01\u821e', '\u8857\u821e', '\u82ad\u857e', '\u827a\u8003\u821e\u8e48'],
}

const sidebarTitleMap: Record<string, string> = {
  courseware: '\u6210\u5957\u8bfe\u4ef6',
  lesson_plan: '\u6210\u5957\u6559\u6848',
  exercise: '\u6210\u5957\u7ec3\u4e60',
  study_guide: '\u6210\u5957\u5b66\u6848',
  knowledge: '\u77e5\u8bc6\u70b9',
  collection: '\u8d44\u6e90\u5408\u96c6',
}

const currentTab = ref(tabList.value[0]?.key || 'courseware')
const currentStage = ref('primary')
const currentSubject = ref('语文')

const sidebarTitle = computed(() => sidebarTitleMap[currentTab.value] || '\u6210\u5957\u8bfe\u4ef6')

const subjectChips = computed(() =>
  (subjectsByStage[currentStage.value] || subjectsByStage.primary).map((s) => ({
    key: s,
    label: s,
  }))
)

watch(currentStage, (stage) => {
  const subjects = subjectsByStage[stage] || subjectsByStage.primary
  if (!subjects.includes(currentSubject.value)) {
    currentSubject.value = subjects[0] || ''
  }
})

const { items: apiItems, loaded: apiLoaded } = useSyncPrepPanel(currentStage, currentSubject, currentTab)

const displayItems = computed(() => {
  if (apiLoaded.value && apiItems.value.length) {
    return apiItems.value
  }
  if (props.items?.length) {
    return props.items.slice(0, 18)
  }
  return []
})
</script>

<style scoped>
.sidebar-title {
  font-size: 18px;
  font-weight: 700;
  color: #ff6b00;
  margin: 0 0 6px;
}

.sidebar-desc {
  font-size: 12px;
  color: #999;
  margin: 0;
  line-height: 1.5;
}
</style>
