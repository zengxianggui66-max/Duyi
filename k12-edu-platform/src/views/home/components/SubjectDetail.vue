<template>
  <div class="subject-detail">
    <button type="button" class="close-btn" aria-label="关闭" @click="$emit('close')">×</button>

    <div v-if="loading" class="detail-loading">
      <span>正在加载筛选项…</span>
    </div>

    <div v-else class="detail-body">
      <!-- 同步备课 -->
      <section class="detail-block">
        <h3 class="block-title">同步备课</h3>
        <div class="block-rows">
          <div class="filter-row">
            <span class="filter-label">教材版本</span>
            <div v-if="!displayVersions.length" class="filter-tags filter-tags--hint">
              <span class="hint-text">暂无可用版本</span>
            </div>
            <div v-else class="filter-tags">
              <button
                v-for="ver in displayVersions"
                :key="`${ver.key}-${ver.editionName}`"
                type="button"
                :class="['filter-tag', { active: selectedVersion === ver.key }]"
                @click="$emit('select-version', ver, subject)"
              >
                {{ ver.name }}
                <span v-if="ver.isNew" class="tag-new">新</span>
              </button>
            </div>
          </div>

          <div v-if="resourceTypes.length" class="filter-row">
            <span class="filter-label">资料类型</span>
            <div class="filter-tags">
              <button
                v-for="typeName in resourceTypes"
                :key="typeName"
                type="button"
                :class="['filter-tag', { active: selectedResourceType === typeName }]"
                @click="$emit('select-resource-type', typeName, subject)"
              >
                {{ typeName }}
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- 复习备考 -->
      <section v-if="reviewModules.length" class="detail-block">
        <h3 class="block-title">复习备考</h3>
        <div class="block-rows">
          <div class="filter-row">
            <span class="filter-label">试题试卷</span>
            <div class="filter-tags">
              <button
                v-for="moduleName in reviewModules"
                :key="moduleName"
                type="button"
                :class="['filter-tag', { active: selectedReviewModule === moduleName }]"
                @click="$emit('select-review-module', moduleName, subject)"
              >
                {{ moduleName }}
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- 升学备考（小学：小升初） -->
      <section v-if="promotionModules.length" class="detail-block">
        <h3 class="block-title">{{ promotionSectionTitle }}</h3>
        <div class="block-rows">
          <div class="filter-row">
            <span class="filter-label">{{ promotionSectionTitle }}</span>
            <div class="filter-tags">
              <button
                v-for="moduleName in promotionModules"
                :key="moduleName"
                type="button"
                :class="['filter-tag', { active: selectedPromotionModule === moduleName }]"
                @click="$emit('select-promotion-module', moduleName, subject)"
              >
                {{ moduleName }}
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { EditionVersionOption } from '@/utils/editionAdapter'
import type { StageKey } from '@/config/subjectConfig'

interface Subject {
  key: string
  name: string
}

const props = defineProps<{
  stage: StageKey
  subject: Subject
  versions: EditionVersionOption[]
  resourceTypes: string[]
  reviewModules: string[]
  promotionModules: string[]
  promotionSectionTitle: string
  loading?: boolean
  selectedVersion: string
  selectedResourceType: string
  selectedReviewModule: string
  selectedPromotionModule: string
}>()

defineEmits<{
  close: []
  'select-version': [version: EditionVersionOption, subject: Subject]
  'select-resource-type': [typeName: string, subject: Subject]
  'select-review-module': [moduleName: string, subject: Subject]
  'select-promotion-module': [moduleName: string, subject: Subject]
}>()

const displayVersions = computed(() => props.versions || [])
</script>

<style scoped>
.subject-detail {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
  max-height: 100%;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #b3d4fc;
  box-shadow: 0 8px 24px rgba(26, 92, 191, 0.12);
  overflow: hidden;
}

.close-btn {
  position: absolute;
  top: 8px;
  right: 10px;
  z-index: 2;
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  border-radius: 50%;
  font-size: 22px;
  line-height: 1;
  color: #909399;
  cursor: pointer;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #f0f2f5;
  color: #606266;
}

.detail-loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  text-align: center;
  font-size: 14px;
  color: #909399;
}

.detail-body {
  flex: 1;
  min-height: 0;
  padding: 16px 0 24px;
  overflow-y: auto;
}

.detail-block {
  display: flex;
  align-items: stretch;
  border-bottom: 1px solid #f0f0f0;
}

.detail-block:last-child {
  border-bottom: none;
}

.block-title {
  flex: 0 0 100px;
  margin: 0;
  padding: 22px 12px 22px 24px;
  font-size: 14px;
  font-weight: 700;
  color: #303133;
  line-height: 1.5;
  border-right: 1px solid #f5f5f5;
  background: #fafbfc;
}

.block-rows {
  flex: 1;
  min-width: 0;
  padding: 10px 40px 14px 20px;
}

.filter-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 18px 0;
  border-bottom: 1px solid #f5f5f5;
}

.filter-row:last-child {
  border-bottom: none;
}

.filter-label {
  flex: 0 0 76px;
  font-size: 13px;
  font-weight: 500;
  color: #909399;
  line-height: 32px;
  white-space: nowrap;
}

.filter-label::after {
  content: '：';
}

.filter-tags {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 12px 20px;
  min-width: 0;
  align-items: center;
}

.filter-tags--hint {
  min-height: 32px;
}

.hint-text {
  font-size: 13px;
  color: #c0c4cc;
}

.filter-tag {
  padding: 4px 0;
  border: none;
  background: transparent;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: color 0.15s;
  white-space: nowrap;
  line-height: 28px;
}

.filter-tag:hover {
  color: #ff6b00;
}

.filter-tag.active {
  color: #ff6b00;
  font-weight: 600;
}

.tag-new {
  display: inline-block;
  margin-left: 2px;
  padding: 0 3px;
  font-size: 10px;
  font-weight: 600;
  line-height: 14px;
  color: #fff;
  background: #f56c6c;
  border-radius: 2px;
  vertical-align: super;
}

@media (max-width: 768px) {
  .detail-block {
    flex-direction: column;
  }

  .block-title {
    flex: none;
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #f5f5f5;
    padding: 10px 16px;
  }

  .block-rows {
    padding: 2px 16px 8px;
  }
}
</style>
