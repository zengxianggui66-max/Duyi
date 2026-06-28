<template>
  <el-card class="form-card">
    <template #header>
      <div class="card-header">
        <span class="card-title">资源信息预览</span>
      </div>
    </template>

    <div class="preview-container">
      <div class="resource-preview-header">
        <div class="resource-cover">
          <el-icon v-if="!uploadFile" class="cover-icon" :size="64"><Document /></el-icon>
          <img v-else-if="isImageFile" :src="previewUrl" alt="预览图" class="cover-image" />
          <el-icon v-else class="cover-icon" :size="64">
            <component :is="getFileIcon(detectedFormat)" />
          </el-icon>
        </div>
        <div class="resource-basic-info">
          <h2 class="resource-title">{{ formData.title || '未填写' }}</h2>
          <div class="resource-meta">
            <el-tag v-if="formData.gradeLevel" size="small">{{ gradeLevelLabel }}</el-tag>
            <el-tag v-if="formData.subject" type="success" size="small">{{ subjectLabel }}</el-tag>
            <el-tag v-if="formData.grade" type="warning" size="small">{{ gradeLabel }}</el-tag>
            <el-tag v-if="formData.isFree === 1" type="danger" size="small">免费</el-tag>
          </div>
        </div>
      </div>

      <el-descriptions :column="2" border class="resource-details">
        <el-descriptions-item label="资源简介" :span="2">
          {{ formData.description || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="栏目">{{ formData.module || '—' }}</el-descriptions-item>
        <el-descriptions-item label="资源类型">{{ resourceTypeLabel }}</el-descriptions-item>
        <el-descriptions-item v-if="!isSyncUpload" label="子类型">{{ subTypeLabel }}</el-descriptions-item>
        <el-descriptions-item label="教材年级">{{ formData.gradeName || gradeLabel || '未选择' }}</el-descriptions-item>
        <el-descriptions-item label="教材版本">{{ formData.editionName || formData.version || '未选择' }}</el-descriptions-item>
        <el-descriptions-item v-if="formData.unitName || formData.lessonName" label="教材目录" :span="2">
          {{ formData.lessonName ? `${formData.unitName} · ${formData.lessonName}` : formData.unitName }}
        </el-descriptions-item>
        <el-descriptions-item v-if="fromBrowse" label="资源形态">{{ resourceModeLabel }}</el-descriptions-item>
        <el-descriptions-item label="资源难度">{{ difficultyLabel }}</el-descriptions-item>
        <el-descriptions-item label="适用场景">{{ scenariosLabel }}</el-descriptions-item>
        <el-descriptions-item v-if="!uploadModuleSchema.hideExamPrep" label="备考类型" :span="2">{{ examTypesLabel }}</el-descriptions-item>
        <el-descriptions-item label="资源标签">{{ tagsLabel }}</el-descriptions-item>
        <el-descriptions-item label="排序权重">{{ formData.sortWeight }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="isSuiteMode && uploadFiles.length" class="file-preview">
        <el-divider content-position="left">成套文件（{{ uploadFiles.length }} 份）</el-divider>
        <el-table :data="suitePreviewRows" size="small" border>
          <el-table-column type="index" label="#" width="48" />
          <el-table-column prop="title" label="将发布标题" min-width="220" show-overflow-tooltip />
          <el-table-column prop="name" label="文件名" min-width="160" show-overflow-tooltip />
          <el-table-column prop="sizeLabel" label="大小" width="90" />
        </el-table>
      </div>

      <div v-else-if="uploadFile" class="file-preview">
        <el-divider content-position="left">文件信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="文件名">{{ uploadFile.name }}</el-descriptions-item>
          <el-descriptions-item label="文件大小">{{ formatFileSize(uploadFile.size) }}</el-descriptions-item>
          <el-descriptions-item label="文件格式">{{ detectedFormat?.toUpperCase() }}</el-descriptions-item>
          <el-descriptions-item label="预览状态">
            <el-tag :type="isPreviewable ? 'success' : 'info'" size="small">
              {{ previewStatusLabel }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <UploadFilePreview
          :file="uploadFile"
          :format-meta="formatMeta"
          :preview-kind="activePreviewKind"
          :blob-url="previewUrl"
          :docx-html="docxPreviewHtml"
          :server-url="serverPreviewUrl"
          :server-provider="serverPreviewProvider"
          :server-message="serverPreviewMessage"
          :server-preview-info="serverPreviewInfo"
          :has-local-preview="hasLocalPreviewContent"
          :has-server-preview="hasServerPreview"
          :loading="previewLoading"
          :error="previewError"
          :server-warning="serverPreviewWarning"
        />
      </div>

      <div class="submit-confirm">
        <el-alert
          v-if="!canSubmit"
          type="warning"
          :closable="false"
          show-icon
          title="请完善以下信息后再提交"
        >
          <ul class="confirm-list">
            <li v-if="!formData.title">资源名称未填写</li>
            <li v-if="!formData.description">资源简介未填写</li>
            <li v-if="!formData.gradeLevel">学段未选择</li>
            <li v-if="!formData.subject">学科未选择</li>
            <li v-if="!hasUploadFiles">未上传文件</li>
            <li v-if="isSyncUpload && !formData.teachingType">资源类型未选择</li>
            <li v-if="isSyncUpload && !placementConfirmed && !fromBrowse">请先完成上传位置选择</li>
            <li v-if="isSyncUpload && !formData.gradeName">教材册别未选择</li>
            <li v-if="isSyncUpload && !formData.editionName">教材版本未选择</li>
          </ul>
        </el-alert>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Document } from '@element-plus/icons-vue'
import UploadFilePreview from '@/components/upload/UploadFilePreview.vue'
import { suggestSuiteItemTitle } from '@/utils/uploadRoute'
import { useUploadFormInject } from '@/composables/useUploadFormInject'

const {
  formData,
  uploadFile,
  uploadFiles,
  isSuiteMode,
  hasUploadFiles,
  uploadModuleSchema,
  previewUrl,
  formatMeta,
  activePreviewKind,
  hasLocalPreviewContent,
  hasServerPreview,
  serverPreviewUrl,
  serverPreviewProvider,
  serverPreviewMessage,
  serverPreviewInfo,
  docxPreviewHtml,
  previewLoading,
  previewError,
  serverPreviewWarning,
  isImageFile,
  detectedFormat,
  isPreviewable,
  previewStatusLabel,
  canSubmit,
  fromBrowse,
  placementConfirmed,
  isSyncUpload,
  resourceModeLabel,
  gradeLevelLabel,
  subjectLabel,
  gradeLabel,
  resourceTypeLabel,
  subTypeLabel,
  difficultyLabel,
  scenariosLabel,
  examTypesLabel,
  tagsLabel,
  formatFileSize,
  getFileIcon,
} = useUploadFormInject()

const suitePreviewRows = computed(() =>
  uploadFiles.value.map((file, index) => ({
    name: file.name,
    sizeLabel: formatFileSize(file.size),
    title: suggestSuiteItemTitle(formData.title, file.name, index + 1),
  })),
)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-size: 18px;
  font-weight: 600;
}
.resource-preview-header {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}
.resource-cover {
  width: 120px;
  height: 120px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}
.resource-title {
  font-size: 20px;
  margin: 0 0 12px;
}
.resource-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.resource-details {
  margin-bottom: 20px;
}
.file-preview {
  margin-bottom: 20px;
}
.confirm-list {
  margin: 8px 0 0;
  padding-left: 20px;
}
</style>
