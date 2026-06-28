<template>
  <div class="series-filter-bar">
    <FilterOptionRow
      label="系列："
      :options="seriesOptions"
      :model-value="selectedBrandCode"
      @update:model-value="$emit('select', $event)"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import FilterOptionRow from '../shared/FilterOptionRow.vue'
import type { ResourceSeriesItem } from '../../config/resourceSeriesConfig'

const props = defineProps<{
  seriesTabs: ResourceSeriesItem[]
  selectedBrandCode: string
}>()

defineEmits<{
  select: [code: string]
}>()

const seriesOptions = computed(() =>
  props.seriesTabs.map((item) => ({
    value: item.code,
    label: item.name,
  })),
)
</script>

<style scoped>
.series-filter-bar {
  padding: 0 24px;
}
</style>
