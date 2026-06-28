import { ElMessage } from 'element-plus'
import { trackAuthEvent } from '@/utils/authAnalytics'

export function requireAuthAgreement(agreed: boolean): boolean {
  if (!agreed) {
    trackAuthEvent('login_agreement_blocked')
    ElMessage.warning('请先阅读并同意用户协议与隐私政策')
    return false
  }
  return true
}
