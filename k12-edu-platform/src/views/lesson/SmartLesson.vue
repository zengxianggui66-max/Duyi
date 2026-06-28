<template>
  <div class="smart-lesson-page">
    <section class="smart-banner">
      <div class="container">
        <router-link to="/lesson" class="breadcrumb-link">备课中心</router-link>
        <span class="breadcrumb-sep">/</span>
        <span>智能备课</span>
        <h1>🤖 智能备课助手</h1>
        <p>AI 驱动，一键生成教学设计与备课素材</p>
      </div>
    </section>

    <div class="container" style="padding: 32px 24px;">
      <div class="smart-layout">
        <!-- 左侧配置面板 -->
        <div class="config-panel">
          <div class="panel-card">
            <h3>📚 备课配置</h3>

            <el-form label-position="top" :model="formData">
              <el-form-item label="学段">
                <el-select v-model="formData.gradeLevel" placeholder="选择学段" style="width: 100%;">
                  <el-option label="小学" value="primary" />
                  <el-option label="初中" value="junior" />
                  <el-option label="高中" value="senior" />
                </el-select>
              </el-form-item>

              <el-form-item label="学科">
                <el-select v-model="formData.subject" placeholder="选择学科" style="width: 100%;">
                  <el-option v-for="s in subjectList" :key="s.key" :label="s.icon + ' ' + s.name" :value="s.key" />
                </el-select>
              </el-form-item>

              <el-form-item label="年级">
                <el-select v-model="formData.grade" placeholder="选择年级" style="width: 100%;">
                  <el-option v-for="g in gradeList" :key="g" :label="g" :value="g" />
                </el-select>
              </el-form-item>

              <el-form-item label="课题名称">
                <el-input v-model="formData.topic" placeholder="例如：分数的初步认识" />
              </el-form-item>

              <el-form-item label="教材版本">
                <el-select v-model="formData.version" placeholder="选择版本" style="width: 100%;">
                  <el-option label="部编版" value="bubei" />
                  <el-option label="人教版" value="renjiao" />
                  <el-option label="北师大版" value="bshida" />
                  <el-option label="外研版" value="waiyan" />
                </el-select>
              </el-form-item>

              <el-form-item label="生成类型">
                <el-checkbox-group v-model="formData.types">
                  <el-checkbox label="courseware">教学课件</el-checkbox>
                  <el-checkbox label="lessonPlan">教案设计</el-checkbox>
                  <el-checkbox label="studyGuide">导学案</el-checkbox>
                  <el-checkbox label="exercises">配套练习</el-checkbox>
                </el-checkbox-group>
              </el-form-item>

              <el-form-item v-if="prepBasket.resourceItems.length" label="资料篮引用">
                <div class="basket-ref-list">
                  <el-tag
                    v-for="item in prepBasket.resourceItems.slice(0, 8)"
                    :key="`${item.itemType}-${item.refId}`"
                    size="small"
                    class="basket-tag"
                  >
                    {{ item.title }}
                  </el-tag>
                  <p v-if="prepBasket.resourceItems.length > 8" class="basket-more">
                    另有 {{ prepBasket.resourceItems.length - 8 }} 项资源将一并引用
                  </p>
                </div>
              </el-form-item>

              <el-button type="primary" size="large" round style="width: 100%;" @click="handleGenerate" :loading="generating">
                🤖 AI 智能生成
              </el-button>
            </el-form>
          </div>
        </div>

        <!-- 右侧预览区 -->
        <div class="preview-panel">
          <div v-if="!generated" class="preview-empty">
            <div class="empty-icon">🤖</div>
            <h3>AI 备课助手</h3>
            <p>配置左侧参数后，点击「AI 智能生成」<br/>即可自动生成教学设计与备课素材</p>
            <div class="features">
              <div class="feature-item">✅ 课件自动生成</div>
              <div class="feature-item">✅ 教案一键输出</div>
              <div class="feature-item">✅ 导学案配套</div>
              <div class="feature-item">✅ 练习题自动匹配</div>
              <div class="feature-item">✅ 知识点图谱</div>
              <div class="feature-item">✅ 个性化推荐</div>
            </div>
          </div>

          <div v-else class="preview-content">
            <div class="preview-tabs">
              <span
                v-for="tab in tabs"
                :key="tab.key"
                class="preview-tab"
                :class="{ active: activeTab === tab.key }"
                @click="activeTab = tab.key"
              >{{ tab.icon }} {{ tab.name }}</span>
            </div>
            <div class="preview-body">
              <div v-if="activeTab === 'courseware'" class="generated-content">
                <h2>{{ formData.topic }} - 教学课件</h2>
                <div class="ppt-preview">
                  <div v-for="(slide, idx) in 5" :key="idx" class="slide-card">
                    <span class="slide-num">第 {{ idx + 1 }} 页</span>
                    <div class="slide-content">
                      <div v-if="idx === 0" class="slide-title-slide">
                        <h3>{{ formData.topic }}</h3>
                        <p>{{ formData.version === 'renjiao' ? '人教版' : formData.version === 'bubei' ? '部编版' : formData.version === 'bshida' ? '北师大版' : '外研版' }} · {{ formData.grade }}</p>
                      </div>
                      <div v-else-if="idx === 1" class="slide-normal">
                        <h4>教学目标</h4>
                        <ul>
                          <li>知识与技能：理解并掌握{{ formData.topic }}的核心概念</li>
                          <li>过程与方法：通过观察、比较、归纳，培养思维能力</li>
                          <li>情感态度：激发学习兴趣，感受学科之美</li>
                        </ul>
                      </div>
                      <div v-else-if="idx === 2" class="slide-normal">
                        <h4>重点难点</h4>
                        <p><strong>教学重点：</strong>{{ formData.topic }}的核心原理与应用</p>
                        <p><strong>教学难点：</strong>从具体到抽象的思维跨越</p>
                      </div>
                      <div v-else-if="idx === 3" class="slide-normal">
                        <h4>教学过程</h4>
                        <p>1. 情境导入 → 2. 新知探究 → 3. 巩固练习 → 4. 课堂总结 → 5. 作业布置</p>
                      </div>
                      <div v-else class="slide-normal">
                        <h4>课堂小结</h4>
                        <p>回顾本节课所学内容，强调核心知识要点。</p>
                        <p>预习下一节课内容，完成课后练习。</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else-if="activeTab === 'lessonPlan'" class="generated-content">
                <h2>{{ formData.topic }} - 教案设计</h2>
                <div class="lesson-plan-content">
                  <div class="plan-section">
                    <h4>一、教学目标</h4>
                    <p><strong>1. 知识与技能：</strong>理解并掌握{{ formData.topic }}的核心概念，能够运用所学知识解决实际问题。</p>
                    <p><strong>2. 过程与方法：</strong>通过小组合作、自主探究的方式，培养学生的分析问题和解决问题的能力。</p>
                    <p><strong>3. 情感态度价值观：</strong>激发学习兴趣，培养严谨的学科思维和良好的学习习惯。</p>
                  </div>
                  <div class="plan-section">
                    <h4>二、教学重难点</h4>
                    <p><strong>重点：</strong>{{ formData.topic }}的基本概念和核心原理。</p>
                    <p><strong>难点：</strong>灵活运用知识解决综合问题。</p>
                  </div>
                  <div class="plan-section">
                    <h4>三、教学过程</h4>
                    <p><strong>环节一：情境导入（5分钟）</strong><br/>通过生活中的实例引入课题，激发学生兴趣。</p>
                    <p><strong>环节二：新知探究（20分钟）</strong><br/>教师引导学生观察、讨论、归纳，构建知识框架。</p>
                    <p><strong>环节三：巩固练习（10分钟）</strong><br/>分层练习，即时反馈，查漏补缺。</p>
                    <p><strong>环节四：课堂小结（5分钟）</strong><br/>回顾总结，梳理知识脉络。</p>
                  </div>
                </div>
              </div>
              <div v-else class="generated-content">
                <h2>{{ formData.topic }} - 导学案 / 练习</h2>
                <div class="placeholder-content">
                  <el-empty description="更多生成内容正在开发中..." />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { subjectConfig } from '@/constants'
