<template>
  <div class="course-catalog">
    <!-- 课程信息卡片 -->
    <div class="course-info-card">
      <div class="cover-area">
        <div class="book-cover">
          <div class="book-spine"></div>
          <div class="book-front">
            <div class="book-brand">新课堂</div>
            <div class="book-subject">{{ currentSubject?.name }}</div>
          </div>
          <span v-if="isNewTextbook" class="new-badge">新教材</span>
        </div>
      </div>
      <div class="course-meta">
        <h3 class="course-title">{{ currentSubject?.name }} · {{ currentGradeLevelName }}</h3>
        <div class="course-version">{{ currentVersionName }}</div>
        <button class="switch-version-btn" @click="showVersionModal = true">切换版本/册别</button>
      </div>
    </div>

    <!-- 版本/册别选择弹窗（共享组件） -->
    <VersionVolumeSelector
      :visible="showVersionModal"
      :versions="currentSubjectVersions"
      :volumes="volumeList"
      :selected-version-key="selectedVersionKey"
      :selected-volume-id="selectedVolumeId"
      @confirm="handleVersionConfirm"
      @close="showVersionModal = false"
    />

    <!-- M2：catalog 目录树 -->
    <CatalogSidebar
      v-if="useCatalogBrowse"
      :nodes="catalogNodes"
      :active-node-id="catalogActiveNodeId"
      :loading="catalogLoading"
      :display-mode="catalogDisplayMode"
      :all-expanded="catalogAllExpanded"
      :is-expanded="catalogIsExpanded"
      @select="$emit('catalog-select', $event)"
      @toggle-expand="$emit('catalog-toggle-expand', $event)"
      @toggle-all="$emit('catalog-toggle-all')"
      @prefetch="$emit('catalog-prefetch', $event)"
    />

    <!-- 单元目录（带折叠展开，legacy） -->
    <div v-else class="unit-directory">
      <div class="unit-header">
        <span>目录</span>
        <button class="collapse-all-btn" @click="toggleAllUnits">{{ allUnitsExpanded ? '折叠全部' : '展开全部' }}</button>
      </div>
      <div class="unit-list">
        <div
            v-for="(unit, idx) in internalUnitList"
            :key="idx"
            class="unit-item-wrapper"
        >
          <div :class="['unit-item', { active: isUnitRowActive(unit) }]">
            <span
              v-if="unit.subUnits?.length"
              class="expand-icon"
              @click.stop="toggleUnitExpand(unit)"
            >{{ unit.expanded ? '∨' : '⊕' }}</span>
            <span v-else class="expand-icon expand-icon--placeholder" aria-hidden="true"></span>
            <span :class="['unit-name', { active: activeUnit === unit.name }]" @click.stop="handleUnitClick(unit.name)">{{ unit.name }}</span>
          </div>
          <Transition name="sub-unit-list">
            <div v-if="unit.expanded && unit.subUnits?.length" class="sub-unit-list">
              <div v-for="sub in unit.subUnits" :key="sub" :class="['sub-unit', { active: activeUnit === sub }]" @click.stop="handleUnitClick(sub)">{{ sub }}</div>
            </div>
          </Transition>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import CatalogSidebar from '../catalog/CatalogSidebar.vue'
import VersionVolumeSelector from './VersionVolumeSelector.vue'
import type { CatalogNode, DisplayMode } from '@/types/browse'

// ========== Props 定义 ==========
interface Props {
  // 学段和学科相关
  currentStage: string
  currentStageName: string
  currentSubject: any
  currentSubjects: any[]
  
  // 版本相关
  currentSubjectVersions: any[]
  selectedVersionKey: string
  selectedVersionName: string
  isNewTextbook: boolean
  currentPublisher: string
  
  // 册别相关
  volumeList: any[]
  selectedVolumeId: string
  currentGradeLevelName: string
  
  // 单元目录相关
  currentUnitList: any[]
  activeUnit: string
  allUnitsExpanded: boolean

  // M2 catalog 模式
  useCatalogBrowse?: boolean
  catalogNodes?: CatalogNode[]
  catalogActiveNodeId?: number | null
  catalogLoading?: boolean
  catalogDisplayMode?: DisplayMode
  catalogAllExpanded?: boolean
  catalogIsExpanded?: (nodeId: number) => boolean
}

const props = defineProps<Props>()

// ========== Emits 定义 ==========
const emit = defineEmits<{
  'update:selectedVersionKey': [value: string]
  'update:isNewTextbook': [value: boolean]
  'update:selectedVolumeId': [value: string]
  'update:activeUnit': [value: string]
  'update:allUnitsExpanded': [value: boolean]
  'confirm-version': [versionKey: string, isNew: boolean, volumeId: string]
  'catalog-select': [nodeId: number]
  'catalog-toggle-expand': [nodeId: number]
  'catalog-toggle-all': []
  'catalog-prefetch': [nodeId: number]
}>()

