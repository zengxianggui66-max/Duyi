/**
 * 会员相关 API
 */
import { request } from './request'
import type { ApiResult, SilentRequestConfig } from './request'

// ==================== 类型定义 ====================

export interface MemberInfo {
  id: number
  userId: number
  level: string
  expireTime?: string
  privileges: string[]
}

export type MemberLevel = 0 | 1 | 2 | 3 // 0: 普通用户, 1: 月度会员, 2: 年度会员, 3: 永久会员

// ==================== API 方法 ====================

export const memberApi = {
  /**
   * 创建会员订单
   */
  createOrder(memberLevel: MemberLevel) {
    return request.post<ApiResult<{ orderId: string; payUrl: string }>>(
      '/member/order',
      null,
      { params: { memberLevel } }
    )
  },

  /**
   * 获取会员信息
   */
  getInfo() {
    return request.get<ApiResult<MemberInfo>>('/member/info', {
      silentError: true,
    } as SilentRequestConfig)
  },

  /**
   * 验证会员权限
   */
  checkPrivilege(privilegeCode: string) {
    return request.get<ApiResult<{ hasPrivilege: boolean }>>(
      '/member/check-privilege',
      {
        params: { privilegeCode },
      }
    )
  },
}
