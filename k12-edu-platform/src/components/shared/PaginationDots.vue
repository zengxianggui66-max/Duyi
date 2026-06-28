<template>
  <div class="pagination-dots" role="tablist">
    <button
      v-for="(_, index) in count"
      :key="index"
      type="button"
      class="dot-btn"
      :class="{ active: modelValue === index }"
      :aria-label="`\u7b2c ${index + 1} \u9875`"
      @click="$emit('update:modelValue', index)"
    />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  count: number
  modelValue: number
}>()

defineEmits<{
  'update:modelValue': [index: number]
}>()
</script>

<style scoped>
.pagination-dots {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 24px;
}

.dot-btn {
  width: 8px;
  height: 8px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: #e8e8e8;
  cursor: pointer;
  transition: all 0.2s;
}

.dot-btn.active {
  width: 24px;
  border-radius: 4px;
  background: linear-gradient(90deg, #4facfe, #00f2fe);
}
</style>
