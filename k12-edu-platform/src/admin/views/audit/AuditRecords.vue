<template>
  <AdminPageShell title="审核记录" desc="资源审核流水，可按资源、审核员、动作、时间范围筛选">
    <div class="audit-records__toolbar">
      <el-input
        v-model="keyword"
        placeholder="资源标题 / 驳回原因"
        clearable
        style="width: 200px"
        @keyup.enter="reload"
      />
      <el-input
        v-model="resourceIdInput"
        placeholder="资源ID"
        clearable
        style="width: 120px"
        @keyup.enter="reload"
      />
      <el-input
        v-model="auditorNameInput"
        placeholder="审核员"
        clearable
        style="width: 120px"
        @keyup.enter="reload"
      />
      <el-select v-model="action" placeholder="动作" clearable style="width: 140px">
        <el-option label="审核通过(自动上架)" value="approve_publish" />
        <el-option label="审核通过(待发布)" value="approve_audit" />
        <el-option label="审核通过" value="approve" />
        <el-option label="驳回" value="reject" />
      </el-select>
      <el-date-picker
        v-model="timeRange"
        type="datetimerange"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        value-format="YYYY-MM-DD HH:mm:ss"
        style="width: 340px"
      />
      <el-button type="primary" @click="reload">查询</el-button>
      <el-button text @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="records" border stripe>
      <el-table-column prop="createdAt" label="审核时间" width="170" />
      <el-table-column prop="resourceId" label="资源ID" width="90" />
      <el-table-column prop="resourceTitle" label="资源标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="stage" label="学段" width="80" />
      <el-table-column prop="subject" label="学科" width="90" />
      <el-table-column label="动作" width="120">
        <template #default="{ row }">
          <el-tag :type="actionTagType(row.action)" size="small">
            {{ actionLabel(row.action) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态变更" width="160">
        <template #default="{ row }">
          {{ row.beforeStatusLabel }} → {{ row.afterStatusLabel }}
        </template>
      </el-table-column>
      <el-table-column prop="auditorName" label="审核人" width="100" />
      <el-table-column prop="reason" label="原因/备注" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="goDetail(row.resourceId)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="audit-records__pager">
      <el-pagination
        v-model:current-page="current"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </div>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AdminPageShell from '@/admin/components/AdminPageShell.vue'
import { listAuditLogs, type AuditLogItem } from '@/admin/api/audit'

const router = useRouter()
const loading = ref(false)
const records = ref<AuditLogItem[]>([])
const keyword = ref('')
const resourceIdInput = ref('')
const auditorNameInput = ref('')
const action = ref('')
const timeRange = ref<[string, string] | null>(null)
const current = ref(1)
const size = ref(20)
const total = ref(0)

function actionTagType(act: string) {
  if (act === 'approve_publish' || act === 'approve_audit' || act === 'approve') return 'success'
  if (act === 'reject') return 'danger'
  return 'info'
}

function actionLabel(act: string) {
  const map: Record<string, string> = {
    approve_publish: '通过·上架',
    approve_audit: '通过·待发布',
    approve: '审核通过',
    reject: '驳回',
  }
  return map[act] || act
}

async function load() {
  loading.value = true
  try {
    const resourceId = resourceIdInput.value.trim()
      ? Number(resourceIdInput.value.trim())
      : undefined
    const page = await listAuditLogs({
      current: current.value,
      size: size.value,
      keyword: keyword.value || undefined,
      resourceId: Number.isFinite(resourceId) ? resourceId : undefined,
      action: action.value || undefined,
      startTime: timeRange.value?.[0] || undefined,
      endTime: timeRange.value?.[1] || undefined,
    })
    records.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

function reload() {
  current.value = 1
  void load()
}

function reset() {
  keyword.value = ''
  resourceIdInput.value = ''
  auditorNameInput.value = ''
  action.value = ''
  timeRange.value = null
  reload()
}

function goDetail(id: number) {
  router.push(`/admin/resource-main/${id}`)
}

onMounted(load)
</script>

<style scoped>
.audit-records__toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.audit-records__pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
