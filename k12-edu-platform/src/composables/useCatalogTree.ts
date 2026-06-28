/**
 * 目录树 Composable（M2）
 * 按品牌解析 scheme，加载 catalog/tree，并与旧 activeUnit 兼容
 */
import { ref, computed, watch, type Ref, type ComputedRef } from 'vue'
import type { RouteLocationNormalizedLoaded } from 'vue-router'
import { catalogApi } from '@/api/catalog'
import { unwrapData } from '@/api/request'
import {
  findSeriesByCode,
  brandCodeForApi,
  type DisplayMode,
} from '@/config/resourceSeriesConfig'
import type { CatalogNode, CatalogScheme } from '@/types/browse'
import type { UnitItem } from './useUnitDirectory'
import { USE_CATALOG_BROWSE } from '@/config/featureFlags'
import { stageNames, type StageKey } from '@/config/subjectConfig'
import { findTextbookRootNode } from '@/utils/sortResourceTypes'
import { unitDataMap } from '@/config/unitData'

export interface UseCatalogTreeOptions {
  route: RouteLocationNormalizedLoaded
  selectedBrandCode: Ref<string>
  selectedVolumeId: Ref<string>
  currentGradeLevelName: Ref<string>
  selectedVersionName: Ref<string>
  currentSubject: Ref<{ key?: string; name?: string } | null>
  currentStage: Ref<string>
}

