<template>
  <div class="smart-exam-page">
    <section class="smart-banner">
      <div class="container">
        <router-link to="/exam" class="breadcrumb-link">试题试卷</router-link>
        <span class="breadcrumb-sep">/</span>
        <span>智能组卷</span>
        <h1>🎯 智能组卷</h1>
        <p>AI 驱动，基于知识图谱智能生成高质量试卷</p>
      </div>
    </section>

    <div class="container" style="padding: 32px 24px;">
      <div class="smart-layout">
        <!-- 左侧配置 -->
        <div class="config-panel">
          <div class="panel-card">
            <h3>📝 组卷配置</h3>
            <el-form label-position="top" :model="formData">
              <el-form-item label="学段">
                <el-select v-model="formData.gradeLevel" style="width: 100%;">
                  <el-option label="小学" value="primary" />
                  <el-option label="初中" value="junior" />
                  <el-option label="高中" value="senior" />
                </el-select>
              </el-form-item>

              <el-form-item label="学科">
                <el-select v-model="formData.subject" style="width: 100%;">
                  <el-option v-for="s in subjectList" :key="s.key" :label="s.icon + ' ' + s.name" :value="s.key" />
                </el-select>
              </el-form-item>

              <el-form-item label="难度等级">
                <el-slider v-model="formData.difficulty" :min="1" :max="5" :step="1" show-stops :marks="difficultyMarks" />
              </el-form-item>

              <el-form-item label="题型配置">
                <div class="type-config">
                  <div v-for="qt in questionTypes" :key="qt.key" class="type-row">
                    <el-checkbox v-model="qt.enabled">{{ qt.name }}</el-checkbox>
                    <el-input-number v-model="qt.count" :min="1" :max="30" size="small" style="width: 90px;" />
                  </div>
                </div>
              </el-form-item>

              <el-form-item label="总分">
                <el-input-number v-model="formData.totalScore" :min="10" :max="300" :step="10" style="width: 100%;" />
              </el-form-item>

              <el-form-item label="考试时长（分钟）">
                <el-input-number v-model="formData.duration" :min="30" :max="180" :step="10" style="width: 100%;" />
              </el-form-item>

              <el-form-item label="资料篮">
                <el-checkbox v-model="formData.useBasketQuestions">优先使用资料篮试题</el-checkbox>
              </el-form-item>

              <el-button type="primary" size="large" round style="width: 100%;" @click="handleGenerate" :loading="generating">
                🎯 智能组卷
              </el-button>
            </el-form>
          </div>
        </div>

        <!-- 右侧预览 -->
        <div class="preview-panel">
          <div v-if="!paperGenerated" class="preview-empty">
            <div class="empty-icon">🎯</div>
            <h3>智能组卷引擎</h3>
            <p>配置组卷参数后，点击「智能组卷」<br/>AI 将根据知识图谱自动匹配题目并生成试卷</p>
            <div class="features">
              <div class="feature-item">✅ 知识点全覆盖</div>
              <div class="feature-item">✅ 难度梯度合理</div>
              <div class="feature-item">✅ 题目去重检测</div>
              <div class="feature-item">✅ 支持多题型混合</div>
              <div class="feature-item">✅ AI 智能评分</div>
              <div class="feature-item">✅ 一键导出打印</div>
            </div>
          </div>

          <div v-else class="paper-preview">
            <div class="paper-header">
              <h2>{{ assemblyResult?.title || paperTitle }}</h2>
              <div class="paper-info">
                <span>总分：{{ assemblyResult?.totalScore ?? formData.totalScore }}分</span>
                <span>时长：{{ assemblyResult?.duration ?? formData.duration }}分钟</span>
                <span>共 {{ assemblyResult?.questionCount ?? totalQuestions }} 题</span>
                <span v-if="assemblyResult?.paperId">试卷 #{{ assemblyResult.paperId }}</span>
              </div>
              <div class="paper-actions">
                <el-button type="primary" @click="handleExport(false)">下载 Word</el-button>
                <el-button type="success" @click="handleExport(true)">下载答案</el-button>
                <router-link v-if="assemblyResult?.paperId" :to="`/lesson/assemble?paperId=${assemblyResult.paperId}`">
                  <el-button>编辑试卷</el-button>
                </router-link>
              </div>
            </div>
            <div class="paper-body preview-html" v-html="assemblyResult?.previewHtml" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { subjectConfig } from '@/constants'
import { prepApi, type ExamAssemblyVO } from '@/api/prep'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { downloadBlob } from '@/utils/downloadBlob'

const userStore = useUserStore()

const formData = ref({
  gradeLevel: 'junior',
  subject: 'math',
  difficulty: 3,
  totalScore: 100,
  duration: 90,
  useBasketQuestions: true,
})

const difficultyMarks = { 1: '简单', 2: '较易', 3: '中等', 4: '较难', 5: '困难' }

const questionTypes = ref([
  { key: 'choice', name: '选择题', enabled: true, count: 10, score: 3 },
  { key: 'fill', name: '填空题', enabled: true, count: 6, score: 4 },
  { key: 'judge', name: '判断题', enabled: false, count: 5, score: 2 },
  { key: 'short', name: '简答题', enabled: true, count: 4, score: 8 },
  { key: 'essay', name: '解答题', enabled: true, count: 3, score: 12 },
])