import { lessonApi } from '@/api'
import { ElMessage } from 'element-plus'
import { usePrepBasketStore } from '@/store/prepBasket'

const prepBasket = usePrepBasketStore()

const formData = ref({
  gradeLevel: 'primary',
  subject: 'math',
  grade: '三年级上册',
  topic: '',
  version: 'renjiao',
  types: ['courseware', 'lessonPlan'],
})

const generating = ref(false)
const generated = ref(false)
const generatedData = ref<any>(null)
const activeTab = ref('courseware')

const tabs = [
  { key: 'courseware', name: '教学课件', icon: '📊' },
  { key: 'lessonPlan', name: '教案设计', icon: '📋' },
  { key: 'studyGuide', name: '导学案', icon: '📝' },
  { key: 'exercises', name: '配套练习', icon: '✏️' },
]

const subjectList = computed(() =>
  subjectConfig.filter(s => s.gradeLevels.includes(formData.value.gradeLevel))
)

const gradeList = computed(() => {
  const level = formData.value.gradeLevel
  if (level === 'primary') return ['一年级上册', '二年级上册', '三年级上册', '四年级上册', '五年级上册', '六年级上册']
  if (level === 'junior') return ['七年级上册', '八年级上册', '九年级上册']
  return ['高一上册', '高二上册', '高三上册']
})

