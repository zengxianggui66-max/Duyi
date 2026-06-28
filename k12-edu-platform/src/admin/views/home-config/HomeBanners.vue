<template>
  <div>
    <el-alert
      v-if="!canEdit"
      type="info"
      :closable="false"
      show-icon
      class="home-ops__hint"
      title="当前为只读模式"
      description="可查看轮播配置；新增、编辑、启停、删除需 content_admin 或 super_admin 权限。"
    />

    <div class="home-ops__toolbar">
      <el-checkbox v-model="showDisabled" @change="load">显示已禁用</el-checkbox>
      <el-button v-if="canEdit" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增轮播
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" border stripe empty-text="暂无数据">
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column prop="subtitle" label="副标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="ctaText" label="按钮" width="90" />
      <el-table-column prop="icon" label="图标" width="64" align="center" />
      <el-table-column label="跳转" width="100">
        <template #default="{ row }">{{ row.navTarget?.type ?? '-' }}</template>
      </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-form label-width="96px" @submit.prevent="save">
        <el-form-item label="标题" required><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.subtitle" /></el-form-item>
        <el-form-item label="按钮文案"><el-input v-model="form.ctaText" placeholder="立即查看" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" placeholder="emoji" /></el-form-item>
        <el-form-item label="渐变起"><el-input v-model="form.bgColorStart" placeholder="#667EEA" /></el-form-item>
        <el-form-item label="渐变止"><el-input v-model="form.bgColorEnd" placeholder="#764BA2" /></el-form-item>
        <el-form-item label="可见学段">
          <el-select v-model="form.stageKeys" multiple clearable style="width: 100%" placeholder="留空=全学段">
            <el-option v-for="s in stageOptions" :key="s.key" :label="s.name" :value="s.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" :max="999" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
        <NavTargetForm v-model="form.navTarget" />
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
import NavTargetForm from '@/admin/components/NavTargetForm.vue'
import { adminHomeOpsApi, type AdminHomeBannerItem } from '@/admin/api/homeOps'
import { stages } from '@/config/subjectConfig'
import type { NavTarget } from '@/types/homeOps'

const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:home:edit'))

const loading = ref(false)
const saving = ref(false)
const showDisabled = ref(true)
const records = ref<AdminHomeBannerItem[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const stageOptions = stages

const form = reactive({
  title: '',
  subtitle: '',
  ctaText: '立即查看',
  icon: '📚',
  bgColorStart: '#667EEA',
  bgColorEnd: '#764BA2',
  stageKeys: [] as string[],
  sort: 0,
  remark: '',
  navTarget: { type: 'route', routePath: '/' } as NavTarget,
})

const dialogTitle = computed(() => (editingId.value ? '编辑轮播' : '新增轮播'))

function resetForm() {
  form.title = ''
  form.subtitle = ''
  form.ctaText = '立即查看'
  form.icon = '📚'
  form.bgColorStart = '#667EEA'
  form.bgColorEnd = '#764BA2'
  form.stageKeys = []
  form.sort = 0
  form.remark = ''
  form.navTarget = { type: 'route', routePath: '/lesson' }
}

async function load() {
  loading.value = true
  try {
    records.value = (await adminHomeOpsApi.listBanners(showDisabled.value)) ?? []
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

function openEdit(row: AdminHomeBannerItem) {
  editingId.value = row.id
  form.title = row.title
  form.subtitle = row.subtitle ?? ''
  form.ctaText = row.ctaText ?? '立即查看'
  form.icon = row.icon ?? '📚'
  form.bgColorStart = row.bgColorStart ?? '#667EEA'
  form.bgColorEnd = row.bgColorEnd ?? '#764BA2'
  form.stageKeys = [...(row.stageKeys ?? [])]
  form.sort = row.sort ?? 0
  form.remark = row.remark ?? ''
  form.navTarget = { ...row.navTarget, query: row.navTarget.query ? { ...row.navTarget.query } : undefined }
  dialogVisible.value = true
}

async function save() {
  if (!form.title.trim()) {
    ElMessage.warning('请填写标题')
    return
  }
  saving.value = true
  const payload = {
    slotCode: 'home_hero',
    title: form.title.trim(),
    subtitle: form.subtitle || undefined,
    ctaText: form.ctaText || undefined,
    icon: form.icon || undefined,
    bgColorStart: form.bgColorStart,
    bgColorEnd: form.bgColorEnd,
    navTarget: form.navTarget,
    stageKeys: form.stageKeys.length ? form.stageKeys : undefined,
    sort: form.sort,
    status: 1,
    remark: form.remark || undefined,
  }
  try {
    if (editingId.value) {
      await adminHomeOpsApi.updateBanner(editingId.value, payload)
    } else {
      await adminHomeOpsApi.createBanner(payload)
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

async function toggleStatus(row: AdminHomeBannerItem, status: 0 | 1) {
  try {
    await adminHomeOpsApi.setBannerStatus(row.id, status)
    ElMessage.success(status === 1 ? '已启用' : '已禁用')
    await load()
  } catch (e) {
    ElMessage.error(String(e))
  }
}

async function removeRow(row: AdminHomeBannerItem) {
  try {
    await ElMessageBox.confirm(`确定删除轮播「${row.title}」？`, '确认删除', { type: 'warning' })
    await adminHomeOpsApi.deleteBanner(row.id)
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
}
</style>
