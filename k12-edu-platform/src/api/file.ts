/**
 * 文件上传相关 API
 */
import { request } from './request'
import type { ApiResult } from './request'

// ==================== 类型定义 ====================

export interface UploadResult {
  fileName: string
  fileUrl: string
  filePath: string
  fileSize: number
  fileFormat: string
  contentType: string
  isPreviewable: boolean
  previewType: string
  uploadRecordId: number
  ossBucket?: string
  ossObjectKey?: string
  message?: string
}

export interface UploadResourceResult extends UploadResult {
  resourceId: number
}

export interface FileFormat {
  extension: string
  name: string
  isSupported: boolean
  maxSizeMb: number
}

/** 服务端预览详情（阶段 C） */
export interface FilePreviewInfo {
  previewUrl: string
  previewType: string
  previewMode: 'native' | 'pdf' | 'html' | 'slides' | 'embed' | 'processing' | 'none'
  originalUrl: string
  converted: boolean
  provider: string
  message?: string
  slideUrls?: string[]
  slideCount?: number
}

// ==================== API 方法 ====================

export const fileApi = {
  /**
   * 上传文件
   * @param formData 包含 file, title, description, gradeLevel, subject 等字段
   */
  upload(formData: FormData) {
    return request.post<ApiResult<UploadResult>>('/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  /**
   * 上传文件并创建资源
   * @param formData 包含 file, title, gradeLevel, subject 等字段
   */
  uploadResource(formData: FormData) {
    return request.post<ApiResult<UploadResourceResult>>(
      '/file/upload-resource',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    )
  },

  /**
   * 获取支持的文件格式列表
   */
  getFormats() {
    return request.get<ApiResult<FileFormat[]>>('/file/formats')
  },

  /**
   * 检查文件格式是否支持
   */
  checkFormat(filename: string) {
    return request.get<ApiResult<boolean>>('/file/check-format', {
      params: { filename },
    })
  },

  /**
   * 删除文件
   */
  delete(path: string) {
    return request.delete<ApiResult>('/file/delete', { params: { path } })
  },

  /**
   * 获取文件预览URL
   */
  getPreviewUrl(fileUrl: string) {
    return request.get<ApiResult<string>>('/file/preview', {
      params: { fileUrl },
    })
  },

  /**
   * 获取文件预览详情（转码 PDF / POI HTML / 嵌入兜底）
   */
  getPreviewInfo(fileUrl: string, config?: { timeout?: number; silentError?: boolean }) {
    return request.get<ApiResult<FilePreviewInfo>>('/file/preview-info', {
      params: { fileUrl },
      timeout: config?.timeout ?? 20000,
      silentError: config?.silentError,
    })
  },

  /**
   * 获取文件下载URL
   */
  getDownloadUrl(fileUrl: string) {
    return request.get<ApiResult<string>>('/file/download', {
      params: { fileUrl },
    })
  },
}

