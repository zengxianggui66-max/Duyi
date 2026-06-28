<template>
  <div class="preview-fails-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-select v-model="statusFilter" placeholder="状态" clearable size="small" style="width: 120px" @change="search">
          <el-option :value="0" label="待处理" />
          <el-option :value="1" label="已处理" />
          <el-option :value="2" label="已忽略" />
        </el-select>
        <el-input
          v-model="keyword"
          placeholder="搜索标题"
          clearable
          style="width: 200px"
          size="small"
          @clear="search"
          @keyup.enter="search"
        />
        <el-button size="small" @click="search">查询</el-button>
      </div>
      <div class="toolbar-right">
        <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="item">
          <el-tag type="warning" size="small">待处理</el-tag>
        </el-badge>
        <el-button size="small" :loading="loading" @click="loadTable">刷新</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <el-table :data="tableData" v-loading="loading" size="small" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="资源标题" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <router-link v-if="row.globalId" :to="`/admin/resource-main/${row.globalId}`" class="link">
            {{ row.title }}
          </router-link>
          <span v-else>{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sourceType" label="来源" width="120">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ row.sourceType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="failReason" label="失败原因" min-width="200" show-overflow-tooltip />
      <el-table-column prop="failCount" label="失败次数" width="90" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="row.failCount > 3 ? 'danger' : 'warning'">{{ row.failCount }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastFailTime" label="最近失败" width="170">
        <template #default="{ row }">
          {{ formatDateTime(row.lastFailTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-popconfirm title="确认标记为已处理？" @confirm="handleMarkHandled(row)">
              <template #reference>
                <el-button link type="primary" size="small">标记已处理</el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm title="确认忽略该记录？" @confirm="handleMarkIgnored(row)">
              <template #reference>
                <el-button link type="warning" size="small">忽略</el-button>
              </template>
            </el-popconfirm>
          </template>
          <template v-else>
            <span class="handled-info" v-if="row.handlerName">
              {{ row.handlerName }} {{ formatDateTime(row.handlerTime) }}
            </span>
            <span v-else class="handled-info">-</span>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        small
        @current-change="loadTable"
        @size-change="loadTable"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  listPreviewFails, markPreviewFailHandled, markPreviewFailIgnored,
  type PreviewFailItem,
} from '@/admin/api/quality'

const loading = ref(false)
const tableData = ref<PreviewFailItem[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

const statusFilter = ref<number | undefined>(0)
const keyword = ref('')
const pendingCount = ref(0)

async function loadTable() {
  loading.value = true
  try {
    const res = await listPreviewFails({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: statusFilter.value,
      keyword: keyword.value || undefined,
    })
    tableData.value = res.records ?? []
    total.value = res.total ?? 0
  } catch (e) {
    console.error('加载预览失败队列失败', e)
  } finally {
    loading.value = false
  }
}

function search() {
  pageNum.value = 1
  loadTable()
}

async function handleMarkHandled(row: PreviewFailItem) {
  try {
    await markPreviewFailHandled(row.id)
    ElMessage.success('已标记为已处理')
    loadTable()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

async function handleMarkIgnored(row: PreviewFailItem) {
  try {
    await markPreviewFailIgnored(row.id)
    ElMessage.success('已忽略')
    loadTable()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

function statusLabel(status: number): string {
  const map: Record<number, string> = { 0: '待处理', 1: '已处理', 2: '已忽略' }
  return map[status] ?? '未知'
}

function statusTagType(status: number): 'warning' | 'success' | 'info' {
  const map: Record<number, 'warning' | 'success' | 'info'> = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] ?? 'info'
}

function formatDateTime(dt?: string): string {
  if (!dt) return '-'
  const d = new Date(dt)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(loadTable)
</script>

<style scoped>
.preview-fails-page {
  padding: 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 8px;
}

.toolbar-left {
  display: flex;
  gap: 8px;
  align-items: center;
}

.toolbar-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.link {
  color: #409eff;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

.handled-info {
  font-size: 12px;
  color: #909399;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
