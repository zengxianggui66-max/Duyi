<template>
  <div>
    <el-alert
      v-if="!canEdit"
      type="info"
      :closable="false"
      show-icon
      class="home-ops__hint"
      title="当前为只读模式"
      description="可查看热门词；编辑需 content_admin 或 super_admin 权限。"
    />

    <div class="home-ops__toolbar">
      <el-checkbox v-model="showDisabled" @change="load">显示已禁用</el-checkbox>
      <el-button v-if="canEdit && sortDirty" type="success" :loading="reordering" @click="saveOrder">
        保存排序
      </el-button>
      <el-button v-if="canEdit" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增热门词
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" border stripe empty-text="暂无数据">
      <el-table-column prop="label" label="展示文案" min-width="140" />
      <el-table-column prop="actionType" label="类型" width="90" />
      <el-table-column prop="badge" label="角标" width="72" />
      <el-table-column label="跳转" width="100">
        <template #default="{ row }">{{ row.navTarget?.type ?? '-' }}</template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="72" align="center" />
      <el-table-column label="状态" width="88" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="canEdit" label="排序" width="120" align="center">
        <template #default="{ row, $index }">
          <el-button link :disabled="$index === 0" @click="moveRow($index, -1)">↑</el-button>
          <el-button link :disabled="$index === records.length - 1" @click="moveRow($index, 1)">↓</el-button>
        </template>
      </el-table-column>
      <el-table-column v-if="canEdit" label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 1" link type="warning" @click="toggleStatus(row, 0)">禁用</el-button>
          <el-button v-else link type="success" @click="toggleStatus(row, 1)">启用</el-button>
          <el-button link type="danger" @click="removeRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-form label-width="96px" @submit.prevent="save">
        <el-form-item label="展示文案" required><el-input v-model="form.label" /></el-form-item>
        <el-form-item label="Action 类型" required>
          <el-select v-model="form.actionType" style="width: 100%" @change="syncActionType">
            <el-option label="browse 导航" value="browse" />
            <el-option label="search 搜索" value="search" />
          </el-select>
        </el-form-item>
        <el-form-item label="角标"><el-input v-model="form.badge" placeholder="热 / 新" /></el-form-item>
        <el-form-item label="可见学段">
          <el-select v-model="form.stageKeys" multiple clearable style="width: 100%" placeholder="留空=全学段">
            <el-option v-for="s in stageOptions" :key="s.key" :label="s.name" :value="s.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" :max="999" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
        <NavTargetForm v-model="form.navTarget" />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="canEdit" type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useAdminAuthStore } from '@/admin/store/adminAuth'
import NavTargetForm from '@/admin/components/NavTargetForm.vue'
import { adminHomeOpsApi, type AdminHomeHotWordItem } from '@/admin/api/homeOps'
import { stages } from '@/config/subjectConfig'
import type { NavTarget } from '@/types/homeOps'

const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:home:edit'))

const loading = ref(false)
const saving = ref(false)
const reordering = ref(false)
const sortDirty = ref(false)
const showDisabled = ref(true)
const records = ref<AdminHomeHotWordItem[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const stageOptions = stages

const form = reactive({
  label: '',
  actionType: 'browse' as 'browse' | 'search',
  badge: '',
  stageKeys: [] as string[],
  sort: 0,
  remark: '',
  navTarget: { type: 'browse', stageKey: 'primary', subjectKey: 'chinese', versionKey: 'tongbian2024' } as NavTarget,
})

const dialogTitle = computed(() => (editingId.value ? '编辑热门词' : '新增热门词'))

function syncActionType() {
  form.navTarget = {
    ...form.navTarget,
    type: form.actionType,
  }
}

watch(
  () => form.navTarget.type,
  (type) => {
    if (type === 'browse' || type === 'search') {
      form.actionType = type
    }
  },
)

function resetForm() {
  form.label = ''
  form.actionType = 'browse'
  form.badge = ''
  form.stageKeys = []
  form.sort = 0
  form.remark = ''
  form.navTarget = {
    type: 'browse',
    stageKey: 'primary',
    subjectKey: 'chinese',
    versionKey: 'tongbian2024',
    query: { module: '同步备课' },
  }
}

async function load() {
  loading.value = true
  try {
    records.value = (await adminHomeOpsApi.listHotWords(showDisabled.value)) ?? []
    sortDirty.value = false
  } catch (e) {
    ElMessage.error(String(e))
  } finally {
    loading.value = false
  }
}

function moveRow(index: number, delta: number) {
  const next = index + delta
  if (next < 0 || next >= records.value.length) return
  const list = [...records.value]
  const tmp = list[index]
  list[index] = list[next]
  list[next] = tmp
  records.value = list
  sortDirty.value = true
}

async function saveOrder() {
  reordering.value = true
  const items = records.value.map((row, idx) => ({
    id: row.id,
    sort: (records.value.length - idx) * 10,
  }))
  try {
    await adminHomeOpsApi.reorderHotWords(items)
    ElMessage.success('排序已保存')
    sortDirty.value = false
    await load()
  } catch (e) {
    ElMessage.error(String(e))
  } finally {
    reordering.value = false
  }
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: AdminHomeHotWordItem) {
  editingId.value = row.id
  form.label = row.label
  form.actionType = row.actionType
  form.badge = row.badge ?? ''
  form.stageKeys = [...(row.stageKeys ?? [])]
  form.sort = row.sort ?? 0
  form.remark = row.remark ?? ''
  form.navTarget = { ...row.navTarget, query: row.navTarget.query ? { ...row.navTarget.query } : undefined }
  dialogVisible.value = true
}

async function save() {
  if (!form.label.trim()) {
    ElMessage.warning('请填写展示文案')
    return
  }
  if (form.navTarget.type !== form.actionType) {
    ElMessage.warning('action_type 须与跳转类型一致')
    return
  }
  saving.value = true
  const payload = {
    label: form.label.trim(),
    actionType: form.actionType,
    navTarget: form.navTarget,
    badge: form.badge || undefined,
    stageKeys: form.stageKeys.length ? form.stageKeys : undefined,
    sort: form.sort,
    status: 1,
    remark: form.remark || undefined,
  }
  try {
    if (editingId.value) {
      await adminHomeOpsApi.updateHotWord(editingId.value, payload)
    } else {
      await adminHomeOpsApi.createHotWord(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await load()
  } catch (e) {
    ElMessage.error(String(e))
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: AdminHomeHotWordItem, status: 0 | 1) {
  try {
    await adminHomeOpsApi.setHotWordStatus(row.id, status)
    ElMessage.success(status === 1 ? '已启用' : '已禁用')
    await load()
  } catch (e) {
    ElMessage.error(String(e))
  }
}

async function removeRow(row: AdminHomeHotWordItem) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.label}」？`, '确认删除', { type: 'warning' })
    await adminHomeOpsApi.deleteHotWord(row.id)
    ElMessage.success('已删除')
    await load()
  } catch {
    /* cancelled */
  }
}

onMounted(load)
</script>

<style scoped>
.home-ops__hint {
  margin-bottom: 12px;
}
.home-ops__toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
</style>
