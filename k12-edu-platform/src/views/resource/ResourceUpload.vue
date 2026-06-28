<template>
  <div class="resource-upload-page">
    <div class="page-container">
      <div class="page-header">
        <h1 class="page-title">{{ isDraftEditMode ? '继续编辑草稿' : '上传教学资源' }}</h1>
        <p class="page-desc">
          <template v-if="isDraftEditMode">
            草稿已恢复，请按步骤完善上传位置、基本信息与文件后提交审核
          </template>
          <template v-else-if="isBrowseWizard">
            已带入当前学科页的栏目、目录与类型，确认后即可提交审核
          </template>
          <template v-else>
            请先选择上传位置，再填写资源信息与文件；同步备课资源提交后将进入审核队列
          </template>
        </p>
      </div>

      <el-alert
        v-if="isDraftEditMode"
        type="info"
        :closable="false"
        show-icon
        class="draft-banner"
        title="当前为草稿编辑模式"
        description="修改后可直接「保存草稿」或「提交审核」；提交前请确认册别、版本与文件均已填写完整。"
      />

      <el-alert
        v-if="usingOfflineOptions"
        type="warning"
        :closable="false"
        show-icon
        class="offline-banner"
        title="使用离线默认选项"
        description="分类服务暂不可用，当前展示的是本地兜底数据；提交审核已禁用，您仍可保存草稿。"
      />

      <div class="step-navigation">
        <el-steps :active="currentStep" finish-status="success" align-center>
          <template v-if="isBrowseWizard">
            <el-step title="上传资源" description="确认位置、文件与说明" />
            <el-step title="预览发布" description="确认并发布" />
          </template>
          <template v-else>
            <el-step title="选择位置" description="学段、栏目与教材目录" />
            <el-step title="基本信息" description="标题、简介与标签" />
            <el-step title="文件上传" description="上传教学文件" />
            <el-step
              v-if="showClassificationStep"
              title="分类配置"
              description="资源分类与属性"
            />
            <el-step title="预览发布" description="确认并发布" />
          </template>
        </el-steps>
      </div>

      <div class="step-content">
        <template v-if="isBrowseWizard">
          <div v-show="currentStep === 0" class="step-panel">
            <UploadContextCard
              :from-browse="fromBrowse"
              :stage-label="gradeLevelLabel"
              :subject-label="subjectLabel"
              :module="formData.module"
              :teaching-type="formData.teachingType"
              :grade-name="formData.gradeName"
              :edition-name="formData.editionName"
              :unit-name="formData.unitName"
              :lesson-name="formData.lessonName"
              :resource-mode="browseSnapshot.resourceMode"
            />
            <UploadRecommendPanel
              v-if="showUploadRecommendations"
              :recommendations="uploadRecommendations"
              :active-id="activeRecommendationId"
              :context-line="recommendContextLine"
              :auto-applied-hint="recommendAutoHint"
              :show-legacy="formData.gradeLevel === 'art' || formData.gradeLevel === 'dance'"
              :stage-key="formData.gradeLevel"
              @apply="applyRecommendationById"
              @legacy="applyLegacyTemplateById"
            />
            <UploadStepFile />
            <UploadStepBasic :browse-wizard-mode="true" />
          </div>
          <div v-show="currentStep === 1" class="step-panel">
            <UploadStepPreview />
          </div>
        </template>

        <template v-else>
          <div v-show="currentStep === 0" class="step-panel">
            <UploadStepPlacement />
            <UploadContextCard
              v-if="placementConfirmed"
              :from-browse="false"
              :stage-label="gradeLevelLabel"
              :subject-label="subjectLabel"
              :module="formData.module"
              :teaching-type="formData.teachingType"
              :grade-name="formData.gradeName"
              :edition-name="formData.editionName"
              :unit-name="formData.unitName"
              :lesson-name="formData.lessonName"
              :resource-mode="browseSnapshot.resourceMode"
            />
          </div>
          <div v-show="currentStep === 1" class="step-panel">
            <UploadStepBasic />
          </div>
          <div v-show="currentStep === 2" class="step-panel">
            <UploadStepFile />
          </div>
          <div v-show="currentStep === 3 && showClassificationStep" class="step-panel">
            <UploadStepClassification />
          </div>
          <div v-show="isDirectPreviewStep" class="step-panel">
            <UploadStepPreview />
          </div>
        </template>
      </div>

      <UploadWizardActions />
    </div>
  </div>
</template>

<script setup lang="ts">
import { provide } from 'vue'
import UploadStepBasic from '@/components/upload/UploadStepBasic.vue'
import UploadStepFile from '@/components/upload/UploadStepFile.vue'
import UploadStepClassification from '@/components/upload/UploadStepClassification.vue'
import UploadStepPreview from '@/components/upload/UploadStepPreview.vue'
import UploadStepPlacement from '@/components/upload/UploadStepPlacement.vue'
import UploadWizardActions from '@/components/upload/UploadWizardActions.vue'
import UploadContextCard from '@/components/upload/UploadContextCard.vue'
import UploadRecommendPanel from '@/components/upload/UploadRecommendPanel.vue'
import { useResourceUploadForm, UPLOAD_FORM_KEY } from '@/composables/useResourceUploadForm'

const uploadForm = useResourceUploadForm()
provide(UPLOAD_FORM_KEY, uploadForm)

const {
  currentStep,
  isBrowseWizard,
  isDraftEditMode,
  fromBrowse,
  placementConfirmed,
  showClassificationStep,
  isDirectPreviewStep,
  formData,
  browseSnapshot,
  gradeLevelLabel,
  subjectLabel,
  showUploadRecommendations,
  uploadRecommendations,
  activeRecommendationId,
  recommendContextLine,
  recommendAutoHint,
  applyRecommendationById,
  applyLegacyTemplateById,
  usingOfflineOptions,
} = uploadForm
</script>

<style scoped>
.resource-upload-page {
  min-height: 100vh;
  background: var(--bg-page);
  padding: 24px;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.page-desc {
  color: var(--text-secondary);
  margin: 0;
}

.step-navigation {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  margin-bottom: 24px;
}

.draft-banner {
  margin-bottom: 16px;
}

.offline-banner {
  margin-bottom: 16px;
}

.step-content {
  margin-bottom: 80px;
}

.step-panel {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

:deep(.form-card) {
  border-radius: var(--radius-lg);
}
</style>
