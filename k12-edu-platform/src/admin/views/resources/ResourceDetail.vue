<template>
  <AdminPageShell title="资源详情" desc="编辑资源元数据，并按审核状态、上架状态执行生命周期操作">
    <el-skeleton v-if="loading" :rows="10" animated />
    <template v-else-if="detail">
      <div class="detail-actions">
        <el-button @click="goBack">返回列表</el-button>
        <el-button v-if="canPublishAction" v-permission="'admin:resource:edit'" type="success" @click="onPublish">
          上架
        </el-button>
        <el-button v-if="canOfflineAction" v-permission="'admin:resource:edit'" type="warning" @click="onOffline">
          下架
        </el-button>
        <el-button
          v-if="canRecommend"
          v-permission="'admin:resource:recommend'"
          :type="detail.isRecommend === 1 ? 'default' : 'warning'"
          @click="onToggleRecommend"
        >
          {{ detail.isRecommend === 1 ? '取消推荐' : '设为推荐' }}
        </el-button>
        <el-button
          v-if="canTop"
          v-permission="'admin:resource:top'"
          :type="detail.isTop === 1 ? 'default' : 'danger'"
          @click="onToggleTop"
        >
          {{ detail.isTop === 1 ? '取消置顶' : '设为置顶' }}
        </el-button>
        <el-button v-if="canDelete" v-permission="'admin:resource:delete'" type="danger" plain @click="onDelete">
          移入回收站
        </el-button>
      </div>

      <el-descriptions :column="2" border class="detail-readonly">
        <el-descriptions-item label="全局ID">{{ detail.globalId }}</el-descriptions-item>
        <el-descriptions-item label="来源ID">{{ detail.sourceId }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="getAuditStatusTagType(detail)">{{ getAuditStatusLabel(detail) }}</el-tag>
          <el-tag size="small" :type="getPublishStatusTagType(detail)" style="margin-left: 6px">
            {{ getPublishStatusLabel(detail) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="来源">{{ sourceTypeLabel(detail.sourceType) }}</el-descriptions-item>
        <el-descriptions-item label="学段">{{ detail.stage || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学科">{{ detail.subject || '-' }}</el-descriptions-item>
        <el-descriptions-item label="文件">{{ detail.originalFilename || detail.fileExt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="大小">{{ formatSize(detail.fileSizeKb) }}</el-descriptions-item>
        <el-descriptions-item label="上传人ID">{{ detail.uploaderId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="上传时间">{{ detail.uploadTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="浏览/下载">
          {{ detail.viewCount ?? 0 }} / {{ detail.downloadCount ?? 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="目录节点ID">{{ detail.catalogNodeId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="目录路径" :span="2">{{ detail.catalogPath || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.ossUrl" label="文件链接" :span="2">
          <el-link :href="detail.ossUrl" target="_blank" type="primary">打开 OSS 文件</el-link>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">编辑信息</el-divider>

      <el-form v-permission="'admin:resource:edit'" :model="form" label-width="96px" class="detail-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="标题" prop="title" required>
              <el-input v-model="form.title" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="栏目" prop="module">
              <el-input v-model="form.module" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-input v-model="form.type" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="册别" prop="gradeName">
              <el-input v-model="form.gradeName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本" prop="edition">
              <el-input v-model="form.edition" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目录节点ID" prop="catalogNodeId">
              <el-input-number v-model="form.catalogNodeId" :min="1" :max="999999999" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单元" prop="unitName">
              <el-input v-model="form.unitName" placeholder="仅作展示兼容，挂载以目录节点ID为准" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课文" prop="lessonName">
              <el-input v-model="form.lessonName" placeholder="仅作展示兼容，挂载以目录节点ID为准" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序权重" prop="sort">
              <el-input-number v-model="form.sort" :min="0" :max="99999" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否免费" prop="isFree">
              <el-radio-group v-model="form.isFree">
                <el-radio :label="1">免费</el-radio>
                <el-radio :label="0">付费</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="简介" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="4" maxlength="500" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button v-permission="'admin:resource:edit'" type="primary" :loading="saving" @click="onSave">
            保存修改
          </el-button>
        </el-form-item>
      </el-form>
    </template>
    <el-empty v-else description="资源不存在或无权访问" />
  </AdminPageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminPageShell from '@/admin/components/AdminPageShell.vue'
import {
  deletePrimaryResource,
  getPrimaryResource,
  offlinePrimaryResource,
  publishPrimaryResource,
  setRecommendPrimaryResource,
  setTopPrimaryResource,
  updatePrimaryResource,
  type AdminPrimaryResourceDetail,
  type PrimaryResourceUpdate,
} from '@/admin/api/resources'
import {
  canRecycle,
  canOffline,
  canPublish,
  getAuditStatusLabel,
  getAuditStatusTagType,
  getPublishStatusLabel,
  getPublishStatusTagType,
} from '@/admin/utils/resourceStatus'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const detail = ref<AdminPrimaryResourceDetail | null>(null)

const form = reactive<PrimaryResourceUpdate & { title: string; isFree: number; sort: number }>({
  title: '',
  module: '',
  type: '',
  gradeName: '',
  edition: '',
  unitName: '',
  lessonName: '',
  catalogNodeId: undefined,
  remark: '',
  isFree: 1,
  sort: 0,
})

const canPublishAction = computed(() => (detail.value ? canPublish(detail.value) : false))
const canOfflineAction = computed(() => (detail.value ? canOffline(detail.value) : false))
const canRecommend = computed(() => canOfflineAction.value)
const canTop = computed(() => canOfflineAction.value)
const canDelete = computed(() => (detail.value ? canRecycle(detail.value) : false))

function sourceTypeLabel(t?: string) {
  const map: Record<string, string> = {
    primary_chinese: '小学语文',
    junior_math: '初中数学',
    junior_english: '初中英语',
  }
  return map[t || ''] || t || '-'
}

function formatSize(kb?: number) {
  if (kb == null) return '-'
  if (kb < 1024) return `${kb} KB`
  return `${(kb / 1024).toFixed(1)} MB`
}

function fillForm(d: AdminPrimaryResourceDetail) {
  form.title = d.title ?? ''
  form.module = d.module ?? ''
  form.type = d.type ?? ''
  form.gradeName = d.gradeName ?? ''
  form.edition = d.edition ?? ''
  form.unitName = d.unitName ?? ''
  form.lessonName = d.lessonName ?? ''
  form.catalogNodeId = d.catalogNodeId || undefined
  form.remark = d.remark ?? ''
  form.isFree = d.isFree ?? 1
  form.sort = d.sort ?? 0
}

async function load() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  try {
    detail.value = await getPrimaryResource(id)
    fillForm(detail.value)
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/admin/resource-main')
}

async function onSave() {
  if (!detail.value || !form.title.trim()) {
    ElMessage.warning('请填写标题')
    return
  }
  saving.value = true
  try {
    await updatePrimaryResource(detail.value.globalId, { ...form })
    ElMessage.success('已保存')
    await load()
  } finally {
    saving.value = false
  }
}

async function onPublish() {
  if (!detail.value) return
  await ElMessageBox.confirm('确认上架该资源？', '上架', { type: 'info' })
  await publishPrimaryResource(detail.value.globalId)
  ElMessage.success('已上架')
  await load()
}

async function onOffline() {
  if (!detail.value) return
  await ElMessageBox.confirm('确认下架该资源？', '下架', { type: 'warning' })
  await offlinePrimaryResource(detail.value.globalId)
  ElMessage.success('已下架')
  await load()
}

async function onToggleRecommend() {
  if (!detail.value) return
  const enabled = detail.value.isRecommend !== 1
  await setRecommendPrimaryResource(detail.value.globalId, enabled)
  ElMessage.success(enabled ? '已设为推荐' : '已取消推荐')
  await load()
}

async function onToggleTop() {
  if (!detail.value) return
  const enabled = detail.value.isTop !== 1
  if (enabled) {
    const { value } = await ElMessageBox.prompt('请输入置顶排序，数字越大越靠前', '设为置顶', {
      inputValue: String(detail.value.topSort ?? 0),
      inputPattern: /^\d+$/,
      inputErrorMessage: '请输入非负整数',
    })
    await setTopPrimaryResource(detail.value.globalId, true, Number(value))
  } else {
    await setTopPrimaryResource(detail.value.globalId, false)
  }
  ElMessage.success(enabled ? '已置顶' : '已取消置顶')
  await load()
}

async function onDelete() {
  if (!detail.value) return
  await ElMessageBox.confirm('确认移入回收站？', '回收资源', { type: 'warning' })
  await deletePrimaryResource(detail.value.globalId)
  ElMessage.success('已移入回收站')
  router.push('/admin/resource-main')
}

onMounted(load)
</script>

<style scoped>
.detail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.detail-readonly {
  margin-bottom: 8px;
}

.detail-form {
  max-width: 960px;
}
</style>
