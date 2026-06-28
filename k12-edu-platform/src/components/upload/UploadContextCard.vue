<template>
  <el-card class="upload-context-card" shadow="never">
    <template #header>
      <div class="card-head">
        <span class="head-title">上传位置</span>
        <el-tag type="success" effect="plain" size="small">与列表筛选一致</el-tag>
      </div>
    </template>

    <el-descriptions :column="2" border size="small" class="context-desc">
      <el-descriptions-item label="学段">{{ stageLabel }}</el-descriptions-item>
      <el-descriptions-item label="学科">{{ subjectLabel }}</el-descriptions-item>
      <el-descriptions-item label="栏目">
        <el-tag type="primary" effect="plain">{{ module }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="资源类型">
        <el-tag type="warning" effect="plain">{{ teachingType }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="教材册别">{{ gradeName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="教材版本">{{ editionName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="教材目录" :span="2">
        {{ catalogLabel }}
      </el-descriptions-item>
      <el-descriptions-item label="资源形态">{{ resourceModeLabel }}</el-descriptions-item>
      <el-descriptions-item label="上传后展示">
        {{ listHint }}
      </el-descriptions-item>
    </el-descriptions>

    <p v-if="lessonName" class="context-tip">
      资源将出现在课文「{{ lessonName }}」的「{{ teachingType }}」分类下；标题建议包含课文名以便检索。
    </p>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatResourceModeLabel } from '@/utils/uploadRoute'

const props = defineProps<{
  fromBrowse: boolean
  stageLabel: string
  subjectLabel: string
  module: string
  teachingType: string
  gradeName: string
  editionName: string
  unitName: string
  lessonName: string
  resourceMode: 'single' | 'suite'
}>()

const resourceModeLabel = computed(() => formatResourceModeLabel(props.resourceMode))

const catalogLabel = computed(() => {
  if (props.lessonName) {
    return props.unitName ? `${props.unitName} · ${props.lessonName}` : props.lessonName
  }
  return props.unitName || '未指定（将按单元或年级展示）'
})

const listHint = computed(() => {
  const parts = [props.module, props.teachingType !== '全部' ? props.teachingType : ''].filter(Boolean)
  return parts.length ? `当前栏目 · ${parts.join(' · ')} 列表` : '学科资源列表'
})
</script>

<style scoped>
.upload-context-card {
  margin-bottom: 20px;
  border: 1px solid #d9ecff;
  background: linear-gradient(180deg, #f8fbff 0%, #fff 100%);
}
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.head-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.context-desc {
  margin-bottom: 0;
}
.context-tip {
  margin: 12px 0 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
</style>
