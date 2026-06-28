<template>
  <motion.div
    class="lesson-workbench"
    :initial="{ opacity: 0 }"
    :animate="{ opacity: 1 }"
  >
    <section class="wb-hero">
      <motion.div class="container hero-row">
        <div>
          <h1>📝 备课中心</h1>
          <p>资料篮选资源 · 试题篮选题 · 智能备课与组卷一站完成</p>
        </div>
        <router-link to="/lesson/papers" class="basket-link papers-link">我的试卷</router-link>
        <router-link to="/lesson/basket" class="basket-link">
          📋 资料篮
          <span v-if="prepBasket.totalCount" class="badge">{{ prepBasket.totalCount }}</span>
        </router-link>
      </motion.div>
    </section>

    <motion.div class="container wb-body">
      <nav class="wb-nav card">
        <button
          v-for="nav in navItems"
          :key="nav.key"
          type="button"
          class="nav-btn"
          :class="{ active: activeNav === nav.key }"
          @click="activeNav = nav.key"
        >
          <span class="nav-icon">{{ nav.icon }}</span>
          <span class="nav-label">{{ nav.name }}</span>
          <span class="nav-desc">{{ nav.desc }}</span>
        </button>
      </nav>

      <main class="wb-main card">
        <template v-if="activeNav === 'exam'">
          <div class="section-head">
            <h2>🎯 选题组卷</h2>
            <p>从试题库勾选题目加入右侧资料篮，支持真题/模拟/同步/专项筛选</p>
          </div>
          <div class="filter-bar">
            <el-select v-model="qFilter.gradeLevel" placeholder="学段" clearable style="width: 100px" @change="loadQuestions">
              <el-option label="小学" value="primary" />
              <el-option label="初中" value="junior" />
              <el-option label="高中" value="senior" />
            </el-select>
            <el-select v-model="qFilter.subject" placeholder="学科" clearable style="width: 100px" @change="loadQuestions">
              <el-option v-for="s in subjects" :key="s.key" :label="s.name" :value="s.key" />
            </el-select>
            <el-select v-model="qFilter.sourceType" placeholder="来源" clearable style="width: 110px" @change="loadQuestions">
              <el-option label="真题" value="real" />
              <el-option label="模拟" value="mock" />
              <el-option label="同步" value="sync" />
              <el-option label="专项" value="special" />
            </el-select>
            <el-input
              v-model="qFilter.keyword"
              placeholder="搜索题干/知识点"
              clearable
              style="flex: 1; max-width: 240px"
              @keyup.enter="loadQuestions"
            />
            <el-button type="primary" @click="loadQuestions">搜索</el-button>
          </div>
          <div v-loading="questionsLoading" class="question-list">
            <article v-for="q in questions" :key="q.id" class="question-card">
              <motion.div class="q-stem" v-html="stripHtml(q.stem).slice(0, 120) + '…'" />
              <div class="q-meta">
                <el-tag size="small" effect="plain">{{ subjectName(q.subject) }}</el-tag>
                <span>难度 {{ q.difficulty }}</span>
                <span>{{ q.score }} 分</span>
                <span v-if="q.sourceName">{{ q.sourceName }}</span>
              </div>
              <el-button type="primary" plain size="small" @click="addQuestion(q)">
                + 加入资料篮
              </el-button>
            </article>
            <el-empty v-if="!questionsLoading && !questions.length" description="暂无试题" />
          </div>
          <div class="pager">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="questionTotal"
              :page-size="qFilter.size"
              :current-page="qFilter.current"
              @current-change="onQPage"
            />
          </div>
        </template>

        <template v-else-if="activeNav === 'smart'">
          <div class="placeholder-module">
            <h2>🤖 智能备课</h2>
            <p>基于资料篮内资源一键生成教案、课件与学案</p>
            <router-link to="/lesson/smart">
              <el-button type="primary">进入智能备课</el-button>
            </router-link>
          </div>
        </template>

        <template v-else-if="activeNav === 'school'">
          <div class="section-head">
            <h2>🏫 名校套卷</h2>
            <p>名校同步卷、月考卷、期末卷，支持整卷入篮</p>
          </div>
          <PrepSchoolList />
        </template>

        <template v-else-if="activeNav === 'album'">
          <div class="section-head">
            <h2>💎 精品专辑</h2>
            <p>系列化备考合集，专辑及所含资源一键入篮</p>
          </div>
          <PrepAlbumList />
        </template>
      </main>

      <PrepBasketPanel @go-assemble="activeNav = 'exam'" />
    </motion.div>
  </motion.div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { motion } from 'motion-v'
import { prepApi } from '@/api/prep'
import type { QuestionItem } from '@/api/prep'
import { usePrepBasketStore } from '@/store/prepBasket'
import PrepBasketPanel from './components/PrepBasketPanel.vue'
import PrepSchoolList from './components/PrepSchoolList.vue'
import PrepAlbumList from './components/PrepAlbumList.vue'

