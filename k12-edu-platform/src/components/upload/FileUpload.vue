<template>
  <div class="upload-container">
    <!-- 上传区域 -->
    <el-upload
      ref="uploadRef"
      class="upload-area"
      drag
      :action="uploadUrl"
      :headers="headers"
      :data="uploadData"
      :auto-upload="false"
      :show-file-list="true"
      :file-list="fileList"
      :accept="acceptTypes"
      :before-upload="handleBeforeUpload"
      :on-change="handleChange"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-remove="handleRemove"
      :on-preview="handlePreview"
      multiple
    >
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">
        拖拽文件到此处，或 <em>点击上传</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          <span>支持以下格式：</span>
          <div class="format-tags">
            <el-tag v-for="fmt in formatTags" :key="fmt" size="small">{{ fmt }}</el-tag>
          </div>
          <div class="size-tip">单个文件最大 {{ maxSize }}MB</div>
        </div>
      </template>
    </el-upload>

    <!-- 上传进度 -->
    <div v-if="uploading" class="upload-progress">
      <el-progress :percentage="uploadPercentage" :status="uploadStatus" />
      <div class="progress-text">{{ uploadText }}</div>
    </div>

    <!-- 上传结果 -->
    <div v-if="uploadResults.length > 0" class="upload-results">
      <div class="results-title">上传结果</div>
      <div v-for="(result, index) in uploadResults" :key="index" class="result-item">
        <div class="result-info">
          <el-icon v-if="result.success" color="#67c23a"><CircleCheck /></el-icon>
          <el-icon v-else color="#f56c6c"><CircleClose /></el-icon>
          <span class="result-name">{{ result.name }}</span>
          <span class="result-size">{{ formatSize(result.size) }}</span>
        </div>
        <div class="result-message">{{ result.message }}</div>
        <el-button v-if="result.success" type="primary" link @click="viewResource(result)">
          查看资源
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { UploadFilled, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const props = defineProps({
  // 上传URL
  uploadUrl: {
    type: String,
    default: '/api/file/upload'
  },
  // 是否自动上传
  autoUpload: {
    type: Boolean,
    default: false
  },
  // 文件数量限制
  limit: {
    type: Number,
    default: 10
  },
  // 最大文件大小(MB)
  maxSize: {
    type: Number,
    default: 500
  },
  // 允许的文件类型
  accept: {
    type: String,
    default: '.doc,.docx,.pdf,.ppt,.pptx,.xls,.xlsx,.mp4,.avi,.mov,.mp3,.wav,.flac'
  },
  // 上传时附加的数据
  extraData: {
    type: Object,
    default: () => ({})
  },
  // 用户ID
  userId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['success', 'error', 'change', 'remove'])

const uploadRef = ref(null)
const fileList = ref([])
const uploading = ref(false)
const uploadPercentage = ref(0)
const uploadStatus = ref('')
const uploadText = ref('')
const uploadResults = ref([])

// 请求头
const headers = computed(() => ({
  // 如果需要token认证，添加Authorization头
  // Authorization: localStorage.getItem('token')
}))

// 上传时附加的数据
const uploadData = computed(() => ({
  ...props.extraData,
  userId: props.userId || localStorage.getItem('userId')
}))

// 接受的格式标签
const formatTags = computed(() => {
  return props.accept.split(',').map(ext => ext.replace('.', '').toUpperCase())
})

// 接受的格式（用于el-upload）
const acceptTypes = computed(() => props.accept)

// 上传前校验
const handleBeforeUpload = (file) => {
  const extension = file.name.split('.').pop().toLowerCase()
  const acceptExts = props.accept.split(',').map(ext => ext.replace('.', '').toLowerCase())
  
  if (!acceptExts.includes(extension)) {
    ElMessage.error(`不支持 ${extension.toUpperCase()} 格式！`)
    return false
  }
  
  const fileSizeMB = file.size / 1024 / 1024
  if (fileSizeMB > props.maxSize) {
    ElMessage.error(`文件大小不能超过 ${props.maxSize}MB！`)
    return false
  }
  
  uploadText.value = `正在上传: ${file.name}`
  return true
}

// 文件状态改变
const handleChange = (file, files) => {
  fileList.value = files
  emit('change', files)
}

// 上传成功
const handleSuccess = (response, file, fileList) => {
  if (response.code === 200) {
    const result = {
      name: file.name,
      size: file.size,
      success: true,
      message: '上传成功',
      data: response.data,
      resourceId: response.data?.id
    }
    uploadResults.value.push(result)
    emit('success', result)
    ElMessage.success(`${file.name} 上传成功`)
  } else {
    const result = {
      name: file.name,
      size: file.size,
      success: false,
      message: response.message || '上传失败'
    }
    uploadResults.value.push(result)
    emit('error', result)
    ElMessage.error(`${file.name} 上传失败: ${response.message}`)
  }
  
  uploadPercentage.value = Math.round((uploadResults.value.length / fileList.length) * 100)
  
  if (uploadResults.value.length === fileList.length) {
    uploading.value = false
    uploadStatus.value = uploadResults.value.every(r => r.success) ? 'success' : 'warning'
  }
}

// 上传失败
const handleError = (error, file, fileList) => {
  const result = {
    name: file.name,
    size: file.size,
    success: false,
    message: '网络错误，上传失败'
  }
  uploadResults.value.push(result)
  uploading.value = false
  uploadStatus.value = 'exception'
  emit('error', result)
  ElMessage.error(`${file.name} 上传失败`)
}

// 删除文件
const handleRemove = (file, fileList) => {
  fileList.value = fileList
  emit('remove', file)
}

// 预览文件
const handlePreview = (file) => {
  // 可以在这里打开文件预览
  console.log('预览文件:', file)
}

// 查看资源
const viewResource = (result) => {
  // 跳转到资源详情页
  if (result.resourceId) {
    window.open(`/resource/${result.resourceId}`, '_blank')
  }
}

// 格式化文件大小
const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 手动上传
const submitUpload = () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }
  uploading.value = true
  uploadPercentage.value = 0
  uploadStatus.value = ''
  uploadResults.value = []
  uploadRef.value.submit()
}

// 清空列表
const clearList = () => {
  uploadRef.value.clearFiles()
  fileList.value = []
  uploadResults.value = []
}

// 暴露方法
defineExpose({
  submitUpload,
  clearList
})
</script>

<style scoped>
.upload-container {
  padding: 20px;
}

.upload-area {
  width: 100%;
}

.format-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 10px 0;
}

.format-tags .el-tag {
  font-size: 12px;
}

.size-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}

.upload-progress {
  margin-top: 20px;
}

.progress-text {
  text-align: center;
  color: #606266;
  margin-top: 10px;
}

.upload-results {
  margin-top: 20px;
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.results-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 15px;
}

.result-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 10px;
}

.result-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.result-name {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-size {
  color: #909399;
  font-size: 12px;
}

.result-message {
  color: #606266;
  font-size: 12px;
  flex: 1;
  text-align: center;
}
</style>
