<template>
  <div class="grade-page">
    <GradeNavBar
      :grade-keys="currentGradeKeys"
      :grade-names="currentGradeNames"
      :grade-emojis="currentGradeEmojis"
      :grade-level="gradeLevel"
      v-model:search-keyword="searchKeyword"
      :search-placeholder="searchPlaceholder"
      @switch-grade="switchGrade"
      @search="handleSearch"
    />

    <!-- ===================== 页面标题栏（彩色背景）===================== -->
    <section class="page-header-bar" :style="bannerStyle">
      <div class="container">
        <!-- 面包屑 -->
        <div class="header-breadcrumb">
          <span class="header-breadcrumb-item" @click="$router.push('/')">首页</span>
          <span class="header-breadcrumb-sep">/</span>
          <span class="header-breadcrumb-item current">{{ gradeEmoji }} {{ gradeName }}资源专区</span>
        </div>
        <!-- 标题 -->
        <h1 class="page-header-title">{{ gradeEmoji }} {{ gradeName }}资源专区</h1>
        <!-- 副标题（美术/舞蹈专区） -->
        <p v-if="isArtSection || isDanceSection" class="page-header-subtitle">
          {{ isArtSection ? '素描·色彩·速写·创意设计·美术考级与艺考专区' : '中国舞·芭蕾舞·街舞·拉丁舞·舞蹈考级与艺考专区' }}
        </p>
      </div>
    </section>

    <!-- ===================== 主体区域（左侧学科导航 + 右侧分类内容）===================== -->
    <div class="container grade-body">
      <GradeSubjectSidebar
        :subjects="sidebarSubjects"
        :active-subject="activeSubject"
        @select="toggleSubject"
      />

      <!-- 右侧内容区 -->
      <main class="grade-content-right">
        <!-- 年级标签栏（非小学/初中/高中学段时隐藏，显示在学科前） -->
        <div v-if="!isArtSection && !isDanceSection && !isSpecificGrade" class="grade-tab-bar">
          <span
            v-for="g in currentGradeKeys"
            :key="g"
            class="grade-tab-item"
            :class="{ active: activeGrade === g }"
            @click="selectGrade(g)"
          >
            {{ currentGradeEmojis[g] }} {{ currentGradeNames[g] }}
          </span>
        </div>

        <!-- 顶部标签栏 -->
        <div class="top-tabs-bar">
          <span
            v-for="tab in topTabs"
            :key="tab.key"
            class="top-tab-item"
            :class="{ active: activeTopTab === tab.key }"
            @click="activeTopTab = tab.key"
          >
            {{ tab.name }}
          </span>
        </div>

        <!-- ========== 普通学段内容：同步教学/复习备考/小升初 ========== -->
        <template v-if="!isArtSection && !isDanceSection">
          <!-- 加载状态 -->
          <div v-if="isLoading" class="loading-state">
            <el-skeleton :rows="3" animated />
          </div>

          <template v-else>
            <!-- 同步教学区块 -->
            <div class="content-section">
              <div class="section-title">
                <span class="title-text">同步教学</span>
                <span class="title-badge">新教材</span>
              </div>
              <!-- 版本 -->
              <div v-if="currentVersions.length > 0" class="filter-row">
                <span class="filter-label">版本:</span>
                <div class="filter-tags">
                  <span
                    v-for="v in currentVersions"
                    :key="v.key"
                    class="filter-tag"
                    :class="{ active: selectedVersion === v.key }"
                    @click="goToVersion(v.key, v.name)"
                  >{{ v.name }}</span>
                </div>
              </div>
              <!-- 栏目 -->
              <div v-if="currentCategories.length > 0" class="filter-row">
                <span class="filter-label">栏目:</span>
                <div class="filter-tags">
                  <span class="filter-tag">不限</span>
                  <span
                    v-for="c in currentCategories"
                    :key="c.key"
                    class="filter-tag"
                    :class="{ active: selectedCategory === c.key }"
                    @click="selectedCategory = selectedCategory === c.key ? '' : c.key"
                  >{{ c.icon ? c.icon + ' ' : '' }}{{ c.name }}</span>
                </div>
              </div>
              <!-- 资源类型 -->
              <div v-if="currentResourceTypes.length > 0" class="filter-row">
                <span class="filter-label">类型:</span>
                <div class="filter-tags">
                  <span class="filter-tag">全部</span>
                  <span
                    v-for="t in currentResourceTypes"
                    :key="t.key"
                    class="filter-tag"
                    :class="{ active: selectedQuickTag === t.key }"
                    @click="selectedQuickTag = selectedQuickTag === t.key ? '' : t.key"
                  >{{ t.icon ? t.icon + ' ' : '' }}{{ t.name }}</span>
                </div>
              </div>
            </div>

            <!-- 复习备考区块 -->
            <div v-if="currentExamPapers.length > 0" class="content-section">
              <div class="section-title">
                <span class="title-text">复习备考</span>
              </div>
              <div class="filter-row">
                <span class="filter-label">试卷:</span>
                <div class="filter-tags">
                  <span
                    v-for="p in currentExamPapers"
                    :key="p.key"
                    class="filter-tag"
                    :class="{ active: selectedPaper === p.key }"
                    @click="selectedPaper = selectedPaper === p.key ? '' : p.key"
                  >{{ p.name }}</span>
                </div>
              </div>
            </div>

          <!-- 小升初区块 -->
          <div class="content-section">
            <div class="section-title">
              <span class="title-text">升学备考</span>
            </div>
            <div class="filter-row">
              <span class="filter-label">备考:</span>
              <div class="filter-tags">
                <span class="filter-tag">专项复习</span>
                <span class="filter-tag">模拟预测</span>
                <span class="filter-tag">真题汇编</span>
                <span class="filter-tag">升学指导</span>
                <span class="filter-tag more">更多 ›</span>
              </div>
            </div>
          </div>
          </template>
        </template>

        <!-- ========== 美术专区内容 ========== -->
        <template v-if="isArtSection">
          <!-- 考级备考区块 -->
          <div class="content-section art-section">
            <div class="section-title art-title">
              <span class="title-icon">🏅</span>
              <span class="title-text">考级备考</span>
              <span class="title-badge art">考级教材·真题·大纲</span>
            </div>
            <div class="filter-row">
              <span class="filter-label">教材:</span>
              <div class="filter-tags">
                <span class="filter-tag active">不限</span>
                <span class="filter-tag">中国美术学院考级</span>
                <span class="filter-tag">文旅部艺术发展中心</span>
                <span class="filter-tag">书法家协会考级</span>
                <span class="filter-tag">全国美术考级</span>
                <span class="filter-tag more">更多 ›</span>
              </div>
            </div>
            <div class="filter-row">
              <span class="filter-label">级别:</span>
              <div class="filter-tags">
                <span class="filter-tag">1级</span>
                <span class="filter-tag">2级</span>
                <span class="filter-tag">3级</span>
                <span class="filter-tag">4级</span>
                <span class="filter-tag">5级</span>
                <span class="filter-tag">6级</span>
                <span class="filter-tag">7级</span>
                <span class="filter-tag">8级</span>
                <span class="filter-tag">9级</span>
                <span class="filter-tag">10级</span>
              </div>
            </div>
          </div>

          <!-- 纸质类资源区块 -->
          <div class="content-section">
            <div class="section-title">
              <span class="title-icon">📄</span>
              <span class="title-text">纸质类资源</span>
            </div>
            <div class="filter-row">
              <span class="filter-label">类别:</span>
              <div class="filter-tags">
                <span class="filter-tag">不限</span>
                <span class="filter-tag">考级专用教材</span>
                <span class="filter-tag">临摹画册</span>
                <span class="filter-tag">范画集</span>
                <span class="filter-tag">考级真题/作品集</span>
                <span class="filter-tag">练习册/线稿本</span>
              </div>
            </div>
          </div>

          <!-- 视频/素材区块 -->
          <div class="content-section">
            <div class="section-title">
              <span class="title-icon">🎬</span>
              <span class="title-text">视频/素材</span>
            </div>
            <div class="filter-row">
              <span class="filter-label">类型:</span>
              <div class="filter-tags">
                <span class="filter-tag">不限</span>
                <span class="filter-tag">考级步骤示范</span>
                <span class="filter-tag">绘画技法微课</span>
                <span class="filter-tag">作品点评实录</span>
                <span class="filter-tag">电子范画库</span>
              </div>
            </div>
          </div>

          <!-- 文档类资源区块 -->
          <div class="content-section">
            <div class="section-title">
              <span class="title-icon">📊</span>
              <span class="title-text">文档类资源</span>
            </div>
            <div class="filter-row">
              <span class="filter-label">类型:</span>
              <div class="filter-tags">
                <span class="filter-tag">不限</span>
                <span class="filter-tag">考级教案/课件</span>
                <span class="filter-tag">考级大纲与考点</span>
                <span class="filter-tag">学员作品集模板</span>
                <span class="filter-tag">家长须知/通知</span>
              </div>
            </div>
          </div>
        </template>

        <!-- ========== 舞蹈专区内容 ========== -->
        <template v-if="isDanceSection">
          <!-- 考级备考区块 -->
          <div class="content-section dance-section">
            <div class="section-title dance-title">
              <span class="title-icon">🏅</span>
              <span class="title-text">考级备考</span>
              <span class="title-badge dance">考级教材·组合示范·音乐</span>
            </div>
            <div class="filter-row">
              <span class="filter-label">教材:</span>
              <div class="filter-tags">
                <span class="filter-tag active">不限</span>
                <span class="filter-tag">中国舞蹈家协会</span>
                <span class="filter-tag">北京舞蹈学院</span>
                <span class="filter-tag">民族民间舞考级</span>
                <span class="filter-tag">拉丁舞(ISDA/CBDF)</span>
                <span class="filter-tag">芭蕾舞考级</span>
                <span class="filter-tag more">更多 ›</span>
              </div>
            </div>
            <div class="filter-row">
              <span class="filter-label">级别:</span>
              <div class="filter-tags">
                <span class="filter-tag">1级</span>
                <span class="filter-tag">2级</span>
                <span class="filter-tag">3级</span>
                <span class="filter-tag">4级</span>
                <span class="filter-tag">5级</span>
                <span class="filter-tag">6级</span>
                <span class="filter-tag">7级</span>
                <span class="filter-tag">8级</span>
                <span class="filter-tag">9级</span>
                <span class="filter-tag">10级</span>
              </div>
            </div>
          </div>

          <!-- 纸质类资源区块 -->
          <div class="content-section">
            <div class="section-title">
              <span class="title-icon">📖</span>
              <span class="title-text">纸质类资源</span>
            </div>
            <div class="filter-row">
              <span class="filter-label">类型:</span>
              <div class="filter-tags">
                <span class="filter-tag">不限</span>
                <span class="filter-tag">舞蹈考级教材</span>
                <span class="filter-tag">考级组合手册</span>
                <span class="filter-tag">基本功训练手册</span>
                <span class="filter-tag">教案/节拍谱</span>
                <span class="filter-tag">考级评分标准</span>
              </div>
            </div>
          </div>

          <!-- 音乐/视频区块 -->
          <div class="content-section">
            <div class="section-title">
              <span class="title-icon">🎵</span>
              <span class="title-text">音乐/视频</span>
            </div>
            <div class="filter-row">
              <span class="filter-label">类型:</span>
              <div class="filter-tags">
                <span class="filter-tag">不限</span>
                <span class="filter-tag">考级组合示范</span>
                <span class="filter-tag">基本功教学</span>
                <span class="filter-tag">成品舞教学</span>
                <span class="filter-tag">考级队形/镜面教学</span>
                <span class="filter-tag">考级音乐库</span>
              </div>
            </div>
          </div>

          <!-- 文档类资源区块 -->
          <div class="content-section">
            <div class="section-title">
              <span class="title-icon">📋</span>
              <span class="title-text">文档类资源</span>
            </div>
            <div class="filter-row">
              <span class="filter-label">类型:</span>
              <div class="filter-tags">
                <span class="filter-tag">不限</span>
                <span class="filter-tag">考级教学课件</span>
                <span class="filter-tag">考级大纲与流程</span>
                <span class="filter-tag">学员训练计划</span>
                <span class="filter-tag">舞台妆容/服装</span>
              </div>
            </div>
          </div>
        </template>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import GradeNavBar from '@/components/grade/GradeNavBar.vue'