const useCatalogBrowse = computed(() => !!props.useCatalogBrowse)
const catalogNodes = computed(() => props.catalogNodes || [])
const catalogActiveNodeId = computed(() => props.catalogActiveNodeId ?? null)
const catalogLoading = computed(() => !!props.catalogLoading)
const catalogDisplayMode = computed(() => props.catalogDisplayMode)
const catalogAllExpanded = computed(() => !!props.catalogAllExpanded)
const catalogIsExpanded = (nodeId: number) =>
  props.catalogIsExpanded ? props.catalogIsExpanded(nodeId) : false

// ========== 内部状态 ==========
const showVersionModal = ref(false)

// 内部单元列表（管理展开状态）
const internalUnitList = ref<any[]>([])

function shouldExpandForActive(unit: { name: string; subUnits?: string[] }) {
  const active = props.activeUnit
  if (!active) return false
  if (unit.name === active) return true
  return !!(unit.subUnits?.length && unit.subUnits.includes(active))
}

function expandUnitForActiveSelection() {
  const active = props.activeUnit
  if (!active) return
  for (const unit of internalUnitList.value) {
    if (unit.subUnits?.includes(active)) {
      unit.expanded = true
      return
    }
    if (unit.name === active && unit.subUnits?.length) {
      unit.expanded = true
    }
  }
}

function isUnitRowActive(unit: { name: string; subUnits?: string[] }) {
  if (props.activeUnit === unit.name) return true
  return !!(unit.subUnits?.length && unit.subUnits.includes(props.activeUnit))
}

// 监听外部 unitList 变化，同步到内部状态
watch(
    () => props.currentUnitList,
    (newList) => {
      if (newList && newList.length > 0) {
        internalUnitList.value = newList.map((u) => {
          const prev = internalUnitList.value.find((p) => p.name === u.name)
          const expandForActive = shouldExpandForActive(u)
          return {
            ...u,
            expanded: props.allUnitsExpanded || (prev?.expanded ?? u.expanded ?? expandForActive),
          }
        })
        expandUnitForActiveSelection()
      } else {
        internalUnitList.value = []
      }
    },
    { immediate: true, deep: true },
)

watch(
  () => props.activeUnit,
  () => {
    expandUnitForActiveSelection()
  },
)

// ========== 计算属性 ==========
const currentVersionName = computed(() => props.selectedVersionName)

// ========== 版本相关方法 ==========
function handleVersionConfirm(versionKey: string, volumeId: string) {
  const ver = props.currentSubjectVersions.find((v: any) => v.key === versionKey)
  if (ver) {
    emit('confirm-version', ver.key, ver.isNew, volumeId)
  }
  showVersionModal.value = false
}

// ========== 单元目录相关方法 ==========
function toggleUnitExpand(unit: any) {
  unit.expanded = !unit.expanded
}

function toggleAllUnits() {
  const newState = !props.allUnitsExpanded
  emit('update:allUnitsExpanded', newState)
  internalUnitList.value.forEach((u: any) => {
    u.expanded = newState
  })
}

function handleUnitClick(unitName: string) {
  emit('update:activeUnit', unitName)
}

/**
 * 解析 activeUnit 为父级单元名
 * 如果 activeUnit 本身就是父级单元名（在 currentUnitList 中能找到），直接返回
 * 如果 activeUnit 是子单元名，则找到其所属父级并返回父级名称
 */
function resolveParentUnitName(unit: string): string | undefined {
  if (!unit) return undefined

  // 遍历当前单元列表，检查是否直接匹配某个父级单元
  for (const parent of internalUnitList.value) {
    if (parent.name === unit) {
      // activeUnit 就是父级单元名，直接使用
      return unit
    }
    // 检查是否是某个父级的子单元
    if (parent.subUnits && parent.subUnits.includes(unit)) {
      return parent.name
    }
  }

  // 未找到匹配（兜底），返回原始值让后端模糊匹配处理
  return unit
}

// 暴露给父组件的方法
defineExpose({
  resolveParentUnitName
})
</script>

<style scoped>
/* 课程目录容器 */
.course-catalog {
  background: #fff;
  border-radius: var(--radius-lg, 16px);
  padding: var(--space-md, 16px);
  border: 1px solid #EEF2F6;
  box-shadow: var(--shadow-card, 0 1px 4px rgba(0,0,0,0.04));
}

/* 课程信息卡片 */
.course-info-card {
  display: flex;
  gap: var(--space-md, 16px);
  margin-bottom: var(--space-md, 16px);
}

.cover-area {
  flex-shrink: 0;
}

