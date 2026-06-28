/** 首页/模块通用 Tab */
export interface ModuleTab {
  key: string
  label: string
}

/** 行式资源列表项 */
export interface ResourceListItem {
  id?: number
  title: string
  date: string
  detailPath?: string
}

/** 模块主题色 */
export type ModuleTheme = 'orange' | 'blue' | 'green'
