<template>
  <div>
    <el-alert
      v-if="!canEdit"
      type="info"
      :closable="false"
      show-icon
      class="home-ops__hint"
      title="当前为只读模式"
      description="可查看置顶推荐；编辑需 content_admin 或 super_admin 权限。"
    />

    <div class="home-ops__toolbar">
      <el-select v-model="filterPanel" clearable placeholder="专区" style="width: 140px" @change="load">
        <el-option v-for="p in panelOptions" :key="p.value" :label="p.label" :value="p.value" />
      </el-select>
      <el-input v-model="filterTabKey" clearable placeholder="Tab Key" style="width: 140px" @keyup.enter="load" />
      <el-checkbox v-model="showDisabled" @change="load">显示已禁用</el-checkbox>
      <el-button @click="load">刷新</el-button>
      <el-button v-if="canEdit" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增置顶
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" border stripe empty-text="暂无数据">
      <el-table-column prop="panelCode" label="专区" width="100" />
      <el-table-column prop="tabKey" label="Tab" width="110" />
      <el-table-column prop="filterKey" label="Filter" width="90" />
      <el-table-column prop="resourceId" label="资源ID" width="88" />
      <el-table-column prop="resourceTitle" label="资源标题" min-width="160" show-overflow-tooltip />
      <el-table-column prop="resourceSource" label="来源" width="130" />
      <el-table-column prop="stageKey" label="学段" width="80" />
      <el-table-column prop="sort" label="排序" width="72" align="center" />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px" destroy-on-close>
      <el-form label-width="108px" @submit.prevent="save">
        <el-form-item label="专区" required>
          <el-select v-model="form.panelCode" style="width: 100%">
            <el-option v-for="p in panelOptions" :key="p.value" :label="p.label" :value="p.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="Tab Key" required><el-input v-model="form.tabKey" placeholder="courseware / middle" /></el-form-item>
        <el-form-item label="Filter Key"><el-input v-model="form.filterKey" placeholder="升学专题，可空" /></el-form-item>
        <el-form-item label="资源 ID" required><el-input-number v-model="form.resourceId" :min="1" style="width: 100%" /></el-form-item>
        <el-form-item label="资源来源" required>
          <el-select v-model="form.resourceSource" style="width: 100%">
            <el-option v-for="s in sourceOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="学段 Key"><el-input v-model="form.stageKey" placeholder="primary / junior" /></el-form-item>
        <el-form-item label="学科名"><el-input v-model="form.subjectName" placeholder="语文" /></el-form-item>
        <el-form-item label="年级/册别"><el-input v-model="form.gradeName" placeholder="六年级下册" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" :max="9999" /></el-form-item>
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
import { useAdminAuthStore } from '@/admin/store/adminAuth'
import {
  adminHomePanelApi,
  PANEL_CODE_OPTIONS,
  RESOURCE_SOURCE_OPTIONS,
  type HomePanelFeaturedItem,
} from '@/admin/api/homePanels'

const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:home:edit'))

const panelOptions = PANEL_CODE_OPTIONS
const sourceOptions = RESOURCE_SOURCE_OPTIONS

const filterPanel = ref('')
const filterTabKey = ref('')
const loading = ref(false)
const saving = ref(false)
const showDisabled = ref(true)
const records = ref<HomePanelFeaturedItem[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  panelCode: 'sync_prep',
  tabKey: 'courseware',
  filterKey: '',
  resourceId: 10001,
  resourceSource: 'oss_primary_chinese',
  stageKey: 'primary',
  subjectName: '语文',
  gradeName: '',
  sort: 100,
})

const dialogTitle = computed(() => (editingId.value ? '编辑置顶' : '新增置顶'))

function resetForm() {
  form.panelCode = 'sync_prep'
  form.tabKey = 'courseware'
  form.filterKey = ''
  form.resourceId = 10001
  form.resourceSource = 'oss_primary_chinese'
  form.stageKey = 'primary'
  form.subjectName = '语文'
  form.gradeName = ''
  form.sort = 100
}

async function load() {
  loading.value = true
  try {
    records.value =
      (await adminHomePanelApi.listFeatured(
        filterPanel.value || undefined,
        filterTabKey.value || undefined,
        undefined,
        showDisabled.value,
      )) ?? []
  } catch (e) {
    ElMessage.error(String(e))
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: HomePanelFeaturedItem) {
  editingId.value = row.id
  form.panelCode = row.panelCode
  form.tabKey = row.tabKey
  form.filterKey = row.filterKey ?? ''
  form.resourceId = row.resourceId
  form.resourceSource = row.resourceSource
  form.stageKey = row.stageKey ?? ''
  form.subjectName = row.subjectName ?? ''
  form.gradeName = row.gradeName ?? ''
  form.sort = row.sort ?? 0
  dialogVisible.value = true
}

async function save() {
  if (!form.tabKey.trim() || !form.resourceId) {
    ElMessage.warning('请填写 Tab 与资源 ID')
    return
  }
  saving.value = true
  const payload = {
    panelCode: form.panelCode,
    tabKey: form.tabKey.trim(),
    filterKey: form.filterKey || undefined,
    resourceId: form.resourceId,
    resourceSource: form.resourceSource,
    stageKey: form.stageKey || undefined,
    subjectName: form.subjectName || undefined,
    gradeName: form.gradeName || undefined,
    sort: form.sort,
    status: 1 as const,
  }
  try {
    if (editingId.value) {
      await adminHomePanelApi.updateFeatured(editingId.value, payload)
    } else {
      await adminHomePanelApi.createFeatured(payload)
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

async function toggleStatus(row: HomePanelFeaturedItem, status: 0 | 1) {
  try {
    await adminHomePanelApi.setFeaturedStatus(row.id, status)
    ElMessage.success(status === 1 ? '已启用' : '已禁用')
    await load()
  } catch (e) {
    ElMessage.error(String(e))
  }
}

async function removeRow(row: HomePanelFeaturedItem) {
  try {
    await ElMessageBox.confirm(`确定删除置顶 #${row.id}？`, '确认删除', { type: 'warning' })
    await adminHomePanelApi.deleteFeatured(row.id)
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
  flex-wrap: wrap;
}
</style>