import GradeSubjectSidebar from '@/components/grade/GradeSubjectSidebar.vue'
import { useGradePage } from '@/composables/useGradePage'

const {
  gradeLevel, currentGradeKeys, currentGradeNames, currentGradeEmojis,
  gradeName, gradeEmoji, bannerStyle, searchKeyword, searchPlaceholder,
  apiSubjects, sidebarSubjects, activeSubject, isArtSection, isDanceSection, isSpecificGrade,
  topTabs, activeTopTab, isLoading, currentVersions, currentCategories,
  currentResourceTypes, currentExamPapers, currentKnowledgePoints,
  selectedVersion, selectedCategory, selectedPaper, selectedKnowledge,
  switchGrade, handleSearch, toggleSubject, goToVersion, selectGrade, activeGrade,
  visibleZoneCards, quickFilterZone, displayResources, formatCount, getCoverColor,
  selectedQuickTag, quickTags,
} = useGradePage()
</script>

<style scoped>
/* ===================== 年级导航栏 ===================== */
.grade-nav-bar {
  background: #fff;
  border-bottom: 1px solid var(--border-light);
  position: sticky;
  top: var(--header-height, 64px);
  z-index: 90;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.grade-nav-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 24px;
  height: 52px;
  flex-wrap: wrap;
}
.grade-nav-tab {
  padding: 7px 18px;
  border-radius: var(--radius-round, 20px);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  color: var(--text-regular);
  transition: all 0.2s ease;
  white-space: nowrap;
  user-select: none;
}
.grade-nav-tab:hover {
  color: var(--color-primary);
  background: var(--color-primary-bg);
}
.grade-nav-tab.active {
  color: #fff;
  background: var(--color-primary);
  box-shadow: 0 3px 10px rgba(67,97,238,0.35);
  transform: translateY(-1px);
}
/* 美术专区导航样式 */
.grade-nav-tab.art-tab.active {
  background: linear-gradient(135deg, #FF69B4, #E91E63);
  box-shadow: 0 3px 10px rgba(233,30,99,0.35);
}
/* 舞蹈专区导航样式 */
.grade-nav-tab.dance-tab.active {
  background: linear-gradient(135deg, #A18CD1, #9C27B0);
  box-shadow: 0 3px 10px rgba(156,39,176,0.35);
}
/* 导航分隔线 */
.nav-divider {
  width: 1px;
  height: 20px;
  background: rgba(255,255,255,0.3);
  margin: 0 8px;
}

/* ===================== 页面标题栏（彩色背景）===================== */
.page-header-bar {
  color: #fff;
  padding: 24px 0;
}
.header-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
}
.header-breadcrumb-item {
  opacity: 0.9;
  cursor: pointer;
  transition: opacity 0.2s;
}
.header-breadcrumb-item:hover {
  opacity: 1;
  text-decoration: underline;
}
.header-breadcrumb-item.current {
  font-weight: 600;
}
.header-breadcrumb-sep {
  opacity: 0.6;
}
.page-header-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  line-height: 1.3;
}
.page-header-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 8px 0 0;
}
.nav-search-box {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ===================== 学科导航（横向，上侧） ===================== */
.subject-nav-top {
  padding: 0 24px;
  margin-top: 16px;
}
.subject-nav-top .subject-nav-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #E4E7ED;
}
.subject-nav-top .subject-nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  font-weight: 500;
}
.subject-nav-top .subject-nav-item:hover {
  background: #F5F7FA;
  color: var(--color-primary);
}
.subject-nav-top .subject-nav-item.active {
  background: var(--color-primary);
  color: #fff;
}
.subject-nav-top .subject-arrow {
  font-size: 16px;
}

