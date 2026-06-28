<template>
  <div>
    <el-alert v-if="!canEdit" type="info" :closable="false" show-icon class="home-ops__hint" title="当前为只读模式" />
    <div class="home-ops__toolbar">
      <el-checkbox v-model="showDisabled" @change="load">显示已禁用</el-checkbox>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="records" border stripe>
      <el-table-column prop="code" label="Code" width="100" />
      <el-table-column prop="name" label="名称" min-width="100" />
      <el-table-column prop="routePath" label="路由" min-width="140" show-overflow-tooltip />
      <el-table-column label="统计" width="140">
        <template #default="{ row }">{{ row.stats?.total ?? '-' }} / {{ row.stats?.elite ?? '-' }}</template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="72" align="center" />
      <el-table-column label="状态" width="88" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="canEdit" label="操作" width="320" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link @click="openTabs(row)">Tab</el-button>
          <el-button link @click="openAlbums(row)">专辑</el-button>
          <el-button v-if="row.status === 1" link type="warning" @click="toggleStatus(row, 0)">禁用</el-button>
          <el-button v-else link type="success" @click="toggleStatus(row, 1)">启用</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="编辑特色频道" width="640px" destroy-on-close>
      <el-form label-width="108px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="背景渐变"><el-input v-model="form.bgGradient" /></el-form-item>
        <el-form-item label="路由"><el-input v-model="form.routePath" /></el-form-item>
        <el-form-item label="精品标题"><el-input v-model="form.eliteTitle" /></el-form-item>
        <el-form-item label="精品描述"><el-input v-model="form.eliteDesc" /></el-form-item>
        <el-form-item label="统计 total"><el-input v-model="form.statsTotal" /></el-form-item>
        <el-form-item label="统计 elite"><el-input v-model="form.statsElite" /></el-form-item>
        <el-form-item label="统计 free"><el-input v-model="form.statsFree" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" :max="999" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="tabsVisible" :title="`Tab · ${activeChannel?.name ?? ''}`" width="720px">
      <el-table :data="tabRecords" border size="small">
        <el-table-column prop="tabKey" label="Key" width="120" />
        <el-table-column prop="tabName" label="名称" min-width="100" />
        <el-table-column prop="searchKeyword" label="搜索词" min-width="140" show-overflow-tooltip />
        <el-table-column v-if="canEdit" label="操作" width="88">
          <template #default="{ row }">
            <el-button link type="primary" @click="editTab(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="tabFormVisible" title="编辑 Tab" width="480px" append-to-body>
      <el-form label-width="96px">
        <el-form-item label="名称"><el-input v-model="tabForm.tabName" /></el-form-item>
        <el-form-item label="搜索词"><el-input v-model="tabForm.searchKeyword" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="tabForm.icon" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tabFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="tabSaving" @click="saveTab">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="albumsVisible" :title="`精品专辑 · ${activeChannel?.name ?? ''}`" width="760px">
      <div v-if="canEdit" class="home-ops__toolbar"><el-button type="primary" @click="openAlbumForm()">新增</el-button></div>
      <el-table :data="albumRecords" border size="small">
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="meta" label="Meta" width="120" />
        <el-table-column prop="resourceCount" label="资源数" width="80" />
        <el-table-column v-if="canEdit" label="操作" width="140">
          <template #default="{ row }">
            <el-button link type="primary" @click="openAlbumForm(row)">编辑</el-button>
            <el-button link type="danger" @click="removeAlbum(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="albumFormVisible" :title="albumEditingId ? '编辑专辑' : '新增专辑'" width="520px" append-to-body>
      <el-form label-width="96px">
        <el-form-item label="标题"><el-input v-model="albumForm.title" /></el-form-item>
        <el-form-item label="Meta"><el-input v-model="albumForm.meta" /></el-form-item>
        <el-form-item label="资源数"><el-input-number v-model="albumForm.resourceCount" :min="0" /></el-form-item>
        <el-form-item label="下载数"><el-input-number v-model="albumForm.downloadCount" :min="0" /></el-form-item>
        <el-form-item label="封面渐变"><el-input v-model="albumForm.coverGradient" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="albumFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="albumSaving" @click="saveAlbum">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminAuthStore } from '@/admin/store/adminAuth'
import {
  adminOpsChannelApi,
  type OpsChannelAlbumItem,
  type OpsChannelItem,
  type OpsChannelTabItem,
} from '@/admin/api/opsChannels'

