<template>
  <div class="theme-class-meeting-page">
    <!-- ========== 顶部导航栏 ========== -->
    <div class="top-nav">
      <div class="container">
        <div class="top-nav-inner">
          <div class="logo-area">
            <span class="logo-text">新课堂教育</span>
            <span class="slogan">让备课更轻松！</span>
          </div>
          <div class="search-area">
            <input
              v-model="searchKeyword"
              type="text"
              class="search-input"
              placeholder="搜索备课资料"
              @keyup.enter="handleSearch"
            />
            <button class="search-btn" @click="handleSearch">搜索</button>
          </div>
          <div class="nav-right">
            <span class="nav-item cart-icon">🛒 资料篮</span>
            <span class="upload-btn" @click="handleUpload">上传资料</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 主菜单 ========== -->
    <div class="main-nav">
      <div class="container">
        <ul class="main-nav-list">
          <li
            v-for="(item, idx) in menuItems"
            :key="idx"
            :class="{ active: item.active }"
            @mouseenter="activeMenuIdx = idx"
            @mouseleave="activeMenuIdx = -1"
          >
            <a class="nav-link" @click="setActiveMenu(idx)">
              {{ item.name }}
              <span v-if="item.dropdownItems" class="dropdown-arrow">▾</span>
            </a>
            <!-- 下拉菜单 -->
            <ul v-if="item.dropdownItems && activeMenuIdx === idx" :class="['dropdown', item.colClass]">
              <li v-for="(sub, subIdx) in item.dropdownItems" :key="subIdx">
                <a class="dropdown-link" @click="handleDropdownClick(sub)">{{ sub }}</a>
              </li>
            </ul>
          </li>
        </ul>
      </div>
    </div>

    <!-- ========== 面包屑 ========== -->
    <div class="breadcrumb-bar">
      <div class="container">
        <div class="breadcrumb">
          <router-link to="/" class="breadcrumb-link">首页</router-link>
          <span class="breadcrumb-sep">/</span>
          <span>主题班会</span>
        </div>
      </div>
    </div>

    <!-- ========== 主内容区 ========== -->
    <div class="container main-container">
      <div class="content-wrapper">
        <!-- ========== 左侧分类导航 ========== -->
        <aside class="left-sidebar">
          <div
            v-for="(side, idx) in sidebarItems"
            :key="idx"
            class="sidebar-item"
            :class="{ active: activeSidebar === side.name }"
            @click="handleSidebarClick(side.name)"
          >
            <i class="sidebar-icon">{{ side.icon }}</i>
            <span>{{ side.name }}</span>
          </div>
        </aside>

        <!-- ========== 右侧主内容 ========== -->
        <main class="main-content">
          <!-- Banner -->
          <div class="banner" @click="handleBannerClick">
            <div class="banner-content">
              <span class="banner-text">{{ bannerTitle }}</span>
            </div>
          </div>

          <!-- ========== 热门主题 ========== -->
          <section class="hot-section">
            <div class="section-header">
              <h3 class="section-title">🔥 热门主题</h3>
            </div>
            <div class="hot-theme-list">
              <div
                v-for="(theme, idx) in hotThemes"
                :key="idx"
                :class="['theme-card', theme.class]"
                @click="handleThemeClick(theme.name, theme.keyword)"
              >
                {{ theme.name }}
              </div>
            </div>
          </section>

          <!-- ========== 热门下载 ========== -->
          <section class="hot-section">
            <div class="section-header">
              <h3 class="section-title">📥 热门下载</h3>
              <div class="download-tabs">
                <a
                  v-for="tab in downloadTabs"
                  :key="tab.key"
                  :class="['tab-link', { active: currentDownloadTab === tab.key }]"
                  @click="currentDownloadTab = tab.key"
                >
                  {{ tab.name }}
                </a>
              </div>
            </div>
            <div class="download-list">
              <div
                v-for="(card, idx) in currentDownloadList"
                :key="idx"
                class="download-card"
                @click="handleDownloadCard(card)"
              >
                <div class="download-img" :style="{ background: getCardBg(idx) }">
                  <span class="img-text">{{ card.imgText }}</span>
                </div>
                <div class="download-info">
                  <p class="download-title">{{ card.title }}</p>
                  <div class="download-meta">
                    <span class="meta-item">⏱ {{ card.duration }}</span>
                    <span class="meta-item">↓ {{ card.downloads }}</span>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </main>
      </div>

      <!-- ========== 最新上传模块 ========== -->
      <section class="latest-section">
        <div class="section-title-bar">
          <div class="section-title">🆕 最新上传</div>
          <div class="section-tabs">
            <a
              v-for="tab in latestTabs"
              :key="tab.key"
              :class="['tab-link', { active: currentLatestTab === tab.key }]"
              @click="currentLatestTab = tab.key"
            >
              {{ tab.name }}
            </a>
          </div>
        </div>
        <div class="upload-list">
          <div
            v-for="(item, idx) in currentLatestList"
            :key="idx"
            class="upload-item"
          >
            <a class="upload-link" @click="handleResourceClick(item)">{{ item.title }}</a>
            <span class="upload-date">{{ item.date }}</span>
          </div>
        </div>
      </section>

      <!-- ========== 班会主题导航 ========== -->
      <section class="theme-nav-section">
        <div class="theme-nav-title">📚 班会主题导航</div>
        <div class="theme-grid">
          <div v-for="(group, idx) in themeGroups" :key="idx" class="theme-column">
            <h4 class="theme-column-title">{{ group.title }}</h4>
            <a
              v-for="(link, lidx) in group.links"
              :key="lidx"
              class="theme-link"
              @click="handleTopicLink(link)"
            >
              {{ link }}
            </a>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useThemeClassMeeting } from '@/composables/useThemeClassMeeting'

