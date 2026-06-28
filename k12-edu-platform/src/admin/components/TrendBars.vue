<template>
  <div class="trend-bars">
    <div v-if="!points.length" class="trend-bars__empty">暂无数据</div>
    <div v-else class="trend-bars__chart">
      <div v-for="p in points" :key="p.date" class="trend-bars__col" :title="tooltip(p)">
        <div class="trend-bars__bar-wrap">
          <div class="trend-bars__bar" :style="{ height: barHeight(p.count) }" />
        </div>
        <span class="trend-bars__date">{{ shortDate(p.date) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { AnalyticsDailyPoint } from '@/admin/api/analytics'

const props = withDefaults(
  defineProps<{
    points: AnalyticsDailyPoint[]
    showCumulative?: boolean
  }>(),
  { showCumulative: false },
)

const maxCount = computed(() => Math.max(...props.points.map((p) => p.count), 1))

function barHeight(count: number) {
  const pct = Math.round((count / maxCount.value) * 100)
  return `${Math.max(pct, count > 0 ? 8 : 0)}%`
}

function shortDate(date: string) {
  return date.slice(5)
}

function tooltip(p: AnalyticsDailyPoint) {
  if (props.showCumulative && p.cumulative != null) {
    return `${p.date} 新增 ${p.count}，累计 ${p.cumulative}`
  }
  return `${p.date} ${p.count}`
}
</script>

<style scoped>
.trend-bars__empty {
  font-size: 13px;
  color: #909399;
  padding: 24px 0;
  text-align: center;
}
.trend-bars__chart {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  height: 160px;
  padding: 8px 0 0;
  overflow-x: auto;
}
.trend-bars__col {
  flex: 1;
  min-width: 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}
.trend-bars__bar-wrap {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.trend-bars__bar {
  width: 70%;
  max-width: 32px;
  background: linear-gradient(180deg, #409eff, #a0cfff);
  border-radius: 4px 4px 0 0;
  min-height: 2px;
  transition: height 0.3s ease;
}
.trend-bars__date {
  margin-top: 6px;
  font-size: 10px;
  color: #909399;
  white-space: nowrap;
}
</style>
