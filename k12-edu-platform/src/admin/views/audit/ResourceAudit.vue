<template>
  <AdminPageShell title="待审队列" desc="统一资源待审核（v_admin_resource_main）· 支持跨学段跨学科 · 审核通过后需单独发布">
    <!-- 筛选栏 -->
    <div class="audit-toolbar">
      <el-select v-model="filters.sourceType" placeholder="来源类型" clearable style="width: 150px" @change="reload">
        <el-option
          v-for="opt in RESOURCE_SOURCE_TYPE_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
        <el-option label="全部" value="" />
      </el-select>

      <el-input
        v-model="keyword"
        placeholder="搜索标题/单元/课文"
        clearable
        style="width: 200px"
        @keyup.enter="reload"
      />

      <el-select v-model="filters.stage" placeholder="学段" clearable style="width: 90px" @change="reload">
        <el-option label="小学" value="小学" />
        <el-option label="初中" value="初中" />
        <el-option label="高中" value="高中" />
      </el-select>

      <el-select v-model="filters.subject" placeholder="学科" clearable style="width: 90px" @change="reload">
        <el-option label="语文" value="语文" />
        <el-option label="数学" value="数学" />
        <el-option label="英语" value="英语" />
      </el-select>

      <el-select v-model="filters.module" placeholder="栏目" clearable style="width: 120px" @change="reload">
        <el-option label="课文原文" value="课文原文" />
        <el-option label="练习册" value="练习册" />
        <el-option label="教辅" value="教辅" />
        <el-option label="考试" value="考试" />
        <el-option label="其他" value="其他" />
      </el-select>

      <el-select v-model="filters.type" placeholder="资源类型" clearable style="width: 110px" @change="reload">
        <el-option label="PDF" value="PDF" />
        <el-option label="PPT" value="PPT" />
        <el-option label="Word" value="Word" />
        <el-option label="图片" value="图片" />
        <el-option label="视频" value="视频" />
        <el-option label="音频" value="音频" />
      </el-select>

      <el-input
        v-model="filters.gradeName"
        placeholder="年级"
        clearable
        style="width: 100px"
        @keyup.enter="reload"
      />

      <el-select v-model="filters.previewStatus" placeholder="预览状态" clearable style="width: 110px" @change="reload">
        <el-option label="可预览" value="previewable" />
        <el-option label="不可预览" value="non_previewable" />
      </el-select>

      <!-- Phase 8: 文件安全状态筛选 -->
      <el-select v-model="filters.fileSafety" placeholder="文件安全" clearable style="width: 110px" @change="reload">
        <el-option label="安全" :value="2" />
        <el-option label="待确认" :value="1" />
        <el-option label="有风险" :value="3" />
        <el-option label="未知" :value="0" />
      </el-select>

      <!-- Phase 8: 审核超时筛选 -->
      <el-select v-model="filters.auditTimeout" placeholder="审核超时" clearable style="width: 120px" @change="reload">
        <el-option label="超24h待审" value="overtime24" />
        <el-option label="超48h待审" value="overtime48" />
        <el-option label="超72h待审" value="overtime72" />
      </el-select>

      <el-button type="primary" @click="reload">查询</el-button>

      <el-button text @click="resetFilters">重置</el-button>

      <div class="audit-toolbar__spacer" />

      <el-button
        v-permission="'admin:audit:approve'"
        type="success"
        :disabled="selectedIds.length === 0"
        :loading="batching"
        @click="batchApprove"
      >
        批量通过（{{ selectedIds.length }}）
      </el-button>

      <el-button
        v-permission="'admin:audit:reject'"
        type="danger"
        plain
        :disabled="selectedIds.length === 0"
        @click="openBatchReject"
      >
        批量驳回（{{ selectedIds.length }}）
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="records"
      border
      stripe
      @selection-change="onSelectionChange"
    >
      <el-table-column type="selection" width="48" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="stage" label="学段" width="80" />
      <el-table-column prop="subject" label="学科" width="90" />
      <el-table-column prop="module" label="栏目" width="90" show-overflow-tooltip />
      <el-table-column prop="type" label="类型" width="90" />
      <el-table-column prop="gradeName" label="年级" width="110" show-overflow-tooltip />
      <el-table-column label="审核" width="88">
        <template #default="{ row }">
          <el-tag size="small" :type="getAuditStatusTagType(row)">
            {{ getAuditStatusLabel(row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="上架" width="88">
        <template #default="{ row }">
          <el-tag size="small" :type="getPublishStatusTagType(row)">
            {{ getPublishStatusLabel(row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="fileExt" label="格式" width="80">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ row.fileExt || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="可预览" width="76" align="center">
        <template #default="{ row }">
          <el-icon v-if="isPreviewable(row)" color="#67C23A"><Check /></el-icon>
          <el-icon v-else color="#C0C4CC"><Close /></el-icon>
        </template>
      </el-table-column>
      <!-- Phase 8: 文件安全状态 -->
      <el-table-column label="文件安全" width="90" align="center">
        <template #default="{ row }">
          <el-tooltip :content="fileSafetyLabel(row.fileSafetyStatus)" placement="top">
            <el-tag size="small" :type="fileSafetyTagType(row.fileSafetyStatus)" effect="plain">
              {{ fileSafetyShortLabel(row.fileSafetyStatus) }}
            </el-tag>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="uploaderId" label="上传者ID" width="90" />
      <el-table-column prop="uploadTime" label="提交时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openPreview(row.id)">预览</el-button>
          <el-button v-permission="'admin:audit:approve'" link type="success" @click="approve(row.id)">
            通过
          </el-button>
          <el-button v-permission="'admin:audit:reject'" link type="danger" @click="openReject(row.id)">
            驳回
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="audit-pagination">
      <el-pagination
        v-model:current-page="current"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </div>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectVisible" :title="rejectDialogTitle" width="560px">
      <el-form label-width="88px">
        <el-form-item v-if="rejectMode === 'single'" label="常用原因">
          <el-select
            v-model="selectedTemplateId"
            placeholder="选择驳回模板（按分类分组）"
            clearable
            style="width: 100%"
            @change="applyTemplate"
          >
            <el-option-group
              v-for="(items, cat) in rejectTemplatesByCat"
              :key="cat"
              :label="cat"
            >
              <el-option
                v-for="item in items"
                :key="item.id"
                :label="item.content"
                :value="item.id"
              />
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="驳回原因" required>
          <el-input
            v-model="rejectReason"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请填写驳回原因，提交后将写入审核记录"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejecting" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>

    <!-- 批量结果详情 -->
    <el-dialog v-model="resultVisible" title="批量审核结果" width="560px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="成功">
          <el-tag type="success">{{ batchResult.successCount }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="跳过">
          <el-tag type="warning">{{ batchResult.skipCount }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <div v-if="batchResult.skipReasons?.length" class="skip-list">
        <div class="skip-title">跳过明细：</div>
        <div v-for="(reason, i) in batchResult.skipReasons" :key="i" class="skip-item">
          {{ reason }}
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="resultVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <AdminAuditPreviewDrawer
      :visible="previewVisible"
      :resource-id="previewResourceId"
      :acting="previewActing"
      @close="closePreview"
      @approve="approveFromPreview"
      @reject="rejectFromPreview"
    />
  </AdminPageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close } from '@element-plus/icons-vue'
import AdminPageShell from '@/admin/components/AdminPageShell.vue'
import AdminAuditPreviewDrawer from '@/admin/components/AdminAuditPreviewDrawer.vue'
import {
  auditPrimaryResource,
  batchAuditResources,
} from '@/admin/api/resources'
import {
  listPendingResourceMain,
  type ResourceMainItem,
} from '@/admin/api/resourceMain'
import { listRejectReasonsByCategory, type RejectReasonItem } from '@/admin/api/audit'
import { RESOURCE_SOURCE_TYPE_OPTIONS } from '@/admin/constants/resourceSourceTypes'
import {
  getAuditStatusLabel,
  getAuditStatusTagType,
  getPublishStatusLabel,
  getPublishStatusTagType,
} from '@/admin/utils/resourceStatus'

const AUDIT_ACTION_STATUS = {
  APPROVE: 1,
  REJECT: 2,
} as const

const PREVIEWABLE_EXTS = new Set(['pdf', 'jpg', 'jpeg', 'png', 'gif', 'webp'])

const loading = ref(false)
const batching = ref(false)
type AuditRow = ResourceMainItem & { id: number; status: number }
const records = ref<AuditRow[]>([])
const selectedIds = ref<number[]>([])
const keyword = ref('')
const current = ref(1)
const size = ref(15)
const total = ref(0)

const filters = reactive({
  /** Phase 7：统一资源域 source_type 筛选 */
  sourceType: '' as string,
  stage: '' as string,
  subject: '' as string,
  module: '' as string,
  type: '' as string,
  gradeName: '' as string,
  previewStatus: '' as string,
  /** Phase 8: 文件安全状态 */
  fileSafety: undefined as number | undefined,
  /** Phase 8: 审核超时筛选 */
  auditTimeout: '' as string,
})

const rejectVisible = ref(false)
const rejecting = ref(false)
const rejectMode = ref<'single' | 'batch'>('single')
const rejectResourceId = ref<number | null>(null)
const rejectReason = ref('')
const rejectTemplatesByCat = ref<Record<string, RejectReasonItem[]>>({})
const selectedTemplateId = ref<number | null>(null)

const resultVisible = ref(false)
const batchResult = reactive({ successCount: 0, skipCount: 0, skipReasons: [] as string[] })

const previewVisible = ref(false)
const previewResourceId = ref<number | null>(null)
const previewActing = ref(false)

const rejectDialogTitle = computed(() =>
  rejectMode.value === 'batch'
    ? `批量驳回（${selectedIds.value.length} 条）`
    : '驳回资源',
)

function isPreviewable(row: AuditRow): boolean {
  const ext = (row.fileExt || '').toLowerCase().replace(/^\./, '')
  return PREVIEWABLE_EXTS.has(ext)
}

async function loadTemplates() {
  try {
    rejectTemplatesByCat.value = await listRejectReasonsByCategory(false)
  } catch {
    rejectTemplatesByCat.value = {}
  }
}

async function load() {
  loading.value = true
  try {
    const page = await listPendingResourceMain({
      current: current.value,
      size: size.value,
      sourceType: filters.sourceType || undefined,
      keyword: keyword.value || undefined,
      stage: filters.stage || undefined,
      subject: filters.subject || undefined,
      module: filters.module || undefined,
      type: filters.type || undefined,
      gradeName: filters.gradeName || undefined,
    })
    let filtered = page.records.map((item) => ({
      ...item,
      id: item.globalId,
      status: item.legacyStatus ?? 0,
    }))
    // 前端二次过滤：预览状态
    if (filters.previewStatus === 'previewable') {
      filtered = filtered.filter(isPreviewable)
    } else if (filters.previewStatus === 'non_previewable') {
      filtered = filtered.filter((r) => !isPreviewable(r))
    }
    // Phase 8: 文件安全状态筛选
    if (typeof filters.fileSafety === 'number') {
      filtered = filtered.filter((r) => r.fileSafetyStatus === filters.fileSafety)
    }
    // Phase 8: 审核超时筛选
    if (filters.auditTimeout) {
      const now = Date.now()
      filtered = filtered.filter((r) => {
        if (!r.uploadTime) return false
        const hoursDiff = (now - new Date(r.uploadTime).getTime()) / (1000 * 60 * 60)
        if (filters.auditTimeout === 'overtime24') return hoursDiff > 24
        if (filters.auditTimeout === 'overtime48') return hoursDiff > 48
        if (filters.auditTimeout === 'overtime72') return hoursDiff > 72
        return false
      })
    }
    records.value = filtered
    total.value = page.total
    selectedIds.value = []
  } finally {
    loading.value = false
  }
}

function reload() {
  current.value = 1
  void load()
}

function resetFilters() {
  filters.sourceType = ''
  filters.stage = ''
  filters.subject = ''
  filters.module = ''
  filters.type = ''
  filters.gradeName = ''
  filters.previewStatus = ''
  filters.fileSafety = undefined
  filters.auditTimeout = ''
  keyword.value = ''
  reload()
}

// Phase 8: 文件安全快捷标签
function fileSafetyShortLabel(status?: number): string {
  const map: Record<number, string> = {
    2: '安全',
    1: '待确认',
    3: '有风险',
    0: '未知',
  }
  return map[status ?? 0] ?? '未知'
}

function fileSafetyLabel(status?: number): string {
  const map: Record<number, string> = {
    2: '文件类型安全，未发现风险',
    1: '压缩包等，建议下载核查',
    3: '可执行/脚本文件，建议人工核查',
    0: '非常见格式，建议确认',
  }
  return map[status ?? 0] ?? '未知状态'
}

function fileSafetyTagType(status?: number): 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<number, 'success' | 'warning' | 'danger' | 'info'> = {
    2: 'success',
    1: 'warning',
    3: 'danger',
    0: 'info',
  }
  return map[status ?? 0] ?? 'info'
}

function onSelectionChange(rows: AuditRow[]) {
  selectedIds.value = rows.map((r) => r.id)
}

function openPreview(id: number) {
  previewResourceId.value = id
  previewVisible.value = true
}

function closePreview() {
  previewVisible.value = false
  previewResourceId.value = null
}

async function approve(id: number) {
  await ElMessageBox.confirm('确认审核通过该资源？通过后进入待发布状态，不会立即前台可见。', '审核通过', {
    type: 'info',
  })
  await auditPrimaryResource(id, AUDIT_ACTION_STATUS.APPROVE)
  ElMessage.success('已审核通过，待发布')
  closePreview()
  void load()
}

async function approveFromPreview(id: number) {
  previewActing.value = true
  try {
    await approve(id)
  } finally {
    previewActing.value = false
  }
}

async function batchApprove() {
  if (selectedIds.value.length === 0) return
  await ElMessageBox.confirm(
    `确认批量通过 ${selectedIds.value.length} 条资源？`,
    '批量审核通过',
    { type: 'info' },
  )
  batching.value = true
  try {
    const result = await batchAuditResources(selectedIds.value, 'approve')
    batchResult.successCount = result.successCount
    batchResult.skipCount = result.skipCount
    batchResult.skipReasons = result.skipReasons || []
    resultVisible.value = true
    ElMessage.success(result.message || '批量通过完成')
    void load()
  } finally {
    batching.value = false
  }
}

function openReject(id: number) {
  rejectMode.value = 'single'
  rejectResourceId.value = id
  rejectReason.value = ''
  selectedTemplateId.value = null
  rejectVisible.value = true
}

function openBatchReject() {
  if (selectedIds.value.length === 0) return
  rejectMode.value = 'batch'
  rejectResourceId.value = null
  rejectReason.value = ''
  selectedTemplateId.value = null
  rejectVisible.value = true
}

function rejectFromPreview(id: number) {
  openReject(id)
}

function applyTemplate(id: number | null) {
  if (id == null) return
  for (const items of Object.values(rejectTemplatesByCat.value)) {
    const item = items.find((r) => r.id === id)
    if (item) {
      rejectReason.value = item.content
      return
    }
  }
}

async function confirmReject() {
  const reason = rejectReason.value.trim()
  if (!reason) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  rejecting.value = true
  try {
    if (rejectMode.value === 'batch') {
      const result = await batchAuditResources(selectedIds.value, 'reject', reason)
      batchResult.successCount = result.successCount
      batchResult.skipCount = result.skipCount
      batchResult.skipReasons = result.skipReasons || []
      resultVisible.value = true
      ElMessage.success(result.message || '批量驳回完成')
    } else if (rejectResourceId.value != null) {
      await auditPrimaryResource(rejectResourceId.value, AUDIT_ACTION_STATUS.REJECT, reason)
      ElMessage.success('已驳回')
      closePreview()
    }
    rejectVisible.value = false
    void load()
  } finally {
    rejecting.value = false
  }
}

onMounted(() => {
  void loadTemplates()
  void load()
})
</script>

<style scoped>
.audit-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  align-items: center;
  flex-wrap: wrap;
}
.audit-toolbar__spacer {
  flex: 1;
}
.audit-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.skip-list {
  margin-top: 12px;
  padding: 8px 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
  max-height: 200px;
  overflow-y: auto;
}
.skip-title {
  font-weight: 600;
  margin-bottom: 6px;
  font-size: 13px;
}
.skip-item {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.8;
}
</style>
