<template>
  <div>
    <el-alert v-if="!canEdit" type="info" :closable="false" show-icon class="home-ops__hint" title="当前为只读模式" />
    <div class="home-ops__toolbar">
      <el-checkbox v-model="showDisabled" @change="load">显示已禁用</el-checkbox>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="records" border stripe>
      <el-table-column prop="funcKey" label="Key" width="120" />
      <el-table-column prop="name" label="名称" width="100" />
      <el-table-column prop="examType" label="examType" width="140" />
      <el-table-column prop="defaultTopic" label="默认专题" width="100" />
      <el-table-column prop="stageKey" label="学段" width="88" />
      <el-table-column prop="examTabLabel" label="Tab 名" width="100" />
      <el-table-column prop="sort" label="排序" width="72" align="center" />
      <el-table-column label="状态" width="88" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="canEdit" label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 1" link type="warning" @click="toggleStatus(row, 0)">禁用</el-button>
          <el-button v-else link type="success" @click="toggleStatus(row, 1)">启用</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="编辑升学入口" width="640px" destroy-on-close>
      <el-form label-width="120px">
        <el-form-item label="显示名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="examType"><el-input v-model="form.examType" /></el-form-item>
        <el-form-item label="默认专题"><el-input v-model="form.defaultTopic" /></el-form-item>
        <el-form-item label="stageKey"><el-input v-model="form.stageKey" /></el-form-item>
        <el-form-item label="试卷 Tab"><el-input v-model="form.paperTab" /></el-form-item>
        <el-form-item label="默认年级"><el-input v-model="form.paperDefaultGrade" /></el-form-item>
        <el-form-item label="Tab 显示名"><el-input v-model="form.examTabLabel" /></el-form-item>
        <el-form-item label="浏览 module"><el-input v-model="form.browseModule" /></el-form-item>
        <el-form-item label="浏览 stageKey"><el-input v-model="form.browseStageKey" /></el-form-item>
        <el-form-item label="默认册别"><el-input v-model="form.browseDefaultVolume" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" :max="999" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAdminAuthStore } from '@/admin/store/adminAuth'
import { adminFuncChannelApi, type AdminFuncChannelItem } from '@/admin/api/funcChannels'

const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:home:edit'))
const loading = ref(false)
const saving = ref(false)
const showDisabled = ref(true)
const records = ref<AdminFuncChannelItem[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  name: '', examType: '', defaultTopic: '', stageKey: '', paperTab: '', paperDefaultGrade: '',
  examTabLabel: '', browseModule: '', browseStageKey: '', browseDefaultVolume: '', sort: 0,
})

async function load() {
  loading.value = true
  try {
    records.value = (await adminFuncChannelApi.list(showDisabled.value)) ?? []
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function openEdit(row: AdminFuncChannelItem) {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name, examType: row.examType, defaultTopic: row.defaultTopic, stageKey: row.stageKey,
    paperTab: row.paperTab, paperDefaultGrade: row.paperDefaultGrade, examTabLabel: row.examTabLabel ?? '',
    browseModule: row.browseModule ?? '', browseStageKey: row.browseStageKey ?? '',
    browseDefaultVolume: row.browseDefaultVolume ?? '', sort: row.sort ?? 0,
  })
  dialogVisible.value = true
}

async function save() {
  if (!editingId.value) return
  saving.value = true
  try {
    await adminFuncChannelApi.update(editingId.value, {
      name: form.name, examType: form.examType, defaultTopic: form.defaultTopic, stageKey: form.stageKey,
      paperTab: form.paperTab, paperDefaultGrade: form.paperDefaultGrade, scrollTarget: 'exam-module',
      examTabLabel: form.examTabLabel || undefined, browseModule: form.browseModule || undefined,
      browseStageKey: form.browseStageKey || undefined, browseDefaultVolume: form.browseDefaultVolume || undefined,
      sort: form.sort, status: 1,
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

async function toggleStatus(row: AdminFuncChannelItem, status: 0 | 1) {
  try {
    await adminFuncChannelApi.setStatus(row.id, status)
    ElMessage.success(status === 1 ? '已启用' : '已禁用')
    await load()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  }
}

onMounted(load)
</script>

<style scoped>
.home-ops__hint { margin-bottom: 16px; }
.home-ops__toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
