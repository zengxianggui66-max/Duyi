<template>
  <!-- Topic 模式资源列表区 -->
  <div class="topic-main-area">
    <!-- 左侧资源区 -->
    <div class="topic-card-grid">
      <!-- 工具行 -->
      <div class="topic-toolbar">
        <button :class="['mode-btn', { active: resourceMode === 'single' }]" @click="$emit('update:resourceMode', 'single')">找单份</button>
        <button :class="['mode-btn', { active: resourceMode === 'suite' }]" @click="$emit('update:resourceMode', 'suite')">找成套</button>
        <span :class="['sort-btn', { active: sortType === 'comprehensive' }]" @click="$emit('update:sortType', 'comprehensive')">综合</span>
        <span :class="['sort-btn', { active: sortType === 'latest' }]" @click="$emit('update:sortType', 'latest')">最新</span>
        <span :class="['sort-btn', { active: sortType === 'downloads' }]" @click="$emit('update:sortType', 'downloads')">下载量</span>
        <div class="doc-type-selector">
          <span>文档类型</span>
          <svg viewBox="0 0 1024 1024" width="10" height="10" fill="#999"><path d="M512 714.667c-14.72 0-29.44-5.621-40.661-16.862L181.973 408.46a57.43 57.43 0 010-81.28 57.392 57.392 0 0181.323 0l248.704 248.683L760.704 327.18a57.392 57.392 0 0181.323 0 57.43 57.43 0 010 81.28L552.66 697.805A57.355 57.355 0 01512 714.667z"/></svg>
        </div>
        <span class="result-count">共 {{ displayTotal }} 条结果</span>
      </div>

      <!-- 骨架加载 -->
      <div v-if="loading" class="topic-skeleton-list">
        <div v-for="i in 5" :key="i" class="topic-skeleton-row" />
      </div>

      <!-- API 资源列表 -->
      <div v-else-if="apiResources.length" class="topic-resource-list">
        <div v-for="item in apiResources" :key="item.id" class="topic-resource-item" @click="$emit('open-resource', item)">
          <div :class="'doc-icon-box ' + getDocIconClass(item)">{{ getDocIconLetter(item) }}</div>
          <div class="topic-resource-info">
            <div class="topic-resource-title">{{ item.title }}</div>
            <div class="topic-resource-meta">
              <span>{{ (item.uploadTime || '').substring(0, 10) }}</span><span>|</span>
              <span>{{ item.downloadCount || 0 }}次下载</span><span>|</span>
              <span>{{ item.uploader || '教学网资源' }}</span>
            </div>
            <div v-if="item.albumName" class="topic-resource-album">
              专辑：<span class="album-link">{{ item.albumName }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <EmptyState
        v-if="!loading && !apiResources.length"
        :description="browseSummary ? `当前筛选：${browseSummary}` : undefined"
        compact
      />
    </div>

    <!-- 右侧边栏（精品推荐 + 精选专题） -->
    <aside class="topic-sidebar">
      <!-- 精品成套资料推荐 -->
      <div v-if="suiteRecommendList.length" class="sidebar-section">
        <div class="sidebar-header">
          <h4>精品成套资料推荐</h4>
        </div>
        <div class="suite-recommend-item" v-for="(rec, ridx) in suiteRecommendList" :key="'sr'+ridx" @click="$emit('open-resource', rec)">
          <span class="recommend-badge">{{ rec.badge || '1' }}</span>
          <div class="recommend-content">
            <p class="recommend-title">{{ rec.title }}</p>
            <span class="recommend-count">{{ rec.countText }}</span>
          </div>
        </div>
      </div>
      <div v-if="sidebarTopics.length || topicLinks.length" class="sidebar-section">
        <div class="sidebar-header">
          <h4>精选专题</h4><span class="sidebar-more">更多 ›</span>
        </div>
        <div class="sidebar-list">
          <div v-for="(sp, sidx) in sidebarTopics" :key="'sp'+sidx" class="sidebar-item" @click="$emit('open-resource', sp)">
            <div class="sidebar-cover" :style="{ background: sp.coverColor || '#4CAF50' }">
              <span style="font-size:9px;color:#fff;font-weight:600;">{{ sp.tag || '专题' }}</span>
            </div>
            <div class="sidebar-content">
              <p class="sidebar-item-title">{{ sp.title }}</p>
              <span class="sidebar-date">{{ sp.date }}</span>
            </div>
          </div>
        </div>
        <div v-if="topicLinks.length" class="sidebar-links">
          <a v-for="(link, lidx) in topicLinks" :key="'tl'+lidx" href="javascript:;" class="sidebar-link">
            <span class="link-dot"></span>{{ link.text }}
          </a>
        </div>
      </div>
    </aside>
  </div>
</template>

<script setup lang="ts">
import EmptyState from '@/components/shared/EmptyState.vue'

defineProps<{
  apiResources: any[]
  displayTotal: number
  loading: boolean
  resourceMode: 'single' | 'suite'
  sortType: string
  browseSummary?: string
  suiteRecommendList: any[]
  sidebarTopics: any[]
  topicLinks: any[]
  getDocIconClass: (item: any) => string
  getDocIconLetter: (item: any) => string
}>()

defineEmits<{
  'update:resourceMode': [value: 'single' | 'suite']
  'update:sortType': [value: string]
  'open-resource': [item: any]
}>()
</script>

<style scoped>
/* 专题布局 */
.topic-main-area {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}
.topic-card-grid {
  flex: 1;
  min-width: 0;
}
.topic-toolbar {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 16px; margin-bottom: 14px;
  background: #fff; border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  flex-wrap: wrap;
}
.mode-btn {
  padding: 5px 16px;
  border-radius: 16px;
  background: #fff;
  color: #5D4E37;
  border: 1px solid #E0D9D0;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.mode-btn.active { background: var(--color-primary, #4361EE); color: #fff; border-color: transparent; }
.sort-btn {
  padding: 4px 12px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: color 0.2s;
}
.sort-btn.active { color: var(--color-primary, #4361EE); font-weight: 600; }
.result-count {
  margin-left: auto;
  font-size: 13px;
  color: #999;
}
.doc-type-selector {
  display: inline-flex; align-items: center; gap: 4px;
  font-size: 13px; color: #666; cursor: pointer;
  padding: 3px 8px; border-radius: 14px;
  border: 1px solid #E0D9D0; background: #fff;
  transition: all 0.2s;
}
.doc-type-selector:hover { border-color: var(--color-primary, #4361EE); color: var(--color-primary, #4361EE); }

/* 骨架加载 */
.topic-skeleton-list { padding: 8px 0; }
.topic-skeleton-row {
  height: 56px;
  margin: 0 0 10px;
  border-radius: 8px;
  background: linear-gradient(90deg, #f5f5f5 25%, #ebebeb 50%, #f5f5f5 75%);
  background-size: 200% 100%;
  animation: topic-shimmer 1.2s ease-in-out infinite;
}
@keyframes topic-shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* 专题资源列表 */
.topic-resource-list {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
  overflow: hidden;
}
.topic-resource-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.15s;
}
.topic-resource-item:last-child { border-bottom: none; }
.topic-resource-item:hover { background: #fafbfc; }
.topic-resource-info { flex: 1; min-width: 0; }
.topic-resource-title {
  font-size: 15px; font-weight: 500; color: #333; line-height: 1.45;
  margin-bottom: 5px; display: -webkit-box;
  -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.topic-resource-meta {
  display: flex; gap: 4px;
  font-size: 12px; color: #999;
  flex-wrap: wrap;
}
.topic-resource-album {
  margin-top: 4px; font-size: 12px; color: #888;
}
.album-link {
  color: var(--color-primary, #4361EE); cursor: pointer;
}
.album-link:hover { text-decoration: underline; }

/* 文档图标 */
.doc-icon-box {
  display: inline-flex; align-items: center; justify-content: center;
  width: 32px; height: 32px; border-radius: 6px; font-size: 10px; font-weight: 700; flex-shrink: 0;
}
.doc-icon-box.icon-pdf-box { background: #C0392B; color: #fff; }
.doc-icon-box.icon-ppt-box { background: #D35400; color: #fff; }
.doc-icon-box.icon-word-box { background: #2B5797; color: #fff; }

/* 右侧精品成套推荐 */
.topic-sidebar {
  width: 280px; flex-shrink: 0;
  background: #FFFBF0;
  border-radius: 10px;
  padding: 16px;
}
.sidebar-section { margin-bottom: 18px; }
.sidebar-section:last-child { margin-bottom: 0; }
.sidebar-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12px;
}
.sidebar-header h4 {
  font-size: 16px; color: #E65100; margin: 0; font-weight: 700;
}
.sidebar-more {
  font-size: 12px; color: #999; cursor: pointer;
}
.suite-recommend-item {
  display: flex; gap: 10px; cursor: pointer;
  padding: 10px; border-radius: 8px;
  background: rgba(255,255,255,0.7);
  transition: background 0.15s; align-items: flex-start;
  margin-bottom: 10px;
}
.suite-recommend-item:last-child { margin-bottom: 0; }
.suite-recommend-item:hover { background: #fff; }
.recommend-badge {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 20px; height: 20px;
  background: linear-gradient(135deg,#FFB74D,#FF9800);
  color: #fff; border-radius: 50%;
  font-size: 11px; font-weight: 700; flex-shrink: 0;
}
.recommend-content { flex: 1; min-width: 0; }
.recommend-title {
  font-size: 13px; color: #333; line-height: 1.4;
  margin: 0 0 3px; display: -webkit-box;
  -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden;
}
.recommend-count {
  font-size: 12px; color: #E65100; font-weight: 600;
}
.sidebar-list {
  display: flex; flex-direction: column; gap: 10px;
  margin-bottom: 14px;
}
.sidebar-item {
  display: flex; gap: 10px; cursor: pointer;
  padding: 6px; border-radius: 6px;
  transition: background 0.15s;
}
.sidebar-item:hover { background: rgba(255,255,255,0.7); }
.sidebar-cover {
  width: 44px; height: 52px; border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.sidebar-content { flex: 1; min-width: 0; }
.sidebar-item-title {
  font-size: 13px; color: #333; line-height: 1.35;
  margin: 0 0 3px; display: -webkit-box;
  -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden;
}
.sidebar-date { font-size: 11px; color: #bbb; }
.sidebar-links {
  display: flex; flex-direction: column; gap: 6px;
  padding-top: 10px;
  border-top: 1px dashed #EEE5C4;
}
.sidebar-link {
  display: flex; align-items: center; gap: 6px;
  font-size: 12px; color: #888;
  text-decoration: none;
  line-height: 1.5;
  white-space: nowrap;
  overflow: hidden; text-overflow: ellipsis;
}
.sidebar-link:hover { color: var(--color-primary, #4361EE); }
.link-dot {
  width: 4px; height: 4px; border-radius: 50%;
  background: #ccc; flex-shrink: 0;
}

@media (max-width: 1100px) {
  .topic-sidebar { display: none; }
}
</style>
