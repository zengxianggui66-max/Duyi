<template>
  <div class="auth-role-selector">
    <button
      v-for="opt in ROLE_OPTIONS"
      :key="opt.value"
      type="button"
      class="auth-role-card"
      :class="{ 'is-active': modelValue === opt.value }"
      @click="$emit('update:modelValue', opt.value)"
    >
      <span class="auth-role-card__icon">{{ opt.icon }}</span>
      <span class="auth-role-card__label">{{ opt.label }}</span>
      <span v-if="showDesc" class="auth-role-card__desc">{{ opt.desc }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ROLE_OPTIONS, type AuthRole } from '@/composables/useAuthSms'

withDefaults(
  defineProps<{
    modelValue: AuthRole
    showDesc?: boolean
  }>(),
  { showDesc: true },
)

defineEmits<{
  'update:modelValue': [value: AuthRole]
}>()
</script>

<style scoped>
.auth-role-selector {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  width: 100%;
}

.auth-role-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 8px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}

.auth-role-card:hover {
  border-color: var(--color-primary-light, #a5b4fc);
}

.auth-role-card.is-active {
  border-color: var(--color-primary, #4361EE);
  background: linear-gradient(180deg, #f5f3ff 0%, #fff 100%);
  box-shadow: 0 4px 12px rgba(67, 97, 238, 0.15);
}

.auth-role-card__icon { font-size: 24px; line-height: 1; }
.auth-role-card__label {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
}
.auth-role-card__desc {
  font-size: 10px;
  color: var(--text-secondary);
  text-align: center;
  line-height: 1.3;
}

@media (max-width: 480px) {
  .auth-role-card {
    padding: 10px 6px;
  }
  .auth-role-card__desc {
    display: none;
  }
}
</style>
