<template>
  <AdminPageShell title="分类目录" :desc="pageDesc">
    <el-alert
      v-if="!canEdit"
      type="info"
      :closable="false"
      show-icon
      class="taxonomy__hint"
      title="当前为只读模式"
      description="可查看各维度配置；新增、编辑、启用/禁用、删除需 content_admin 或 super_admin 权限。"
    />

    <el-tabs v-model="activeTab" class="taxonomy-tabs" @tab-change="onTabChange">
      <el-tab-pane label="学段" name="stages" />
      <el-tab-pane label="学科" name="subjects" />
      <el-tab-pane label="教材版本" name="editions" />
      <el-tab-pane label="年级" name="grades" />
      <el-tab-pane label="册别" name="volumes" />
      <el-tab-pane label="栏目" name="modules" />
      <el-tab-pane label="资源类型" name="resourceTypes" />
    </el-tabs>

    <div class="taxonomy__toolbar">
      <el-select
        v-if="needsStageFilter"
        v-model="filterStageId"
        clearable
        placeholder="按学段筛选"
        style="width: 180px"
        @change="loadCurrent"
      >
        <el-option v-for="s in stages" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-select
        v-if="activeTab === 'resourceTypes'"
        v-model="filterParentId"
        clearable
        placeholder="按父级筛选"
        style="width: 200px"
        @change="loadCurrent"
      >
        <el-option label="一级分组" :value="0" />
        <el-option v-for="g in typeGroups" :key="g.id" :label="g.name" :value="g.id" />
      </el-select>
      <el-checkbox v-if="hasStatusColumn" v-model="showDisabled" @change="loadCurrent">
        显示已禁用
      </el-checkbox>
      <el-button v-if="canEdit" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" border stripe empty-text="暂无数据">
      <template v-if="activeTab === 'stages'">
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="icon" label="图标" width="72" align="center" />
        <el-table-column prop="sort" label="排序" width="72" align="center" />
      </template>

      <template v-else-if="activeTab === 'subjects'">
        <el-table-column prop="stageName" label="学段" width="100" />
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column label="绑定版本" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ formatEditionIds(row.editionIds) }}</template>
        </el-table-column>
        <el-table-column label="栏目" width="72" align="center">
          <template #default="{ row }">{{ (row.moduleIds as number[] | undefined)?.length ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="资料类型" width="88" align="center">
          <template #default="{ row }">{{ (row.resourceTypeIds as number[] | undefined)?.length ?? 0 }}</template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="72" align="center" />
      </template>

      <template v-else-if="activeTab === 'editions'">
        <el-table-column prop="code" label="编码" width="130" />
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="shortName" label="简称" width="90" />
        <el-table-column label="学段" width="80">
          <template #default="{ row }">{{ stageNameOf(row.stageId) }}</template>
        </el-table-column>
        <el-table-column label="学科" width="80">
          <template #default="{ row }">{{ subjectNameOf(row.subjectId) }}</template>
        </el-table-column>
        <el-table-column prop="publisher" label="出版社" min-width="140" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="72" align="center" />
      </template>

      <template v-else-if="activeTab === 'grades'">
        <el-table-column label="学段" width="100">
          <template #default="{ row }">{{ stageNameOf(row.stageId) }}</template>
        </el-table-column>
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="sort" label="排序" width="72" align="center" />
      </template>

      <template v-else-if="activeTab === 'volumes'">
        <el-table-column prop="code" label="编码" width="100" />
        <el-table-column prop="name" label="名称" min-width="100" />
        <el-table-column label="学段" width="80">
          <template #default="{ row }">{{ stageNameOf(row.stageId) }}</template>
        </el-table-column>
        <el-table-column label="学科" width="80">
          <template #default="{ row }">{{ subjectNameOf(row.subjectId) }}</template>
        </el-table-column>
        <el-table-column label="版本" width="110">
          <template #default="{ row }">{{ editionNameOf(row.editionId) }}</template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="72" align="center" />
      </template>

      <template v-else-if="activeTab === 'modules'">
        <el-table-column prop="code" label="编码" width="140" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="moduleCategory" label="分类" width="110" />
        <el-table-column label="适用学段" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.stageNames?.length ? row.stageNames.join('、') : '全学段' }}
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="72" align="center" />
      </template>

      <template v-else-if="activeTab === 'resourceTypes'">
        <el-table-column prop="parentId" label="父级ID" width="80" align="center" />
        <el-table-column prop="code" label="编码" width="140" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="groupName" label="分组" width="110" />
        <el-table-column prop="sort" label="排序" width="72" align="center" />
      </template>

      <el-table-column v-if="hasStatusColumn" label="状态" width="88" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column v-if="canEdit" label="操作" :width="activeTab === 'volumes' ? 160 : 220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button
            v-if="hasStatusColumn"
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="toggleStatus(row, row.status === 1 ? 0 : 1)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button link type="danger" @click="removeRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form label-width="96px" @submit.prevent="save">
        <template v-if="activeTab === 'stages'">
          <el-form-item label="编码" required><el-input v-model="form.code" /></el-form-item>
          <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="图标"><el-input v-model="form.icon" placeholder="emoji" /></el-form-item>
        </template>

        <template v-else-if="activeTab === 'subjects'">
          <el-form-item label="学段" required>
            <el-select v-model="form.stageId" style="width: 100%">
              <el-option v-for="s in stages" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="编码" required><el-input v-model="form.code" /></el-form-item>
          <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="绑定版本">
            <el-select v-model="form.editionIds" multiple filterable style="width: 100%">
              <el-option v-for="e in editions" :key="e.id" :label="e.name" :value="e.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="绑定栏目">
            <el-select v-model="form.moduleIds" multiple filterable style="width: 100%" placeholder="留空=该学段全部栏目">
              <el-option v-for="m in stageModulesForForm" :key="m.id" :label="m.name" :value="m.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="同步资料类型">
            <el-select v-model="form.resourceTypeIds" multiple filterable style="width: 100%" placeholder="留空=teach 分组默认">
              <el-option v-for="t in teachLeafTypes" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
          </el-form-item>
        </template>

        <template v-else-if="activeTab === 'editions'">
          <el-form-item label="学段">
            <el-select v-model="form.stageId" clearable placeholder="通用（不限制）" style="width:100%" @change="onEditionStageChange">
              <el-option v-for="s in stages" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="学科">
            <el-select v-model="form.subjectId" clearable placeholder="通用（不限制）" style="width:100%" :disabled="!form.stageId">
              <el-option v-for="s in editionSubjects" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="编码" required><el-input v-model="form.code" /></el-form-item>
          <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="简称"><el-input v-model="form.shortName" /></el-form-item>
          <el-form-item label="出版社"><el-input v-model="form.publisher" /></el-form-item>
        </template>

        <template v-else-if="activeTab === 'grades'">
          <el-form-item label="学段" required>
            <el-select v-model="form.stageId" style="width: 100%">
              <el-option v-for="s in stages" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="编码"><el-input v-model="form.code" /></el-form-item>
          <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
        </template>

        <template v-else-if="activeTab === 'volumes'">
          <el-form-item label="学段">
            <el-select v-model="form.stageId" clearable placeholder="通用" style="width:100%" @change="onVolumeStageChange">
              <el-option v-for="s in stages" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="学科">
            <el-select v-model="form.subjectId" clearable placeholder="通用" style="width:100%" :disabled="!form.stageId">
              <el-option v-for="s in volumeSubjects" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="版本">
            <el-select v-model="form.editionId" clearable placeholder="通用" style="width:100%" :disabled="!form.subjectId">
              <el-option v-for="e in volumeEditions" :key="e.id" :label="e.name" :value="e.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="编码" required><el-input v-model="form.code" /></el-form-item>
          <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
        </template>

        <template v-else-if="activeTab === 'modules'">
          <el-form-item label="编码" required><el-input v-model="form.code" /></el-form-item>
          <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="分类"><el-input v-model="form.moduleCategory" placeholder="sync / exam" /></el-form-item>
          <el-form-item label="适用学段">
            <el-select v-model="form.stageIds" multiple filterable style="width: 100%" placeholder="留空=全学段">
              <el-option v-for="s in stages" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
        </template>

        <template v-else-if="activeTab === 'resourceTypes'">
          <el-form-item label="父级" required>
            <el-select v-model="form.parentId" style="width: 100%">
              <el-option v-for="g in typeGroups" :key="g.id" :label="g.name" :value="g.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="编码" required><el-input v-model="form.code" /></el-form-item>
          <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="分组编码"><el-input v-model="form.groupCode" /></el-form-item>
          <el-form-item label="分组名称"><el-input v-model="form.groupName" /></el-form-item>
        </template>

        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item v-if="hasStatusColumn" label="状态">
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
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AdminPageShell from '@/admin/components/AdminPageShell.vue'
import {
  adminTaxonomyApi,
  type TaxonomyEditionItem,
  type TaxonomyModuleItem,
  type TaxonomyResourceTypeItem,
  type TaxonomyStageItem,
  type TaxonomySubjectItem,
  type TaxonomyVolumeItem,
} from '@/admin/api/taxonomy'
import { useAdminAuthStore } from '@/admin/store/adminAuth'
import { invalidateTaxonomyCache } from '@/composables/taxonomySource'