/* ===================== 顶部标签栏 ===================== */
.quick-bar {
  display: flex;
  gap: 0;
  padding: 0;
  border-bottom: 2px solid #E4E7ED;
  margin-top: 16px;
  overflow-x: auto;
}
.quick-tab {
  padding: 12px 20px;
  font-size: 15px;
  font-weight: 500;
  color: #606266;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  margin-bottom: -2px;
  transition: all 0.2s;
  white-space: nowrap;
  position: relative;
}
.quick-tab:hover {
  color: var(--color-primary);
}
.quick-tab.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
  font-weight: 600;
}
.tab-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  background: #F56C6C;
  color: #fff;
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 8px;
  font-weight: normal;
}

.classify-content {
  padding: 16px 0;
}
.classify-block {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.classify-section {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}
.section-label {
  width: 60px;
  font-size: 13px;
  font-weight: 600;
  color: #909399;
  padding-top: 6px;
  flex-shrink: 0;
}
.section-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex: 1;
}
.tag-item {
  padding: 6px 14px;
  background: #F5F7FA;
  border: 1px solid #E4E7ED;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}
.tag-item:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.tag-item.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
}
.tag-item.large {
  padding: 10px 20px;
  font-size: 14px;
  border-radius: 6px;
}

/* ===================== 主体区域布局 ===================== */
.grade-body {
  display: flex;
  gap: 20px;
  padding: 20px 0;
}

