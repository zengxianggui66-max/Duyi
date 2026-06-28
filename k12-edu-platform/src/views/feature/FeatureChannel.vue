<template>
  <div class="feature-channel-page">
    <!-- 频道 Banner -->
    <section class="channel-banner" :style="{ background: channelInfo.bgColor }">
      <div class="container">
        <!-- 面包屑 -->
        <div class="breadcrumb">
          <router-link to="/" class="breadcrumb-link">首页</router-link>
          <span class="breadcrumb-sep">/</span>
          <router-link to="/feature" class="breadcrumb-link">特色频道</router-link>
          <span class="breadcrumb-sep">/</span>
          <span>{{ channelInfo.name }}</span>
        </div>
        <!-- 标题区 -->
        <div class="banner-content">
          <div class="banner-left">
            <h1>{{ channelInfo.icon }} {{ channelInfo.name }}</h1>
            <p class="banner-desc">{{ channelInfo.desc }}</p>
          </div>
          <div class="banner-right">
            <div class="banner-stats">
              <div class="stat-item">
                <span class="stat-num">{{ channelInfo.stats.total }}</span>
                <span class="stat-label">资源总数</span>
              </div>
              <div class="stat-item">
                <span class="stat-num">{{ channelInfo.stats.elite }}</span>
                <span class="stat-label">精品资源</span>
              </div>
              <div class="stat-item">
                <span class="stat-num">{{ channelInfo.stats.free }}</span>
                <span class="stat-label">免费资源</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <div class="container main-container">
      <!-- ===================== 顶部主题分类导航 ===================== -->
      <section class="top-nav-section">
        <button
          v-for="tab in mainTabs"
          :key="tab.key"
          class="main-tab-btn"
          :class="{ active: activeMainTab === tab.key }"
          @click="switchMainTab(tab.key)"
        >
          {{ tab.icon }} {{ tab.name }}
        </button>
      </section>

      <!-- ===================== 主内容区：左侧筛选 + 右侧内容 ===================== -->
      <div class="content-layout">
        <FeatureFilterSidebar
          :show-grade-filter="channelInfo.showGradeFilter"
          :show-subject-filter="channelInfo.showSubjectFilter"
          :grade-filters="gradeFilters"
          :level-filters="levelFilters"
          :format-filters="formatFilters"
          :subject-filters="subjectFilters"
          :selected-grade="selectedGrade"
          :selected-level="selectedLevel"
          :selected-format="selectedFormat"
          :selected-subject="selectedSubject"
          @update:selected-grade="selectedGrade = $event"
          @update:selected-level="selectedLevel = $event"
          @update:selected-format="selectedFormat = $event"
          @update:selected-subject="selectedSubject = $event"
        />

        <!-- 右侧主内容 -->
        <main class="right-content">

          <!-- ===================== 班会育人专属：主题导航网格 ===================== -->
          <section v-if="channelType === 'banhui' && activeMainTab === 'all'" class="banhui-theme-nav">
            <div class="bh-section-header">
              <span class="bh-section-title">📚 班会主题导航</span>
              <span class="bh-section-more">全部主题 →</span>
            </div>
            <div class="banhui-theme-grid">
              <div v-for="theme in banhuiThemes" :key="theme.key"
                class="bh-theme-item"
                @click="switchMainTab(theme.key)"
              >
                <span class="bh-theme-icon">{{ theme.icon }}</span>
                <span class="bh-theme-name">{{ theme.name }}</span>
                <span class="bh-theme-count">{{ theme.count }}套</span>
              </div>
            </div>
          </section>

          <!-- ===================== 班会育人专属：热门主题卡片 ===================== -->
          <section v-if="channelType === 'banhui' && activeMainTab === 'all'" class="banhui-hot-section">
            <div class="bh-section-header">
              <span class="bh-section-title">🔥 热门主题</span>
              <div class="bh-hot-tabs">
                <span
                  v-for="tab in hotDownloadTabs"
                  :key="tab.key"
                  :class="['bh-hot-tab', { active: activeHotTab === tab.key }]"
                  @click="activeHotTab = tab.key"
                >{{ tab.name }}</span>
              </div>
            </div>
            <div class="bh-hot-list">
              <div v-for="(item, idx) in hotDownloadItems" :key="item.id"
                class="bh-hot-item"
                @click="goToDetail(item)"
              >
                <span :class="['bh-rank', idx < 3 ? 'top' : '']">{{ idx + 1 }}</span>
                <div class="bh-hot-cover" :style="{ background: getCoverColor(item.id) }">
                  <span class="bh-hot-icon">{{ item.icon }}</span>
                </div>
                <div class="bh-hot-info">
                  <p class="bh-hot-title">{{ item.title }}</p>
                  <p class="bh-hot-meta">{{ item.meta }}</p>
                </div>
                <div class="bh-hot-stat">
                  <span>⬇ {{ formatCount(item.downloadCount) }}</span>
                  <span v-if="item.isFree" class="bh-free-badge">免费</span>
                </div>
              </div>
            </div>
          </section>

          <!-- ===================== 班会育人专属：最新上传 ===================== -->
          <section v-if="channelType === 'banhui' && activeMainTab === 'all'" class="banhui-latest-section">
            <div class="bh-section-header">
              <span class="bh-section-title">🆕 最新上传</span>
              <span class="bh-section-more">查看更多 →</span>
            </div>
            <div class="bh-latest-list">
              <div v-for="item in latestUploads" :key="item.id" class="bh-latest-item" @click="goToDetail(item)">
                <div class="bh-latest-cover" :style="{ background: getCoverColor(item.id) }">
                  <span>{{ item.icon }}</span>
                </div>
                <div class="bh-latest-info">
                  <p class="bh-latest-title">{{ item.title }}</p>
                  <p class="bh-latest-meta">
                    <span class="bh-tag">{{ item.tag }}</span>
                    <span>{{ item.format }}</span>
                    <span>{{ item.pageCount }}页</span>
                    <span>{{ item.uploadTime }}</span>
                  </p>
                </div>
                <div class="bh-latest-right">
                  <span v-if="item.isFree" class="bh-free-badge">免费</span>
                  <span v-else class="bh-vip-badge">精品</span>
                  <span class="bh-dl-count">⬇ {{ formatCount(item.downloadCount) }}</span>
                </div>
              </div>
            </div>
          </section>

          <!-- 精品推荐区 -->
          <section class="elite-section" v-if="activeMainTab === 'all'">
            <div class="section-header">
              <h3 class="section-title">{{ channelInfo.eliteTitle || '🏅 精品推荐' }}</h3>
              <span class="section-desc">{{ channelInfo.eliteDesc || '精选优质资源合集' }}</span>
            </div>
            <div class="elite-grid">
              <div v-for="album in eliteAlbums" :key="album.id" class="elite-card" @click="goToDetail(album)">
                <div class="elite-cover" :style="{ background: album.coverColor }">
                  <span class="elite-icon">{{ album.icon }}</span>
                  <span class="elite-count">{{ album.resourceCount }}套</span>
                </div>
                <div class="elite-info">
                  <h4 class="elite-title">{{ album.title }}</h4>
                  <p class="elite-meta">{{ album.meta }}</p>
                  <div class="elite-bottom">
                    <span class="elite-downloads">⬇ {{ formatCount(album.downloadCount) }}</span>
                    <span class="elite-badge">精品专辑</span>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <FeatureResourceList
            :loading="loading"
            :total="total"
            :resources="resources"
            :stats="stats"
            v-model:sort-by="sortBy"
            :current-page="currentPage"
            :default-desc="channelInfo.defaultDesc"
            :get-cover-color="getCoverColor"
            :get-resource-icon="getResourceIcon"
            :get-type-name="getTypeName"
            :format-count="formatCount"
            @sort-change="loadData"
            @page-change="onPageChange"
            @open="goToDetail"
          />

        </main>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import FeatureFilterSidebar from '@/components/feature/FeatureFilterSidebar.vue'