type TabName = 'stages' | 'subjects' | 'editions' | 'grades' | 'volumes' | 'modules' | 'resourceTypes'

interface TaxonomyForm {
  code?: string
  name?: string
  icon?: string
  stageId?: number
  subjectId?: number
  editionId?: number
  editionIds?: number[]
  moduleIds?: number[]
  resourceTypeIds?: number[]
  shortName?: string
  publisher?: string
  moduleCategory?: string
  stageIds?: number[]
  parentId?: number
  groupCode?: string
  groupName?: string
  sort?: number
  status?: number
}

const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:taxonomy:edit'))
const pageDesc = computed(() =>
  canEdit.value
    ? '维护学段、学科、版本、年级、册别、栏目与资源类型；学科可绑定栏目与同步资料类型（Phase 5-F）；修改后 C 端 /api/taxonomy/* 与 /api/home/subject-nav 立即生效。'
    : '查看平台分类维度配置（只读）。',
)

const activeTab = ref<TabName>('stages')
const loading = ref(false)
const saving = ref(false)
const showDisabled = ref(true)
const filterStageId = ref<number | undefined>()
const filterParentId = ref<number | undefined>()
const records = ref<Record<string, unknown>[]>([])
const stages = ref<TaxonomyStageItem[]>([])
const editions = ref<TaxonomyEditionItem[]>([])
const typeGroups = ref<{ id: number; name: string }[]>([])
const stageModulesForForm = ref<TaxonomyModuleItem[]>([])
const teachLeafTypes = ref<TaxonomyResourceTypeItem[]>([])

