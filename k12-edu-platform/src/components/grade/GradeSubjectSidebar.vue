<template>
  <aside class="subject-sidebar-left">
    <div class="subject-nav-list-vertical">
      <div
        v-for="sub in subjects"
        :key="sub.key || sub.id"
        class="subject-nav-item-vertical"
        :class="{ active: activeSubject === (sub.key || String(sub.id)) }"
        @click="$emit('select', sub.key || String(sub.id))"
      >
        <span class="subject-name">{{ sub.name }}</span>
        <span class="subject-arrow">›</span>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
defineProps<{
  subjects: { id: number; name: string; key?: string }[]
  activeSubject: string
}>()

defineEmits<{
  select: [key: string]
}>()
</script>

<style scoped>
.subject-sidebar-left {
  width: 160px;
  flex-shrink: 0;
}
.subject-nav-list-vertical {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #eee;
  overflow: hidden;
}
.subject-nav-item-vertical {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
  transition: background 0.2s;
}
.subject-nav-item-vertical:last-child {
  border-bottom: none;
}
.subject-nav-item-vertical:hover,
.subject-nav-item-vertical.active {
  background: #ecf5ff;
  color: #409eff;
}
.subject-name {
  font-size: 14px;
}
.subject-arrow {
  color: #ccc;
}
</style>
