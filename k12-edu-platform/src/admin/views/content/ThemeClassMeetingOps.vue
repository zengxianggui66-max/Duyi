<template>

  <div class="theme-class-meeting-ops">

    <el-alert

      type="info"

      :closable="false"

      show-icon

      class="theme-class-meeting-ops__hint"

      title="主题班会运营说明"

      description="班会频道 Tab / 精品专辑在此维护（code=banhui）；班会资源走 edu_resource 主域，在资源中心按 module=主题班会 管理。前台 /theme-class-meeting 与 /feature/banhui 已同源 browse API。"

    />



    <el-alert

      v-if="!canEdit"

      type="warning"

      :closable="false"

      show-icon

      class="theme-class-meeting-ops__hint"

      title="当前为只读模式"

      description="编辑 Tab / 专辑需 admin:home:edit 权限。"

    />



    <div class="theme-class-meeting-ops__actions">

      <el-checkbox v-model="showDisabled" @change="load">显示已禁用</el-checkbox>

      <el-button @click="load">刷新</el-button>

      <el-button type="primary" @click="goResources">资源中心 · edu_resource</el-button>

      <el-button link @click="openPublic">前台 /theme-class-meeting ↗</el-button>

    </div>



    <h4 class="theme-class-meeting-ops__subtitle">Tab 配置（banhui）</h4>

    <el-table v-loading="loading" :data="tabs" border stripe empty-text="暂无 Tab 配置">

      <el-table-column prop="tabName" label="Tab 名称" min-width="140" />

      <el-table-column prop="tabKey" label="Key" width="120" />

      <el-table-column prop="searchKeyword" label="搜索关键词" min-width="160" show-overflow-tooltip />

      <el-table-column prop="sort" label="排序" width="72" align="center" />

      <el-table-column label="状态" width="88" align="center">

        <template #default="{ row }">

          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">

            {{ row.status === 1 ? '启用' : '禁用' }}

          </el-tag>

        </template>

      </el-table-column>

      <el-table-column v-if="canEdit" label="操作" width="160" fixed="right">

        <template #default="{ row }">

          <el-button link type="primary" @click="editTab(row)">编辑</el-button>

          <el-button v-if="row.status === 1" link type="warning" @click="toggleTabStatus(row, 0)">禁用</el-button>

          <el-button v-else link type="success" @click="toggleTabStatus(row, 1)">启用</el-button>

        </template>

      </el-table-column>

    </el-table>



    <div class="theme-class-meeting-ops__album-header">

      <h4 class="theme-class-meeting-ops__subtitle">精品专辑（banhui）</h4>

      <el-button v-if="canEdit" type="primary" size="small" @click="openAlbumForm()">新增专辑</el-button>

    </div>

    <el-table v-loading="loading" :data="albums" border stripe empty-text="暂无专辑">

      <el-table-column prop="title" label="专辑标题" min-width="180" />

      <el-table-column prop="meta" label="Meta" width="120" show-overflow-tooltip />

      <el-table-column prop="resourceCount" label="资源数" width="80" align="center" />

      <el-table-column prop="sort" label="排序" width="72" align="center" />

      <el-table-column label="状态" width="88" align="center">

        <template #default="{ row }">

          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">

            {{ row.status === 1 ? '启用' : '禁用' }}

          </el-tag>

        </template>

      </el-table-column>

      <el-table-column v-if="canEdit" label="操作" width="200" fixed="right">

        <template #default="{ row }">

          <el-button link type="primary" @click="openAlbumForm(row)">编辑</el-button>

          <el-button v-if="row.status === 1" link type="warning" @click="toggleAlbumStatus(row, 0)">禁用</el-button>

          <el-button v-else link type="success" @click="toggleAlbumStatus(row, 1)">启用</el-button>

          <el-button link type="danger" @click="removeAlbum(row)">删除</el-button>

        </template>

      </el-table-column>

    </el-table>



    <el-dialog v-model="tabFormVisible" title="编辑 Tab" width="480px" destroy-on-close>

      <el-form label-width="96px">

        <el-form-item label="Key"><el-input v-model="tabForm.tabKey" disabled /></el-form-item>

        <el-form-item label="名称"><el-input v-model="tabForm.tabName" /></el-form-item>

        <el-form-item label="搜索词"><el-input v-model="tabForm.searchKeyword" /></el-form-item>

        <el-form-item label="图标"><el-input v-model="tabForm.icon" /></el-form-item>

        <el-form-item label="排序"><el-input-number v-model="tabForm.sort" :min="0" :max="999" /></el-form-item>

      </el-form>

      <template #footer>

        <el-button @click="tabFormVisible = false">取消</el-button>

        <el-button type="primary" :loading="tabSaving" @click="saveTab">保存</el-button>

      </template>

    </el-dialog>



    <el-dialog v-model="albumFormVisible" :title="albumEditingId ? '编辑专辑' : '新增专辑'" width="520px" destroy-on-close>

      <el-form label-width="96px">

        <el-form-item label="标题" required><el-input v-model="albumForm.title" /></el-form-item>

        <el-form-item label="Meta"><el-input v-model="albumForm.meta" /></el-form-item>

        <el-form-item label="资源数"><el-input-number v-model="albumForm.resourceCount" :min="0" /></el-form-item>

        <el-form-item label="下载数"><el-input-number v-model="albumForm.downloadCount" :min="0" /></el-form-item>

        <el-form-item label="封面渐变"><el-input v-model="albumForm.coverGradient" placeholder="linear-gradient(...)" /></el-form-item>

        <el-form-item label="链接"><el-input v-model="albumForm.linkPath" placeholder="/resource/123" /></el-form-item>

        <el-form-item label="排序"><el-input-number v-model="albumForm.sort" :min="0" :max="999" /></el-form-item>

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

