<template>
  <el-dialog
    v-model="visible"
    :title="`上传资源 · ${channelName}`"
    width="800px"
    :close-on-click-modal="false"
    append-to-body
    destroy-on-close
    class="upload-dialog"
    @closed="resetForm"
  >
    <!-- 步骤条 -->
    <el-steps :active="currentStep" finish-status="success" align-center class="dialog-steps">
      <el-step title="基础信息" />
      <el-step title="上传文件" />
      <el-step title="分类配置" />
      <el-step title="确认提交" />
    </el-steps>

    <div class="step-body">
      <!-- ====== Step 0: 基础信息 ====== -->
      <div v-show="currentStep === 0" class="step-panel">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
          <el-form-item label="资源标题" prop="title">
            <el-input v-model="form.title" placeholder="如：《学会感恩》主题班会教案PPT" maxlength="100" show-word-limit />
          </el-form-item>

          <el-form-item label="资源简介" prop="description">
            <el-input
              v-model="form.description"
              type="textarea" :rows="3"
              placeholder="描述资源的核心内容、适用场景、特色亮点..."
              maxlength="500" show-word-limit
            />
          </el-form-item>

          <el-form-item label="资源类型" prop="resourceType">
            <el-radio-group v-model="form.resourceType">
              <el-radio-button value="courseware">课件</el-radio-button>
              <el-radio-button value="lesson_plan">教案</el-radio-button>
              <el-radio-button value="material">素材</el-radio-button>
              <el-radio-button value="document">文档</el-radio-button>
              <el-radio-button value="video">视频</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="适用学段" prop="gradeLevel">
                <el-select v-model="form.gradeLevel" placeholder="请选择" style="width:100%" @change="onGradeLevelChange">
                  <el-option
                    v-for="stage in stageOptions"
                    :key="stage.key"
                    :label="stage.name"
                    :value="stage.key"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="适用年级">
                <el-select v-model="form.grade" placeholder="请选择" style="width:100%" :disabled="!form.gradeLevel">
                  <el-option v-for="g in currentGrades" :key="g.value" :label="g.label" :value="g.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="学科">
                <el-select v-model="form.subject" placeholder="请选择" style="width:100%" :disabled="!form.gradeLevel">
                  <el-option v-for="s in currentSubjects" :key="s.value" :label="s.label" :value="s.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="适用场景">
                <el-input v-model="form.scenario" placeholder="如：主题班会、安全教育" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="教材版本">
                <el-select v-model="form.version" placeholder="请选择" style="width:100%">
                  <el-option label="通用版" value="通用版" />
                  <el-option label="人教版" value="人教版" />
                  <el-option label="北师大版" value="北师大版" />
                  <el-option label="苏教版" value="苏教版" />
                  <el-option label="外研版" value="外研版" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="资源亮点">
            <div class="highlights-input">
              <el-tag
                v-for="(h, i) in form.highlights"
                :key="i"
                closable
                @close="removeHighlight(i)"
                class="highlight-tag"
              >{{ h }}</el-tag>
              <el-input
                v-if="showHighlightInput"
                ref="highlightInputRef"
                v-model="highlightInputVal"
                size="small"
                style="width: 160px"
                @keyup.enter="addHighlight"
                @blur="addHighlight"
                placeholder="输入后回车确认"
              />
              <el-button v-else size="small" @click="showHighlightInput = true">+ 添加亮点</el-button>
            </div>
          </el-form-item>

          <el-form-item label="是否免费">
            <el-switch v-model="form.isFree" :active-value="1" :inactive-value="0" active-text="免费" inactive-text="付费" />
            <el-input-number
              v-if="!form.isFree"
              v-model="form.price"
              :min="0.1" :max="99.9" :precision="1" :step="1"
              class="price-input"
              placeholder="设置价格"
            />
            <span v-if="!form.isFree" class="price-hint">元</span>
          </el-form-item>
        </el-form>
      </div>

      <!-- ====== Step 1: 文件上传 ====== -->
      <div v-show="currentStep === 1" class="step-panel">
        <!-- 课件主文件 -->
        <div class="upload-section">
          <div class="upload-section-header">
            <span class="upload-section-title">📊 课件主文件</span>
            <span class="upload-section-hint">支持 .ppt/.pptx/.pdf，≤ 50MB，<strong>必须上传</strong></span>
          </div>
          <el-upload
            class="file-uploader"
            drag
            :auto-upload="false"
            :limit="1"
            accept=".ppt,.pptx,.pdf"
            :before-upload="(f: any) => beforeUpload(f, 50)"
            :on-change="(f: any) => onFileChange(f, 'courseware')"
            :on-remove="() => onFileRemove('courseware')"
            :file-list="coursewareFileList"
          >
            <div class="upload-placeholder">
              <el-icon class="upload-icon" size="36"><UploadFilled /></el-icon>
              <p>拖拽或点击上传课件</p>
              <p class="upload-hint-text">.ppt / .pptx / .pdf</p>
            </div>
          </el-upload>
          <div v-if="files.courseware" class="file-info-bar success">
            <el-icon><DocumentChecked /></el-icon>
            <span>{{ files.courseware.name }}</span>
            <span class="file-size">{{ formatSize(files.courseware.size) }}</span>
            <el-tag type="success" size="small">已就绪</el-tag>
          </div>
        </div>

        <!-- 教案文件 -->
        <div class="upload-section">
          <div class="upload-section-header">
            <span class="upload-section-title">📋 配套教案</span>
            <span class="upload-section-hint">支持 .doc/.docx/.pdf，≤ 10MB，可选</span>
          </div>
          <el-upload
            class="file-uploader file-uploader-sm"
            drag
            :auto-upload="false"
            :limit="1"
            accept=".doc,.docx,.pdf"
            :before-upload="(f: any) => beforeUpload(f, 10)"
            :on-change="(f: any) => onFileChange(f, 'lessonPlan')"
            :on-remove="() => onFileRemove('lessonPlan')"
            :file-list="lessonPlanFileList"
          >
            <div class="upload-placeholder-sm">
              <el-icon size="24"><UploadFilled /></el-icon>
              <span>拖拽或点击上传教案（可选）</span>
            </div>
          </el-upload>
          <div v-if="files.lessonPlan" class="file-info-bar success">
            <el-icon><DocumentChecked /></el-icon>
            <span>{{ files.lessonPlan.name }}</span>
            <span class="file-size">{{ formatSize(files.lessonPlan.size) }}</span>
          </div>
        </div>

        <!-- 素材包 -->
        <div class="upload-section">
          <div class="upload-section-header">
            <span class="upload-section-title">📦 拓展素材包</span>
            <span class="upload-section-hint">支持 .zip/.rar，≤ 100MB，可选</span>
          </div>
          <el-upload
            class="file-uploader file-uploader-sm"
            drag
            :auto-upload="false"
            :limit="1"
            accept=".zip,.rar"
            :before-upload="(f: any) => beforeUpload(f, 100)"
            :on-change="(f: any) => onFileChange(f, 'material')"
            :on-remove="() => onFileRemove('material')"
            :file-list="materialFileList"
          >
            <div class="upload-placeholder-sm">
              <el-icon size="24"><UploadFilled /></el-icon>
              <span>拖拽或点击上传素材包（可选）</span>
            </div>
          </el-upload>
          <div v-if="files.material" class="file-info-bar success">
            <el-icon><DocumentChecked /></el-icon>
            <span>{{ files.material.name }}</span>
            <span class="file-size">{{ formatSize(files.material.size) }}</span>
          </div>
        </div>

        <!-- 封面图 -->
        <div class="upload-section">
          <div class="upload-section-header">
            <span class="upload-section-title">🖼️ 资源封面图</span>
            <span class="upload-section-hint">支持 .jpg/.png，≤ 5MB，建议尺寸 400×250px</span>
          </div>
          <div class="cover-upload-area">
            <el-upload
              class="cover-uploader"
              :auto-upload="false"
              :show-file-list="false"
              accept=".jpg,.jpeg,.png,.webp"
              :before-upload="(f: any) => beforeUpload(f, 5)"
              :on-change="onCoverChange"
            >
              <div v-if="coverPreviewUrl" class="cover-preview">
                <img :src="coverPreviewUrl" alt="封面预览" />
                <div class="cover-overlay">点击更换封面</div>
              </div>
              <div v-else class="cover-placeholder">
                <el-icon size="32"><Picture /></el-icon>
                <span>点击上传封面图</span>
              </div>
            </el-upload>
          </div>
        </div>

        <!-- 上传说明 -->
        <el-alert type="info" :closable="false" show-icon title="上传说明">
          <template #default>
            <ul class="upload-tips">
              <li>课件主文件为必须上传，教案和素材包可选</li>
              <li>上传完成后系统将自动分析文件，生成PPT预览图</li>
              <li>文件上传成功后，内容将进入审核流程，审核通过后正式发布</li>
            </ul>
          </template>
        </el-alert>
      </div>

      <!-- ====== Step 2: 分类配置 ====== -->
      <div v-show="currentStep === 2" class="step-panel">
        <el-form :model="form" label-width="110px">
          <el-form-item label="频道分类">
            <el-select v-model="form.channelCategory" placeholder="选择频道子分类" style="width:100%">
              <el-option v-for="tab in channelTabs" :key="tab.key" :label="tab.name" :value="tab.key" />
            </el-select>
          </el-form-item>

          <el-form-item label="难度等级">
            <el-radio-group v-model="form.difficulty">
              <el-radio value="basic">基础</el-radio>
              <el-radio value="improved">提高</el-radio>
              <el-radio value="excellent">培优</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="使用场景">
            <el-checkbox-group v-model="form.scenarios">
              <el-checkbox value="classroom">课堂教学</el-checkbox>
              <el-checkbox value="homework">课后作业</el-checkbox>
              <el-checkbox value="review">复习备考</el-checkbox>
              <el-checkbox value="competition">竞赛辅导</el-checkbox>
              <el-checkbox value="parent">家长辅导</el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item label="资源标签">
            <el-checkbox-group v-model="form.tags">
              <el-checkbox value="quality">精品推荐</el-checkbox>
              <el-checkbox value="newup">新上传</el-checkbox>
              <el-checkbox value="hot">热门</el-checkbox>
              <el-checkbox value="complete">完整课包</el-checkbox>
              <el-checkbox value="interactive">互动性强</el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item label="允许预览">
            <el-switch v-model="form.allowPreview" :active-value="1" :inactive-value="0" />
            <span class="hint-text">开启后可在线预览资源内容</span>
          </el-form-item>

          <el-form-item label="版本说明">
            <el-input v-model="form.versionNote" placeholder="如：第一版，2026年春季更新" />
          </el-form-item>
        </el-form>
      </div>

      <!-- ====== Step 3: 预览确认 ====== -->
      <div v-show="currentStep === 3" class="step-panel">
        <!-- 预览卡片 -->
        <div class="preview-card">
          <div class="preview-cover" :style="{ background: previewColor }">
            <img v-if="coverPreviewUrl" :src="coverPreviewUrl" class="preview-cover-img" alt="封面" />
            <span v-else class="preview-icon">{{ getResourceIcon() }}</span>
            <span class="preview-type-badge">{{ getResourceTypeName() }}</span>
            <span class="preview-free-badge" v-if="form.isFree">免费</span>
          </div>
          <div class="preview-info">
            <h3 class="preview-title">{{ form.title || '（未填写标题）' }}</h3>
            <div class="preview-tags">
              <el-tag v-if="form.gradeLevel" size="small" type="success">{{ gradeLevelLabel }}</el-tag>
              <el-tag v-if="form.grade" size="small">{{ gradeLabel }}</el-tag>
              <el-tag v-if="form.subject" size="small" type="warning">{{ subjectLabel }}</el-tag>
              <el-tag v-if="form.resourceType" size="small" type="info">{{ getResourceTypeName() }}</el-tag>
              <el-tag v-if="form.scenario" size="small">{{ form.scenario }}</el-tag>
              <el-tag size="small" type="danger" v-if="form.isFree">免费</el-tag>
            </div>
            <p class="preview-desc">{{ form.description || '（未填写简介）' }}</p>
          </div>
        </div>

        <!-- 文件列表 -->
        <div class="preview-files">
          <h4 class="preview-subtitle">📎 上传文件</h4>
          <div class="preview-file-list">
            <div v-if="files.courseware" class="preview-file-item">
              <el-icon color="#409EFF"><DocumentChecked /></el-icon>
              <span>{{ files.courseware.name }}</span>
              <span class="file-size">{{ formatSize(files.courseware.size) }}</span>
              <el-tag size="small" type="primary">课件</el-tag>
            </div>
            <div v-if="files.lessonPlan" class="preview-file-item">
              <el-icon color="#67C23A"><DocumentChecked /></el-icon>
              <span>{{ files.lessonPlan.name }}</span>
              <span class="file-size">{{ formatSize(files.lessonPlan.size) }}</span>
              <el-tag size="small" type="success">教案</el-tag>
            </div>
            <div v-if="files.material" class="preview-file-item">
              <el-icon color="#E6A23C"><DocumentChecked /></el-icon>
              <span>{{ files.material.name }}</span>
              <span class="file-size">{{ formatSize(files.material.size) }}</span>
              <el-tag size="small" type="warning">素材包</el-tag>
            </div>
            <el-empty v-if="!files.courseware && !files.lessonPlan && !files.material" description="未上传任何文件" :image-size="60" />
          </div>
        </div>

        <!-- 提交提示 -->
        <el-alert v-if="!canSubmit" type="warning" show-icon :closable="false" title="请补充以下信息再提交">
          <ul class="warn-list">
            <li v-if="!form.title">资源标题未填写</li>
            <li v-if="!form.description">资源简介未填写</li>
            <li v-if="!form.gradeLevel">适用学段未选择</li>
            <li v-if="!form.resourceType">资源类型未选择</li>
            <li v-if="!files.courseware">课件主文件未上传</li>
          </ul>
        </el-alert>

        <el-alert v-else type="success" show-icon :closable="false" title="信息填写完整，可以提交审核！" description="提交后资源将进入审核流程，审核通过后将正式发布到平台。" />
      </div>
    </div>

    <!-- 底部按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <div class="footer-left">
          <el-button v-if="currentStep > 0" @click="currentStep--">← 上一步</el-button>
        </div>
        <div class="footer-right">
          <el-button @click="saveDraft">💾 保存草稿</el-button>
          <el-button @click="handleClose">取消</el-button>
          <el-button
            v-if="currentStep < 3"
            type="primary"
            :disabled="!canNext"
            @click="goNext"
          >
            下一步 →
          </el-button>
          <el-button
            v-else
            type="success"
            :disabled="!canSubmit"
            :loading="submitting"
            @click="handleSubmit"
          >
            🚀 提交审核
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, reactive, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { UploadFilled, DocumentChecked, Picture } from '@element-plus/icons-vue'
import { fileApi } from '@/api'
import { useUploadTaxonomyCascade } from '@/composables/useUploadTaxonomyCascade'

