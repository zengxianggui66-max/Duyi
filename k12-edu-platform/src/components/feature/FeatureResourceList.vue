<template>
  <section class="resource-section">
    <div class="content-header">
      <div class="result-info">
        <span class="result-count">共 <strong>{{ total }}</strong> 个资源</span>
        <span class="result-stat">精品 <strong>{{ stats.elite }}</strong> | 免费 <strong>{{ stats.free }}</strong></span>
      </div>
      <div class="sort-box">
        <span class="sort-label">排序</span>
        <el-select :model-value="sortBy" style="width: 130px;" @update:model-value="$emit('update:sortBy', $event); $emit('sort-change')">
          <el-option label="最新上传" value="newest" />
          <el-option label="最多下载" value="downloads" />
          <el-option label="最高评分" value="rating" />
          <el-option label="热门推荐" value="hot" />
        </el-select>
      </div>
    </div>

    <div v-loading="loading" class="resource-list">
      <div
        v-for="item in resources"
        :key="item.id"
        class="resource-card-v2"
        @click="$emit('open', item)"
      >
        <div class="card-cover" :style="{ background: getCoverColor(item.id) }">
          <span class="cover-icon">{{ getResourceIcon(item.resourceType) }}</span>
          <span class="type-badge">{{ getTypeName(item.resourceType) }}</span>
          <span v-if="item.level === 'elite'" class="level-badge elite">精品</span>
          <span v-else-if="item.level === 'vip'" class="level-badge vip">特供</span>
          <span v-else-if="item.isFree" class="level-badge free">免费</span>
        </div>
        <div class="card-info">
          <h4 class="card-title">{{ item.title }}</h4>
          <p class="card-desc">{{ item.description || defaultDesc }}</p>
          <div class="card-meta-row">
            <span class="meta-item">学段：{{ item.gradeLevel || '通用' }}</span>
            <span class="meta-item">类型：{{ getTypeName(item.resourceType) }}</span>
          </div>
          <div class="card-detail-row">
            <span class="detail-item">📄 {{ item.pageCount || 0 }}页</span>
            <span class="detail-item">📁 {{ item.format || 'PPT' }}</span>
            <span class="detail-item">👤 {{ item.authorName || '佚名' }}</span>
          </div>
          <div class="card-stats-row">
            <span class="stat-item">⬇ {{ formatCount(item.downloadCount || 0) }}</span>
            <span class="stat-item">⭐ {{ item.rating || '5.0' }}</span>
            <span class="stat-item">👁️ {{ formatCount(item.viewCount || 0) }}</span>
          </div>
        </div>
      </div>

      <div v-if="!loading && !resources.length" class="empty-hint">
        <el-empty description="暂无相关资源" :image-size="120">
          <el-button type="primary" @click="$router.push('/feature')">返回特色频道</el-button>
        </el-empty>
      </div>
    </div>

    <div v-if="total > 0" class="pagination-wrap">
      <el-pagination
        :current-page="currentPage"
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="12"
        @current-change="$emit('page-change', $event)"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  loading: boolean
  total: number
  resources: any[]
  stats: { elite: number | string; free: number | string }
  sortBy: string
  currentPage: number
  defaultDesc?: string
  getCoverColor: (id: number) => string
  getResourceIcon: (type?: string) => string
  getTypeName: (type?: string) => string
  formatCount: (count: number) => string
}>()

defineEmits<{
  'update:sortBy': [value: string]
  'sort-change': []
  'page-change': [page: number]
  open: [item: any]
}>()
</script>

<style scoped>
.resource-section {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 16px;
}
.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.result-info { display: flex; align-items: baseline; gap: 12px; }
.result-count { font-size: 13px; color: var(--text-secondary); }
.result-count strong { color: var(--text-primary); }
.result-stat { font-size: 12px; color: var(--text-secondary); }
.result-stat strong { color: #ff6b6b; }
.sort-box { display: flex; align-items: center; gap: 8px; }
.sort-label { font-size: 13px; color: var(--text-secondary); }
.resource-list { display: flex; flex-direction: column; gap: 10px; }
.resource-card-v2 {
  display: flex;
  gap: 14px;
  padding: 12px;
  background: var(--bg-page);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.25s;
}
.resource-card-v2:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  background: #fff;
}
.card-cover {
  width: 110px;
  height: 78px;
  flex-shrink: 0;
  border-radius: var(--radius-sm);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}
.cover-icon { font-size: 26px; color: rgba(255, 255, 255, 0.9); }
.type-badge {
  position: absolute;
  bottom: 4px;
  left: 4px;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  padding: 2px 6px;
  border-radius: var(--radius-round);
  font-size: 9px;
}
.level-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  padding: 2px 6px;
  border-radius: var(--radius-round);
  font-size: 9px;
  font-weight: 600;
  color: #fff;
}
.level-badge.elite { background: linear-gradient(135deg, #f59e0b, #ef4444); }
.level-badge.vip { background: linear-gradient(135deg, #8b5cf6, #6d28d9); }
.level-badge.free { background: #10b981; }
.card-info { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.card-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 4px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-desc {
  font-size: 11px;
  color: var(--text-secondary);
  margin: 0 0 6px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-meta-row { display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 3px; }
.meta-item {
  font-size: 10px;
  color: var(--text-secondary);
  padding: 2px 6px;
  background: #fff;
  border-radius: var(--radius-sm);
}
.card-detail-row { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 3px; }
.detail-item { font-size: 10px; color: var(--text-secondary); }
.card-stats-row { display: flex; gap: 10px; margin-top: auto; }
.stat-item { font-size: 11px; color: var(--text-secondary); }
.empty-hint { padding: 40px 0; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 16px; }
</style>
