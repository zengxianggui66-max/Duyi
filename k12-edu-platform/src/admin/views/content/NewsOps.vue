<template>
  <div class="news-ops">
    <el-alert
      v-if="!canEdit"
      type="info"
      :closable="false"
      show-icon
      class="news-ops__hint"
      title="当前为只读模式"
      description="可查看资讯列表；发布/编辑/审核需 admin:content:edit 权限。"
    />

    <div class="news-ops__toolbar">
      <el-input v-model="keyword" placeholder="搜索标题" clearable style="width: 200px" @keyup.enter="reload" />
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 130px" @change="reload">
        <el-option label="待审核" :value="2" />
        <el-option label="已发布" :value="1" />
        <el-option label="草稿" :value="0" />
        <el-option label="已驳回" :value="3" />
      </el-select>
      <el-select v-model="category" placeholder="频道" clearable style="width: 130px" @change="reload">
        <el-option v-for="c in NEWS_CATEGORY_OPTIONS" :key="c.value" :label="c.label" :value="c.value" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
      <el-button v-if="canEdit" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新建资讯
      </el-button>
      <el-button link @click="openPublic">前台 /news ↗</el-button>
    </div>

    <el-table v-loading="loading" :data="records" border stripe empty-text="暂无数据">
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="频道" width="110" />
      <el-table-column prop="author" label="作者" width="100" />
      <el-table-column label="标记" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.isTop === 1" size="small" type="danger" class="news-ops__tag">置顶</el-tag>
          <el-tag v-if="row.isFeatured === 1" size="small" type="warning" class="news-ops__tag">精选</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="阅读" width="72" align="center" />
      <el-table-column label="状态" width="96" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布时间" width="170" />
      <el-table-column v-if="canEdit" label="操作" width="320" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <template v-if="row.status === 2">
            <el-button link type="success" @click="toggleStatus(row, 1)">通过</el-button>
            <el-button link type="danger" @click="toggleStatus(row, 3)">驳回</el-button>
          </template>
          <template v-else-if="row.status === 1">
            <el-button link @click="openPreview(row.id)">查看前台 ↗</el-button>
            <el-button link type="warning" @click="toggleStatus(row, 0)">下线</el-button>
          </template>
          <template v-else-if="row.status === 3">
            <el-button link type="primary" @click="toggleStatus(row, 2)">重新提交</el-button>
          </template>
          <template v-else>
            <el-button link type="success" @click="toggleStatus(row, 2)">提交审核</el-button>
            <el-button link type="success" @click="toggleStatus(row, 1)">直接发布</el-button>
          </template>
          <el-button link type="danger" @click="removeRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="news-ops__pager">
      <el-pagination
        v-model:current-page="current"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="load"
        @size-change="reload"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-form v-loading="formLoading" label-width="88px" @submit.prevent="save">
        <el-form-item label="标题" required><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="摘要"><el-input v-model="form.summary" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="频道" required>
          <el-select v-model="form.category" style="width: 100%">
            <el-option v-for="c in NEWS_CATEGORY_OPTIONS" :key="c.value" :label="`${c.icon} ${c.label}`" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="作者"><el-input v-model="form.author" /></el-form-item>
        <el-form-item label="封面">
          <div class="news-ops__cover">
            <el-upload :show-file-list="false" :http-request="uploadCover" accept="image/*">
              <el-button type="primary" plain :loading="coverUploading">上传封面</el-button>
            </el-upload>
            <el-input v-model="form.coverUrl" placeholder="或粘贴封面 URL" />
            <img v-if="form.coverUrl" :src="form.coverUrl" alt="封面预览" class="news-ops__cover-preview" />
          </div>
        </el-form-item>
        <el-form-item label="正文"><el-input v-model="form.content" type="textarea" :rows="8" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="form.tags" placeholder="逗号分隔" /></el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item v-if="form.isTop === 1" label="置顶序">
          <el-input-number v-model="form.topOrder" :min="0" :max="99" />
        </el-form-item>
        <el-form-item label="精选">
          <el-switch v-model="form.isFeatured" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="2">提交审核</el-radio>
            <el-radio :value="0">草稿</el-radio>
            <el-radio :value="1">直接发布</el-radio>
          </el-radio-group>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { UploadRequestOptions } from 'element-plus'
import { useAdminAuthStore } from '@/admin/store/adminAuth'
import { adminContentApi, type AdminArticleItem, type ArticleWrite } from '@/admin/api/content'
import { NEWS_CATEGORY_OPTIONS } from '@/constants/newsZone'
import { fileApi } from '@/api'

const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:content:edit'))

