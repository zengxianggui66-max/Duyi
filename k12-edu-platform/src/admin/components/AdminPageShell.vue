<template>
  <div class="admin-page-shell">
    <div class="admin-page-shell__head">
      <h1 class="admin-page-shell__title">{{ title }}</h1>
      <p v-if="desc" class="admin-page-shell__desc">{{ desc }}</p>
    </div>
    <div class="admin-page-shell__body">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  desc?: string
}>()
</script>

<style scoped>
.admin-page-shell__head {
  margin-bottom: 20px;
}

.admin-page-shell__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
}

.admin-page-shell__desc {
  margin: 8px 0 0;
  font-size: 14px;
  color: #8e8ea0;
}

.admin-page-shell__body {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  min-height: 360px;
  box-shadow: 0 1px 4px rgba(26, 26, 46, 0.06);
}
</style>
