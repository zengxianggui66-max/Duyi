import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const src = path.join(__dirname, '../src/components/culture/CultureUploadDialog.vue')
const dst = path.join(__dirname, '../src/components/competition/CompetitionUploadDialog.vue')

let c = fs.readFileSync(src, 'utf8')

c = c.replace('上传传统文化研学资源', '上传竞赛专区资源')
c = c.replace('width="560px"', 'width="580px"')
c = c.replace(
  `      <el-form-item label="资源类型" prop="resourceKind">
        <el-radio-group v-model="form.resourceKind">
          <el-radio label="platform">平台文件</el-radio>
          <el-radio label="external">外链策展</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="如：杜甫草堂诗词研学手册" />`,
  `      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="如：全国初中数学联赛真题汇编" />`
)
c = c.replace('label="主题"', 'label="竞赛类型"')
c = c.replace('placeholder="选择主题"', 'placeholder="选择类型"')
c = c.replace(
  `      <el-form-item label="地域" prop="region">
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

      <el-form-item v-if="form.resourceKind === 'platform'" label="上传文件" prop="file">`,
  `      <el-form-item label="学段" prop="gradeStage">
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

      <el-form-item label="上传文件" prop="file">`
)
c = c.replace(
  `      <template v-else>
        <el-form-item label="外链地址" prop="externalUrl">
          <el-input v-model="form.externalUrl" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="来源名称">
          <el-input v-model="form.sourceName" placeholder="如：中国非遗网" />
        </el-form-item>
      </template>

      <el-form-item label="免费开放">`,
  `      <el-form-item label="免费开放">`
)
c = c.replace(
  "import { cultureStudyApi } from '@/api/cultureStudy'",
  `import { competitionApi } from '@/api/competition'
import {
  COMPETITION_SUBJECTS,
  COMPETITION_GRADE_STAGES,
  COMPETITION_FORMATS,
} from '@/constants/competitionZone'`
)
c = c.replace(
  `const props = defineProps<{
  modelValue: boolean
  categories: FilterOption[]
  regions: FilterOption[]
  durations: FilterOption[]
}>()`,
  `const props = defineProps<{
  modelValue: boolean
  categories: FilterOption[]
  gradeStages: FilterOption[]
  resourceForms: FilterOption[]
}>()`
)
c = c.replace(
  `const categoryOptions = computed(() => props.categories.filter((c) => c.key !== 'all'))
const regionOptions = computed(() => props.regions.filter((r) => r.key !== 'all'))
const durationOptions = computed(() => props.durations.filter((d) => d.key !== 'all'))`,
  `const categoryOptions = computed(() => props.categories.filter((c) => c.key !== 'all'))
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
)`
)
c = c.replace(
  `const form = reactive({
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
})`,
  `const form = reactive({
  title: '',
  summary: '',
  category: '',
  gradeStage: 'all',
  subject: '',
  resourceForm: '',
  competitionName: '',
  tags: '学科竞赛',
  isFree: 1,
})`
)
c = c.replace(
  `const rules: FormRules = {
  title: [{ required: true, message: '请填写标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择主题', trigger: 'change' }],
  region: [{ required: true, message: '请选择地域', trigger: 'change' }],
  durationType: [{ required: true, message: '请选择研学时长', trigger: 'change' }],
  externalUrl: [
    {
      validator: (_rule, value, callback) => {
        if (form.resourceKind === 'external' && !value) {
          callback(new Error('请填写外链地址'))
        } else if (form.resourceKind === 'external' && value && !/^https?:\\/\\/.+/.test(value)) {
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
}`,
  `const rules: FormRules = {
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
}`
)
c = c.replace(
  `function resetForm() {
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
)`,
  `function resetForm() {
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
})`
)
c = c.replace(
  `async function submit() {
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
}`,
  `async function submit() {
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
}`
)

fs.writeFileSync(dst, c, 'utf8')
console.log('Written', dst)
