import { inject } from 'vue'
import { UPLOAD_FORM_KEY, type UploadFormContext } from './useResourceUploadForm'

export function useUploadFormInject(): UploadFormContext {
  const ctx = inject(UPLOAD_FORM_KEY)
  if (!ctx) throw new Error('useUploadFormInject must be used within ResourceUpload page')
  return ctx
}
