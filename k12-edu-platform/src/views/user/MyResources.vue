<template>
  <div class="my-resources-page">
    <div class="page-header-bar">
      <div class="container">
        <div class="header-breadcrumb">
          <span class="breadcrumb-item" @click="$router.push('/')">首页</span>
          <span class="breadcrumb-sep">/</span>
          <span class="breadcrumb-item current">我的上传</span>
        </div>
        <h1 class="page-header-title">上传管理</h1>
        <p class="page-header-subtitle">
          查看您上传的教学资源，按学段、学科与资源类型筛选；上传流程与学科列表同步备课一致
        </p>
      </div>
    </div>

    <div class="container my-resources-body">
      <aside class="section-sidebar">
        <div class="section-nav">
          <div
            v-for="section in sectionConfig"
            :key="section.key"
            class="section-nav-item"
            :class="{ active: activeSection === section.key }"
            @click="selectSection(section.key)"
          >
            <span class="section-icon">{{ section.icon }}</span>
            <span class="section-name">{{ section.name }}</span>
          </div>
        </div>

        <div class="subject-nav">
          <div class="subject-nav-title">{{ subjectNavTitle }}</div>
          <div class="subject-nav-list">
            <button
              class="subject-nav-item"
              :class="{ active: !activeSubject }"
              @click="selectSubject('')"
            >
              <span class="subject-dot" style="background: #909399"></span>
              全部
            </button>
            <button
              v-for="sub in currentSubjects"
              :key="sub.key"
              class="subject-nav-item"
              :class="{ active: activeSubject === sub.key }"
              @click="selectSubject(sub.key)"
            >
              <span class="subject-dot" :style="{ background: sub.color || '#5B7CF9' }"></span>
              {{ sub.name }}
            </button>
          </div>
        </div>
      </aside>

      <main class="resources-content" v-loading="loading">
        <div class="content-toolbar">
          <div class="toolbar-left">
            <span class="result-count">共 {{ displayTotal }} 个资源</span>
            <span v-if="!userStore.isLoggedIn" class="login-hint">（登录后加载您的上传记录）</span>
          </div>
          <div class="toolbar-right">
            <el-button type="primary" @click="handleUpload">上传资源</el-button>
            <el-dropdown @command="handleExport">
              <el-button>导出</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="all">导出全部</el-dropdown-item>
                  <el-dropdown-item command="selected">导出选中</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <div class="status-tabs-row">
          <el-radio-group
            :model-value="activeStatusFilter"
            size="small"
            @change="onStatusFilterChange"
          >
            <el-radio-button v-for="tab in statusTabs" :key="tab.key" :value="tab.key">
              {{ tab.label }}
            </el-radio-button>
          </el-radio-group>
        </div>

        <div class="resource-type-tabs">
          <span
            v-for="type in currentResourceTypes"
            :key="type.key"
            class="type-tab"
            :class="{ active: activeType === type.key }"
            @click="selectType(type.key)"
          >
            <span class="type-icon">{{ type.icon }}</span>
            {{ type.name }}
            <span class="type-count">{{ getTypeCount(type.key) }}</span>
          </span>
        </div>

        <el-empty
          v-if="!loading && listItems.length === 0"
          description="暂无资源，点击「上传资源」发布到当前学段"
          class="list-empty"
        />

        <div v-else class="resource-list">
          <div
            v-for="item in listItems"
            :key="item.id"
            class="resource-item card"
          >
            <div class="resource-info">
              <div class="resource-header">
                <span class="resource-type-badge">{{ item.typeName }}</span>
                <span v-if="item.subjectName" class="resource-subject">{{ item.subjectName }}</span>
                <span class="resource-format">{{ item.format }}</span>
                <span class="resource-grade">{{ item.grade }}</span>
                <span v-if="statusLabel(item.status, item.auditStatus, item.publishStatus)" class="resource-status">
                  {{ statusLabel(item.status, item.auditStatus, item.publishStatus) }}
                </span>
              </div>
              <h3 class="resource-title">{{ item.title }}</h3>
              <div class="resource-meta">
                <span class="meta-item">{{ item.updateTime }}</span>
                <span class="meta-item">{{ item.viewCount }} 次浏览</span>
                <span class="meta-item">{{ item.downloadCount }} 次下载</span>
                <span v-if="item.module" class="meta-item">{{ item.module }}</span>
                <span v-if="item.isPublic" class="meta-item public">已公开</span>
                <span v-else class="meta-item private">未公开</span>
              </div>
            </div>
            <div class="resource-actions">
              <!-- 草稿 (status=-1): 编辑 + 提交审核 + 删除 -->
              <template v-if="item.status === -1">
                <el-button size="small" @click="handleEdit(item)">编辑</el-button>
                <el-button size="small" type="primary" @click="handleSubmitDraft(item)">提交审核</el-button>
                <el-button size="small" type="danger" plain @click="handleDeleteDraft(item)">删除</el-button>
              </template>

              <!-- 待审核 (status=0): 查看 + 撤回 -->
              <template v-else-if="item.status === 0">
                <el-button size="small" @click="handleViewDetail(item)">查看</el-button>
                <el-popconfirm
                  title="确定撤回该资源的审核申请吗？撤回后将变为草稿状态。"
                  confirm-button-text="确认撤回"
                  cancel-button-text="取消"
                  @confirm="handleWithdraw(item)"
                >
                  <template #reference>
                    <el-button size="small" type="warning" plain>撤回</el-button>
                  </template>
                </el-popconfirm>
              </template>

              <!-- 已发布 (status=1): 查看前台 + 下载 -->
              <template v-else-if="item.status === 1">
                <el-button size="small" type="primary" @click="handleViewFront(item)">查看前台</el-button>
                <el-button size="small" @click="handleDownload(item)">下载</el-button>
              </template>

              <!-- 未通过 (status=2): 驳回原因 + 复制为草稿 -->
              <template v-else-if="item.status === 2">
                <el-popover placement="top" :width="360" trigger="click">
                  <template #reference>
                    <el-button size="small" type="danger" plain>驳回原因</el-button>
                  </template>
                  <div class="reject-detail">
                    <p class="reject-reason">{{ item.rejectReason || '管理员未填写具体原因' }}</p>
                    <p v-if="item.auditorName" class="reject-meta">审核人：{{ item.auditorName }}</p>
                    <p v-if="item.rejectedAt" class="reject-meta">驳回时间：{{ item.rejectedAt }}</p>
                  </div>
                </el-popover>
                <el-button size="small" type="primary" @click="handleReupload(item)">复制为草稿</el-button>
              </template>

              <!-- 已下架 (status=3): 查看 -->
              <template v-else-if="item.status === 3 && item.publishStatus === 0">
                <el-button size="small" @click="handleViewDetail(item)">查看</el-button>
                <span class="offline-hint">审核通过，待管理员发布</span>
              </template>
              <template v-else-if="item.status === 3">
                <el-button size="small" @click="handleViewDetail(item)">查看</el-button>
                <span class="offline-hint">内容已下架</span>
              </template>
            </div>
          </div>
        </div>

        <div v-if="displayTotal > 0" class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="displayTotal"
            layout="total, sizes, prev, pager, next, jumper"
            background
          />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useMyResources, type MyResourceStatusFilter } from '@/composables/useMyResources'

