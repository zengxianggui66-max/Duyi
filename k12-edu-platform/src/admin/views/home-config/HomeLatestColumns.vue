<template>
  <div>
    <el-alert
      v-if="!canEdit"
      type="info"
      :closable="false"
      show-icon
      class="home-ops__hint"
      title="当前为只读模式"
      description="可查看最新内容三列配置；编辑需 content_admin 或 super_admin 权限。"
    />

    <div class="home-ops__toolbar">
      <el-checkbox v-model="showDisabled" @change="load">显示已禁用</el-checkbox>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="records" border stripe empty-text="暂无数据">
      <el-table-column prop="columnKey" label="Key" width="100" />
      <el-table-column prop="title" label="列标题" min-width="100" />
      <el-table-column prop="morePath" label="更多链接" min-width="120" show-overflow-tooltip />
      <el-table-column prop="dataSource" label="数据源" width="88" />
      <el-table-column label="规则摘要" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.dataSource === 'api'">article 服务</span>
          <span v-else-if="row.dataSource === 'manual'">手工维护</span>
          <span v-else>{{ ruleSummary(row.rule) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="72" align="center" />
      <el-table-column label="状态" width="88" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="canEdit" label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link @click="previewRow(row)">预览</el-button>
          <el-button v-if="row.dataSource === 'manual'" link @click="openItems(row)">条目</el-button>
          <el-button v-if="row.status === 1" link type="warning" @click="toggleStatus(row, 0)">禁用</el-button>
          <el-button v-else link type="success" @click="toggleStatus(row, 1)">启用</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="编辑最新内容列" width="640px" destroy-on-close>
      <el-form label-width="108px" @submit.prevent="save">
        <el-form-item label="列标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="更多链接"><el-input v-model="form.morePath" placeholder="/resource/sync" /></el-form-item>
        <el-form-item label="数据源">
          <el-select v-model="form.dataSource" style="width: 100%">
            <el-option label="规则查询 rule" value="rule" />
            <el-option label="手工维护 manual" value="manual" />
            <el-option label="资讯 API api" value="api" />
          </el-select>
        </el-form-item>
        <template v-if="form.dataSource === 'rule'">
          <el-form-item label="学段 stageKey">
            <el-input v-model="form.stageKey" placeholder="留空则 C 端传参或不限" />
          </el-form-item>
          <el-form-item label="学科">
            <el-input v-model="form.subjectName" placeholder="可选" />
          </el-form-item>
          <el-form-item label="栏目 module">
            <el-input v-model="form.moduleNamesText" placeholder="逗号分隔，如 试卷" />
          </el-form-item>
          <el-form-item label="排除栏目">
            <el-input v-model="form.excludeModuleNamesText" placeholder="逗号分隔" />
          </el-form-item>
          <el-form-item label="资料类型">
            <el-input v-model="form.resourceTypeNamesText" placeholder="逗号分隔，如 课件" />
          </el-form-item>
          <el-form-item label="标题关键词"><el-input v-model="form.titleKeyword" /></el-form-item>
          <el-form-item label="条数"><el-input-number v-model="form.limit" :min="1" :max="20" /></el-form-item>
        </template>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" :max="999" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="canEdit" type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="列预览（前 8 条）" width="560px">
      <el-table :data="previewItems" border size="small" empty-text="无数据（api 列请在前台查看资讯）">
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="date" label="日期" width="110" />
        <el-table-column prop="itemType" label="类型" width="88" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="itemsVisible" :title="`手工条目 · ${itemsColumn?.title ?? ''}`" width="720px" destroy-on-close>
      <div v-if="canEdit" class="home-ops__toolbar">
        <el-button type="primary" @click="openItemForm()">新增条目</el-button>
      </div>
      <el-table v-loading="itemsLoading" :data="manualItems" border size="small" empty-text="暂无手工条目">
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="date" label="日期" width="110" />
        <el-table-column prop="itemType" label="类型" width="88" />
        <el-table-column v-if="canEdit" label="操作" width="140">
          <template #default="{ row }">
            <el-button link type="primary" @click="openItemForm(row)">编辑</el-button>
            <el-button link type="danger" @click="removeItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="itemFormVisible" :title="itemEditingId ? '编辑条目' : '新增条目'" width="520px" append-to-body>
        <el-form label-width="96px">
          <el-form-item label="标题"><el-input v-model="itemForm.title" /></el-form-item>
          <el-form-item label="日期"><el-input v-model="itemForm.itemDate" placeholder="yyyy-MM-dd" /></el-form-item>
          <el-form-item label="资源 ID"><el-input-number v-model="itemForm.resourceId" :min="1" controls-position="right" /></el-form-item>
          <el-form-item label="资源来源">
            <el-select v-model="itemForm.resourceSource" style="width: 100%" clearable>
              <el-option label="edu_resource" value="edu_resource" />
              <el-option label="oss_primary_chinese" value="oss_primary_chinese" />
              <el-option label="edu_resource_suite" value="edu_resource_suite" />
            </el-select>
          </el-form-item>
          <el-form-item label="资讯 ID"><el-input-number v-model="itemForm.articleId" :min="1" controls-position="right" /></el-form-item>
          <el-form-item label="自定义链接"><el-input v-model="itemForm.linkPath" placeholder="/news/1 或 /resource/1" /></el-form-item>
          <el-form-item label="排序"><el-input-number v-model="itemForm.sort" :min="0" :max="999" /></el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="itemFormVisible = false">取消</el-button>
          <el-button type="primary" :loading="itemSaving" @click="saveItem">保存</el-button>
        </template>
      </el-dialog>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminAuthStore } from '@/admin/store/adminAuth'
import {
  adminHomeLatestApi,
  joinCsvList,
  splitCsvList,
  type HomeLatestColumnItem,
  type HomeLatestListItem,
  type HomeLatestRule,
} from '@/admin/api/homeLatestColumns'

const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:home:edit'))

const loading = ref(false)
const saving = ref(false)
const showDisabled = ref(true)
const records = ref<HomeLatestColumnItem[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const previewVisible = ref(false)
const previewItems = ref<HomeLatestListItem[]>([])

const itemsVisible = ref(false)
const itemsLoading = ref(false)
const itemsColumn = ref<HomeLatestColumnItem | null>(null)
const manualItems = ref<HomeLatestListItem[]>([])

const itemFormVisible = ref(false)
const itemSaving = ref(false)
const itemEditingId = ref<number | null>(null)
const itemForm = reactive({
  title: '',
  itemDate: '',
  resourceId: undefined as number | undefined,
  resourceSource: '',
  articleId: undefined as number | undefined,
  linkPath: '',
  sort: 0,
})

const form = reactive({
  title: '',
  morePath: '',
  dataSource: 'rule' as 'rule' | 'manual' | 'api',
  stageKey: '',
  subjectName: '',
  moduleNamesText: '',
  excludeModuleNamesText: '',
  resourceTypeNamesText: '',
  titleKeyword: '',
  limit: 8,
  sort: 0,
})

function ruleSummary(rule?: HomeLatestRule | null) {
  if (!rule) return '-'
  const parts: string[] = []
  if (rule.moduleNames?.length) parts.push(`栏目:${rule.moduleNames.join('/')}`)
  if (rule.resourceTypeNames?.length) parts.push(`类型:${rule.resourceTypeNames.join('/')}`)
  if (rule.titleKeyword) parts.push(`关键词:${rule.titleKeyword}`)
  parts.push(`limit=${rule.limit ?? 8}`)
  return parts.join(' ')
}

async function load() {
  loading.value = true
  try {
    records.value = (await adminHomeLatestApi.listColumns(showDisabled.value)) ?? []
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function openEdit(row: HomeLatestColumnItem) {
  editingId.value = row.id
  form.title = row.title
  form.morePath = row.morePath
  form.dataSource = row.dataSource
  form.stageKey = row.rule?.stageKey ?? ''
  form.subjectName = row.rule?.subjectName ?? ''
  form.moduleNamesText = joinCsvList(row.rule?.moduleNames)
  form.excludeModuleNamesText = joinCsvList(row.rule?.excludeModuleNames)
  form.resourceTypeNamesText = joinCsvList(row.rule?.resourceTypeNames)
  form.titleKeyword = row.rule?.titleKeyword ?? ''
  form.limit = row.rule?.limit ?? 8
  form.sort = row.sort ?? 0
  dialogVisible.value = true
}

async function save() {
  if (!editingId.value) return
  saving.value = true
  try {
    const rule: HomeLatestRule | null =
      form.dataSource === 'rule'
        ? {
            stageKey: form.stageKey || undefined,
            subjectName: form.subjectName || undefined,
            moduleNames: splitCsvList(form.moduleNamesText),
            excludeModuleNames: splitCsvList(form.excludeModuleNamesText),
            resourceTypeNames: splitCsvList(form.resourceTypeNamesText),
            titleKeyword: form.titleKeyword || undefined,
            limit: form.limit,
            orderBy: 'upload_time_desc',
          }
        : null
    await adminHomeLatestApi.updateColumn(editingId.value, {
      title: form.title,
      morePath: form.morePath,
      dataSource: form.dataSource,
      rule,
      sort: form.sort,
      status: 1,
    })
    ElMessage.success('已保存')
    dialogVisible.value = false
    await load()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '保存失败')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: HomeLatestColumnItem, status: 0 | 1) {
  try {
    await adminHomeLatestApi.setColumnStatus(row.id, status)
    ElMessage.success(status === 1 ? '已启用' : '已禁用')
    await load()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function previewRow(row: HomeLatestColumnItem) {
  try {
    previewItems.value = (await adminHomeLatestApi.preview(row.id, 'primary')) ?? []
    previewVisible.value = true
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '预览失败')
  }
}

async function openItems(row: HomeLatestColumnItem) {
  itemsColumn.value = row
  itemsVisible.value = true
  itemsLoading.value = true
  try {
    manualItems.value = (await adminHomeLatestApi.listManualItems(row.id)) ?? []
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载条目失败')
  } finally {
    itemsLoading.value = false
  }
}

function openItemForm(row?: HomeLatestListItem) {
  itemEditingId.value = row?.id ?? null
  itemForm.title = row?.title ?? ''
  itemForm.itemDate = row?.date ?? ''
  itemForm.resourceId = row?.resourceId
  itemForm.resourceSource = row?.resourceSource ?? ''
  itemForm.articleId = row?.articleId
  itemForm.linkPath = row?.linkPath ?? ''
  itemForm.sort = 0
  itemFormVisible.value = true
}

async function saveItem() {
  if (!itemsColumn.value) return
  itemSaving.value = true
  const payload = {
    title: itemForm.title,
    itemDate: itemForm.itemDate || undefined,
    resourceId: itemForm.resourceId,
    resourceSource: itemForm.resourceSource || undefined,
    articleId: itemForm.articleId,
    linkPath: itemForm.linkPath || undefined,
    sort: itemForm.sort,
    status: 1 as const,
  }
  try {
    if (itemEditingId.value) {
      await adminHomeLatestApi.updateManualItem(itemEditingId.value, payload)
    } else {
      await adminHomeLatestApi.createManualItem(itemsColumn.value.id, payload)
    }
    ElMessage.success('条目已保存')
    itemFormVisible.value = false
    manualItems.value = (await adminHomeLatestApi.listManualItems(itemsColumn.value.id)) ?? []
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '保存失败')
  } finally {
    itemSaving.value = false
  }
}

async function removeItem(row: HomeLatestListItem) {
  if (!row.id || !itemsColumn.value) return
  await ElMessageBox.confirm('确定删除该条目？', '确认')
  try {
    await adminHomeLatestApi.deleteManualItem(row.id)
    ElMessage.success('已删除')
    manualItems.value = (await adminHomeLatestApi.listManualItems(itemsColumn.value.id)) ?? []
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '删除失败')
  }
}

onMounted(load)
</script>

<style scoped>
.home-ops__hint {
  margin-bottom: 16px;
}
.home-ops__toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
