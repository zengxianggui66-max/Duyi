<template>
  <div v-if="totalPages > 1 || totalCount > 0" class="app-pagination">
    <!-- 左侧：总条数 -->
    <div class="app-pagination__info">
      共 {{ totalCount }} 条
    </div>

    <!-- 中间：页码按钮 -->
    <div class="app-pagination__controls">
      <button
        class="page-btn"
        :disabled="currentPage === 1"
        @click="onPage(currentPage - 1)"
      >上一页</button>
      <template v-for="pg in visiblePages" :key="String(pg)">
        <button
          v-if="typeof pg === 'number'"
          :class="['page-btn', { active: currentPage === pg }]"
          @click="onPage(pg)"
        >{{ pg }}</button>
        <span v-else class="page-ellipsis">{{ pg }}</span>
      </template>
      <button
        class="page-btn"
        :disabled="currentPage === totalPages"
        @click="onPage(currentPage + 1)"
      >下一页</button>
    </div>

    <!-- 右侧：每页条数选择器（可选） -->
    <div v-if="pageSize != null" class="app-pagination__size">
      <span class="page-size-label">每页</span>
      <select
        class="page-size-dropdown"
        :value="pageSize"
        @change="onPageSizeChange"
      >
        <option
          v-for="size in pageSizeOptions"
          :key="size"
          :value="size"
        >{{ size }}</option>
      </select>
      <span class="page-size-label">条</span>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  currentPage: number
  totalPages: number
  totalCount?: number
  pageSize?: number
  pageSizeOptions?: number[]
  visiblePages: (number | string)[]
}>(), {
  totalCount: 0,
  pageSize: 10,
  pageSizeOptions: () => [10, 20, 30, 40, 50],
})

const emit = defineEmits<{
  'update:currentPage': [value: number]
  'update:pageSize': [value: number]
}>()

function onPage(p: number) {
  emit('update:currentPage', p)
}

function onPageSizeChange(e: Event) {
  const val = Number((e.target as HTMLSelectElement).value)
  emit('update:pageSize', val)
}
</script>

<style scoped>
.app-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  padding: 12px 0;
  border-top: 1px solid #ebeef5;
}

.app-pagination__info {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
}

.app-pagination__controls {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  box-shadow: inset 0 1px 2px rgba(255, 255, 255, 0.8),
              0 1px 2px rgba(0, 0, 0, 0.04);
}

.app-pagination__controls .page-btn {
  min-width: 36px;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #e2e8f0;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  transition: all 0.15s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.app-pagination__controls .page-btn:hover:not(:disabled) {
  color: #3b82f6;
  border-color: #93c5fd;
  background: linear-gradient(180deg, #ffffff 0%, #eff6ff 100%);
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.15);
  transform: translateY(-1px);
}

.app-pagination__controls .page-btn.active {
  background: linear-gradient(180deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  border-color: #2563eb;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(37, 99, 235, 0.35),
              inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.app-pagination__controls .page-btn:disabled {
  background: #f1f5f9;
  color: #cbd5e1;
  border-color: #e2e8f0;
  cursor: not-allowed;
  box-shadow: none;
}

.app-pagination__size {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #64748b;
  white-space: nowrap;
  padding: 6px 10px;
  background: #f8fafc;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.page-size-label {
  padding: 0 2px;
  font-weight: 500;
}

.page-size-dropdown {
  height: 32px;
  padding: 0 28px 0 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  font-size: 13px;
  font-weight: 500;
  color: #475569;
  cursor: pointer;
  outline: none;
  transition: all 0.15s ease;
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%2394a3b8' d='M6 8L1 3h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.page-size-dropdown:hover,
.page-size-dropdown:focus {
  border-color: #93c5fd;
  background: linear-gradient(180deg, #ffffff 0%, #eff6ff 100%);
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.1);
}

.app-pagination__controls .page-ellipsis {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 36px;
  padding: 0 4px;
  font-size: 14px;
  color: #94a3b8;
  user-select: none;
  cursor: default;
  font-weight: 500;
}
</style>