export function useCatalogTree(opts: UseCatalogTreeOptions) {
  const {
    route,
    selectedBrandCode,
    selectedVolumeId,
    currentGradeLevelName,
    selectedVersionName,
    currentSubject,
    currentStage,
  } = opts

  const schemes = ref<CatalogScheme[]>([])
  const activeScheme = ref<CatalogScheme | null>(null)
  const catalogTree = ref<CatalogNode[]>([])
  const activeNodeId = ref<number | null>(null)
  const loading = ref(false)
  const expandedNodeIds = ref<Set<number>>(new Set())
  const allExpanded = ref(false)

  const displayMode = computed<DisplayMode>(
    () => activeScheme.value?.displayMode || findSeriesByCode(selectedBrandCode.value)?.displayMode || 'lesson_hub',
  )

  const activeNode = computed(() => findNodeById(catalogTree.value, activeNodeId.value))

  /** 转为 CourseCatalog 兼容的单元列表（仅 unit + lesson 两层） */
  const unitListCompat = computed<UnitItem[]>(() => catalogTreeToUnitList(catalogTree.value))

  const activeUnitName = computed({
    get: () => activeNode.value?.name || '',
    set: (name: string) => {
      const node = findNodeByName(catalogTree.value, name)
      if (node) {
        selectNode(node.id)
      }
    },
  })

  function initNodeFromRoute() {
    const id = readRouteNodeId()
    if (id != null) {
      activeNodeId.value = id
    }
  }

  function readRouteNodeId(): number | null {
    const raw = route.query.node
    if (typeof raw === 'string' && raw) {
      const id = Number(raw)
      if (!Number.isNaN(id)) return id
    }
    return null
  }

  function hasRouteCatalogContext(): boolean {
    const q = route.query
    return (
      (typeof q.node === 'string' && !!q.node) ||
      (typeof q.lesson === 'string' && !!q.lesson) ||
      (typeof q.unit === 'string' && !!q.unit)
    )
  }

  /** 树加载后按 URL 恢复目录选中（面包屑/详情回跳） */
  function restoreActiveNodeFromRoute(): boolean {
    const routeNodeId = readRouteNodeId()
    if (routeNodeId != null && findNodeById(catalogTree.value, routeNodeId)) {
      selectNode(routeNodeId, true)
      return true
    }

    const lesson = route.query.lesson
    if (typeof lesson === 'string' && lesson) {
      const node = findNodeByName(catalogTree.value, lesson)
      if (node) {
        selectNode(node.id, true)
        return true
      }
    }

    const unit = route.query.unit
    if (typeof unit === 'string' && unit) {
      const node = findNodeByName(catalogTree.value, unit)
      if (node) {
        selectNode(node.id, true)
        return true
      }
    }

    return false
  }

  async function loadSchemes() {
    const brand = brandCodeForApi(selectedBrandCode.value)
    try {
      const res = await catalogApi.getSchemes({
        brandCode: brand,
        stage: stageNames[currentStage.value as StageKey] || currentStage.value,
        subject: currentSubject.value?.name,
      }, { silentError: true })
      schemes.value = unwrapData(res) || []
    } catch {
      schemes.value = []
    }
    resolveActiveScheme()
  }

  function resolveActiveScheme() {
    const series = findSeriesByCode(selectedBrandCode.value)
    const code = series?.defaultSchemeCode
    if (code) {
      activeScheme.value = schemes.value.find((s) => s.code === code) || schemes.value[0] || null
    } else {
      activeScheme.value = schemes.value[0] || null
    }
  }

  async function loadTree() {
    if (!USE_CATALOG_BROWSE) return
    if (!activeScheme.value) {
      await loadSchemes()
    }
    if (!activeScheme.value) {
      loadFallbackTree()
      return
    }

    const hadTree = catalogTree.value.length > 0
    if (!hadTree) {
      loading.value = true
    }
    try {
      const baseParams = {
        schemeId: activeScheme.value.id,
        schemeCode: activeScheme.value.code,
        volumeKey: selectedVolumeId.value || undefined,
        gradeName: currentGradeLevelName.value || undefined,
        edition: selectedVersionName.value || undefined,
        subject: currentSubject.value?.name,
      }
      const res = await catalogApi.getTree(baseParams, { silentError: true })
      let tree = unwrapData(res) || []
      // 某些历史目录 meta.subject 与前端口径不一致时，先按学科过滤会被清空；
      // 回退为不带 subject 的查询，避免误触发前端静态目录兜底。
      if (!tree.length && baseParams.subject) {
        const retryRes = await catalogApi.getTree(
          { ...baseParams, subject: undefined },
          { silentError: true },
        )
        tree = unwrapData(retryRes) || []
      }
      catalogTree.value = tree
      if (!catalogTree.value.length) {
        loadFallbackTree()
      }
      if (restoreActiveNodeFromRoute()) {
        // 已从 URL 恢复目录位置
      } else if (activeNodeId.value && findNodeById(catalogTree.value, activeNodeId.value)) {
        ensureExpandedForActive()
      } else if (catalogTree.value.length > 0) {
        activeNodeId.value = null
        const first = pickDefaultNode(catalogTree.value)
        if (first) {
          selectNode(first.id, false)
        }
      }
    } catch (e) {
      console.error('[useCatalogTree] loadTree failed', e)
      loadFallbackTree()
    } finally {
      loading.value = false
    }
  }


  function loadFallbackTree() {
    const fallback = buildFallbackCatalogTree(selectedVolumeId.value)
    catalogTree.value = fallback
    if (fallback.length > 0) {
      const currentStillExists = activeNodeId.value && findNodeById(fallback, activeNodeId.value)
      if (currentStillExists) {
        ensureExpandedForActive()
      } else {
        const first = pickDefaultNode(fallback)
        if (first) selectNode(first.id, false)
      }
    } else {
      activeNodeId.value = null
    }
  }
  function pickDefaultNode(nodes: CatalogNode[]): CatalogNode | null {
    const first = nodes[0]
    if (!first) return null
    if (displayMode.value === 'lesson_hub' && first.children?.length) {
      return first.children[0]
    }
    const firstUnit = findFirstUnitNode(nodes)
    if (firstUnit) return firstUnit
    return first
  }

  /** 同步备课默认选中第一个单元父节点（跳过课本根） */
  function findFirstUnitNode(nodes: CatalogNode[]): CatalogNode | null {
    for (const n of nodes) {
      const t = n.nodeType
      if (t === 'unit' || t === 'folder' || t === 'section') {
        return n
      }
      if (n.children?.length) {
        const found = findFirstUnitNode(n.children)
        if (found) return found
      }
    }
    return null
  }

  function selectNode(id: number, expandAncestors = true) {
    activeNodeId.value = id
    if (expandAncestors) {
      expandAncestorsOf(id)
    }
  }

  function expandAncestorsOf(targetId: number) {
    const path = findPathToNode(catalogTree.value, targetId)
    path.forEach((n) => expandedNodeIds.value.add(n.id))
  }

  function ensureExpandedForActive() {
    if (activeNodeId.value) {
      expandAncestorsOf(activeNodeId.value)
    }
  }

  function toggleNodeExpand(nodeId: number) {
    if (expandedNodeIds.value.has(nodeId)) {
      expandedNodeIds.value.delete(nodeId)
    } else {
      expandedNodeIds.value.add(nodeId)
    }
  }

  function isNodeExpanded(nodeId: number) {
    return allExpanded.value || expandedNodeIds.value.has(nodeId)
  }

  function toggleAllExpand() {
    allExpanded.value = !allExpanded.value
    if (allExpanded.value) {
      collectAllIds(catalogTree.value).forEach((id) => expandedNodeIds.value.add(id))
    } else {
      expandedNodeIds.value.clear()
      ensureExpandedForActive()
    }
  }

  function resolveParentUnitName(unitOrLessonName: string): string {
    if (!unitOrLessonName) return ''
    const node = findNodeByName(catalogTree.value, unitOrLessonName)
    if (!node) return unitOrLessonName
    if (node.nodeType === 'unit' || node.nodeType === 'folder') {
      return node.name
    }
    const parent = findParentNode(catalogTree.value, node.id)
    return parent?.name || unitOrLessonName
  }

  function isLessonLeaf(name: string): boolean {
    const node = findNodeByName(catalogTree.value, name)
    return node?.nodeType === 'lesson'
  }

  watch(selectedBrandCode, async () => {
    if (!hasRouteCatalogContext()) {
      activeNodeId.value = null
    }
    expandedNodeIds.value.clear()
    await loadSchemes()
    await loadTree()
  })

  watch(
    () => [
      selectedVolumeId.value,
      currentGradeLevelName.value,
      selectedVersionName.value,
      currentSubject.value?.key,
    ],
    () => {
      if (USE_CATALOG_BROWSE) {
        if (!hasRouteCatalogContext()) {
          activeNodeId.value = null
        }
        loadTree()
      }
    },
  )

  function resolvePlacementForUpload(nodeId: number | null): {
    unitName: string
    lessonName: string
  } {
    if (nodeId == null || nodeId <= 0) {
      return { unitName: '', lessonName: '' }
    }
    const node = findNodeById(catalogTree.value, nodeId)
    if (!node) return { unitName: '', lessonName: '' }
    const root = findTextbookRootNode(catalogTree.value)
    if (root && node.id === root.id) {
      return { unitName: '', lessonName: '' }
    }
    const parent = findParentNode(catalogTree.value, nodeId)
    const parentIsUnit =
      parent &&
      (parent.nodeType === 'unit' ||
        parent.nodeType === 'folder' ||
        parent.nodeType === 'section')
    const isLesson =
      node.nodeType === 'lesson' ||
      (!!parentIsUnit &&
        node.nodeType !== 'unit' &&
        node.nodeType !== 'folder')
    if (isLesson && parent) {
      return { unitName: parent.name, lessonName: node.name }
    }
    return { unitName: node.name, lessonName: '' }
  }

  function findNodeByIdPublic(nodeId: number | null) {
    return findNodeById(catalogTree.value, nodeId)
  }

  return {
    schemes,
    activeScheme,
    catalogTree,
    activeNodeId,
    activeNode,
    activeUnitName,
    unitListCompat,
    displayMode,
    resolvePlacementForUpload,
    findNodeById: findNodeByIdPublic,
    loading,
    expandedNodeIds,
    allExpanded,
    initNodeFromRoute,
    loadSchemes,
    loadTree,
    selectNode,
    toggleNodeExpand,
    isNodeExpanded,
    toggleAllExpand,
    resolveParentUnitName,
    isLessonLeaf,
  }
}

