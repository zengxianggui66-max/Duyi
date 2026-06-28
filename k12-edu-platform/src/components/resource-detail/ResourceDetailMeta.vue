<template>
  <div v-if="resource" class="resource-header">
    <div class="header-left">
      <h1 class="resource-title">{{ resource.title }}</h1>
      <!-- 精简元信息行：类型色号 + 核心三要素 -->
      <div class="meta-row">
        <el-tag
          :type="resourceTypeTagType(resource.resourceType) as any"
          effect="dark"
          size="small"
          class="type-tag"
        >
          {{ formatResourceType(resource.resourceType) }}
        </el-tag>
        <span class="meta-divider">|</span>
        <span class="meta-text">{{ gradeLevelMap[resource.gradeLevel] || resource.gradeLevel || '小学' }}</span>
        <span class="meta-text">{{ resource.grade || '—' }}</span>
        <span class="meta-text" v-if="resource.subject">{{ resource.subject }}</span>
        <span class="meta-divider">|</span>
        <span class="meta-text">{{ resource.version || '通用版' }}</span>
        <el-tag
          size="small"
          :type="resource.isFree === 1 || resource.isFree === true ? 'danger' : 'warning'"
          effect="plain"
          class="price-tag"
        >
          {{ resource.isFree === 1 || resource.isFree === true ? '免费' : '会员' }}
        </el-tag>
      </div>
      <!-- 统计数字行 -->
      <div class="stat-row">
        <span class="stat-item">
          <el-icon><Download /></el-icon>
          <strong>{{ formatCount(resource.downloadCount) }}</strong> 下载
        </span>
        <span class="stat-item">
          <el-icon><Star /></el-icon>
          <strong>{{ formatCount(resource.collectCount) }}</strong> 收藏
        </span>
        <span class="stat-item">
          <el-icon><StarFilled /></el-icon>
          <strong>{{ (resource.score || 5.0).toFixed(1) }}</strong> / 5.0
        </span>
      </div>
    </div>
    <slot name="actions" />
  </div>
</template>

<script setup lang="ts">
import { Download, Star, StarFilled } from '@element-plus/icons-vue'

defineProps<{
  resource: Record<string, any> | null
  gradeLevelMap: Record<string, string>
  formatResourceType: (type?: string) => string
  formatMediaType: (type?: string) => string
  formatCount: (n?: number) => string
  resourceTypeTagType: (type?: string) => string
}>()
</script>

<style scoped>
.resource-header {
  background: #fff;
  border-radius: 12px;
  padding: 28px 32px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.header-left { flex: 1; }
.resource-title { font-size: 26px; font-weight: 700; color: #1a1a2e; margin-bottom: 16px; line-height: 1.4; }

.meta-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}
.type-tag {
  font-weight: 600;
  letter-spacing: 0.5px;
}
.price-tag {
  font-weight: 500;
}
.meta-divider { color: #dcdfe6; font-size: 14px; }
.meta-text { color: #909399; font-size: 13px; }

.stat-row {
  display: flex;
  gap: 28px;
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 14px;
}
.stat-item strong {
  color: #303133;
  font-size: 16px;
}
</style>

