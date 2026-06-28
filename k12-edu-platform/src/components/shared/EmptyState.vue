<template>
  <div class="empty-state" :class="{ compact }">
    <p v-if="icon" class="empty-icon" aria-hidden="true">{{ icon }}</p>
    <p class="empty-title">{{ title }}</p>
    <p v-if="description" class="empty-desc">{{ description }}</p>
    <p v-if="hint" class="empty-hint">{{ hint }}</p>
    <slot />
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string
    description?: string
    hint?: string
    icon?: string
    compact?: boolean
  }>(),
  {
    title: '暂无匹配的资源',
    hint: '请切换目录节点、资源类型或放宽筛选条件',
    icon: '📭',
  },
)
</script>

<style scoped>
.empty-state {
  padding: 48px 24px;
  text-align: center;
  color: #909399;
  background: #fafafa;
  border-radius: 8px;
  border: 1px dashed #e4e7ed;
}

.empty-state.compact {
  padding: 28px 16px;
}

.empty-icon {
  font-size: 36px;
  margin: 0 0 12px;
  line-height: 1;
}

.empty-title {
  font-size: 15px;
  color: #606266;
  margin: 0 0 8px;
  font-weight: 500;
}

.empty-desc {
  font-size: 13px;
  margin: 0 0 6px;
  color: #909399;
}

.empty-hint {
  font-size: 12px;
  margin: 0;
  color: #c0c4cc;
}
</style>