const loading = ref(false)
const saving = ref(false)
const formLoading = ref(false)
const coverUploading = ref(false)
const keyword = ref('')
const statusFilter = ref<number | undefined>()
const category = ref<string>()
const records = ref<AdminArticleItem[]>([])
const current = ref(1)
const size = ref(15)
const total = ref(0)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const form = reactive<ArticleWrite & { content?: string }>({
  title: '',
  summary: '',
  content: '',
  coverUrl: '',
  category: 'policy',
  author: '',
  tags: '',
  status: 2,
  isTop: 0,
  topOrder: 0,
  isFeatured: 0,
})

const dialogTitle = computed(() => (editingId.value ? '编辑资讯' : '新建资讯'))

function statusLabel(status?: number) {
  switch (status) {
    case 1: return '已发布'
    case 2: return '待审核'
    case 3: return '已驳回'
    default: return '草稿'
  }
}

function statusTagType(status?: number): 'success' | 'warning' | 'danger' | 'info' {
  switch (status) {
    case 1: return 'success'
    case 2: return 'warning'
    case 3: return 'danger'
    default: return 'info'
  }
}

function resetForm() {
  form.title = ''
  form.summary = ''
  form.content = ''
  form.coverUrl = ''
  form.category = 'policy'
  form.author = ''
  form.tags = ''
  form.status = 2
  form.isTop = 0
  form.topOrder = 0
  form.isFeatured = 0
}

async function load() {
  loading.value = true
  try {
    const page = await adminContentApi.listArticles({
      keyword: keyword.value || undefined,
      category: category.value || undefined,
      status: statusFilter.value,
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

async function openEdit(row: AdminArticleItem) {
  editingId.value = row.id
  dialogVisible.value = true
  formLoading.value = true
  try {
    const detail = await adminContentApi.getArticle(row.id)
    form.title = detail.title
    form.summary = detail.summary || ''
    form.content = detail.content || ''
    form.coverUrl = detail.coverUrl || ''
    form.category = detail.category || 'policy'
    form.author = detail.author || ''
    form.tags = detail.tags || ''
    form.status = detail.status ?? 2
    form.isTop = detail.isTop ?? 0
    form.topOrder = detail.topOrder ?? 0
    form.isFeatured = detail.isFeatured ?? 0
  } catch {
    ElMessage.error('加载资讯详情失败')
    dialogVisible.value = false
    editingId.value = null
  } finally {
    formLoading.value = false
  }
}

async function uploadCover(options: UploadRequestOptions) {
  coverUploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', options.file)
    fd.append('title', form.title || '资讯封面')
    const res = await fileApi.upload(fd)
    form.coverUrl = res.data.data?.fileUrl || ''
    ElMessage.success('封面上传成功')
  } catch {
    ElMessage.error('封面上传失败')
  } finally {
    coverUploading.value = false
  }
}

async function save() {
  if (!form.title.trim() || !form.category) {
    ElMessage.warning('请填写标题并选择频道')
    return
  }
  saving.value = true
  try {
    let savedId = editingId.value
    if (editingId.value) {
      await adminContentApi.updateArticle(editingId.value, form)
      ElMessage.success('已保存')
    } else {
      savedId = await adminContentApi.createArticle(form)
      ElMessage.success(form.status === 1 ? '已发布' : form.status === 2 ? '已提交审核' : '已保存草稿')
    }
    dialogVisible.value = false
    await load()
    if (form.status === 1 && savedId) {
      openPreview(savedId)
    }
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: AdminArticleItem, status: number) {
  const labels: Record<number, string> = { 0: '已下线', 1: '已通过并发布', 2: '已提交审核', 3: '已驳回' }
  await adminContentApi.setArticleStatus(row.id, status)
  ElMessage.success(labels[status] ?? '状态已更新')
  await load()
  if (status === 1) {
    openPreview(row.id)
  }
}

async function removeRow(row: AdminArticleItem) {
  await ElMessageBox.confirm(`确认删除「${row.title}」？`, '删除', { type: 'warning' })
  await adminContentApi.deleteArticle(row.id)
  ElMessage.success('已删除')
  await load()
}

function openPublic() {
  window.open('/news', '_blank')
}

function openPreview(id: number) {
  window.open(`/news/${id}`, '_blank')
}

onMounted(load)
</script>

<style scoped>
.news-ops__hint {
  margin-bottom: 12px;
}
.news-ops__toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
}
.news-ops__pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.news-ops__cover {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}
.news-ops__cover-preview {
  max-width: 280px;
  max-height: 160px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid var(--el-border-color);
}
.news-ops__tag + .news-ops__tag {
  margin-left: 4px;
}
</style>
