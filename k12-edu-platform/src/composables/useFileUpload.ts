/**
 * 文件上传 composable
 * 统一管理文件上传逻辑和状态
 */
import { ref, computed, shallowRef } from 'vue'
import { ElMessage } from 'element-plus'
import { fileApi } from '@/api'
import type { UploadResult } from '@/api/types'

export interface UploadFile {
  file: File
  name: string
  size: number
  progress: number
  status: 'pending' | 'uploading' | 'success' | 'error'
  result?: UploadResult
  error?: string
}

export interface UseFileUploadOptions {
  maxSize?: number // MB
  allowedTypes?: string[]
  maxFiles?: number
  onSuccess?: (result: UploadResult, file: File) => void
  onError?: (error: Error, file: File) => void
  onProgress?: (progress: number, file: File) => void
}

const DEFAULT_ALLOWED_TYPES = [
  '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx',
  '.pdf', '.txt', '.zip', '.rar',
  '.jpg', '.jpeg', '.png', '.gif', '.mp4', '.mp3'
]

export function useFileUpload(options: UseFileUploadOptions = {}) {
  const {
    maxSize = 50, // 默认 50MB
    allowedTypes = DEFAULT_ALLOWED_TYPES,
    maxFiles = 10,
    onSuccess,
    onError,
    onProgress,
  } = options

  const files = shallowRef<UploadFile[]>([])
  const uploading = ref(false)
  const uploadProgress = ref(0)

  // 总体进度
  const totalProgress = computed(() => {
    if (files.value.length === 0) return 0
    const total = files.value.reduce((sum, f) => sum + f.progress, 0)
    return Math.round(total / files.value.length)
  })

  // 是否有文件正在上传
  const isUploading = computed(() => uploading.value)

  // 待上传文件数量
  const pendingCount = computed(() =>
    files.value.filter(f => f.status === 'pending').length
  )

  // 上传成功文件数量
  const successCount = computed(() =>
    files.value.filter(f => f.status === 'success').length
  )

  // 上传失败文件数量
  const errorCount = computed(() =>
    files.value.filter(f => f.status === 'error').length
  )

  // 验证文件
  function validateFile(file: File): string | null {
    // 检查文件大小
    if (file.size > maxSize * 1024 * 1024) {
      return `文件 ${file.name} 超过 ${maxSize}MB 限制`
    }

    // 检查文件数量
    if (files.value.length >= maxFiles) {
      return `最多只能上传 ${maxFiles} 个文件`
    }

    // 检查文件类型
    const ext = '.' + file.name.split('.').pop()?.toLowerCase()
    if (!allowedTypes.includes(ext)) {
      return `文件 ${file.name} 类型不支持`
    }

    return null
  }

  // 添加文件
  function addFiles(newFiles: FileList | File[]) {
    const fileArray = Array.from(newFiles)
    const added: UploadFile[] = []

    for (const file of fileArray) {
      const error = validateFile(file)
      if (error) {
        ElMessage.warning(error)
        continue
      }

      const uploadFile: UploadFile = {
        file,
        name: file.name,
        size: file.size,
        progress: 0,
        status: 'pending',
      }
      added.push(uploadFile)
    }

    if (added.length > 0) {
      files.value = [...files.value, ...added]
    }

    return added
  }

  // 移除文件
  function removeFile(index: number) {
    const newFiles = [...files.value]
    newFiles.splice(index, 1)
    files.value = newFiles
  }

  // 清除所有文件
  function clearFiles() {
    files.value = []
  }

  // 更新文件状态
  function updateFile(index: number, updates: Partial<UploadFile>) {
    const newFiles = [...files.value]
    newFiles[index] = { ...newFiles[index], ...updates }
    files.value = newFiles
  }

  // 上传单个文件
  async function uploadFile(index: number): Promise<UploadResult | null> {
    const fileItem = files.value[index]
    if (!fileItem || fileItem.status === 'uploading') return null

    updateFile(index, { status: 'uploading', progress: 0 })

    try {
      // 模拟上传进度（实际项目中需要通过 axios 的 onUploadProgress 获取）
      const progressTimer = setInterval(() => {
        const currentFile = files.value[index]
        if (currentFile && currentFile.progress < 90) {
          updateFile(index, { progress: currentFile.progress + 10 })
          onProgress?.(currentFile.progress + 10, fileItem.file)
        }
      }, 100)

      const formData = new FormData()
      formData.append('file', fileItem.file)

      const res = await fileApi.upload(formData)
      const result = res.data.data

      clearInterval(progressTimer)
      updateFile(index, { status: 'success', progress: 100, result })
      onSuccess?.(result, fileItem.file)

      return result
    } catch (e) {
      const error = e instanceof Error ? e : new Error(String(e))
      updateFile(index, { status: 'error', error: error.message })
      onError?.(error, fileItem.file)
      return null
    }
  }

  // 上传所有待上传文件
  async function uploadAll() {
    if (uploading.value) return

    uploading.value = true
    uploadProgress.value = 0

    const pendingIndexes = files.value
      .map((f, i) => ({ f, i }))
      .filter(({ f }) => f.status === 'pending')
      .map(({ i }) => i)

    try {
      for (let i = 0; i < pendingIndexes.length; i++) {
        await uploadFile(pendingIndexes[i])
        uploadProgress.value = Math.round(((i + 1) / pendingIndexes.length) * 100)
      }
    } finally {
      uploading.value = false
    }
  }

  // 格式化文件大小
  function formatSize(bytes: number): string {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  return {
    files,
    uploading,
    uploadProgress,
    totalProgress,
    isUploading,
    pendingCount,
    successCount,
    errorCount,
    validateFile,
    addFiles,
    removeFile,
    clearFiles,
    uploadFile,
    uploadAll,
    formatSize,
  }
}