// ===== Props & Emits =====
const props = defineProps<{
  modelValue: boolean
  channelName?: string
  channelType?: string
  channelTabs?: { key: string; name: string; icon: string }[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'uploaded': []
}>()

const router = useRouter()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

// ===== State =====
const currentStep = ref(0)
const submitting = ref(false)
const formRef = ref<FormInstance>()

// 文件列表（用于 el-upload 回显）
const coursewareFileList = ref<any[]>([])
const lessonPlanFileList = ref<any[]>([])
const materialFileList = ref<any[]>([])

// 实际文件对象
const files = reactive<{ courseware: File | null; lessonPlan: File | null; material: File | null }>({
  courseware: null,
  lessonPlan: null,
  material: null
})

// 封面图预览
const coverPreviewUrl = ref('')
const coverFile = ref<File | null>(null)

// 亮点标签
const showHighlightInput = ref(false)
const highlightInputVal = ref('')
const highlightInputRef = ref<any>()

// ===== Form Data =====
const form = reactive({
  title: '',
  description: '',
  resourceType: 'courseware',
  gradeLevel: '',
  grade: '',
  subject: '',
  module: '',
  editionName: '',
  gradeName: '',
  unitName: '',
  lessonName: '',
  teachingType: '',
  catalogNodeId: undefined as number | undefined,
  scenario: '',
  version: '通用版',
  isFree: 1,
  price: 9.9,
  highlights: [] as string[],
  channelCategory: 'all',
  difficulty: 'basic',
  scenarios: [] as string[],
  tags: [] as string[],
  allowPreview: 1,
  versionNote: ''
})

const rules = {
  title: [{ required: true, message: '请填写资源标题', trigger: 'blur' }],
  description: [{ required: true, message: '请填写资源简介', trigger: 'blur' }],
  gradeLevel: [{ required: true, message: '请选择适用学段', trigger: 'change' }],
  resourceType: [{ required: true, message: '请选择资源类型', trigger: 'change' }]
}

// ===== 联动数据（taxonomy API） =====
const {
  stageOptions,
  currentSubjects,
  currentGrades,
  currentTextbookVersions,
  subjectLabel: taxonomySubjectLabel,
  handleGradeLevelChange,
  handleSubjectChange,
  initCascade,
} = useUploadTaxonomyCascade(form)

async function ensureStageOptions() {
  if (!stageOptions.value.length) {
    await initCascade()
  }
}

function onGradeLevelChange() {
  void handleGradeLevelChange()
}

function onSubjectChange() {
  void handleSubjectChange()
}

// ===== Computed Labels =====
const gradeLevelLabel = computed(() => {
  return stageOptions.value.find((s) => s.key === form.gradeLevel)?.name || ''
})

const gradeLabel = computed(() => {
  return currentGrades.value.find(g => g.value === form.grade)?.label || form.grade || ''
})

const subjectLabel = taxonomySubjectLabel

const previewColor = computed(() => {
  const colors = [
    'linear-gradient(135deg, #667eea, #764ba2)',
    'linear-gradient(135deg, #FF6B6B, #ee5a24)',
    'linear-gradient(135deg, #4facfe, #00f2fe)',
    'linear-gradient(135deg, #43e97b, #38f9d7)',
  ]
  return colors[form.title.length % colors.length]
})

// ===== 资源图标/类型名 =====
function getResourceIcon() {
  const m: Record<string, string> = { courseware: '📊', lesson_plan: '📋', material: '📦', document: '📄', video: '🎬' }
  return m[form.resourceType] || '📄'
}

function getResourceTypeName() {
  const m: Record<string, string> = { courseware: '课件', lesson_plan: '教案', material: '素材', document: '文档', video: '视频' }
  return m[form.resourceType] || form.resourceType
}

// ===== 亮点标签 =====
function addHighlight() {
  const val = highlightInputVal.value.trim()
  if (val && form.highlights.length < 6) {
    form.highlights.push(val)
  }
  highlightInputVal.value = ''
  showHighlightInput.value = false
}

function removeHighlight(index: number) {
  form.highlights.splice(index, 1)
}

// ===== 文件操作 =====
function beforeUpload(file: File, maxMB: number) {
  const sizeMB = file.size / 1024 / 1024
  if (sizeMB > maxMB) {
    ElMessage.error(`文件大小不能超过 ${maxMB}MB，当前 ${sizeMB.toFixed(1)}MB`)
    return false
  }
  return true
}

function onFileChange(fileInfo: any, type: 'courseware' | 'lessonPlan' | 'material') {
  if (fileInfo.raw) {
    files[type] = fileInfo.raw
  }
}

function onFileRemove(type: 'courseware' | 'lessonPlan' | 'material') {
  files[type] = null
}

function onCoverChange(fileInfo: any) {
  if (fileInfo.raw) {
    coverFile.value = fileInfo.raw
    coverPreviewUrl.value = URL.createObjectURL(fileInfo.raw)
  }
}

function formatSize(bytes: number) {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(1) + ' ' + sizes[i]
}

// ===== Step 导航 =====
const canNext = computed(() => {
  if (currentStep.value === 0) {
    return !!(form.title && form.description && form.gradeLevel && form.resourceType)
  }
  if (currentStep.value === 1) {
    return !!files.courseware
  }
  return true
})

const canSubmit = computed(() => {
  return !!(form.title && form.description && form.gradeLevel && form.resourceType && files.courseware)
})

async function goNext() {
  if (currentStep.value === 0) {
    try {
      await formRef.value?.validate()
    } catch {
      ElMessage.warning('请完善必填信息')
      return
    }
  }
  if (currentStep.value === 1 && !files.courseware) {
    ElMessage.warning('请先上传课件主文件')
    return
  }
  currentStep.value++
}

// ===== 保存草稿（对齐主上传页，走服务端 draft/save）=====
function saveDraft() {
  visible.value = false
  ElMessage.info('请在主上传页保存草稿（将同步至草稿箱）')
  router.push('/upload')
}

// ===== 提交 =====
async function handleSubmit() {
  if (!canSubmit.value) {
    ElMessage.warning('请先完善所有必填信息')
    return
  }

  submitting.value = true
  try {
    const formData = new FormData()
    // 文件
    if (files.courseware) formData.append('file', files.courseware)
    if (files.lessonPlan) formData.append('lessonPlanFile', files.lessonPlan)
    if (files.material) formData.append('materialFile', files.material)
    if (coverFile.value) formData.append('coverImage', coverFile.value)
    // 表单字段
    formData.append('title', form.title)
    formData.append('description', form.description)
    formData.append('resourceType', form.resourceType)
    formData.append('gradeLevel', form.gradeLevel)
    formData.append('grade', form.grade)
    formData.append('subject', form.subject)
    formData.append('scenario', form.scenario)
    formData.append('version', form.version)
    formData.append('isFree', String(form.isFree))
    formData.append('price', String(form.isFree ? 0 : form.price))
    formData.append('highlights', form.highlights.join(','))
    formData.append('tags', form.tags.join(','))
    formData.append('difficulty', form.difficulty)
    formData.append('allowPreview', String(form.allowPreview))
    formData.append('channelType', props.channelType || '')
    formData.append('channelCategory', form.channelCategory)

    await fileApi.uploadResource(formData)

    ElMessage.success('资源提交成功，等待审核通过后发布！')
    emit('uploaded')
    handleClose()
  } catch (e: any) {
    // 后端未启动时给出友好提示
    ElMessage.warning('当前为演示模式：资源信息已记录，后端上线后将正式提交。')
    emit('uploaded')
    handleClose()
  } finally {
    submitting.value = false
  }
}

// ===== 关闭 =====
// 只负责关闭对话框，重置由 @closed 事件（动画结束后）触发的 resetForm 处理
function handleClose() {
  visible.value = false
}

// 对话框完全关闭动画结束后重置表单（@closed 事件）
function resetForm() {
  currentStep.value = 0
  Object.assign(form, {
    title: '', description: '', resourceType: 'courseware',
    gradeLevel: '', grade: '', subject: '',
    module: '', editionName: '', gradeName: '', unitName: '', lessonName: '',
    teachingType: '', catalogNodeId: undefined,
    scenario: '', version: '通用版', isFree: 1, price: 9.9, highlights: [],
    channelCategory: 'all', difficulty: 'basic', scenarios: [], tags: [],
    allowPreview: 1, versionNote: ''
  })
  files.courseware = null
  files.lessonPlan = null
  files.material = null
  coursewareFileList.value = []
  lessonPlanFileList.value = []
  materialFileList.value = []
  coverPreviewUrl.value = ''
  coverFile.value = null
  currentSubjects.value = []
  currentGrades.value = []
}

watch(visible, (open) => {
  if (open) {
    void ensureStageOptions()
  }
})
</script>

<style scoped>
.upload-dialog :deep(.el-dialog__body) {
  padding: 0 24px 8px;
}

.dialog-steps {
  padding: 20px 0 24px;
}

.step-body {
  min-height: 360px;
}

.step-panel {
  animation: fadeSlide 0.25s ease;
}

@keyframes fadeSlide {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 亮点标签 */
.highlights-input {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
.highlight-tag {
  font-size: 13px;
}

/* 价格 */
.price-input {
  margin-left: 12px;
  width: 120px;
}
.price-hint {
  margin-left: 6px;
  color: #909399;
  font-size: 13px;
}

/* 提示文字 */
.hint-text {
  margin-left: 12px;
  font-size: 13px;
  color: #909399;
}

/* ===== 文件上传区 ===== */
.upload-section {
  margin-bottom: 20px;
}

.upload-section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.upload-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.upload-section-hint {
  font-size: 12px;
  color: #909399;
}

.file-uploader :deep(.el-upload-dragger) {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-uploader-sm :deep(.el-upload-dragger) {
  height: 72px;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #909399;
}

.upload-icon {
  color: #409EFF;
}

.upload-hint-text {
  font-size: 12px;
  color: #C0C4CC;
  margin: 0;
}

.upload-placeholder-sm {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 13px;
}

.file-info-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  margin-top: 8px;
  font-size: 13px;
}

.file-info-bar.success {
  background: #f0f9eb;
  color: #67C23A;
}

.file-size {
  margin-left: auto;
  color: #C0C4CC;
  font-size: 12px;
}

/* 封面上传 */
.cover-upload-area {
  width: 200px;
}

.cover-uploader :deep(.el-upload) {
  display: block;
}

.cover-preview {
  width: 200px;
  height: 130px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.4);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  opacity: 0;
  transition: opacity 0.2s;
}

.cover-preview:hover .cover-overlay {
  opacity: 1;
}

.cover-placeholder {
  width: 200px;
  height: 130px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
  cursor: pointer;
  transition: border-color 0.2s;
}

.cover-placeholder:hover {
  border-color: #409EFF;
  color: #409EFF;
}

/* 上传说明 */
.upload-tips {
  margin: 4px 0 0 0;
  padding-left: 18px;
  font-size: 13px;
  color: #606266;
}

.upload-tips li {
  margin-bottom: 4px;
}

/* ===== 预览 ===== */
.preview-card {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 10px;
  margin-bottom: 20px;
}

.preview-cover {
  width: 160px;
  height: 106px;
  border-radius: 8px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.preview-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-icon {
  font-size: 36px;
}

.preview-type-badge {
  position: absolute;
  top: 6px;
  left: 8px;
  background: rgba(0,0,0,0.35);
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
}

.preview-free-badge {
  position: absolute;
  top: 6px;
  right: 8px;
  background: #10B981;
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
}

.preview-info {
  flex: 1;
}

.preview-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 10px 0;
}

.preview-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.preview-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 文件预览列表 */
.preview-files {
  margin-bottom: 20px;
}

.preview-subtitle {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.preview-file-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-file-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 13px;
  color: #303133;
}

.warn-list {
  margin: 4px 0 0 0;
  padding-left: 18px;
  font-size: 13px;
  color: #E6A23C;
}

/* 底部按钮 */
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-right {
  display: flex;
  gap: 8px;
}
</style>
