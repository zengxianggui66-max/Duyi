<template>
  <div class="exam-page">
    <section class="exam-banner">
      <div class="container">
        <router-link to="/lesson" class="breadcrumb-link">备课中心</router-link>
        <span class="breadcrumb-sep">/</span>
        <span>试题试卷</span>
        <h1>📄 试题试卷</h1>
        <p>海量试题试卷，覆盖同步练习、单元测试、期中期末、模拟考试、升学真题</p>
      </div>
    </section>

    <div class="container" style="padding: 24px;">
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <div class="filter-row">
          <span class="filter-label">学段：</span>
          <el-radio-group v-model="filters.gradeLevel" @change="loadExams">
            <el-radio-button label="primary">小学</el-radio-button>
            <el-radio-button label="junior">初中</el-radio-button>
            <el-radio-button label="senior">高中</el-radio-button>
          </el-radio-group>
        </div>
        <div class="filter-row">
          <span class="filter-label">学科：</span>
          <div class="filter-tags">
            <span
              v-for="s in currentSubjects"
              :key="s.key"
              class="filter-tag"
              :class="{ active: filters.subject === s.key }"
              @click="filters.subject = filters.subject === s.key ? '' : s.key"
            >{{ s.name }}</span>
          </div>
        </div>
        <div class="filter-row">
          <span class="filter-label">类型：</span>
          <div class="filter-tags">
            <span
              v-for="e in examTypes"
              :key="e.key"
              class="filter-tag"
              :class="{ active: filters.examType === e.key }"
              @click="filters.examType = filters.examType === e.key ? '' : e.key"
            >{{ e.icon }} {{ e.name }}</span>
          </div>
        </div>
      </div>

      <!-- 试卷列表 -->
      <div class="exam-list">
        <div v-for="item in examList" :key="item.id" class="exam-item card">
          <div class="exam-badge" :style="{ background: item.badgeColor }">
            {{ item.examTypeName }}
          </div>
          <div class="exam-main">
            <h4>{{ item.title }}</h4>
            <div class="exam-meta">
              <el-tag size="small" effect="plain">{{ item.gradeName }}</el-tag>
              <el-tag size="small" effect="plain" type="success">{{ item.subjectName }}</el-tag>
              <el-tag size="small" effect="plain" type="warning">{{ item.score }}分</el-tag>
              <span>{{ item.questionCount }}题</span>
              <span>{{ item.duration }}分钟</span>
            </div>
          </div>
          <div class="exam-right">
            <span class="exam-count">⬇ {{ item.downloads }}</span>
            <el-button type="primary" round size="small">{{ item.isFree ? '免费下载' : '会员下载' }}</el-button>
          </div>
        </div>
      </div>

      <div class="pagination-wrap">
        <el-pagination background layout="prev, pager, next" :total="150" :page-size="15" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { subjects } from '@/constants'
import { examApi } from '@/api'
import { ElMessage } from 'element-plus'

// 定义筛选过滤器
const filters = ref({ gradeLevel: 'primary', subject: '', examType: '' })
const examList = ref<any[]>([])
const loading = ref(false)
const total = ref(0)

// 获取当前学段对应的学科列表
const currentSubjects = computed(() => {
  return subjects.filter(s => s.gradeLevels.includes(filters.value.gradeLevel))
})

// 考试类型配置
const examTypes = [
  { key: 'unit', name: '单元测试', icon: '📝' },
  { key: 'monthly', name: '月考', icon: '📅' },
  { key: 'midterm', name: '期中', icon: '📋' },
  { key: 'final', name: '期末', icon: '🎯' },
  { key: 'mock', name: '模拟', icon: '🏆' },
]

// 学段名称映射
const gradeLevelNames: Record<string, string> = {
  primary: '小学',
  junior: '初中',
  senior: '高中',
}

async function loadExams() {
  loading.value = true
  try {
    const res = await examApi.getHistory()
    const data = res.data.data || []
    examList.value = data.map((item: any) => ({
      id: item.id,
      title: item.title || `${gradeLevelNames[filters.value.gradeLevel] || ''}数学试卷`,
      gradeName: item.gradeLevel || gradeLevelNames[filters.value.gradeLevel] || '',
      subjectName: item.subject || '数学',
      examTypeName: ['单元测试', '月考', '期中', '期末', '模拟'][item.id % 5],
      badgeColor: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399'][item.id % 5],
      score: item.totalScore || 100,
      questionCount: item.questionCount || 20,
      duration: item.duration || 90,
      downloads: `${(Math.floor(Math.random() * 80) + 5) / 10}万`,
      isFree: item.id % 3 !== 0,
    }))
    total.value = examList.value.length
  } catch (_e: any) {
    ElMessage.error('获取试卷列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadExams()
})
</script>

<style scoped>
.exam-banner {
  background: linear-gradient(135deg, #3B82F6, #6366F1);
  color: #fff;
  padding: 40px 0;
}
.breadcrumb-link { color: rgba(255,255,255,0.8); }
.breadcrumb-sep { margin: 0 8px; opacity: 0.6; }
.exam-banner h1 { font-size: 32px; font-weight: 700; margin: 12px 0 8px; }
.exam-banner p { opacity: 0.9; }

.filter-bar {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid var(--border-light);
}
.filter-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}
.filter-row:last-child { margin-bottom: 0; }
.filter-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  padding-top: 6px;
  flex-shrink: 0;
  width: 48px;
}
.filter-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.filter-tag {
  padding: 4px 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-round);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-regular);
}
.filter-tag:hover { border-color: var(--color-primary); color: var(--color-primary); }
.filter-tag.active { background: var(--color-primary); border-color: var(--color-primary); color: #fff; }

.exam-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.exam-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  gap: 16px;
}
.exam-badge {
  padding: 6px 14px;
  border-radius: var(--radius-round);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}
.exam-main { flex: 1; min-width: 0; }
.exam-main h4 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.exam-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--text-secondary);
}
.exam-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex-shrink: 0;
}
.exam-count { font-size: 12px; color: var(--text-secondary); }

.pagination-wrap { display: flex; justify-content: center; margin-top: 32px; }
</style>
