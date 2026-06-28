/**
 * 登录/注册行为埋点（DEV 控制台 + 可选 session 汇总，便于后续接入正式统计）
 */
export type AuthAnalyticsEvent =
  | 'login_tab_switch'
  | 'login_submit_sms'
  | 'login_submit_password'
  | 'login_oauth_start'
  | 'login_agreement_blocked'
  | 'register_tab_switch'
  | 'register_submit_sms'
  | 'register_submit_account'
  | 'sms_code_send_success'
  | 'sms_code_send_fail'
  | 'captcha_dialog_open'
  | 'captcha_dialog_confirm'

const SESSION_KEY = 'auth.analytics.events'

export function trackAuthEvent(
  event: AuthAnalyticsEvent,
  payload?: Record<string, unknown>,
): void {
  const record = {
    event,
    payload,
    ts: Date.now(),
  }

  if (import.meta.env.DEV) {
    console.info('[auth-analytics]', event, payload ?? '')
  }

  try {
    const raw = sessionStorage.getItem(SESSION_KEY)
    const list: typeof record[] = raw ? JSON.parse(raw) : []
    list.push(record)
    if (list.length > 100) list.splice(0, list.length - 100)
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(list))
  } catch {
    // ignore quota / private mode
  }
}