const prepBasket = usePrepBasketStore()

const navItems = [
  { key: 'exam', icon: '🎯', name: '选题组卷', desc: '试题库勾选入篮' },
  { key: 'smart', icon: '🤖', name: '智能备课', desc: 'AI 生成教案课件' },
  { key: 'school', icon: '🏫', name: '名校套卷', desc: '名校同步与真题卷' },
  { key: 'album', icon: '💎', name: '精品专辑', desc: '专题资源合集' },
]

const activeNav = ref('exam')
const subjects = [
  { key: 'chinese', name: '语文' },
  { key: 'math', name: '数学' },
  { key: 'english', name: '英语' },
  { key: 'physics', name: '物理' },
  { key: 'chemistry', name: '化学' },
  { key: 'biology', name: '生物' },
  { key: 'history', name: '历史' },
  { key: 'geography', name: '地理' },
]

const qFilter = reactive({
  gradeLevel: 'junior',
  subject: 'math',
  sourceType: '',
  keyword: '',
  current: 1,
  size: 8,
})

const questions = ref<QuestionItem[]>([])
const questionTotal = ref(0)
const questionsLoading = ref(false)

function subjectName(key: string) {
  return subjects.find((s) => s.key === key)?.name || key
}

function stripHtml(html: string) {
  return html.replace(/<[^>]+>/g, '')
}

async function loadQuestions() {
  questionsLoading.value = true
  try {
    const res = await prepApi.getQuestionList({ ...qFilter })
    const data = res.data.data
    questions.value = data?.records || []
    questionTotal.value = data?.total || 0
  } finally {
    questionsLoading.value = false
  }
}

function onQPage(p: number) {
  qFilter.current = p
  loadQuestions()
}

async function addQuestion(q: QuestionItem) {
  await prepBasket.addItem({
    itemType: 'question',
    refId: q.id,
    title: stripHtml(q.stem).slice(0, 80) || `试题 #${q.id}`,
    subtitle: `${subjectName(q.subject)} · ${q.sourceName || q.sourceType || ''} · ${q.score}分`,
    score: Number(q.score),
    metaJson: JSON.stringify({
      questionType: q.questionType,
      difficulty: q.difficulty,
      gradeLevel: q.gradeLevel,
      subject: q.subject,
    }),
  })
}

onMounted(() => {
  prepBasket.fetchBasket()
  loadQuestions()
})
</script>

<style scoped>
.lesson-workbench {
  min-height: 100%;
  background: var(--bg-page, #f5f7fa);
  padding-bottom: 48px;
}
.wb-hero {
  background: linear-gradient(135deg, #4c1d95, #6d28d9, #4338ca);
  color: #fff;
  padding: 28px 0;
}
.hero-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.wb-hero h1 {
  margin: 0 0 6px;
  font-size: 28px;
}
.wb-hero p {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}
.basket-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.95);
  color: #6d28d9;
  border-radius: 999px;
  font-weight: 600;
  text-decoration: none;
}
.badge {
  background: #ef4444;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 999px;
}
.wb-body {
  display: grid;
  grid-template-columns: 200px 1fr 300px;
  gap: 20px;
  padding: 24px;
  align-items: start;
}
.wb-nav {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.nav-btn {
  text-align: left;
  padding: 12px;
  border: 1px solid transparent;
  border-radius: 10px;
  background: transparent;
  cursor: pointer;
  transition: all 0.2s;
}
.nav-btn:hover,
.nav-btn.active {
  background: #f5f3ff;
  border-color: #c4b5fd;
}
.nav-icon {
  font-size: 20px;
  display: block;
  margin-bottom: 4px;
}
.nav-label {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
  display: block;
}
.nav-desc {
  font-size: 11px;
  color: var(--text-secondary);
}
.wb-main {
  padding: 20px;
  min-height: 480px;
}
.section-head h2 {
  margin: 0 0 6px;
  font-size: 18px;
}
.section-head p {
  margin: 0 0 16px;
  font-size: 13px;
  color: var(--text-secondary);
}
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}
.question-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.question-card {
  padding: 14px;
  border: 1px solid var(--border-light);
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.q-stem {
  font-size: 14px;
  line-height: 1.6;
}
.q-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 12px;
  color: var(--text-secondary);
  align-items: center;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
.placeholder-module {
  text-align: center;
  padding: 60px 20px;
}
.placeholder-module h2 {
  margin-bottom: 8px;
}
.placeholder-module p {
  color: var(--text-secondary);
  margin-bottom: 20px;
}
.placeholder-module .el-button {
  margin: 0 6px;
}

@media (max-width: 1100px) {
  .wb-body {
    grid-template-columns: 1fr;
  }
}
</style>
