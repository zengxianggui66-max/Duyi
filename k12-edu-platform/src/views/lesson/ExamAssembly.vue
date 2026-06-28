<template>
  <motion.div class="exam-assembly-page" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }">
    <section class="page-hero">
      <motion.div class="container hero-row">
        <motion.div>
          <router-link to="/lesson" class="back-link">← 备课中心</router-link>
          <h1>📄 手工组卷</h1>
          <p>拖拽排序试题 · 预览试卷 · 保存并导出打印</p>
        </motion.div>
        <div class="hero-actions">
          <el-button @click="loadPreview" :loading="previewLoading">刷新预览</el-button>
          <el-button type="primary" :loading="saving" :disabled="!orderedQuestions.length" @click="savePaper">
            保存试卷
          </el-button>
          <el-button :disabled="!previewHtml" @click="handleExportWord(false)">导出 Word</el-button>
          <el-button :disabled="!previewHtml" @click="handleExportWord(true)">导出答案</el-button>
          <el-button :disabled="!previewHtml" @click="handlePrint">打印</el-button>
        </div>
      </motion.div>
    </section>

    <motion.div class="container assembly-body">
      <aside class="question-sort card">
        <header>
          <h3>试题顺序（{{ orderedQuestions.length }}）</h3>
          <el-input v-model="paperTitle" placeholder="试卷标题" style="margin-top: 8px" />
        </header>
        <p v-if="!orderedQuestions.length" class="empty">请先在备课中心选题加入资料篮</p>
        <ul v-else class="sort-list">
          <li
            v-for="(q, idx) in orderedQuestions"
            :key="q.id"
            class="sort-item"
            draggable="true"
            :class="{ dragging: dragIdx === idx }"
            @dragstart="dragIdx = idx"
            @dragover.prevent
            @drop="onDrop(idx)"
          >
            <span class="num">{{ idx + 1 }}</span>
            <span class="title">{{ q.title }}</span>
            <div class="sort-btns">
              <el-button link :disabled="idx === 0" @click="move(idx, -1)">↑</el-button>
              <el-button link :disabled="idx === orderedQuestions.length - 1" @click="move(idx, 1)">↓</el-button>
              <el-button link type="danger" @click="removeAt(idx)">×</el-button>
            </div>
          </li>
        </ul>
      </aside>

      <main class="preview-panel card">
        <h3>试卷预览</h3>
        <div v-loading="previewLoading" ref="printRef" class="preview-html" v-html="previewHtml" />
        <el-empty v-if="!previewLoading && !previewHtml" description="点击「刷新预览」生成试卷" />
        <p v-if="assemblyMeta" class="meta-line">
          满分 {{ assemblyMeta.totalScore }} 分 · {{ assemblyMeta.questionCount }} 题
          <span v-if="assemblyMeta.paperId"> · 已保存 #{{ assemblyMeta.paperId }}</span>
        </p>
      </main>
    </motion.div>
  </motion.div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { motion } from 'motion-v'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { prepApi, type ExamAssemblyVO } from '@/api/prep'
import { usePrepBasketStore } from '@/store/prepBasket'
import { useUserStore } from '@/store/user'
import { downloadBlob } from '@/utils/downloadBlob'

const route = useRoute()
const router = useRouter()
const dragIdx = ref<number | null>(null)
const prepBasket = usePrepBasketStore()
const userStore = useUserStore()

const paperTitle = ref('资料篮组卷试卷')
const previewHtml = ref('')
const previewLoading = ref(false)
const saving = ref(false)
const assemblyMeta = ref<ExamAssemblyVO | null>(null)
const printRef = ref<HTMLElement | null>(null)

const orderedQuestions = ref<{ id: number; title: string; refId: number; basketItemId?: number }[]>([])

function syncFromBasket() {
  orderedQuestions.value = prepBasket.questionItems.map((i) => ({
    id: i.refId,
    refId: i.refId,
    title: i.title,
    basketItemId: i.id,
  }))
}

async function persistOrder() {
  const ids = prepBasket.questionItems
    .map((i) => i.id)
    .filter((id): id is number => id != null)
  if (!ids.length) return
  const newOrder = orderedQuestions.value
    .map((q) => prepBasket.questionItems.find((i) => i.refId === q.refId)?.id)
    .filter((id): id is number => id != null)
  if (newOrder.length) {
    await prepBasket.reorder(newOrder)
  }
}

function onDrop(targetIdx: number) {
  if (dragIdx.value == null || dragIdx.value === targetIdx) return
  const arr = [...orderedQuestions.value]
  const [item] = arr.splice(dragIdx.value, 1)
  arr.splice(targetIdx, 0, item)
  orderedQuestions.value = arr
  dragIdx.value = null
  persistOrder()
  loadPreview()
}

function move(idx: number, delta: number) {
  const arr = [...orderedQuestions.value]
  const target = idx + delta
  if (target < 0 || target >= arr.length) return
  ;[arr[idx], arr[target]] = [arr[target], arr[idx]]
  orderedQuestions.value = arr
  persistOrder()
  loadPreview()
}

function removeAt(idx: number) {
  const item = orderedQuestions.value[idx]
  const basketItem = prepBasket.questionItems.find((i) => i.refId === item.refId)
  if (basketItem?.id) {
    prepBasket.removeItem(basketItem.id)
  }
  orderedQuestions.value.splice(idx, 1)
  loadPreview()
}