/** Phase 6：级联选择辅助数据 */
const subjectCache = ref<TaxonomySubjectItem[]>([])
const editionSubjects = ref<TaxonomySubjectItem[]>([])
const volumeSubjects = ref<TaxonomySubjectItem[]>([])
const volumeEditions = ref<TaxonomyEditionItem[]>([])

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<TaxonomyForm>({})
const formEnabled = ref(true)

const needsStageFilter = computed(() => ['subjects', 'grades', 'modules'].includes(activeTab.value))
const hasStatusColumn = computed(() => true)
const dialogTitle = computed(() => `${editingId.value ? '编辑' : '新增'}${tabLabel(activeTab.value)}`)

function tabLabel(tab: TabName) {
  const map: Record<TabName, string> = {
    stages: '学段',
    subjects: '学科',
    editions: '教材版本',
    grades: '年级',
    volumes: '册别',
    modules: '栏目',
    resourceTypes: '资源类型',
  }
  return map[tab]
}

function stageNameOf(stageId?: number) {
  return stages.value.find((s) => s.id === stageId)?.name || '-'
}

function subjectNameOf(subjectId?: number) {
  if (!subjectId) return '通用'
  return subjectCache.value.find((s) => s.id === subjectId)?.name || '-'
}

function editionNameOf(editionId?: number) {
  if (!editionId) return '通用'
  return editions.value.find((e) => e.id === editionId)?.name || '-'
}

async function loadSubjectCache() {
  subjectCache.value = await adminTaxonomyApi.listSubjects(undefined, true)
}

async function onEditionStageChange(stageId?: number) {
  form.subjectId = undefined
  if (stageId) {
    editionSubjects.value = subjectCache.value.filter((s) => s.stageId === stageId)
  } else {
    editionSubjects.value = []
  }
}

