<template>
  <el-dialog
    v-model="visible"
    title="上传传统文化研学资源"
    width="560px"
    destroy-on-close
    @closed="resetForm"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="upload-form">
      <el-form-item label="资源类型" prop="resourceKind">
        <el-radio-group v-model="form.resourceKind">
          <el-radio label="platform">平台文件</el-radio>
          <el-radio label="external">外链策展</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="如：杜甫草堂诗词研学手册" />
      </el-form-item>

      <el-form-item label="简介" prop="summary">
        <el-input v-model="form.summary" type="textarea" :rows="2" maxlength="500" show-word-limit placeholder="简要说明适用场景与内容亮点" />
      </el-form-item>

      <el-form-item label="主题" prop="category">
        <el-select v-model="form.category" placeholder="选择主题">
          <el-option
            v-for="c in categoryOptions"
            :key="c.key"
            :label="`${c.icon} ${c.name}`"
            :value="c.key"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="地域" prop="region">
        <el-select v-model="form.region" placeholder="选择地域">
          <el-option v-for="r in regionOptions" :key="r.key" :label="r.name" :value="r.key" />
        </el-select>
      </el-form-item>

      <el-form-item label="研学时长" prop="durationType">
        <el-select v-model="form.durationType" placeholder="选择时长">
          <el-option v-for="d in durationOptions" :key="d.key" :label="d.name" :value="d.key" />
        </el-select>
      </el-form-item>

      <el-form-item label="适用对象">
        <el-input v-model="form.suitableAudience" placeholder="默认：研学机构" />
      </el-form-item>

      <el-form-item label="地点/线路">
        <el-input v-model="form.location" placeholder="如：杜甫草堂、都江堰" />
      </el-form-item>

      <el-form-item label="标签">
        <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
      </el-form-item>

      <el-form-item v-if="form.resourceKind === 'platform'" label="上传文件" prop="file">
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

      <template v-else>
        <el-form-item label="外链地址" prop="externalUrl">
          <el-input v-model="form.externalUrl" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="来源名称">
          <el-input v-model="form.sourceName" placeholder="如：中国非遗网" />
        </el-form-item>
      </template>

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
import { cultureStudyApi } from '@/api/cultureStudy'

export interface FilterOption {
  key: string
  name: string
  icon?: string
}

const props = defineProps<{
  modelValue: boolean
  categories: FilterOption[]
  regions: FilterOption[]
  durations: FilterOption[]
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
const regionOptions = computed(() => props.regions.filter((r) => r.key !== 'all'))
const durationOptions = computed(() => props.durations.filter((d) => d.key !== 'all'))

const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const submitting = ref(false)
const selectedFile = ref<File | null>(null)

const form = reactive({
  resourceKind: 'platform' as 'platform' | 'external',
  title: '',
  summary: '',
  category: '',
  region: 'bashu',
  durationType: '',
  suitableAudience: '研学机构',
  location: '',
  tags: '',
  externalUrl: '',
  sourceName: '',
  isFree: 1,
})

const rules: FormRules = {
  title: [{ required: true, message: '请填写标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择主题', trigger: 'change' }],
  region: [{ required: true, message: '请选择地域', trigger: 'change' }],
  durationType: [{ required: true, message: '请选择研学时长', trigger: 'change' }],
  externalUrl: [
    {
      validator: (_rule, value, callback) => {
        if (form.resourceKind === 'external' && !value) {
          callback(new Error('请填写外链地址'))
        } else if (form.resourceKind === 'external' && value && !/^https?:\/\/.+/.test(value)) {
          callback(new Error('外链需以 http:// 或 https:// 开头'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  file: [
    {
      validator: (_rule, _value, callback) => {
        if (form.resourceKind === 'platform' && !selectedFile.value) {
          callback(new Error('请选择要上传的文件'))
        } else {
          callback()
        }
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
  form.resourceKind = 'platform'
  form.title = ''
  form.summary = ''
  form.category = ''
  form.region = 'bashu'
  form.durationType = ''
  form.suitableAudience = '研学机构'
  form.location = ''
  form.tags = ''
  form.externalUrl = ''
  form.sourceName = ''
  form.isFree = 1
  selectedFile.value = null
  uploadRef.value?.clearFiles()
  formRef.value?.resetFields()
}

watch(
  () => form.resourceKind,
  () => {
    selectedFile.value = null
    uploadRef.value?.clearFiles()
  }
)

async function submit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const fd = new FormData()
    if (form.resourceKind === 'platform' && selectedFile.value) {
      fd.append('file', selectedFile.value)
    }
    fd.append('title', form.title)
    fd.append('summary', form.summary)
    fd.append('category', form.category)
    fd.append('region', form.region)
    fd.append('durationType', form.durationType)
    fd.append('suitableAudience', form.suitableAudience)
    fd.append('location', form.location)
    fd.append('resourceKind', form.resourceKind)
    fd.append('tags', form.tags)
    fd.append('isFree', String(form.isFree))
    if (form.resourceKind === 'external') {
      fd.append('externalUrl', form.externalUrl)
      fd.append('sourceName', form.sourceName)
    }
    await cultureStudyApi.uploadResource(fd)
    ElMessage.success('上传成功，资源已发布到平台')
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
