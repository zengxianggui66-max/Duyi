<template>
  <el-dialog
    :model-value="modelValue"
    title="安全验证"
    width="400px"
    align-center
    class="captcha-dialog"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
    @closed="$emit('closed')"
    @opened="$emit('opened')"
  >
    <p class="captcha-dialog__hint">{{ hint }}</p>
    <div v-if="captchaType && captchaType !== 'arithmetic'" class="captcha-dialog__type-tag">
      验证方式：{{ captchaTypeLabel }}
    </div>
    <div class="captcha-dialog__body">
      <el-input
        :model-value="code"
        placeholder="请输入计算结果"
        size="large"
        maxlength="8"
        @update:model-value="$emit('update:code', $event)"
        @keyup.enter="$emit('confirm')"
      />
      <el-button size="large" class="captcha-dialog__refresh" @click="$emit('refresh')">
        {{ question || '获取题目' }}
      </el-button>
    </div>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="loading" @click="$emit('confirm')">
        {{ confirmLabel }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    code: string
    question?: string
    loading?: boolean
    captchaType?: string
    hint?: string
    confirmLabel?: string
  }>(),
  {
    question: '',
    loading: false,
    captchaType: 'arithmetic',
    hint: '请完成图形验证后获取短信验证码',
    confirmLabel: '确认并发送',
  },
)

defineEmits<{
  'update:modelValue': [value: boolean]
  'update:code': [value: string]
  confirm: []
  refresh: []
  closed: []
  opened: []
}>()

const captchaTypeLabel = computed(() => {
  if (props.captchaType === 'slider') return '滑块验证'
  if (props.captchaType === 'arithmetic') return '算术验证'
  return props.captchaType
})
</script>

<style scoped>
.captcha-dialog__hint {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--text-secondary);
}

.captcha-dialog__type-tag {
  margin-bottom: 10px;
  font-size: 11px;
  color: var(--text-secondary);
}

.captcha-dialog__body {
  display: flex;
  gap: 12px;
}

.captcha-dialog__body .el-input {
  flex: 1;
}

.captcha-dialog__refresh {
  flex-shrink: 0;
  min-width: 100px;
}

@media (max-width: 480px) {
  :deep(.el-dialog) {
    width: calc(100vw - 32px) !important;
  }
}
</style>
