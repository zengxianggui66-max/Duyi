<template>
  <el-card class="form-card placement-card">
    <template #header>
      <div class="card-header">
        <span class="card-title">选择上传位置</span>
        <el-button type="primary" link @click="goBrowseUpload">从学科页进入可自动带入</el-button>
      </div>
    </template>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="placement-alert"
      title="上传位置决定资源在学科列表中的展示栏目、目录与类型，请先选择后再填写详情。"
    />

    <el-form label-width="120px" class="placement-form">
      <el-form-item label="学段" required>
        <el-radio-group
          v-model="formData.gradeLevel"
          :disabled="fromBrowse"
          @change="onStageChange"
        >
          <el-radio-button
            v-for="s in stageOptions"
            :key="s.key"
            :value="s.key"
          >
            {{ s.name }}
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="学科" required>
        <el-select
          v-model="formData.subject"
          placeholder="请选择学科"
          :disabled="!formData.gradeLevel || fromBrowse"
          style="width: 100%"
          @change="onSubjectChange"
        >
          <el-option
            v-for="s in currentSubjects"
            :key="s.value"
            :label="s.label"
            :value="s.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="栏目" required>
        <el-select v-model="formData.module" style="width: 100%" @change="onModuleChange">
          <el-option
            v-for="m in moduleOptions"
            :key="m"
            :label="m"
            :value="m"
          />
        </el-select>
      </el-form-item>

      <template v-if="placementSchema.showSyncCatalog">
        <el-form-item label="教材册别" required>
          <el-select
            v-model="formData.gradeName"
            placeholder="请选择册别"
            filterable
            style="width: 100%"
            :loading="optionsLoading"
            @change="handleCatalogChange"
          >
            <el-option v-for="g in gradeOptions" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>

        <el-form-item label="教材版本" required>
          <el-select
            v-model="formData.editionName"
            placeholder="请选择版本"
            filterable
            style="width: 100%"
            :loading="optionsLoading"
            @change="handleCatalogChange"
          >
            <el-option v-for="e in editionOptions" :key="e" :label="e" :value="e" />
          </el-select>
        </el-form-item>

        <el-form-item label="教材单元">
          <el-select
            v-model="selectedUnit"
            placeholder="可选，不选则按年级展示"
            clearable
            filterable
            style="width: 100%"
            :loading="treeLoading"
            :disabled="!canLoadCatalogTree"
            @change="handleUnitChange"
          >
            <el-option v-for="u in unitTree" :key="u.name" :label="u.name" :value="u.name" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="lessonOptions.length" label="课文">
          <el-select
            v-model="formData.lessonName"
            placeholder="可选，关联到具体课文"
            clearable
            filterable
            style="width: 100%"
            @change="handleLessonChange"
          >
            <el-option v-for="l in lessonOptions" :key="l" :label="l" :value="l" />
          </el-select>
        </el-form-item>
      </template>

      <template v-if="placementSchema.showTeachingType">
        <el-form-item label="资源类型" required>
          <el-radio-group v-model="formData.teachingType" class="type-group" @change="emitPlacementChange">
            <el-radio
              v-for="t in effectiveTeachingTypeOptions"
              :key="t"
              :value="t"
            >
              {{ t }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </template>

      <el-form-item v-if="placementSchema.showResourceMode" label="资源形态">
        <el-radio-group v-model="resourceMode" @change="emitPlacementChange">
          <el-radio value="single">找单份</el-radio>
          <el-radio value="suite">找成套</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="placementSummary" label="位置预览">
        <p class="placement-summary">{{ placementSummary }}</p>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { formatUploadContextSummary } from '@/utils/uploadRoute'
import { getUploadModuleSchema } from '@/constants/uploadSchema'
import { useUploadFormInject } from '@/composables/useUploadFormInject'

const router = useRouter()

const {
  formData,
  browseSnapshot,
  uploadModuleSchema,
  subjectLabel,
  fromBrowse,
  syncPlacementFromForm,
  stageOptions,
  currentSubjects,
  moduleOptions,
  gradeOptions,
  editionOptions,
  resourceTypeOptions,
  optionsLoading,
  treeLoading,
  unitTree,
  selectedUnit,
  lessonOptions,
  canLoadCatalogTree,
  handleGradeLevelChange,
  handleSubjectChange,
  handleModuleChange,
  handleCatalogChange,
  handleUnitChange,
  handleLessonChange,
} = useUploadFormInject()

const placementSchema = computed(() => uploadModuleSchema.value)

const effectiveTeachingTypeOptions = computed(() => {
  const schemaTypes = placementSchema.value.teachingTypeOptions
  if (!resourceTypeOptions.value.length) return schemaTypes
  const intersect = schemaTypes.filter((t) => resourceTypeOptions.value.includes(t))
  if (intersect.length) return intersect
  return schemaTypes
})

function sanitizeTeachingType() {
  const allowed = effectiveTeachingTypeOptions.value
  const current = formData.teachingType
  const looksLikeCatalogRoot =
    !!current && (current.includes('（统编版）') || current.includes('（人教版）'))
  if (looksLikeCatalogRoot || (current && !allowed.includes(current))) {
    formData.teachingType = effectiveDefaultTeachingType.value
  }
}

const effectiveDefaultTeachingType = computed(
  () => effectiveTeachingTypeOptions.value[0] || placementSchema.value.defaultTeachingType,
)

const resourceMode = computed({
  get: () => browseSnapshot.value.resourceMode || 'single',
  set: (v: 'single' | 'suite') => {
    browseSnapshot.value.resourceMode = v
    emitPlacementChange()
  },
})

const placementSummary = computed(() => {
  if (!formData.gradeLevel || !formData.subject) return ''
  const snap = syncPlacementFromForm()
  return formatUploadContextSummary(snap, subjectLabel.value)
})

function emitPlacementChange() {
  syncPlacementFromForm()
}

async function onStageChange() {
  await handleGradeLevelChange()
  emitPlacementChange()
}

async function onSubjectChange() {
  await handleSubjectChange()
  emitPlacementChange()
}

async function onModuleChange() {
  const schema = getUploadModuleSchema(formData.module)
  await handleModuleChange(schema.showSyncCatalog)
  sanitizeTeachingType()
  if (!effectiveTeachingTypeOptions.value.includes(formData.teachingType)) {
    formData.teachingType = effectiveDefaultTeachingType.value
  }
  emitPlacementChange()
}

function goBrowseUpload() {
  if (formData.gradeLevel && formData.subject) {
    router.push({
      name: 'SubjectDetail',
      params: {
        stage: formData.gradeLevel,
        subject: formData.subject,
        version: 'tongbian2024',
      },
      query: formData.module ? { module: formData.module } : undefined,
    })
    return
  }
  router.push('/')
}
</script>

<style scoped>
.placement-card {
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.card-title {
  font-size: 18px;
  font-weight: 600;
}
.placement-alert {
  margin-bottom: 20px;
}
.type-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
}
.placement-summary {
  margin: 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
</style>
