/**
 * 期末复习浏览状态：复习课件双 Tab、检测卷筛选、面包屑路径
 */
import { ref, computed, watch, type Ref, type ComputedRef } from 'vue'
import type { RouteLocationNormalizedLoaded, Router } from 'vue-router'
import type { CatalogNode } from '@/types/browse'
import {
  FINAL_REVIEW_MODULE,
  REVIEW_KIND,
  REVIEW_SCOPE,
  readReviewMeta,
  findNodeById,
  findReviewKindAncestor,
  buildFinalReviewPathLabels,
  findFirstCoursewareLeaf,
  findSyncPrepUnitNode,
  type ReviewScope,
} from '@/config/finalReviewConfig'
import { resolveExamBrowseParams } from '@/constants/examColumnFilters'
import { SYNC_PREP_COLUMN } from '@/constants/syncPrepColumnFilters'

export function useFinalReview(opts: {
  route: RouteLocationNormalizedLoaded
  router: Router
  activeColumn: Ref<string>
  catalogTree: Ref<CatalogNode[]>
  activeCatalogNodeId: Ref<number | null>
  selectedVolumeId: Ref<string>
  selectCatalogNode: (id: number) => void
  examSelectedType: Ref<string>
  examSelectedRegion: Ref<string>
  examSelectedVersion: Ref<string>
  searchKeyword: Ref<string>
  reviewScope: Ref<ReviewScope>
}) {
  const {
    route,
    router,
    activeColumn,
    catalogTree,
    activeCatalogNodeId,
    selectedVolumeId,
    selectCatalogNode,
    examSelectedType,
    examSelectedRegion,
    examSelectedVersion,
    searchKeyword,
    reviewScope,
  } = opts

  const isFinalReviewColumn = computed(() => activeColumn.value === FINAL_REVIEW_MODULE)

  const activeCatalogNode = computed(() =>
    findNodeById(catalogTree.value, activeCatalogNodeId.value),
  )

  const activeReviewMeta = computed(() => readReviewMeta(activeCatalogNode.value))

  const isCoursewareContext = computed(() => {
    if (!isFinalReviewColumn.value) return false
    const kindNode = findReviewKindAncestor(catalogTree.value, activeCatalogNodeId.value)
    return readReviewMeta(kindNode).reviewKind === REVIEW_KIND.courseware
  })

  const isExamPaperContext = computed(() => {
    if (!isFinalReviewColumn.value) return false
    const kindNode = findReviewKindAncestor(catalogTree.value, activeCatalogNodeId.value)
    const meta = readReviewMeta(kindNode)
    return meta.reviewKind === REVIEW_KIND.exam || activeCatalogNode.value?.name === '期末检测卷'
  })

  const showCoursewareTabs = computed(() => isCoursewareContext.value)

  const showExamFilters = computed(() => isExamPaperContext.value)

  const reviewPathLabels = computed(() =>
    buildFinalReviewPathLabels(catalogTree.value, activeCatalogNodeId.value),
  )

  const syncPrepLinkForUnit = computed(() => {
    const meta = activeReviewMeta.value
    if (meta.reviewScope !== REVIEW_SCOPE.unit || !meta.canonicalUnit) return null
    const unitNode = findSyncPrepUnitNode(catalogTree.value, meta.canonicalUnit)
    if (!unitNode) return null
    return {
      module: SYNC_PREP_COLUMN,
      node: String(unitNode.id),
      unit: meta.canonicalUnit,
      label: `查看「${meta.canonicalUnit}」同步备课`,
    }
  })

  const finalReviewExamParams = computed(() => {
    if (!showExamFilters.value) return null
    return resolveExamBrowseParams({
      column: '期末',
      selectedType: examSelectedType.value,
      selectedRegion: examSelectedRegion.value,
      selectedVersion: examSelectedVersion.value,
      searchKeyword: searchKeyword.value,
      layoutUsesExamFilters: true,
    })
  })

  function initReviewScopeFromRoute() {
    const raw = route.query.reviewScope
    if (raw === REVIEW_SCOPE.unit || raw === REVIEW_SCOPE.special) {
      reviewScope.value = raw
      return
    }
    const meta = activeReviewMeta.value
    if (meta.reviewScope === REVIEW_SCOPE.unit || meta.reviewScope === REVIEW_SCOPE.special) {
      reviewScope.value = meta.reviewScope as ReviewScope
    }
  }

  function switchCoursewareScope(scope: ReviewScope) {
    reviewScope.value = scope
    const leaf = findFirstCoursewareLeaf(catalogTree.value, selectedVolumeId.value, scope)
    if (leaf) {
      selectCatalogNode(leaf.id)
    }
    const query = { ...route.query, reviewScope: scope }
    router.replace({ query })
  }

  watch(activeCatalogNodeId, () => {
    if (!isFinalReviewColumn.value) return
    const meta = activeReviewMeta.value
    if (meta.reviewScope === REVIEW_SCOPE.unit || meta.reviewScope === REVIEW_SCOPE.special) {
      reviewScope.value = meta.reviewScope as ReviewScope
    }
  })

  watch(
    () => route.query.reviewScope,
    () => initReviewScopeFromRoute(),
  )

  return {
    reviewScope,
    isFinalReviewColumn,
    isCoursewareContext,
    isExamPaperContext,
    showCoursewareTabs,
    showExamFilters,
    reviewPathLabels,
    syncPrepLinkForUnit,
    finalReviewExamParams,
    initReviewScopeFromRoute,
    switchCoursewareScope,
  }
}
