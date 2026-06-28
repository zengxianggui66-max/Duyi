<template>
  <router-view v-if="layout === 'admin'" />
  <AuthLayout v-else-if="layout === 'auth'" />
  <MainLayout v-else />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from '@/components/layout/MainLayout.vue'
import AuthLayout from '@/components/layout/AuthLayout.vue'

const route = useRoute()

const layout = computed(() => {
  if (route.path.startsWith('/admin')) return 'admin'
  if (route.meta.layout === 'auth') return 'auth'
  return 'main'
})
</script>
