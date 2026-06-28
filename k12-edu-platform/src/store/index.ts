/**
 * Store 模块统一导出
 * Pinia 安装入口
 */
import { createPinia } from 'pinia'

// 导出各模块 Store
export { useUserStore } from './user'
export { useSearchStore } from './search'
export { useResourceStore } from './resource'
export { useNewsStore } from './news'
export { usePrepBasketStore } from './prepBasket'

// 创建 Pinia 实例
const pinia = createPinia()

export default pinia
