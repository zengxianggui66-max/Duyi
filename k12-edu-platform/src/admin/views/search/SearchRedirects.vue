<template>
  <div>
    <el-alert
      v-if="!canEdit"
      type="info"
      :closable="false"
      show-icon
      class="hint"
      title="当前为只读模式"
      description="查看重定向规则；编辑需 admin:search:edit 权限。泛学科词（语文/数学等）不可配置直达。"
    />

    <div class="toolbar">
      <el-checkbox v-model="showDisabled" @change="load">显示已禁用</el-checkbox>
      <el-button @click="load">刷新</el-button>
      <el-button v-if="canEdit" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增重定向
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" border stripe empty-text="暂无数据">
      <el-table-column prop="keyword" label="关键词" min-width="120" />
      <el-table-column prop="title" label="展示标题" min-width="120" show-overflow-tooltip />
      <el-table-column prop="routePath" label="路由" min-width="180" show-overflow-tooltip />
      <el-table-column prop="priority" label="优先级" width="80" align="center" />
      <el-table-column label="状态" width="88" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
      <el-table-column v-if="canEdit" label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 1" link type="warning" @click="toggleStatus(row, 0)">禁用</el-button>
          <el-button v-else link type="success" @click="toggleStatus(row, 1)">启用</el-button>
          <el-button link type="danger" @click="removeRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form label-width="96px" @submit.prevent="save">
        <el-form-item label="关键词" required>
          <el-input v-model="form.keyword" placeholder="精确匹配，如 传统文化" />
        </el-form-item>
        <el-form-item label="展示标题"><el-input v-model="form.title" placeholder="默认同关键词" /></el-form-item>
        <el-form-item label="路由路径" required>
          <el-input v-model="form.routePath" placeholder="/culture" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="form.priority" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formEnabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="canEdit" type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  createSearchRedirect,
  deleteSearchRedirect,
  listSearchRedirects,
  setSearchRedirectStatus,
  updateSearchRedirect,
  type SearchRedirectItem,
  type SearchRedirectWrite,
} from '@/admin/api/search'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:search:edit'))

const loading = ref(false)
const saving = ref(false)
const showDisabled = ref(false)
const records = ref<SearchRedirectItem[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const dialogTitle = computed(() => (editingId.value ? '编辑重定向' : '新增重定向'))

const form = reactive<SearchRedirectWrite>({
  keyword: '',
  title: '',
  routePath: '',
  priority: 0,
  status: 1,
  remark: '',
})
const formEnabled = computed({
  get: () => form.status === 1,
  set: (v: boolean) => {
    form.status = v ? 1 : 0
  },
})

function resetForm() {
  form.keyword = ''
  form.title = ''
  form.routePath = ''
  form.priority = 0
  form.status = 1
  form.remark = ''
}

async function load() {
  loading.value = true
  try {
    records.value = await listSearchRedirects(showDisabled.value)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: SearchRedirectItem) {
  editingId.value = row.id ?? null
  form.keyword = row.keyword
  form.title = row.title ?? ''
  form.routePath = row.routePath
  form.priority = row.priority ?? 0
  form.status = row.status ?? 1
  form.remark = row.remark ?? ''
  dialogVisible.value = true
}

async function save() {
  if (!form.keyword.trim() || !form.routePath?.trim()) {
    ElMessage.warning('请填写关键词与路由路径')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateSearchRedirect(editingId.value, { ...form })
      ElMessage.success('已更新')
    } else {
      await createSearchRedirect({ ...form })
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: SearchRedirectItem, status: number) {
  if (!row.id) return
  await setSearchRedirectStatus(row.id, status)
  ElMessage.success(status === 1 ? '已启用' : '已禁用')
  await load()
}

async function removeRow(row: SearchRedirectItem) {
  if (!row.id) return
  await ElMessageBox.confirm(`确定删除重定向「${row.keyword}」？`, '确认', { type: 'warning' })
  await deleteSearchRedirect(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.hint {
  margin-bottom: 16px;
}
</style>
