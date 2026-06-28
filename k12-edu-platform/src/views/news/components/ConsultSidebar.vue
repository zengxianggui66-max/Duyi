<template>
  <aside class="consult-sidebar card">
    <h3>💬 升学/课程咨询</h3>
    <p class="consult-desc">留下联系方式，专属顾问将为您解读政策、规划备考路径</p>
    <el-form :model="form" label-position="top" @submit.prevent="submit">
      <el-form-item label="姓名" required>
        <el-input v-model="form.name" placeholder="您的姓名" maxlength="20" />
      </el-form-item>
      <el-form-item label="手机号" required>
        <el-input v-model="form.phone" placeholder="11位手机号" maxlength="11" />
      </el-form-item>
      <el-form-item label="年级/学段">
        <el-select v-model="form.grade" placeholder="请选择" style="width: 100%">
          <el-option label="小学" value="小学" />
          <el-option label="初一" value="初一" />
          <el-option label="初二" value="初二" />
          <el-option label="初三" value="初三" />
          <el-option label="高一" value="高一" />
          <el-option label="高二" value="高二" />
          <el-option label="高三" value="高三" />
        </el-select>
      </el-form-item>
      <el-form-item label="咨询意向">
        <el-radio-group v-model="form.intentType">
          <el-radio value="trial">试听体验</el-radio>
          <el-radio value="material">资料领取</el-radio>
          <el-radio value="promotion">优惠活动</el-radio>
          <el-radio value="general">一般咨询</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选" />
      </el-form-item>
      <el-button type="primary" native-type="submit" :loading="submitting" style="width: 100%">
        提交咨询
      </el-button>
    </el-form>
  </aside>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { newsApi } from '@/api'
import type { ConsultLeadPayload } from '@/api/types'

const props = defineProps<{
  articleId?: number
}>()

const submitting = ref(false)
const form = reactive<ConsultLeadPayload>({
  name: '',
  phone: '',
  grade: '',
  intentType: 'general',
  remark: '',
  sourcePage: 'news-detail',
})

async function submit() {
  if (!form.name?.trim()) {
    ElMessage.warning('请输入姓名')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.phone || '')) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  submitting.value = true
  try {
    await newsApi.submitConsult({
      ...form,
      articleId: props.articleId,
      sourcePage: props.articleId ? `news/${props.articleId}` : 'news',
    })
    ElMessage.success('提交成功，顾问将尽快联系您')
    form.name = ''
    form.phone = ''
    form.remark = ''
  } catch {
    /* 全局错误已提示 */
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.consult-sidebar {
  padding: 20px;
  position: sticky;
  top: 88px;
}
.consult-sidebar h3 {
  font-size: 17px;
  font-weight: 700;
  margin: 0 0 8px;
}
.consult-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0 0 16px;
}
:deep(.el-radio-group) {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}
</style>
