<template>
  <div class="resource-card" @click="$emit('click', resource)">
    <!-- 文档图标 -->
    <div :class="['doc-icon', iconClass]">
      {{ iconLetter }}
    </div>

    <!-- 资源信息 -->
    <div class="card-info">
      <h4 class="card-title">{{ resource.title }}</h4>
      <div class="card-meta">
        <span v-if="resource.uploadTime" class="meta-item">
          {{ formatDate(resource.uploadTime) }}
        </span>
        <span v-if="resource.downloadCount" class="meta-item">
          {{ resource.downloadCount }}次下载
        </span>
        <span v-if="resource.uploader" class="meta-item">
          {{ resource.uploader }}
        </span>
        <span v-if="resource.format" class="meta-item format">
          {{ resource.format }}
        </span>
      </div>
      <!-- 专辑信息 -->
      <div v-if="resource.albumName" class="card-album">
        专辑：<span class="album-name">{{ resource.albumName }}</span>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="card-actions" @click.stop>
      <el-tooltip content="预览" placement="top">
        <button class="action-btn" @click="$emit('preview', resource)">
          <View />
        </button>
      </el-tooltip>
      <el-tooltip content="下载" placement="top">
        <button class="action-btn primary" @click="$emit('download', resource)">
          <Download />
        </button>
      </el-tooltip>
      <el-tooltip content="收藏" placement="top">
        <button class="action-btn" @click="$emit('collect', resource)">
          <Star />
        </button>
      </el-tooltip>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { View, Download, Star } from '@element-plus/icons-vue'

interface Resource {
  id: string | number
  title: string
  uploadTime?: string
  downloadCount?: number
  uploader?: string
  format?: string
  fileExt?: string
  albumName?: string
  docType?: string
}

const props = defineProps<{
  resource: Resource
}>()

defineEmits<{
  click: [resource: Resource]
  preview: [resource: Resource]
  download: [resource: Resource]
  collect: [resource: Resource]
}>()

// 根据文件类型获取图标字母
const iconLetter = computed(() => {
  const ext = props.resource.fileExt?.toLowerCase() || props.resource.format?.toLowerCase() || ''
  const typeMap: Record<string, string> = {
    'pdf': 'PDF',
    'doc': 'DOC',
    'docx': 'DOC',
    'xls': 'XLS',
    'xlsx': 'XLS',
    'ppt': 'PPT',
    'pptx': 'PPT',
    'jpg': 'JPG',
    'jpeg': 'JPG',
    'png': 'PNG',
    'gif': 'GIF',
    'mp4': 'MP4',
    'mp3': 'MP3',
    'zip': 'ZIP',
    'rar': 'RAR',
  }
  return typeMap[ext] || (props.resource.docType || 'W').substring(0, 3).toUpperCase()
})

// 根据文件类型获取图标样式类
const iconClass = computed(() => {
  const ext = props.resource.fileExt?.toLowerCase() || props.resource.format?.toLowerCase() || ''
  const classMap: Record<string, string> = {
    'pdf': 'icon-pdf',
    'doc': 'icon-doc',
    'docx': 'icon-doc',
    'xls': 'icon-xls',
    'xlsx': 'icon-xls',
    'ppt': 'icon-ppt',
    'pptx': 'icon-ppt',
    'jpg': 'icon-img',
    'jpeg': 'icon-img',
    'png': 'icon-img',
    'gif': 'icon-img',
    'mp4': 'icon-video',
    'mp3': 'icon-audio',
    'zip': 'icon-zip',
    'rar': 'icon-zip',
  }
  return classMap[ext] || 'icon-default'
})

// 格式化日期
function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  return dateStr.substring(0, 10)
}
</script>

<style scoped>
.resource-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: var(--space-md, 16px);
  background: var(--bg-card, #fff);
  border-radius: var(--radius-md, 10px);
  cursor: pointer;
  transition: all var(--transition-normal, 0.25s ease);
  border: 1px solid var(--border-light, #F0F2F8);
  box-shadow: var(--shadow-card, 0 1px 4px rgba(0, 0, 0, 0.04));
}

.resource-card:hover {
  background: #F8FAFD;
  border-color: var(--color-primary, #4361EE);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(67, 97, 238, 0.1);
}

/* 文档图标 */
.doc-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}

.icon-pdf { background: linear-gradient(135deg, #FF6B6B, #FF8E53); }
.icon-doc { background: linear-gradient(135deg, #409EFF, #66B1FF); }
.icon-xls { background: linear-gradient(135deg, #67C23A, #85CE61); }
.icon-ppt { background: linear-gradient(135deg, #E6A23C, #F56C6C); }
.icon-img { background: linear-gradient(135deg, #909399, #C0C4CC); }
.icon-video { background: linear-gradient(135deg, #9C27B0, #BA68C8); }
.icon-audio { background: linear-gradient(135deg, #00BCD4, #4DD0E1); }
.icon-zip { background: linear-gradient(135deg, #795548, #A1887F); }
.icon-default { background: linear-gradient(135deg, #607D8B, #90A4AE); }

/* 资源信息 */
.card-info {
  flex: 1;
  min-width: 0;
}

.card-title {
  /* Body: 14px/400 */
  font-size: var(--fs-body, 14px);
  font-weight: var(--fw-body, 400);
  color: var(--text-primary, #1A1A2E);
  margin: 0 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: var(--fs-caption, 12px);
  color: var(--text-secondary, #8E8EA0);
}

.meta-item {
  display: inline-flex;
  align-items: center;
}

.meta-item.format {
  padding: 2px 8px;
  background: #F5F7FA;
  border-radius: 4px;
  font-weight: 600;
  color: #606266;
}

.card-album {
  margin-top: 6px;
  font-size: 12px;
  color: #8C9AA8;
}

.album-name {
  color: var(--color-primary, #4361EE);
  cursor: pointer;
}

.album-name:hover {
  text-decoration: underline;
}

/* 操作按钮 */
.card-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.resource-card:hover .card-actions {
  opacity: 1;
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #F5F7FA;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  color: #606266;
}

.action-btn:hover {
  background: var(--color-primary-bg, #EBF0FF);
  color: var(--color-primary, #4361EE);
}

.action-btn.primary {
  background: var(--color-primary, #4361EE);
  color: #fff;
}

.action-btn.primary:hover {
  background: var(--color-primary-light, #6B83F2);
}
</style>
