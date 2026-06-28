/**
 * 认证相关 API
 */
import { request } from './request'
import type { ApiResult, SilentRequestConfig } from './request'

export interface LoginData {
  username: string
  password: string
  role?: string
}

export interface SmsLoginData {
  phone: string
  code: string
  role?: string
}

export interface RegisterData {
  username: string
  password: string
  email?: string
  nickname?: string
  role?: string
  agreedToTerms: string
}

export interface SmsRegisterData {
  phone: string
  code: string
  role?: string
  nickname?: string
  agreedToTerms: string
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  role: string
  roleName?: string
  memberLevel: string | number
  avatar?: string
  phone?: string
  email?: string
  /** 0保密 1男 2女 */
  gender?: 0 | 1 | 2
  /** YYYY-MM-DD */
  birthday?: string
  createTime?: string
  needBindPhone?: boolean
  oauthBinds?: string[]
}

export interface UpdateProfileData {
  nickname: string
  avatar?: string
  gender?: 0 | 1 | 2
  birthday?: string
  email?: string
}

export interface UpdateProfileResult {
  userInfo: UserInfo
  token?: string
  accessToken?: string
  expiresIn?: number
}

export interface LoginResult {
  token: string
  accessToken?: string
  expiresIn?: number
  userInfo: UserInfo
  needBindPhone?: boolean
  isNewUser?: boolean
  bindSuccess?: boolean
  needsRoleSelection?: boolean
  oauthType?: string
  oauthId?: string
  nickname?: string
  avatar?: string
}

export interface OAuthBindItem {
  type: string
  typeName: string
  bound: boolean
  nickname?: string
  avatar?: string
  bindTime?: string
  lastLoginAt?: string
  canUnbind?: boolean
  unbindReason?: string
}

export interface OAuthBindLogItem {
  id?: number | string
  action: 'bind' | 'unbind' | 'login' | string
  provider?: string
  providerName?: string
  result: 'success' | 'fail' | string
  reasonCode?: string
  reasonMessage?: string
  ip?: string
  createdAt?: string
}

export interface OAuthCompleteData {
  type: string
  oauthId: string
  nickname?: string
  avatar?: string
  role?: string
}

export interface CaptchaResult {
  captchaKey: string
  question: string
  captchaType?: string
}

export const authApi = {
  login(data: LoginData) {
    return request.post<ApiResult<LoginResult>>('/auth/login', data)
  },

  register(data: RegisterData) {
    return request.post<ApiResult<LoginResult>>('/auth/register', data)
  },

  smsRegister(data: SmsRegisterData) {
    return request.post<ApiResult<LoginResult>>('/auth/sms/register', data)
  },

  getCaptcha(silent = false) {
    return request.get<ApiResult<CaptchaResult>>('/auth/captcha', {
      silentError: silent,
    } as SilentRequestConfig)
  },

  sendSmsCode(data: {
    phone: string
    type?: string
    captchaKey: string
    captchaCode: string
  }) {
    return request.post<ApiResult>('/auth/sms/send', data)
  },

  smsLogin(data: SmsLoginData) {
    return request.post<ApiResult<LoginResult>>('/auth/sms/login', data)
  },

  getOAuthUrl(type: string, redirectUri?: string, role?: string) {
    return request.get<ApiResult<{ type: string; state: string; authorizationUrl: string }>>(
      '/auth/oauth/url',
      { params: { type, redirectUri, mode: 'login', role } }
    )
  },

  getOAuthBindUrl(type: string, redirectUri?: string) {
    return request.get<ApiResult<{ type: string; state: string; authorizationUrl: string }>>(
      '/auth/oauth/bind-url',
      { params: { type, redirectUri } }
    )
  },

  oauthLogin(data: { code: string; type: string; state: string }) {
    return request.post<ApiResult<LoginResult>>('/auth/oauth/login', data)
  },

  completeOAuthRegister(data: OAuthCompleteData) {
    return request.post<ApiResult<LoginResult>>('/auth/oauth/complete', data)
  },

  listOAuthBinds() {
    return request.get<ApiResult<OAuthBindItem[]>>('/auth/oauth/binds')
  },

  listOAuthBindLogs(params?: { size?: number }) {
    return request.get<ApiResult<OAuthBindLogItem[]>>('/auth/oauth/logs', { params })
  },

  unbindOAuth(type: string) {
    return request.delete<ApiResult>(`/auth/oauth/bind/${type}`)
  },

  bindPhone(data: { phone: string; code: string }) {
    return request.post<ApiResult>('/auth/bind-phone', data)
  },

  changePhone(data: { newPhone: string; oldCode: string; newCode: string }) {
    return request.post<ApiResult>('/auth/change-phone', data)
  },

  changePassword(data: {
    oldPassword?: string
    newPassword: string
    phone?: string
    code?: string
  }) {
    return request.put<ApiResult>('/auth/password', data)
  },

  logout() {
    return request.post<ApiResult>('/auth/logout')
  },

  getCurrentUser() {
    return request.get<ApiResult<UserInfo>>('/auth/current', {
      silentError: true,
    } as SilentRequestConfig)
  },

  updateProfile(data: UpdateProfileData) {
    return request.put<ApiResult<UpdateProfileResult>>('/auth/profile', data)
  },
}