const {
  activeSection,
  activeSubject,
  activeType,
  activeStatusFilter,
  statusTabs,
  currentPage,
  pageSize,
  loading,
  sectionConfig,
  currentSubjects,
  subjectNavTitle,
  currentResourceTypes,
  listItems,
  displayTotal,
  getTypeCount,
  selectSection,
  selectSubject,
  selectType,
  selectStatusFilter,
  handleUpload,
  handleSubmitDraft,
  handleWithdraw,
  handleViewFront,
  handleViewDetail,
  handleEdit,
  handleReupload,
  handleDownload,
  handleDeleteDraft,
  handleMore,
  handleExport,
  statusLabel,
  userStore,
} = useMyResources()

function onStatusFilterChange(val: string | number | boolean | undefined) {
  if (typeof val === 'string') {
    selectStatusFilter(val as MyResourceStatusFilter)
  }
}
</script>

<style scoped>
.my-resources-page {
  min-height: 100vh;
  background: var(--bg-body);
}

.page-header-bar {
  background: linear-gradient(135deg, #5b7cf9, #8b5cf6);
  color: #fff;
  padding: 32px 0;
}
.header-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
}
.breadcrumb-item {
  opacity: 0.85;
  cursor: pointer;
}
.breadcrumb-item:hover {
  opacity: 1;
}
.breadcrumb-item.current {
  opacity: 1;
  font-weight: 500;
}
.breadcrumb-sep {
  opacity: 0.5;
}
.page-header-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
}
.page-header-subtitle {
  font-size: 14px;
  opacity: 0.85;
  margin: 0;
  max-width: 720px;
  line-height: 1.5;
}

