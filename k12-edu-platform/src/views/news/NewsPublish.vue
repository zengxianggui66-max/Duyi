<template>
  <div class="news-publish-page">
    <motion.div
      class="container"
      :initial="{ opacity: 0, y: 12 }"
      :animate="{ opacity: 1, y: 0 }"
    >
      <nav class="breadcrumb">
        <router-link to="/news">教育资讯</router-link>
        <span>/</span>
        <span>发布资讯</span>
      </nav>

      <header class="page-header">
        <h1>✍️ 发布教育资讯</h1>
        <p>分享政策解读、教研动态、名师经验或升学备考内容</p>
      </header>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="publish-form card"
        @submit.prevent="submit"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入资讯标题" maxlength="120" show-word-limit />
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="选择频道" style="width: 100%">
            <el-option
              v-for="ch in NEWS_CHANNELS"
              :key="ch.key"
              :label="`${ch.icon} ${ch.name}`"
              :value="ch.key"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="摘要" prop="summary">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="2"
            placeholder="列表页展示的简短摘要"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="正文" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="12"
            placeholder="支持 HTML，如 &lt;p&gt;段落&lt;/p&gt;、&lt;h2&gt;小标题&lt;/h2&gt;"
          />
        </el-form-item>

        <el-form-item label="封面图">
          <motion.div class="cover-upload">
            <el-upload
              :show-file-list="false"
              :http-request="uploadCover"
              accept="image/*"
            >
              <el-button type="primary" plain :loading="coverUploading">上传封面</el-button>
            </el-upload>
            <img v-if="form.coverUrl" :src="form.coverUrl" alt="封面预览" class="cover-preview" />
          </motion.div>
        </el-form-item>

        <el-form-item label="作者">
          <el-input v-model="form.author" placeholder="默认使用当前用户昵称" />
        </el-form-item>

        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔，如：成都中考,政策" />
        </el-form-item>

        <el-form-item label="学段">
          <el-select v-model="form.gradeLevels" style="width: 100%">
            <el-option
              v-for="g in GRADE_FILTER_OPTIONS"
              :key="g.key"
              :label="g.label"
              :value="g.key"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="地区">
          <el-select v-model="form.regions" style="width: 100%">
            <el-option
              v-for="r in REGION_FILTER_OPTIONS"
              :key="r.key"
              :label="r.label"
              :value="r.key"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="内容类型">
          <el-radio-group v-model="form.contentType">
            <el-radio value="article">图文</el-radio>
            <el-radio value="video">视频</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="展示咨询">
          <el-switch v-model="consultOn" />
        </el-form-item>

        <el-form-item label="发布状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">立即发布</el-radio>
            <el-radio :value="0">存为草稿</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="submitting" @click="submit">
            提交发布
          </el-button>
          <el-button size="large" @click="router.push('/news')">取消</el-button>
        </el-form-item>
      </el-form>
    </motion.div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { motion } from 'motion-v'
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus'
import { ElMessage } from 'element-plus'
import { newsApi, fileApi } from '@/api'
import { useUserStore } from '@/store'
import { NEWS_CHANNELS, GRADE_FILTER_OPTIONS, REGION_FILTER_OPTIONS } from '@/constants/newsZone'
import type { ArticleCreatePayload } from '@/api/types'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const coverUploading = ref(false)
const consultOn = ref(true)

const form = reactive<ArticleCreatePayload>({
  title: '',
  summary: '',
  content: '',
  coverUrl: '',
  category: 'policy',
  author: '',
  tags: '',
  gradeLevels: 'all',
  regions: 'all',
  contentType: 'article',
  status: 1,
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  summary: [{ required: true, message: '请输入摘要', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文', trigger: 'blur' }],
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.replace({ path: '/login', query: { redirect: '/news/publish' } })
    return
  }
  form.author = userStore.nickname
})

async function uploadCover(options: UploadRequestOptions) {
  coverUploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', options.file)
    fd.append('title', form.title || '资讯封面')
    const res = await fileApi.upload(fd)
    form.coverUrl = res.data.data?.fileUrl || ''
    ElMessage.success('封面上传成功')
  } catch {
    ElMessage.error('封面上传失败')
  } finally {
    coverUploading.value = false
  }
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload: ArticleCreatePayload = {
      ...form,
      consultEnabled: consultOn.value ? 1 : 0,
      author: form.author || userStore.nickname,
    }
    const res = await newsApi.createArticle(payload)
    const id = res.data.data
    ElMessage.success(form.status === 1 ? '发布成功' : '草稿已保存')
    if (form.status === 1 && id) {
      router.push(`/news/${id}`)
    } else {
      router.push('/news')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.news-publish-page {
  min-height: 100%;
  background: var(--bg-page, #f5f7fa);
  padding: 24px 0 48px;
}
.breadcrumb {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 16px;
}
.breadcrumb a {
  color: var(--color-primary);
  text-decoration: none;
}
.breadcrumb span {
  margin: 0 8px;
}
.page-header {
  margin-bottom: 24px;
}
.page-header h1 {
  font-size: 26px;
  font-weight: 700;
  margin: 0 0 8px;
}
.page-header p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
}
.publish-form {
  padding: 32px 40px;
  max-width: 800px;
}
.cover-upload {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;
}
.cover-preview {
  max-width: 320px;
  max-height: 180px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid var(--border-light);
}
</style>
