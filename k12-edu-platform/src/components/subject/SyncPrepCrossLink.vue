<template>
  <div v-if="link" class="sync-prep-link-bar">
    <router-link :to="linkRoute" class="sync-prep-link">
      {{ link.label }}
    </router-link>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { RouteLocationRaw } from 'vue-router'

const props = defineProps<{
  link: {
    module: string
    node: string
    unit: string
    label: string
  } | null
  routeParams: {
    stage: string
    subject: string
    version: string
  }
  brand?: string
  volume?: string
}>()

const linkRoute = computed((): RouteLocationRaw => {
  if (!props.link) return '/'
  const query: Record<string, string> = {
    module: props.link.module,
    node: props.link.node,
    unit: props.link.unit,
  }
  if (props.brand) query.brand = props.brand
  if (props.volume) query.volume = props.volume
  return {
    name: 'SubjectDetail',
    params: props.routeParams,
    query,
  }
})
</script>

<style scoped>
.sync-prep-link-bar {
  padding: 8px 16px;
  border-bottom: 1px solid #eef2f6;
  background: #f0f9ff;
}

.sync-prep-link {
  font-size: 13px;
  color: #409eff;
  text-decoration: none;
}

.sync-prep-link:hover {
  text-decoration: underline;
}
</style>
