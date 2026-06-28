<template>
  <div class="role-manage">
    <div class="role-manage__toolbar">
      <el-button v-permission="'admin:role:create'" type="primary" @click="openCreate">新建角色</el-button>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="roles" border stripe>
      <el-table-column prop="code" label="编码" width="160" />
      <el-table-column prop="name" label="名称" width="140" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isBuiltin ? 'info' : 'success'" size="small">
            {{ row.isBuiltin ? '内置' : '自定义' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            v-permission="'admin:role:assign_permission'"
            link
            type="primary"
            :disabled="row.code === 'super_admin'"
            @click="openPerm(row)"
          >
            分配权限
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="createVisible" title="新建角色" width="480px">
      <el-form label-width="80px">
        <el-form-item label="编码">
          <el-input v-model="createForm.code" placeholder="如 custom_editor" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="createForm.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permVisible" :title="`分配权限 - ${currentRole?.name || ''}`" width="560px">
      <el-tree
        ref="treeRef"
        :data="permTree"
        show-checkbox
        node-key="id"
        default-expand-all
        :props="{ label: 'name', children: 'children' }"
      />
      <template #footer>
        <el-button @click="permVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitPerm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, type ElTree } from 'element-plus'
import {
  assignRolePermissions,
  createRole,
  getPermissionTree,
  listRoles,
  type AdminPermissionNode,
  type AdminRole,
} from '@/admin/api/roles'

const loading = ref(false)
const saving = ref(false)
const roles = ref<AdminRole[]>([])
const permTree = ref<AdminPermissionNode[]>([])
const createVisible = ref(false)
const permVisible = ref(false)
const currentRole = ref<AdminRole | null>(null)
const treeRef = ref<InstanceType<typeof ElTree>>()
const createForm = ref({ code: '', name: '', description: '' })

async function load() {
  loading.value = true
  try {
    roles.value = await listRoles()
  } finally {
    loading.value = false
  }
}

async function loadTree() {
  permTree.value = await getPermissionTree()
}

function openCreate() {
  createForm.value = { code: '', name: '', description: '' }
  createVisible.value = true
}

async function submitCreate() {
  if (!createForm.value.code || !createForm.value.name) {
    ElMessage.warning('请填写编码和名称')
    return
  }
  saving.value = true
  try {
    await createRole(createForm.value)
    ElMessage.success('创建成功')
    createVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function openPerm(row: AdminRole) {
  currentRole.value = row
  if (!permTree.value.length) {
    await loadTree()
  }
  permVisible.value = true
  setTimeout(() => {
    treeRef.value?.setCheckedKeys(row.permissionIds || [], false)
  }, 0)
}

async function submitPerm() {
  if (!currentRole.value) return
  const checked = treeRef.value?.getCheckedKeys(false) as number[]
  const half = treeRef.value?.getHalfCheckedKeys() as number[]
  const permissionIds = [...new Set([...(checked || []), ...(half || [])])]
  saving.value = true
  try {
    await assignRolePermissions(currentRole.value.id, permissionIds)
    ElMessage.success('权限已保存')
    permVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.role-manage__toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 8px;
}
</style>
