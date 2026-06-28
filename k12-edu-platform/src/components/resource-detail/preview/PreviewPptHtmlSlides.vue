<template>
  <div class="preview-slides" :class="{ 'is-full': isFullPreview }">
    <div v-if="loading" class="slide-loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>正在解析幻灯片…</span>
    </div>

    <el-alert v-else-if="error" type="warning" :closable="false" show-icon :title="error" />

    <template v-else-if="slides.length > 0">
      <div class="slide-container" :class="{ 'full-preview': isFullPreview }">
        <div class="slide-stage" :class="{ 'content-slide': currentSlide > 0 }">
          <div class="sky-band"></div>
          <div class="sun"><span>七彩课堂</span></div>
          <div class="cloud cloud-a"></div>
          <div class="cloud cloud-b"></div>
          <div class="cloud cloud-c"></div>

          <div class="slide-course">{{ courseLabel }}</div>
          <div class="slide-title">{{ activeSlide.title }}</div>

          <div v-if="currentSlide === 0" class="student-scene" aria-hidden="true">
            <div class="student boy">
              <span class="head"></span><span class="hair"></span><span class="body"></span>
              <span class="tie"></span><span class="arm left"></span><span class="arm right"></span>
              <span class="leg left"></span><span class="leg right"></span>
            </div>
            <div class="student girl">
              <span class="head"></span><span class="hair"></span><span class="body"></span>
              <span class="tie"></span><span class="arm left"></span><span class="arm right"></span>
              <span class="leg left"></span><span class="leg right"></span>
            </div>
          </div>

          <div v-else class="slide-text-panel">
            <p v-for="(line, index) in activeSlide.lines" :key="index">{{ line }}</p>
          </div>

          <div class="book-stack" aria-hidden="true">
            <span></span><span></span><span></span>
          </div>
          <div class="hills" aria-hidden="true"></div>
          <div class="trees" aria-hidden="true"><span></span><span></span><span></span></div>
        </div>

        <button class="slide-nav prev" :disabled="currentSlide <= 0" @click="$emit('prev')">
          <el-icon><ArrowLeft /></el-icon>
        </button>
        <button
          class="slide-nav next"
          :disabled="currentSlide >= slides.length - 1"
          @click="$emit('next')"
        >
          <el-icon><ArrowRight /></el-icon>
        </button>
      </div>
      <div class="slide-indicator">
        <span>第 {{ currentSlide + 1 }} / {{ slides.length }} 页</span>
        <div class="slide-dots">
          <span
            v-for="i in slides.length"
            :key="i"
            class="dot"
            :class="{ active: currentSlide === i - 1 }"
            @click="$emit('goto', i - 1)"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { resolvePreviewAssetUrl } from '@/utils/filePreview'
import { ArrowLeft, ArrowRight, Loading } from '@element-plus/icons-vue'

interface SlideModel {
  title: string
  lines: string[]
}

const props = defineProps<{
  title: string
  htmlUrl: string
  currentSlide: number
  isFullPreview: boolean
}>()

const emit = defineEmits<{
  prev: []
  next: []
  goto: [index: number]
  'pages-loaded': [count: number]
}>()

const loading = ref(true)
const error = ref('')
const slides = ref<SlideModel[]>([])

const activeSlide = computed(() => slides.value[props.currentSlide] || slides.value[0] || { title: props.title, lines: [] })
const courseLabel = computed(() => inferCourseLabel(props.title))

function cleanText(text: string): string {
  return text.replace(/\s+/g, ' ').replace(/^第\s*\d+\s*页[:：]?\s*/, '').trim()
}

function inferCourseLabel(title: string): string {
  const subject = title.match(/语文|数学|英语|道德与法治|科学|音乐|美术/)?.[0] || '语文'
  const grade = title.match(/[一二三四五六七八九]年级/)?.[0] || '一年级'
  const volume = title.match(/上册|下册/)?.[0] || '上册'
  return `${subject}  ${grade}  ${volume}`
}

function parseOneSlide(el: Element, index: number): SlideModel {
  const heading = el.querySelector('h1,h2,h3,h4')?.textContent || ''
  const titleText = cleanText(heading) || (index === 0 ? props.title : `第 ${index + 1} 页`)
  const lines = Array.from(el.querySelectorAll('p,li'))
    .map((node) => cleanText(node.textContent || ''))
    .filter((line) => line && !line.includes('已生成 HTML 幻灯片摘要'))
    .slice(0, 5)
  return { title: titleText, lines }
}

