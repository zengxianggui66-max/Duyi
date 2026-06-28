<template>
  <section class="banner-section">
    <div class="banner-container">
      <div
        class="banner-slider"
        :style="{ transform: `translateX(-${currentIndex * 100}%)` }"
      >
        <div
          v-for="(banner, index) in banners"
          :key="banner.id ?? index"
          class="banner-slide"
        >
          <div
            class="banner-bg"
            :style="{ background: `linear-gradient(135deg, ${banner.color1}, ${banner.color2})` }"
          >
            <div class="banner-content">
              <div class="banner-text">
                <h2 class="banner-title">{{ banner.title }}</h2>
                <p class="banner-desc">{{ banner.description }}</p>
                <button class="banner-cta" @click="handleCta(banner)">
                  {{ banner.cta }}
                </button>
              </div>
              <div class="banner-image">
                {{ banner.icon }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="banners.length > 1" class="banner-dots">
        <button
          v-for="(_, index) in banners"
          :key="index"
          :class="['dot', { active: currentIndex === index }]"
          @click="goToSlide(index)"
        ></button>
      </div>

      <button v-if="banners.length > 1" class="arrow arrow-left" @click="prevSlide">‹</button>
      <button v-if="banners.length > 1" class="arrow arrow-right" @click="nextSlide">›</button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import type { BannerViewModel } from '@/types/homeOps'
import {
  openExternalNavTarget,
  resolveNavTargetRoute,
  scrollToNavTarget,
} from '@/utils/resolveNavTarget'

const props = defineProps<{
  banners: BannerViewModel[]
}>()

const router = useRouter()
const currentIndex = ref(0)
let autoPlayTimer: ReturnType<typeof setInterval> | null = null

function goToSlide(index: number) {
  currentIndex.value = index
}

function nextSlide() {
  const len = props.banners.length
  if (len <= 0) return
  currentIndex.value = (currentIndex.value + 1) % len
}

function prevSlide() {
  const len = props.banners.length
  if (len <= 0) return
  currentIndex.value = (currentIndex.value - 1 + len) % len
}

function handleCta(banner: BannerViewModel) {
  const target = banner.navTarget
  if (!target) return
  if (target.type === 'external') {
    openExternalNavTarget(target)
    return
  }
  if (target.type === 'scroll') {
    scrollToNavTarget(target)
    return
  }
  const route = resolveNavTargetRoute(target)
  if (route) router.push(route)
}

function startAutoPlay() {
  stopAutoPlay()
  if (props.banners.length <= 1) return
  autoPlayTimer = setInterval(nextSlide, 5000)
}

function stopAutoPlay() {
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer)
    autoPlayTimer = null
  }
}

watch(
  () => props.banners.length,
  () => {
    currentIndex.value = 0
    startAutoPlay()
  },
)

onMounted(() => {
  startAutoPlay()
})

onUnmounted(() => {
  stopAutoPlay()
})
</script>

<style scoped>
.banner-section {
  flex-shrink: 0;
  margin-bottom: 0;
}

.banner-container {
  position: relative;
  width: 100%;
  height: 280px;
  overflow: hidden;
  border-radius: 20px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

.banner-slider {
  display: flex;
  transition: transform 0.5s ease;
  height: 100%;
}

.banner-slide {
  flex: 0 0 100%;
  width: 100%;
  height: 100%;
}

.banner-bg {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
}

.banner-content {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 48px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.banner-text {
  flex: 1;
}

.banner-title {
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 12px;
  text-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.banner-desc {
  font-size: 16px;
  color: rgba(255,255,255,0.9);
  margin: 0 0 24px;
  max-width: 400px;
}

.banner-cta {
  padding: 12px 32px;
  background: #fff;
  border: none;
  border-radius: 40px;
  font-size: 15px;
  font-weight: 600;
  color: #1a5cbf;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.banner-cta:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.2);
}

.banner-image {
  font-size: 120px;
  opacity: 0.9;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.1));
}

.banner-dots {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: none;
  background: rgba(255,255,255,0.5);
  cursor: pointer;
  transition: all 0.2s;
}

.dot.active {
  background: #fff;
  width: 24px;
  border-radius: 5px;
}

.arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.35);
  color: rgba(26, 92, 191, 0.85);
  font-size: 24px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  backdrop-filter: blur(4px);
}

.arrow:hover {
  background: rgba(255, 255, 255, 0.55);
  color: #1a5cbf;
  transform: translateY(-50%) scale(1.06);
}

.arrow-left {
  left: 16px;
}

.arrow-right {
  right: 16px;
}
</style>