import { useRouter } from 'vue-router'

import { ElMessage, ElMessageBox } from 'element-plus'

import { useAdminAuthStore } from '@/admin/store/adminAuth'

import { adminOpsChannelApi, type OpsChannelAlbumItem, type OpsChannelTabItem } from '@/admin/api/opsChannels'



const CHANNEL_CODE = 'banhui'

const router = useRouter()

const adminStore = useAdminAuthStore()

const canEdit = computed(() => adminStore.hasPermission('admin:home:edit'))



const loading = ref(false)

const showDisabled = ref(true)

const tabs = ref<OpsChannelTabItem[]>([])

const albums = ref<OpsChannelAlbumItem[]>([])



const tabFormVisible = ref(false)

const tabSaving = ref(false)

const tabEditingId = ref<number | null>(null)

const tabForm = reactive({ tabKey: '', tabName: '', icon: '', searchKeyword: '', sort: 0, status: 1 as 0 | 1 })



const albumFormVisible = ref(false)

const albumSaving = ref(false)

const albumEditingId = ref<number | null>(null)

const albumForm = reactive({

  title: '', meta: '', resourceCount: 0, downloadCount: 0,

  coverGradient: '', linkPath: '', sort: 0, status: 1 as 0 | 1,

})



async function load() {

  loading.value = true

  try {

    const [tabList, albumList] = await Promise.all([

      adminOpsChannelApi.listTabs(CHANNEL_CODE, showDisabled.value),

      adminOpsChannelApi.listAlbums(CHANNEL_CODE, showDisabled.value),

    ])

    tabs.value = tabList ?? []

    albums.value = albumList ?? []

  } catch (e: unknown) {

    ElMessage.error(e instanceof Error ? e.message : '加载失败')

  } finally {

    loading.value = false

  }

}



function editTab(row: OpsChannelTabItem) {

  tabEditingId.value = row.id

  Object.assign(tabForm, {

    tabKey: row.tabKey,

    tabName: row.tabName,

    icon: row.icon ?? '',

    searchKeyword: row.searchKeyword ?? '',

    sort: row.sort ?? 0,

    status: (row.status ?? 1) as 0 | 1,

  })

  tabFormVisible.value = true

}



async function saveTab() {

  if (!tabEditingId.value || !tabForm.tabName.trim()) {

    ElMessage.warning('请填写 Tab 名称')

    return

  }

  tabSaving.value = true

  try {

    await adminOpsChannelApi.updateTab(tabEditingId.value, {

      tabKey: tabForm.tabKey,

      tabName: tabForm.tabName,

      icon: tabForm.icon || undefined,

      searchKeyword: tabForm.searchKeyword || undefined,

      sort: tabForm.sort,

      status: tabForm.status,

    })

    ElMessage.success('Tab 已保存')

    tabFormVisible.value = false

    await load()

  } catch (e: unknown) {

    ElMessage.error(e instanceof Error ? e.message : '保存失败')

  } finally {

    tabSaving.value = false

  }

}



