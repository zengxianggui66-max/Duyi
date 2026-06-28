<template>
  <AdminPageShell title="管理员账号" desc="后台 staff 账号（user.role=admin）· 分配 sys_role 后台权限">
    <div class="user-toolbar">
      <el-input
        v-model="keyword"
        placeholder="用户名 / 昵称"
        clearable
        style="width: 220px"
        @keyup.enter="load(1)"
      />
      <el-select v-model="status" placeholder="状态" clearable style="width: 100px" @change="load(1)">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe empty-text="暂无管理员">
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="nickname" label="昵称" width="140" />
      <el-table-column label="后台角色" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <template v-if="row.adminRoles?.length">
            <el-tag
              v-for="code in row.adminRoles"
              :key="code"
              size="small"
              :type="code === 'super_admin' ? 'danger' : 'info'"
              style="margin-right: 4px"
            >
              {{ roleLabel(code) }}
            </el-tag>
          </template>
          <span v-else class="text-muted">未分配</span>
        </template>
      </el-table-column>
      <el-table-column prop="phoneMasked" label="手机号" width="120" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginTime" label="最近登录" min-width="170" />
      <el-table-column label="操作" min-width="280" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="canManageRow(row)"
            v-permission="'admin:admin_user:assign_role'"
            link
            type="primary"
            @click="openRoleDialog(row)"
          >
            分配后台角色
          </el-button>
          <el-tooltip v-else content="仅超级管理员可操作 super_admin 账号" placement="top">
            <el-button link disabled>分配后台角色</el-button>
          </el-tooltip>
          <el-button
            v-if="canManageRow(row)"
            v-permission="'admin:user:reset_password'"
            link
            type="warning"
            @click="onResetPassword(row)"
          >
            重置密码
          </el-button>
          <el-button
            v-if="canManageRow(row)"
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

    <el-dialog v-model="roleVisible" :title="`分配后台角色 - ${currentUser?.username}`" width="480px">
      <el-checkbox-group v-model="selectedRoleIds">
        <el-checkbox
          v-for="r in allRoles"
          :key="r.id"
          :label="r.id"
          :value="r.id"
          :disabled="isSuperAdminRoleDisabled(r)"
        >
          {{ r.name }} ({{ r.code }})
        </el-checkbox>
      </el-checkbox-group>
      <p v-if="!adminStore.isSuperAdmin" class="role-hint">
        非超级管理员不可分配或变更 super_admin 角色
      </p>
      <template #footer>
        <el-button @click="roleVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitRoles">保存</el-button>
      </template>
    </el-dialog>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminPageShell from '@/admin/components/AdminPageShell.vue'
import { useAdminAuthStore } from '@/admin/store/adminAuth'
import { assignUserRoles, getUserRoleIds, listRoles, type AdminRole } from '@/admin/api/roles'
import {
  disableAdminUser,
  enableAdminUser,
  listAdminUsers,
  resetUserPassword,
  type AdminUserRow,
} from '@/admin/api/users'

const adminStore = useAdminAuthStore()
const loading = ref(false)
const saving = ref(false)
const rows = ref<AdminUserRow[]>([])
const keyword = ref('')
const status = ref<number>()
const current = ref(1)
const size = ref(20)
const total = ref(0)

const allRoles = ref<AdminRole[]>([])
const roleVisible = ref(false)
const currentUser = ref<AdminUserRow | null>(null)
const selectedRoleIds = ref<number[]>([])

const ROLE_LABEL: Record<string, string> = {
  super_admin: '超级管理员',
  content_admin: '内容管理员',
  auditor: '审核员',
  operator: '运营人员',
  customer_service: '客服',
  finance_admin: '财务',
  system_admin: '系统配置',
}

function roleLabel(code: string) {
  return ROLE_LABEL[code] || code
}

function isSuperAdminTarget(row: AdminUserRow) {
  return row.adminRoles?.includes('super_admin') ?? false
}

function canManageRow(row: AdminUserRow) {
  return adminStore.isSuperAdmin || !isSuperAdminTarget(row)
}

function isSuperAdminRoleDisabled(role: AdminRole) {
  if (role.code !== 'super_admin') return false
  return !adminStore.isSuperAdmin
}

async function load(page = current.value) {
  loading.value = true
  current.value = page
  try {
    const data = await listAdminUsers({
      current: current.value,
      size: size.value,
      keyword: keyword.value || undefined,
      status: status.value,
      staffOnly: true,
    })
    rows.value = data.records
    total.value = data.total > 0 ? data.total : data.records.length
  } finally {
    loading.value = false
  }
}

async function openRoleDialog(row: AdminUserRow) {
  if (!canManageRow(row)) {
    ElMessage.warning('仅超级管理员可管理 super_admin 账号')
    return
  }
  currentUser.value = row
  if (!allRoles.value.length) {
    allRoles.value = await listRoles()
  }
  selectedRoleIds.value = await getUserRoleIds(row.id)
  roleVisible.value = true
}

async function submitRoles() {
  if (!currentUser.value) return
  saving.value = true
  try {
    await assignUserRoles(currentUser.value.id, selectedRoleIds.value)
    ElMessage.success('后台角色已更新')
    roleVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function onResetPassword(row: AdminUserRow) {
  if (!canManageRow(row)) {
    ElMessage.warning('仅超级管理员可管理 super_admin 账号')
    return
  }
  await ElMessageBox.confirm(`确定重置 ${row.username} 的密码？`, '提示', { type: 'warning' })
  const pwd = await resetUserPassword(row.id)
  ElMessageBox.alert(`新密码：${pwd}`, '重置成功', { type: 'success' })
}

async function toggleStatus(row: AdminUserRow) {
  if (!canManageRow(row)) {
    ElMessage.warning('仅超级管理员可管理 super_admin 账号')
    return
  }
  const action = row.status === 1 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定${action}管理员 ${row.username}？`, '提示', { type: 'warning' })
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
.text-muted {
  color: var(--el-text-color-secondary);
}
.role-hint {
  margin: 12px 0 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
