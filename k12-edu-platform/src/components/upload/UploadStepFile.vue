<template>
  <el-card class="form-card">
    <template #header>
      <div class="card-header">
        <span class="card-title">{{ isSuiteMode ? '成套文件上传' : '文件上传' }}</span>
        <span class="file-format-hint">
          {{ isSuiteMode ? '可一次选择多份文件，将发布到同一上传位置' : '支持 Word、PDF、PPT、视频、音频、图片等格式' }}
        </span>
      </div>
    </template>

    <el-alert
      v-if="isSuiteMode"
      type="success"
      :closable="false"
      show-icon
      class="suite-tip"
      title="成套模式：每份文件将生成一条资源记录，标题默认为「套件名-文件名」，可在下一步预览中确认。"
    />

    <el-alert
      v-if="draftSavedFile && !uploadFile && !isSuiteMode"
      type="success"
      :closable="false"
      show-icon
      class="draft-file-tip"
      :title="`草稿已保存文件：${draftSavedFile.name}`"
      description="如需更换文件，请重新选择上传；不更换可直接进入下一步提交审核。"
    />

    <el-upload
      drag
      :auto-upload="false"
      :limit="isSuiteMode ? 30 : 1"
      :multiple="isSuiteMode"
      :file-list="suiteFileList"
      accept=".doc,.docx,.pdf,.ppt,.pptx,.xls,.xlsx,.mp4,.avi,.mov,.mp3,.wav,.jpg,.jpeg,.png,.gif,.zip,.rar"
      @change="onChange"
      @remove="onRemove"
    >
      <el-icon class="upload-icon"><UploadFilled /></el-icon>
      <div class="el-upload__text">
        {{ isSuiteMode ? '拖拽多个文件到此处，或' : '拖拽文件到此处，或' }}
        <em>点击上传</em>
      </div>
    </el-upload>

    <div class="file-size-limits">
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          <span class="limit-item">文档类：50MB 以内</span>
          <span class="limit-item">PPT：100MB 以内</span>
          <span class="limit-item">视频类：500MB 以内</span>
          <span class="limit-item">音频类：100MB 以内</span>
          <span class="limit-item">图片类：20MB 以内</span>
        </template>
      </el-alert>
    </div>

    <div v-if="isSuiteMode && uploadFiles.length" class="suite-file-table">
      <el-divider content-position="left">已选 {{ uploadFiles.length }} 个文件</el-divider>
      <el-table :data="uploadFiles" size="small" border>
        <el-table-column type="index" label="#" width="48" />
        <el-table-column prop="name" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column label="大小" width="100">
          <template #default="{ row }">{{ formatFileSize(row.size) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ $index }">
            <el-button type="danger" link @click="removeSuiteFile($index)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div v-if="!isSuiteMode && uploadFile" class="file-info-panel">
      <el-divider content-position="left">已选文件</el-divider>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="文件名">{{ uploadFile.name }}</el-descriptions-item>
        <el-descriptions-item label="大小">{{ formatFileSize(uploadFile.size) }}</el-descriptions-item>
        <el-descriptions-item label="格式">{{ detectedFormat?.toUpperCase() }}</el-descriptions-item>
        <el-descriptions-item label="预览">
          <el-tag :type="previewTagType" size="small">
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
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { UploadFile, UploadFiles, UploadRawFile } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import UploadFilePreview from '@/components/upload/UploadFilePreview.vue'
import { useUploadFormInject } from '@/composables/useUploadFormInject'

const {
  uploadFile,
  uploadFiles,
  isSuiteMode,
  draftSavedFile,
  detectedFormat,
  formatMeta,
  activePreviewKind,
  hasLocalPreviewContent,
  hasServerPreview,
  serverPreviewUrl,
  serverPreviewProvider,
  serverPreviewMessage,
  serverPreviewInfo,
  previewUrl,
  docxPreviewHtml,
  previewLoading,
  previewError,
  serverPreviewWarning,
  previewStatusLabel,
  isPreviewable,
  setUploadFiles,
  removeSuiteFile,
  formatFileSize,
} = useUploadFormInject()

const suiteFileList = computed<UploadFile[]>(() =>
  uploadFiles.value.map((f, i) => ({
    name: f.name,
    uid: i,
    status: 'success',
  })),
)

const previewTagType = computed(() => {
  if (previewError.value) return 'danger'
  if (isPreviewable.value) return 'success'
  if (formatMeta.value?.isPreviewable) return 'warning'
  return 'info'
})

async function onChange(_uploadFile: UploadFile, files: UploadFiles) {
  const raws = files.map((f) => f.raw).filter((f): f is UploadRawFile => !!f)
  if (!raws.length) {
    await setUploadFiles([])
    return
  }
  const nextFiles = isSuiteMode.value ? raws : raws.slice(-1)
  await setUploadFiles(nextFiles)
}

async function onRemove() {
  await setUploadFiles([])
}
</script>

<style scoped>
.draft-file-tip {
  margin-bottom: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.card-title {
  font-weight: 600;
}
.file-format-hint {
  font-size: 12px;
  color: #909399;
}
.upload-icon {
  font-size: 48px;
  color: #409eff;
}
.file-size-limits {
  margin-top: 16px;
}
.suite-tip {
  margin-bottom: 16px;
}
.limit-item {
  margin-right: 16px;
  font-size: 13px;
}
.file-info-panel {
  margin-top: 16px;
}
.suite-file-table {
  margin-top: 16px;
}
</style>