// ---------- 树工具 ----------

export function catalogTreeToUnitList(nodes: CatalogNode[]): UnitItem[] {
  return nodes
    .filter((n) => n.nodeType === 'unit' || (n.subUnits?.length ?? 0) > 0)
    .map((n) => ({
      name: n.name,
      subUnits: n.subUnits?.length
        ? [...n.subUnits]
        : (n.children || [])
            .filter((c) => c.nodeType === 'lesson')
            .map((c) => c.name),
      expanded: false,
    }))
}

function findNodeById(nodes: CatalogNode[], id: number | null): CatalogNode | null {
  if (id == null) return null
  for (const n of nodes) {
    if (n.id === id) return n
    if (n.children?.length) {
      const found = findNodeById(n.children, id)
      if (found) return found
    }
  }
  return null
}

function findNodeByName(nodes: CatalogNode[], name: string): CatalogNode | null {
  for (const n of nodes) {
    if (n.name === name) return n
    if (n.children?.length) {
      const found = findNodeByName(n.children, name)
      if (found) return found
    }
  }
  return null
}

function findParentNode(nodes: CatalogNode[], childId: number, parent: CatalogNode | null = null): CatalogNode | null {
  for (const n of nodes) {
    if (n.id === childId) return parent
    if (n.children?.length) {
      const found = findParentNode(n.children, childId, n)
      if (found) return found
    }
  }
  return null
}

function findPathToNode(nodes: CatalogNode[], targetId: number, path: CatalogNode[] = []): CatalogNode[] {
  for (const n of nodes) {
    const next = [...path, n]
    if (n.id === targetId) return next
    if (n.children?.length) {
      const found = findPathToNode(n.children, targetId, next)
      if (found.length) return found
    }
  }
  return []
}

function collectAllIds(nodes: CatalogNode[]): number[] {
  const ids: number[] = []
  for (const n of nodes) {
    ids.push(n.id)
    if (n.children?.length) {
      ids.push(...collectAllIds(n.children))
    }
  }
  return ids
}

function buildFallbackCatalogTree(volumeId: string): CatalogNode[] {
  const units = unitDataMap[volumeId] || []
  return units.map((unit, unitIndex) => {
    const unitId = -((unitIndex + 1) * 1000)
    return {
      id: unitId,
      code: `fallback_${volumeId}_unit_${unitIndex + 1}`,
      name: unit.name,
      namePath: unit.name,
      depth: 0,
      nodeType: 'unit',
      subUnits: [...(unit.subUnits || [])],
      children: (unit.subUnits || []).map((lesson, lessonIndex) => ({
        id: unitId - lessonIndex - 1,
        code: `fallback_${volumeId}_unit_${unitIndex + 1}_lesson_${lessonIndex + 1}`,
        name: lesson,
        namePath: `${unit.name}/${lesson}`,
        depth: 1,
        nodeType: 'lesson',
        children: [],
      })),
    }
  })
}