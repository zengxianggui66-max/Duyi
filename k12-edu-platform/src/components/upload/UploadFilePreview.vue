<template>
  <div v-if="file" class="upload-file-preview">
    <div class="preview-toolbar">
      <span class="preview-title">文件预览</span>
      <div class="toolbar-tags">
        <el-tag :type="tagType" size="small">{{ previewLabel }}</el-tag>
        <el-tag v-if="hasServerPreview" type="success" size="small" effect="plain">已上传云端</el-tag>
        <el-tag
          v-if="previewKind === 'ppt-viewer' && serverPreviewInfo?.previewMode === 'slides'"
          type="primary"
          size="small"
          effect="plain"
        >
          真实幻灯片
        </el-tag>
        <el-tag
          v-else-if="serverProvider === 'libreoffice'"
          type="primary"
          size="small"
          effect="plain"
        >
          PDF 转码
        </el-tag>
        <el-tag
          v-else-if="serverProvider === 'poi'"
          type="info"
          size="small"
          effect="plain"
        >
          HTML 摘要
        </el-tag>
      </div>
    </div>

    <div v-if="loading" class="preview-loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>{{ loadingText }}</span>
    </div>

    <el-alert
      v-if="serverWarning && !loading"
      type="warning"
      :closable="false"
      show-icon
      :title="serverWarning"
      class="preview-server-warning"
    />

    <el-alert
      v-if="error && !loading && !hasLocalPreview"
      type="warning"
      :closable="false"
      show-icon
      :title="error"
      class="preview-error"
    />

    <template v-if="canShowPreview">
      <!-- 本地预览（阶段 A） -->
      <div v-if="showLocal && previewKind === 'image'" class="preview-box preview-image">
        <img :src="blobUrl" :alt="file.name" />
      </div>

      <div v-else-if="showLocal && previewKind === 'document'" class="preview-box preview-document">
        <iframe :src="blobUrl" :title="file.name" />
      </div>

      <div v-else-if="showLocal && previewKind === 'video'" class="preview-box preview-video">
        <video :src="blobUrl" controls playsinline />
      </div>

      <div v-else-if="showLocal && previewKind === 'audio'" class="preview-box preview-audio">
        <audio :src="blobUrl" controls />
      </div>

      <div
        v-else-if="showLocal && previewKind === 'docx'"
        class="preview-box preview-docx"
        v-html="docxHtml"
      />

      <!-- 云端预览（阶段 B） -->
      <div v-else-if="showServer && previewKind === 'image'" class="preview-box preview-image">
        <img :src="serverUrl" :alt="file.name" />
      </div>

      <div v-else-if="showServer && previewKind === 'document'" class="preview-box preview-document">
        <iframe :src="serverUrl" :title="file.name" />
      </div>

      <div v-else-if="showServer && previewKind === 'video'" class="preview-box preview-video">
        <video :src="serverUrl" controls playsinline />
      </div>

      <div v-else-if="showServer && previewKind === 'audio'" class="preview-box preview-audio">
        <audio :src="serverUrl" controls />
      </div>

      <div
        v-else-if="showServer && previewKind === 'ppt-viewer'"
        class="preview-box preview-ppt-upload"
      >
        <PreviewPptViewer
          :title="file.name"
          :preview-info="serverPreviewInfo ?? null"
          :original-url="serverUrl"
          :is-full-preview="false"
          :current-slide="uploadSlide"
          @prev-slide="uploadSlide = Math.max(0, uploadSlide - 1)"
          @next-slide="uploadSlide = Math.min(uploadSlideMax, uploadSlide + 1)"
          @goto-slide="uploadSlide = $event"
          @slide-count="uploadSlideMax = Math.max(0, $event - 1)"
        />
      </div>

      <div v-else-if="showServer && previewKind === 'server-html'" class="preview-box preview-server-html">
        <iframe :src="serverUrl" :title="file.name" class="html-iframe" />
        <p v-if="serverMessage" class="html-hint">{{ serverMessage }}</p>
      </div>

      <div v-else-if="showServer && previewKind === 'office-embed'" class="preview-box preview-office">
        <iframe
          v-if="officeEmbedUrl"
          :src="officeEmbedUrl"
          class="office-iframe"
          :title="file.name"
        />
        <div class="office-fallback">
          <p v-if="serverMessage">{{ serverMessage }}</p>
          <p v-else>正在通过在线文档服务加载预览（PPT/Word/Excel）。若无法显示，请使用下方按钮打开原文件。</p>
          <el-button type="primary" link @click="openServerFile">在新窗口打开原文件</el-button>
        </div>
      </div>
    </template>

    <el-alert
      v-else-if="formatMeta?.isPreviewable && !loading"
      type="info"
      :closable="false"
      show-icon
      class="preview-hint"
    >
      文件已上传至云端，预览加载中或需公网可访问的文件地址。提交前请确认内容无误。
    </el-alert>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import type { FilePreviewInfo } from '@/api/file'
