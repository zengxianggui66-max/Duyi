<template>
  <AdminPageShell title="教材目录树" :desc="pageDesc">
    <el-alert
      v-if="!canEdit"
      type="info"
      :closable="false"
      show-icon
      class="catalog__hint"
      title="当前为只读模式"
      description="可预览目录树；新增、编辑、导入需 content_admin 或 super_admin 权限。"
    />

    <div class="catalog__toolbar">
      <el-select v-model="schemeId" placeholder="目录方案" style="width: 280px" @change="onSchemeChange">
        <el-option v-for="s in schemes" :key="s.id" :label="s.name" :value="s.id">
          <span>{{ s.name }}</span>
          <span v-if="s.stageName || s.subjectName || s.editionName || s.volumeName" style="color:#999;font-size:12px;margin-left: 6px;">
            （{{ [s.stageName, s.subjectName, s.editionName, s.volumeName].filter(Boolean).join(' · ') }}）
          </span>
        </el-option>
      </el-select>
      <el-select v-model="volumeKey" clearable placeholder="册别 volumeKey" style="width: 160px" @change="loadAll">
        <el-option v-for="v in volumeKeys" :key="v" :label="v" :value="v" />
      </el-select>
      <el-input
        v-model="subjectFilter"
        clearable
        placeholder="学科 meta 过滤"
        style="width: 140px"
        @change="loadTree"
      />
      <el-checkbox v-model="showDisabled" @change="loadAll">显示已禁用</el-checkbox>
      <el-button v-if="canEdit" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增节点
      </el-button>
      <el-button
        v-if="canEdit && volumeKey"
        type="warning"
        plain
        :loading="importing"
        @click="importJson"
      >
        从 JSON 导入 {{ volumeKey }}
      </el-button>
    </div>

    <el-row :gutter="16">
      <el-col :span="10">
        <el-card shadow="never" header="目录树预览">
          <el-tree
            v-loading="treeLoading"
            :data="treeData"
            node-key="id"
            :props="{ label: 'name', children: 'children' }"
            default-expand-all
            highlight-current
            @node-click="onTreeNodeClick"
          />
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-table v-loading="loading" :data="records" border stripe empty-text="暂无节点" height="520">
          <el-table-column prop="depth" label="层级" width="60" align="center" />
          <el-table-column prop="nodeType" label="类型" width="80" />
          <el-table-column prop="name" label="名称" min-width="140" show-overflow-tooltip />
          <el-table-column prop="code" label="编码" min-width="140" show-overflow-tooltip />
          <el-table-column prop="sort" label="排序" width="64" align="center" />
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="canEdit" label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button
                link
                :type="row.status === 1 ? 'warning' : 'success'"
                @click="toggleStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button link type="danger" @click="removeRow(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑目录节点' : '新增目录节点'" width="560px" destroy-on-close>
      <el-form label-width="96px">
        <el-form-item label="方案" required>
          <el-select v-model="form.schemeId" style="width: 100%">
            <el-option v-for="s in schemes" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="父节点 ID">
          <el-input-number v-model="form.parentId" :min="0" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="编码" required><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.nodeType" style="width: 100%">
            <el-option v-for="t in nodeTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" :max="9999" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" placeholder="可选 emoji" /></el-form-item>
        <el-form-item label="Meta JSON">
          <el-input
            v-model="form.meta"
            type="textarea"
            :rows="3"
            placeholder='{"volumeKey":"y1s1","subject":"语文"}'
          />
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
  createCatalogNode,
  deleteCatalogNode,
  getAdminCatalogTree,
  importUnitCatalogJson,
  listCatalogNodes,
  listCatalogSchemes,
  setCatalogNodeStatus,
  updateCatalogNode,
  type CatalogNodeAdminItem,
  type CatalogSchemeItem,
} from '@/admin/api/catalog'
import { useAdminAuthStore } from '@/admin/store/adminAuth'
import { invalidateCatalogCache } from '@/composables/catalogSource'

const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:taxonomy:edit'))
const pageDesc = computed(() =>
  canEdit.value
    ? '维护 edu_catalog_node 教材单元/课文目录；DB 优先替代 unit-catalog.json，C 端 /api/catalog/tree 与上传页单元树同步生效。'
    : '预览教材目录树（只读）。',
)

const volumeKeys = [
  'y1s1', 'y1s2', 'y2s1', 'y2s2', 'y3s1', 'y3s2', 'y4s1', 'y4s2', 'y5s1', 'y5s2', 'y6s1', 'y6s2',
  'j7s1', 'j7s2', 'j8s1', 'j8s2', 'j9s1', 'j9s2',
]
const nodeTypes = ['folder', 'unit', 'lesson', 'section', 'leaf']

