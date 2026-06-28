<template>
  <el-dialog
    v-model="visible"
    title="上传专题资源"
    width="600px"
    destroy-on-close
    @closed="resetForm"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="upload-form">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="如：成都七年级期末冲刺模拟卷" />
      </el-form-item>

      <el-form-item label="简介" prop="summary">
        <el-input v-model="form.summary" type="textarea" :rows="2" maxlength="500" show-word-limit placeholder="适用学段、专题场景与内容说明" />
      </el-form-item>

      <el-form-item label="专题场景" prop="category">
        <el-select v-model="form.category" placeholder="选择场景">
          <el-option
            v-for="c in categoryOptions"
            :key="c.key"
            :label="`${c.icon || ''} ${c.name}`"
            :value="c.key"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="地域" prop="region">
        <el-select v-model="form.region" placeholder="选择地域">
          <el-option v-for="r in regionOptions" :key="r.key" :label="r.name" :value="r.key" />
        </el-select>
      </el-form-item>

      <el-form-item label="学段" prop="gradeStage">
        <el-select v-model="form.gradeStage" placeholder="选择学段">
          <el-option v-for="g in gradeOptions" :key="g.key" :label="g.name" :value="g.key" />
        </el-select>
      </el-form-item>

      <el-form-item label="学科">
        <el-select v-model="form.subject" clearable placeholder="可选">
          <el-option v-for="s in subjectOptions" :key="s.key" :label="s.name" :value="s.key" />
        </el-select>
      </el-form-item>

      <el-form-item label="资源形态" prop="resourceForm">
        <el-select v-model="form.resourceForm" placeholder="选择形态">
          <el-option v-for="f in formOptions" :key="f.key" :label="f.name" :value="f.key" />
        </el-select>
      </el-form-item>

      <el-form-item label="专题说明">
        <el-input v-model="form.topicLabel" placeholder="如：期末冲刺、小升初衔接" />
      </el-form-item>

      <el-form-item label="学年">
        <el-input v-model="form.schoolYear" placeholder="如：2025-2026" />
      </el-form-item>

      <el-form-item label="标签">
        <el-input v-model="form.tags" placeholder="多个标签用逗号分隔，默认含专题资源" />
      </el-form-item>

      <el-form-item label="上传文件" prop="file">
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          :on-change="onFileChange"
          :on-remove="onFileRemove"
          accept=".pdf,.ppt,.pptx,.doc,.docx,.mp4,.zip"
        >
          <el-button type="primary" plain>选择文件</el-button>
          <template #tip>
            <motion.div class="upload-tip" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }">支持 PDF、PPT、Word、MP4、ZIP</motion.div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form-item label="免费开放">
        <el-switch v-model="form.isFree" :active-value="1" :inactive-value="0" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">提交发布</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules, type UploadFile, type UploadInstance } from 'element-plus'
import { motion } from 'motion-v'
import { topicApi } from '@/api/topic'
import {
  TOPIC_SUBJECTS,
  TOPIC_GRADE_STAGES,
  TOPIC_REGIONS,
  TOPIC_FORMAT_API_MAP,
  resolveSchoolYear,
} from '@/constants/topicZone'

export interface FilterOption {
  key: string
  name: string
  icon?: string
}

const props = defineProps<{
  modelValue: boolean
  categories: FilterOption[]
  regions: FilterOption[]
  gradeStages: FilterOption[]
  resourceForms: FilterOption[]
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
  success: []
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const categoryOptions = computed(() => props.categories.filter((c) => c.key !== 'all'))
const regionOptions = computed(() =>
  props.regions.length
    ? props.regions.filter((r) => r.key !== 'all')
    : TOPIC_REGIONS.filter((r) => r.key !== 'all')
)
const gradeOptions = computed(() =>
  props.gradeStages.length
    ? props.gradeStages.filter((g) => g.key !== 'all')
    : TOPIC_GRADE_STAGES.filter((g) => g.key !== 'all')
)
const subjectOptions = computed(() => TOPIC_SUBJECTS.filter((s) => s.key !== 'all'))
const formOptions = computed(() => {
  const list = props.resourceForms.length
    ? props.resourceForms
    : [
        { key: 'exam', name: '试卷/真题' },
        { key: 'material', name: '讲义/作业' },
        { key: 'lesson_plan', name: '教案' },
        { key: 'ppt', name: '课件PPT' },
        { key: 'video', name: '视频' },
        { key: 'doc', name: 'PDF文档' },
      ]
  return list.filter((f) => f.key !== 'all')
})

const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const submitting = ref(false)
const selectedFile = ref<File | null>(null)

const form = reactive({
  title: '',
  summary: '',
  category: '',
  region: 'all',
  gradeStage: 'all',
  subject: '',
  resourceForm: '',
  topicLabel: '',
  schoolYear: resolveSchoolYear(),
  tags: '专题资源',
  isFree: 1,
})

const rules: FormRules = {
  title: [{ required: true, message: '请填写标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择专题场景', trigger: 'change' }],
  region: [{ required: true, message: '请选择地域', trigger: 'change' }],
  gradeStage: [{ required: true, message: '请选择学段', trigger: 'change' }],
  resourceForm: [{ required: true, message: '请选择资源形态', trigger: 'change' }],
  file: [
    {
      validator: (_rule, _value, callback) => {
        if (!selectedFile.value) callback(new Error('请选择要上传的文件'))
        else callback()
      },
      trigger: 'change',
    },
  ],
}

function onFileChange(file: UploadFile) {
  selectedFile.value = file.raw || null
  formRef.value?.validateField('file')
}

function onFileRemove() {
  selectedFile.value = null
}

function resetForm() {
  form.title = ''
  form.summary = ''
  form.category = ''
  form.region = 'all'
  form.gradeStage = 'all'
  form.subject = ''
  form.resourceForm = ''
  form.topicLabel = ''
  form.schoolYear = resolveSchoolYear()
  form.tags = '专题资源'
  form.isFree = 1
  selectedFile.value = null
  uploadRef.value?.clearFiles()
  formRef.value?.resetFields()
}

watch(visible, (v) => {
  if (!v) resetForm()
})

async function submit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || !selectedFile.value) return

  const apiForm = TOPIC_FORMAT_API_MAP[form.resourceForm] || form.resourceForm

  submitting.value = true
  try {
    const fd = new FormData()
    fd.append('file', selectedFile.value)
    fd.append('title', form.title)
    fd.append('summary', form.summary)
    fd.append('category', form.category)
    fd.append('region', form.region)
    fd.append('gradeStage', form.gradeStage)
    if (form.subject) fd.append('subject', form.subject)
    fd.append('resourceForm', apiForm)
    if (form.topicLabel) fd.append('topicLabel', form.topicLabel)
    if (form.schoolYear) fd.append('schoolYear', form.schoolYear)
    fd.append('tags', form.tags)
    fd.append('isFree', String(form.isFree))
    await topicApi.uploadResource(fd)
    ElMessage.success('上传成功，资源已发布到专题专区')
    visible.value = false
    emit('success')
  } catch (e: unknown) {
    const err = e as { message?: string }
    if (err.message && !err.message.includes('登录')) {
      ElMessage.error(err.message || '上传失败')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.upload-form {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
}
.upload-tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}
</style>