const {
  searchKeyword,
  activeMenuIdx,
  menuItems,
  sidebarItems,
  activeSidebar,
  hotThemes,
  bannerTitle,
  downloadTabs,
  currentDownloadTab,
  currentDownloadList,
  latestTabs,
  currentLatestTab,
  currentLatestList,
  themeGroups,
  setActiveMenu,
  handleSearch,
  handleDropdownClick,
  handleSidebarClick,
  handleThemeClick,
  handleBannerClick,
  handleDownloadCard,
  handleResourceClick,
  handleTopicLink,
  getCardBg,
} = useThemeClassMeeting()

function handleUpload() {
  /* 上传入口待接资源中心 */
}
</script>

<style scoped>
/* ===================== 页面基础 ===================== */
.theme-class-meeting-page {
  background-color: #f5f5f5;
  min-height: 100vh;
}

.container {
  width: 1200px;
  margin: 0 auto;
}

/* ========== 顶部导航栏 ========== */
.top-nav {
  background: #fff;
  border-bottom: 1px solid #eee;
  padding: 12px 0;
}

.top-nav-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo-area {
  display: flex;
  align-items: center;
}

.logo-text {
  font-weight: bold;
  color: #409eff;
  font-size: 18px;
  margin-right: 8px;
}

.slogan {
  font-size: 14px;
  color: #666;
}

.search-area {
  display: flex;
  align-items: center;
}

.search-input {
  width: 400px;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #ccc;
  border-radius: 4px 0 0 4px;
  outline: none;
  font-size: 14px;
}

.search-input:focus {
  border-color: #409eff;
}

.search-btn {
  height: 36px;
  padding: 0 20px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 0 4px 4px 0;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}

.search-btn:hover {
  background: #337ecc;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 14px;
  color: #666;
}

.nav-item {
  cursor: pointer;
}

.nav-item:hover {
  color: #409eff;
}

.cart-icon {
  margin-right: 8px;
}