const subjectList = computed(() =>
  subjectConfig.filter(s => s.gradeLevels.includes(formData.value.gradeLevel))
)

const generating = ref(false)
const paperGenerated = ref(false)
const assemblyResult = ref<ExamAssemblyVO | null>(null)
const lastQuestionIds = ref<number[]>([])

const totalQuestions = computed(() =>
  questionTypes.value.filter(t => t.enabled).reduce((sum, t) => sum + t.count, 0)
)

const paperTitle = computed(() => {
  const subject = subjectList.value.find(s => s.key === formData.value.subject)?.name || '数学'
  return `${subject}智能组卷（${formData.value.totalScore}分）`
})

async function handleGenerate() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后使用智能组卷')
    return
  }
  generating.value = true
  try {
    const enabledTypes = questionTypes.value.filter((t) => t.enabled)
    const res = await prepApi.smartGenerateExam({
      gradeLevel: formData.value.gradeLevel,
      subject: formData.value.subject,
      difficulty: formData.value.difficulty,
      duration: formData.value.duration,
      useBasketQuestions: formData.value.useBasketQuestions,
      typeCounts: enabledTypes.map((t) => ({ questionType: t.key, count: t.count })),
    })
    assemblyResult.value = res.data.data || null
    lastQuestionIds.value = extractQuestionIds(assemblyResult.value)
    paperGenerated.value = true
    ElMessage.success('试卷已从试题库生成并保存！')
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '生成失败，请稍后重试'
    ElMessage.error(msg)
  } finally {
    generating.value = false
  }
}

function extractQuestionIds(data: ExamAssemblyVO | null) {
  const ids: number[] = []
  for (const sec of (data?.sections || []) as { questions?: { id?: number }[] }[]) {
    for (const q of sec.questions || []) {
      if (q.id) ids.push(q.id)
    }
  }
  return ids
}

async function handleExport(answerOnly: boolean) {
  if (!lastQuestionIds.value.length) {
    ElMessage.warning('请先生成试卷')
    return
  }
  try {
    const res = await prepApi.exportWord(
      {
        title: assemblyResult.value?.title,
        questionIds: lastQuestionIds.value,
      },
      answerOnly
    )
    const name =
      (answerOnly
        ? (assemblyResult.value?.title || '试卷') + '-参考答案'
        : assemblyResult.value?.title || '试卷') + '.doc'
    downloadBlob(res.data as Blob, name)
    ElMessage.success('已开始下载')
  } catch {
    ElMessage.error('导出失败')
  }
}
</script>

<style scoped>
.smart-banner {
  background: linear-gradient(135deg, #F59E0B, #EF4444, #EC4899);
  color: #fff;
  padding: 40px 0;
}
.breadcrumb-link { color: rgba(255,255,255,0.8); }
.breadcrumb-sep { margin: 0 8px; opacity: 0.6; }
.smart-banner h1 { font-size: 32px; font-weight: 700; margin: 12px 0 8px; }
.smart-banner p { opacity: 0.9; }

.smart-layout { display: flex; gap: 24px; }
.config-panel { width: 340px; flex-shrink: 0; }
.panel-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 24px;
  border: 1px solid var(--border-light);
  position: sticky;
  top: 88px;
}
.panel-card h3 { font-size: 17px; font-weight: 700; margin-bottom: 20px; padding-bottom: 12px; border-bottom: 1px solid var(--border-light); }

.type-config { width: 100%; }
.type-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 0;
}

.preview-panel { flex: 1; min-width: 0; }

.preview-empty {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 80px 40px;
  text-align: center;
  border: 2px dashed var(--border-color);
}
.empty-icon { font-size: 64px; margin-bottom: 16px; }
.preview-empty h3 { font-size: 20px; margin-bottom: 8px; }
.preview-empty p { color: var(--text-secondary); margin-bottom: 24px; line-height: 1.8; }
.features {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  max-width: 400px;
  margin: 0 auto;
  text-align: left;
}
.feature-item { padding: 8px 16px; background: var(--color-primary-bg); border-radius: var(--radius-sm); font-size: 14px; }

.paper-preview {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}
.paper-header {
  padding: 24px;
  border-bottom: 1px solid var(--border-light);
}
.paper-header h2 { font-size: 22px; margin-bottom: 8px; }
.paper-info { display: flex; gap: 20px; margin-bottom: 16px; font-size: 14px; color: var(--text-secondary); }
.paper-actions { display: flex; gap: 12px; }

.paper-body { padding: 24px; }
.paper-section { margin-bottom: 28px; }
.paper-section h3 { font-size: 16px; font-weight: 600; margin-bottom: 16px; color: var(--color-primary); padding-bottom: 8px; border-bottom: 1px solid var(--border-light); }
.question-item { margin-bottom: 14px; }
.q-text { font-size: 14px; line-height: 1.8; color: var(--text-primary); }
.q-options { padding-left: 24px; margin-top: 6px; }
.q-options p { font-size: 13px; color: var(--text-regular); margin-bottom: 4px; }
.preview-html { line-height: 1.7; font-size: 14px; }
.preview-html :deep(h1) { text-align: center; font-size: 20px; }
.preview-html :deep(h2) { font-size: 16px; margin-top: 20px; color: var(--color-primary); }

@media (max-width: 768px) {
  .smart-layout { flex-direction: column; }
  .config-panel { width: 100%; }
}
</style>