const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:home:edit'))
const loading = ref(false)
const saving = ref(false)
const showDisabled = ref(true)
const records = ref<OpsChannelItem[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const activeChannel = ref<OpsChannelItem | null>(null)
const tabsVisible = ref(false)
const tabRecords = ref<OpsChannelTabItem[]>([])
const tabFormVisible = ref(false)
const tabSaving = ref(false)
const tabEditingId = ref<number | null>(null)
const tabForm = reactive({ tabKey: '', tabName: '', icon: '', searchKeyword: '', sort: 0 })

const albumsVisible = ref(false)
const albumRecords = ref<OpsChannelAlbumItem[]>([])
const albumFormVisible = ref(false)
const albumSaving = ref(false)
const albumEditingId = ref<number | null>(null)
const albumForm = reactive({
  title: '', meta: '', resourceCount: 0, downloadCount: 0, coverGradient: '', sort: 0,
})

const form = reactive({
  name: '', icon: '', description: '', bgGradient: '', routePath: '',
  eliteTitle: '', eliteDesc: '', statsTotal: '', statsElite: '', statsFree: '', sort: 0,
})

async function load() {
  loading.value = true
  try {
    records.value = (await adminOpsChannelApi.list(showDisabled.value)) ?? []
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function openEdit(row: OpsChannelItem) {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name, icon: row.icon ?? '', description: row.description ?? '',
    bgGradient: row.bgGradient ?? '', routePath: row.routePath ?? '',
    eliteTitle: row.ui?.eliteTitle ?? '', eliteDesc: row.ui?.eliteDesc ?? '',
    statsTotal: row.stats?.total ?? '', statsElite: row.stats?.elite ?? '', statsFree: row.stats?.free ?? '',
    sort: row.sort ?? 0,
  })
  dialogVisible.value = true
}

async function save() {
  if (!editingId.value) return
  saving.value = true
  try {
    await adminOpsChannelApi.update(editingId.value, {
      name: form.name, icon: form.icon, description: form.description, bgGradient: form.bgGradient,
      routePath: form.routePath, sort: form.sort, status: 1,
      stats: { total: form.statsTotal, elite: form.statsElite, free: form.statsFree },
      ui: { showGradeFilter: true, showSubjectFilter: false, eliteTitle: form.eliteTitle, eliteDesc: form.eliteDesc },
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

async function toggleStatus(row: OpsChannelItem, status: 0 | 1) {
  try {
    await adminOpsChannelApi.setStatus(row.id, status)
    ElMessage.success(status === 1 ? '已启用' : '已禁用')
    await load()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function openTabs(row: OpsChannelItem) {
  activeChannel.value = row
  tabsVisible.value = true
  tabRecords.value = (await adminOpsChannelApi.listTabs(row.code)) ?? []
}

function editTab(row: OpsChannelTabItem) {
  tabEditingId.value = row.id
  Object.assign(tabForm, {
    tabKey: row.tabKey, tabName: row.tabName, icon: row.icon ?? '', searchKeyword: row.searchKeyword ?? '', sort: row.sort ?? 0,
  })
  tabFormVisible.value = true
}

async function saveTab() {
  if (!tabEditingId.value) return
  tabSaving.value = true
  try {
    await adminOpsChannelApi.updateTab(tabEditingId.value, {
      tabKey: tabForm.tabKey, tabName: tabForm.tabName, icon: tabForm.icon, searchKeyword: tabForm.searchKeyword,
      sort: tabForm.sort, status: 1,
    })
    ElMessage.success('Tab 已保存')
    tabFormVisible.value = false
    if (activeChannel.value) tabRecords.value = (await adminOpsChannelApi.listTabs(activeChannel.value.code)) ?? []
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '保存失败')
  } finally {
    tabSaving.value = false
  }
}

async function openAlbums(row: OpsChannelItem) {
  activeChannel.value = row
  albumsVisible.value = true
  albumRecords.value = (await adminOpsChannelApi.listAlbums(row.code)) ?? []
}

function openAlbumForm(row?: OpsChannelAlbumItem) {
  albumEditingId.value = row?.id ?? null
  Object.assign(albumForm, {
    title: row?.title ?? '', meta: row?.meta ?? '', resourceCount: row?.resourceCount ?? 0,
    downloadCount: row?.downloadCount ?? 0, coverGradient: row?.coverGradient ?? '', sort: row?.sort ?? 0,
  })
  albumFormVisible.value = true
}

async function saveAlbum() {
  if (!activeChannel.value) return
  albumSaving.value = true
  const payload = {
    title: albumForm.title, meta: albumForm.meta, resourceCount: albumForm.resourceCount,
    downloadCount: albumForm.downloadCount, coverGradient: albumForm.coverGradient, sort: albumForm.sort, status: 1 as const,
  }
  try {
    if (albumEditingId.value) {
      await adminOpsChannelApi.updateAlbum(albumEditingId.value, payload)
    } else {
      await adminOpsChannelApi.createAlbum(activeChannel.value.code, payload)
    }
    ElMessage.success('专辑已保存')
    albumFormVisible.value = false
    albumRecords.value = (await adminOpsChannelApi.listAlbums(activeChannel.value.code)) ?? []
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '保存失败')
  } finally {
    albumSaving.value = false
  }
}

async function removeAlbum(row: OpsChannelAlbumItem) {
  await ElMessageBox.confirm('确定删除该专辑？', '确认')
  await adminOpsChannelApi.deleteAlbum(row.id)
  ElMessage.success('已删除')
  if (activeChannel.value) albumRecords.value = (await adminOpsChannelApi.listAlbums(activeChannel.value.code)) ?? []
}

onMounted(load)
</script>

<style scoped>
.home-ops__hint { margin-bottom: 16px; }
.home-ops__toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