const schemes = ref<CatalogSchemeItem[]>([])
const schemeId = ref<number>()
const volumeKey = ref('')
const subjectFilter = ref('')
const showDisabled = ref(true)
const loading = ref(false)
const treeLoading = ref(false)
const saving = ref(false)
const importing = ref(false)
const records = ref<CatalogNodeAdminItem[]>([])
const treeData = ref<Record<string, unknown>[]>([])

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formEnabled = ref(true)
const form = reactive({
  schemeId: 0,
  parentId: 0,
  code: '',
  name: '',
  nodeType: 'unit',
  sort: 0,
  icon: '',
  meta: '',
})

async function loadSchemes() {
  schemes.value = await listCatalogSchemes()
  if (!schemeId.value && schemes.value.length) {
    schemeId.value = schemes.value.find((s) => s.code === 'textbook_unit')?.id ?? schemes.value[0].id
  }
}

async function loadTree() {
  if (!schemeId.value) return
  treeLoading.value = true
  try {
    const scheme = schemes.value.find((s) => s.id === schemeId.value)
    treeData.value = (await getAdminCatalogTree({
      schemeId: schemeId.value,
      schemeCode: scheme?.code,
      volumeKey: volumeKey.value || undefined,
      subject: subjectFilter.value || undefined,
      includeDisabled: showDisabled.value,
    })) as unknown as Record<string, unknown>[]
  } catch {
    treeData.value = []
  } finally {
    treeLoading.value = false
  }
}

async function loadTable() {
  if (!schemeId.value) return
  loading.value = true
  try {
    records.value = await listCatalogNodes({
      schemeId: schemeId.value,
      volumeKey: volumeKey.value || undefined,
      includeDisabled: showDisabled.value,
    })
  } catch {
    records.value = []
    ElMessage.error('加载节点列表失败')
  } finally {
    loading.value = false
  }
}

function loadAll() {
  void loadTree()
  void loadTable()
}

function onSchemeChange() {
  loadAll()
}

function onTreeNodeClick(node: { id?: number }) {
  if (node?.id) {
    form.parentId = Number(node.id)
  }
}

function resetForm() {
  form.schemeId = schemeId.value || 0
  form.parentId = 0
  form.code = ''
  form.name = ''
  form.nodeType = 'unit'
  form.sort = 0
  form.icon = ''
  form.meta = volumeKey.value ? `{"volumeKey":"${volumeKey.value}","subject":"语文"}` : ''
}

function openCreate() {
  editingId.value = null
  resetForm()
  formEnabled.value = true
  dialogVisible.value = true
}

function openEdit(row: CatalogNodeAdminItem) {
  editingId.value = row.id
  form.schemeId = row.schemeId
  form.parentId = row.parentId || 0
  form.code = row.code
  form.name = row.name
  form.nodeType = row.nodeType
  form.sort = row.sort || 0
  form.icon = row.icon || ''
  form.meta = row.meta || ''
  formEnabled.value = row.status !== 0
  dialogVisible.value = true
}

async function save() {
  if (!form.schemeId || !form.code || !form.name || !form.nodeType) {
    ElMessage.warning('请填写必填项')
    return
  }
  saving.value = true
  const payload = {
    ...form,
    parentId: form.parentId || undefined,
    status: formEnabled.value ? 1 : 0,
  }
  try {
    if (editingId.value) {
      await updateCatalogNode(editingId.value, payload)
    } else {
      await createCatalogNode(payload)
    }
    ElMessage.success('保存成功')
    invalidateCatalogCache()
    dialogVisible.value = false
    loadAll()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: CatalogNodeAdminItem) {
  try {
    await setCatalogNodeStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('状态已更新')
    loadAll()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function removeRow(row: CatalogNodeAdminItem) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.name}」？`, '确认删除', { type: 'warning' })
    await deleteCatalogNode(row.id)
    ElMessage.success('已删除')
    loadAll()
  } catch (err: any) {
    const msg = err?.response?.data?.message || err?.message || ''
    if (msg.includes('已被') || msg.includes('资源挂载')) {
      ElMessage.warning(msg || '删除失败')
    }
    if (msg.includes('请先删除子节点')) {
      ElMessage.warning('请先删除子节点')
    }
  }
}

async function importJson() {
  if (!volumeKey.value) {
    ElMessage.warning('请先选择册别 volumeKey')
    return
  }
  try {
    await ElMessageBox.confirm(
      `将把 unit-catalog.json 中「${volumeKey.value}」导入 DB（覆盖同 volumeKey 节点），继续？`,
      '确认导入',
      { type: 'warning' },
    )
    importing.value = true
    const res = await importUnitCatalogJson(volumeKey.value)
    ElMessage.success(`已导入 ${res.importedCount} 个节点`)
    loadAll()
  } catch {
    /* cancelled or failed */
  } finally {
    importing.value = false
  }
}

onMounted(async () => {
  await loadSchemes()
  loadAll()
})
</script>

<style scoped>
.catalog__hint {
  margin-bottom: 16px;
}
.catalog__toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