/* 左侧学科导航 */
.subject-sidebar-left {
  width: 140px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #E4E7ED;
  overflow: hidden;
}
.subject-nav-list-vertical {
  display: flex;
  flex-direction: column;
}
.subject-nav-item-vertical {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  cursor: pointer;
  border-bottom: 1px solid #F1F2F6;
  transition: all 0.2s;
  font-size: 14px;
  color: #606266;
}
.subject-nav-item-vertical:last-child {
  border-bottom: none;
}
.subject-nav-item-vertical:hover {
  background: #F5F7FA;
}
.subject-nav-item-vertical.active {
  background: var(--color-primary);
  color: #fff;
}
.subject-nav-item-vertical .subject-arrow {
  font-size: 16px;
  opacity: 0.6;
}

/* 右侧内容区 */
.grade-content-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 顶部标签栏 */
.top-tabs-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #E4E7ED;
  padding: 12px 16px;
}
.top-tab-item {
  padding: 8px 16px;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
}
.top-tab-item:hover {
  color: var(--color-primary);
  background: #F5F7FA;
}
.top-tab-item.active {
  color: #fff;
  background: var(--color-primary);
}

/* 年级标签栏 */
.grade-tab-bar {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.grade-tab-item {
  padding: 8px 20px;
  border-radius: 20px;
  background: #f5f7fa;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.grade-tab-item:hover {
  background: #ecf5ff;
  color: #409EFF;
  border-color: #409EFF;
}
.grade-tab-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 面包屑导航 */
.breadcrumb-nav {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #E4E7ED;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 4px;
}
.breadcrumb-item {
  font-size: 14px;
  color: #909399;
  display: flex;
  align-items: center;
}
.breadcrumb-item::after {
  content: '>';
  margin: 0 8px;
  color: #C0C4CC;
}
.breadcrumb-item:last-child::after {
  content: '';
}
.breadcrumb-item:last-child {
  color: #303133;
  font-weight: 600;
}
.breadcrumb-item.clickable {
  color: #409EFF;
  cursor: pointer;
}
.breadcrumb-item.clickable:hover {
  text-decoration: underline;
}
.breadcrumb-item.active {
  color: #303133;
  font-weight: 600;
}

/* 学科标题栏 */
.subject-header-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #E4E7ED;
}
.back-link {
  font-size: 14px;
  color: #409EFF;
  cursor: pointer;
}
.back-link:hover {
  text-decoration: underline;
}
.current-subject {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 内容区块通用样式 */
.content-section {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #E4E7ED;
  padding: 16px;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.title-icon {
  font-size: 18px;
}
.title-text {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.title-badge {
  font-size: 11px;
  padding: 2px 6px;
  background: #F56C6C;
  color: #fff;
  border-radius: 4px;
}
/* 美术专区样式 */
.art-section {
  border-left: 3px solid #E91E63;
}
.art-title .title-text {
  color: #E91E63;
}
.title-badge.art {
  background: linear-gradient(135deg, #FF69B4, #E91E63);
}
/* 舞蹈专区样式 */
.dance-section {
  border-left: 3px solid #9C27B0;
}
.dance-title .title-text {
  color: #9C27B0;
}
.title-badge.dance {
  background: linear-gradient(135deg, #A18CD1, #9C27B0);
}

/* 筛选行 */
.filter-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 10px;
}
.filter-row:last-child {
  margin-bottom: 0;
}
.filter-label {
  font-size: 13px;
  color: #909399;
  width: 50px;
  flex-shrink: 0;
  padding-top: 4px;
}
.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.filter-tag {
  padding: 4px 12px;
  font-size: 13px;
  color: #606266;
  background: #F5F7FA;
  border: 1px solid #E4E7ED;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
}
.filter-tag:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.filter-tag.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
}
.filter-tag.more {
  color: #909399;
}
.tag-dot {
  width: 4px;
  height: 4px;
  background: #F56C6C;
  border-radius: 50%;
}
.tag-dot.red {
  background: #F56C6C;
}

/* 右侧内容区 */
.grade-content {
  flex: 1;
  min-width: 0;
}

/* 选择学科提示 */
.select-subject-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 8px;
  border: 1px dashed #DCDFE6;
  text-align: center;
}
.hint-icon {
  font-size: 48px;
  margin-bottom: 16px;
}
.hint-text {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
.hint-sub {
  font-size: 14px;
  color: #909399;
}

/* 分类导航栏 */
.classify-bar {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #E4E7ED;
  margin-bottom: 16px;
}

.content-toolbar {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
  background: var(--bg-card);
  padding: 14px 18px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  flex-wrap: wrap;
}
.search-box {
  display: flex;
  gap: 8px;
  align-items: center;
}
.sort-group {
  display: flex;
  gap: 2px;
}
.sort-item {
  padding: 5px 12px;
  border-radius: var(--radius-round);
  font-size: 12px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s;
  white-space: nowrap;
}
.sort-item:hover { color: var(--color-primary); }
.sort-item.active {
  background: var(--color-primary-bg);
  color: var(--color-primary);
  font-weight: 600;
}
.result-count {
  margin-left: auto;
  font-size: 13px;
  color: var(--text-secondary);
}

/* 资源列表 */
.resource-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.resource-list-item {
  display: flex;
  align-items: center;
  padding: 16px;
  text-decoration: none;
  color: var(--text-primary);
  gap: 16px;
}
.item-cover {
  width: 96px;
  height: 68px;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.item-icon { font-size: 30px; color: rgba(255,255,255,0.9); }
.badge-free {
  position: absolute;
  top: -6px;
  right: -6px;
  background: #10B981;
  color: #fff;
  padding: 1px 8px;
  border-radius: var(--radius-round);
  font-size: 10px;
  font-weight: 600;
}
.item-info { flex: 1; min-width: 0; }
.item-info h4 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.item-tags {
  display: flex;
  gap: 5px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.item-meta {
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: var(--text-secondary);
}
.item-action { flex-shrink: 0; }
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

@media (max-width: 768px) {
  .grade-nav-inner { overflow-x: auto; }
  .nav-search-box { margin-left: 0; width: 100%; }
  .nav-search-box .el-input { width: 140px !important; }
  .subject-nav-top .subject-nav-list { padding: 10px; }
  .subject-nav-top .subject-nav-item { padding: 6px 12px; font-size: 13px; }
  .grade-body { flex-direction: column; }
  .grade-sidebar { width: 100%; }
  .zone-card { min-width: 100px; }
  .content-toolbar { flex-wrap: wrap; }
  .resource-list-item { flex-direction: column; }
}
</style>