async function onVolumeStageChange(stageId?: number) {
  form.subjectId = undefined
  form.editionId = undefined
  if (stageId) {
    volumeSubjects.value = subjectCache.value.filter((s) => s.stageId === stageId)
  } else {
    volumeSubjects.value = []
    volumeEditions.value = []
  }
}

watch(
  () => form.subjectId,
  async (subjectId) => {
    if (activeTab.value === 'volumes' && dialogVisible.value) {
      if (subjectId) {
        volumeEditions.value = await adminTaxonomyApi.listEditions(form.stageId, subjectId, true)
      } else {
        volumeEditions.value = []
      }
    }
  },
)

function formatEditionIds(ids?: number[]) {
  if (!ids?.length) return '-'
  return ids.map((id) => editions.value.find((e) => e.id === id)?.name || `#${id}`).join('、')
}

async function loadTeachLeafTypes() {
  const all = await adminTaxonomyApi.listResourceTypes(undefined, true)
  teachLeafTypes.value = all.filter((t) => (t.parentId ?? 0) > 0 && t.groupCode === 'teach')
}

async function loadStageModulesForForm(stageId?: number) {
  if (!stageId) {
    stageModulesForForm.value = []
    return
  }
  stageModulesForForm.value = await adminTaxonomyApi.listModules(stageId, true)
}

watch(
  () => form.stageId,
  (stageId) => {
    if (activeTab.value === 'subjects' && dialogVisible.value) {
      void loadStageModulesForForm(stageId)
    }
  },
)

async function loadStages() {
  stages.value = await adminTaxonomyApi.listStages(true)
}

async function loadEditions() {
  editions.value = await adminTaxonomyApi.listEditions(undefined, undefined, true)
}

async function loadTypeGroups() {
  const groups = await adminTaxonomyApi.listResourceTypes(undefined, true)
  typeGroups.value = groups.filter((g) => !g.parentId || g.parentId === 0).map((g) => ({ id: g.id, name: g.name }))
}

