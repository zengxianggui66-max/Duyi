<template>
  <el-card class="form-card">
    <template #header>
      <div class="card-header">
        <span class="card-title">{{ compactBasicMode ? '标题与说明' : '资源基本信息' }}</span>
        <span class="required-hint">带 * 的为必填项</span>
      </div>
    </template>

    <UploadBrowseContextBar
      v-if="!compactBasicMode"
      :from-browse="fromBrowse"
      :module="formData.module"
      :summary="contextSummary"
      :lesson-name="formData.lessonName"
    />

    <UploadRecommendPanel
      v-if="!compactBasicMode && showUploadRecommendations"
      :recommendations="uploadRecommendations"
      :active-id="activeRecommendationId"
      :context-line="recommendContextLine"
      :auto-applied-hint="recommendAutoHint"
      :show-legacy="!isSyncUpload || formData.gradeLevel === 'art' || formData.gradeLevel === 'dance'"
      :stage-key="formData.gradeLevel"
      @apply="applyRecommendationById"
      @legacy="applyLegacyTemplateById"
    />

    <el-form
      :model="formData"
      :rules="basicRules"
      ref="basicFormRef"
      label-width="120px"
      class="upload-form"
    >
      <template v-if="isSyncUpload && !compactBasicMode">
        <el-form-item label="栏目">
          <el-input v-model="formData.module" disabled />
        </el-form-item>
        <el-form-item label="教材年级">
          <el-input v-model="formData.gradeName" disabled placeholder="请从学科页进入上传以自动带入" />
        </el-form-item>
        <el-form-item label="教材版本">
          <el-input v-model="formData.editionName" disabled />
        </el-form-item>
        <el-form-item v-if="formData.unitName || formData.lessonName" label="教材目录">
          <el-input
            :model-value="formData.lessonName ? `${formData.unitName} · ${formData.lessonName}` : formData.unitName"
            disabled
          />
        </el-form-item>
      </template>

      <el-form-item label="资源名称" prop="title">
        <el-input
          v-model="formData.title"
          placeholder="如：外研版三年级英语上册同步教案"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="资源简介" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="4"
          placeholder="描述资源的核心内容、适用场景、特色亮点..."
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item v-if="showTeachingFlowField" label="教学流程">
        <el-input
          v-model="formData.teachingFlowText"
          type="textarea"
          :rows="5"
          placeholder="每行一个环节，例如：&#10;5分钟|导入|播放短视频引出课题&#10;一、情境导入（约5分钟）：联系学生经验，引出课题"
        />
        <p class="field-hint">选填。填写后将在资源详情页「教学流程」侧边栏展示；不填则根据简介智能生成。</p>
      </el-form-item>

      <el-form-item v-if="!compactBasicMode" label="学段" prop="gradeLevel">
        <el-radio-group
          v-model="formData.gradeLevel"
          :disabled="fromBrowse"
          @change="handleGradeLevelChange"
        >
          <el-radio-button
            v-for="stage in stageOptions"
            :key="stage.key"
            :value="stage.key"
          >
            {{ stage.name }}
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="!compactBasicMode" label="学科" prop="subject">
        <el-select
          v-model="formData.subject"
          placeholder="请先选择学段"
          :disabled="!formData.gradeLevel || fromBrowse"
          @change="handleSubjectChange"
          style="width: 100%"
        >
          <el-option
            v-for="subject in currentSubjects"
            :key="subject.value"
            :label="subject.label"
            :value="subject.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item v-if="!compactBasicMode" label="年级" prop="grade">
        <el-select
          v-model="formData.grade"
          placeholder="请选择年级"
          :disabled="!formData.gradeLevel"
          style="width: 100%"
        >
          <el-option
            v-for="grade in currentGrades"
            :key="grade.value"
            :label="grade.label"
            :value="grade.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="资源属性">
        <div class="resource-attrs">
          <el-checkbox v-model="formData.isFree" :true-value="1" :false-value="0">
            免费资源
          </el-checkbox>
          <el-checkbox v-model="formData.allowPreview" :true-value="1" :false-value="0">
            允许在线预览
          </el-checkbox>
        </div>
        <p class="field-hint">「精品/同步」等运营标签由平台审核后标注，上传时无需填写。</p>
      </el-form-item>

    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import UploadBrowseContextBar from '@/components/upload/UploadBrowseContextBar.vue'
import UploadRecommendPanel from '@/components/upload/UploadRecommendPanel.vue'
import { useUploadFormInject } from '@/composables/useUploadFormInject'

const props = withDefaults(
  defineProps<{
    browseWizardMode?: boolean
  }>(),
  { browseWizardMode: false },
)

const {
  placementConfirmed,
  isSyncUpload,
  formData,
  basicRules,
  basicFormRef,
  currentSubjects,
  currentGrades,
  stageOptions,
  fromBrowse,
  contextSummary,
  showUploadRecommendations,
  uploadRecommendations,
  activeRecommendationId,
  recommendContextLine,
  recommendAutoHint,
  applyRecommendationById,
  applyLegacyTemplateById,
  showTeachingFlowField,
  handleGradeLevelChange,
  handleSubjectChange,
} = useUploadFormInject()

const compactBasicMode = computed(
  () => props.browseWizardMode || (placementConfirmed.value && isSyncUpload.value),
)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-size: 18px;
  font-weight: 600;
}
.required-hint {
  font-size: 12px;
  color: #909399;
}
.resource-attrs {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 24px;
}
.resource-attrs :deep(.el-checkbox) {
  margin-right: 0;
}
.field-hint {
  margin: 6px 0 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
</style>