import type { FormatPreviewMeta } from '@/utils/previewType'
import { canLocalPreview, getPreviewStatusLabel } from '@/utils/previewType'
import { buildOfficeEmbedUrl } from '@/utils/officePreview'
import PreviewPptViewer from '@/components/resource-detail/preview/PreviewPptViewer.vue'

type PreviewKind =
  | 'image'
  | 'document'
  | 'video'
  | 'audio'
  | 'docx'
  | 'server-html'
  | 'ppt-viewer'
  | 'office-embed'
  | 'none'

const uploadSlide = ref(0)
const uploadSlideMax = ref(0)

const props = defineProps<{
  file: File | null
  formatMeta: FormatPreviewMeta | null
  previewKind: PreviewKind | string
  blobUrl: string
  docxHtml: string
  serverUrl: string
  serverProvider?: string
  serverMessage?: string
  serverPreviewInfo?: FilePreviewInfo | null
  hasLocalPreview: boolean
  hasServerPreview: boolean
  loading: boolean
  error: string
  serverWarning?: string
}>()

const showLocal = computed(() => props.hasLocalPreview && !!props.previewKind && props.previewKind !== 'none')

const showServer = computed(() => {
  if (showLocal.value) return false
  if (props.previewKind === 'ppt-viewer' && props.serverPreviewInfo) return true
  return props.hasServerPreview && !!props.serverUrl
})

watch(
  () => props.file?.name,
  () => {
    uploadSlide.value = 0
    uploadSlideMax.value = 0
  },
)

const canShowPreview = computed(
  () =>
    (showLocal.value || showServer.value) &&
    !props.loading &&
    (!props.error || props.hasLocalPreview),
)

const previewLabel = computed(() =>
  getPreviewStatusLabel(props.formatMeta, {
    hasLocal: props.hasLocalPreview,
    hasServer: props.hasServerPreview,
    serverProvider: props.serverProvider,
  }),
)

const loadingText = computed(() => {
  if (canLocalPreview(props.formatMeta)) {
    return '正在生成本地预览并同步至云端…'
  }
  return '正在上传至云端并生成转码预览…'
})

const tagType = computed(() => {
  if (props.error) return 'danger'
  if (props.hasLocalPreview || props.hasServerPreview) return 'success'
  if (props.formatMeta?.isPreviewable) return 'warning'
  return 'info'
})

const officeEmbedUrl = computed(() => {
  if (props.previewKind !== 'office-embed' || !props.serverUrl) return null
  return buildOfficeEmbedUrl(props.serverUrl)
})

function openServerFile() {
  if (props.serverUrl) window.open(props.serverUrl, '_blank', 'noopener')
}
</script>

<style scoped>
.upload-file-preview {
  margin-top: 16px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  overflow: hidden;
  background: #fafbfc;
}

.preview-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  gap: 12px;
}

.toolbar-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: flex-end;
}

.preview-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  flex-shrink: 0;
}

.preview-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 48px;
  color: #909399;
  font-size: 14px;
}

.preview-error,
.preview-server-warning,
.preview-hint {
  margin: 12px;
}

.preview-box {
  background: #fff;
}

.preview-image {
  display: flex;
  justify-content: center;
  padding: 16px;
  max-height: 480px;
  overflow: auto;
}

.preview-image img {
  max-width: 100%;
  max-height: 440px;
  object-fit: contain;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.preview-document iframe,
.office-iframe,
.html-iframe {
  width: 100%;
  height: 520px;
  border: none;
  display: block;
}

.preview-ppt-upload {
  background: #fff;
}

.preview-server-html .html-hint {
  margin: 0;
  padding: 10px 16px;
  font-size: 12px;
  color: #909399;
  border-top: 1px solid #ebeef5;
}

.preview-video {
  padding: 12px;
  background: #000;
}

.preview-video video {
  width: 100%;
  max-height: 420px;
  display: block;
}

.preview-audio {
  padding: 24px 16px;
}

.preview-audio audio {
  width: 100%;
}

.preview-docx {
  padding: 20px 24px;
  max-height: 520px;
  overflow: auto;
  font-size: 14px;
  line-height: 1.75;
  color: #303133;
}

.preview-docx :deep(p) {
  margin: 0 0 0.75em;
}

.preview-docx :deep(h1),
.preview-docx :deep(h2),
.preview-docx :deep(h3) {
  margin: 1em 0 0.5em;
  font-weight: 600;
}

.preview-docx :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 1em;
}

.preview-docx :deep(td),
.preview-docx :deep(th) {
  border: 1px solid #dcdfe6;
  padding: 6px 10px;
}

.preview-office .office-fallback {
  padding: 12px 16px;
  font-size: 13px;
  color: #606266;
  border-top: 1px solid #ebeef5;
}
</style>
