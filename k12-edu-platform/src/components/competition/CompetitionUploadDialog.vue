<template>
  <el-dialog
    v-model="visible"
    title="上传竞赛专区资源"
    width="580px"
    destroy-on-close
    @closed="resetForm"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="upload-form">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="如：全国初中数学联赛真题汇编" />
      </el-form-item>

      <el-form-item label="简介" prop="summary">
        <el-input v-model="form.summary" type="textarea" :rows="2" maxlength="500" show-word-limit placeholder="简要说明适用场景与内容亮点" />
      </el-form-item>

      <el-form-item label="竞赛类型" prop="category">
        <el-select v-model="form.category" placeholder="选择类型">
          <el-option
            v-for="c in categoryOptions"
            :key="c.key"
            :label="`${c.icon} ${c.name}`"
            :value="c.key"
          />
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

      <el-form-item label="赛事/考级">
        <el-input v-model="form.competitionName" placeholder="如：华杯赛、NOIP、KET" />
      </el-form-item>

      <el-form-item label="标签">
        <el-input v-model="form.tags" placeholder="多个标签用逗号分隔，默认含学科竞赛" />
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
            <div class="upload-tip">支持 PDF、PPT、Word、MP4、ZIP，单文件不超过 500MB</div>
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
import { competitionApi } from '@/api/competition'
import {
  COMPETITION_SUBJECTS,
  COMPETITION_GRADE_STAGES,
  COMPETITION_FORMATS,
} from '@/constants/competitionZone'

export interface FilterOption {
  key: string
  name: string
  icon?: string
}

const props = defineProps<{
  modelValue: boolean
  categories: FilterOption[]
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
const gradeOptions = computed(() =>
  props.gradeStages.length
    ? props.gradeStages.filter((g) => g.key !== 'all')
    : COMPETITION_GRADE_STAGES.filter((g) => g.key !== 'all')
)
const subjectOptions = computed(() => COMPETITION_SUBJECTS.filter((s) => s.key !== 'all'))
const formOptions = computed(() =>
  props.resourceForms.length
    ? props.resourceForms.filter((f) => f.key !== 'all')
    : COMPETITION_FORMATS.filter((f) => f.key !== 'all').map((f) => ({ key: f.key, name: f.name }))
)

const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const submitting = ref(false)
const selectedFile = ref<File | null>(null)

const form = reactive({
  title: '',
  summary: '',
  category: '',
  gradeStage: 'all',
  subject: '',
  resourceForm: '',
  competitionName: '',
  tags: '学科竞赛',
  isFree: 1,
})

const rules: FormRules = {
  title: [{ required: true, message: '请填写标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择竞赛类型', trigger: 'change' }],
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
  form.gradeStage = 'all'
  form.subject = ''
  form.resourceForm = ''
  form.competitionName = ''
  form.tags = '学科竞赛'
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

  submitting.value = true
  try {
    const fd = new FormData()
    fd.append('file', selectedFile.value)
    fd.append('title', form.title)
    fd.append('summary', form.summary)
    fd.append('category', form.category)
    fd.append('gradeStage', form.gradeStage)
    if (form.subject) fd.append('subject', form.subject)
    fd.append('resourceForm', form.resourceForm)
    if (form.competitionName) fd.append('competitionName', form.competitionName)
    fd.append('tags', form.tags)
    fd.append('isFree', String(form.isFree))
    await competitionApi.uploadResource(fd)
    ElMessage.success('上传成功，资源已发布到竞赛专区')
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