const questionIds = computed(() => orderedQuestions.value.map((q) => q.refId))

async function loadPreview() {
  if (!questionIds.value.length) {
    previewHtml.value = ''
    assemblyMeta.value = null
    return
  }
  previewLoading.value = true
  try {
    if (userStore.isLoggedIn) {
      await persistOrder()
      const res = await prepApi.previewExam(questionIds.value, paperTitle.value)
      const data = res.data.data
      assemblyMeta.value = data || null
      previewHtml.value = data?.previewHtml || buildLocalPreview()
    } else {
      previewHtml.value = buildLocalPreview()
    }
  } catch {
    previewHtml.value = buildLocalPreview()
  } finally {
    previewLoading.value = false
  }
}

function buildLocalPreview() {
  let html = `<div class="exam-paper-preview"><h1>${paperTitle.value}</h1><ol>`
  orderedQuestions.value.forEach((q, i) => {
    html += `<li>${i + 1}. ${q.title}</li>`
  })
  html += '</ol></div>'
  return html
}

async function savePaper() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后保存试卷')
    router.push('/login')
    return
  }
  if (!questionIds.value.length) {
    ElMessage.warning('资料篮中没有试题')
    return
  }
  saving.value = true
  try {
    const res = await prepApi.assembleExam({
      title: paperTitle.value,
      questionIds: questionIds.value,
      duration: 90,
    })
    const data = res.data.data
    assemblyMeta.value = data || null
    previewHtml.value = data?.previewHtml || previewHtml.value
    ElMessage.success(`试卷已保存${data?.paperId ? ' #' + data.paperId : ''}`)
  } finally {
    saving.value = false
  }
}

async function handleExportWord(answerOnly: boolean) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后导出')
    return
  }
  if (!questionIds.value.length) return
  try {
    const res = await prepApi.exportWord(
      { title: paperTitle.value, questionIds: questionIds.value },
      answerOnly
    )
    const name = (answerOnly ? paperTitle.value + '-参考答案' : paperTitle.value) + '.doc'
    downloadBlob(res.data as Blob, name)
    ElMessage.success('已开始下载 Word')
  } catch {
    ElMessage.error('导出失败')
  }
}

function handlePrint() {
  if (!previewHtml.value) {
    ElMessage.warning('请先生成预览')
    return
  }
  const w = window.open('', '_blank')
  if (!w) return
  w.document.write(`<!DOCTYPE html><html><head><meta charset="utf-8"><title>${paperTitle.value}</title>
    <style>body{font-family:serif;padding:24px;max-width:800px;margin:0 auto} h1{text-align:center} h2{margin-top:24px} .q-item{margin:12px 0}</style>
    </head><body>${previewHtml.value}</body></html>`)
  w.document.close()
  w.print()
}

watch(
  () => prepBasket.questionItems.length,
  () => syncFromBasket()
)

async function loadSavedPaper(paperId: number) {
  const res = await prepApi.getPaperDetail(paperId)
  const data = res.data.data
  if (!data) return
  paperTitle.value = data.title
  assemblyMeta.value = data
  previewHtml.value = data.previewHtml || ''
  const ids: number[] = []
  for (const sec of (data.sections || []) as { questions?: { id?: number }[] }[]) {
    for (const q of sec.questions || []) {
      if (q.id) ids.push(q.id)
    }
  }
  if (ids.length) {
    orderedQuestions.value = ids.map((id) => ({
      id,
      refId: id,
      title: `试题 #${id}`,
    }))
  }
}

onMounted(async () => {
  await prepBasket.fetchBasket()
  const paperId = route.query.paperId ? Number(route.query.paperId) : 0
  if (paperId && userStore.isLoggedIn) {
    await loadSavedPaper(paperId)
    return
  }
  syncFromBasket()
  if (orderedQuestions.value.length) {
    loadPreview()
  }
})
</script>

<style scoped>
.exam-assembly-page {
  min-height: 100%;
  background: var(--bg-page, #f5f7fa);
  padding-bottom: 48px;
}
.page-hero {
  background: linear-gradient(135deg, #1e3a5f, #2563eb);
  color: #fff;
  padding: 24px 0;
}
.back-link {
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
}
.hero-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 16px;
}
.page-hero h1 {
  margin: 8px 0 4px;
  font-size: 26px;
}
.hero-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.assembly-body {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 20px;
  margin-top: 24px;
}
@media (max-width: 900px) {
  .assembly-body {
    grid-template-columns: 1fr;
  }
}
.card {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  padding: 20px;
}
.question-sort header h3 {
  margin: 0;
  font-size: 16px;
}
.sort-list {
  list-style: none;
  padding: 0;
  margin: 16px 0 0;
}
.sort-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 0;
  border-bottom: 1px dashed var(--border-light);
  cursor: grab;
}
.sort-item.dragging {
  opacity: 0.5;
}
.num {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #eff6ff;
  color: #2563eb;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.title {
  flex: 1;
  font-size: 13px;
  line-height: 1.4;
}
.empty {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 16px;
}
.preview-panel h3 {
  margin: 0 0 16px;
}
.preview-html {
  min-height: 320px;
  line-height: 1.7;
  font-size: 14px;
}
.preview-html :deep(h1) {
  text-align: center;
  font-size: 20px;
}
.preview-html :deep(h2) {
  font-size: 16px;
  margin-top: 20px;
}
.meta-line {
  margin-top: 16px;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
