<template>
  <div class="content-package-crud">
    <el-alert
      v-if="!canEdit"
      type="info"
      :closable="false"
      show-icon
      class="content-package-crud__hint"
      title="当前为只读模式"
      :description="`可查看${entityLabel}；增删改需 admin:content:edit 权限。资源文件请在「资源中心」按 sourceType 管理。`"
    />

    <div class="content-package-crud__toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索标题/简介"
        clearable
        style="width: 220px"
        @keyup.enter="reload"
      />
      <el-checkbox v-model="showDisabled" @change="reload">显示已禁用</el-checkbox>
      <el-button type="primary" @click="reload">查询</el-button>
      <el-button v-if="canEdit" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增{{ entityLabel }}
      </el-button>
      <el-button link type="primary" @click="goResources">管理关联资源 →</el-button>
      <el-button link @click="openPublic">前台预览 ↗</el-button>
    </div>

    <el-table v-loading="loading" :data="records" border stripe empty-text="暂无数据">
      <el-table-column prop="title" :label="`${entityLabel}标题`" min-width="180" />
      <el-table-column v-if="showCategory" prop="category" label="分类" width="100" />
      <el-table-column v-if="showRegion" prop="region" label="地域" width="90" />
      <el-table-column v-if="showGradeStage" prop="gradeStage" label="学段" width="90" />
      <el-table-column v-if="showCultureFields" prop="location" label="地点" width="120" show-overflow-tooltip />
      <el-table-column prop="resourceCount" label="资源数" width="80" align="center" />
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

    <div class="content-package-crud__pager">
      <el-pagination
        v-model:current-page="current"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="load"
        @size-change="reload"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form label-width="96px" @submit.prevent="save">
        <el-form-item label="标题" required><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.summary" type="textarea" :rows="2" /></el-form-item>
        <el-form-item v-if="showCategory" label="分类"><el-input v-model="form.category" placeholder="elite / math ..." /></el-form-item>
        <el-form-item v-if="showRegion" label="地域"><el-input v-model="form.region" placeholder="all / chengdu / sichuan" /></el-form-item>
        <el-form-item v-if="showGradeStage" label="学段"><el-input v-model="form.gradeStage" placeholder="all / primary / junior" /></el-form-item>
        <template v-if="showCultureFields">
          <el-form-item label="时长类型"><el-input v-model="form.durationType" placeholder="half_day / one_day" /></el-form-item>
          <el-form-item label="时长标签"><el-input v-model="form.durationLabel" placeholder="一日研学" /></el-form-item>
          <el-form-item label="适合人群"><el-input v-model="form.suitableAudience" /></el-form-item>
          <el-form-item label="地点"><el-input v-model="form.location" /></el-form-item>
        </template>
        <el-form-item label="封面 URL"><el-input v-model="form.coverUrl" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" placeholder="emoji" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="form.tags" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" :max="9999" /></el-form-item>
        <el-form-item label="免费">
          <el-switch v-model="formIsFree" />
        </el-form-item>
        <el-form-item label="精品">
          <el-switch v-model="formIsElite" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useAdminAuthStore } from '@/admin/store/adminAuth'
import type { ContentPackageItem, ContentPackageWrite, ContentQuery, PageResult } from '@/admin/api/content'

const props = defineProps<{
  entityLabel: string
  resourceLink: string
  resourceSourceType: string
  listFn: (q?: ContentQuery) => Promise<PageResult<ContentPackageItem>>
  createFn: (body: ContentPackageWrite) => Promise<ContentPackageItem>
  updateFn: (id: number, body: ContentPackageWrite) => Promise<ContentPackageItem>
  statusFn: (id: number, status: number) => Promise<void>
  deleteFn: (id: number) => Promise<void>
  showCategory?: boolean
  showRegion?: boolean
  showGradeStage?: boolean
  showCultureFields?: boolean
}>()

const router = useRouter()
const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:content:edit'))

const loading = ref(false)
const saving = ref(false)
const showDisabled = ref(true)
const keyword = ref('')
const records = ref<ContentPackageItem[]>([])
const current = ref(1)
const size = ref(15)
const total = ref(0)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const form = reactive<ContentPackageWrite>({
  title: '',
  summary: '',
  category: '',
  region: 'all',
  gradeStage: 'all',
  durationType: '',
  durationLabel: '',
  suitableAudience: '',
  location: '',
  coverUrl: '',
  icon: '📚',
  tags: '',
  sort: 0,
  isFree: 1,
  isElite: 1,
  status: 1,
})

const formIsFree = computed({
  get: () => form.isFree === 1,
  set: (v: boolean) => { form.isFree = v ? 1 : 0 },
})
const formIsElite = computed({
  get: () => form.isElite === 1,
  set: (v: boolean) => { form.isElite = v ? 1 : 0 },
})

const dialogTitle = computed(() => (editingId.value ? `编辑${props.entityLabel}` : `新增${props.entityLabel}`))

function resetForm() {
  form.title = ''
  form.summary = ''
  form.category = props.showCategory ? 'elite' : ''
  form.region = 'all'
  form.gradeStage = 'all'
  form.durationType = ''
  form.durationLabel = ''
  form.suitableAudience = ''
  form.location = ''
  form.coverUrl = ''
  form.icon = '📚'
  form.tags = ''
  form.sort = 0
  form.isFree = 1
  form.isElite = 1
  form.status = 1
}

async function load() {
  loading.value = true
  try {
    const page = await props.listFn({
      keyword: keyword.value || undefined,
      includeDisabled: showDisabled.value,
      current: current.value,
      size: size.value,
    })
    records.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

function reload() {
  current.value = 1
  void load()
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: ContentPackageItem) {
  editingId.value = row.id
  form.title = row.title
  form.summary = row.summary || ''
  form.category = row.category || ''
  form.region = row.region || 'all'
  form.gradeStage = row.gradeStage || 'all'
  form.durationType = row.durationType || ''
  form.durationLabel = row.durationLabel || ''
  form.suitableAudience = row.suitableAudience || ''
  form.location = row.location || ''
  form.coverUrl = row.coverUrl || ''
  form.icon = row.icon || '📚'
  form.tags = row.tags || ''
  form.sort = row.sort ?? 0
  form.isFree = row.isFree ?? 1
  form.isElite = row.isElite ?? 1
  form.status = row.status ?? 1
  dialogVisible.value = true
}

async function save() {
  if (!form.title.trim()) {
    ElMessage.warning('请填写标题')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await props.updateFn(editingId.value, form)
      ElMessage.success('已保存')
    } else {
      await props.createFn(form)
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: ContentPackageItem, status: number) {
  await props.statusFn(row.id, status)
  ElMessage.success(status === 1 ? '已启用' : '已禁用')
  await load()
}

async function removeRow(row: ContentPackageItem) {
  await ElMessageBox.confirm(`确认删除「${row.title}」？`, '删除', { type: 'warning' })
  await props.deleteFn(row.id)
  ElMessage.success('已删除')
  await load()
}

function goResources() {
  router.push({ path: '/admin/resources', query: { sourceType: props.resourceSourceType } })
}

function openPublic() {
  window.open(props.resourceLink, '_blank')
}

onMounted(load)
</script>

<style scoped>
.content-package-crud__hint {
  margin-bottom: 12px;
}
.content-package-crud__toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
}
.content-package-crud__pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
