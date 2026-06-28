<template>
  <Teleport to="body">
    <div v-if="visible" class="version-modal-mask" @click.self="$emit('close')">
      <div class="version-modal">
        <div class="modal-header">
          <span class="modal-title">选择版本册别</span>
        </div>

        <!-- 版本选择 -->
        <div class="modal-section">
          <span class="section-label">版本：</span>
          <div class="version-buttons">
            <button
              v-for="ver in versions"
              :key="ver.key"
              :class="['version-btn', { active: internalVersionKey === ver.key }]"
              @click="internalVersionKey = ver.key"
            >
              {{ ver.name }}
              <span v-if="ver.isNew" class="new-tag">新</span>
            </button>
          </div>
        </div>

        <!-- 册别选择（按年级分组） -->
        <div class="modal-section">
          <span class="section-label">册别：</span>
          <div class="volume-grid">
            <div v-for="group in gradeVolumeGroups" :key="group.gradeName" class="volume-row">
              <span class="volume-grade-label">{{ group.gradeName }}</span>
              <div class="volume-buttons">
                <button
                  v-for="vol in group.volumes"
                  :key="vol.id"
                  :class="['volume-btn', { active: internalVolumeId === vol.id }]"
                  @click="internalVolumeId = vol.id"
                >
                  {{ vol.name }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="modal-footer">
          <button class="confirm-btn" @click="handleConfirm">确定</button>
          <button class="cancel-btn" @click="$emit('close')">取消</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

interface Props {
  visible: boolean
  versions: any[]
  volumes: any[]
  selectedVersionKey: string
  selectedVolumeId: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  confirm: [versionKey: string, volumeId: string]
  close: []
}>()

const internalVersionKey = ref('')
const internalVolumeId = ref('')

// 打开弹窗时同步外部选中状态
watch(() => props.visible, (val) => {
  if (val) {
    internalVersionKey.value = props.selectedVersionKey || props.versions[0]?.key || ''
    internalVolumeId.value = props.selectedVolumeId || firstVolumeId.value
  }
})

const firstVolumeId = computed(() => props.volumes[0]?.id || '')

function semesterSortKey(name: string): number {
  if (name.includes('上册') || name.includes('必修一') || name.includes('选择性必修一')) return 0
  if (name.includes('下册') || name.includes('必修二') || name.includes('选择性必修二')) return 1
  return 2
}

// 按年级分组册别（同一年级的上册和下册排在一行）
const gradeVolumeGroups = computed(() => {
  const groups: Record<string, { gradeName: string; volumes: any[] }> = {}
  for (const vol of props.volumes) {
    const match = vol.name.match(/(一|二|三|四|五|六|七|八|九|十)年级/)
    const grade = match ? match[0] : '其他'
    if (!groups[grade]) groups[grade] = { gradeName: grade, volumes: [] }
    groups[grade].volumes.push(vol)
  }
  for (const g of Object.values(groups)) {
    g.volumes.sort((a: any, b: any) => semesterSortKey(a.name) - semesterSortKey(b.name))
  }
  const order = ['一年级', '二年级', '三年级', '四年级', '五年级', '六年级', '七年级', '八年级', '九年级', '十年级']
  return Object.values(groups).sort((a, b) => order.indexOf(a.gradeName) - order.indexOf(b.gradeName))
})

function handleConfirm() {
  if (internalVersionKey.value && internalVolumeId.value) {
    emit('confirm', internalVersionKey.value, internalVolumeId.value)
  }
}
</script>

<style scoped>
.version-modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.version-modal {
  background: #fff;
  border-radius: 16px;
  padding: 28px 32px;
  width: 680px;
  max-width: 92vw;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 12px 40px rgba(0,0,0,0.18);
}

.modal-header {
  margin-bottom: 20px;
}

.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: #1A1A2E;
}

.modal-section {
  margin-bottom: 24px;
}

.section-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.version-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.version-btn {
  padding: 8px 18px;
  border: 1px solid #DCDFE6;
  background: #fff;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  color: #303133;
  transition: all 0.15s;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.version-btn:hover {
  border-color: #409EFF;
  color: #409EFF;
}

.version-btn.active {
  background: #409EFF;
  color: #fff;
  border-color: #409EFF;
}

.new-tag {
  display: inline-block;
  padding: 0 4px;
  font-size: 10px;
  line-height: 14px;
  color: #fff;
  background: #F56C6C;
  border-radius: 4px;
  vertical-align: super;
}

.volume-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.volume-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.volume-grade-label {
  flex: 0 0 60px;
  font-size: 13px;
  color: #909399;
  line-height: 34px;
  white-space: nowrap;
}

.volume-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.volume-btn {
  padding: 6px 16px;
  border: 1px solid #DCDFE6;
  background: #fff;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  color: #303133;
  transition: all 0.15s;
}

.volume-btn:hover {
  border-color: #409EFF;
  color: #409EFF;
}

.volume-btn.active {
  background: #409EFF;
  color: #fff;
  border-color: #409EFF;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #F0F0F0;
}

.confirm-btn {
  padding: 8px 32px;
  background: #409EFF;
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.15s;
}

.confirm-btn:hover {
  background: #66b1ff;
}

.confirm-btn:active {
  background: #3a8ee6;
}

.cancel-btn {
  padding: 8px 32px;
  background: #fff;
  color: #606266;
  border: 1px solid #DCDFE6;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.15s;
}

.cancel-btn:hover {
  color: #409EFF;
  border-color: #409EFF;
}
</style>
