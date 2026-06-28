<template>
  <div v-if="resourceMode === 'suite'" class="suite-section">
    <div class="suite-card-grid">
      <div
          v-for="suite in paginatedSuites"
          :key="suite.key"
          class="suite-card"
      >
        <div class="card-left">
          <div class="card-icon">{{ suite.icon }}</div>
          <div class="card-info">
            <div class="card-title">{{ suite.title }}</div>
            <div class="card-sub">{{ suite.sub }}</div>
            <div class="card-meta">{{ suite.fileCount }}个文件 | {{ suite.updateTime }}</div>
          </div>
        </div>
        <div class="card-right">
          <button class="download-btn">一键下载</button>
        </div>
        <div class="card-tag" v-if="suite.tag">{{ suite.tag }}</div>
      </div>
    </div>

    <AppPagination
      :currentPage="currentPage"
      :totalPages="totalPages"
      :totalCount="total"
      :visiblePages="visiblePages"
      @update:currentPage="(p: number) => $emit('update:currentPage', p)"
    />
  </div>
</template>

<script setup lang="ts">
import AppPagination from '@/components/shared/AppPagination.vue'

interface Suite {
  key: string
  icon: string
  title: string
  sub: string
  tag?: string
  fileCount: number
  updateTime: string
}

defineProps<{
  resourceMode: 'single' | 'suite'
  suites: Suite[]
  paginatedSuites: Suite[]
  total: number
  currentPage: number
  totalPages: number
  visiblePages: number[]
}>()

defineEmits<{
  'update:currentPage': [value: number]
}>()
</script>

<style scoped>
.suite-section {
  margin-top: 4px;
  padding: 0 20px 20px;
}

.suite-card-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.suite-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transition: 0.2s;
  position: relative;
}
.suite-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
}
.card-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.card-icon {
  font-size: 32px;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.card-sub {
  font-size: 13px;
  color: #606266;
  margin-top: 4px;
}
.card-meta {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.download-btn {
  padding: 8px 20px;
  background: #409EFF;
  color: #fff;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: 0.2s;
}
.download-btn:hover {
  background: #66B1FF;
}
.card-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 2px 8px;
  background: #F56C6C;
  color: #fff;
  font-size: 11px;
  border-radius: 4px;
}

</style>
