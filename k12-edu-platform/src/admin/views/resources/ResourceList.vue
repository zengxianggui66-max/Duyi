<template>
  <AdminPageShell
    title="资源管理"
    desc="统一资源主域：学科 / 专题 / 传统文化 / 竞赛 / 通用（edu_resource）五类 sourceType 均可筛选与管理"
  >
    <div class="resource-toolbar">
      <el-select v-model="sourceType" placeholder="来源类型" clearable style="width: 150px" @change="load">
        <el-option
          v-for="opt in RESOURCE_SOURCE_TYPE_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-input
        v-model="keyword"
        placeholder="搜索标题/目录/文件名"
        clearable
        style="width: 220px"
        @keyup.enter="load"
      />
      <el-select v-model="stage" placeholder="学段" clearable style="width: 100px" @change="load">
        <el-option label="小学" value="小学" />
        <el-option label="初中" value="初中" />
        <el-option label="高中" value="高中" />
      </el-select>
      <el-select v-model="subject" placeholder="学科" clearable style="width: 100px" @change="load">
        <el-option label="语文" value="语文" />
        <el-option label="数学" value="数学" />
        <el-option label="英语" value="英语" />
      </el-select>
      <el-select v-model="auditStatus" placeholder="审核状态" clearable style="width: 120px" @change="load">
        <el-option v-for="opt in auditStatusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-select v-model="publishStatus" placeholder="上架状态" clearable style="width: 120px" @change="load">
        <el-option v-for="opt in publishStatusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-select v-model="isFree" placeholder="价格" clearable style="width: 90px" @change="load">
        <el-option label="免费" :value="1" />
        <el-option label="付费" :value="0" />
      </el-select>
      <el-select v-model="isRecommend" placeholder="推荐" clearable style="width: 100px" @change="load">
        <el-option label="已推荐" :value="1" />
        <el-option label="未推荐" :value="0" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </div>

    <div v-if="selectedRows.length" class="resource-batch-bar">
      <span class="resource-batch-bar__hint">已选 {{ selectedRows.length }} 项</span>
      <el-button v-permission="'admin:resource:batch'" size="small" type="success" :disabled="!batchEligible.publish" @click="onBatch('publish')">
        批量上架{{ batchEligible.publish ? `(${batchEligible.publish})` : '' }}
      </el-button>
      <el-button v-permission="'admin:resource:batch'" size="small" :disabled="!batchEligible.offline" @click="onBatch('offline')">
        批量下架{{ batchEligible.offline ? `(${batchEligible.offline})` : '' }}
      </el-button>
      <el-button v-permission="'admin:resource:batch'" size="small" type="warning" :disabled="!batchEligible.recommend" @click="onBatch('recommend')">
        批量推荐{{ batchEligible.recommend ? `(${batchEligible.recommend})` : '' }}
      </el-button>
      <el-button v-permission="'admin:resource:batch'" size="small" :disabled="!batchEligible.unrecommend" @click="onBatch('unrecommend')">
        取消推荐{{ batchEligible.unrecommend ? `(${batchEligible.unrecommend})` : '' }}
      </el-button>
      <el-button v-permission="'admin:resource:batch'" size="small" type="danger" :disabled="!batchEligible.top" @click="onBatch('top')">
        批量置顶{{ batchEligible.top ? `(${batchEligible.top})` : '' }}
      </el-button>
      <el-button v-permission="'admin:resource:batch'" size="small" :disabled="!batchEligible.untop" @click="onBatch('untop')">
        取消置顶{{ batchEligible.untop ? `(${batchEligible.untop})` : '' }}
      </el-button>
      <el-button v-permission="'admin:resource:batch'" size="small" type="primary" plain :disabled="!batchEligible.restore" @click="onBatch('restore')">
        批量恢复{{ batchEligible.restore ? `(${batchEligible.restore})` : '' }}
      </el-button>
      <el-button v-permission="'admin:resource:batch'" size="small" type="danger" plain :disabled="!batchEligible.recycle" @click="onBatch('recycle')">
        移入回收站{{ batchEligible.recycle ? `(${batchEligible.recycle})` : '' }}
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" border stripe @selection-change="onSelectionChange">
      <el-table-column type="selection" width="48" fixed="left" />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip fixed="left" />
      <el-table-column label="来源" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="sourceTypeTagType(row.sourceType)">{{ sourceTypeLabel(row.sourceType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="stage" label="学段" width="72" />
      <el-table-column prop="subject" label="学科" width="72" />
      <el-table-column prop="edition" label="版本" width="110" show-overflow-tooltip />
      <el-table-column prop="gradeName" label="册别" width="110" show-overflow-tooltip />
      <el-table-column prop="module" label="栏目" width="96" show-overflow-tooltip />
      <el-table-column prop="type" label="类型" width="90" />
      <el-table-column prop="catalogNodeId" label="目录ID" width="88" />
      <el-table-column prop="uploaderId" label="上传人" width="80" />
      <el-table-column label="审核" width="96">
        <template #default="{ row }">
          <el-tag size="small" :type="getAuditStatusTagType(row)">{{ getAuditStatusLabel(row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="上架" width="96">
        <template #default="{ row }">
          <el-tag size="small" :type="getPublishStatusTagType(row)">{{ getPublishStatusLabel(row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="推荐" width="64" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.isRecommend === 1" size="small" type="warning">是</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="免费" width="64" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.isFree === 1" size="small" type="success">是</el-tag>
          <el-tag v-else-if="row.isFree === 0" size="small" type="info">否</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="downloadCount" label="下载" width="72" align="right" />
      <el-table-column prop="viewCount" label="浏览" width="72" align="right" />
      <el-table-column prop="uploadTime" label="上传时间" width="165" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="goDetail(row.globalId)">详情</el-button>
          <el-button v-if="canPublish(row)" v-permission="'admin:resource:edit'" link type="success" @click="onPublish(row.globalId)">上架</el-button>
          <el-button v-if="canOffline(row)" v-permission="'admin:resource:edit'" link type="warning" @click="onOffline(row.globalId)">下架</el-button>
          <el-button v-if="row.publishStatus === PUBLISH_STATUS.RECYCLED" v-permission="'admin:resource:edit'" link type="primary" @click="onRestore(row.globalId)">恢复</el-button>
          <el-button v-if="canRecycle(row)" v-permission="'admin:resource:delete'" link type="danger" @click="onDelete(row.globalId)">回收</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="resource-pagination">
      <el-pagination
        v-model:current-page="current"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[15, 30, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="onSizeChange"
      />
    </div>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminPageShell from '@/admin/components/AdminPageShell.vue'
import {
  batchPrimaryResources,
  deletePrimaryResource,
  offlinePrimaryResource,
  publishPrimaryResource,
  restorePrimaryResource,
  type BatchResourceAction,
} from '@/admin/api/resources'
import { listResourceMain, type ResourceMainItem } from '@/admin/api/resourceMain'
import {
  RESOURCE_SOURCE_TYPE_OPTIONS,
  resourceSourceTypeLabel,
  resourceSourceTypeTagType,
} from '@/admin/constants/resourceSourceTypes'
import {
  AUDIT_STATUS,
  PUBLISH_STATUS,
  canOffline,
  canPublish,
  canRecycle,
  getAuditStatusLabel,
  getAuditStatusTagType,
  getPublishStatusLabel,
  getPublishStatusTagType,
} from '@/admin/utils/resourceStatus'

const router = useRouter()
const loading = ref(false)
const records = ref<ResourceMainItem[]>([])
const selectedRows = ref<ResourceMainItem[]>([])

const sourceType = ref<string>()
const keyword = ref('')
const stage = ref<string>()
const subject = ref<string>()
const auditStatus = ref<number | undefined>()
const publishStatus = ref<number | undefined>()
const isRecommend = ref<number | undefined>()
const isFree = ref<number | undefined>()
const current = ref(1)
const size = ref(15)
const total = ref(0)

const auditStatusOptions = [
  { label: '草稿', value: AUDIT_STATUS.DRAFT },
  { label: '待审核', value: AUDIT_STATUS.PENDING },
  { label: '审核通过', value: AUDIT_STATUS.APPROVED },
  { label: '已驳回', value: AUDIT_STATUS.REJECTED },
  { label: '复审中', value: AUDIT_STATUS.RECHECKING },
]

const publishStatusOptions = [
  { label: '未上架', value: PUBLISH_STATUS.UNPUBLISHED },
  { label: '已上架', value: PUBLISH_STATUS.PUBLISHED },
  { label: '已下架', value: PUBLISH_STATUS.OFFLINE },
  { label: '定时上架', value: PUBLISH_STATUS.SCHEDULED },
  { label: '回收站', value: PUBLISH_STATUS.RECYCLED },
]

const batchLabels: Record<BatchResourceAction, string> = {
  publish: '批量上架',
  offline: '批量下架',
  recommend: '批量推荐',
  unrecommend: '取消推荐',
  top: '批量置顶',
  untop: '取消置顶',
  recycle: '移入回收站',
  restore: '批量恢复',
}

const batchHints: Record<BatchResourceAction, string> = {
  publish: '所选资源中没有“审核通过且未上架”的项目。',
  offline: '所选资源中没有“已上架”的项目。',
  recommend: '所选资源中没有“已上架”的项目。',
  unrecommend: '所选资源中没有“已上架”的项目。',
  top: '所选资源中没有“已上架”的项目。',
  untop: '所选资源中没有“已上架”的项目。',
  recycle: '所选资源无法移入回收站，已上架资源请先下架。',
  restore: '所选资源中没有“回收站”的项目。',
}

const batchEligible = computed(() => {
  const rows = selectedRows.value
  const published = rows.filter((r) => r.publishStatus === PUBLISH_STATUS.PUBLISHED).length
  return {
    publish: rows.filter((r) => canPublish(r)).length,
    offline: published,
    recommend: published,
    unrecommend: published,
    top: published,
    untop: published,
    recycle: rows.filter((r) => canRecycle(r)).length,
    restore: rows.filter((r) => r.publishStatus === PUBLISH_STATUS.RECYCLED).length,
  }
})

function sourceTypeLabel(t?: string) {
  return resourceSourceTypeLabel(t)
}

function sourceTypeTagType(t?: string): 'success' | 'primary' | 'warning' | 'info' | 'danger' | undefined {
  return resourceSourceTypeTagType(t)
}

function onSelectionChange(rows: ResourceMainItem[]) {
  selectedRows.value = rows
}

async function load() {
  loading.value = true
  try {
    const page = await listResourceMain({
      current: current.value,
      size: size.value,
      sourceType: sourceType.value || undefined,
      keyword: keyword.value || undefined,
      stage: stage.value || undefined,
      subject: subject.value || undefined,
      auditStatus: auditStatus.value,
      publishStatus: publishStatus.value,
      isRecommend: isRecommend.value,
      isFree: isFree.value,
    })
    records.value = page.records
    total.value = page.total
    selectedRows.value = []
  } finally {
    loading.value = false
  }
}

function onSizeChange() {
  current.value = 1
  load()
}

function goDetail(globalId: number) {
  router.push(`/admin/resource-main/${globalId}`)
}

async function onPublish(globalId: number) {
  await ElMessageBox.confirm('确认上架该资源？', '上架', { type: 'info' })
  await publishPrimaryResource(globalId)
  ElMessage.success('已上架')
  load()
}

async function onOffline(globalId: number) {
  await ElMessageBox.confirm('确认下架该资源？', '下架', { type: 'warning' })
  await offlinePrimaryResource(globalId)
  ElMessage.success('已下架')
  load()
}

async function onDelete(globalId: number) {
  await ElMessageBox.confirm('确认移入回收站？', '回收资源', { type: 'warning' })
  await deletePrimaryResource(globalId)
  ElMessage.success('已移入回收站')
  load()
}

async function onRestore(globalId: number) {
  await ElMessageBox.confirm('确认从回收站恢复？恢复后资源为“已下架”。', '恢复资源', { type: 'info' })
  await restorePrimaryResource(globalId)
  ElMessage.success('已恢复为已下架')
  load()
}

async function onBatch(action: BatchResourceAction) {
  const eligible = batchEligible.value[action]
  if (!eligible) {
    ElMessage.warning(batchHints[action])
    return
  }
  await ElMessageBox.confirm(`确认对 ${eligible} 项执行“${batchLabels[action]}”？`, batchLabels[action], {
    type: 'warning',
  })
  const ids = selectedRows.value.map((r) => r.globalId)
  const result = await batchPrimaryResources(ids, action)
  if (result.skipCount > 0) {
    ElMessage.warning(result.message || `成功 ${result.successCount} 条，跳过 ${result.skipCount} 条`)
  } else {
    ElMessage.success(result.message || '批量操作完成')
  }
  load()
}

onMounted(load)
</script>

<style scoped>
.resource-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.resource-batch-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.resource-batch-bar__hint {
  font-size: 13px;
  color: #606266;
  margin-right: 4px;
}

.resource-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
