<template>
  <div class="preview-slides">
    <div class="slide-container" :class="{ 'full-preview': isFullPreview }">
      <div class="slide-content">
        <div class="slide-page" v-if="currentSlide === 0">
          <div class="ppt-cover">
            <div class="ppt-cover-deco" />
            <h2 class="ppt-cover-title">{{ title }}</h2>
            <p class="ppt-cover-sub">适用年级：{{ grade || '四年级' }}</p>
            <p class="ppt-cover-sub">设计理念：以情感体验为核心，引导学生在活动中感悟感恩</p>
            <div class="ppt-cover-footer">新课堂教育</div>
          </div>
        </div>
        <div class="slide-page" v-else-if="currentSlide === 1">
          <div class="ppt-inner">
            <h3 class="ppt-page-title">🎯 导入环节</h3>
            <div class="ppt-content-block">
              <p class="ppt-text">播放感恩主题短视频《感恩的心》</p>
              <p class="ppt-text">讲述一个关于感恩的小故事</p>
              <p class="ppt-text">引导学生思考：什么是感恩？</p>
            </div>
            <div class="ppt-qa-box">
              <p class="ppt-qa">❓ 互动提问：你最近一次说"谢谢"是什么时候？</p>
            </div>
          </div>
        </div>
        <div class="slide-page" v-else-if="currentSlide === 2">
          <div class="ppt-inner">
            <h3 class="ppt-page-title">💬 互动讨论</h3>
            <div class="ppt-content-block">
              <p class="ppt-text">分享身边的感恩瞬间</p>
              <p class="ppt-text">小组讨论：情景问答</p>
              <p class="ppt-text">角色扮演：如果你是老师/父母</p>
            </div>
            <div class="ppt-qa-box">
              <p class="ppt-qa">💭 情景：当你生病时，父母是如何照顾你的？</p>
            </div>
          </div>
        </div>
        <div class="slide-page" v-else-if="currentSlide === 3">
          <div class="ppt-inner">
            <h3 class="ppt-page-title">💝 感恩行动</h3>
            <div class="ppt-content-block">
              <p class="ppt-text">写下感恩卡片</p>
              <p class="ppt-text">分享给父母/老师</p>
              <p class="ppt-text">制作感恩手抄报</p>
            </div>
            <div class="ppt-highlight-box">
              <p class="ppt-highlight">🌟 感恩不止于言语，更要付诸行动</p>
            </div>
          </div>
        </div>
        <div class="slide-page" v-else-if="currentSlide === 4">
          <div class="ppt-inner">
            <h3 class="ppt-page-title">📝 总结升华</h3>
            <div class="ppt-content-block">
              <p class="ppt-text">班主任总结发言</p>
              <p class="ppt-text">发起感恩行动倡议</p>
              <p class="ppt-text">合唱《感恩的心》</p>
            </div>
            <div class="ppt-qa-box">
              <p class="ppt-qa">📋 课后作业：完成一件感恩的事，下节课分享</p>
            </div>
          </div>
        </div>
      </div>

      <button class="slide-nav prev" :disabled="currentSlide === 0" @click="$emit('prev')">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <button class="slide-nav next" :disabled="currentSlide >= slideCount - 1" @click="$emit('next')">
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
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

defineProps<{
  title: string
  grade?: string
  currentSlide: number
  slideCount: number
  isFullPreview: boolean
}>()

defineEmits<{
  prev: []
  next: []
  goto: [index: number]
}>()
</script>

<style scoped>
.preview-slides {
  text-align: center;
}
.slide-container {
  position: relative;
  background: #1a1a2e;
  border-radius: 8px;
  overflow: hidden;
  aspect-ratio: 16/9;
}
.slide-container.full-preview {
  position: fixed;
  inset: 0;
  width: 100vw !important;
  height: 100vh !important;
  z-index: 9999;
  aspect-ratio: auto;
  border-radius: 0;
}
.slide-content,
.slide-page {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ppt-cover {
  text-align: center;
  color: #fff;
  padding: 40px;
  position: relative;
}
.ppt-cover-deco {
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, #409eff, #67c23a);
  margin: 0 auto 24px;
  border-radius: 2px;
}
.ppt-cover-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
}
.ppt-cover-sub {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 8px;
}
.ppt-cover-footer {
  position: absolute;
  bottom: -60px;
  left: 0;
  right: 0;
  color: rgba(255, 255, 255, 0.4);
  font-size: 12px;
}
.ppt-inner {
  text-align: left;
  color: #fff;
  padding: 40px 60px;
  max-width: 800px;
}
.ppt-page-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid rgba(255, 255, 255, 0.2);
}
.ppt-text {
  font-size: 18px;
  line-height: 2;
  color: rgba(255, 255, 255, 0.9);
  padding-left: 20px;
  position: relative;
  margin: 0;
}
.ppt-text::before {
  content: '▸';
  position: absolute;
  left: 0;
  color: #409eff;
}
.ppt-qa-box {
  background: rgba(64, 158, 255, 0.15);
  border: 1px solid rgba(64, 158, 255, 0.3);
  border-radius: 8px;
  padding: 16px 20px;
  margin-top: 16px;
}
.ppt-qa {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
}
.ppt-highlight-box {
  background: rgba(230, 162, 60, 0.15);
  border: 1px solid rgba(230, 162, 60, 0.3);
  border-radius: 8px;
  padding: 16px 20px;
  margin-top: 16px;
}
.ppt-highlight {
  font-size: 18px;
  color: #e6a23c;
  margin: 0;
}
.slide-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
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
}
.slide-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #dcdfe6;
  cursor: pointer;
}
.dot.active {
  background: var(--el-color-primary);
}
</style>
