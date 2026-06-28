<template>
  <div class="promotion-topic-cards">
    <button
      v-for="topic in topics"
      :key="topic"
      type="button"
      class="topic-card"
      @click="$emit('select', topic)"
    >
      <span class="topic-icon">{{ getMeta(topic).icon }}</span>
      <span class="topic-name">{{ topic }}</span>
      <span class="topic-desc">{{ getMeta(topic).description }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import {
  KINDERGARTEN_BRIDGE_TOPIC_BROWSE,
  type PromotionTopicBrowseTarget,
} from '@/constants/promotionTopicBrowse'

defineProps<{
  topics: string[]
}>()

defineEmits<{
  select: [topic: string]
}>()

function getMeta(topic: string): PromotionTopicBrowseTarget {
  return (
    KINDERGARTEN_BRIDGE_TOPIC_BROWSE[topic] ?? {
      stageKey: 'primary',
      subjectKey: 'chinese',
      browseModule: '开学专区',
      icon: '📚',
      description: '查看更多幼小衔接资源',
    }
  )
}
</script>

<style scoped>
.promotion-topic-cards {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.topic-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  padding: 16px 18px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  text-align: left;
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.15s;
}

.topic-card:hover {
  border-color: #95de64;
  box-shadow: 0 4px 16px rgba(82, 196, 26, 0.12);
  transform: translateY(-2px);
}

.topic-icon {
  font-size: 28px;
  line-height: 1;
}

.topic-name {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

.topic-desc {
  font-size: 13px;
  line-height: 1.5;
  color: #909399;
}

@media (max-width: 1024px) {
  .promotion-topic-cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .promotion-topic-cards {
    grid-template-columns: 1fr;
  }
}
</style>