.upload-btn {
  background: #ff7d00;
  color: #fff;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}

.upload-btn:hover {
  background: #e66f00;
}

/* ========== 主菜单 ========== */
.main-nav {
  background: #409eff;
  position: relative;
  z-index: 999;
}

.main-nav-list {
  list-style: none;
  display: flex;
  margin: 0;
  padding: 0;
  position: relative;
}

.main-nav-list li {
  position: relative;
  padding: 0 22px;
  line-height: 44px;
}

.main-nav-list li:hover,
.main-nav-list li.active {
  background: #337ecc;
}

.nav-link {
  color: #fff;
  text-decoration: none;
  font-size: 14px;
  display: block;
  cursor: pointer;
}

.dropdown-arrow {
  margin-left: 4px;
  font-size: 12px;
}

/* 下拉菜单 */
.dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  background: #fff;
  min-width: 200px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  border: 1px solid #eee;
  z-index: 1000;
  padding: 8px 0;
  list-style: none;
  margin: 0;
}

.dropdown li {
  padding: 0;
  line-height: 36px;
  list-style: none;
}

.dropdown li:hover {
  background: #f0f7ff;
}

.dropdown-link {
  display: block;
  color: #333;
  padding: 0 15px;
  font-size: 13px;
  text-decoration: none;
  cursor: pointer;
}

.dropdown-link:hover {
  color: #409eff;
}

/* 双列下拉 */
.dropdown.double-col {
  display: none;
  grid-template-columns: 1fr 1fr;
  min-width: 320px;
  padding: 12px;
  gap: 4px 0;
}

.dropdown.double-col li {
  line-height: 32px;
}

/* 三列下拉 */
.dropdown.triple-col {
  display: none;
  grid-template-columns: 1fr 1fr 1fr;
  min-width: 480px;
  padding: 12px;
  gap: 4px 0;
}

.dropdown.triple-col li {
  line-height: 32px;
}

.main-nav-list li:hover .dropdown {
  display: block;
}

.main-nav-list li:hover .dropdown.double-col,
.main-nav-list li:hover .dropdown.triple-col {
  display: grid;
}

/* ========== 面包屑 ========== */
.breadcrumb-bar {
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
  padding: 10px 0;
}

.breadcrumb {
  font-size: 13px;
  color: #666;
}

.breadcrumb-link {
  color: #409eff;
  text-decoration: none;
}

.breadcrumb-link:hover {
  text-decoration: underline;
}

.breadcrumb-sep {
  margin: 0 8px;
}

/* ========== 主容器 ========== */
.main-container {
  padding: 20px 0 60px;
}

.content-wrapper {
  display: flex;
  gap: 0;
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
}

/* ========== 左侧分类导航 ========== */
.left-sidebar {
  width: 180px;
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid #eee;
  padding: 16px 0;
}

.sidebar-item {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: all 0.2s;
}

.sidebar-item:hover {
  background: #f0f7ff;
  color: #409eff;
}

.sidebar-item.active {
  background: #e6f3ff;
  color: #409eff;
  border-right: 3px solid #409eff;
}

.sidebar-icon {
  margin-right: 8px;
  font-style: normal;
}

/* ========== 右侧主内容 ========== */
.main-content {
  flex: 1;
  padding: 16px;
  min-width: 0;
}

