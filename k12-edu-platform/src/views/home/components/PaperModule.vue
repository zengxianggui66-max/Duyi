<template>
  <HomeResourcePanel
    :title="title"
    :tabs="tabList"
    v-model:tab-model="currentTab"
    theme="blue"
    :items="displayItems"
    :max-items="18"
    list-layout="split"
    @select="$emit('select', $event)"
  >
    <template #sidebar>
      <p class="sidebar-desc">{{ sidebarDesc }}</p>

      <FilterChipGroup
        :chips="stageChips"
        :model-value="currentStage"
        theme="blue"
        variant="stage"
        @update:model-value="switchStage"
      />

      <FilterChipGroup
        :chips="gradeChips"
        v-model="currentGrade"
        theme="blue"
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
import { usePaperZonePanel } from '@/composables/useHomePanels'

const props = withDefaults(
  defineProps<{
    title?: string
    tabs?: ModuleTab[]
    papers?: ResourceListItem[]
    tabKey?: string
    stageKey?: string
    gradeName?: string
  }>(),
  {
    title: '试卷专区',
    tabKey: 'midterm',
    stageKey: 'primary',
    gradeName: '六年级下册',
  }
)

defineEmits<{
  select: [item: ResourceListItem]
}>()

const sidebarDesc = '\u7cbe\u9009\u8bd5\u5377\uff0c\u6709\u7684\u653e\u77e3\u7cbe\u51c6\u590d\u4e60'

const tabList = computed(() =>
  props.tabs?.length
    ? props.tabs
    : [
        { key: 'midterm', label: '\u671f\u4e2d' },
        { key: 'final', label: '\u671f\u672b' },
        { key: 'opening', label: '\u5f00\u5b66\u8003' },
        { key: 'monthly', label: '\u6708\u8003' },
        { key: 'winter', label: '\u5bd2\u5047' },
        { key: 'summer', label: '\u6691\u5047' },
        { key: 'entrance', label: '\u5347\u5b66' },
      ]
)

const stageChips = [
  { key: 'preschool', label: '幼儿' },
  { key: 'primary', label: '\u5c0f\u5b66' },
  { key: 'junior', label: '\u521d\u4e2d' },
  { key: 'senior', label: '\u9ad8\u4e2d' },
]

const gradesByStage: Record<string, string[]> = {
  preschool: [
    '中班上学期', '中班下学期',
    '大班上学期', '大班下学期',
    '暑假衔接',
  ],
  primary: [
    '\u4e00\u5e74\u7ea7\u4e0a\u518c', '\u4e00\u5e74\u7ea7\u4e0b\u518c',
    '\u4e8c\u5e74\u7ea7\u4e0a\u518c', '\u4e8c\u5e74\u7ea7\u4e0b\u518c',
    '\u4e09\u5e74\u7ea7\u4e0a\u518c', '\u4e09\u5e74\u7ea7\u4e0b\u518c',
    '\u56db\u5e74\u7ea7\u4e0a\u518c', '\u56db\u5e74\u7ea7\u4e0b\u518c',
    '\u4e94\u5e74\u7ea7\u4e0a\u518c', '\u4e94\u5e74\u7ea7\u4e0b\u518c',
    '\u516d\u5e74\u7ea7\u4e0a\u518c', '\u516d\u5e74\u7ea7\u4e0b\u518c',
  ],
  junior: [
    '\u4e03\u5e74\u7ea7\u4e0a\u518c', '\u4e03\u5e74\u7ea7\u4e0b\u518c',
    '\u516b\u5e74\u7ea7\u4e0a\u518c', '\u516b\u5e74\u7ea7\u4e0b\u518c',
    '\u4e5d\u5e74\u7ea7\u4e0a\u518c', '\u4e5d\u5e74\u7ea7\u4e0b\u518c',
  ],
  senior: [
    '\u9ad8\u4e00\u4e0a\u518c', '\u9ad8\u4e00\u4e0b\u518c',
    '\u9ad8\u4e8c\u4e0a\u518c', '\u9ad8\u4e8c\u4e0b\u518c',
    '\u9ad8\u4e09\u4e0a\u518c', '\u9ad8\u4e09\u4e0b\u518c',
  ],
}

const currentTab = ref(props.tabKey)
const currentStage = ref(props.stageKey)
const currentGrade = ref(props.gradeName)

watch(
  () => [props.tabKey, props.stageKey, props.gradeName] as const,
  ([tabKey, stageKey, gradeName]) => {
    if (tabKey) currentTab.value = tabKey
    if (stageKey) currentStage.value = stageKey
    if (gradeName) currentGrade.value = gradeName
  },
)

const gradeChips = computed(() =>
  (gradesByStage[currentStage.value] || gradesByStage.primary).map((g) => ({
    key: g,
    label: g,
  }))
)

const { items: apiItems, loaded: apiLoaded } = usePaperZonePanel(currentStage, currentGrade, currentTab)

const displayItems = computed(() => {
  if (apiLoaded.value && apiItems.value.length) {
    return apiItems.value
  }
  if (props.papers?.length) {
    return props.papers.slice(0, 18)
  }
  return []
})

function switchStage(key: string) {
  currentStage.value = key
  const grades = gradesByStage[key]
  currentGrade.value = grades?.[0] || ''
}
</script>

<style scoped>
.sidebar-desc {
  font-size: 13px;
  font-weight: 400;
  color: #999;
  margin: 0;
  line-height: 1.5;
}
</style>