.my-resources-body {
  display: flex;
  gap: 24px;
  padding: 24px 0;
}

.section-sidebar {
  width: 200px;
  flex-shrink: 0;
}
.section-nav {
  background: #fff;
  border-radius: 8px;
  padding: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}
.section-nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  color: var(--text-regular);
}
.section-nav-item:hover {
  background: var(--bg-body);
}
.section-nav-item.active {
  background: var(--color-primary);
  color: #fff;
}
.section-icon {
  font-size: 18px;
}
.section-name {
  font-weight: 500;
}

.subject-nav {
  margin-top: 16px;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}
.subject-nav-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
  padding-left: 4px;
}
.subject-nav-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 360px;
  overflow-y: auto;
}
.subject-nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border: none;
  background: none;
  border-radius: 4px;
  font-size: 13px;
  color: var(--text-regular);
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
}
.subject-nav-item:hover {
  background: var(--bg-body);
}
.subject-nav-item.active {
  background: var(--color-primary-bg);
  color: var(--color-primary);
}
.subject-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.resources-content {
  flex: 1;
  min-width: 0;
}

.content-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
}
.result-count {
  font-size: 14px;
  color: var(--text-secondary);
}
.login-hint {
  margin-left: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}
.toolbar-right {
  display: flex;
  gap: 12px;
}

.status-tabs-row {
  margin-bottom: 16px;
}
.resource-type-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
}
.type-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-size: 14px;
  color: var(--text-regular);
  background: var(--bg-body);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}
.type-tab:hover {
  background: var(--color-primary-bg);
  color: var(--color-primary);
}
.type-tab.active {
  background: var(--color-primary);
  color: #fff;
}
.type-icon {
  font-size: 16px;
}
.type-count {
  font-size: 12px;
  padding: 2px 6px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
}
.type-tab:not(.active) .type-count {
  background: var(--border-light);
}

.list-empty {
  padding: 48px 0;
  background: #fff;
  border-radius: 8px;
}

.resource-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.resource-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px;
  transition: box-shadow 0.2s;
}
.resource-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}
.resource-info {
  flex: 1;
  min-width: 0;
}
.resource-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.resource-type-badge {
  font-size: 12px;
  padding: 2px 8px;
  background: var(--color-primary-bg);
  color: var(--color-primary);
  border-radius: 4px;
  font-weight: 500;
}
.resource-subject {
  font-size: 12px;
  color: var(--text-secondary);
}
.resource-format {
  font-size: 12px;
  padding: 2px 8px;
  background: var(--color-success-bg);
  color: var(--color-success);
  border-radius: 4px;
}
.resource-grade,
.resource-status {
  font-size: 12px;
  color: var(--text-secondary);
}
.resource-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 12px;
}
.resource-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.meta-item {
  font-size: 13px;
  color: var(--text-secondary);
}
.meta-item.public {
  color: var(--color-success);
}
.resource-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
  margin-left: 20px;
}

.reject-detail {
  font-size: 13px;
  line-height: 1.6;
}
.reject-reason {
  color: var(--color-danger, #e74c3c);
  margin-bottom: 8px;
  padding: 8px;
  background: #fff5f5;
  border-radius: 4px;
}
.reject-meta {
  color: var(--text-secondary);
  margin: 4px 0;
  font-size: 12px;
}
.offline-hint {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

@media (max-width: 768px) {
  .my-resources-body {
    flex-direction: column;
  }
  .section-sidebar {
    width: 100%;
  }
  .section-nav {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }
  .resource-item {
    flex-direction: column;
  }
  .resource-actions {
    flex-direction: row;
    margin-left: 0;
    margin-top: 16px;
  }
}
</style>
