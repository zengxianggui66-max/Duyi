<template>
  <div
    ref="containerRef"
    class="virtual-list-container"
    :style="{ overflowY: 'auto', overflowX: 'hidden' }"
    @scroll="onScroll"
  >
    <div
      class="virtual-list-phantom"
      :style="{ height: `${totalHeight}px` }"
    >
      <div
        class="virtual-list-visible"
        :style="{ transform: `translateY(${offsetY}px)` }"
      >
        <div
          v-for="(item, idx) in visibleItems"
          :key="itemKey(item, startIndex + idx)"
          class="virtual-list-item"
        >
          <slot
            name="item"
            :item="item"
            :index="startIndex + idx"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'

const props = defineProps<{
  items: any[]
  /** 预估的每一项高度（px），用于计算总高度 */
  estimatedItemHeight: number
  /** 唯一 key 生成函数 */
  itemKey: (item: any, index: number) => string | number
  /** 容器固定高度（px）；不传则用实际容器高度 */
  containerHeight?: number
  /** 缓冲区条数（上下额外渲染） */
  overscan?: number
}>()

const containerRef = ref<HTMLElement | null>(null)
const actualContainerHeight = ref(props.containerHeight || 400)
const scrollTop = ref(0)

// 记录实际高度（用于提升滚动条精确度）
const itemHeights = ref<Map<string | number, number>>(new Map())

const overscan = computed(() => props.overscan ?? 5)
const itemCount = computed(() => props.items.length)
const totalHeight = computed(() => {
  // 优先使用已测量的高度，否则用估算值
  let total = 0
  for (const item of props.items) {
    const key = props.itemKey(item, 0)
    const h = itemHeights.value.get(key)
    total += h ?? props.estimatedItemHeight
  }
  return total
})

const startIndex = computed(() => {
  let accumulated = 0
  for (let i = 0; i < itemCount.value; i++) {
    const item = props.items[i]
    const key = props.itemKey(item, i)
    const h = itemHeights.value.get(key) ?? props.estimatedItemHeight
    if (accumulated + h > scrollTop.value) return Math.max(0, i - overscan.value)
    accumulated += h
  }
  return Math.max(0, itemCount.value - 1 - overscan.value)
})

const endIndex = computed(() => {
  const viewBottom = scrollTop.value + actualContainerHeight.value
  let accumulated = 0
  for (let i = 0; i < itemCount.value; i++) {
    const item = props.items[i]
    const key = props.itemKey(item, i)
    const h = itemHeights.value.get(key) ?? props.estimatedItemHeight
    accumulated += h
    if (accumulated >= viewBottom) return Math.min(itemCount.value - 1, i + overscan.value)
  }
  return itemCount.value - 1
})

const visibleItems = computed(() => {
  return props.items.slice(startIndex.value, endIndex.value + 1)
})

const offsetY = computed(() => {
  let offset = 0
  for (let i = 0; i < startIndex.value; i++) {
    const item = props.items[i]
    const key = props.itemKey(item, i)
    const h = itemHeights.value.get(key) ?? props.estimatedItemHeight
    offset += h
  }
  return offset
})

function onScroll() {
  if (containerRef.value) {
    scrollTop.value = containerRef.value.scrollTop
  }
}

function measureItem(index: number, el: HTMLElement | null) {
  if (!el) return
  const item = props.items[index]
  const key = props.itemKey(item, index)
  const height = el.getBoundingClientRect().height
  if (height > 0 && itemHeights.value.get(key) !== height) {
    itemHeights.value.set(key, height)
  }
}

function updateContainerHeight() {
  if (containerRef.value && !props.containerHeight) {
    actualContainerHeight.value = containerRef.value.clientHeight
  }
}

let resizeObserver: ResizeObserver | null = null

onMounted(() => {
  if (containerRef.value && !props.containerHeight) {
    actualContainerHeight.value = containerRef.value.clientHeight
    resizeObserver = new ResizeObserver(() => updateContainerHeight())
    resizeObserver.observe(containerRef.value)
  }
})

onUnmounted(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
})
</script>
