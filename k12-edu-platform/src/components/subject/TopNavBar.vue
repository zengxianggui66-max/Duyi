<template>
  <div class="top-nav">
    <div class="top-nav-inner">
      <div class="stage-bar">
        <button
          v-for="stage in stages"
          :key="stage.key"
          :class="['stage-btn', { active: currentStage === stage.key }]"
          @click="$emit('switch-stage', stage.key)"
        >
          {{ stage.name }}
        </button>
      </div>

      <div v-if="menuItems?.length" class="func-bar">
        <button
          v-for="item in menuItems"
          :key="item.key"
          type="button"
          :class="['func-btn', { active: currentFuncKey === item.key }]"
          @click="$emit('update:currentFuncKey', item.key)"
        >
          {{ item.name }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { type Stage } from '../../config/subjectConfig'

export interface TopMenuItem {
  key: string
  name: string
}

defineProps<{
  stages: Stage[]
  currentStage: string
  menuItems?: TopMenuItem[]
  currentFuncKey?: string
}>()

defineEmits<{
  'switch-stage': [key: string]
  'update:currentFuncKey': [key: string]
}>()
</script>

<style scoped>
.top-nav {
  background: #f5f7fa;
  border-bottom: 1px solid #eef2f6;
  position: sticky;
  top: var(--header-height, 64px);
  z-index: 100;
}

.top-nav-inner {
  max-width: 1440px;
  margin: 0 auto;
  padding: 10px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.stage-bar {
  display: flex;
  gap: 6px;
  background: #fff;
  padding: 4px;
  border-radius: 40px;
  border: 1px solid #eef2f6;
  box-shadow: var(--shadow-sm);
}

.stage-btn {
  padding: 7px 18px;
  border: none;
  background: transparent;
  border-radius: 32px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}

.stage-btn:hover {
  color: #409eff;
}

.stage-btn.active {
  background: #409eff;
  color: #fff;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.25);
}

.func-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: flex-end;
}

.func-btn {
  padding: 6px 14px;
  border: 1px solid transparent;
  background: transparent;
  border-radius: 20px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}

.func-btn:hover {
  color: #409eff;
  background: #ecf5ff;
}

.func-btn.active {
  color: #409eff;
  background: #ecf5ff;
  border-color: #d9ecff;
  font-weight: 600;
}

@media (max-width: 768px) {
  .top-nav-inner {
    flex-direction: column;
    align-items: stretch;
  }
  .func-bar {
    justify-content: flex-start;
    overflow-x: auto;
    flex-wrap: nowrap;
  }
}
</style>
