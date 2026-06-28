/**
 * 单元目录 Composable
 * @deprecated M2 起学科页优先使用 useCatalogTree（VITE_USE_CATALOG_BROWSE=true）
 * 管理单元列表、展开/折叠、单元选择
 * 单元数据优先从后端 API 动态获取，API 无数据时回退到静态 unitData.ts
 *
 * 核心特性：
 * - 册别切换时自动重新加载对应单元目录
 * - 版本切换时自动重新加载对应单元目录（不同版本可能有不同单元）
 * - 学科切换时自动重新加载对应单元目录
 * - API 失败或无数据时自动回退到本地静态配置
 */
import { ref, computed, watch } from 'vue'
import { primaryChineseApi } from '../api'
import { unwrapData } from '../api/request'
import { unitDataMap } from '../config/unitData'
import type { Ref } from 'vue'

export interface UnitItem {
  name: string
  subUnits: string[]
  expanded: boolean
}

export function useUnitDirectory(
  currentStage: Ref<string>,
  currentSubject: Ref<any>,
  selectedVolumeId: Ref<string>,
  selectedVersionName: Ref<string>,
  currentGradeLevelName: Ref<string>,
) {
  const currentUnitListRaw = ref<UnitItem[]>([])
  const allUnitsExpanded = ref(false)
  const activeUnit = ref('')
  const loading = ref(false)

  const currentUnitList = computed(() => currentUnitListRaw.value)

  /**
   * 从后端 API 获取单元列表
   * 根据当前年级/版本/册别/学科查询 edu_unit 表
   */
  async function fetchUnitsFromApi() {
    // 从 currentGradeLevelName（如"一年级上册"）中解析年级和册别
    const { gradeName, volumeName } = parseGradeAndVolume(currentGradeLevelName.value)
    const editionName = selectedVersionName.value
    const subjectName = currentSubject.value?.name || ''

    // 缺少必要参数时不请求，尝试使用静态数据
    if (!gradeName || !editionName || !volumeName || !subjectName) {
      loadStaticUnitData()
      return
    }

    loading.value = true
    try {
      const fullGradeName = currentGradeLevelName.value || `${gradeName}${volumeName}`
      const res = await primaryChineseApi.getUnitTree({
        volumeKey: selectedVolumeId.value || undefined,
        gradeName: fullGradeName || gradeName,
        edition: editionName,
        subject: subjectName,
      }, { silentError: true })
      const data = unwrapData(res) || []

      if (data.length > 0) {
        currentUnitListRaw.value = data.map((item) => ({
          name: item.name || '',
          subUnits: item.subUnits || [],
          expanded: allUnitsExpanded.value,
        })).filter((u) => u.name)
      } else {
        loadStaticUnitData()
      }

      // 默认选中第一个单元
      if (currentUnitListRaw.value.length > 0 && !activeUnit.value) {
        activeUnit.value = currentUnitListRaw.value[0].name
      }
      ensureExpandedForActiveUnit()
    } catch (error) {
      console.error('获取单元列表失败:', error)
      // API 请求失败，回退到静态数据
      loadStaticUnitData()
    } finally {
      loading.value = false
    }
  }

  /**
   * 从 gradeLevelName（如"一年级上册"）中解析年级和册别
   */
  function parseGradeAndVolume(gradeLevelName: string): { gradeName: string; volumeName: string } {
    if (!gradeLevelName) return { gradeName: '', volumeName: '' }

    // 匹配 "X年级上册" / "X年级下册" / "必修X" / "选择性必修X" 等格式
    const match = gradeLevelName.match(/(.+?)(上册|下册|必修一|必修二|选择性必修一|选择性必修二)$/)
    if (match) {
      return { gradeName: match[1], volumeName: match[2] }
    }

    // 兜底：尝试从 volumeId 解析
    const volName = getVolumeNameById(selectedVolumeId.value)
    if (volName) {
      // 从 gradeLevelName 中移除册别部分得到年级
      const grade = gradeLevelName.replace(/上册|下册|必修一|必修二|选择性必修一|选择性必修二$/, '')
      return { gradeName: grade, volumeName: volName }
    }

    return { gradeName: gradeLevelName, volumeName: '' }
  }

  /**
   * 根据 volumeId 获取册别名称（上册/下册）
   */
  function getVolumeNameById(volumeId: string): string {
    if (!volumeId) return ''
    // volumeId 格式如: y1s1, y1s2, j7s1, s10s1 等，最后两位表示册别
    if (volumeId.endsWith('s1')) return '上册'
    if (volumeId.endsWith('s2')) return '下册'
    return ''
  }

  /**
   * 从静态配置加载单元数据（作为 API 的回退）
   */
  function loadStaticUnitData() {
    const volId = selectedVolumeId.value
    if (!volId) {
      currentUnitListRaw.value = []
      return
    }

    const staticData = unitDataMap[volId]
    if (staticData && staticData.length > 0) {
      currentUnitListRaw.value = staticData.map(item => ({
        name: item.name,
        subUnits: item.subUnits || [],
        expanded: allUnitsExpanded.value,
      }))
      console.log(`[useUnitDirectory] 使用静态数据: ${volId}, 共 ${staticData.length} 个单元`)
    } else {
      currentUnitListRaw.value = []
      console.log(`[useUnitDirectory] 静态数据中无匹配: ${volId}`)
    }

    // 默认选中第一个单元
    if (currentUnitListRaw.value.length > 0 && !activeUnit.value) {
      activeUnit.value = currentUnitListRaw.value[0].name
    }
    ensureExpandedForActiveUnit()
  }

  function ensureExpandedForActiveUnit() {
    const active = activeUnit.value
    if (!active) return
    for (const unit of currentUnitListRaw.value) {
      if (unit.subUnits?.includes(active)) {
        unit.expanded = true
        return
      }
    }
  }

  /**
   * 初始化/刷新单元列表
   */
  function initUnitList() {
    fetchUnitsFromApi()
  }

  /**
   * 核心：监听册别变化，自动重新加载单元目录
   */
  watch(selectedVolumeId, (newVolId, oldVolId) => {
    if (newVolId && newVolId !== oldVolId) {
      activeUnit.value = ''
      fetchUnitsFromApi()
    }
  })

  /** 监听学科变化（切换语文/数学时也要更新目录） */
  watch(() => currentSubject.value?.key, (newKey, oldKey) => {
    if (newKey && newKey !== oldKey) {
      activeUnit.value = '' // 重置选中单元
      fetchUnitsFromApi()
    }
  })

  /** 监听版本变化（切换版本时也要更新目录） */
  watch(selectedVersionName, (newName, oldName) => {
    if (newName && newName !== oldName) {
      fetchUnitsFromApi()
    }
  })

  /** 监听年级变化（切换年级时也要更新目录） */
  watch(currentGradeLevelName, (newName, oldName) => {
    if (newName && newName !== oldName) {
      fetchUnitsFromApi()
    }
  })

  watch(activeUnit, () => {
    ensureExpandedForActiveUnit()
  })

  function toggleUnitExpand(unit: UnitItem) {
    unit.expanded = !unit.expanded
  }

  function toggleAllUnits() {
    allUnitsExpanded.value = !allUnitsExpanded.value
    currentUnitListRaw.value.forEach(u => u.expanded = allUnitsExpanded.value)
  }

  /**
   * 解析子单元名称为父级单元名称
   * 用于后端查询时传递正确的单元名
   */
  function resolveParentUnitName(unitName: string): string {
    if (!unitName) return ''
    const directMatch = currentUnitListRaw.value.find(u => u.name === unitName)
    if (directMatch) return unitName
    for (const unit of currentUnitListRaw.value) {
      if (unit.subUnits.includes(unitName)) {
        return unit.name
      }
    }
    return unitName
  }

  return {
    currentUnitListRaw,
    currentUnitList,
    activeUnit,
    allUnitsExpanded,
    loading,
    initUnitList,
    toggleUnitExpand,
    toggleAllUnits,
    resolveParentUnitName,
  }
}
