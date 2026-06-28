<template>
  <AdminPageShell title="平台用户" desc="教师 / 学生 / 家长等 C 端注册用户（不含后台 staff）">
    <div class="user-toolbar">
      <el-input
        v-model="keyword"
        placeholder="用户名 / 昵称 / 手机号"
        clearable
        style="width: 220px"
        @keyup.enter="load(1)"
      />
      <el-select v-model="portalRole" placeholder="身份" clearable style="width: 120px" @change="load(1)">
        <el-option v-for="o in PORTAL_ROLE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-select v-model="status" placeholder="状态" clearable style="width: 100px" @change="load(1)">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
      <el-button v-permission="'admin:user:export'" @click="onExport">导出 CSV</el-button>
      <el-button
        v-permission="'admin:user:edit'"
        type="warning"
        :disabled="!selectedIds.length"
        @click="batchDisable"
      >
        批量禁用
      </el-button>
      <el-button
        v-permission="'admin:user:edit'"
        type="success"
        :disabled="!selectedIds.length"
        @click="batchEnable"
      >
        批量启用
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="rows"
      border
      stripe
      empty-text="暂无用户"
      @selection-change="onSelectionChange"
    >
      <el-table-column v-permission="'admin:user:edit'" type="selection" width="48" />
      <el-table-column prop="username" label="用户名" width="140" show-overflow-tooltip />
      <el-table-column prop="nickname" label="昵称" width="120" show-overflow-tooltip />
      <el-table-column prop="phoneMasked" label="手机号" width="120" />
      <el-table-column label="身份" width="90">
        <template #default="{ row }">{{ row.portalRoleName || row.portalRole }}</template>
      </el-table-column>
      <el-table-column label="注册方式" width="100">
        <template #default="{ row }">{{ REGISTER_FROM_LABEL[row.registerFrom || ''] || row.registerFrom }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column prop="lastLoginTime" label="最近登录" width="170" />
      <el-table-column label="操作" min-width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="goDetail(row.id)">详情</el-button>
          <el-button
            v-permission="'admin:user:edit'"
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="user-pager">
      <el-pagination
        v-model:current-page="current"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="load(1)"
      />
    </div>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminPageShell from '@/admin/components/AdminPageShell.vue'
import {
  PORTAL_ROLE_OPTIONS,
  REGISTER_FROM_LABEL,
  batchUpdateUserStatus,
  disableAdminUser,
  enableAdminUser,
  exportAdminUsers,
  listAdminUsers,
  type AdminUserRow,
} from '@/admin/api/users'

const router = useRouter()
const loading = ref(false)
const rows = ref<AdminUserRow[]>([])
const selectedIds = ref<number[]>([])
const keyword = ref('')
const portalRole = ref<string>()
const status = ref<number>()
const current = ref(1)
const size = ref(20)
const total = ref(0)

async function load(page = current.value) {
  loading.value = true
  current.value = page
  try {
    const data = await listAdminUsers({
      current: current.value,
      size: size.value,
      keyword: keyword.value || undefined,
      portalRole: portalRole.value,
      status: status.value,
      staffOnly: false,
    })
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function goDetail(id: number) {
  router.push(`/admin/users/${id}`)
}

function onSelectionChange(selection: AdminUserRow[]) {
  selectedIds.value = selection.map((r) => r.id)
}

async function onExport() {
  await exportAdminUsers({
    keyword: keyword.value || undefined,
    portalRole: portalRole.value,
    status: status.value,
    staffOnly: false,
  })
  ElMessage.success('导出已开始下载')
}

async function batchDisable() {
  await ElMessageBox.confirm(`确定禁用选中的 ${selectedIds.value.length} 个用户？`, '批量禁用', {
    type: 'warning',
  })
  const n = await batchUpdateUserStatus(selectedIds.value, 0)
  ElMessage.success(`已处理 ${n} 个用户`)
  selectedIds.value = []
  await load()
}

async function batchEnable() {
  await ElMessageBox.confirm(`确定启用选中的 ${selectedIds.value.length} 个用户？`, '批量启用', {
    type: 'warning',
  })
  const n = await batchUpdateUserStatus(selectedIds.value, 1)
  ElMessage.success(`已处理 ${n} 个用户`)
  selectedIds.value = []
  await load()
}

async function toggleStatus(row: AdminUserRow) {
  const action = row.status === 1 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定${action}用户 ${row.username}？`, '提示', { type: 'warning' })
  if (row.status === 1) {
    await disableAdminUser(row.id)
  } else {
    await enableAdminUser(row.id)
  }
  ElMessage.success(`${action}成功`)
  await load()
}

onMounted(() => load(1))
</script>

<style scoped>
.user-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}
.user-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
