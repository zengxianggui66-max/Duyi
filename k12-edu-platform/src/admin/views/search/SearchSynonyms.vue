<template>
  <div>
    <el-alert
      v-if="!canEdit"
      type="info"
      :closable="false"
      show-icon
      class="hint"
      title="当前为只读模式"
      description="查看同义词；编辑需 admin:search:edit 权限。"
    />

    <div class="toolbar">
      <el-select v-model="domainFilter" clearable placeholder="domain" style="width: 140px" @change="load">
        <el-option label="global" value="global" />
        <el-option label="subject" value="subject" />
        <el-option label="grade" value="grade" />
        <el-option label="type" value="type" />
        <el-option label="news" value="news" />
        <el-option label="module" value="module" />
      </el-select>
      <el-checkbox v-model="showDisabled" @change="load">显示已禁用</el-checkbox>
      <el-button @click="load">刷新</el-button>
      <el-button v-if="canEdit" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增同义词
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" border stripe empty-text="暂无数据">
      <el-table-column prop="word" label="标准词" min-width="100" />
      <el-table-column prop="synonyms" label="同义词（逗号分隔）" min-width="200" show-overflow-tooltip />
      <el-table-column prop="domain" label="domain" width="90" />
      <el-table-column prop="canonical" label="归一化" width="100" show-overflow-tooltip />
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
      <el-form label-width="96px" @submit.prevent="save">
        <el-form-item label="标准词" required><el-input v-model="form.word" /></el-form-item>
        <el-form-item label="同义词" required>
          <el-input v-model="form.synonyms" type="textarea" :rows="2" placeholder="逗号分隔，如 中文,国文" />
        </el-form-item>
        <el-form-item label="domain">
          <el-select v-model="form.domain" style="width: 100%">
            <el-option label="global" value="global" />
            <el-option label="subject" value="subject" />
            <el-option label="grade" value="grade" />
            <el-option label="type" value="type" />
            <el-option label="news" value="news" />
            <el-option label="module" value="module" />
          </el-select>
        </el-form-item>
        <el-form-item label="归一化词"><el-input v-model="form.canonical" placeholder="默认同标准词" /></el-form-item>
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
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  createSearchSynonym,
  deleteSearchSynonym,
  listSearchSynonyms,
  setSearchSynonymStatus,
  updateSearchSynonym,
  type SearchSynonymItem,
  type SearchSynonymWrite,
} from '@/admin/api/search'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

const adminStore = useAdminAuthStore()
const route = useRoute()
const router = useRouter()
const canEdit = computed(() => adminStore.hasPermission('admin:search:edit'))

const loading = ref(false)
const saving = ref(false)
const showDisabled = ref(false)
const domainFilter = ref<string>()
const records = ref<SearchSynonymItem[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const dialogTitle = computed(() => (editingId.value ? '编辑同义词' : '新增同义词'))

const form = reactive<SearchSynonymWrite>({
  word: '',
  synonyms: '',
  domain: 'global',
  canonical: '',
  status: 1,
})
const formEnabled = computed({
  get: () => form.status === 1,
  set: (v: boolean) => {
    form.status = v ? 1 : 0
  },
})

function resetForm() {
  form.word = ''
  form.synonyms = ''
  form.domain = 'global'
  form.canonical = ''
  form.status = 1
}

async function load() {
  loading.value = true
  try {
    records.value = await listSearchSynonyms(showDisabled.value, domainFilter.value)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  resetForm()
  applyDraftFromQuery()
  dialogVisible.value = true
}

function openEdit(row: SearchSynonymItem) {
  editingId.value = row.id ?? null
  form.word = row.word
  form.synonyms = row.synonyms
  form.domain = row.domain ?? 'global'
  form.canonical = row.canonical ?? ''
  form.status = row.status ?? 1
  dialogVisible.value = true
}

function applyDraftFromQuery() {
  const draft = route.query.draft
  if (typeof draft === 'string' && draft.trim()) {
    form.word = draft.trim()
    form.synonyms = draft.trim()
    form.canonical = draft.trim()
    form.status = 0
  }
}

async function save() {
  if (!form.word.trim() || !form.synonyms.trim()) {
    ElMessage.warning('请填写标准词与同义词')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateSearchSynonym(editingId.value, { ...form })
      ElMessage.success('已更新')
    } else {
      await createSearchSynonym({ ...form })
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    if (route.query.draft) {
      router.replace({ path: route.path })
    }
    await load()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: SearchSynonymItem, status: number) {
  if (!row.id) return
  await setSearchSynonymStatus(row.id, status)
  ElMessage.success(status === 1 ? '已启用' : '已禁用')
  await load()
}

async function removeRow(row: SearchSynonymItem) {
  if (!row.id) return
  await ElMessageBox.confirm(`确定删除同义词「${row.word}」？`, '确认', { type: 'warning' })
  await deleteSearchSynonym(row.id)
  ElMessage.success('已删除')
  await load()
}

watch(
  () => route.query.draft,
  (draft) => {
    if (typeof draft === 'string' && draft.trim() && canEdit.value) {
      openCreate()
    }
  },
)

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
