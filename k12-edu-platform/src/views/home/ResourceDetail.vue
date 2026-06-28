<template>
  <div class="resource-detail-page">
    <div class="container">
      <!-- 1. 顶部面包屑导航 -->
      <div class="breadcrumb-nav">
        <!-- 从学科浏览页过来时显示完整可点击的路径面包屑 -->
        <BrowseBreadcrumb v-if="detailBreadcrumbItems.length > 1" :items="detailBreadcrumbItems" />
        <!-- 其他来源（直接访问、分享链接等）使用简洁面包屑 -->
        <el-breadcrumb v-else separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="subjectBackLink" :to="subjectBackLink">学科资源</el-breadcrumb-item>
          <el-breadcrumb-item v-else :to="{ path: '/feature' }">班会育人</el-breadcrumb-item>
          <el-breadcrumb-item>{{ currentChannel }}</el-breadcrumb-item>
          <el-breadcrumb-item class="current">{{ resource?.title || '资源详情' }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <ResourceDetailMeta
        v-if="resource"
        :resource="resource"
        :grade-level-map="gradeLevelMap"
        :format-resource-type="formatResourceType"
        :format-media-type="formatMediaType"
        :format-count="formatCount"
        :resource-type-tag-type="resourceTypeTagType"
      >
        <template #actions>
          <ResourceDetailActions
            :is-collected="isCollected"
            :downloading="downloading"
            @download="handleDownload"
            @collect="handleCollect"
            @share="handleShare"
            @add-to-folder="handleAddToFolder"
          />
        </template>
      </ResourceDetailMeta>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="8" animated />
      </div>

      <!-- 加载失败 / 无数据 -->
      <div v-else-if="!resource" class="empty-state">
        <el-empty description="资源不存在或暂时无法加载">
          <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
        </el-empty>
      </div>

      <!-- 同课文切换条（学科资源场景） -->
      <LessonSameLessonBar
        v-if="showSameLessonBar && resource && !loading"
        :lesson-name="lessonName"
        :parent-unit="lessonParentUnit"
        :active-type="activeLessonType"
        :subject-back-link="subjectBackLink"
        :type-counts="sameLessonTypeCounts"
        @select-type="handleSelectLessonType"
      />

      <!-- 资源核心内容区（统一布局） -->
      <div class="resource-content" v-if="resource && !loading">
        <div class="content-main">
          <ResourcePreviewPanel
            ref="previewPanelRef"
            :resource="resource"
            :preview-type="previewType"
            :is-full-preview="isFullPreview"
            :current-slide="currentSlide"
            :slide-count="slideCount"
            :is-playing="isPlaying"
            :audio-progress="audioProgress"
            :current-time="currentTime"
            :audio-duration="audioDuration"
            :pdf-url="pdfUrl"
            :playback-url="playbackUrl"
            :document-preview-url="documentPreviewUrl"
            :document-preview-mode="documentPreviewMode"
            :file-preview-info="filePreviewInfo"
            :preview-loading="previewLoading"
            :preview-processing="previewProcessing"
            :preview-error="previewError"
            @toggle-fullscreen="toggleFullPreview"
            @slide-count="setSlideCount"
            @prev-slide="prevSlide"
            @next-slide="nextSlide"
            @goto-slide="(i) => (currentSlide = i)"
            @toggle-audio="toggleAudio"
            @audio-loaded="onAudioLoaded"
            @audio-timeupdate="onAudioTimeUpdate"
            @seek-audio="seekAudio"
          />

          <LessonPlanStructured
            v-if="showLessonPlanStructured"
            :data="lessonPlanStructured"
            collapsible
            :expanded="lessonPlanExpanded"
            @toggle="toggleLessonPlanExpanded"
          />

          <!-- 附件下载区 -->
          <el-card class="download-card">
            <template #header>
              <div class="card-header">
                <span class="header-title">📦 附件下载</span>
              </div>
            </template>
            
            <div class="download-list">
              <div class="download-item" v-for="(file, index) in attachFiles" :key="index">
                <div class="file-icon" :class="file.iconClass">
                  <span class="file-ext">{{ file.ext }}</span>
                </div>
                <div class="file-info">
                  <span class="file-name">{{ file.name }}</span>
                  <span class="file-desc">{{ file.desc }}</span>
                  <span class="file-size">{{ file.size }}</span>
                </div>
                <el-button type="primary" plain @click="downloadFile(file)">
                  <el-icon><Download /></el-icon>
                  下载
                </el-button>
              </div>
            </div>
          </el-card>

          <!-- 3.3 资源详情区 -->
          <el-card class="detail-card">
            <template #header>
              <div class="card-header">
                <span class="header-title">📋 资源详情</span>
              </div>
            </template>
            
            <div class="detail-content">
              <h3 class="section-title">资源简介</h3>
              <p class="description">{{ resource.description || '通过视频、故事、讨论等环节培养学生感恩意识，引导学生理解父母、老师的付出，学会表达感恩之情，适合中小学各年级开展主题班会使用。' }}</p>
              
              <h3 class="section-title">资源亮点</h3>
              <ul class="highlights">
                <li v-for="(h, i) in resourceHighlights" :key="i">
                  <span class="highlight-icon">{{ h.icon }}</span>
                  <span><strong>{{ h.title }}：</strong>{{ h.desc }}</span>
                </li>
              </ul>
              
              <h3 class="section-title">适用信息</h3>
              <el-descriptions
                :column="2"
                border
                size="small"
                class="resource-descriptions"
              >
                <el-descriptions-item label="资源类型">{{ formatResourceType(resource.resourceType) }}</el-descriptions-item>
                <el-descriptions-item label="适用学段">{{ gradeLevelMap[resource.gradeLevel] || resource.gradeLevel || '小学' }}</el-descriptions-item>
                <el-descriptions-item label="适用年级">{{ resource.grade || '—' }}</el-descriptions-item>
                <el-descriptions-item label="教材版本">{{ resource.version || '通用版' }}</el-descriptions-item>
                <el-descriptions-item label="学科">{{ resource.subject || '—' }}</el-descriptions-item>
                <el-descriptions-item label="上传时间">{{ (resource.uploadTime || '').substring(0, 10) || '—' }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-card>
        </div>

        <!-- 侧栏：教学流程 + 同课文资源 + 拓展资源 -->
        <div class="content-sidebar">
          <el-card v-if="teachingFlowSteps.length" class="teaching-flow-card">
            <template #header>
              <div class="card-header">
                <div class="card-header-left">
                  <span class="header-title">📚 教学流程</span>
                  <span v-if="lessonPlanStructured.source === 'fallback'" class="header-hint">智能生成</span>
                </div>
                <el-button text size="small" @click="flowExpanded = !flowExpanded">
                  {{ flowExpanded ? '收起' : '展开' }}
                  <el-icon :class="{ 'rotate-icon': flowExpanded }"><ArrowDown /></el-icon>
                </el-button>
              </div>
            </template>

            <el-collapse-transition>
              <div v-show="flowExpanded">
                <el-timeline>
                  <el-timeline-item
                    v-for="(step, index) in teachingFlowSteps"
                    :key="index"
                    :timestamp="step.duration"
                    placement="top"
                    :color="step.color"
                  >
                    <h4>{{ step.title }}</h4>
                    <p v-if="step.content">{{ step.content }}</p>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </el-collapse-transition>
          </el-card>

          <LessonSameLessonSidebar
            v-if="showSameLessonBar"
            :items="filteredSameLessonResources"
            :current-id="resource.id"
            :loading="sameLessonLoading"
            @open="goToSameLessonResource"
          />

          <el-card class="related-resources-card">
            <template #header>
              <div class="card-header">
                <span class="header-title">🔗 拓展资源</span>
                <span v-if="relatedResources.length" class="header-hint">同课文 · 其他类型 · {{ relatedResources.length }} 条</span>
              </div>
            </template>

            <div v-if="relatedResourcesLoading" class="related-loading">
              <el-skeleton :rows="3" animated />
            </div>
            <div v-else-if="!relatedResources.length" class="related-empty">
              <p>暂无同课文拓展资源</p>
              <p class="empty-hint">上传同目录下的课件、教案、练习后将在此展示</p>
            </div>
            <div v-else class="related-card-list">
              <div
                class="related-card-item"
                v-for="item in relatedResources"
                :key="item.id"
                @click="goToResource(item.id)"
              >
                <div class="related-color-bar" :style="{ background: resourceTypeColor(item.type) }"></div>
                <div class="related-info">
                  <span class="related-title">{{ item.title }}</span>
                  <span class="related-meta">⬇ {{ formatCount(item.downloadCount) }}</span>
                </div>
                <el-tag size="small" :type="resourceTypeTagType(item.type)" effect="plain">
                  {{ item.type ? formatResourceType(item.type) : '资源' }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 5. 底部同类型推荐区 -->
      <div class="recommend-section" v-if="!loading && (recommendResourcesLoading || recommendResources.length > 0)">
        <h2 class="section-header">{{ recommendSectionTitle }}</h2>
        <div v-if="recommendResourcesLoading" class="recommend-loading">
          <el-skeleton :rows="2" animated />
        </div>
        <div v-else class="recommend-grid">
          <div
            class="recommend-card" 
            v-for="item in recommendResources" 
            :key="item.id" 
            @click="goToResource(item.id)"
          >
            <div class="recommend-cover" :style="{ background: getRecommendColor(item.id) }">
              <span class="recommend-icon">{{ item.icon || '📄' }}</span>
              <el-tag
                v-if="item.type"
                size="small"
                :type="resourceTypeTagType(item.type)"
                effect="dark"
                class="recommend-type-badge"
              >
                {{ formatResourceType(item.type) }}
              </el-tag>
            </div>
            <div class="recommend-info">
              <h4 class="recommend-title">{{ item.title }}</h4>
              <div class="recommend-meta">
                <span v-if="item.lessonName">{{ item.lessonName }}</span>
                <span v-else-if="item.unitName">{{ item.unitName }}</span>
                <span>⬇ {{ item.downloadCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分享弹窗 -->
    <el-dialog v-model="showShareDialog" title="分享资源" width="420px">
      <div class="share-content">
        <p class="share-title">{{ resource?.title }}</p>
        <div class="share-url">
          <el-input v-model="shareUrl" readonly>
            <template #append>
              <el-button @click="copyShareUrl">复制</el-button>
            </template>
          </el-input>
        </div>
        <div class="share-platforms">
          <div class="platform-btn" @click="shareToWechat">
            <div class="platform-icon wechat-icon">💬</div>
            <span>微信</span>
          </div>
          <div class="platform-btn" @click="shareToQQ">
            <div class="platform-icon qq-icon">🐧</div>
            <span>QQ</span>
          </div>
          <div class="platform-btn" @click="shareToWorkWechat">
            <div class="platform-icon">💼</div>
            <span>企业微信</span>
          </div>
          <div class="platform-btn" @click="copyShareUrl">
            <div class="platform-icon">🔗</div>
            <span>复制链接</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, watchEffect } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowDown, Download } from '@element-plus/icons-vue'
import ResourceDetailMeta from '@/components/resource-detail/ResourceDetailMeta.vue'
import ResourceDetailActions from '@/components/resource-detail/ResourceDetailActions.vue'
import ResourcePreviewPanel from '@/components/resource-detail/ResourcePreviewPanel.vue'
import LessonPlanStructured from '@/components/resource-detail/LessonPlanStructured.vue'
import LessonSameLessonBar from '@/components/resource-detail/LessonSameLessonBar.vue'
import LessonSameLessonSidebar from '@/components/resource-detail/LessonSameLessonSidebar.vue'
import BrowseBreadcrumb from '@/components/subject/BrowseBreadcrumb.vue'
import { useResourceDetail } from '@/composables/useResourceDetail'
import { useRecentViews } from '@/composables/useRecentViews'
import { buildCompactDetailUrl } from '@/utils/detailUrl'

const previewPanelRef = ref<InstanceType<typeof ResourcePreviewPanel> | null>(null)
const flowExpanded = ref(false)

const {
  resource, loading, isCollected, downloading, showShareDialog, shareUrl,
  lessonPlanExpanded, isFullPreview, previewType, currentSlide, slideCount,
  audioPlayer, isPlaying, audioProgress, currentTime, audioDuration,
  attachFiles, relatedResources, relatedResourcesLoading,
  recommendResources, recommendResourcesLoading, recommendSectionTitle, resourceHighlights,
  currentChannel, subjectBackLink, detailBreadcrumbItems, gradeLevelMap, pdfUrl, playbackUrl,
  documentPreviewUrl, documentPreviewMode,
  filePreviewInfo, previewLoading, previewProcessing, previewError, setSlideCount,
  showLessonPlanStructured, showSameLessonBar, lessonPlanStructured, teachingFlowSteps,
  lessonName, lessonParentUnit, activeLessonType, filteredSameLessonResources, sameLessonLoading, sameLessonTypeCounts,
  formatResourceType, formatMediaType, formatCount, resourceTypeTagType, resourceTypeColor, getRecommendColor,
  handleDownload, handleCollect, handleShare, handleAddToFolder,
  copyShareUrl, shareToWechat, shareToQQ, shareToWorkWechat,
  prevSlide, nextSlide, toggleFullPreview, toggleAudio, onAudioLoaded,
  onAudioTimeUpdate, seekAudio, downloadFile, goToResource,
  goToSameLessonResource, handleSelectLessonType, toggleLessonPlanExpanded,
} = useResourceDetail()

const route = useRoute()
const { addView } = useRecentViews()

watchEffect(() => {
  const el = previewPanelRef.value?.audioPlayer
  if (el) audioPlayer.value = el
})

// 资源加载后记录浏览历史（含完整元数据用于筛选）
watch(resource, (val) => {
  if (val?.id) {
    addView({
      id: val.id,
      title: val.title || '',
      subject: val.subject || val.gradeLevel || '',
      stageKey: val.gradeLevel || '',
      stage: (val.gradeLevel === 'primary' ? '小学' : val.gradeLevel === 'junior' ? '初中' : val.gradeLevel === 'senior' ? '高中' : val.gradeLevel) || '',
      gradeName: val.grade || '',
      teachingType: val.resourceType ? (val.resourceType.includes('courseware') ? '课件' : val.resourceType.includes('lesson_plan') ? '教案' : val.resourceType.includes('exercise') ? '练习' : val.resourceType.includes('exam') ? '试卷' : '') : '',
      fileExt: val.fileFormat || val.contentType || '',
      ossUrl: val.fileUrl || '',
      url: buildCompactDetailUrl(route.path, route.query as any),
    })
  }
}, { immediate: true })
</script>

<style scoped>
.resource-detail-page {
  background: #f5f7fa;
  min-height: 100vh;
  padding: 20px 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 面包屑导航 */
.breadcrumb-nav {
  margin-bottom: 20px;
}

.breadcrumb-nav .current {
  color: var(--el-color-primary);
}

.empty-state {
  background: #fff;
  border-radius: 12px;
  padding: 48px 24px;
  margin-top: 12px;
}

/* ==================== 资源头部 ==================== */
.resource-header {
  background: #fff;
  border-radius: 12px;
  padding: 28px 32px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.header-left {
  flex: 1;
}

.resource-title {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 16px;
  line-height: 1.4;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.key-stats {
  display: flex;
  gap: 24px;
  color: #909399;
  font-size: 14px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  flex-shrink: 0;
}

/* 加载状态 */
.loading-state {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
}

/* ==================== 内容区域 ==================== */
.resource-content {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 20px;
  margin-bottom: 20px;
}

.content-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 卡片通用 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* ==================== 预览区 ==================== */
.preview-card {
  border-radius: 12px;
}

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
  top: 0;
  left: 0;
  width: 100vw !important;
  height: 100vh !important;
  z-index: 9999;
  aspect-ratio: auto;
  border-radius: 0;
}

.slide-content {
  width: 100%;
  height: 100%;
}

.slide-page {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* PPT 封面样式 */
.ppt-cover {
  text-align: center;
  color: #fff;
  padding: 40px;
  position: relative;
}

.ppt-cover-deco {
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, #409EFF, #67C23A);
  margin: 0 auto 24px;
  border-radius: 2px;
}

.ppt-cover-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #fff 0%, #e0e0ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.ppt-cover-sub {
  font-size: 16px;
  color: rgba(255,255,255,0.7);
  margin-bottom: 8px;
}

.ppt-cover-footer {
  position: absolute;
  bottom: -60px;
  left: 0;
  right: 0;
  color: rgba(255,255,255,0.4);
  font-size: 12px;
}

/* PPT 内页样式 */
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
  border-bottom: 2px solid rgba(255,255,255,0.2);
}

.ppt-content-block {
  margin-bottom: 20px;
}

.ppt-text {
  font-size: 18px;
  line-height: 2;
  color: rgba(255,255,255,0.9);
  padding-left: 20px;
  position: relative;
}

.ppt-text::before {
  content: '▸';
  position: absolute;
  left: 0;
  color: #409EFF;
}

.ppt-qa-box {
  background: rgba(64,158,255,0.15);
  border: 1px solid rgba(64,158,255,0.3);
  border-radius: 8px;
  padding: 16px 20px;
  margin-top: 16px;
}

.ppt-qa {
  font-size: 16px;
  color: rgba(255,255,255,0.85);
  margin: 0;
}

.ppt-highlight-box {
  background: rgba(230,162,60,0.15);
  border: 1px solid rgba(230,162,60,0.3);
  border-radius: 8px;
  padding: 16px 20px;
  margin-top: 16px;
}

.ppt-highlight {
  font-size: 18px;
  color: #E6A23C;
  margin: 0;
  font-weight: 500;
}

/* 幻灯片导航 */
.slide-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255,255,255,0.9);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.slide-nav:hover {
  background: #fff;
  transform: translateY(-50%) scale(1.1);
}

.slide-nav.prev { left: 16px; }
.slide-nav.next { right: 16px; }

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
  transition: all 0.2s;
}

.dot:hover {
  background: #c0c4cc;
}

.dot.active {
  background: var(--el-color-primary);
  transform: scale(1.2);
}

/* 视频预览 */
.preview-video video {
  border-radius: 8px;
  width: 100%;
}

/* 音频预览 */
.preview-audio .audio-player {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 24px;
}

.audio-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.audio-title {
  font-weight: 600;
  font-size: 16px;
}

.audio-duration {
  color: #909399;
}

.audio-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.play-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--el-color-primary);
  border: none;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: #e4e7ed;
  border-radius: 3px;
  cursor: pointer;
  position: relative;
}

