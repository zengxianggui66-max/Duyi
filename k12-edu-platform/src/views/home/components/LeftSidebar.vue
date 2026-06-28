<template>
  <aside ref="sidebarRef" class="left-sidebar">
    <div class="subject-list">
      <div
        v-for="subject in subjects"
        :key="subject.key"
        class="subject-item"
      >
        <button
          :class="['subject-btn', { active: current?.key === subject.key }]"
          @click="$emit('select', subject)"
        >
          <span class="subject-name">{{ subject.name }}</span>
          <span v-if="subject.isNew" class="new-dot" title="新课标"></span>
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface Subject {
  key: string
  name: string
  isNew?: boolean
}

defineProps<{
  subjects: Subject[]
  current: Subject | null
}>()

defineEmits<{
  select: [subject: Subject]
}>()

const sidebarRef = ref<HTMLElement | null>(null)
defineExpose({ sidebarRef })
</script>

<style scoped>
.left-sidebar {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  border: 1px solid #eef2f6;
}

.subject-list {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 每项固定占容器高度的 1/8，最多视觉上排满 8 行 */
.subject-item {
  flex: 0 0 12.5%;
  height: 12.5%;
  min-height: 48px;
  max-height: 12.5%;
  display: flex;
  border-bottom: 1px solid #f0f2f6;
  box-sizing: border-box;
}

.subject-item:last-child {
  border-bottom: none;
}

.subject-btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 100%;
  padding: 0 14px 0 18px;
  background: none;
  border: none;
  text-align: left;
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  position: relative;
}

.subject-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.subject-btn:hover {
  background: #f8fafd;
  color: #409eff;
}

.subject-btn.active {
  background: linear-gradient(90deg, #ecf5ff 0%, #f8fbff 100%);
  color: #409eff;
  font-weight: 600;
}

.subject-btn.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  border-radius: 0 2px 2px 0;
  background: #409eff;
}

.subject-btn.active::after {
  content: '▶';
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 11px;
  color: #409eff;
  opacity: 0.85;
}

.new-dot {
  flex-shrink: 0;
  width: 7px;
  height: 7px;
  margin-left: 6px;
  background: #f56c6c;
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba(245, 108, 108, 0.2);
}

.subject-list::-webkit-scrollbar {
  width: 4px;
}

.subject-list::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 2px;
}
</style>
