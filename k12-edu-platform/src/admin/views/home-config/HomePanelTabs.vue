<template>
  <div>
    <el-alert
      v-if="!canEdit"
      type="info"
      :closable="false"
      show-icon
      class="home-ops__hint"
      title="当前为只读模式"
      description="可查看专区 Tab 配置；编辑需 content_admin 或 super_admin 权限。"
    />

    <div class="home-ops__toolbar">
      <el-select v-model="panelCode" style="width: 160px" @change="load">
        <el-option v-for="p in panelOptions" :key="p.value" :label="p.label" :value="p.value" />
      </el-select>
      <el-checkbox v-model="showDisabled" @change="load">显示已禁用</el-checkbox>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="records" border stripe empty-text="暂无数据">
      <el-table-column prop="tabKey" label="Tab Key" width="120" />
      <el-table-column prop="filterKey" label="Filter" width="100" />
      <el-table-column prop="tabLabel" label="展示名" min-width="100" />
      <el-table-column prop="queryMode" label="模式" width="88" />
      <el-table-column label="栏目" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ joinCsvList(row.moduleNames) }}</template>
      </el-table-column>
      <el-table-column prop="titleKeyword" label="标题关键词" width="100" />
      <el-table-column prop="sort" label="排序" width="72" align="center" />
      <el-table-column label="状态" width="88" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="canEdit" label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link @click="previewRow(row)">预览</el-button>
          <el-button v-if="row.status === 1" link type="warning" @click="toggleStatus(row, 0)">禁用</el-button>
          <el-button v-else link type="success" @click="toggleStatus(row, 1)">启用</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="编辑 Tab 配置" width="640px" destroy-on-close>
      <el-form label-width="108px" @submit.prevent="save">
        <el-form-item label="展示名"><el-input v-model="form.tabLabel" /></el-form-item>
        <el-form-item label="查询模式">
          <el-select v-model="form.queryMode" style="width: 100%">
            <el-option label="resource" value="resource" />
            <el-option label="suite" value="suite" />
          </el-select>
        </el-form-item>
        <el-form-item label="栏目 module">
          <el-input v-model="form.moduleNamesText" placeholder="逗号分隔，如 同步备课,期中" />
        </el-form-item>
        <el-form-item label="排除栏目">
          <el-input v-model="form.excludeModuleNamesText" placeholder="逗号分隔" />
        </el-form-item>
        <el-form-item label="资料类型">
          <el-input v-model="form.resourceTypeNamesText" placeholder="逗号分隔，如 课件,教案" />
        </el-form-item>
        <el-form-item label="标题关键词"><el-input v-model="form.titleKeyword" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" :max="999" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="canEdit" type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="Tab 预览（前 8 条）" width="560px">
      <el-table :data="previewItems" border size="small" empty-text="无数据">
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="date" label="日期" width="110" />
        <el-table-column prop="source" label="来源" width="120" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAdminAuthStore } from '@/admin/store/adminAuth'
import {
  adminHomePanelApi,
  joinCsvList,
  PANEL_CODE_OPTIONS,
  splitCsvList,
  type HomePanelTabItem,
} from '@/admin/api/homePanels'

const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:home:edit'))

const panelOptions = PANEL_CODE_OPTIONS
const panelCode = ref('sync_prep')
const loading = ref(false)
const saving = ref(false)
const showDisabled = ref(true)
const records = ref<HomePanelTabItem[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const previewVisible = ref(false)
const previewItems = ref<{ title: string; date?: string; source?: string }[]>([])

const form = reactive({
  tabLabel: '',
  queryMode: 'resource',
  moduleNamesText: '',
  excludeModuleNamesText: '',
  resourceTypeNamesText: '',
  titleKeyword: '',
  sort: 0,
})

async function load() {
  loading.value = true
  try {
    records.value = (await adminHomePanelApi.listTabs(panelCode.value, showDisabled.value)) ?? []
  } catch (e) {
    ElMessage.error(String(e))
  } finally {
    loading.value = false
  }
}

function openEdit(row: HomePanelTabItem) {
  editingId.value = row.id
  form.tabLabel = row.tabLabel ?? ''
  form.queryMode = row.queryMode ?? 'resource'
  form.moduleNamesText = joinCsvList(row.moduleNames)
  form.excludeModuleNamesText = joinCsvList(row.excludeModuleNames)
  form.resourceTypeNamesText = joinCsvList(row.resourceTypeNames)
  form.titleKeyword = row.titleKeyword ?? ''
  form.sort = row.sort ?? 0
  dialogVisible.value = true
}

async function save() {
  if (!editingId.value) return
  saving.value = true
  try {
    await adminHomePanelApi.updateTab(editingId.value, {
      tabLabel: form.tabLabel,
      queryMode: form.queryMode,
      moduleNames: splitCsvList(form.moduleNamesText),
      excludeModuleNames: splitCsvList(form.excludeModuleNamesText),
      resourceTypeNames: splitCsvList(form.resourceTypeNamesText),
      titleKeyword: form.titleKeyword || undefined,
      sort: form.sort,
    })
    ElMessage.success('已保存')
    dialogVisible.value = false
    await load()
  } catch (e) {
    ElMessage.error(String(e))
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: HomePanelTabItem, status: 0 | 1) {
  try {
    await adminHomePanelApi.setTabStatus(row.id, status)
    ElMessage.success(status === 1 ? '已启用' : '已禁用')
    await load()
  } catch (e) {
    ElMessage.error(String(e))
  }
}

async function previewRow(row: HomePanelTabItem) {
  try {
    const params: Record<string, string | number> = {
      panelCode: row.panelCode,
      tabKey: row.tabKey,
      limit: 8,
    }
    if (row.filterKey) params.filterKey = row.filterKey
    if (row.panelCode === 'sync_prep') {
      params.stageKey = 'primary'
      params.subjectName = '语文'
    } else if (row.panelCode === 'paper_zone') {
      params.stageKey = 'primary'
      params.gradeName = '六年级下册'
    } else if (row.panelCode === 'promotion') {
      params.filterKey = row.filterKey ?? '真题'
    }
    const data = await adminHomePanelApi.preview(params as never)
    previewItems.value = data?.items ?? []
    previewVisible.value = true
  } catch (e) {
    ElMessage.error(String(e))
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
