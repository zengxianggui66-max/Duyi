<template>
  <div class="prep-paper-list-page">
    <div class="container">
      <nav class="breadcrumb">
        <router-link to="/lesson">备课中心</router-link>
        <span>/</span>
        <span>我的试卷</span>
      </nav>
      <header class="page-head">
        <h1>📚 我的试卷</h1>
        <router-link to="/lesson/assemble">
          <el-button type="primary">手工组卷</el-button>
        </router-link>
      </header>
      <div v-loading="loading" class="paper-table card">
        <el-table v-if="papers.length" :data="papers" stripe>
          <el-table-column prop="title" label="试卷名称" min-width="200" />
          <el-table-column prop="totalScore" label="满分" width="80" />
          <el-table-column prop="questionCount" label="题数" width="80" />
          <el-table-column prop="duration" label="时长(分)" width="90" />
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="viewPaper(row.id)">查看</el-button>
              <el-button link @click="exportPaper(row, false)">Word</el-button>
              <el-button link @click="exportPaper(row, true)">答案</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无试卷，先去组卷吧" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { prepApi, type ExamPaperSummary } from '@/api/prep'
import { downloadBlob } from '@/utils/downloadBlob'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const papers = ref<ExamPaperSummary[]>([])

async function load() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  loading.value = true
  try {
    const res = await prepApi.listMyPapers()
    papers.value = res.data.data || []
  } finally {
    loading.value = false
  }
}

function viewPaper(id: number) {
  router.push({ path: '/lesson/assemble', query: { paperId: String(id) } })
}

async function exportPaper(row: ExamPaperSummary, answerOnly: boolean) {
  try {
    const detail = await prepApi.getPaperDetail(row.id)
    const ids = extractIds(detail.data.data)
    const res = await prepApi.exportWord(
      { title: row.title, questionIds: ids },
      answerOnly
    )
    const name = (answerOnly ? row.title + '-参考答案' : row.title) + '.doc'
    downloadBlob(res.data as Blob, name)
    ElMessage.success('已开始下载')
  } catch {
    ElMessage.error('导出失败')
  }
}

function extractIds(data: { sections?: { questions?: { id?: number }[] }[] } | undefined) {
  const ids: number[] = []
  for (const sec of data?.sections || []) {
    for (const q of sec.questions || []) {
      if (q.id) ids.push(q.id)
    }
  }
  return ids
}

onMounted(load)
</script>

<style scoped>
.prep-paper-list-page {
  padding: 24px 0 48px;
  min-height: 100%;
  background: var(--bg-page);
}
.breadcrumb {
  font-size: 14px;
  margin-bottom: 12px;
}
.breadcrumb a {
  color: var(--color-primary);
  text-decoration: none;
}
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid var(--border-light);
}
</style>
