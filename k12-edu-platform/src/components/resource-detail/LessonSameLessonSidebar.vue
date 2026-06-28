<template>
  <el-card class="same-lesson-card">
    <template #header>
      <div class="card-header">
        <span class="header-title">📚 本课配套</span>
        <span class="header-count">{{ items.length }} 份</span>
      </div>
    </template>

    <div v-if="loading" class="loading-hint">加载中…</div>
    <div v-else-if="!items.length" class="empty-hint">暂无同课其他资源</div>
    <div v-else class="same-lesson-list">
      <div
        v-for="item in items"
        :key="item.id"
        :class="['same-item', { current: item.id === currentId }]"
        @click="item.id !== currentId && $emit('open', item)"
      >
        <span class="type-badge">{{ item.type || '资源' }}</span>
        <span class="item-title">{{ item.title }}</span>
        <span v-if="item.id === currentId" class="current-tag">当前</span>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import type { PrimaryChineseItem } from '@/api/types'

defineProps<{
  items: PrimaryChineseItem[]
  currentId?: number
  loading?: boolean
}>()

defineEmits<{
  open: [item: PrimaryChineseItem]
}>()
</script>

<style scoped>
.same-lesson-card {
  border-radius: 12px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-title {
  font-size: 15px;
  font-weight: 600;
}
.header-count {
  font-size: 12px;
  color: #909399;
}
.loading-hint,
.empty-hint {
  padding: 16px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}
.same-lesson-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.same-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid #eef2f6;
  cursor: pointer;
  transition: background 0.2s;
}
.same-item:not(.current):hover {
  background: #f5f9ff;
  border-color: #d9ecff;
}
.same-item.current {
  background: #ecf5ff;
  border-color: #b3d8ff;
  cursor: default;
}
.type-badge {
  flex-shrink: 0;
  padding: 2px 6px;
  font-size: 11px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
}
.item-title {
  flex: 1;
  font-size: 13px;
  line-height: 1.5;
  color: #303133;
}
.current-tag {
  flex-shrink: 0;
  font-size: 11px;
  color: #409eff;
}
</style>
