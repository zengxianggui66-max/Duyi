/**
 * 后端版本名称 ↔ 前端路由 key 适配
 * 与 subjectConfig.allVersions 及 oss 表 edition 字段对齐
 */
import type { VersionItem } from '@/config/subjectConfig'

export interface EditionVersionOption {
  key: string
  name: string
  isNew?: boolean
  /** 后端查询用的版本名（与 oss_primary_chinese_resource.edition 一致） */
  editionName: string
  id?: number
}

const STATIC_VERSIONS: VersionItem[] = [
  { id: 1, key: 'tongbian2024', name: '统编版(2024)', isNew: true },
  { id: 2, key: 'renjiao', name: '人教版', isNew: true },
  { id: 3, key: 'beishida', name: '北师大版', isNew: false },
  { id: 4, key: 'sujiao', name: '苏教版', isNew: false },
  { id: 5, key: 'hujiao', name: '沪教版', isNew: false },
  { id: 6, key: 'xishida', name: '西师大版', isNew: false },
  { id: 7, key: 'yuwen', name: '语文版', isNew: false },
  { id: 8, key: 'jijiao', name: '冀教版', isNew: false },
  { id: 9, key: 'tongbian2016', name: '统编版(2016)', isNew: false },
]

/** 名称归一化便于匹配（与后端 ResourceBrowseService 一致） */
export function normalizeEditionLabel(name: string) {
  return name
    .trim()
    .replace(/\s+/g, '')
    .replace(/（/g, '(')
    .replace(/）/g, ')')
}

function findStaticByName(name: string): VersionItem | undefined {
  const n = normalizeEditionLabel(name)
  return STATIC_VERSIONS.find(
    (v) =>
      normalizeEditionLabel(v.name) === n ||
      v.name === name.trim(),
  )
}

/** 根据版本名推断是否为新教材（2024/2025 或配置标记） */
export function inferEditionIsNew(name: string, staticItem?: VersionItem): boolean {
  if (staticItem?.isNew != null) return staticItem.isNew
  const n = name.trim()
  return /\(2024\)|\(2025\)|（2024）|（2025）|2024|2025/.test(n)
}

/** 版本名 → 前端路由 key */
export function editionNameToKey(name: string): string {
  const trimmed = name.trim()
  const staticItem = findStaticByName(trimmed)
  if (staticItem) return staticItem.key

  const n = normalizeEditionLabel(trimmed)
  if (n.includes('统编') && n.includes('2024')) return 'tongbian2024'
  if (n.includes('统编') && n.includes('2016')) return 'tongbian2016'
  if (n.includes('统编')) return 'tongbian2024'
  if (n.includes('人教')) return 'renjiao'
  if (n.includes('北师大')) return 'beishida'
  if (n.includes('苏教')) return 'sujiao'
  if (n.includes('沪教')) return 'hujiao'
  if (n.includes('西师大')) return 'xishida'
  if (n.includes('语文版')) return 'yuwen'
  if (n.includes('冀教')) return 'jijiao'

  return trimmed
    .replace(/[()（）\s]/g, '')
    .toLowerCase()
    .slice(0, 32) || 'unknown'
}

export function mapEditionNameToOption(
  name: string,
  id?: number,
): EditionVersionOption {
  const editionName = name.trim()
  const staticItem = findStaticByName(editionName)
  return {
    key: editionNameToKey(editionName),
    name: staticItem?.name ?? editionName,
    editionName,
    isNew: inferEditionIsNew(editionName, staticItem),
    id: id ?? staticItem?.id,
  }
}

/** edu-resource filter-options 中的 editions */
export function mapEduEditionRows(
  rows: Array<{ id?: number; name?: string } | Record<string, unknown>>,
): EditionVersionOption[] {
  const list: EditionVersionOption[] = []
  const seen = new Set<string>()
  for (const row of rows) {
    const name = String(row.name ?? '').trim()
    if (!name || seen.has(name)) continue
    seen.add(name)
    const id = typeof row.id === 'number' ? row.id : Number(row.id)
    list.push(mapEditionNameToOption(name, Number.isFinite(id) ? id : undefined))
  }
  return list
}

/** primary-chinese 返回的 editions 字符串列表 */
export function mapEditionStrings(names: string[]): EditionVersionOption[] {
  const list: EditionVersionOption[] = []
  const seen = new Set<string>()
  for (const raw of names) {
    const name = String(raw ?? '').trim()
    if (!name || seen.has(name)) continue
    seen.add(name)
    list.push(mapEditionNameToOption(name))
  }
  return list
}

/** 首页版本 key 与学科详情页路由对齐 */
export function normalizeVersionKeyForRoute(key: string) {
  if (key === 'tongbian') return 'tongbian2024'
  return key
}