/* ===== CSS 书脊效果 ===== */
.book-cover {
  width: 70px;
  height: 90px;
  display: flex;
  position: relative;
  flex-shrink: 0;
  border-radius: var(--radius-sm, 6px);
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.book-spine {
  width: 14px;
  height: 100%;
  background: linear-gradient(180deg, #d4a830 0%, #b8860b 50%, #a07508 100%);
  flex-shrink: 0;
  position: relative;
}

.book-spine::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 2px;
  height: 100%;
  background: rgba(0, 0, 0, 0.08);
}

.book-front {
  flex: 1;
  height: 100%;
  background: linear-gradient(180deg, #F5C542 0%, #E8B830 40%, #D4A520 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4px;
  position: relative;
}

.book-front::before {
  content: '';
  position: absolute;
  inset: 2px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 2px;
  pointer-events: none;
}

.book-brand {
  font-size: 8px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
  text-align: center;
  line-height: 1.2;
  margin-bottom: 2px;
  text-shadow: 0 1px 1px rgba(0, 0, 0, 0.15);
}

.book-subject {
  font-size: 9px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.85);
  text-align: center;
  line-height: 1.2;
  max-width: 50px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  text-shadow: 0 1px 1px rgba(0, 0, 0, 0.15);
}

.new-badge {
  position: absolute;
  top: -6px;
  right: -10px;
  background: var(--color-hot, #EF4444);
  color: #fff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: var(--radius-round, 999px);
  z-index: 1;
}

.course-meta {
  flex: 1;
}

.course-title {
  /* H4: 15px/600 */
  font-size: var(--fs-h4, 15px);
  font-weight: var(--fw-h4, 600);
  margin-bottom: 6px;
  line-height: var(--lh-h4, 1.5);
  color: var(--text-primary, #1A1A2E);
}

.version-publisher {
  font-size: var(--fs-caption, 12px);
  color: var(--text-secondary, #8E8EA0);
  margin-bottom: 6px;
}

.switch-version-btn {
  font-size: var(--fs-small, 11px);
  background: #F5F7FA;
  border: 1px solid var(--border-color, #E8ECF4);
  border-radius: var(--radius-round, 999px);
  padding: 4px 12px;
  cursor: pointer;
  transition: all var(--transition-fast, 0.15s ease);
  color: var(--text-regular, #4A4A68);
}

.switch-version-btn:hover {
  background: var(--color-primary-bg, #EBF0FF);
  border-color: var(--color-primary, #4361EE);
  color: var(--color-primary, #4361EE);
}

/* 单元目录 */
.unit-directory {
  margin-top: var(--space-sm, 8px);
}

.unit-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: var(--space-sm, 8px);
  font-size: var(--fs-body, 14px);
  font-weight: 600;
  color: var(--text-primary, #1A1A2E);
}

.collapse-all-btn {
  font-size: var(--fs-caption, 12px);
  background: transparent;
  border: 1px solid var(--border-color, #E8ECF4);
  border-radius: var(--radius-round, 999px);
  padding: 2px 10px;
  cursor: pointer;
  color: var(--text-secondary, #8E8EA0);
  transition: all var(--transition-fast, 0.15s ease);
}

.collapse-all-btn:hover {
  background: #F5F7FA;
  border-color: var(--color-primary, #4361EE);
  color: var(--color-primary, #4361EE);
}

.unit-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.unit-item-wrapper {
  margin-bottom: 4px;
}

.unit-item {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 6px 8px;
  border-radius: var(--radius-md, 10px);
  transition: all var(--transition-fast, 0.15s ease);
}

.unit-item:hover {
  background: #F5F7FA;
}

/* 展开/收起过渡动画 */
.sub-unit-list-enter-active,
.sub-unit-list-leave-active {
  overflow: hidden;
  transition: max-height var(--transition-collapse, 0.25s ease),
              opacity var(--transition-collapse, 0.25s ease);
}
.sub-unit-list-enter-from,
.sub-unit-list-leave-to {
  max-height: 0;
  opacity: 0;
}

.expand-icon {
  flex-shrink: 0;
  font-size: 14px;
  width: 20px;
  color: var(--text-placeholder, #B0B0C0);
  cursor: pointer;
  user-select: none;
  transition: transform var(--transition-fast, 0.15s ease);
}

.expand-icon--placeholder {
  cursor: default;
  visibility: hidden;
}

.unit-name {
  font-size: var(--fs-body, 14px);
  color: var(--text-primary, #1A1A2E);
}

.unit-name.active {
  color: var(--color-primary, #4361EE);
  font-weight: 600;
}

.unit-item.active {
  background: var(--color-primary-bg, #EBF0FF);
}

.sub-unit-list {
  padding-left: 24px;
  margin-top: 4px;
  margin-left: 18px;
  border-left: 2px solid var(--border-color, #E8ECF4);
  overflow: hidden;
}

.sub-unit {
  padding: 5px 10px;
  font-size: var(--fs-body, 14px);
  cursor: pointer;
  border-radius: var(--radius-sm, 6px);
  color: var(--text-regular, #4A4A68);
  transition: all var(--transition-fast, 0.15s ease);
  position: relative;
}

.sub-unit::before {
  content: '';
  position: absolute;
  left: -10px;
  top: 0;
  width: 8px;
  height: 50%;
  border-bottom: 2px solid var(--border-color, #E8ECF4);
}

.sub-unit:hover {
  background: #F0F5FF;
  color: var(--color-primary, #4361EE);
}

.sub-unit.active {
  background: var(--color-primary-bg, #EBF0FF);
  color: var(--color-primary, #4361EE);
  font-weight: 500;
}
</style>