import FeatureResourceList from '@/components/feature/FeatureResourceList.vue'
import { useFeatureChannel, banhuiThemes, hotDownloadTabs } from '@/composables/useFeatureChannel'

const {
  channelType, channelInfo, loading, total, resources, mainTabs, activeMainTab,
  eliteAlbums, stats, gradeFilters, levelFilters, formatFilters, subjectFilters,
  selectedGrade, selectedLevel, selectedFormat, selectedSubject, sortBy, currentPage,
  activeHotTab, hotDownloadItems, latestUploads, switchMainTab, loadData, goToDetail,
  getCoverColor, getResourceIcon, getTypeName, formatCount,
} = useFeatureChannel()

function onPageChange(p: number) {
  currentPage.value = p
  loadData()
}
</script>

<style scoped>
/* ===================== Banner ===================== */
.channel-banner {
  color: #fff;
  padding: 32px 0 28px;
}
.breadcrumb {
  margin-bottom: 16px;
  font-size: 13px;
}
.breadcrumb-link {
  color: rgba(255,255,255,0.8);
  text-decoration: none;
}
.breadcrumb-link:hover { color: #fff; }
.breadcrumb-sep { margin: 0 8px; opacity: 0.6; }

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
}
.banner-left h1 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
}
.banner-desc {
  font-size: 14px;
  opacity: 0.9;
  max-width: 500px;
}
.banner-stats {
  display: flex;
  gap: 32px;
}
.stat-item {
  text-align: center;
}
.stat-num {
  display: block;
  font-size: 28px;
  font-weight: 700;
}
.stat-label {
  font-size: 12px;
  opacity: 0.85;
}

