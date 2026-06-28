<template>
  <div class="preview-slides" :class="{ 'is-full': isFullPreview }">
    <div class="slide-container" :class="{ 'full-preview': isFullPreview }">
      <div class="slide-content">
        <img
          v-if="currentUrl"
          :src="currentUrl"
          :alt="`${title} - 第 ${currentSlide + 1} 页`"
          class="slide-image"
          loading="lazy"
          @error="onSlideError"
        />
      </div>

      <button class="slide-nav prev" :disabled="currentSlide <= 0" @click="$emit('prev')">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <button
        class="slide-nav next"
        :disabled="currentSlide >= slideCount - 1"
        @click="$emit('next')"
      >
        <el-icon><ArrowRight /></el-icon>
      </button>
    </div>

    <div class="slide-indicator">
      <span>第 {{ currentSlide + 1 }} / {{ slideCount }} 页</span>
      <div class="slide-dots">
        <span
          v-for="i in slideCount"
          :key="i"
          class="dot"
          :class="{ active: currentSlide === i - 1 }"
          @click="$emit('goto', i - 1)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const props = defineProps<{
  title: string
  slideUrls: string[]
  currentSlide: number
  isFullPreview: boolean
}>()

defineEmits<{
  prev: []
  next: []
  goto: [index: number]
}>()

const hasSlideError = ref(false)
const slideCount = computed(() => props.slideUrls.length)
const currentUrl = computed(() => {
  if (hasSlideError.value) return ''
  return props.slideUrls[props.currentSlide] || ''
})

function onSlideError() {
  hasSlideError.value = true
}
</script>

<style scoped>
.preview-slides.is-full {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: #000;
  display: flex;
  flex-direction: column;
}

.slide-container {
  position: relative;
  background: #1a1a2e;
  border-radius: 8px;
  overflow: hidden;
  aspect-ratio: 16/9;
  flex: 1;
}

.slide-container.full-preview {
  border-radius: 0;
  aspect-ratio: auto;
  height: calc(100vh - 56px);
}

.slide-content {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  box-sizing: border-box;
}

.slide-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 4px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.35);
}

.slide-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.92);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.slide-nav.prev {
  left: 16px;
}

.slide-nav.next {
  right: 16px;
}

.slide-nav:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.slide-indicator {
  padding: 12px;
  color: #909399;
  font-size: 14px;
  text-align: center;
}

.slide-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
  flex-wrap: wrap;
  max-height: 48px;
  overflow: auto;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #dcdfe6;
  cursor: pointer;
  flex-shrink: 0;
}

.dot.active {
  background: var(--el-color-primary);
}

.is-full .slide-indicator {
  color: rgba(255, 255, 255, 0.75);
}

.is-full .dot {
  background: rgba(255, 255, 255, 0.35);
}

.is-full .dot.active {
  background: #fff;
}
</style>
