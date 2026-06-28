/**
 * 学科浏览 / 目录树类型（方案 A）
 */
export type DisplayMode = 'lesson_hub' | 'category_list' | 'unit_matrix'

export type CatalogNodeType = 'folder' | 'unit' | 'lesson' | 'section' | 'leaf'

export interface CatalogScheme {
  id: number
  code: string
  name: string
  brandId?: number
  brandCode?: string
  displayMode: DisplayMode
  sort?: number
}

export interface CatalogNode {
  id: number
  code: string
  name: string
  namePath: string
  depth: number
  nodeType: CatalogNodeType | string
  icon?: string
  meta?: Record<string, unknown>
  children?: CatalogNode[]
  /** 兼容旧单元树：课文名列表 */
  subUnits?: string[]
}

export interface CatalogBreadcrumbItem {
  id: number
  name: string
  code?: string
  nodeType?: string
}
