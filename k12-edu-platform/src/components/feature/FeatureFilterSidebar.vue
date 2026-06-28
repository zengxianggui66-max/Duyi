<template>
  <aside class="left-sidebar">
    <div v-if="showGradeFilter" class="filter-block">
      <h3 class="filter-block-title">学段</h3>
      <div class="filter-options">
        <button
          v-for="g in gradeFilters"
          :key="g.key"
          class="filter-option"
          :class="{ active: selectedGrade === g.key }"
          @click="$emit('update:selectedGrade', g.key)"
        >
          {{ g.name }}
        </button>
      </div>
    </div>

    <div class="filter-block">
      <h3 class="filter-block-title">等级</h3>
      <div class="filter-options">
        <button
          v-for="l in levelFilters"
          :key="l.key"
          class="filter-option"
          :class="{ active: selectedLevel === l.key }"
          @click="$emit('update:selectedLevel', l.key)"
        >
          {{ l.name }}
        </button>
      </div>
    </div>

    <div class="filter-block">
      <h3 class="filter-block-title">格式</h3>
      <div class="filter-options">
        <button
          v-for="f in formatFilters"
          :key="f.key"
          class="filter-option"
          :class="{ active: selectedFormat === f.key }"
          @click="$emit('update:selectedFormat', f.key)"
        >
          {{ f.name }}
        </button>
      </div>
    </div>

    <div v-if="showSubjectFilter" class="filter-block">
      <h3 class="filter-block-title">学科</h3>
      <div class="filter-options">
        <button
          v-for="s in subjectFilters"
          :key="s.key"
          class="filter-option"
          :class="{ active: selectedSubject === s.key }"
          @click="$emit('update:selectedSubject', s.key)"
        >
          {{ s.name }}
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
defineProps<{
  showGradeFilter?: boolean
  showSubjectFilter?: boolean
  gradeFilters: { key: string; name: string }[]
  levelFilters: { key: string; name: string }[]
  formatFilters: { key: string; name: string }[]
  subjectFilters: { key: string; name: string }[]
  selectedGrade: string
  selectedLevel: string
  selectedFormat: string
  selectedSubject: string
}>()

defineEmits<{
  'update:selectedGrade': [key: string]
  'update:selectedLevel': [key: string]
  'update:selectedFormat': [key: string]
  'update:selectedSubject': [key: string]
}>()
</script>

<style scoped>
.left-sidebar {
  width: 200px;
  flex-shrink: 0;
}

.filter-block {
  margin-bottom: 20px;
}

.filter-block-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-option {
  padding: 4px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 16px;
  background: #fff;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-option:hover {
  border-color: #409eff;
  color: #409eff;
}

.filter-option.active {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
}
</style>
