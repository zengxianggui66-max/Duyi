<template>
  <AdminPageShell title="业务字典" :desc="pageDesc">
    <el-alert
      v-if="!canEdit"
      type="info"
      :closable="false"
      show-icon
      class="dict__hint"
      title="当前为只读模式"
      description="可查看考试场景、地区、文件格式；编辑需 content_admin 或 super_admin 权限。"
    />

    <el-tabs v-model="activeTab" class="dict-tabs" @tab-change="loadCurrent">
      <el-tab-pane label="考试场景" name="examScenes" />
      <el-tab-pane label="地区" name="regions" />
      <el-tab-pane label="文件格式" name="fileFormats" />
    </el-tabs>

    <div class="dict__toolbar">
      <el-select
        v-if="activeTab === 'regions'"
        v-model="filterParentId"
        clearable
        placeholder="按上级筛选"
        style="width: 200px"
        @change="loadCurrent"
      >
        <el-option label="顶级（parentId=0）" :value="0" />
        <el-option v-for="r in regionOptions" :key="r.id" :label="r.name" :value="r.id" />
      </el-select>
      <el-checkbox v-model="showDisabled" @change="loadCurrent">显示已禁用</el-checkbox>
      <el-button v-if="canEdit" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" border stripe empty-text="暂无数据">
      <template v-if="activeTab === 'examScenes'">
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="examLevel" label="级别" width="110" />
        <el-table-column prop="sort" label="排序" width="72" align="center" />
      </template>

      <template v-else-if="activeTab === 'regions'">
        <el-table-column prop="parentId" label="上级ID" width="88" align="center" />
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="level" label="层级" width="72" align="center" />
        <el-table-column prop="sort" label="排序" width="72" align="center" />
      </template>

      <template v-else>
        <el-table-column prop="code" label="编码" width="100" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="extensions" label="扩展名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="previewType" label="预览类型" width="110" />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form label-width="96px" @submit.prevent="save">
        <template v-if="activeTab === 'examScenes'">
          <el-form-item label="编码" required><el-input v-model="form.code" /></el-form-item>
          <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="级别"><el-input v-model="form.examLevel" placeholder="unit/midterm/final 等" /></el-form-item>
        </template>

        <template v-else-if="activeTab === 'regions'">
          <el-form-item label="上级ID" required>
            <el-input-number v-model="form.parentId" :min="0" :max="999999" style="width: 100%" />
          </el-form-item>
          <el-form-item label="编码" required><el-input v-model="form.code" /></el-form-item>
          <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="层级">
            <el-input-number v-model="form.level" :min="0" :max="3" />
            <span class="form-tip">0全国 1省 2市 3区县</span>
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="编码" required><el-input v-model="form.code" /></el-form-item>
          <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="扩展名" required><el-input v-model="form.extensions" placeholder="doc,docx" /></el-form-item>
          <el-form-item label="预览类型"><el-input v-model="form.previewType" placeholder="office/pdf/audio" /></el-form-item>
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
  adminDictionaryApi,
  type DictionaryExamSceneItem,
  type DictionaryFileFormatItem,
  type DictionaryRegionItem,
} from '@/admin/api/dictionary'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

type TabName = 'examScenes' | 'regions' | 'fileFormats'
type RowItem = DictionaryExamSceneItem | DictionaryRegionItem | DictionaryFileFormatItem

interface DictionaryForm {
  code?: string
  name?: string
  examLevel?: string
  parentId?: number
  level?: number
  extensions?: string
  previewType?: string
  sort?: number
  status?: number
}

const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:taxonomy:edit'))
const pageDesc = computed(() =>
  canEdit.value
    ? '维护考试场景、地区、文件格式；修改后 C 端 /api/dictionary/* 立即生效。'
    : '查看业务字典配置（只读）。',
)

const activeTab = ref<TabName>('examScenes')
const loading = ref(false)
const saving = ref(false)
const showDisabled = ref(true)
const filterParentId = ref<number | undefined>()
const records = ref<RowItem[]>([])
const regionOptions = ref<DictionaryRegionItem[]>([])

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<DictionaryForm>({})
const formEnabled = ref(true)

