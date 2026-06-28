<template>
  <HomeResourcePanel
    :title="title"
    :tabs="examTypeList"
    v-model:tab-model="currentExamType"
    theme="green"
    :items="displayItems"
    :max-items="18"
    list-layout="grid"
    @select="$emit('select', $event)"
    @update:tab-model="onExamTypeChange"
  >
    <template #sidebar>
      <FilterChipGroup
        :chips="topicChips"
        v-model="currentTopic"
        theme="green"
        :columns="3"
        variant="grid"
      />
      <button type="button" class="browse-all-btn" @click="emitBrowseAll">
        查看全部 {{ currentTopic }} 资源 →
      </button>
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
import { usePromotionPanel } from '@/composables/useHomePanels'
import {
  PROMOTION_EXAM_TYPE_TABS,
  PROMOTION_TOPICS_MAP,
} from '@/constants/homeFuncChannels'

const props = withDefaults(
  defineProps<{
    title?: string
    exams?: ResourceListItem[]
    examType?: string
    topic?: string
    promotionExamTabs?: { key: string; label: string }[]
    promotionTopicsMap?: Record<string, string[]>
  }>(),
  {
    title: '升学专区',
    examType: 'kindergarten_bridge',
    topic: '拼音识字',
  }
)

const emit = defineEmits<{
  select: [item: ResourceListItem]
  'update:examType': [key: string]
  'update:topic': [topic: string]
  'browse-all': [payload: { examType: string; topic: string }]
}>()

const examTypeList = computed<ModuleTab[]>(() => {
  const tabs = props.promotionExamTabs?.length
    ? props.promotionExamTabs
    : PROMOTION_EXAM_TYPE_TABS.map((tab) => ({ key: tab.key, label: tab.label }))
  return tabs.map((tab) => ({ key: tab.key, label: tab.label }))
})

const topicsMap = computed(() =>
  props.promotionTopicsMap && Object.keys(props.promotionTopicsMap).length
    ? props.promotionTopicsMap
    : PROMOTION_TOPICS_MAP,
)

const currentExamType = ref(props.examType)
const currentTopic = ref(props.topic)

watch(
  () => [props.examType, props.topic] as const,
  ([examType, topic]) => {
    if (examType) currentExamType.value = examType
    if (topic) currentTopic.value = topic
  },
)

watch(currentTopic, (topic) => {
  emit('update:topic', topic)
})

const topicChips = computed(() =>
  (topicsMap.value[currentExamType.value] || topicsMap.value.middle || []).map((t) => ({
    key: t,
    label: t,
  })),
)

const { items: apiItems, loaded: apiLoaded } = usePromotionPanel(currentExamType, currentTopic)

const displayItems = computed(() => {
  if (apiLoaded.value && apiItems.value.length) {
    return apiItems.value
  }
  if (props.exams?.length) {
    return props.exams.slice(0, 18)
  }
  return []
})

function onExamTypeChange(key: string) {
  currentExamType.value = key
  const topics = topicsMap.value[key]
  currentTopic.value = topics?.[0] || '真题'
  emit('update:examType', key)
}

function emitBrowseAll() {
  emit('browse-all', {
    examType: currentExamType.value,
    topic: currentTopic.value,
  })
}
</script>

<style scoped>
.browse-all-btn {
  margin-top: auto;
  padding: 10px 12px;
  width: 100%;
  font-size: 13px;
  font-weight: 600;
  color: #52c41a;
  background: rgba(82, 196, 26, 0.08);
  border: 1px dashed rgba(82, 196, 26, 0.45);
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;
}

.browse-all-btn:hover {
  background: rgba(82, 196, 26, 0.14);
  border-color: #52c41a;
}
</style>
