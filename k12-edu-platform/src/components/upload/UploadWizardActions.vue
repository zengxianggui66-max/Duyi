<template>
  <div class="bottom-actions">
    <div class="action-left">
      <el-button v-if="currentStep > 0" @click="handlePrev">
        <el-icon><ArrowLeft /></el-icon>
        上一步
      </el-button>
    </div>
    <div class="action-right">
      <el-button @click="handleSaveDraft">保存草稿</el-button>
      <el-button @click="handleCancel">取消上传</el-button>
      <el-button
        v-if="currentStep < wizardMaxStep"
        type="primary"
        :disabled="!canNext"
        @click="handleNext"
      >
        下一步
        <el-icon><ArrowRight /></el-icon>
      </el-button>
      <el-button
        v-else
        type="success"
        :disabled="!canSubmit"
        :loading="submitting"
        @click="handleSubmit"
      >
        {{ submitButtonLabel }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { useUploadFormInject } from '@/composables/useUploadFormInject'

const {
  currentStep,
  wizardMaxStep,
  submitButtonLabel,
  canNext,
  canSubmit,
  submitting,
  handlePrev,
  handleNext,
  handleSaveDraft,
  handleCancel,
  handleSubmit,
} = useUploadFormInject()
</script>

<style scoped>
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid #ebeef5;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 100;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
}
.action-right {
  display: flex;
  gap: 12px;
}
</style>
