<template>
  <AdminPageShell title="用户详情" desc="基本资料 · OAuth · 上传收藏 · 行为流水（只读）">
    <div class="detail-toolbar">
      <el-button @click="$router.back()">返回列表</el-button>
      <el-button
        v-if="detail && detail.status === 1"
        v-permission="'admin:user:edit'"
        type="warning"
        @click="onDisable"
      >
        禁用
      </el-button>
      <el-button
        v-else-if="detail"
        v-permission="'admin:user:edit'"
        type="success"
        @click="onEnable"
      >
        启用
      </el-button>
    </div>

    <el-skeleton v-if="loading" :rows="8" animated />

    <template v-else-if="detail">
      <div class="detail-summary">
        <el-avatar :size="56" :src="detail.avatar">{{ avatarFallback }}</el-avatar>
        <div class="detail-summary__main">
          <h2>{{ detail.nickname || detail.username }}</h2>
          <p class="detail-summary__meta">
            @{{ detail.username }}
            · {{ detail.portalRoleName || detail.portalRole }}
            · {{ detail.status === 1 ? '正常' : '禁用' }}
          </p>
          <p v-if="stats" class="detail-summary__stats">
            上传 {{ stats.uploadCount }} · 收藏 {{ stats.collectionCount }}
            <template v-if="hasBehaviorPerm">
              · 浏览 {{ stats.viewCount ?? 0 }} · 下载 {{ stats.downloadCount ?? 0 }}
              · 搜索 {{ stats.searchCount ?? 0 }} · 登录 {{ stats.loginCount ?? 0 }}
            </template>
          </p>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane label="基本资料" name="profile">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户 ID">{{ detail.id }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ detail.username }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ detail.nickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ detail.phoneMasked || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ detail.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="身份">
              {{ detail.portalRoleName || detail.portalRole }}
            </el-descriptions-item>
            <el-descriptions-item label="注册方式">
              {{ REGISTER_FROM_LABEL[detail.registerFrom || ''] || detail.registerFrom || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="性别">
              {{ genderLabel(detail.gender) }}
            </el-descriptions-item>
            <el-descriptions-item label="生日">{{ detail.birthday || '-' }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ detail.createTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="最近登录">{{ detail.lastLoginTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ detail.updateTime || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="OAuth 绑定" name="oauth">
          <el-table :data="detail.oauthBinds || []" border stripe empty-text="暂无第三方绑定">
            <el-table-column label="平台" width="120">
              <template #default="{ row }">{{ row.oauthTypeName || row.oauthType }}</template>
            </el-table-column>
            <el-table-column prop="oauthId" label="OpenID" min-width="180" show-overflow-tooltip />
            <el-table-column prop="nickname" label="昵称" width="140" />
            <el-table-column label="头像" width="80">
              <template #default="{ row }">
                <el-avatar v-if="row.avatar" :size="32" :src="row.avatar" />
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="bindTime" label="绑定时间" width="170" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="上传资源" name="uploads">
          <el-table v-loading="uploadLoading" :data="uploads" border stripe empty-text="暂无上传">
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="stage" label="学段" width="90" />
            <el-table-column prop="subject" label="学科" width="90" />
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.statusLabel || row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="uploadTime" label="上传时间" width="170" />
          </el-table>
          <div class="detail-pager">
            <el-pagination
              v-model:current-page="uploadPage"
              v-model:page-size="uploadSize"
              :total="uploadTotal"
              layout="total, prev, pager, next"
              @current-change="loadUploads"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="收藏" name="collections">
          <el-table v-loading="collectLoading" :data="collections" border stripe empty-text="暂无收藏">
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="stage" label="学段" width="90" />
            <el-table-column prop="subject" label="学科" width="90" />
            <el-table-column prop="module" label="模块" width="120" show-overflow-tooltip />
            <el-table-column prop="collectTime" label="收藏时间" width="170" />
          </el-table>
          <div class="detail-pager">
            <el-pagination
              v-model:current-page="collectPage"
              v-model:page-size="collectSize"
              :total="collectTotal"
              layout="total, prev, pager, next"
              @current-change="loadCollections"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane v-if="hasBehaviorPerm" label="浏览" name="views">
          <el-table v-loading="viewLoading" :data="viewRows" border stripe empty-text="暂无浏览记录">
            <el-table-column prop="title" label="资源标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="resourceType" label="类型" width="100" />
            <el-table-column prop="ip" label="IP" width="130" />
            <el-table-column prop="createTime" label="时间" width="170" />
          </el-table>
          <div class="detail-pager">
            <el-pagination
              v-model:current-page="viewPage"
              v-model:page-size="viewSize"
              :total="viewTotal"
              layout="total, prev, pager, next"
              @current-change="loadViews"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane v-if="hasBehaviorPerm" label="下载" name="downloads">
          <el-table v-loading="downloadLoading" :data="downloadRows" border stripe empty-text="暂无下载记录">
            <el-table-column prop="title" label="资源标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="resourceType" label="类型" width="100" />
            <el-table-column prop="ip" label="IP" width="130" />
            <el-table-column prop="createTime" label="时间" width="170" />
          </el-table>
          <div class="detail-pager">
            <el-pagination
              v-model:current-page="downloadPage"
              v-model:page-size="downloadSize"
              :total="downloadTotal"
              layout="total, prev, pager, next"
              @current-change="loadDownloads"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane v-if="hasBehaviorPerm" label="搜索" name="searches">
          <el-table v-loading="searchLoading" :data="searchRows" border stripe empty-text="暂无搜索记录">
            <el-table-column prop="keyword" label="关键词" min-width="160" show-overflow-tooltip />
            <el-table-column prop="hitCount" label="命中" width="80" />
            <el-table-column prop="sourceApi" label="来源" width="140" show-overflow-tooltip />
            <el-table-column prop="createTime" label="时间" width="170" />
          </el-table>
          <div class="detail-pager">
            <el-pagination
              v-model:current-page="searchPage"
              v-model:page-size="searchSize"
              :total="searchTotal"
              layout="total, prev, pager, next"
              @current-change="loadSearches"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane v-if="hasBehaviorPerm" label="登录" name="logins">
          <el-table v-loading="loginLoading" :data="loginRows" border stripe empty-text="暂无登录记录">
            <el-table-column label="方式" width="100">
              <template #default="{ row }">{{ loginTypeLabel(row.loginType) }}</template>
            </el-table-column>
            <el-table-column label="结果" width="80">
              <template #default="{ row }">
                <el-tag :type="row.success === 1 ? 'success' : 'danger'" size="small">
                  {{ row.success === 1 ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="ip" label="IP" width="130" />
            <el-table-column prop="failReason" label="失败原因" min-width="140" show-overflow-tooltip />
            <el-table-column prop="createTime" label="时间" width="170" />
          </el-table>
          <div class="detail-pager">
            <el-pagination
              v-model:current-page="loginPage"
              v-model:page-size="loginSize"
              :total="loginTotal"
              layout="total, prev, pager, next"
              @current-change="loadLogins"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="运营备注" name="remarks">
          <div v-if="hasRemarkPerm" class="remark-form">
            <el-input
              v-model="remarkDraft"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="客服跟进备注（500 字内）"
            />
            <el-button type="primary" :loading="remarkSaving" style="margin-top: 8px" @click="submitRemark">
              添加备注
            </el-button>
          </div>
          <el-table v-loading="remarkLoading" :data="remarkRows" border stripe empty-text="暂无备注" style="margin-top: 12px">
            <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
            <el-table-column prop="adminUsername" label="操作人" width="120" />
            <el-table-column prop="createTime" label="时间" width="170" />
          </el-table>
          <div class="detail-pager">
            <el-pagination
              v-model:current-page="remarkPage"
              v-model:page-size="remarkSize"
              :total="remarkTotal"
              layout="total, prev, pager, next"
              @current-change="loadRemarks"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="管理操作" name="oplogs">
          <el-table v-loading="opLogLoading" :data="opLogRows" border stripe empty-text="暂无管理操作记录">
            <el-table-column prop="username" label="操作人" width="120" />
            <el-table-column prop="action" label="动作" width="120" />
            <el-table-column prop="requestUri" label="接口" min-width="200" show-overflow-tooltip />
            <el-table-column label="结果" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="170" />
          </el-table>
          <div class="detail-pager">
            <el-pagination
              v-model:current-page="opLogPage"
              v-model:page-size="opLogSize"
              :total="opLogTotal"
              layout="total, prev, pager, next"
              @current-change="loadOpLogs"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>

    <el-empty v-else description="用户不存在或无权查看" />
  </AdminPageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminPageShell from '@/admin/components/AdminPageShell.vue'
import { useAdminAuthStore } from '@/admin/store/adminAuth'
import type { CollectItem } from '@/api/collect'
import {
  REGISTER_FROM_LABEL,
  disableAdminUser,
  enableAdminUser,
  getAdminUserDetail,
  getAdminUserStats,
  listUserActions,
  listUserCollections,
  listUserLoginLogs,
  listUserRemarks,
  listUserOperationLogs,
  addUserRemark,
  listUserUploads,
  type AdminUserAction,
  type AdminUserDetail,
  type AdminUserLoginLog,
  type AdminUserOperationLog,
  type AdminUserRemark,
  type AdminUserStats,
  type AdminUserUpload,
} from '@/admin/api/users'

const route = useRoute()
const adminStore = useAdminAuthStore()
const userId = computed(() => Number(route.params.id))
const hasBehaviorPerm = computed(() => adminStore.hasPermission('admin:user:view_behavior'))
const hasRemarkPerm = computed(() => adminStore.hasPermission('admin:user:remark'))

const loading = ref(true)
const detail = ref<AdminUserDetail | null>(null)
const stats = ref<AdminUserStats | null>(null)
const activeTab = ref('profile')

const uploadLoading = ref(false)
const uploads = ref<AdminUserUpload[]>([])
const uploadPage = ref(1)
const uploadSize = ref(10)
const uploadTotal = ref(0)

const collectLoading = ref(false)
const collections = ref<CollectItem[]>([])
const collectPage = ref(1)
const collectSize = ref(10)
const collectTotal = ref(0)

const viewLoading = ref(false)
const viewRows = ref<AdminUserAction[]>([])
const viewPage = ref(1)
const viewSize = ref(10)
const viewTotal = ref(0)

const downloadLoading = ref(false)
const downloadRows = ref<AdminUserAction[]>([])
const downloadPage = ref(1)
const downloadSize = ref(10)
const downloadTotal = ref(0)

const searchLoading = ref(false)
const searchRows = ref<AdminUserAction[]>([])
const searchPage = ref(1)
const searchSize = ref(10)
const searchTotal = ref(0)

const loginLoading = ref(false)
const loginRows = ref<AdminUserLoginLog[]>([])
const loginPage = ref(1)
const loginSize = ref(10)
const loginTotal = ref(0)

const remarkLoading = ref(false)
const remarkRows = ref<AdminUserRemark[]>([])
const remarkPage = ref(1)
const remarkSize = ref(10)
const remarkTotal = ref(0)
const remarkDraft = ref('')
const remarkSaving = ref(false)

const opLogLoading = ref(false)
const opLogRows = ref<AdminUserOperationLog[]>([])
const opLogPage = ref(1)
const opLogSize = ref(10)
const opLogTotal = ref(0)

const LOGIN_TYPE_LABEL: Record<string, string> = {
  password: '密码',
  sms: '短信',
  oauth: '第三方',
  admin: '后台',
}

function loginTypeLabel(t?: string) {
  return (t && LOGIN_TYPE_LABEL[t]) || t || '-'
}

const avatarFallback = computed(() => {
  const name = detail.value?.nickname || detail.value?.username || '?'
  return name.slice(0, 1).toUpperCase()
})

function genderLabel(g?: number) {
  if (g === 1) return '男'
  if (g === 2) return '女'
  return '未知'
}

async function loadDetail() {
  loading.value = true
  try {
    const [d, s] = await Promise.all([
      getAdminUserDetail(userId.value),
      getAdminUserStats(userId.value).catch(() => null),
    ])
    detail.value = d
    stats.value = s
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

async function loadUploads(page = uploadPage.value) {
  uploadLoading.value = true
  uploadPage.value = page
  try {
    const data = await listUserUploads(userId.value, uploadPage.value, uploadSize.value)
    uploads.value = data.records
    uploadTotal.value = data.total
  } finally {
    uploadLoading.value = false
  }
}

async function loadCollections(page = collectPage.value) {
  collectLoading.value = true
  collectPage.value = page
  try {
    const data = await listUserCollections(userId.value, collectPage.value, collectSize.value)
    collections.value = data.records
    collectTotal.value = data.total
  } finally {
    collectLoading.value = false
  }
}

async function loadViews(page = viewPage.value) {
  viewLoading.value = true
  viewPage.value = page
  try {
    const data = await listUserActions(userId.value, 'view', viewPage.value, viewSize.value)
    viewRows.value = data.records
    viewTotal.value = data.total
  } finally {
    viewLoading.value = false
  }
}

async function loadDownloads(page = downloadPage.value) {
  downloadLoading.value = true
  downloadPage.value = page
  try {
    const data = await listUserActions(userId.value, 'download', downloadPage.value, downloadSize.value)
    downloadRows.value = data.records
    downloadTotal.value = data.total
  } finally {
    downloadLoading.value = false
  }
}

async function loadSearches(page = searchPage.value) {
  searchLoading.value = true
  searchPage.value = page
  try {
    const data = await listUserActions(userId.value, 'search', searchPage.value, searchSize.value)
    searchRows.value = data.records
    searchTotal.value = data.total
  } finally {
    searchLoading.value = false
  }
}

async function loadLogins(page = loginPage.value) {
  loginLoading.value = true
  loginPage.value = page
  try {
    const data = await listUserLoginLogs(userId.value, loginPage.value, loginSize.value)
    loginRows.value = data.records
    loginTotal.value = data.total
  } finally {
    loginLoading.value = false
  }
}

async function loadRemarks(page = remarkPage.value) {
  remarkLoading.value = true
  remarkPage.value = page
  try {
    const data = await listUserRemarks(userId.value, remarkPage.value, remarkSize.value)
    remarkRows.value = data.records
    remarkTotal.value = data.total
  } finally {
    remarkLoading.value = false
  }
}

async function submitRemark() {
  const text = remarkDraft.value.trim()
  if (!text) {
    ElMessage.warning('请输入备注内容')
    return
  }
  remarkSaving.value = true
  try {
    await addUserRemark(userId.value, text)
    remarkDraft.value = ''
    ElMessage.success('备注已添加')
    await loadRemarks(1)
  } finally {
    remarkSaving.value = false
  }
}

async function loadOpLogs(page = opLogPage.value) {
  opLogLoading.value = true
  opLogPage.value = page
  try {
    const data = await listUserOperationLogs(userId.value, opLogPage.value, opLogSize.value)
    opLogRows.value = data.records
    opLogTotal.value = data.total
  } finally {
    opLogLoading.value = false
  }
}

async function onDisable() {
  await ElMessageBox.confirm('确定禁用该用户？', '提示', { type: 'warning' })
  await disableAdminUser(userId.value)
  ElMessage.success('已禁用')
  await loadDetail()
}

async function onEnable() {
  await ElMessageBox.confirm('确定启用该用户？', '提示', { type: 'warning' })
  await enableAdminUser(userId.value)
  ElMessage.success('已启用')
  await loadDetail()
}

watch(activeTab, (tab) => {
  if (tab === 'uploads' && !uploads.value.length && uploadTotal.value === 0) {
    loadUploads(1)
  }
  if (tab === 'collections' && !collections.value.length && collectTotal.value === 0) {
    loadCollections(1)
  }
  if (tab === 'views' && !viewRows.value.length && viewTotal.value === 0) {
    loadViews(1)
  }
  if (tab === 'downloads' && !downloadRows.value.length && downloadTotal.value === 0) {
    loadDownloads(1)
  }
  if (tab === 'searches' && !searchRows.value.length && searchTotal.value === 0) {
    loadSearches(1)
  }
  if (tab === 'logins' && !loginRows.value.length && loginTotal.value === 0) {
    loadLogins(1)
  }
  if (tab === 'remarks' && !remarkRows.value.length && remarkTotal.value === 0) {
    loadRemarks(1)
  }
  if (tab === 'oplogs' && !opLogRows.value.length && opLogTotal.value === 0) {
    loadOpLogs(1)
  }
})

watch(userId, () => {
  activeTab.value = 'profile'
  uploads.value = []
  collections.value = []
  viewRows.value = []
  downloadRows.value = []
  searchRows.value = []
  loginRows.value = []
  remarkRows.value = []
  opLogRows.value = []
  uploadTotal.value = 0
  collectTotal.value = 0
  viewTotal.value = 0
  downloadTotal.value = 0
  searchTotal.value = 0
  loginTotal.value = 0
  remarkTotal.value = 0
  opLogTotal.value = 0
  remarkDraft.value = ''
  loadDetail()
})

onMounted(() => loadDetail())
</script>

<style scoped>
.detail-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.detail-summary {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}
.detail-summary__main h2 {
  margin: 0 0 4px;
  font-size: 20px;
}
.detail-summary__meta,
.detail-summary__stats {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.detail-summary__stats {
  margin-top: 6px;
}
.detail-tabs {
  margin-top: 8px;
}
.detail-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
.remark-form {
  max-width: 640px;
}
</style>