.progress {
  height: 100%;
  background: var(--el-color-primary);
  border-radius: 3px;
  transition: width 0.1s;
}

.time-display {
  color: #909399;
  font-size: 13px;
  white-space: nowrap;
}

/* 文档预览 */
.preview-document iframe {
  border: none;
  border-radius: 8px;
}

/* 不支持预览 */
.preview-unsupported {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.preview-unsupported .tip {
  font-size: 13px;
  margin-top: 8px;
}

/* ==================== 教案预览 ==================== */
.lesson-plan-card {
  border-radius: 12px;
}

.rotate-icon {
  transform: rotate(90deg);
}

.lesson-plan-content {
  padding: 8px 0;
}

.plan-section {
  margin-bottom: 20px;
}

.plan-section h4 {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
  padding-left: 12px;
  border-left: 3px solid var(--el-color-primary);
}

.plan-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.plan-section li {
  padding: 6px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

/* ==================== 资源详情区 ==================== */
.detail-card {
  border-radius: 12px;
}

.detail-content {
  padding: 4px 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-left: 12px;
  border-left: 3px solid var(--el-color-primary);
}

.description {
  color: #606266;
  line-height: 1.8;
  margin-bottom: 20px;
  font-size: 15px;
}

.highlights {
  list-style: none;
  padding: 0;
  margin: 0 0 20px 0;
}

.highlights li {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.highlight-icon {
  flex-shrink: 0;
  font-size: 16px;
}

/* el-descriptions 表格化详情 */
.resource-descriptions :deep(.el-descriptions__cell) {
  padding: 8px 12px;
}
.resource-descriptions :deep(.el-descriptions__label) {
  color: #909399;
  font-weight: 500;
  width: 100px;
}

/* 旧版 apply-info 已不再使用，移除以避免干扰 */

/* ==================== 附件下载区 ==================== */
.download-card {
  border-radius: 12px;
}

.download-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.download-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.2s;
}

.download-item:hover {
  background: #ecf5ff;
}

.file-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-ppt {
  background: linear-gradient(135deg, #ff6b35, #ff4444);
}

.icon-doc {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
}

.icon-zip {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
}

.file-ext {
  color: #fff;
  font-size: 13px;
  font-weight: 700;
}

.file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.file-desc {
  font-size: 12px;
  color: #909399;
}

.file-size {
  font-size: 12px;
  color: #c0c4cc;
}

/* ==================== 侧边栏 ==================== */
.content-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.teaching-flow-card,
.related-resources-card {
  border-radius: 12px;
}

.card-header .header-hint {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
}

.related-loading,
.related-empty {
  padding: 8px 4px 12px;
}

.related-empty p {
  margin: 0 0 6px;
  font-size: 13px;
  color: #606266;
}

.related-empty .empty-hint {
  font-size: 12px;
  color: #909399;
}

/* 侧边栏拓展资源 —— 微型卡片 */
.related-card-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.related-card-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: #fafbfc;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.related-card-item:hover {
  background: #ecf5ff;
  transform: translateX(2px);
}
.related-color-bar {
  width: 3px;
  height: 32px;
  border-radius: 2px;
  flex-shrink: 0;
}
.related-info {
  flex: 1;
  min-width: 0;
}
.related-title {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}
.related-meta {
  font-size: 12px;
  color: #909399;
}

/* ==================== 底部推荐区 ==================== */
.recommend-section {
  margin-bottom: 20px;
}

.recommend-loading {
  padding: 8px 0 16px;
}

.section-header {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 4px solid var(--el-color-primary);
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.recommend-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.recommend-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}

.recommend-cover {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.recommend-icon {
  font-size: 40px;
}

.recommend-type-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  font-size: 11px;
  padding: 0 6px;
  height: 20px;
  line-height: 20px;
  border: none;
}

.recommend-info {
  padding: 12px 16px;
}

.recommend-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.recommend-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

/* ==================== 分享弹窗 ==================== */
.share-content {
  text-align: center;
}

.share-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 16px;
}

.share-url {
  margin-bottom: 20px;
}

.share-platforms {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.platform-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: transform 0.2s;
}

.platform-btn:hover {
  transform: scale(1.1);
}

.platform-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  background: #f5f7fa;
}

.wechat-icon {
  background: #07c160;
  color: #fff;
}

.qq-icon {
  background: #12b7f5;
  color: #fff;
}

.platform-btn span {
  font-size: 12px;
  color: #909399;
}

/* ==================== 响应式 ==================== */
@media (max-width: 1024px) {
  .resource-content {
    grid-template-columns: 1fr;
  }
  
  .recommend-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .resource-header {
    flex-direction: column;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .recommend-grid {
    grid-template-columns: 1fr;
  }
  
  .rating-stats {
    flex-direction: column;
    gap: 20px;
  }
}
</style>
