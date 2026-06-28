<template>
  <section class="grade-nav-bar">
    <div class="container grade-nav-inner">
      <span
        v-for="g in gradeKeys"
        :key="g"
        class="grade-nav-tab"
        :class="{ active: gradeLevel === g }"
        @click="$emit('switch-grade', g)"
      >
        {{ gradeEmojis[g] }} {{ gradeNames[g] }}
      </span>
      <span class="nav-divider" />
      <span
        class="grade-nav-tab art-tab"
        :class="{ active: gradeLevel === 'art' }"
        @click="$emit('switch-grade', 'art')"
      >
        🎨 美术
      </span>
      <span
        class="grade-nav-tab dance-tab"
        :class="{ active: gradeLevel === 'dance' }"
        @click="$emit('switch-grade', 'dance')"
      >
        💃 舞蹈
      </span>
      <div class="nav-search-box">
        <el-input
          :model-value="searchKeyword"
          :placeholder="searchPlaceholder"
          clearable
          style="width: 200px;"
          @update:model-value="$emit('update:searchKeyword', $event)"
          @keyup.enter="$emit('search')"
        />
        <el-button type="primary" :icon="Search" @click="$emit('search')">搜索</el-button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { Search } from '@element-plus/icons-vue'

defineProps<{
  gradeKeys: string[]
  gradeNames: Record<string, string>
  gradeEmojis: Record<string, string>
  gradeLevel: string
  searchKeyword: string
  searchPlaceholder: string
}>()

defineEmits<{
  'switch-grade': [key: string]
  'update:searchKeyword': [value: string]
  search: []
}>()
</script>

<style scoped>
.grade-nav-bar {
  background: #fff;
  border-bottom: 1px solid #eee;
  padding: 10px 0;
}
.grade-nav-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.grade-nav-tab {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  color: #666;
  transition: all 0.2s;
}
.grade-nav-tab:hover,
.grade-nav-tab.active {
  background: #409eff;
  color: #fff;
}
.nav-divider {
  width: 1px;
  height: 20px;
  background: #e0e0e0;
  margin: 0 4px;
}
.nav-search-box {
  margin-left: auto;
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
