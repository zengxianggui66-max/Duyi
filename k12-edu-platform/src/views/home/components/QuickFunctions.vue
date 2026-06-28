<template>
  <section class="quick-functions">
    <div class="section-header">
      <h3 class="section-title">快捷功能</h3>
    </div>
    <div class="function-grid">
      <div
        v-for="func in functions"
        :key="func.key"
        class="function-card"
        :style="{ '--card-color': func.color }"
        @click="handleClick(func)"
      >
        <div class="card-icon">{{ func.icon }}</div>
        <div class="card-content">
          <div class="card-title">{{ func.title }}</div>
          <div class="card-desc">{{ func.description }}</div>
        </div>
        <div class="card-arrow">›</div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { QuickEntryViewModel } from '@/types/homeOps'
import {
  openExternalNavTarget,
  resolveNavTargetRoute,
  scrollToNavTarget,
} from '@/utils/resolveNavTarget'

defineProps<{
  functions: QuickEntryViewModel[]
}>()

const router = useRouter()

function handleClick(func: QuickEntryViewModel) {
  const target = func.navTarget
  if (!target) return
  if (target.type === 'external') {
    openExternalNavTarget(target)
    return
  }
  if (target.type === 'scroll') {
    scrollToNavTarget(target)
    return
  }
  const route = resolveNavTargetRoute(target)
  if (route) router.push(route)
}
</script>

<style scoped>
.quick-functions {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}

.function-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.function-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #eef0f5;
}

.function-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: var(--card-color, #4facfe);
}

.card-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.card-desc {
  font-size: 12px;
  color: #888;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-arrow {
  font-size: 20px;
  color: #ccc;
  flex-shrink: 0;
}
</style>
