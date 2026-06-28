<template>
  <el-card class="form-card">
    <template #header>
      <div class="card-header">
        <span class="card-title">资源分类配置</span>
      </div>
    </template>

    <el-form :model="formData" label-width="140px" class="upload-form">
      <template v-if="isSyncUpload">
        <el-form-item label="资源类型" required>
          <el-radio-group v-model="formData.teachingType" class="teaching-type-group">
            <el-radio
              v-for="t in syncTeachingTypes"
              :key="t"
              :value="t"
              class="teaching-type-radio"
            >
              {{ t }}
            </el-radio>
          </el-radio-group>
          <p class="field-hint">与学科资源列表中的类型一致，上传后将出现在对应分类下。</p>
        </el-form-item>
      </template>

      <template v-else>
        <el-form-item label="资源类型" prop="resourceType">
          <el-radio-group v-model="formData.resourceType" @change="handleResourceTypeChange">
            <el-radio value="paper">纸质类</el-radio>
            <el-radio value="video">音视频类</el-radio>
            <el-radio value="document">文档类</el-radio>
            <el-radio value="tool">数字化工具</el-radio>
            <el-radio value="expand">拓展资源</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="子类型">
          <el-select v-model="formData.subType" placeholder="请选择子类型" style="width: 100%">
            <el-option
              v-for="subType in currentSubTypes"
              :key="subType.value"
              :label="subType.label"
              :value="subType.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="教材版本">
          <el-select v-model="formData.version" placeholder="请选择教材版本" style="width: 100%">
            <el-option-group
              v-for="group in currentTextbookVersions"
              :key="group.label"
              :label="group.label"
            >
              <el-option
                v-for="version in group.options"
                :key="version.value"
                :label="version.label"
                :value="version.value"
              />
            </el-option-group>
          </el-select>
        </el-form-item>
      </template>

      <template v-if="!uploadModuleSchema.hideExamPrep">
        <el-divider content-position="left">备考类型配置</el-divider>

        <el-form-item label="备考类型">
          <el-checkbox-group v-model="formData.examTypes">
            <el-checkbox
              v-for="examType in currentExamTypes"
              :key="examType.value"
              :label="examType.value"
            >
              {{ examType.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </template>

      <el-divider content-position="left">额外配置</el-divider>

      <el-form-item label="适用场景">
        <el-checkbox-group v-model="formData.scenarios">
          <el-checkbox value="classroom">课堂教学</el-checkbox>
          <el-checkbox value="homework">课后作业</el-checkbox>
          <el-checkbox value="review">复习备考</el-checkbox>
          <el-checkbox value="competition">竞赛辅导</el-checkbox>
          <el-checkbox value="exam_level">考级集训</el-checkbox>
          <el-checkbox value="parent">家长辅导</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="资源难度">
        <el-radio-group v-model="formData.difficulty">
          <el-radio value="basic">基础</el-radio>
          <el-radio value="improved">提高</el-radio>
          <el-radio value="excellent">培优</el-radio>
          <el-radio value="competition">竞赛</el-radio>
          <el-radio value="art_exam">艺考</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="免费资源">
        <el-switch v-model="formData.isFree" :active-value="1" :inactive-value="0" />
        <span class="switch-hint">开启后标记为免费资源，下载无需积分</span>
      </el-form-item>

      <el-form-item label="允许预览">
        <el-switch v-model="formData.allowPreview" :active-value="1" :inactive-value="0" />
        <span class="switch-hint">开启后可在线预览，关闭则仅支持下载</span>
      </el-form-item>

      <el-form-item label="排序权重">
        <el-input-number v-model="formData.sortWeight" :min="0" :max="100" />
        <span class="input-hint">0-100，权重越高在列表中越靠前</span>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { useUploadFormInject } from '@/composables/useUploadFormInject'

const {
  formData,
  isSyncUpload,
  uploadModuleSchema,
  syncTeachingTypes,
  currentSubTypes,
  currentTextbookVersions,
  currentExamTypes,
  handleResourceTypeChange,
} = useUploadFormInject()
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
.switch-hint,
.input-hint,
.field-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}
.field-hint {
  margin: 8px 0 0;
  width: 100%;
}
.teaching-type-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
}
.teaching-type-radio {
  margin-right: 0;
}
</style>