async function toggleTabStatus(row: OpsChannelTabItem, status: 0 | 1) {

  try {

    await adminOpsChannelApi.updateTab(row.id, {

      tabKey: row.tabKey,

      tabName: row.tabName,

      icon: row.icon,

      searchKeyword: row.searchKeyword,

      sort: row.sort,

      status,

    })

    ElMessage.success(status === 1 ? '已启用' : '已禁用')

    await load()

  } catch (e: unknown) {

    ElMessage.error(e instanceof Error ? e.message : '操作失败')

  }

}



function openAlbumForm(row?: OpsChannelAlbumItem) {

  albumEditingId.value = row?.id ?? null

  Object.assign(albumForm, {

    title: row?.title ?? '',

    meta: row?.meta ?? '',

    resourceCount: row?.resourceCount ?? 0,

    downloadCount: row?.downloadCount ?? 0,

    coverGradient: row?.coverGradient ?? '',

    linkPath: row?.linkPath ?? '',

    sort: row?.sort ?? 0,

    status: (row?.status ?? 1) as 0 | 1,

  })

  albumFormVisible.value = true

}



async function saveAlbum() {

  if (!albumForm.title.trim()) {

    ElMessage.warning('请填写专辑标题')

    return

  }

  albumSaving.value = true

  const payload = {

    title: albumForm.title,

    meta: albumForm.meta || undefined,

    resourceCount: albumForm.resourceCount,

    downloadCount: albumForm.downloadCount,

    coverGradient: albumForm.coverGradient || undefined,

    linkPath: albumForm.linkPath || undefined,

    sort: albumForm.sort,

    status: albumForm.status,

  }

  try {

    if (albumEditingId.value) {

      await adminOpsChannelApi.updateAlbum(albumEditingId.value, payload)

    } else {

      await adminOpsChannelApi.createAlbum(CHANNEL_CODE, payload)

    }

    ElMessage.success('专辑已保存')

    albumFormVisible.value = false

    await load()

  } catch (e: unknown) {

    ElMessage.error(e instanceof Error ? e.message : '保存失败')

  } finally {

    albumSaving.value = false

  }

}



async function toggleAlbumStatus(row: OpsChannelAlbumItem, status: 0 | 1) {

  try {

    await adminOpsChannelApi.updateAlbum(row.id, {

      title: row.title,

      meta: row.meta,

      resourceCount: row.resourceCount,

      downloadCount: row.downloadCount,

      coverGradient: row.coverGradient,

      linkPath: row.linkPath,

      sort: row.sort,

      status,

    })

    ElMessage.success(status === 1 ? '已启用' : '已禁用')

    await load()

  } catch (e: unknown) {

    ElMessage.error(e instanceof Error ? e.message : '操作失败')

  }

}



async function removeAlbum(row: OpsChannelAlbumItem) {

  await ElMessageBox.confirm(`确认删除专辑「${row.title}」？`, '删除', { type: 'warning' })

  try {

    await adminOpsChannelApi.deleteAlbum(row.id)

    ElMessage.success('已删除')

    await load()

  } catch (e: unknown) {

    ElMessage.error(e instanceof Error ? e.message : '删除失败')

  }

}



function goResources() {

  router.push({ path: '/admin/resources', query: { sourceType: 'edu_resource' } })

}



function openPublic() {

  window.open('/theme-class-meeting', '_blank')

}



onMounted(load)

</script>



<style scoped>

.theme-class-meeting-ops__hint {

  margin-bottom: 16px;

}

.theme-class-meeting-ops__actions {

  display: flex;

  flex-wrap: wrap;

  gap: 8px;

  align-items: center;

  margin-bottom: 16px;

}

.theme-class-meeting-ops__subtitle {

  margin: 0 0 12px;

  font-size: 14px;

  font-weight: 600;

}

.theme-class-meeting-ops__album-header {

  display: flex;

  align-items: center;

  justify-content: space-between;

  margin-top: 20px;

}

</style>