function parseSlidesFromHtml(html: string): SlideModel[] {
  const doc = new DOMParser().parseFromString(html, 'text/html')
  const sections = Array.from(doc.querySelectorAll('section.slide'))
  if (sections.length > 0) return sections.map(parseOneSlide)

  const headings = Array.from(doc.querySelectorAll('h1,h2,h3'))
  if (headings.length > 0) {
    return headings.map((heading, index) => {
      const container = heading.parentElement || doc.body
      return parseOneSlide(container, index)
    })
  }

  const bodyLines = cleanText(doc.body?.textContent || '')
    .split(/[。；;\n]/)
    .map(cleanText)
    .filter(Boolean)
  return [{ title: props.title, lines: bodyLines.slice(0, 5) }]
}

async function loadHtml(url: string) {
  loading.value = true
  error.value = ''
  slides.value = []
  const fetchUrl = resolvePreviewAssetUrl(url)
  try {
    const res = await fetch(fetchUrl)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const html = await res.text()
    slides.value = parseSlidesFromHtml(html)
    emit('pages-loaded', slides.value.length)
    if (slides.value.length === 0) error.value = '未能从 HTML 中解析出幻灯片页'
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'HTML 幻灯片加载失败'
  } finally {
    loading.value = false
  }
}

watch(
  () => props.htmlUrl,
  (url) => {
    if (url) void loadHtml(url)
  },
  { immediate: true },
)
</script>

<style scoped>
.preview-slides.is-full {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: #0f172a;
  display: flex;
  flex-direction: column;
}

.slide-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 80px;
  color: #909399;
}

.slide-container {
  position: relative;
  background: #dbeafe;
  border-radius: 8px;
  overflow: hidden;
  aspect-ratio: 16 / 9;
  min-height: 420px;
  flex: 1;
  box-shadow: inset 0 0 0 1px rgba(15, 23, 42, 0.08);
}

.slide-container.full-preview {
  border-radius: 0;
  aspect-ratio: auto;
  min-height: calc(100vh - 56px);
}

.slide-stage {
  position: absolute;
  inset: 0;
  overflow: hidden;
  background: linear-gradient(#83c5f7 0 72%, #eef9ff 72% 100%);
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
}

.sky-band {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(255,255,255,0.18), transparent 44%);
}

.slide-course {
  position: absolute;
  top: 7%;
  left: 3.8%;
  z-index: 3;
  color: #050505;
  font-size: clamp(24px, 4.4vw, 58px);
  font-weight: 800;
  letter-spacing: 0;
}

.slide-title {
  position: absolute;
  top: 22%;
  left: 16%;
  right: 14%;
  z-index: 3;
  color: #050505;
  text-align: center;
  font-family: "STKaiti", "KaiTi", "Microsoft YaHei", serif;
  font-size: clamp(36px, 7vw, 96px);
  font-weight: 500;
  line-height: 1.2;
  text-shadow: 2px 3px 0 #fff, 0 3px 6px rgba(0, 0, 0, 0.16);
}

.content-slide .slide-title {
  top: 14%;
  left: 8%;
  right: 8%;
  font-size: clamp(28px, 4.8vw, 60px);
}

.slide-text-panel {
  position: absolute;
  left: 14%;
  right: 14%;
  top: 32%;
  z-index: 4;
  display: grid;
  gap: 14px;
}

.slide-text-panel p {
  margin: 0;
  padding: 10px 18px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.82);
  color: #1f2937;
  font-size: clamp(18px, 2.2vw, 30px);
  line-height: 1.45;
  box-shadow: 0 8px 18px rgba(30, 64, 175, 0.12);
}

.sun {
  position: absolute;
  top: 2%;
  right: 5%;
  width: 14%;
  aspect-ratio: 1;
  z-index: 2;
  border-radius: 50%;
  background: #ffb000;
  box-shadow: 0 0 0 22px rgba(255, 214, 0, 0.85), 0 8px 18px rgba(180, 83, 9, 0.18);
}

.sun span {
  position: absolute;
  right: -18%;
  top: 4%;
  color: #172554;
  font-size: clamp(12px, 1.7vw, 24px);
  font-weight: 800;
  white-space: nowrap;
  text-shadow: 1px 1px 0 #fff;
}

