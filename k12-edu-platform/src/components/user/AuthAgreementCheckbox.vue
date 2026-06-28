<template>
  <el-checkbox :model-value="modelValue" class="auth-agreement" @update:model-value="$emit('update:modelValue', $event === true)">
    我已阅读并同意
    <router-link :to="AUTH_TERMS_PATH" class="auth-agreement__link" @click.stop>《用户协议》</router-link>
    和
    <router-link :to="AUTH_PRIVACY_PATH" class="auth-agreement__link" @click.stop>《隐私政策》</router-link>
  </el-checkbox>
</template>

<script setup lang="ts">
import { AUTH_PRIVACY_PATH, AUTH_TERMS_PATH } from '@/constants/authLegal'

defineProps<{
  modelValue: boolean
}>()

defineEmits<{
  'update:modelValue': [value: boolean]
}>()
</script>

<style scoped>
.auth-agreement {
  height: auto;
  align-items: flex-start;
  white-space: normal;
  line-height: 1.55;
}

.auth-agreement :deep(.el-checkbox__input) {
  margin-top: 2px;
}

.auth-agreement :deep(.el-checkbox__label) {
  font-size: 12px;
  color: #8E8EA0;
  line-height: 1.55;
  padding-left: 8px;
}

.auth-agreement__link {
  color: var(--color-primary, #4361EE);
  text-decoration: none;
  font-weight: 500;
}

.auth-agreement__link:hover {
  text-decoration: underline;
}
</style>

