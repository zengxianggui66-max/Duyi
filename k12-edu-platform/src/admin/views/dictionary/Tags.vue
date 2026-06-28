<template>
  <AdminPageShell title="资源标签" :desc="pageDesc">
    <el-alert
      v-if="!canEdit"
      type="info"
      :closable="false"
      show-icon
      class="tags__hint"
      title="当前为只读模式"
      description="可查看教学场景与浏览/上传属性标签；编辑需 content_admin 或 super_admin 权限。"
    />

    <el-tabs v-model="activeTab" class="tags-tabs" @tab-change="loadCurrent">
      <el-tab-pane label="教学场景" name="teachingScenes" />
      <el-tab-pane label="资源属性标签" name="browseTags" />
    </el-tabs>

    <div class="tags__toolbar">
      <el-checkbox v-model="showDisabled" @change="loadCurrent">显示已禁用</el-checkbox>
      <el-button v-if="canEdit" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" border stripe empty-text="暂无数据">
      <template v-if="activeTab === 'teachingScenes'">
        <el-table-column prop="code" label="编码" width="130" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="sort" label="排序" width="72" align="center" />
      </template>

      <template v-else>
        <el-table-column prop="code" label="编码" width="130" />
        <el-table-column prop="name" label="名称" min-width="100" />
        <el-table-column prop="tagGroup" label="分组" width="90" />
        <el-table-column label="适用学段" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ formatJsonList(row.applicableStages) }}</template>
        </el-table-column>
        <el-table-column label="适用栏目" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ formatJsonList(row.applicableModules) }}</template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="72" align="center" />
      </template>

      <el-table-column label="状态" width="88" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form label-width="108px" @submit.prevent="save">
        <template v-if="activeTab === 'teachingScenes'">
          <el-form-item label="编码" required><el-input v-model="form.code" /></el-form-item>
          <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
        </template>

        <template v-else>
          <el-form-item label="编码" required><el-input v-model="form.code" /></el-form-item>
          <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="分组">
            <el-select v-model="form.tagGroup" style="width: 100%">
              <el-option label="通用 core" value="core" />
              <el-option label="学段 stage" value="stage" />
              <el-option label="栏目 module" value="module" />
            </el-select>
          </el-form-item>
          <el-form-item label="适用学段">
            <el-input
              v-model="form.stageCodesText"
              placeholder="逗号分隔，如 art,dance；留空=全学段"
            />
          </el-form-item>
          <el-form-item label="适用栏目">
            <el-input
              v-model="form.moduleNamesText"
              placeholder="逗号分隔，如 竞赛,专题复习；留空=全栏目"
            />
          </el-form-item>
        </template>

        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formEnabled" active-text="启用" inactive-text="禁用" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AdminPageShell from '@/admin/components/AdminPageShell.vue'
import {
  adminTagApi,
  parseJsonList,
  type TagBrowseTagItem,
  type TagTeachingSceneItem,
} from '@/admin/api/tags'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

type TabName = 'teachingScenes' | 'browseTags'
type RowItem = TagTeachingSceneItem | TagBrowseTagItem

interface TagForm {
  code?: string
  name?: string
  tagGroup?: string
  stageCodesText?: string
  moduleNamesText?: string
  sort?: number
  status?: number
}

const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:taxonomy:edit'))
const pageDesc = computed(() =>
  canEdit.value
    ? '维护教学场景与浏览/上传页资源属性标签；修改后 C 端 /api/dictionary/* 立即生效。'
    : '查看资源标签配置（只读）。',
)

const activeTab = ref<TabName>('teachingScenes')
const loading = ref(false)
const saving = ref(false)
const showDisabled = ref(true)
const records = ref<RowItem[]>([])

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<TagForm>({})
const formEnabled = ref(true)

const dialogTitle = computed(() => {
  const label = activeTab.value === 'teachingScenes' ? '教学场景' : '资源属性标签'
  return `${editingId.value ? '编辑' : '新增'}${label}`
})

function formatJsonList(raw?: string | null): string {
  const list = parseJsonList(raw)
  return list.length ? list.join('、') : '全部'
}

function splitCsv(text?: string): string[] {
  if (!text?.trim()) return []
  return text.split(/[,，]/).map((s) => s.trim()).filter(Boolean)
}

function nextSort(): number {
  if (records.value.length === 0) return 10
  return Math.max(...records.value.map((r) => r.sort ?? 0)) + 10
}

async function loadCurrent() {
  loading.value = true
  try {
    if (activeTab.value === 'teachingScenes') {
      records.value = await adminTagApi.listTeachingScenes(showDisabled.value)
    } else {
      records.value = await adminTagApi.listBrowseTags(showDisabled.value)
    }
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.keys(form).forEach((k) => delete form[k])
  form.sort = nextSort()
  formEnabled.value = true
  if (activeTab.value === 'browseTags') {
    form.tagGroup = 'core'
    form.stageCodesText = ''
    form.moduleNamesText = ''
  }
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: RowItem) {
  editingId.value = row.id
  Object.assign(form, { ...row })
  formEnabled.value = row.status !== 0
  if (activeTab.value === 'browseTags') {
    const tag = row as TagBrowseTagItem
    form.stageCodesText = parseJsonList(tag.applicableStages).join(',')
    form.moduleNamesText = parseJsonList(tag.applicableModules).join(',')
  }
  dialogVisible.value = true
}

async function save() {
  const code = String(form.code ?? '').trim()
  const name = String(form.name ?? '').trim()
  if (!code || !name) {
    ElMessage.warning('请填写编码和名称')
    return
  }

  saving.value = true
  try {
    const status = formEnabled.value ? 1 : 0
    const sort = Number(form.sort ?? 0)

    if (activeTab.value === 'teachingScenes') {
      const payload = { code, name, sort, status }
      if (editingId.value) await adminTagApi.updateTeachingScene(editingId.value, payload)
      else await adminTagApi.createTeachingScene(payload)
    } else {
      const payload = {
        code,
        name,
        tagGroup: String(form.tagGroup ?? 'core'),
        applicableStages: splitCsv(String(form.stageCodesText ?? '')),
        applicableModules: splitCsv(String(form.moduleNamesText ?? '')),
        sort,
        status,
      }
      if (editingId.value) await adminTagApi.updateBrowseTag(editingId.value, payload)
      else await adminTagApi.createBrowseTag(payload)
    }

    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadCurrent()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: RowItem, status: 0 | 1) {
  if (activeTab.value === 'teachingScenes') {
    await adminTagApi.setTeachingSceneStatus(row.id, status)
  } else {
    await adminTagApi.setBrowseTagStatus(row.id, status)
  }
  ElMessage.success(status === 1 ? '已启用' : '已禁用')
  await loadCurrent()
}

async function removeRow(row: RowItem) {
  await ElMessageBox.confirm(`确定删除「${row.name}」？`, '确认删除', { type: 'warning' })
  if (activeTab.value === 'teachingScenes') await adminTagApi.deleteTeachingScene(row.id)
  else await adminTagApi.deleteBrowseTag(row.id)
  ElMessage.success('已删除')
  await loadCurrent()
}

onMounted(loadCurrent)
</script>

<style scoped>
.tags__hint {
  margin-bottom: 16px;
}
.tags__toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
