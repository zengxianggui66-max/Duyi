<template>
  <div>
    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="hint"
      title="统计热词 vs 运营热词"
      description="本页数据来自 search_hot_keyword（用户真实搜索累计）。与首页配置中的 home_hot_word 独立；可复制到运营热词，不会自动同步。"
    />

    <div class="toolbar">
      <el-checkbox v-model="showDisabled" @change="load">显示已禁用</el-checkbox>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="records" border stripe empty-text="暂无数据">
      <el-table-column prop="rank" label="排名" width="72" align="center" />
      <el-table-column prop="keyword" label="关键词" min-width="140" show-overflow-tooltip />
      <el-table-column prop="searchCount" label="搜索次数" width="100" align="right" />
      <el-table-column label="加权分" width="100" align="right">
        <template #default="{ row }">{{ row.boostScore ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="有效热度" width="100" align="right">
        <template #default="{ row }">{{ effectiveScore(row) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="88" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="previewSearch(row.keyword)">预览搜索</el-button>
          <el-button v-if="canEdit" link type="primary" @click="openBoost(row)">加权</el-button>
          <template v-if="canEdit">
            <el-button v-if="row.status === 1" link type="warning" @click="toggleStatus(row, 0)">禁用</el-button>
            <el-button v-else link type="success" @click="toggleStatus(row, 1)">启用</el-button>
          </template>
          <el-button v-if="canCopyHome" link type="primary" @click="copyToHomeHotWord(row)">
            复制到运营热词
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="boostVisible" title="人工加权" width="420px" destroy-on-close>
      <p class="boost-hint">有效热度 = 搜索次数 + 加权分。加权仅影响排序，不修改真实 search_count。</p>
      <el-form label-width="88px">
        <el-form-item label="关键词"><el-input :model-value="boostForm.keyword" disabled /></el-form-item>
        <el-form-item label="搜索次数"><el-input :model-value="String(boostForm.searchCount ?? 0)" disabled /></el-form-item>
        <el-form-item label="加权分" required>
          <el-input-number v-model="boostForm.boostScore" :min="0" :max="999999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="boostVisible = false">取消</el-button>
        <el-button type="primary" :loading="boostSaving" @click="saveBoost">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  buildSearchPreviewUrl,
  listSearchHotKeywords,
  setSearchHotKeywordStatus,
  updateSearchHotKeywordBoost,
  type SearchHotKeywordItem,
} from '@/admin/api/search'
import { adminHomeOpsApi } from '@/admin/api/homeOps'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:search:edit'))
const canCopyHome = computed(() => adminStore.hasPermission('admin:home:edit'))

const loading = ref(false)
const boostSaving = ref(false)
const showDisabled = ref(false)
const records = ref<SearchHotKeywordItem[]>([])
const boostVisible = ref(false)
const boostForm = reactive({
  id: 0,
  keyword: '',
  searchCount: 0,
  boostScore: 0,
})

function effectiveScore(row: SearchHotKeywordItem) {
  return (row.searchCount ?? 0) + (row.boostScore ?? 0)
}

async function load() {
  loading.value = true
  try {
    records.value = await listSearchHotKeywords(showDisabled.value)
  } finally {
    loading.value = false
  }
}

function previewSearch(keyword: string) {
  const url = buildSearchPreviewUrl(keyword)
  window.open(url, '_blank', 'noopener,noreferrer')
}

function openBoost(row: SearchHotKeywordItem) {
  if (!row.id) return
  boostForm.id = row.id
  boostForm.keyword = row.keyword
  boostForm.searchCount = row.searchCount ?? 0
  boostForm.boostScore = row.boostScore ?? 0
  boostVisible.value = true
}

async function saveBoost() {
  if (!boostForm.id) return
  boostSaving.value = true
  try {
    await updateSearchHotKeywordBoost(boostForm.id, boostForm.boostScore)
    ElMessage.success('加权已保存')
    boostVisible.value = false
    await load()
  } finally {
    boostSaving.value = false
  }
}

async function toggleStatus(row: SearchHotKeywordItem, status: number) {
  if (!row.id) return
  await setSearchHotKeywordStatus(row.id, status)
  ElMessage.success(status === 1 ? '已启用' : '已禁用')
  await load()
}

async function copyToHomeHotWord(row: SearchHotKeywordItem) {
  if (!canCopyHome.value) {
    ElMessage.warning('需要 admin:home:edit 权限')
    return
  }
  try {
    await ElMessageBox.confirm(
      `将「${row.keyword}」复制为首页运营热词（默认禁用，需人工启用）？`,
      '复制到运营热词',
      { type: 'info', confirmButtonText: '复制' },
    )
  } catch {
    return
  }
  try {
    await adminHomeOpsApi.createHotWord({
      label: row.keyword,
      actionType: 'search',
      navTarget: {
        type: 'search',
        stageKey: 'primary',
        subjectKey: 'chinese',
        versionKey: 'tongbian2024',
        keyword: row.keyword,
      },
      sort: 0,
      status: 0,
      remark: 'from search_hot_keyword',
    })
    ElMessage.success('已复制到运营热词（状态=禁用，请至首页配置启用）')
  } catch {
    ElMessage.error('复制失败，可能已存在同名运营热词')
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.hint {
  margin-bottom: 16px;
}
.boost-hint {
  margin: 0 0 16px;
  font-size: 13px;
  color: #909399;
}
</style>