async function loadCurrent() {
  loading.value = true
  try {
    switch (activeTab.value) {
      case 'stages':
        records.value = (await adminTaxonomyApi.listStages(showDisabled.value)) as unknown as Record<string, unknown>[]
        break
      case 'subjects':
        records.value = (await adminTaxonomyApi.listSubjects(filterStageId.value, showDisabled.value)) as unknown as Record<string, unknown>[]
        break
      case 'editions':
        records.value = (await adminTaxonomyApi.listEditions(undefined, undefined, showDisabled.value)) as unknown as Record<string, unknown>[]
        break
      case 'grades':
        records.value = (await adminTaxonomyApi.listGrades(filterStageId.value, showDisabled.value)) as unknown as Record<string, unknown>[]
        break
      case 'volumes':
        records.value = (await adminTaxonomyApi.listVolumes(undefined, undefined, undefined, showDisabled.value)) as unknown as Record<string, unknown>[]
        break
      case 'modules':
        records.value = (await adminTaxonomyApi.listModules(filterStageId.value, showDisabled.value)) as unknown as Record<string, unknown>[]
        break
      case 'resourceTypes':
        records.value = (await adminTaxonomyApi.listResourceTypes(filterParentId.value, showDisabled.value)) as unknown as Record<string, unknown>[]
        break
    }
  } catch {
    records.value = []
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function onTabChange() {
  filterStageId.value = undefined
  filterParentId.value = undefined
  void loadCurrent()
}

function resetForm() {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, {
    sort: 0,
    status: 1,
    code: '',
    name: '',
    icon: '',
    editionIds: [] as number[],
    moduleIds: [] as number[],
    resourceTypeIds: [] as number[],
    stageIds: [] as number[],
    stageId: filterStageId.value || stages.value[0]?.id,
    parentId: typeGroups.value[0]?.id || 1,
    moduleCategory: 'sync',
    groupCode: '',
    groupName: '',
    shortName: '',
    publisher: '',
  })
}

function openCreate() {
  editingId.value = null
  resetForm()
  formEnabled.value = true
  if (activeTab.value === 'subjects') {
    void loadStageModulesForForm(form.stageId)
  }
  dialogVisible.value = true
}

function openEdit(row: Record<string, unknown>) {
  editingId.value = row.id as number
  resetForm()
  Object.assign(form, row, {
    editionIds: [...((row.editionIds as number[]) || [])],
    moduleIds: [...((row.moduleIds as number[]) || [])],
    resourceTypeIds: [...((row.resourceTypeIds as number[]) || [])],
    stageIds: [...((row.stageIds as number[]) || [])],
  })
  formEnabled.value = (row.status as number) !== 0
  if (activeTab.value === 'subjects') {
    void loadStageModulesForForm(form.stageId)
  }
  dialogVisible.value = true
}

async function save() {
  saving.value = true
  const payload = { ...form, status: formEnabled.value ? 1 : 0 }
  try {
    const id = editingId.value
    const api = adminTaxonomyApi
    switch (activeTab.value) {
      case 'stages':
        if (id) await api.updateStage(id, payload as never)
        else await api.createStage(payload as never)
        break
      case 'subjects':
        if (id) await api.updateSubject(id, payload as never)
        else await api.createSubject(payload as never)
        break
      case 'editions':
        if (id) await api.updateEdition(id, payload as never)
        else await api.createEdition(payload as never)
        break
      case 'grades':
        if (id) await api.updateGrade(id, payload as never)
        else await api.createGrade(payload as never)
        break
      case 'volumes':
        if (id) await api.updateVolume(id, payload as never)
        else await api.createVolume(payload as never)
        break
      case 'modules':
        if (id) await api.updateModule(id, payload as never)
        else await api.createModule(payload as never)
        break
      case 'resourceTypes':
        if (id) await api.updateResourceType(id, payload as never)
        else await api.createResourceType(payload as never)
        break
    }
    ElMessage.success('保存成功')
    invalidateTaxonomyCache()
    dialogVisible.value = false
    await loadCurrent()
    if (['stages', 'subjects', 'grades', 'modules'].includes(activeTab.value)) await loadStages()
    if (activeTab.value === 'subjects') await loadEditions()
    if (activeTab.value === 'resourceTypes') await loadTypeGroups()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: { id: number }, status: 0 | 1) {
  const api = adminTaxonomyApi
  try {
    switch (activeTab.value) {
      case 'stages':
        await api.setStageStatus(row.id, status)
        break
      case 'subjects':
        await api.setSubjectStatus(row.id, status)
        break
      case 'editions':
        await api.setEditionStatus(row.id, status)
        break
      case 'grades':
        await api.setGradeStatus(row.id, status)
        break
      case 'volumes':
        await api.setVolumeStatus(row.id, status)
        break
      case 'modules':
        await api.setModuleStatus(row.id, status)
        break
      case 'resourceTypes':
        await api.setResourceTypeStatus(row.id, status)
        break
    }
    ElMessage.success(status === 1 ? '已启用' : '已禁用')
    await loadCurrent()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function removeRow(row: { id: number; name?: string }) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.name || row.id}」？`, '确认删除', { type: 'warning' })
    const api = adminTaxonomyApi
    switch (activeTab.value) {
      case 'stages':
        await api.deleteStage(row.id)
        break
      case 'subjects':
        await api.deleteSubject(row.id)
        break
      case 'editions':
        await api.deleteEdition(row.id)
        break
      case 'grades':
        await api.deleteGrade(row.id)
        break
      case 'volumes':
        await api.deleteVolume(row.id)
        break
      case 'modules':
        await api.deleteModule(row.id)
        break
      case 'resourceTypes':
        await api.deleteResourceType(row.id)
        break
    }
    ElMessage.success('已删除')
    await loadCurrent()
  } catch (err: any) {
    // Phase 6：删除被资源引用拦截，提示改为禁用
    const msg = err?.response?.data?.message || err?.message || ''
    if (msg.includes('已被') && msg.includes('引用')) {
      try {
        await ElMessageBox.alert(msg, '删除被拒绝', {
          type: 'warning',
          confirmButtonText: '知道了',
          showCancelButton: true,
          cancelButtonText: '改为禁用',
        })
      } catch {
        // 用户点了"改为禁用"
        await toggleStatus(row, 0)
      }
    }
  }
}

onMounted(async () => {
  await loadStages()
  await loadEditions()
  await loadTypeGroups()
  await loadTeachLeafTypes()
  await loadSubjectCache()
  await loadCurrent()
})
</script>

<style scoped>
.taxonomy__hint {
  margin-bottom: 16px;
}
.taxonomy-tabs {
  margin-bottom: 12px;
}
.taxonomy__toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