/* Banner */
.banner {
  height: 260px;
  background: linear-gradient(135deg, #e6f4ea 0%, #d4edda 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  border-radius: 4px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.banner::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background:
    radial-gradient(circle at 20% 50%, rgba(64, 158, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 50%, rgba(64, 158, 255, 0.1) 0%, transparent 50%);
}

.banner-content {
  position: relative;
  z-index: 1;
}

.banner-text {
  font-size: 36px;
  color: #333;
  font-weight: bold;
  text-shadow: 0 2px 4px rgba(255, 255, 255, 0.8);
}

/* ========== 热门主题 ========== */
.hot-section {
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.download-tabs {
  display: flex;
  gap: 4px;
}

.tab-link {
  font-size: 14px;
  color: #666;
  text-decoration: none;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-link:hover {
  color: #409eff;
  background: #f0f7ff;
}

.tab-link.active {
  color: #409eff;
  font-weight: bold;
  background: #e6f3ff;
}

.hot-theme-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.theme-card {
  height: 160px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  text-align: center;
  padding: 20px;
}

.theme-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.card-1 {
  background: linear-gradient(135deg, #d92b2b 0%, #f2994a 100%);
}

.card-2 {
  background: linear-gradient(135deg, #333 0%, #666 100%);
}

.card-3 {
  background: linear-gradient(135deg, #a8e063 0%, #56ab2f 100%);
}

.card-4 {
  background: linear-gradient(135deg, #87ceeb 0%, #4facfe 100%);
}

/* ========== 热门下载 ========== */
.download-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.download-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
}

.download-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.download-img {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 8px;
}

.img-text {
  font-size: 14px;
  color: #fff;
  font-weight: bold;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.download-info {
  padding: 10px;
}

.download-title {
  font-size: 13px;
  line-height: 1.4;
  color: #333;
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.download-meta {
  display: flex;
  justify-content: space-between;
  color: #999;
  font-size: 12px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

/* ========== 最新上传模块 ========== */
.latest-section {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  margin-top: 20px;
}

.section-title-bar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.section-tabs {
  margin-left: 20px;
}

.upload-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 20px;
}

.upload-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  padding: 6px 0;
  border-bottom: 1px solid #f5f5f5;
}

.upload-item:last-child {
  border-bottom: none;
}

.upload-link {
  color: #333;
  text-decoration: none;
  cursor: pointer;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding-right: 16px;
}

.upload-link:hover {
  color: #409eff;
}

.upload-date {
  color: #999;
  font-size: 12px;
  flex-shrink: 0;
}

/* ========== 班会主题导航 ========== */
.theme-nav-section {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  margin-top: 20px;
}

.theme-nav-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.theme-grid {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  gap: 10px;
}

.theme-column {
  font-size: 14px;
  line-height: 2;
}

.theme-column-title {
  color: #409eff;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: bold;
}

.theme-link {
  display: block;
  color: #666;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.2s;
}

.theme-link:hover {
  color: #409eff;
}

/* ========== 响应式 ========== */
@media (max-width: 1200px) {
  .container {
    width: 100%;
    padding: 0 20px;
  }

  .search-input {
    width: 280px;
  }

  .hot-theme-list,
  .download-list {
    grid-template-columns: repeat(2, 1fr);
  }

  .theme-grid {
    grid-template-columns: repeat(5, 1fr);
  }
}

@media (max-width: 900px) {
  .content-wrapper {
    flex-direction: column;
  }

  .left-sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #eee;
    display: flex;
    flex-wrap: wrap;
    padding: 8px;
    gap: 4px;
  }

  .sidebar-item {
    padding: 8px 12px;
    border-radius: 4px;
  }

  .sidebar-item.active {
    border-right: none;
    background: #e6f3ff;
  }

  .theme-grid {
    grid-template-columns: repeat(4, 1fr);
  }

  .upload-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .top-nav-inner {
    flex-wrap: wrap;
    gap: 12px;
  }

  .search-area {
    order: 3;
    width: 100%;
  }

  .search-input {
    width: 100%;
  }

  .banner {
    height: 180px;
  }

  .banner-text {
    font-size: 24px;
  }

  .hot-theme-list,
  .download-list {
    grid-template-columns: 1fr;
  }

  .theme-card {
    height: 100px;
    font-size: 16px;
  }

  .theme-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .main-nav-list {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .main-nav-list li {
    padding: 0 16px;
    white-space: nowrap;
  }
}
</style>
