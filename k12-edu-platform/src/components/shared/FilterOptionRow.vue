<template>
  <div
    class="filter-option-row"
    :class="{ 'filter-option-row--compact': compact, 'filter-option-row--no-overflow': disableOverflow }"
  >
    <span v-if="label" class="filter-option-row__label">{{ label }}</span>
    <div class="filter-option-row__body">
      <div ref="lineRef" class="filter-option-row__line">
        <button
          v-for="opt in visibleOptions"
          :key="opt.value"
          type="button"
          data-btn
          :class="['filter-option-btn', { 'is-active': modelValue === opt.value, 'filter-option-btn--priority': opt.priority }]"
          @click="select(opt.value)"
        >
          <span v-if="opt.icon" class="filter-option-btn__icon">{{ opt.icon }}</span>
          <span>{{ opt.label }}</span>
          <span v-if="opt.showDot" class="filter-option-btn__dot" aria-hidden="true" />
          <span
            v-if="opt.badge != null"
            class="filter-option-btn__badge"
          >{{ opt.badge > 99 ? '99+' : opt.badge }}</span>
        </button>
        <button
          v-if="!disableOverflow && hasHidden"
          type="button"
          :class="['filter-option-btn', 'is-more', { 'is-expanded': expanded }]"
          @click="toggleExpand"
        >
          {{ expanded ? collapseLabel : moreLabel }}
          <svg
            class="filter-option-row__chevron"
            :class="{ 'is-up': expanded }"
            viewBox="0 0 1024 1024"
            width="10"
            height="10"
            fill="currentColor"
            aria-hidden="true"
          >
            <path d="M512 714.667c-14.72 0-29.44-5.621-40.661-16.862L181.973 408.46a57.43 57.43 0 010-81.28 57.392 57.392 0 0181.323 0l248.704 248.683L760.704 327.18a57.392 57.392 0 0181.323 0 57.43 57.43 0 010 81.28L552.66 697.805A57.355 57.355 0 01512 714.667z" />
          </svg>
        </button>
      </div>
      <div v-if="!disableOverflow && expanded && hasHidden" class="filter-option-row__expand">
        <button
          v-for="opt in hiddenOptions"
          :key="'more-' + opt.value"
          type="button"
          :class="['filter-option-btn', { 'is-active': modelValue === opt.value, 'filter-option-btn--priority': opt.priority }]"
          @click="select(opt.value)"
        >
          <span v-if="opt.icon" class="filter-option-btn__icon">{{ opt.icon }}</span>
          <span>{{ opt.label }}</span>
          <span v-if="opt.showDot" class="filter-option-btn__dot" aria-hidden="true" />
          <span
            v-if="opt.badge != null"
            class="filter-option-btn__badge"
          >{{ opt.badge > 99 ? '99+' : opt.badge }}</span>
        </button>
      </div>
      <div ref="measureRef" class="filter-option-row__measure" aria-hidden="true">
        <button
          v-for="opt in options"
          :key="'m-' + opt.value"
          type="button"
          data-measure-btn
          class="filter-option-btn"
        >
          <span v-if="opt.icon" class="filter-option-btn__icon">{{ opt.icon }}</span>
          <span>{{ opt.label }}</span>
          <span v-if="opt.showDot" class="filter-option-btn__dot" />
          <span
            v-if="opt.badge != null"
            class="filter-option-btn__badge"
          >{{ opt.badge > 99 ? '99+' : opt.badge }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, toRef, watch } from 'vue'
import '@/assets/styles/filter-option.css'
import {
  useFilterRowOverflow,
  type FilterRowOption,
} from '@/composables/useFilterRowOverflow'

export type { FilterRowOption }

const props = withDefaults(
  defineProps<{
    label?: string
    options: FilterRowOption[]
    modelValue: string
    compact?: boolean
    disableOverflow?: boolean
    moreLabel?: string
    collapseLabel?: string
  }>(),
  {
    compact: false,
    disableOverflow: false,
    moreLabel: '更多',
    collapseLabel: '收起',
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const lineRef = ref<HTMLElement | null>(null)
const measureRef = ref<HTMLElement | null>(null)
const optionsRef = computed(() => props.options)
const activeRef = toRef(props, 'modelValue')
const disableOverflow = computed(() => props.disableOverflow)

const {
  visibleOptions: overflowVisibleOptions,
  hiddenOptions: overflowHiddenOptions,
  hasHidden,
  expanded,
  toggleExpand,
  scheduleRecalculate,
} = useFilterRowOverflow(lineRef, measureRef, optionsRef, activeRef)

const visibleOptions = computed(() => disableOverflow.value ? props.options : overflowVisibleOptions.value)
const hiddenOptions = computed(() => disableOverflow.value ? [] : overflowHiddenOptions.value)

watch(
  () => props.options,
  () => scheduleRecalculate(),
  { deep: true },
)

function select(value: string) {
  emit('update:modelValue', value)
  if (hiddenOptions.value.some((o) => o.value === value)) {
    expanded.value = true
  }
}
</script>
