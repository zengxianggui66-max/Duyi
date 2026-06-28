<template>
  <div class="sensitive-words-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="keyword"
          placeholder="搜索敏感词"
          clearable
          style="width: 220px"
          size="small"
          @clear="search"
          @keyup.enter="search"
        />
        <el-select v-model="categoryFilter" placeholder="分类" clearable size="small" style="width: 130px" @change="search">
          <el-option :value="0" label="通用" />
          <el-option :value="1" label="政治" />
          <el-option :value="2" label="色情" />
          <el-option :value="3" label="广告" />
          <el-option :value="4" label="暴力" />
          <el-option :value="5" label="其他" />
        </el-select>
        <el-select v-model="levelFilter" placeholder="级别" clearable size="small" style="width: 110px" @change="search">
          <el-option :value="1" label="警告" />
          <el-option :value="2" label="阻断" />
        </el-select>
        <el-select v-model="statusFilter" placeholder="状态" clearable size="small" style="width: 100px" @change="search">
          <el-option :value="1" label="启用" />
          <el-option :value="0" label="禁用" />
        </el-select>
        <el-button size="small" @click="search">查询</el-button>
      </div>
      <div class="toolbar-right">
        <el-button
          v-if="selectedIds.length > 0"
          size="small"
          @click="batchEnable"
        >批量启用</el-button>
        <el-button
          v-if="selectedIds.length > 0"
          size="small"
          @click="batchDisable"
        >批量禁用</el-button>
        <el-button type="primary" size="small" @click="openCreateDialog">新增敏感词</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <el-table
      :data="tableData"
      v-loading="loading"
      size="small"
      stripe
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="45" />
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="word" label="敏感词" min-width="150">
        <template #default="{ row }">
          <el-tag type="danger" effect="plain" size="small">{{ row.word }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="categoryTagType(row.category)">{{ formatCategory(row.category) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="level" label="级别" width="80">
        <template #default="{ row }">
          <el-tag size="small" :type="row.level === 2 ? 'danger' : 'warning'">
            {{ row.level === 2 ? '阻断' : '警告' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
      <el-table-column prop="updateTime" label="更新时间" width="170">
        <template #default="{ row }">
          {{ formatDateTime(row.updateTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button link size="small" @click="toggleStatus(row)" :type="row.status === 1 ? 'warning' : 'success'">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-popconfirm title="确认删除该敏感词？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button link type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        small
        @current-change="loadTable"
        @size-change="loadTable"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" size="small">
        <el-form-item label="敏感词" prop="word">
          <el-input v-model="form.word" maxlength="100" placeholder="请输入敏感词" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" style="width:100%">
            <el-option :value="0" label="通用" />
            <el-option :value="1" label="政治" />
            <el-option :value="2" label="色情" />
            <el-option :value="3" label="广告" />
            <el-option :value="4" label="暴力" />
            <el-option :value="5" label="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="级别" prop="level">
          <el-radio-group v-model="form.level">
            <el-radio :value="1">警告</el-radio>
            <el-radio :value="2">阻断</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" maxlength="200" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" size="small" :loading="submitting" @click="submitForm">
          {{ editingId ? '保存' : '确认新增' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  listSensitiveWords, createSensitiveWord, updateSensitiveWord,
  deleteSensitiveWord, batchSetSensitiveWordStatus,
  type SensitiveWord, type SensitiveWordDTO,
} from '@/admin/api/quality'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<SensitiveWord[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

const keyword = ref('')
const categoryFilter = ref<number | undefined>(undefined)
const levelFilter = ref<number | undefined>(undefined)
const statusFilter = ref<number | undefined>(undefined)

const selectedIds = ref<number[]>([])

// 对话框
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = ref<SensitiveWordDTO & { word: string; category: number; level: number }>({
  word: '',
  category: 0,
  level: 1,
  remark: '',
})
const rules: FormRules = {
  word: [
    { required: true, message: '请输入敏感词', trigger: 'blur' },
    { max: 100, message: '最多100个字符', trigger: 'blur' },
  ],
}

const dialogTitle = ref('新增敏感词')

async function loadTable() {
  loading.value = true
  try {
    const res = await listSensitiveWords({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      category: categoryFilter.value,
      level: levelFilter.value,
      status: statusFilter.value,
    })
    tableData.value = res.records ?? []
    total.value = res.total ?? 0
  } catch (e) {
    console.error('加载敏感词列表失败', e)
  } finally {
    loading.value = false
  }
}

function search() {
  pageNum.value = 1
  loadTable()
}

function handleSelectionChange(rows: SensitiveWord[]) {
  selectedIds.value = rows.map(r => r.id)
}

function openCreateDialog() {
  editingId.value = null
  dialogTitle.value = '新增敏感词'
  form.value = { word: '', category: 0, level: 1, remark: '' }
  dialogVisible.value = true
}

function openEditDialog(row: SensitiveWord) {
  editingId.value = row.id
  dialogTitle.value = '编辑敏感词'
  form.value = {
    word: row.word,
    category: row.category ?? 0,
    level: row.level ?? 1,
    remark: row.remark ?? '',
  }
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const dto: SensitiveWordDTO = { ...form.value }
    if (editingId.value) {
      await updateSensitiveWord(editingId.value, dto)
      ElMessage.success('编辑成功')
    } else {
      await createSensitiveWord(dto)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadTable()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await deleteSensitiveWord(id)
    ElMessage.success('删除成功')
    loadTable()
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

async function toggleStatus(row: SensitiveWord) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await batchSetSensitiveWordStatus([row.id], newStatus)
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
    loadTable()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

async function batchEnable() {
  if (selectedIds.value.length === 0) return
  try {
    await batchSetSensitiveWordStatus(selectedIds.value, 1)
    ElMessage.success('批量启用成功')
    loadTable()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

async function batchDisable() {
  if (selectedIds.value.length === 0) return
  try {
    await batchSetSensitiveWordStatus(selectedIds.value, 0)
    ElMessage.success('批量禁用成功')
    loadTable()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

function formatCategory(cat: number): string {
  const map: Record<number, string> = {
    0: '通用', 1: '政治', 2: '色情', 3: '广告', 4: '暴力', 5: '其他',
  }
  return map[cat] ?? '通用'
}

function categoryTagType(cat: number): 'info' | 'danger' | 'warning' {
  const map: Record<number, 'info' | 'danger' | 'warning'> = {
    0: 'info', 1: 'danger', 2: 'danger', 3: 'warning', 4: 'danger', 5: 'info',
  }
  return map[cat] ?? 'info'
}

function formatDateTime(dt?: string): string {
  if (!dt) return '-'
  const d = new Date(dt)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(loadTable)
</script>

<style scoped>
.sensitive-words-page {
  padding: 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 8px;
}

.toolbar-left {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