.cloud,
.cloud::before,
.cloud::after {
  position: absolute;
  background: #fff;
  border-radius: 999px;
  filter: drop-shadow(0 6px 8px rgba(30, 64, 175, 0.16));
}

.cloud {
  width: 14%;
  height: 4.8%;
  z-index: 2;
}

.cloud::before,
.cloud::after {
  content: "";
  bottom: 0;
}

.cloud::before { width: 44%; height: 180%; left: 18%; }
.cloud::after { width: 52%; height: 240%; right: 12%; }
.cloud-a { left: -3%; top: 14%; }
.cloud-b { left: 11%; top: 21%; width: 16%; }
.cloud-c { right: -1%; top: 16%; width: 15%; }

.hills {
  position: absolute;
  left: -4%;
  right: -4%;
  bottom: -12%;
  height: 34%;
  z-index: 1;
  background:
    radial-gradient(80% 72% at 24% 12%, #70c900 0 45%, transparent 46%),
    radial-gradient(86% 88% at 82% 18%, #89cd31 0 48%, transparent 49%),
    linear-gradient(180deg, transparent 0 34%, #62bd00 35%);
}

.book-stack {
  position: absolute;
  left: 0;
  bottom: 0;
  z-index: 4;
  width: 18%;
  height: 24%;
}

.book-stack span {
  position: absolute;
  bottom: 0;
  width: 34%;
  height: 88%;
  border-radius: 4px 4px 0 0;
  background: #ef3b64;
  box-shadow: inset 10px 0 0 rgba(255,255,255,0.18), inset 0 -18px 0 rgba(255,255,255,0.9);
}

.book-stack span:nth-child(2) { left: 31%; height: 74%; background: #13b8b0; }
.book-stack span:nth-child(3) { left: 62%; bottom: 0; width: 76%; height: 8%; background: #f5b400; border-radius: 5px; }

.student-scene {
  position: absolute;
  left: 34%;
  right: 28%;
  bottom: 12%;
  z-index: 5;
  height: 40%;
}

.student {
  position: absolute;
  bottom: 0;
  width: 36%;
  height: 100%;
}

.student.girl { left: 48%; }
.student.boy { left: 8%; }
.student span { position: absolute; display: block; }
.student .head { left: 30%; top: 9%; width: 40%; aspect-ratio: 1; border-radius: 50%; background: #ffd0b2; }
.student .hair { left: 26%; top: 6%; width: 48%; height: 20%; border-radius: 50% 50% 42% 42%; background: #7c3f16; }
.student .body { left: 25%; top: 43%; width: 48%; height: 28%; border-radius: 0 0 16px 16px; background: #fff; box-shadow: inset 0 10px 0 #2ea7df; }
.student .tie { left: 46%; top: 48%; width: 10%; height: 30%; background: #c9461e; clip-path: polygon(50% 0, 100% 25%, 70% 100%, 30% 100%, 0 25%); }
.student .arm { top: 34%; width: 12%; height: 34%; border-radius: 999px; background: #ffd0b2; transform-origin: bottom; }
.student .arm.left { left: 18%; transform: rotate(28deg); }
.student .arm.right { right: 16%; transform: rotate(-42deg); }
.student .leg { bottom: 0; width: 12%; height: 30%; border-radius: 999px; background: #ffd0b2; }
.student .leg.left { left: 34%; transform: rotate(25deg); }
.student .leg.right { right: 30%; transform: rotate(-8deg); }
.student.girl .body { box-shadow: inset 0 10px 0 #2ea7df, inset 0 -18px 0 #2d9bd6; }
.student.girl .hair { border-radius: 45% 45% 55% 55%; }

.trees {
  position: absolute;
  right: 3%;
  bottom: 7%;
  z-index: 3;
  display: flex;
  gap: 12px;
}

.trees span {
  width: 34px;
  height: 74px;
  background: linear-gradient(#ffb14a 0 54%, #a56a25 55% 100%);
  clip-path: polygon(50% 0, 100% 70%, 62% 70%, 62% 100%, 42% 100%, 42% 70%, 0 70%);
}

.slide-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid #dbeafe;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.slide-nav.prev { left: 16px; }
.slide-nav.next { right: 16px; }
.slide-nav:disabled { opacity: 0.35; cursor: not-allowed; }

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

.dot.active { background: var(--el-color-primary); }
.is-full .slide-indicator { color: rgba(255, 255, 255, 0.75); }
</style>
