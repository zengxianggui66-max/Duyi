<template>
  <aside v-if="points.length" class="policy-points card">
    <h3>📋 政策要点</h3>
    <ul>
      <li v-for="(p, i) in points" :key="i">{{ p }}</li>
    </ul>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ raw?: string | null }>()

const points = computed(() => {
  if (!props.raw) return []
  try {
    const parsed = JSON.parse(props.raw)
    return Array.isArray(parsed) ? parsed.filter(Boolean) : []
  } catch {
    return props.raw.split(/[；;|\n]/).map((s) => s.trim()).filter(Boolean)
  }
})
</script>

<style scoped>
.policy-points {
  padding: 20px 24px;
  margin-bottom: 24px;
  background: linear-gradient(135deg, #eff6ff, #f0fdf4);
  border-left: 4px solid var(--color-primary, #2563eb);
}
.policy-points h3 {
  font-size: 16px;
  font-weight: 700;
  margin: 0 0 12px;
}
.policy-points ul {
  margin: 0;
  padding-left: 20px;
}
.policy-points li {
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-regular);
  margin-bottom: 6px;
}
</style>
