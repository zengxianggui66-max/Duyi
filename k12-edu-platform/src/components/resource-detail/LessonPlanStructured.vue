<template>
  <el-card class="lesson-plan-structured">
    <template #header>
      <div
        class="card-header"
        :class="{ clickable: collapsible }"
        @click="collapsible ? $emit('toggle') : undefined"
      >
        <span class="header-title">📝 结构化教案</span>
        <el-tag v-if="!data.hasStructured" size="small" type="info">智能摘要</el-tag>
        <el-tag v-else size="small" type="success">已解析</el-tag>
        <el-button
          v-if="collapsible"
          text
          size="small"
          class="collapse-btn"
          @click.stop="$emit('toggle')"
        >
          {{ expanded ? '收起' : '展开' }}
          <el-icon :class="{ 'rotate-icon': expanded }"><ArrowDown /></el-icon>
        </el-button>
      </div>
    </template>

    <el-collapse-transition>
      <div v-show="!collapsible || expanded">
        <p v-if="!data.hasStructured" class="structured-hint">
          平台暂未收录完整结构化字段，以下内容根据资源信息智能生成；完整内容请查看上方资源预览或下载原文件。
        </p>

        <div class="plan-sections">
          <section
            v-for="section in data.sections"
            :key="section.key"
            class="plan-section"
          >
            <h3 class="section-heading">
              <span class="section-icon">{{ section.icon }}</span>
              {{ section.title }}
            </h3>
            <ul class="section-list">
              <li v-for="(line, idx) in section.items" :key="idx">{{ line }}</li>
            </ul>
          </section>
        </div>
      </div>
    </el-collapse-transition>
  </el-card>
</template>

<script setup lang="ts">
import { ArrowDown } from '@element-plus/icons-vue'
import type { LessonPlanStructuredData } from '@/utils/lessonPlanContent'

defineProps<{
  data: LessonPlanStructuredData
  collapsible?: boolean
  expanded?: boolean
}>()

defineEmits<{ toggle: [] }>()
</script>

<style scoped>
.lesson-plan-structured {
  border-radius: 12px;
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}
.card-header.clickable {
  cursor: pointer;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
  flex: 1;
}
.collapse-btn {
  margin-left: auto;
}
.rotate-icon {
  transform: rotate(180deg);
}
.structured-hint {
  margin: 0 0 16px;
  padding: 10px 12px;
  background: #f4f8ff;
  border-radius: 8px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
.plan-sections {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.plan-section {
  padding-bottom: 4px;
  border-bottom: 1px dashed #eef2f6;
}
.plan-section:last-child {
  border-bottom: none;
}
.section-heading {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 10px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.section-icon {
  font-size: 18px;
}
.section-list {
  margin: 0;
  padding-left: 22px;
  color: #606266;
  line-height: 1.85;
  font-size: 14px;
}
</style>