watch(() => formData.value.gradeLevel, () => {
  formData.value.subject = subjectList.value[0]?.key || ''
  formData.value.grade = gradeList.value[0] || ''
})

onMounted(() => prepBasket.fetchBasket())

async function handleGenerate() {
  if (!formData.value.topic) {
    ElMessage.warning('请输入课题名称')
    return
  }
  generating.value = true
  try {
    const res = await lessonApi.generate({
      gradeLevel: formData.value.gradeLevel,
      subject: formData.value.subject,
      topic: formData.value.topic,
      grade: formData.value.grade,
      version: formData.value.version,
      types: formData.value.types,
      basketResourceTitles: prepBasket.resourceItems.map((i) => i.title),
      basketQuestionIds: prepBasket.questionItems.map((i) => i.refId),
    })
    generatedData.value = res.data.data ?? res.data
    generated.value = true
    ElMessage.success('备课方案生成成功！')
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '生成失败，请稍后重试'
    ElMessage.error(msg)
  } finally {
    generating.value = false
  }
}
</script>

<style scoped>
.smart-banner {
  background: linear-gradient(135deg, #7C3AED, #4361EE, #00b4d8);
  color: #fff;
  padding: 40px 0;
}
.breadcrumb-link { color: rgba(255,255,255,0.8); }
.breadcrumb-sep { margin: 0 8px; opacity: 0.6; }
.smart-banner h1 { font-size: 32px; font-weight: 700; margin: 12px 0 8px; }
.smart-banner p { opacity: 0.9; }

.smart-layout {
  display: flex;
  gap: 24px;
}

.config-panel { width: 340px; flex-shrink: 0; }
.panel-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 24px;
  border: 1px solid var(--border-light);
  position: sticky;
  top: 88px;
}
.panel-card h3 {
  font-size: 17px;
  font-weight: 700;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}
.basket-ref-list { display: flex; flex-wrap: wrap; gap: 6px; }
.basket-tag { max-width: 100%; }
.basket-more { width: 100%; font-size: 12px; color: var(--text-secondary); margin: 4px 0 0; }

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
.feature-item {
  padding: 8px 16px;
  background: var(--color-primary-bg);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-regular);
}

.preview-content {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}
.preview-tabs {
  display: flex;
  border-bottom: 1px solid var(--border-light);
  padding: 0 16px;
}
.preview-tab {
  padding: 14px 20px;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}
.preview-tab:hover { color: var(--color-primary); }
.preview-tab.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
  font-weight: 600;
}

.preview-body { padding: 24px; }
.generated-content h2 { font-size: 20px; margin-bottom: 20px; color: var(--text-primary); }

.ppt-preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.slide-card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  overflow: hidden;
}
.slide-num {
  display: block;
  padding: 6px 12px;
  background: var(--color-primary-bg);
  font-size: 12px;
  color: var(--color-primary);
  font-weight: 600;
}
.slide-content { padding: 16px; }
.slide-title-slide {
  text-align: center;
  padding: 20px;
}
.slide-title-slide h3 { font-size: 22px; margin-bottom: 8px; }
.slide-title-slide p { color: var(--text-secondary); }
.slide-normal h4 { font-size: 15px; margin-bottom: 8px; color: var(--color-primary); }
.slide-normal ul { padding-left: 20px; margin-bottom: 8px; }
.slide-normal li { margin-bottom: 4px; font-size: 13px; color: var(--text-regular); }
.slide-normal p { font-size: 13px; color: var(--text-regular); line-height: 1.6; }

.lesson-plan-content { line-height: 1.8; }
.plan-section { margin-bottom: 20px; }
.plan-section h4 { font-size: 16px; font-weight: 600; margin-bottom: 8px; color: var(--color-primary); }
.plan-section p { font-size: 14px; color: var(--text-regular); margin-bottom: 6px; }

@media (max-width: 768px) {
  .smart-layout { flex-direction: column; }
  .config-panel { width: 100%; }
}
</style>
