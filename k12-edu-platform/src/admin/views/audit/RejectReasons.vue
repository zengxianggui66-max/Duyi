<template>
  <AdminPageShell
    title="驳回原因模板"
    :desc="pageDesc"
  >
    <el-alert
      v-if="!canEdit"
      type="info"
      :closable="false"
      show-icon
      class="reject-reasons__hint"
      title="当前为只读模式"
      description="审核员可查看模板并在「待审队列」驳回时选用；新增、编辑、启用/禁用、删除请使用超级管理员（admin）或内容管理员（content_admin）账号。"
    />

    <div class="reject-reasons__toolbar">
      <el-button v-if="canEdit" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增模板
      </el-button>
      <el-checkbox v-model="showDisabled" @change="load">显示已禁用</el-checkbox>
      <span v-if="canEdit" class="toolbar-tip">禁用后不在待审页下拉中展示，可再次启用</span>
    </div>

    <el-table v-loading="loading" :data="records" border stripe empty-text="暂无模板">
      <el-table-column prop="sort" label="排序" width="72" align="center" />
      <el-table-column label="分类" width="110" align="center">
        <template #default="{ row }">
          <el-tag size="small">{{ categoryLabel(row.category) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="驳回原因" min-width="260" show-overflow-tooltip />
      <el-table-column label="状态" width="88" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="170">
        <template #default="{ row }">
          {{ formatTime(row.updatedAt || row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column v-if="canEdit" label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button
            v-if="row.status === 1"
            link
            type="warning"
            @click="toggleStatus(row, 0)"
          >
            禁用
          </el-button>
          <el-button
            v-else
            link
            type="success"
            @click="toggleStatus(row, 1)"
          >
            启用
          </el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑驳回模板' : '新增驳回模板'"
      width="520px"
      destroy-on-close
    >
      <el-form label-width="88px" @submit.prevent="save">
        <el-form-item label="问题分类" required>
          <el-select v-model="form.category" placeholder="选择分类" style="width: 100%">
            <el-option label="通用" :value="0" />
            <el-option label="内容质量" :value="1" />
            <el-option label="格式规范" :value="2" />
            <el-option label="版权合规" :value="3" />
            <el-option label="分类挂载" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="驳回原因" required>
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="审核员驳回时可一键选用，例如：文件格式不符合要求"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
          <span class="form-tip">数字越小越靠前</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="formEnabled"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AdminPageShell from '@/admin/components/AdminPageShell.vue'
import {
  createRejectReason,
  deleteRejectReason,
  listRejectReasons,
  setRejectReasonStatus,
  updateRejectReason,
  type RejectReasonItem,
} from '@/admin/api/audit'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:audit:reasons:edit'))

const pageDesc = computed(() =>
  canEdit.value
    ? '维护审核驳回常用话术；启用项会在「待审队列」驳回弹窗中供审核员选用'
    : '查看平台驳回话术模板（只读）',
)

const loading = ref(false)
const saving = ref(false)
const records = ref<RejectReasonItem[]>([])
const showDisabled = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = ref({ content: '', sort: 0, category: 0 })
const formEnabled = ref(true)

function categoryLabel(cat?: number): string {
  const map: Record<number, string> = {
    0: '通用',
    1: '内容质量',
    2: '格式规范',
    3: '版权合规',
    4: '分类挂载',
    5: '其他',
  }
  return map[cat ?? 0] || '通用'
}

function formatTime(raw?: string): string {
  if (!raw) return '-'
  return raw.replace('T', ' ').slice(0, 19)
}

function nextSort(): number {
  if (records.value.length === 0) return 10
  const max = Math.max(...records.value.map((r) => r.sort ?? 0))
  return max + 10
}

async function load() {
  loading.value = true
  try {
    records.value = await listRejectReasons(showDisabled.value)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.value = { content: '', sort: nextSort(), category: 0 }
  formEnabled.value = true
  dialogVisible.value = true
}

function openEdit(row: RejectReasonItem) {
  editingId.value = row.id
  form.value = { content: row.content, sort: row.sort, category: row.category ?? 0 }
  formEnabled.value = row.status === 1
  dialogVisible.value = true
}

async function save() {
  const content = form.value.content.trim()
  if (!content) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  saving.value = true
  try {
    const payload = {
      content,
      category: form.value.category,
      sort: form.value.sort,
      status: formEnabled.value ? 1 : 0,
    }
    if (editingId.value) {
      await updateRejectReason(editingId.value, payload)
      ElMessage.success('模板已更新')
    } else {
      await createRejectReason(payload)
      ElMessage.success('模板已新增')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: RejectReasonItem, status: 0 | 1) {
  const action = status === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确定${action}模板「${row.content}」吗？`, `${action}模板`, {
    type: status === 1 ? 'info' : 'warning',
  })
  await setRejectReasonStatus(row.id, status)
  ElMessage.success(`已${action}`)
  await load()
}

async function remove(row: RejectReasonItem) {
  await ElMessageBox.confirm(
    `确定删除模板「${row.content}」吗？删除后不可恢复，但历史审核记录中的驳回原因不受影响。`,
    '删除模板',
    { type: 'warning', confirmButtonText: '删除', confirmButtonClass: 'el-button--danger' },
  )
  await deleteRejectReason(row.id)
  ElMessage.success('模板已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.reject-reasons__hint {
  margin-bottom: 16px;
}
.reject-reasons__toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}
.toolbar-tip,
.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
.form-tip {
  margin-left: 12px;
}
</style>