/* ===================== 主容器 ===================== */
.main-container {
  padding: 20px 24px 60px;
}

/* ===================== 顶部主题分类导航 ===================== */
.top-nav-section {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 12px 16px;
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.main-tab-btn {
  padding: 8px 16px;
  border: 1px solid transparent;
  border-radius: 20px;
  background: transparent;
  font-size: 13px;
  color: var(--text-regular);
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.main-tab-btn:hover {
  background: var(--bg-page);
  color: var(--color-primary);
}
.main-tab-btn.active {
  background: linear-gradient(135deg, #FF6B6B, #ee5a24);
  color: #fff;
}

/* ===================== 内容布局 ===================== */
.content-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* ===================== 左侧边栏筛选 ===================== */
.left-sidebar {
  width: 180px;
  flex-shrink: 0;
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 16px;
}

.filter-block {
  margin-bottom: 20px;
}
.filter-block:last-child {
  margin-bottom: 0;
}

.filter-block-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 10px;
  color: var(--text-primary);
}

.filter-options {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.filter-option {
  padding: 8px 12px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  font-size: 13px;
  color: var(--text-regular);
  text-align: left;
  cursor: pointer;
  transition: all 0.2s;
}
.filter-option:hover {
  background: var(--bg-page);
  color: var(--color-primary);
}
.filter-option.active {
  background: linear-gradient(135deg, #FF6B6B, #ee5a24);
  color: #fff;
}

/* ===================== 右侧主内容 ===================== */
.right-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ===================== 精品专辑区 ===================== */
.elite-section {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 16px;
}
.section-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 14px;
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0;
}
.section-desc {
  font-size: 12px;
  color: var(--text-secondary);
}
.elite-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.elite-card {
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s;
}
.elite-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.08);
}
.elite-cover {
  height: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}
.elite-icon {
  font-size: 28px;
  color: rgba(255,255,255,0.95);
}
.elite-count {
  position: absolute;
  bottom: 6px;
  right: 6px;
  background: rgba(0,0,0,0.4);
  color: #fff;
  padding: 2px 6px;
  border-radius: var(--radius-round);
  font-size: 10px;
}
.elite-info {
  padding: 10px;
}
.elite-title {
  font-size: 12px;
  font-weight: 600;
  margin: 0 0 4px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.elite-meta {
  font-size: 10px;
  color: var(--text-secondary);
  margin: 0 0 6px;
}
.elite-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.elite-downloads {
  font-size: 11px;
  color: var(--text-secondary);
}
.elite-badge {
  padding: 2px 6px;
  background: linear-gradient(135deg, #F59E0B, #EF4444);
  color: #fff;
  border-radius: var(--radius-round);
  font-size: 9px;
  font-weight: 600;
}

/* ===================== 资源列表区 ===================== */
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
.result-info {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.result-count {
  font-size: 13px;
  color: var(--text-secondary);
}
.result-count strong { color: var(--text-primary); }
.result-stat {
  font-size: 12px;
  color: var(--text-secondary);
}
.result-stat strong { color: #FF6B6B; }
.sort-box {
  display: flex;
  align-items: center;
  gap: 8px;
}
.sort-label {
  font-size: 13px;
  color: var(--text-secondary);
}

/* ===================== 资源卡片 V2 ===================== */
.resource-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
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
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
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
.cover-icon {
  font-size: 26px;
  color: rgba(255,255,255,0.9);
}
.type-badge {
  position: absolute;
  bottom: 4px;
  left: 4px;
  background: rgba(0,0,0,0.35);
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
.level-badge.elite { background: linear-gradient(135deg, #F59E0B, #EF4444); }
.level-badge.vip { background: linear-gradient(135deg, #8B5CF6, #6D28D9); }
.level-badge.free { background: #10B981; }

.card-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
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
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 3px;
}
.meta-item {
  font-size: 10px;
  color: var(--text-secondary);
  padding: 2px 6px;
  background: #fff;
  border-radius: var(--radius-sm);
}
.card-detail-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 3px;
}
.detail-item {
  font-size: 10px;
  color: var(--text-secondary);
}
.card-stats-row {
  display: flex;
  gap: 10px;
  margin-top: auto;
}
.stat-item {
  font-size: 11px;
  color: var(--text-secondary);
}

.empty-hint { padding: 40px 0; }
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

/* ===================== 响应式 ===================== */
@media (max-width: 1100px) {
  .elite-grid { grid-template-columns: repeat(3, 1fr); }
  .banhui-theme-grid { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 900px) {
  .content-layout { flex-direction: column; }
  .left-sidebar { width: 100%; flex-direction: row; flex-wrap: wrap; display: flex; }
  .filter-block { flex: 1; min-width: 140px; }
  .filter-options { flex-direction: row; flex-wrap: wrap; }
  .filter-option { flex: 1; min-width: 50px; text-align: center; }
  .banhui-theme-grid { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 600px) {
  .elite-grid { grid-template-columns: repeat(2, 1fr); }
  .resource-card-v2 { flex-direction: column; }
  .card-cover { width: 100%; height: 90px; }
  .top-nav-section { overflow-x: auto; flex-wrap: nowrap; }
  .banhui-theme-grid { grid-template-columns: repeat(3, 1fr); }
  .main-tab-btn { flex-shrink: 0; }
}

/* ===================== 班会育人专属样式 ===================== */

/* 公共 section 头部 */
.bh-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.bh-section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.bh-section-more {
  font-size: 12px;
  color: #FF6B6B;
  cursor: pointer;
}
.bh-section-more:hover { text-decoration: underline; }

/* 班会主题导航网格 */
.banhui-theme-nav {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 16px;
}
.banhui-theme-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 10px;
}
.bh-theme-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 6px;
  background: var(--bg-page);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.bh-theme-item:hover {
  background: #fff7f5;
  border-color: #FF6B6B;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(255,107,107,0.15);
}
.bh-theme-icon {
  font-size: 24px;
  margin-bottom: 4px;
}
.bh-theme-name {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-primary);
}
.bh-theme-count {
  font-size: 10px;
  color: var(--text-secondary);
  margin-top: 2px;
}

/* 热门下载区 */
.banhui-hot-section {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 16px;
}
.bh-hot-tabs {
  display: flex;
  gap: 4px;
}
.bh-hot-tab {
  padding: 4px 12px;
  border-radius: var(--radius-round);
  font-size: 12px;
  cursor: pointer;
  color: var(--text-secondary);
  border: 1px solid var(--border-light);
  transition: all 0.2s;
}
.bh-hot-tab.active {
  background: linear-gradient(135deg, #FF6B6B, #ee5a24);
  color: #fff;
  border-color: transparent;
}
.bh-hot-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.bh-hot-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.2s;
}
.bh-hot-item:hover { background: var(--bg-page); }
.bh-rank {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  background: #eee;
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.bh-rank.top {
  background: linear-gradient(135deg, #FF6B6B, #ee5a24);
  color: #fff;
}
.bh-hot-cover {
  width: 44px;
  height: 34px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.bh-hot-icon { font-size: 18px; }
.bh-hot-info {
  flex: 1;
  min-width: 0;
}
.bh-hot-title {
  font-size: 13px;
  font-weight: 500;
  margin: 0 0 2px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.bh-hot-meta {
  font-size: 11px;
  color: var(--text-secondary);
  margin: 0;
}
.bh-hot-stat {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 3px;
  flex-shrink: 0;
  font-size: 11px;
  color: var(--text-secondary);
}
.bh-free-badge {
  padding: 1px 6px;
  background: #10B981;
  color: #fff;
  border-radius: var(--radius-round);
  font-size: 10px;
  font-weight: 600;
}
.bh-vip-badge {
  padding: 1px 6px;
  background: linear-gradient(135deg, #F59E0B, #EF4444);
  color: #fff;
  border-radius: var(--radius-round);
  font-size: 10px;
  font-weight: 600;
}

/* 最新上传区 */
.banhui-latest-section {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 16px;
}
.bh-latest-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.bh-latest-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 10px;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
}
.bh-latest-item:hover {
  background: #fff7f5;
  box-shadow: 0 2px 8px rgba(255,107,107,0.08);
}
.bh-latest-cover {
  width: 44px;
  height: 34px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}
.bh-latest-info {
  flex: 1;
  min-width: 0;
}
.bh-latest-title {
  font-size: 13px;
  font-weight: 500;
  margin: 0 0 4px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.bh-latest-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  color: var(--text-secondary);
  margin: 0;
}
.bh-tag {
  padding: 1px 6px;
  background: #fff0f0;
  color: #FF6B6B;
  border-radius: var(--radius-round);
  font-size: 10px;
  font-weight: 600;
}
.bh-latest-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  flex-shrink: 0;
}
.bh-dl-count {
  font-size: 11px;
  color: var(--text-secondary);
}
</style>
