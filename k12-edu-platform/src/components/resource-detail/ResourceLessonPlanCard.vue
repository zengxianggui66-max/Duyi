<template>
  <el-card class="lesson-plan-card">
    <template #header>
      <div class="card-header" style="cursor: pointer" @click="$emit('toggle')">
        <span class="header-title">📝 教案文本预览</span>
        <el-icon :class="{ 'rotate-icon': expanded }"><ArrowRight /></el-icon>
      </div>
    </template>
    <div v-show="expanded" class="lesson-plan-content">
      <div class="plan-section">
        <h4>教学目标</h4>
        <ul>
          <li><strong>知识目标：</strong>理解感恩的含义，了解感恩的不同表达方式</li>
          <li><strong>情感目标：</strong>培养学生感恩意识，体会父母、老师的付出</li>
          <li><strong>行为目标：</strong>学会用行动表达感恩，养成感恩习惯</li>
        </ul>
      </div>
      <div class="plan-section">
        <h4>教学重难点</h4>
        <ul>
          <li><strong>重点：</strong>引导学生理解感恩的内涵，学会表达感恩</li>
          <li><strong>难点：</strong>将感恩意识转化为实际行动</li>
        </ul>
      </div>
      <div class="plan-section">
        <h4>教学准备</h4>
        <ul>
          <li>教具：多媒体课件、感恩视频、背景音乐</li>
          <li>素材：感恩卡片、情景案例</li>
          <li>时间：1课时（35分钟）</li>
        </ul>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ArrowRight } from '@element-plus/icons-vue'

defineProps<{ expanded: boolean }>()
defineEmits<{ toggle: [] }>()
</script>

<style scoped>
.lesson-plan-card {
  border-radius: 12px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
}
.rotate-icon {
  transform: rotate(90deg);
}
.lesson-plan-content {
  padding: 0 4px;
}
.plan-section {
  margin-bottom: 16px;
}
.plan-section h4 {
  margin: 0 0 8px;
  font-size: 15px;
}
.plan-section ul {
  margin: 0;
  padding-left: 20px;
  color: #606266;
  line-height: 1.8;
}
</style>