const dialogTitle = computed(() => {
  const labels: Record<TabName, string> = {
    examScenes: '考试场景',
    regions: '地区',
    fileFormats: '文件格式',
  }
  return `${editingId.value ? '编辑' : '新增'}${labels[activeTab.value]}`
})

function nextSort(): number {
  if (records.value.length === 0) return 10
  return Math.max(...records.value.map((r) => r.sort ?? 0)) + 10
}

async function loadRegionsForFilter() {
  regionOptions.value = await adminDictionaryApi.listRegions(undefined, true)
}

async function loadCurrent() {
  loading.value = true
  try {
    if (activeTab.value === 'examScenes') {
      records.value = await adminDictionaryApi.listExamScenes(showDisabled.value)
    } else if (activeTab.value === 'regions') {
      records.value = await adminDictionaryApi.listRegions(filterParentId.value, showDisabled.value)
    } else {
      records.value = await adminDictionaryApi.listFileFormats(showDisabled.value)
    }
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.keys(form).forEach((k) => delete form[k])
  form.sort = nextSort()
  formEnabled.value = true
  if (activeTab.value === 'regions') {
    form.parentId = filterParentId.value ?? 0
    form.level = 1
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
  dialogVisible.value = true
}

async function save() {
  const code = String(form.code ?? '').trim()
  const name = String(form.name ?? '').trim()
  if (!code || !name) {
    ElMessage.warning('请填写编码和名称')
    return
  }
  if (activeTab.value === 'fileFormats' && !String(form.extensions ?? '').trim()) {
    ElMessage.warning('请填写扩展名')
    return
  }

  saving.value = true
  try {
    const status = formEnabled.value ? 1 : 0
    const sort = Number(form.sort ?? 0)

    if (activeTab.value === 'examScenes') {
      const payload = {
        code,
        name,
        examLevel: String(form.examLevel ?? '').trim() || undefined,
        sort,
        status,
      }
      if (editingId.value) await adminDictionaryApi.updateExamScene(editingId.value, payload)
      else await adminDictionaryApi.createExamScene(payload)
    } else if (activeTab.value === 'regions') {
      const payload = {
        parentId: Number(form.parentId ?? 0),
        code,
        name,
        level: Number(form.level ?? 1),
        sort,
        status,
      }
      if (editingId.value) await adminDictionaryApi.updateRegion(editingId.value, payload)
      else await adminDictionaryApi.createRegion(payload)
    } else {
      const payload = {
        code,
        name,
        extensions: String(form.extensions ?? '').trim(),
        previewType: String(form.previewType ?? '').trim() || undefined,
        sort,
        status,
      }
      if (editingId.value) await adminDictionaryApi.updateFileFormat(editingId.value, payload)
      else await adminDictionaryApi.createFileFormat(payload)
    }

    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadCurrent()
    if (activeTab.value === 'regions') await loadRegionsForFilter()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: RowItem, status: 0 | 1) {
  if (activeTab.value === 'examScenes') await adminDictionaryApi.setExamSceneStatus(row.id, status)
  else if (activeTab.value === 'regions') await adminDictionaryApi.setRegionStatus(row.id, status)
  else await adminDictionaryApi.setFileFormatStatus(row.id, status)
  ElMessage.success(status === 1 ? '已启用' : '已禁用')
  await loadCurrent()
}

async function removeRow(row: RowItem) {
  await ElMessageBox.confirm(`确定删除「${row.name}」？`, '确认删除', { type: 'warning' })
  if (activeTab.value === 'examScenes') await adminDictionaryApi.deleteExamScene(row.id)
  else if (activeTab.value === 'regions') await adminDictionaryApi.deleteRegion(row.id)
  else await adminDictionaryApi.deleteFileFormat(row.id)
  ElMessage.success('已删除')
  await loadCurrent()
}

onMounted(async () => {
  await loadRegionsForFilter()
  await loadCurrent()
})
</script>

<style scoped>
.dict__hint {
  margin-bottom: 16px;
}
.dict__toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
