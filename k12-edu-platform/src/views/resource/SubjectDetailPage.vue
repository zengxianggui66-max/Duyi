<template>
  <div class="platform-page">
    <div class="page-body">
      <div
        class="browse-filter-panel"
        :class="{ 'has-extended-filters': columnLayout === 'exam' || columnLayout === 'topic' || columnLayout === 'reading_writing' || showFinalReviewExamFilters }"
      >
        <BrowseBreadcrumb :items="breadcrumbItems" />
        <SeriesFilterBar
          :series-tabs="seriesTabs"
          :selected-brand-code="selectedBrandCode"
          @select="onSelectBrand"
        />
        <FilterBar
          :current-subjects="currentSubjects"
          :current-subject="currentSubject"
          :top-visible-columns="displayTopVisibleColumns"
          :grouped-columns="groupedColumns"
          :active-column="activeColumn"
          :group-count-map="moreColumnGroupCountMap"
          :show-group-counts="true"
          :column-count-map="moduleCountMap"
          :show-column-counts="true"
          @select-subject="selectSubject"
          @update:active-column="activeColumn = $event"
        />

        <!-- 月考/期中/期末/开学专区/专题等：二级筛选与顶部同卡 -->
        <div v-if="columnLayout === 'exam' || columnLayout === 'topic' || showFinalReviewExamFilters" class="extended-filters">
          <div class="extended-filter-select-row">
            <div v-if="columnLayout === 'exam' || showGradeFilter" class="extended-filter-select-item">
              <span class="extended-filter-select-label">年级：</span>
              <el-select v-model="selectedGrade" size="default" placeholder="请选择年级" clearable>
                <el-option v-for="opt in gradeOptions" :key="'g-' + opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </div>
            <div v-if="(columnLayout === 'exam' || showGradeFilter) && showSemesterFilter" class="extended-filter-select-item">
              <span class="extended-filter-select-label">学期：</span>
              <el-select v-model="selectedSemester" size="default" placeholder="请选择学期" clearable>
                <el-option v-for="opt in semesterOptions" :key="'s-' + opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </div>
            <div class="extended-filter-select-item">
              <span class="extended-filter-select-label">年份：</span>
              <el-select v-model="selectedYear" size="default" placeholder="请选择年份" clearable>
                <el-option v-for="opt in yearOptions" :key="'y-' + opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </div>
            <div class="extended-filter-select-item">
              <span class="extended-filter-select-label">版本：</span>
              <el-select v-model="selectedVersion" size="default" placeholder="请选择版本" clearable>
                <el-option v-for="opt in versionOptions" :key="'v-' + opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </div>
            <div v-if="showExamRegionFilter || showFinalReviewExamFilters" class="extended-filter-select-item">
              <span class="extended-filter-select-label">地区：</span>
              <el-select v-model="selectedRegion" size="default" placeholder="请选择地区" clearable>
                <el-option v-for="opt in regionOptions" :key="'r-' + opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </div>
            <div class="extended-filter-select-item">
              <span class="extended-filter-select-label">类型：</span>
              <el-select v-model="selectedType" size="default" placeholder="请选择类型" clearable>
                <el-option v-for="opt in examTypeOptions" :key="'t-' + opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </div>
          </div>
        </div>
      </div>

      <div v-if="currentSubject" class="content-two-columns" :class="'layout-' + columnLayout">
        <!-- ⚠️ 注意：如果页面空白，通常是 currentSubject 未从路由恢复。
             顶层 initFromRoute() + applySubjectFromRoute() 应在 setup 阶段同步完成，
             首次动态导入加载失败时会出现空白，刷新后恢复。 -->
        <!-- 同步备课布局（默认双栏：左侧目录 + 右侧资源列表） -->
        <template v-if="columnLayout === 'default' || columnLayout === 'category_list'">
          <CourseCatalog
              :currentStage="currentStage"
              :currentStageName="currentStageName"
              :currentSubject="currentSubject"
              :currentSubjects="currentSubjects"
              :currentSubjectVersions="currentSubjectVersions"
              :selectedVersionKey="selectedVersionKey"
              :selectedVersionName="selectedVersionName"
              :isNewTextbook="isNewTextbook"
              :currentPublisher="currentPublisher"
              :volumeList="volumeList"
              :selectedVolumeId="selectedVolumeId"
              :currentGradeLevelName="currentGradeLevelName"
              :currentUnitList="displayUnitList"
              :activeUnit="activeUnit"
              :allUnitsExpanded="allUnitsExpanded"
              :use-catalog-browse="useCatalogBrowse"
              :catalog-nodes="catalogTree"
              :catalog-active-node-id="activeCatalogNodeId"
              :catalog-loading="catalogLoading"
              :catalog-display-mode="catalogDisplayMode"
              :catalog-all-expanded="catalogAllExpanded"
              :catalog-is-expanded="catalogIsNodeExpanded"
              @update:selectedVersionKey="selectedVersionKey = $event"
              @update:isNewTextbook="isNewTextbook = $event"
              @update:selected-volume-id="onVolumeIdUpdate"
              @update:activeUnit="activeUnit = $event"
              @update:allUnitsExpanded="onToggleAllUnits"
              @confirm-version="onConfirmVersion"
              @catalog-select="onCatalogSelect"
              @catalog-toggle-expand="onCatalogToggleExpand"
              @catalog-toggle-all="onCatalogToggleAll"
              @catalog-prefetch="onCatalogPrefetch"
          />

          <div class="resource-list-area">
            <ReviewCoursewareTabs
              :visible="showCoursewareTabs"
              :model-value="reviewScope"
              @update:model-value="onReviewScopeChange"
            />
            <SyncPrepCrossLink
              :link="syncPrepLinkForUnit"
              :route-params="{
                stage: currentStage,
                subject: currentSubject?.key || 'chinese',
                version: selectedVersionKey,
              }"
              :brand="selectedBrandCode || undefined"
              :volume="selectedVolumeId || undefined"
            />
            <ResourceTypeBar
              :resource-types="resourceTypes"
              :teacher-book-types="teacherBookTypes"
              :active-resource-type="activeResourceType"
              :type-counts="displayTypeCounts"
              :sort-counts="displaySortCounts"
              :show-counts="resourceMode === 'single'"
              :sort-ready="sortStatsReady"
              :stats-ready="contextStatsReady"
              :stats-loading="contextStatsLoading"
              :scope-key="typeStatsScopeKey"
              @select-resource-type="selectResourceType"
            />
            <div class="resource-list-scroll">
            <AdvancedFilterBar
              :resource-mode="resourceMode"
              :sort-type="sortType"
              :total-count="listTotalCount"
              :batch-mode="batchMode"
              @update:resource-mode="resourceMode = $event"
              @update:sort-type="sortType = $event"
              @update:batch-mode="batchMode = $event"
            />

            <SuiteSection
              v-show="!isLessonHubActive"
              :resource-mode="resourceMode"
              :suites="suitePackages"
              :paginated-suites="suitePaginatedSuites"
              :total="suitePackages.length"
              :current-page="suiteCurrentPage"
              :total-pages="suiteTotalPages"
              :visible-pages="suiteVisiblePages"
              @update:current-page="suiteCurrentPage = $event"
              @open-resource="openResource"
            />

            <div v-show="isLessonHubActive" class="lesson-hub-panel">
              <LessonHubHeader
                :lesson-name="lessonName"
                :parent-unit="parentUnitName"
                :stage-name="currentStageName"
                :subject-name="currentSubject?.name"
                :grade-level-name="currentGradeLevelName"
                :version-name="selectedVersionName"
                :column="activeColumn"
                :suite-hint="lessonSuiteHint"
                @upload="goToUpload"
              />
              <LessonGroupedList
                :resource-mode="resourceMode"
                :groups="groupedResources"
                :total="lessonDisplayTotal"
                :loading="lessonLoading"
                :active-type="activeResourceType"
                @open-resource="openResource"
              />
            </div>

            <ResourceListSection
                v-show="!isLessonHubActive"
                :resourceMode="resourceMode"
                :batch-mode="batchMode"
                :resources="apiResources"
                :total="apiTotal"
                :loading="apiLoading"
                :currentPage="apiCurrentPage"
                :pageSize="apiPageSize"
                :totalPages="apiTotalPages"
                :visiblePages="apiVisiblePages"
                :browse-summary="browseSummary"
                :show-upload-action="showEmptyUploadAction"
                :upload-label="emptyUploadLabel"
                :placement-hint="emptyPlacementHint"
                @open-resource="openResource"
                @update:currentPage="goApiPage"
                @update:pageSize="setApiPageSize"
                @upload="goToUpload"
                @download-resource="handleDownloadResource"
                @add-to-basket="handleAddToBasket"
                @toggle-favorite="handleToggleFavorite"
                @batch-download="handleBatchDownload"
                @batch-add-to-basket="handleBatchAddToBasket"
                @batch-favorite="handleBatchFavorite"
            />
            </div>
          </div>

          
        </template>

        <!-- 考试类：列表区（筛选已并入顶部 browse-filter-panel） -->
        <template v-if="columnLayout === 'exam'">
          <!-- 模式切换 + 排序 -->
          <div class="exam-toolbar">
            <!-- 资源模式组 -->
            <div class="toolbar-group">
              <button :class="['filter-chip', { active: resourceMode==='single' }]" @click="resourceMode = 'single'">找单份</button>
              <button :class="['filter-chip', { active: resourceMode==='suite' }]" @click="resourceMode = 'suite'">找成套</button>
            </div>
            <!-- 文档类型筛选 -->
            <el-dropdown trigger="click" @command="(cmd: string) => selectedDocType = cmd">
              <button class="filter-chip" :class="{ active: selectedDocType !== '' }">
                <span>{{ docTypeOptions.find(o => o.value === selectedDocType)?.label || '文档类型' }}</span>
                <el-icon :size="10"><ArrowDown /></el-icon>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="opt in docTypeOptions"
                    :key="opt.value"
                    :command="opt.value"
                    :class="{ 'is-active': selectedDocType === opt.value }"
                  >
                    {{ opt.label }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <!-- 排序组 -->
            <div class="toolbar-group">
              <button :class="['filter-chip', { active: sortType==='comprehensive' }]" @click="sortType='comprehensive'">综合</button>
              <button :class="['filter-chip', { active: sortType==='latest' }]" @click="sortType='latest'">最新</button>
            </div>
            <span class="result-count">共 {{ apiTotal }} 条结果</span>
          </div>
          <div v-if="apiLoading" class="topic-skeleton-list exam-skeleton">
            <div v-for="i in 5" :key="i" class="topic-skeleton-row" />
          </div>
          <EmptyState
            v-else-if="!apiResources.length"
            :description="browseSummary ? `当前筛选：${browseSummary}` : undefined"
            compact
          />
          <!-- 资源列表 -->
          <div v-else class="exam-resource-list">
            <div v-for="item in apiResources" :key="item.id" class="exam-resource-item" @click="openResource(item)">
              <div :class="'doc-icon-box ' + getDocIconClass(item)">{{ getDocIconLetter(item) }}</div>
              <div class="exam-resource-info">
                <div class="exam-resource-title">{{ item.title }}</div>
                <div class="exam-resource-meta">
                  <span>{{ (item.uploadTime || '').substring(0, 10) }}</span><span>|</span>
                  <span>{{ item.downloadCount || 0 }}次下载</span><span>|</span>
                  <span>{{ item.uploader || '教学网资源' }}</span>
                </div>
              </div>
            </div>
          </div>
          <AppPagination
            :currentPage="apiCurrentPage"
            :totalPages="apiTotalPages"
            :totalCount="apiTotal"
            :visiblePages="apiVisiblePages"
            @update:currentPage="goApiPage"
          />
        </template>

        <!-- 专题类：列表区（筛选已并入顶部 browse-filter-panel） -->
        <template v-if="columnLayout === 'topic'">
          <div class="topic-main-area">
            <!-- 左侧资源区 -->
            <div class="topic-card-grid">
              <!-- 工具栏 -->
              <div class="topic-toolbar">
                <div class="toolbar-group">
                  <button :class="['filter-chip', { active: resourceMode==='single' }]" @click="resourceMode = 'single'">找单份</button>
                  <button :class="['filter-chip', { active: resourceMode==='suite' }]" @click="resourceMode = 'suite'">找成套</button>
                </div>
                <div class="toolbar-group">
                  <button :class="['filter-chip', { active: sortType==='comprehensive' }]" @click="sortType='comprehensive'">综合</button>
                  <button :class="['filter-chip', { active: sortType==='latest' }]" @click="sortType='latest'">最新</button>
                  <button :class="['filter-chip', { active: sortType==='downloads' }]" @click="sortType='downloads'">下载量</button>
                </div>
                <el-dropdown trigger="click" @command="(cmd: string) => selectedDocType = cmd">
                  <div class="doc-type-selector">
                    <span>{{ docTypeOptions.find(o => o.value === selectedDocType)?.label || '文档类型' }}</span>
                    <el-icon :size="10"><ArrowDown /></el-icon>
                  </div>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item
                        v-for="opt in docTypeOptions"
                        :key="opt.value"
                        :command="opt.value"
                        :class="{ 'is-active': selectedDocType === opt.value }"
                      >
                        {{ opt.label }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <span class="result-count">共 {{ topicDisplayTotal }} 条结果</span>
              </div>

              <div v-if="apiLoading" class="topic-skeleton-list">
                <div v-for="i in 5" :key="i" class="topic-skeleton-row" />
              </div>

              <!-- API 资源列表 -->
              <div v-else-if="apiResources.length" class="topic-resource-list">
                <div v-for="item in apiResources" :key="item.id" class="topic-resource-item" @click="openResource(item)">
                  <div :class="'doc-icon-box ' + getDocIconClass(item)">{{ getDocIconLetter(item) }}</div>
                  <div class="topic-resource-info">
                    <div class="topic-resource-title">{{ item.title }}</div>
                    <div class="topic-resource-meta">
                      <span>{{ (item.uploadTime || '').substring(0, 10) }}</span><span>|</span>
                      <span>{{ item.downloadCount || 0 }}次下载</span><span>|</span>
                      <span>{{ item.uploader || '教学网资源' }}</span>
                    </div>
                    <div v-if="item.albumName" class="topic-resource-album">
                      专辑：<span class="album-link">{{ item.albumName }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <EmptyState
                v-if="!apiLoading && !apiResources.length"
                :description="browseSummary ? `当前筛选：${browseSummary}` : undefined"
                compact
              />
            </div>
          </div>
        </template>

        <!-- 国学阅读·作文深度融合布局 -->
        <template v-if="columnLayout === 'reading_writing'">
          <div class="reading-writing-container">
            <!-- 子标签栏 -->
            <div class="reading-writing-tabs">
              <button
                :class="['rw-tab', { active: activeReadingWritingTab === 'sinology' }]"
                @click="activeReadingWritingTab = 'sinology'"
              >国学阅读</button>
              <button
                :class="['rw-tab', { active: activeReadingWritingTab === 'composition' }]"
                @click="activeReadingWritingTab = 'composition'"
              >作文</button>
            </div>

            <!-- ===== 国学阅读子面板 ===== -->
            <template v-if="activeReadingWritingTab === 'sinology'">
              <ReadingWritingFilterBar
                class="sinology-filter-bar"
                :filters="sinologyFilterItems"
                :on-filter-change="searchSinology"
                :keyword="sinologyKeywordConfig"
                :search-action="{ text: '搜索', onClick: searchSinology }"
                :nowrap="true"
              />

              <div v-loading="sinologyLoading" class="sinology-card-grid">
                <div
                  v-for="item in sinologyList"
                  :key="item.id"
                  class="sinology-card"
                  @click="openSinologyDetail(item)"
                >
                  <div class="sinology-card-header">
                    <span class="sinology-genre-badge">{{ item.genre || '古诗' }}</span>
                    <span class="sinology-dynasty" v-if="item.dynasty">{{ item.dynasty }}</span>
                  </div>
                  <h4 class="sinology-card-title">{{ item.title }}</h4>
                  <p class="sinology-card-excerpt" v-if="item.content">
                    {{ item.content.length > 60 ? item.content.substring(0, 60) + '…' : item.content }}
                  </p>
                  <div class="sinology-card-meta">
                    <span v-if="item.author">{{ item.author }}</span>
                    <span v-if="item.difficulty" :class="'difficulty-tag difficulty-' + item.difficulty">
                      {{ Array.from({ length: item.difficulty }, () => '★').join('') }}
                    </span>
                  </div>
                </div>
              </div>

              <EmptyState
                v-if="!sinologyLoading && !sinologyList.length"
                description="暂无国学阅读素材，请调整筛选条件或尝试搜索"
                compact
              />

              <AppPagination
                v-if="sinologyTotalPages > 1"
                :currentPage="sinologyCurrentPage"
                :totalPages="sinologyTotalPages"
                :totalCount="sinologyTotal"
                :visiblePages="sinologyVisiblePages"
                @update:currentPage="sinologyCurrentPage = $event"
              />

              <!-- 详情弹窗 -->
              <el-dialog
                v-model="sinologyDialogVisible"
                :title="currentSinology?.title || '素材详情'"
                width="720px"
                top="5vh"
                destroy-on-close
              >
                <div v-if="currentSinology" class="sinology-detail">
                  <div class="sinology-detail-header">
                    <span class="sinology-genre-badge large">{{ currentSinology.genre }}</span>
                    <span class="sinology-detail-meta" v-if="currentSinology.dynasty">{{ currentSinology.dynasty }}</span>
                    <span class="sinology-detail-meta" v-if="currentSinology.author">· {{ currentSinology.author }}</span>
                  </div>
                  <div class="sinology-detail-section" v-if="currentSinology.content">
                    <h5>原文</h5>
                    <p>{{ currentSinology.content }}</p>
                  </div>
                  <div class="sinology-detail-section" v-if="currentSinology.translation">
                    <h5>译文</h5>
                    <p>{{ currentSinology.translation }}</p>
                  </div>
                  <div class="sinology-detail-section" v-if="currentSinology.appreciation">
                    <h5>赏析</h5>
                    <p>{{ currentSinology.appreciation }}</p>
                  </div>
                  <div class="sinology-detail-section" v-if="currentSinology.compositionHint">
                    <h5>写作提示</h5>
                    <p>{{ currentSinology.compositionHint }}</p>
                  </div>
                  <div class="sinology-detail-section" v-if="currentSinology.keyPhrases">
                    <h5>关键词</h5>
                    <p class="key-phrases-text">{{ currentSinology.keyPhrases }}</p>
                  </div>
                </div>
              </el-dialog>
            </template>

            <!-- ===== 作文子面板 ===== -->
            <template v-else>
              <ReadingWritingFilterBar
                class="composition-filter-bar"
                :filters="compositionFilterItems"
                :on-filter-change="onCompositionFilterChange"
                :nowrap="true"
              />

              <div class="exam-toolbar">
                <div class="toolbar-group">
                  <button :class="['filter-chip', { active: resourceMode==='single' }]" @click="resourceMode = 'single'">找单份</button>
                  <button :class="['filter-chip', { active: resourceMode==='suite' }]" @click="resourceMode = 'suite'">找成套</button>
                </div>
                <div class="toolbar-group">
                  <button :class="['filter-chip', { active: sortType==='comprehensive' }]" @click="sortType='comprehensive'">综合</button>
                  <button :class="['filter-chip', { active: sortType==='latest' }]" @click="sortType='latest'">最新</button>
                </div>
                <span class="result-count">共 {{ apiTotal }} 条结果</span>
              </div>

              <div v-if="apiLoading" class="topic-skeleton-list exam-skeleton">
                <div v-for="i in 5" :key="i" class="topic-skeleton-row" />
              </div>
              <EmptyState
                v-else-if="!apiResources.length"
                :description="browseSummary ? `当前筛选：${browseSummary}` : undefined"
                compact
              />
              <div v-else class="exam-resource-list">
                <div v-for="item in apiResources" :key="item.id" class="exam-resource-item" @click="openResource(item)">
                  <div :class="'doc-icon-box ' + getDocIconClass(item)">{{ getDocIconLetter(item) }}</div>
                  <div class="exam-resource-info">
                    <div class="exam-resource-title">{{ item.title }}</div>
                    <div class="exam-resource-meta">
                      <span>{{ (item.uploadTime || '').substring(0, 10) }}</span><span>|</span>
                      <span>{{ item.downloadCount || 0 }}次下载</span><span>|</span>
                      <span>{{ item.uploader || '教学网资源' }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <AppPagination
                :currentPage="apiCurrentPage"
                :totalPages="apiTotalPages"
                :totalCount="apiTotal"
                :visiblePages="apiVisiblePages"
                @update:currentPage="goApiPage"
              />
            </template>
          </div>
        </template>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'

// 瀵煎叆瀛愮粍浠?
import BrowseBreadcrumb from '../../components/subject/BrowseBreadcrumb.vue'
import SeriesFilterBar from '../../components/subject/SeriesFilterBar.vue'
import FilterBar from '../../components/subject/FilterBar.vue'
import CourseCatalog from '../../components/subject/CourseCatalog.vue'
import ResourceTypeBar from '../../components/subject/ResourceTypeBar.vue'
import AdvancedFilterBar from '../../components/subject/AdvancedFilterBar.vue'
import SuiteSection from '../../components/subject/SuiteSection.vue'
import ResourceListSection from '../../components/subject/ResourceListSection.vue'
import LessonHubHeader from '../../components/subject/LessonHubHeader.vue'
import LessonGroupedList from '../../components/subject/LessonGroupedList.vue'
import ReviewCoursewareTabs from '../../components/subject/ReviewCoursewareTabs.vue'
import SyncPrepCrossLink from '../../components/subject/SyncPrepCrossLink.vue'
import ReadingWritingFilterBar from '../../components/subject/ReadingWritingFilterBar.vue'

// 瀵煎叆 Composables
import { useStageSubject } from '../../composables/useStageSubject'
import { useVersionVolume } from '../../composables/useVersionVolume'
import { useUnitDirectory } from '../../composables/useUnitDirectory'
import { useCatalogTree } from '@/composables/useCatalogTree'
import { USE_CATALOG_BROWSE } from '@/config/featureFlags'
import { useBrowseTypeStats } from '@/composables/useBrowseTypeStats'
import { scrollCatalogNodeIntoView } from '@/utils/catalogScroll'
import EmptyState from '@/components/shared/EmptyState.vue'
import AppPagination from '@/components/shared/AppPagination.vue'
import type { FilterRowOption } from '@/composables/useFilterRowOverflow'
import { useColumnFilter } from '../../composables/useColumnFilter'
import { useApiResources } from '../../composables/useApiResources'
import { useLessonHub, useLessonHubGate } from '../../composables/useLessonHub'
import { useSubjectPageState } from '@/composables/useSubjectPageState'
import { useResourceBrowseContext, consumeUrlSyncFlag, buildBreadcrumbItems } from '@/composables/useResourceBrowseContext'
import { useResourceSeries } from '@/composables/useResourceSeries'
import { useFinalReview } from '@/composables/useFinalReview'
import { REVIEW_SCOPE, buildFinalReviewPathLabels, FINAL_REVIEW_MODULE, findFirstCoursewareLeaf } from '@/config/finalReviewConfig'
import type { ReviewScope } from '@/config/finalReviewConfig'
import { brandCodeForApi } from '@/config/resourceSeriesConfig'
import { stageNames, type StageKey } from '@/config/subjectConfig'
import { resolveVolumeIdByGradeSemester } from '@/config/volumeData'
import { buildUploadLocation } from '@/utils/uploadRoute'
import {
  resourceTypeTabsForColumn,
  MORE_TYPE_TABS,
  SYNC_PREP_COLUMN,
  mapSyncPrepTypeToQuery,
  syncPrepTabCountFallbackKey,
} from '@/constants/syncPrepColumnFilters'
import { normalizeEditionLabel } from '@/utils/editionAdapter'
import { composeTextbookStatsParams, warnTypeCountMismatch } from '@/utils/sortResourceTypes'
import { resolveCatalogIncludeSubtreeById } from '@/utils/catalogBrowseScope'
import { getExamTypeList } from '@/constants/examColumnFilters'

import { prepApi, sinologyApi } from '../../api'
import { unwrapData, requestCache } from '../../api/request'
import { resourceGateway } from '@/api/resourceGateway'
import type { SinologyReadingItem } from '@/api/types'
import type { ExamBrowseQuery } from '@/constants/examColumnFilters'

const route = useRoute()
const router = useRouter()

// 浣跨敤 Composables
const {
  currentStage,
  currentSubject,
  currentSubjects,
  currentStageName,
  selectSubject,
  ensureSubjectRouteValid,
} = useStageSubject()

const {
  currentSubjectVersions,
  selectedVersionKey,
  selectedVersionName,
  isNewTextbook,
  currentPublisher,
  volumeList,
  selectedVolumeId,
  currentGradeLevelName,
  setDefaultVolumeByStage,
  handleConfirmVersion,
} = useVersionVolume(currentStage, currentSubject)

const {
  seriesTabs,
  selectedBrandCode,
  selectedSeriesName,
  brandCodeParam,
  initBrandFromRoute,
  selectBrand,
} = useResourceSeries({ route, currentPublisher })

const useCatalogBrowse = USE_CATALOG_BROWSE

const {
  currentUnitList,
  activeUnit,
  allUnitsExpanded,
  initUnitList,
  toggleUnitExpand,
  toggleAllUnits,
  resolveParentUnitName: resolveParentUnitNameLegacy,
} = useUnitDirectory(currentStage, currentSubject, selectedVolumeId, selectedVersionName, currentGradeLevelName)

const catalogCtx = useCatalogTree({
  route,
  selectedBrandCode,
  selectedVolumeId,
  currentGradeLevelName,
  selectedVersionName,
  currentSubject,
  currentStage,
})

const {
  catalogTree,
  activeNodeId: activeCatalogNodeId,
  displayMode: catalogDisplayMode,
  loading: catalogLoading,
  allExpanded: catalogAllExpanded,
  initNodeFromRoute: initCatalogNodeFromRoute,
  loadTree: loadCatalogTree,
  selectNode: selectCatalogNode,
  toggleNodeExpand: toggleCatalogNodeExpand,
  isNodeExpanded: catalogIsNodeExpanded,
  toggleAllExpand: toggleCatalogAllExpand,
  resolveParentUnitName: resolveParentFromCatalog,
  resolvePlacementForUpload,
  findNodeById: findCatalogNodeById,
} = catalogCtx

/** 目录节点统计/列表范围（与后端 CatalogBrowseScope 一致） */
function resolveCatalogIncludeSubtree(nodeId = activeCatalogNodeId.value): boolean {
  if (!USE_CATALOG_BROWSE) return true
  return resolveCatalogIncludeSubtreeById(findCatalogNodeById, nodeId, true)
}

const displayUnitList = computed(() =>
  useCatalogBrowse ? catalogCtx.unitListCompat.value : currentUnitList.value,
)

const reviewScope = ref<ReviewScope>(REVIEW_SCOPE.unit)

function getReviewPathLabels() {
  return buildFinalReviewPathLabels(catalogTree.value, activeCatalogNodeId.value)
}

function resolveParentUnitName(unitName: string): string {
  if (useCatalogBrowse) {
    return resolveParentFromCatalog(unitName)
  }
  return resolveParentUnitNameLegacy(unitName)
}

const {
  currentColumns,
  displayTopVisibleColumns,
  groupedColumns,
  activeColumn,
} = useColumnFilter(currentStage, selectedBrandCode)

const resourceMode = ref<'single' | 'suite'>('single')
const sortType = ref<'comprehensive' | 'latest' | 'downloads'>('comprehensive')
const batchMode = ref(false)
const activeResourceType = ref('全部')
const searchKeyword = ref('')
const cartCount = ref(0)

/** 文档类型筛选（开学专区等专题布局） */
const docTypeOptions = [
  { label: '全部', value: '' },
  { label: 'Word文档', value: 'doc' },
  { label: 'PPT课件', value: 'ppt' },
  { label: 'PDF文档', value: 'pdf' },
  { label: '压缩包', value: 'zip' },
]
const selectedDocType = ref('')

const suppressTopicDemo = computed(() => !!selectedBrandCode.value)
const apiResourcesStub = ref<any[]>([])
const apiTotalStub = ref(0)
const pageState = useSubjectPageState(
  activeColumn,
  apiResourcesStub,
  apiTotalStub,
  currentSubject,
  suppressTopicDemo,
  searchKeyword,
  currentStage,
  currentGradeLevelName,
)
const {
  gradeList,
  selectedGrade,
  semesterList,
  selectedSemester,
  yearList,
  selectedYear,
  versionDisplayList,
  versionMoreList,
  selectedVersion,
  examTypeList,
  selectedType,
  onlyQuality,
  onlyAnswered,
  onlyTextVersion,
  regionDisplayList,
  regionMoreList,
  selectedRegion,
  showExamRegionFilter,
  examBrowseParams,
  layoutUsesExamFilters,
  topicDisplayTotal,
  showGradeFilter,
  showSemesterFilter,
  getTopicCoverColor,
  getSubjectLabel,
  getTypeLabel,
  getDocIconClass,
  getDocIconLetter,
  getDocIconClassFull,
  columnLayout,
  readingWritingColumns,
} = pageState

// ===== 国学阅读·作文深度融合 =====
const activeReadingWritingTab = ref<'sinology' | 'composition'>('sinology')

// -- 国学阅读筛选状态 --
const sinologyGrade = ref('')
const sinologyVersion = ref('')
const sinologyUnit = ref('')
const sinologySemester = ref('')
const sinologyKeyword = ref('')
const sinologyList = ref<SinologyReadingItem[]>([])
const sinologyTotal = ref(0)
const sinologyLoading = ref(false)
const sinologyCurrentPage = ref(1)
const sinologyPageSize = 12

const sinologyVersionOptions = ref<Array<{ value: string; label: string; cnt?: number }>>([])
const sinologyUnitOptions = ref<Array<{ value: string; label: string; cnt?: number }>>([])
const sinologySemesterOptions = ref<Array<{ value: string; label: string; cnt?: number }>>([])
const sinologyGradeSelectOptions = [
  { value: '', label: '全部年级' },
  { value: '一年级', label: '一年级' },
  { value: '二年级', label: '二年级' },
  { value: '三年级', label: '三年级' },
  { value: '四年级', label: '四年级' },
  { value: '五年级', label: '五年级' },
  { value: '六年级', label: '六年级' },
]

const sinologyDialogVisible = ref(false)
const currentSinology = ref<SinologyReadingItem | null>(null)

const sinologyTotalPages = computed(() => Math.ceil(sinologyTotal.value / sinologyPageSize))
const sinologyVisiblePages = computed(() => {
  const total = sinologyTotalPages.value
  const current = sinologyCurrentPage.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  let start = Math.max(1, current - 3)
  let end = Math.min(total, current + 3)
  if (end - start < 6) {
    if (start === 1) end = Math.min(total, start + 6)
    else if (end === total) start = Math.max(1, end - 6)
  }
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

// -- 作文筛选状态 --
const compositionGrade = ref('')
const compositionYear = ref('')
const compositionVersion = ref('')
const compositionRegion = ref('')
const compositionType = ref('')
const compositionSemester = ref('')

const compGradeOptions = computed(() => [
  { value: '', label: '全部年级' },
  ...gradeList.value.map((g) => ({ value: g, label: g })),
])
const compYearOptions = [
  { value: '', label: '全部年份' },
  ...yearList.filter(y => y !== '全部').map(y => ({ value: y, label: y })),
]
const compVersionOptions = [
  { value: '', label: '全部版本' },
  { value: '统编版（2024）', label: '统编版（2024）' },
  { value: '人教版', label: '人教版' },
  { value: '北师大版', label: '北师大版' },
  { value: '苏教版', label: '苏教版' },
]
const compRegionOptions = [
  { value: '', label: '全部地区' },
  { value: '全国', label: '全国' },
  { value: '北京', label: '北京' },
  { value: '上海', label: '上海' },
  { value: '广东', label: '广东' },
  { value: '浙江', label: '浙江' },
  { value: '江苏', label: '江苏' },
  { value: '四川', label: '四川' },
]
const compTypeOptions = [
  { value: '', label: '全部类型' },
  { value: '命题作文', label: '命题作文' },
  { value: '半命题作文', label: '半命题作文' },
  { value: '话题作文', label: '话题作文' },
  { value: '材料作文', label: '材料作文' },
  { value: '看图作文', label: '看图作文' },
]
const compSemesterOptions = [
  { value: '', label: '全部学期' },
  { value: '上学期', label: '上学期' },
  { value: '下学期', label: '下学期' },
]

const sinologyFilterItems = computed(() => [
  {
    key: 'sinology-grade',
    modelValue: sinologyGrade.value,
    placeholder: '年级',
    options: sinologyGradeSelectOptions,
    onUpdate: (value: string) => { sinologyGrade.value = value },
  },
  {
    key: 'sinology-version',
    modelValue: sinologyVersion.value,
    placeholder: '版本',
    options: sinologyVersionOptions.value,
    onUpdate: (value: string) => { sinologyVersion.value = value },
  },
  {
    key: 'sinology-unit',
    modelValue: sinologyUnit.value,
    placeholder: '单元',
    options: sinologyUnitOptions.value,
    onUpdate: (value: string) => { sinologyUnit.value = value },
  },
  {
    key: 'sinology-semester',
    modelValue: sinologySemester.value,
    placeholder: '学期',
    options: sinologySemesterOptions.value,
    onUpdate: (value: string) => { sinologySemester.value = value },
  },
])

const sinologyKeywordConfig = computed(() => ({
  modelValue: sinologyKeyword.value,
  placeholder: '搜索标题/作者/内容…',
  onUpdate: (value: string) => { sinologyKeyword.value = value },
  onEnter: searchSinology,
}))

const compositionFilterItems = computed(() => [
  {
    key: 'composition-grade',
    modelValue: compositionGrade.value,
    placeholder: '年级',
    options: compGradeOptions.value,
    onUpdate: (value: string) => { compositionGrade.value = value },
  },
  {
    key: 'composition-year',
    modelValue: compositionYear.value,
    placeholder: '年份',
    options: compYearOptions,
    onUpdate: (value: string) => { compositionYear.value = value },
  },
  {
    key: 'composition-version',
    modelValue: compositionVersion.value,
    placeholder: '版本',
    options: compVersionOptions,
    onUpdate: (value: string) => { compositionVersion.value = value },
  },
  {
    key: 'composition-region',
    modelValue: compositionRegion.value,
    placeholder: '地区',
    options: compRegionOptions,
    onUpdate: (value: string) => { compositionRegion.value = value },
  },
  {
    key: 'composition-type',
    modelValue: compositionType.value,
    placeholder: '类型',
    options: compTypeOptions,
    onUpdate: (value: string) => { compositionType.value = value },
  },
  {
    key: 'composition-semester',
    modelValue: compositionSemester.value,
    placeholder: '学期',
    options: compSemesterOptions,
    onUpdate: (value: string) => { compositionSemester.value = value },
  },
])

// 作文面板是否激活
const isCompositionPanelActive = computed(
  () => columnLayout.value === 'reading_writing' && activeReadingWritingTab.value === 'composition',
)

// 作文面板 API 查询参数
const compositionBrowseParams = computed<ExamBrowseQuery | null>(() => {
  if (!isCompositionPanelActive.value) return null
  const parts: string[] = []
  const kw = searchKeyword.value?.trim()
  if (kw) parts.push(kw)
  if (compositionRegion.value && compositionRegion.value !== '') parts.push(compositionRegion.value)
  if (compositionYear.value && compositionYear.value !== '') parts.push(compositionYear.value)
  if (compositionSemester.value && compositionSemester.value !== '') parts.push(compositionSemester.value)
  if (compositionGrade.value && compositionGrade.value !== '') parts.push(compositionGrade.value)
  return {
    type: compositionType.value && compositionType.value !== '' ? compositionType.value : undefined,
    keyword: parts.length ? parts.join(' ') : undefined,
    edition: compositionVersion.value && compositionVersion.value !== '' ? compositionVersion.value : undefined,
  }
})

// -- 函数 --
async function loadSinologyFilterEnums() {
  try {
    const res = await sinologyApi.getFilterEnums()
    const data = (res as any)?.data
    if (data) {
      sinologyVersionOptions.value = data.editions || []
      sinologyUnitOptions.value = data.units || []
      sinologySemesterOptions.value = data.semesters || []
    }
  } catch {
    // 使用默认选项
    sinologyVersionOptions.value = [
      { value: '统编版（2024）', label: '统编版（2024）' },
      { value: '人教版', label: '人教版' },
      { value: '北师大版', label: '北师大版' },
    ]
    sinologyUnitOptions.value = [
      { value: '我上学了', label: '我上学了' },
      { value: '识字', label: '识字' },
      { value: '课文', label: '课文' },
    ]
    sinologySemesterOptions.value = [
      { value: '上册', label: '上册' },
      { value: '下册', label: '下册' },
    ]
  }
}

async function searchSinology() {
  sinologyLoading.value = true
  sinologyCurrentPage.value = 1
  try {
    const res = await sinologyApi.search({
      gradeName: sinologyGrade.value || undefined,
      editionName: sinologyVersion.value || undefined,
      unitName: sinologyUnit.value || undefined,
      semesterName: sinologySemester.value || undefined,
      keyword: sinologyKeyword.value || undefined,
    })
    const data = (res as any)?.data
    sinologyList.value = Array.isArray(data) ? data : []
    sinologyTotal.value = sinologyList.value.length
  } catch {
    sinologyList.value = []
    sinologyTotal.value = 0
  } finally {
    sinologyLoading.value = false
  }
}

async function openSinologyDetail(item: SinologyReadingItem) {
  try {
    const res = await sinologyApi.getDetail(item.id)
    const data = (res as any)?.data
    currentSinology.value = data || item
  } catch {
    currentSinology.value = item
  }
  sinologyDialogVisible.value = true
}

function onCompositionFilterChange() {
  apiCurrentPage.value = 1
  fetchApiResources()
}

// 监听国学阅读或作文栏目进入时自动初始化
watch(() => activeColumn.value, (col) => {
  if (readingWritingColumns.includes(col)) {
    activeReadingWritingTab.value = col === '国学阅读' ? 'sinology' : 'composition'
  }
})

// 进入国学阅读子标签时加载筛选枚举
watch(activeReadingWritingTab, (tab) => {
  if (tab === 'sinology' && sinologyVersionOptions.value.length === 0) {
    loadSinologyFilterEnums()
  }
})

function toFilterOptions(list: string[]): FilterRowOption[] {
  return list.map((label) => ({ value: label, label }))
}

const gradeOptions = computed(() => toFilterOptions(gradeList.value))
const semesterOptions = computed(() => toFilterOptions(semesterList))
const yearOptions = computed(() => toFilterOptions(yearList))
const versionOptions = computed(() =>
  toFilterOptions([...versionDisplayList, ...versionMoreList.value]),
)
const regionOptions = computed(() =>
  toFilterOptions([...regionDisplayList.value, ...regionMoreList.value]),
)

const finalReview = useFinalReview({
  route,
  router,
  activeColumn,
  catalogTree,
  activeCatalogNodeId,
  selectedVolumeId,
  selectCatalogNode,
  examSelectedType: selectedType,
  examSelectedRegion: selectedRegion,
  examSelectedVersion: selectedVersion,
  searchKeyword,
  reviewScope,
})

const {
  showCoursewareTabs,
  showExamFilters: showFinalReviewExamFilters,
  reviewPathLabels,
  syncPrepLinkForUnit,
  finalReviewExamParams,
  initReviewScopeFromRoute,
  switchCoursewareScope,
} = finalReview

const effectiveLayoutUsesExamFilters = computed(
  () => layoutUsesExamFilters.value || showFinalReviewExamFilters.value || isCompositionPanelActive.value,
)
const effectiveExamBrowseParams = computed(
  () => finalReviewExamParams.value ?? examBrowseParams.value ?? compositionBrowseParams.value,
)

function onReviewScopeChange(scope: ReviewScope) {
  switchCoursewareScope(scope)
}

const examTypeOptions = computed(() => {
  const column = showFinalReviewExamFilters.value ? '期末' : activeColumn.value
  return toFilterOptions(getExamTypeList(column))
})

const resourceTypes = computed(() => resourceTypeTabsForColumn(activeColumn.value))
const teacherBookTypes = ref([...MORE_TYPE_TABS])

/** 从目录树点选期末复习节点时已定位，勿再自动跳到首个课件叶子 */
const skipFinalReviewAutoLeaf = ref(false)

watch(activeColumn, (col) => {
  const allowedTypes = resourceTypeTabsForColumn(col)
  if (!allowedTypes.includes(activeResourceType.value)) {
    activeResourceType.value = '全部'
  }
  if (col !== FINAL_REVIEW_MODULE || !useCatalogBrowse) return
  if (skipFinalReviewAutoLeaf.value) {
    skipFinalReviewAutoLeaf.value = false
    return
  }
  nextTick(() => {
    const leaf = findFirstCoursewareLeaf(catalogTree.value, selectedVolumeId.value, reviewScope.value)
    if (leaf) selectCatalogNode(leaf.id)
  })
})

function selectResourceType(type: string) {
  activeResourceType.value = type
}

const lessonHub = useLessonHub(
  currentStage,
  currentSubject,
  currentGradeLevelName,
  selectedVersionName,
  activeColumn,
  activeUnit,
  activeResourceType,
  searchKeyword,
  sortType,
  resolveParentUnitName,
  brandCodeParam,
  activeCatalogNodeId,
)

const {
  isLessonHubActive,
  lessonName,
  parentUnitName,
  groupedResources,
  displayTotal: lessonDisplayTotal,
  lessonLoading,
  lessonSuiteHint,
  fetchLessonResources,
} = lessonHub

const apiFetchEnabled = useLessonHubGate(isLessonHubActive)

const browse = useResourceBrowseContext({
  route,
  router,
  currentStage,
  currentSubject,
  selectedVersionKey,
  selectedVolumeId,
  activeColumn,
  activeResourceType,
  activeUnit,
  searchKeyword,
  resourceMode,
  selectedBrandCode,
  activeCatalogNodeId,
  resolveParentUnitName,
  reviewScope,
  getReviewPathLabels,
})

const {
  apiResources,
  apiTotal,
  apiLoading,
  apiCurrentPage,
  apiPageSize,
  apiTotalPages,
  apiVisiblePages,
  fetchApiResources,
  goApiPage,
  setApiPageSize,
  buildApiParams,
  buildStatsParams,
} = useApiResources(
  currentStage,
  currentSubject,
  currentGradeLevelName,
  selectedVersionName,
  activeColumn,
  activeResourceType,
  activeUnit,
  searchKeyword,
  sortType,
  resolveParentUnitName,
  brandCodeParam,
  activeCatalogNodeId,
  apiFetchEnabled,
  effectiveExamBrowseParams,
  effectiveLayoutUsesExamFilters,
  selectedDocType,
  resolveCatalogIncludeSubtree,
)

watch(
  apiResources,
  (v) => {
    apiResourcesStub.value = v
  },
  { deep: true },
)
watch(apiTotal, (v) => {
  apiTotalStub.value = v
})

/** 整册课本类型统计：仅用于 Tab 排序，不随单元/课文变化 */
function buildTextbookStatsParams(): import('@/api/types').PrimaryChineseParams {
  const edition = selectedVersionName.value
    ? normalizeEditionLabel(selectedVersionName.value)
    : undefined
  return composeTextbookStatsParams({
    stage: stageNames[currentStage.value as StageKey] || undefined,
    subject: (currentSubject.value as { name?: string })?.name || undefined,
    module: activeColumn.value || undefined,
    gradeName: currentGradeLevelName.value || undefined,
    edition: edition || undefined,
    brandCode: brandCodeParam.value || undefined,
    catalogTree: catalogTree.value,
  })
}

/** 当前浏览上下文统计：随目录节点/单元/栏目实时更新（不含已选类型筛选） */
function buildContextStatsParams(): import('@/api/types').PrimaryChineseParams {
  return buildStatsParams() as import('@/api/types').PrimaryChineseParams
}

const {
  typeCounts: textbookSortCounts,
  statsReady: sortStatsReady,
  scheduleFetchTypeStats: scheduleTextbookSortStats,
} = useBrowseTypeStats(buildTextbookStatsParams, apiFetchEnabled)

const {
  typeCounts: contextTypeCounts,
  statsReady: contextStatsReady,
  statsLoading: contextStatsLoading,
  scheduleFetchTypeStats: scheduleContextTypeStats,
} = useBrowseTypeStats(buildContextStatsParams, apiFetchEnabled)

/** 目录/筛选 scope 变化时重置「更多」展开态 */
const typeStatsScopeKey = computed(() =>
  JSON.stringify(buildContextStatsParams()),
)

function applySyncPrepCountFallback(raw: Record<string, number>): Record<string, number> {
  if (activeColumn.value !== SYNC_PREP_COLUMN) return raw
  const out = { ...raw }
  for (const tab of resourceTypes.value) {
    const fallback = syncPrepTabCountFallbackKey(tab)
    if (fallback && raw[fallback] != null && out[tab] == null) {
      out[tab] = raw[fallback]
    }
  }
  return out
}

/** 角标：仅绑定当前目录上下文 stats（不用课本级兜底，避免跨节点串数） */
const displayTypeCounts = computed(() => {
  if (resourceMode.value === 'suite') return {}
  const merged = applySyncPrepCountFallback(contextTypeCounts.value)
  if (contextStatsReady.value) {
    warnTypeCountMismatch(merged, 'catalog-context')
  }
  return merged
})

/** 排序：整册课本级数量（固定顺序） */
const displaySortCounts = computed(() =>
  applySyncPrepCountFallback(textbookSortCounts.value),
)

watch(resourceMode, (mode) => {
  if (mode === 'single') {
    nextTick(() => refreshTypeStats())
  }
})

/** stats 就绪后，若当前类型在该目录下为 0 条则回退「全部」 */
watch([contextTypeCounts, contextStatsReady], () => {
  if (!contextStatsReady.value || activeResourceType.value === '全部') return
  const merged = applySyncPrepCountFallback(contextTypeCounts.value)
  const key = activeResourceType.value
  const fb = syncPrepTabCountFallbackKey(key)
  const count = merged[key] ?? (fb ? merged[fb] : undefined) ?? 0
  if (count <= 0) activeResourceType.value = '全部'
})

const moduleCountMap = ref<Record<string, number>>({})

const moreColumnGroupCountMap = computed<Record<string, number>>(() => {
  const out: Record<string, number> = {}
  for (const group of groupedColumns.value) {
    out[group.key] = group.columns.reduce(
      (sum, col) => sum + (moduleCountMap.value[col.name] || 0),
      0,
    )
  }
  return out
})

// 成套资源数据（从真实 API 获取）
const suitePackages = ref<any[]>([])
const suiteCurrentPage = ref(1)
const suitePageSize = ref(3)
const suiteLoading = ref(false)

const listTotalCount = computed(() => {
  if (isLessonHubActive.value && resourceMode.value === 'single') {
    return lessonDisplayTotal.value
  }
  return resourceMode.value === 'single' ? apiTotal.value : suitePackages.value.length
})

// 根据当前筛选条件动态生成成套资源列表
async function fetchSuitePackages() {
  suiteLoading.value = true
  try {
    const nodeId = activeCatalogNodeId.value
    const useNode =
      USE_CATALOG_BROWSE && nodeId != null && nodeId > 0
    const exam = effectiveLayoutUsesExamFilters.value ? effectiveExamBrowseParams.value : null
    const syncPrepQuery =
      !exam && activeColumn.value === SYNC_PREP_COLUMN
        ? mapSyncPrepTypeToQuery(activeResourceType.value)
        : {}
    const params = {
      stage: stageNames[currentStage.value as StageKey] || undefined,
      subject: (currentSubject.value as { name?: string })?.name || undefined,
      gradeName: currentGradeLevelName.value || undefined,
      edition:
        exam?.edition ??
        (layoutUsesExamFilters.value ? undefined : selectedVersionName.value || undefined),
      module: activeColumn.value || undefined,
      type: exam?.type ?? syncPrepQuery.type,
      subType: exam?.subType ?? syncPrepQuery.subType,
      keyword: exam?.keyword,
      brandCode: brandCodeParam.value,
      ...(useNode
        ? {
            catalogNodeId: nodeId,
            includeSubtree: resolveCatalogIncludeSubtree(),
          }
        : { unitName: resolveParentUnitName(activeUnit.value) || undefined }),
    }
    const silent = { silentError: true } as import('@/api/request').SilentRequestConfig
    const { suites } = await resourceGateway.getPrimaryChineseSuites(params, silent)
    suitePackages.value = suites
  } catch (_e) {
    suitePackages.value = []
  } finally {
    suiteLoading.value = false
  }
}

const suitePaginatedSuites = computed(() => {
  const start = (suiteCurrentPage.value - 1) * suitePageSize.value
  return suitePackages.value.slice(start, start + suitePageSize.value)
})

const suiteTotalPages = computed(() =>
  Math.ceil(suitePackages.value.length / suitePageSize.value)
)

const suiteVisiblePages = computed(() => {
  const total = suiteTotalPages.value
  const current = suiteCurrentPage.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  let start = Math.max(1, current - 3)
  let end = Math.min(total, current + 3)
  if (end - start < 6) {
    if (start === 1) end = start + 6
    else if (end === total) start = end - 6
  }
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

// 方法
function handleSearch() {
  // 触发 API 重新加载（watch 已监听 searchKeyword）
}

function showCart() {
  // TODO: 跳转到资料篮页面或打开弹窗
}

const showEmptyUploadAction = computed(
  () =>
    !layoutUsesExamFilters.value &&
    resourceMode.value === 'single' &&
    (useCatalogBrowse
      ? activeCatalogNodeId.value != null && activeCatalogNodeId.value > 0
      : !!activeUnit.value),
)

const emptyUploadLabel = computed(() => {
  const type =
    activeResourceType.value && activeResourceType.value !== '全部'
      ? activeResourceType.value
      : ''
  let nodeName = ''
  if (useCatalogBrowse && activeCatalogNodeId.value) {
    nodeName = findCatalogNodeById(activeCatalogNodeId.value)?.name || activeUnit.value || ''
  } else {
    nodeName = activeUnit.value || ''
  }
  if (nodeName && type) return `上传「${nodeName}」${type}`
  if (nodeName) return `上传「${nodeName}」资源`
  return '上传资源'
})

const emptyPlacementHint = computed(() => {
  if (!useCatalogBrowse || !activeCatalogNodeId.value) return ''
  const { unitName, lessonName } = resolvePlacementForUpload(activeCatalogNodeId.value)
  const module = activeColumn.value || '同步备课'
  const type =
    activeResourceType.value && activeResourceType.value !== '全部'
      ? activeResourceType.value
      : '课件'
  if (lessonName && unitName) {
    return `上传后将发布到「${module}」· ${unitName} · ${lessonName} · ${type}，与当前筛选一致`
  }
  if (unitName) {
    return `上传后将发布到「${module}」· ${unitName} · ${type}，与当前筛选一致`
  }
  return ''
})

function goToUpload() {
  if (!currentSubject.value?.key) {
    router.push('/upload')
    return
  }

  let unit: string | undefined
  let lesson: string | undefined

  if (useCatalogBrowse && activeCatalogNodeId.value && activeCatalogNodeId.value > 0) {
    const placement = resolvePlacementForUpload(activeCatalogNodeId.value)
    unit = placement.unitName || undefined
    lesson = placement.lessonName || undefined
  } else {
    const unitRaw = activeUnit.value
    const parent = resolveParentUnitName(unitRaw)
    const isLesson = !!unitRaw && parent !== unitRaw
    unit = isLesson ? parent : unitRaw || undefined
    lesson = isLesson ? unitRaw : undefined
  }

  const teachingType =
    activeResourceType.value && activeResourceType.value !== '全部'
      ? activeResourceType.value
      : '课件'

  router.push(
    buildUploadLocation({
      brandCode: brandCodeForApi(selectedBrandCode.value),
      catalogNodeId:
        activeCatalogNodeId.value && activeCatalogNodeId.value > 0
          ? activeCatalogNodeId.value
          : undefined,
      stage: currentStage.value,
      subjectKey: currentSubject.value.key,
      versionKey: selectedVersionKey.value,
      module: activeColumn.value || '同步备课',
      teachingType,
      volumeId: selectedVolumeId.value,
      gradeName: currentGradeLevelName.value,
      editionName: selectedVersionName.value,
      unit,
      lesson,
      resourceMode: resourceMode.value,
    }),
  )
}

function viewMoreResources() {
  // TODO: 跳转到单份资源专区
}

const browseSummary = computed(() => {
  const unit = activeUnit.value
  const parent = resolveParentUnitName(unit)
  const unitLabel = unit && parent !== unit ? `${parent} · ${unit}` : unit
  return browse.formatBrowseSummary({
    seriesName: selectedSeriesName.value,
    stageName: currentStageName.value,
    subjectName: currentSubject.value?.name,
    gradeLevelName: currentGradeLevelName.value,
    versionName: selectedVersionName.value,
    column: activeColumn.value,
    resourceType: activeResourceType.value,
    unitLabel,
  })
})

/** 可点击的面包屑数据 */
const breadcrumbItems = computed(() => {
  const unit = activeUnit.value
  const parent = resolveParentUnitName(unit)
  const unitLabel = unit && parent !== unit ? `${parent} · ${unit}` : unit
  const context = {
    stage: currentStage.value,
    subject: currentSubject.value?.key || '',
    version: selectedVersionKey.value,
    brand: selectedBrandCode.value || undefined,
    volume: selectedVolumeId.value || undefined,
    module: activeColumn.value || undefined,
    type: activeResourceType.value !== '全部' ? activeResourceType.value : undefined,
    unit: undefined as string | undefined,
    lesson: undefined as string | undefined,
    mode: resourceMode.value || undefined,
    node: (activeCatalogNodeId.value && activeCatalogNodeId.value > 0)
      ? String(activeCatalogNodeId.value) : undefined,
  }
  if (unit && parent !== unit) {
    context.unit = parent
    context.lesson = unit
  } else if (unit) {
    context.unit = unit
  }
  return buildBreadcrumbItems({
    seriesName: selectedSeriesName.value,
    stageName: currentStageName.value,
    subjectName: currentSubject.value?.name,
    gradeLevelName: currentGradeLevelName.value,
    versionName: selectedVersionName.value,
    column: activeColumn.value,
    resourceType: activeResourceType.value,
    reviewPath: activeColumn.value === '期末复习' ? reviewPathLabels.value : undefined,
    unitLabel,
  }, context)
})

function openResource(item: { id: number }) {
  if (!item?.id) return
  resourceGateway.recordBySource('primary_chinese', item.id, 'view').catch(() => {})
  browse.navigateToDetail(item)
}

/** 已收藏的资源 id 集合 */
const favoriteIds = ref<Set<number>>(new Set())

/** 立即下载 */
function handleDownloadResource(item: any) {
  if (!item?.id) return
  if (item.ossUrl) {
    window.open(item.ossUrl, '_blank')
    ElMessage.success(`开始下载: ${item.title}`)
    resourceGateway.recordBySource('primary_chinese', item.id, 'download').catch(() => {})
  } else {
    ElMessage.warning('暂无下载链接，请联系管理员')
  }
}

/** 加入资料篮 */
async function handleAddToBasket(item: any) {
  if (!item?.id) return
  try {
    const checkRes = await prepApi.checkExists('resource', item.id)
    if (checkRes?.data?.data?.exists) {
      ElMessage.info('该资源已在资料篮中')
      return
    }
    await prepApi.addBasketItem({
      itemType: 'resource',
      refId: item.id,
      title: item.title || '',
      subtitle: item.type || '',
    })
    ElMessage.success(`已加入资料篮: ${item.title}`)
  } catch {
    ElMessage.error('加入资料篮失败，请稍后重试')
  }
}

/** 批量下载 */
function handleBatchDownload(items: any[]) {
  items.forEach((item) => handleDownloadResource(item))
}

/** 批量加入资料篮 */
async function handleBatchAddToBasket(items: any[]) {
  let count = 0
  for (const item of items) {
    try {
      const checkRes = await prepApi.checkExists('resource', item.id)
      if (checkRes?.data?.data?.exists) continue
      await prepApi.addBasketItem({
        itemType: 'resource',
        refId: item.id,
        title: item.title || '',
        subtitle: item.type || '',
      })
      count++
    } catch {
      // 单个失败继续下一个
    }
  }
  ElMessage.success(`已将 ${count} 个资源加入资料篮`)
}

/** 批量收藏 */
function handleBatchFavorite(items: any[]) {
  let count = 0
  for (const item of items) {
    if (item._favorited) continue
    item._favorited = true
    count++
  }
  if (count > 0) {
    ElMessage.success(`已收藏 ${count} 个资源`)
  } else {
    ElMessage.info('所选资源均已收藏')
  }
}

/** 切换收藏状态 */
async function handleToggleFavorite(item: any) {
  if (!item?.id) return
  const isFav = !!item._favorited
  if (isFav) {
    // 取消收藏
    try {
      // await primaryChineseApi.unfavorite?.(item.id) // 若 API 支持
      delete item._favorited
      ElMessage.success(`已取消收藏: ${item.title}`)
    } catch {
      ElMessage.error('操作失败')
    }
  } else {
    try {
      // await primaryChineseApi.favorite?.(item.id) // 若 API 支持
      item._favorited = true
      ElMessage.success(`已收藏: ${item.title}`)
    } catch {
      ElMessage.error('操作失败')
    }
  }
}

function refreshTypeStats() {
  scheduleTextbookSortStats()
  scheduleContextTypeStats()
  fetchModuleStats()
}

function refreshContextTypeStats() {
  scheduleContextTypeStats()
}

async function fetchModuleStats() {
  const params = buildContextStatsParams()
  const reqParams = {
    ...params,
    module: undefined,
  } as Omit<import('@/api/types').PrimaryChineseParams, 'current' | 'size'>
  try {
    const { modules } = await resourceGateway.getPrimaryChineseModuleStats(
      reqParams,
      { silentError: true } as import('@/api/request').SilentRequestConfig,
    )
    const map: Record<string, number> = {}
    for (const item of modules) {
      const name = (item.module || '').trim()
      if (!name) continue
      map[name] = Number(item.count || 0)
    }
    moduleCountMap.value = map
  } catch {
    moduleCountMap.value = {}
  }
}

function onSelectBrand(code: string) {
  selectBrand(code)
  activeUnit.value = ''
  activeCatalogNodeId.value = null
  suiteCurrentPage.value = 1
  apiCurrentPage.value = 1
  browse.syncToUrl()
  refreshPageData()
  refreshTypeStats()
}

function readCatalogDefaultModule(node: { meta?: Record<string, unknown> } | null | undefined): string {
  const raw = node?.meta?.defaultModule
  return typeof raw === 'string' && raw.trim() ? raw.trim() : ''
}

function onCatalogSelect(nodeId: number) {
  selectCatalogNode(nodeId)
  const node = catalogCtx.findNodeById(nodeId)
  if (node) {
    activeUnit.value = node.name
    const defaultModule = readCatalogDefaultModule(node)
    if (defaultModule && defaultModule !== activeColumn.value) {
      skipFinalReviewAutoLeaf.value = true
      activeColumn.value = defaultModule
    }
  }
  activeResourceType.value = '全部'
  suiteCurrentPage.value = 1
  apiCurrentPage.value = 1
  browse.syncToUrl()
  refreshResourceLists()
  nextTick(() => refreshContextTypeStats())
  nextTick(() => scrollCatalogNodeIntoView(nodeId))
}

function onCatalogToggleExpand(nodeId: number) {
  toggleCatalogNodeExpand(nodeId)
}

function onCatalogToggleAll() {
  toggleCatalogAllExpand()
}

function onToggleAllUnits(val: boolean) {
  allUnitsExpanded.value = val
  if (useCatalogBrowse && val !== catalogAllExpanded.value) {
    toggleCatalogAllExpand()
  }
}

/** 目录树节点 hover 预加载：提前填充 LRU 缓存，用户点击时即时展示 */
const catalogPrefetchTimers = new Map<number, ReturnType<typeof setTimeout>>()
function onCatalogPrefetch(nodeId: number) {
  // 防抖：200ms 内同一节点不重复预取
  const existing = catalogPrefetchTimers.get(nodeId)
  if (existing) clearTimeout(existing)
  catalogPrefetchTimers.set(
    nodeId,
    setTimeout(async () => {
      catalogPrefetchTimers.delete(nodeId)
      try {
        const params = buildApiParamsForNode(nodeId)
        const key = `gateway:primary_chinese:${JSON.stringify(params)}`
        // 如果缓存中已有该节点的数据，跳过
        if (requestCache.get(key)) return
        const res = await resourceGateway.listPrimaryChineseResources(params, { silentError: true } as import('@/api/request').SilentRequestConfig)
        requestCache.set(key, res)
      } catch {
        // 静默预取失败
      }
    }, 200),
  )
}

/** 为指定 catalogNodeId 构建 API 参数（用于预取） */
function buildApiParamsForNode(nodeId: number): Record<string, unknown> {
  return {
    stage: stageNames[currentStage.value as StageKey] || undefined,
    subject: (currentSubject.value as { name?: string })?.name || undefined,
    module: activeColumn.value || undefined,
    gradeName: currentGradeLevelName.value || undefined,
    edition: selectedVersionName.value || undefined,
    brandCode: brandCodeParam.value || undefined,
    catalogNodeId: nodeId,
    includeSubtree: resolveCatalogIncludeSubtree(nodeId),
    current: 1,
    size: 10,
    sortField: 'upload_time',
    sortOrder: 'desc',
  }
}

function initFromRoute() {
  initBrandFromRoute()
  if (useCatalogBrowse) {
    initCatalogNodeFromRoute()
  }
  const { subject } = browse.initFromRoute()
  browse.applySubjectFromRoute(currentSubjects.value, subject)
  initReviewScopeFromRoute()
  const kw = route.query.keyword
  if (typeof kw === 'string' && kw.trim()) {
    searchKeyword.value = kw.trim()
  }
  if (useCatalogBrowse && activeCatalogNodeId.value) {
    const node = catalogCtx.findNodeById(activeCatalogNodeId.value)
    if (node) activeUnit.value = node.name
  }
}

function onVolumeIdUpdate(volumeId: string) {
  selectedVolumeId.value = volumeId
}

/** 弹窗确认版本/册别后：刷新目录与资源（避免仅改 URL 时目录不更新） */
function onConfirmVersion(versionKey: string, isNew: boolean, volumeId: string) {
  handleConfirmVersion(versionKey, isNew, volumeId)
  activeUnit.value = ''
  suiteCurrentPage.value = 1
  apiCurrentPage.value = 1
  initUnitList()
  refreshResourceLists()
}

/** 仅当尚未选中册别时设默认（一年级上册），避免覆盖用户已选册别 */
function ensureDefaultVolume() {
  if (!selectedVolumeId.value) {
    setDefaultVolumeByStage()
  }
}

async function refreshPageData() {
  if (useCatalogBrowse) {
    await catalogCtx.loadSchemes()
    await loadCatalogTree()
    if (catalogCtx.activeNode.value) {
      activeUnit.value = catalogCtx.activeNode.value.name
      nextTick(() => scrollCatalogNodeIntoView(activeCatalogNodeId.value!))
    }
  } else {
    initUnitList()
  }
  suiteCurrentPage.value = 1
  apiCurrentPage.value = 1
  refreshResourceLists()
  refreshTypeStats()
}

/** 仅刷新右侧资源列表，不重置目录（避免切换单元时页面跳动） */
function refreshResourceLists() {
  if (isLessonHubActive.value) {
    fetchLessonResources()
  } else {
    fetchApiResources()
  }
  fetchSuitePackages()
}

/** 扩展筛选年级/学期 → 同步册别，驱动 API gradeName */
watch(
  () => [selectedGrade.value, selectedSemester.value, layoutUsesExamFilters.value] as const,
  ([grade, semester, usesExamFilters]) => {
    if (!usesExamFilters || !grade) return
    const volumeId = resolveVolumeIdByGradeSemester(
      currentStage.value,
      grade,
      semester || '全部',
    )
    if (!volumeId || volumeId === selectedVolumeId.value) return
    selectedVolumeId.value = volumeId
    suiteCurrentPage.value = 1
    apiCurrentPage.value = 1
    refreshResourceLists()
  },
)

// 监听关键参数变化，自动刷新成套资源
watch(
  () => [
    currentGradeLevelName.value,
    selectedVersionName.value,
    activeColumn.value,
    currentSubject.value?.key,
    brandCodeParam.value,
    selectedType.value,
    selectedRegion.value,
    selectedVersion.value,
    selectedGrade.value,
    selectedSemester.value,
    examBrowseParams.value,
  ],
  () => {
    suiteCurrentPage.value = 1
    fetchSuitePackages()
  },
  { deep: true }
)

watch(
  () => [route.params.stage, route.params.subject, route.params.version] as const,
  () => {
    initFromRoute()
    browse.applySubjectFromRoute(currentSubjects.value)
    ensureDefaultVolume()
    refreshPageData()
  },
)

/** 浏览器前进/后退：恢复筛选；程序内 URL 同步不重复刷列表（避免闪烁） */
watch(
  () => route.query,
  (q, oldQ) => {
    const fromSync = consumeUrlSyncFlag()
    const curVol = typeof q.volume === 'string' ? q.volume : ''
    const oldVol =
      typeof (oldQ as Record<string, unknown>)?.volume === 'string'
        ? String((oldQ as Record<string, string>).volume)
        : ''

    if (fromSync) {
      if (curVol && curVol !== oldVol) {
        activeUnit.value = ''
        activeCatalogNodeId.value = null
        suiteCurrentPage.value = 1
        apiCurrentPage.value = 1
        refreshPageData()
      }
      return
    }

    initFromRoute()
    if (useCatalogBrowse) {
      void loadCatalogTree().then(() => {
        if (activeCatalogNodeId.value) {
          const node = findCatalogNodeById(activeCatalogNodeId.value)
          if (node) activeUnit.value = node.name
          nextTick(() => scrollCatalogNodeIntoView(activeCatalogNodeId.value!))
        }
        suiteCurrentPage.value = 1
        apiCurrentPage.value = 1
        refreshResourceLists()
      })
      return
    }
    suiteCurrentPage.value = 1
    apiCurrentPage.value = 1
    refreshResourceLists()
  },
)

const disposeUrlSync = browse.setupUrlSync(() => [
  currentStage.value,
  currentSubject.value?.key,
  selectedVersionKey.value,
  selectedVolumeId.value,
  selectedBrandCode.value,
  activeCatalogNodeId.value,
  activeColumn.value,
  activeResourceType.value,
  searchKeyword.value,
  activeUnit.value,
  resourceMode.value,
  reviewScope.value,
])

// 同步恢复路由状态，避免首屏 currentSubject 为空导致页面无内容
initFromRoute()
browse.applySubjectFromRoute(currentSubjects.value)
ensureDefaultVolume()

onMounted(async () => {
  const ok = await ensureSubjectRouteValid(route)
  if (!ok) return
  refreshPageData()
})

function stopUrlSync() {
  disposeUrlSync()
  browse.disposeSync()
}

onBeforeRouteLeave(() => {
  stopUrlSync()
})

onUnmounted(() => {
  stopUrlSync()
})
</script>

<style scoped>
.platform-page {
  min-height: 100vh;
  background: #F5F7FA;
}

.page-body {
  max-width: 1440px;
  margin: 0 auto;
  padding: var(--space-lg, 24px);
}

/* 顶部：当前筛选 + 系列 + 学科栏目 合并为一块 */
.browse-filter-panel {
  --browse-panel-divider: #eef2f6;
  background: #fff;
  border-radius: var(--radius-md, 10px);
  border: 1px solid #eef2f6;
  box-shadow: var(--shadow-card, 0 1px 4px rgba(0, 0, 0, 0.04));
  overflow: hidden;
  margin-bottom: 16px;
}

.browse-filter-panel :deep(.browse-breadcrumb) {
  margin-bottom: 0;
  border: none;
  border-radius: 0;
  box-shadow: none;
  border-bottom: none;
  padding: 10px 24px;
  position: relative;
}

.browse-filter-panel :deep(.series-filter-bar) {
  margin-bottom: 0;
  border-radius: 0;
  box-shadow: none;
  border-bottom: none;
  position: relative;
}

.browse-filter-panel :deep(.series-filter-bar .filter-option-row) {
  padding-bottom: 4px;
  border-bottom: none;
}

.browse-filter-panel :deep(.filter-bar-container) {
  border-radius: 0;
  box-shadow: none;
  padding-top: 2px;
  padding-bottom: 8px;
  border-bottom: none;
}

.browse-filter-panel :deep(.filter-bar-container > .filter-option-row:first-child) {
  padding-top: 4px;
}

.browse-filter-panel :deep(.filter-bar-container .filter-row) {
  border-bottom: none;
}

.browse-filter-panel.has-extended-filters :deep(.filter-bar-container) {
  border-bottom: none;
  padding-bottom: 14px;
}

.browse-filter-panel .extended-filters {
  padding: 0 24px 12px;
  position: relative;
}

.extended-filter-select-row {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
  padding-top: 12px;
}

.extended-filter-select-item {
  display: flex;
  align-items: center;
  min-width: 0;
}

.extended-filter-select-label {
  flex-shrink: 0;
  width: 44px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary, #8E8EA0);
  white-space: nowrap;
}

.extended-filter-select-item :deep(.el-select) {
  flex: 1;
  min-width: 0;
}

.extended-filter-select-item :deep(.el-select__wrapper) {
  min-height: 34px;
}

.browse-filter-panel .extended-filters::before {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 1px;
  background: var(--browse-panel-divider);
}

.browse-filter-panel .extended-filters :deep(.filter-option-row) {
  border-bottom: none;
  position: relative;
}

.browse-filter-panel .extended-filters :deep(.filter-option-row:not(:last-child))::after,
.browse-filter-panel .extended-filters .filter-row-dual:not(:last-child)::after,
.browse-filter-panel .extended-filters .filter-row-actions:not(:last-child)::after {
  content: '';
  position: absolute;
  left: -24px;
  right: -24px;
  bottom: 0;
  height: 1px;
  background: var(--browse-panel-divider);
}

.browse-filter-panel .extended-filters .filter-row-dual {
  position: relative;
  border-bottom: none;
}

.browse-filter-panel .extended-filters .filter-row-actions {
  position: relative;
}

.filter-row-dual {
  display: flex;
  gap: 20px;
  border-bottom: none;
  position: relative;
}

.filter-row-dual :deep(.filter-option-row) {
  flex: 1;
  min-width: 0;
  border-bottom: none;
}

.filter-row-actions {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 0 4px;
}

.filter-row-actions :deep(.filter-option-row) {
  flex: 1;
  min-width: 0;
  padding: 0;
  border-bottom: none;
}

@media (max-width: 1600px) {
  .extended-filter-select-row {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .extended-filter-select-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

/* 全宽统一分割线（贯通面板左右，避免各行 border 错位拼接） */
.browse-filter-panel :deep(.browse-breadcrumb)::after,
.browse-filter-panel :deep(.series-filter-bar)::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 1px;
  background: var(--browse-panel-divider);
}

.browse-filter-panel :deep(.filter-bar-container > .filter-option-row:not(:last-child))::after {
  content: '';
  position: absolute;
  left: -24px;
  right: -24px;
  bottom: 0;
  height: 1px;
  background: var(--browse-panel-divider);
}

.browse-filter-panel :deep(.filter-bar-container > .filter-option-row) {
  border-bottom: none;
  position: relative;
}

.browse-filter-panel :deep(.series-filter-bar .filter-option-row),
.browse-filter-panel :deep(.filter-bar-container .filter-option-row) {
  border-bottom: none;
}

.lesson-hub-panel,
.resource-list-area :deep(.single-resource-section) {
  min-height: 120px;
}

.content-two-columns {
  display: flex;
  gap: 16px;
  margin-top: 16px;
  align-items: stretch;
}

.content-two-columns.layout-default :deep(.course-catalog),
.content-two-columns.layout-category_list :deep(.course-catalog) {
  flex: 0 0 280px;
  width: 280px;
}

.content-two-columns.layout-exam,
.content-two-columns.layout-topic,
.content-two-columns.layout-reading_writing {
  display: block;
  margin-top: 0;
}

.resource-list-area {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: var(--bg-card, #fff);
  border-radius: var(--radius-md, 10px);
  border: 1px solid #eef2f6;
  box-shadow: var(--shadow-card, 0 1px 4px rgba(0, 0, 0, 0.04));
  /* 类型栏「更多」展开需增高，不可 hidden 裁切 */
  overflow: visible;
}

.resource-list-area :deep(.resource-type-bar) {
  position: relative;
  z-index: 2;
  overflow: visible;
}

.resource-list-scroll {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: auto;
  border-radius: 0 0 12px 12px;
}

/* 考试/专题二级筛选行（在 browse-filter-panel 内） */
.filter-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  padding: 14px 0;
  border-bottom: 1px solid #F0EEEB;
}
.filter-row:last-child { border-bottom: none; }
.filter-row.version-more-row { border-bottom: none; padding-top: 0; }

.filter-label {
  font-size: 14px;
  color: #666;
  font-weight: 600;
  white-space: nowrap;
  min-width: 52px;
  letter-spacing: 0.3px;
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  flex: 1;
}

.grade-tag {
  padding: 5px 18px;
  border-radius: 18px;
  font-size: 13px;
  color: #555;
  cursor: pointer;
  background: #F5F7FA;
  white-space: nowrap;
  user-select: none;
  transition: all 0.2s ease;
}
.grade-tag:hover { background: #E8ECF1; color: #333; }
.grade-tag.active-grade {
  background: linear-gradient(135deg, var(--color-primary, #4361EE), var(--color-primary-dark, #3250D3));
  color: #fff;
  box-shadow: 0 2px 8px rgba(67, 97, 238, 0.35);
}
.version-more-trigger {
  color: #999 !important;
  background: transparent !important;
  padding: 5px 12px !important;
  border-radius: 18px !important;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
}
.version-more-trigger:hover { background: #f0f0f0 !important; }

.version-more-row { padding-top: 6px; }

/* 考试布局工具栏（找单份/找成套/综合/最新） */
.exam-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.toolbar-group {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.filter-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 16px;
  border-radius: 18px;
  border: 1px solid #E8E3DC;
  background: #FAFAF8;
  color: #6B5E4E;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
  white-space: nowrap;
  outline: none;
  line-height: 1.4;
}
.filter-chip:hover {
  border-color: var(--color-primary, #4361EE);
  color: var(--color-primary, #4361EE);
  background: #F5F7FF;
}
.filter-chip:active {
  transform: scale(0.97);
}
.filter-chip.active {
  background: var(--color-primary, #4361EE);
  color: #fff;
  border-color: var(--color-primary, #4361EE);
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(67, 97, 238, 0.30);
}
.result-count {
  margin-left: auto;
  font-size: 13px;
  color: #B0A898;
}

/* 空状态 */
.exam-empty-state {
  padding: 48px 0;
  text-align: center;
  color: #909399;
  font-size: 14px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
  margin-bottom: 12px;
}

.topic-skeleton-list {
  padding: 8px 0;
}

.topic-skeleton-row {
  height: 56px;
  margin: 0 0 10px;
  border-radius: 8px;
  background: linear-gradient(90deg, #f5f5f5 25%, #ebebeb 50%, #f5f5f5 75%);
  background-size: 200% 100%;
  animation: topic-shimmer 1.2s ease-in-out infinite;
}

.exam-skeleton .topic-skeleton-row {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

@keyframes topic-shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 资源列表 */
.exam-resource-list {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
  overflow: hidden;
}
.exam-resource-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 20px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.15s;
}
.exam-resource-item:last-child { border-bottom: none; }
.exam-resource-item:hover { background: #fafbfc; }
.exam-resource-info { flex: 1; min-width: 0; }
.exam-resource-title { font-size: 15px; font-weight: 500; color: #333; line-height: 1.4; }
.exam-resource-meta {
  display: flex; gap: 4px;
  font-size: 12px; color: #999;
  margin-top: 4px; flex-wrap: wrap;
}

/* 文档图标 */
.doc-icon-box {
  display: inline-flex; align-items: center; justify-content: center;
  width: 32px; height: 32px; border-radius: 6px; font-size: 10px; font-weight: 700; flex-shrink: 0;
}
.doc-icon-box.icon-pdf-box { background: #C0392B; color: #fff; }
.doc-icon-box.icon-ppt-box { background: #D35400; color: #fff; }
.doc-icon-box.icon-word-box { background: #2B5797; color: #fff; }

/* 版本更多展开动画 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* ===== 专题布局 ===== */
.topic-main-area {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.topic-card-grid {
  flex: 1;
  min-width: 0;
}

.topic-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  margin-bottom: 12px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  flex-wrap: wrap;
}



/* ===== 专题资源列表（匹配目标截图的列表样式）===== */
.topic-resource-list {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
  overflow: hidden;
}
.topic-resource-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.15s;
}
.topic-resource-item:last-child { border-bottom: none; }
.topic-resource-item:hover { background: #fafbfc; }
.topic-resource-info { flex: 1; min-width: 0; }
.topic-resource-title {
  font-size: 15px; font-weight: 500; color: #333; line-height: 1.45;
  margin-bottom: 5px; display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.topic-resource-meta {
  display: flex; gap: 4px;
  font-size: 12px; color: #999;
  flex-wrap: wrap;
}
.topic-resource-album {
  margin-top: 4px; font-size: 12px; color: #888;
}
.album-link {
  color: var(--color-primary, #4361EE); cursor: pointer;
}
.album-link:hover { text-decoration: underline; }

/* 卡片网格（保留供其他场景使用） */
.topic-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.topic-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #f0f0f0;
}
.topic-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.1);
}

/* 卡片封面 */
.topic-card-cover {
  height: 100px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 10px 14px;
  position: relative;
}
.cover-subject-tag {
  display: inline-block;
  padding: 3px 10px;
  background: linear-gradient(135deg, var(--color-primary, #4361EE), var(--color-primary-dark, #3250D3));
  color: #fff;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}
.cover-title-row {
  display: flex; align-items: flex-end;
  height: 100%; padding-bottom: 2px;
}
.cover-type-text {
  color: #E65100;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.3;
  text-align: right;
  max-width: 80px;
}

/* 卡片内容区 */
.topic-card-body {
  padding: 12px 14px 14px;
}
.card-title-row {
  display: flex; align-items: flex-start; gap: 6px;
  margin-bottom: 8px;
}
.featured-badge {
  display: inline-flex; align-items: center; justify-content: center;
  width: 18px; height: 18px;
  background: #FF6B00; color: #fff;
  border-radius: 3px;
  font-size: 11px; font-weight: 700;
  flex-shrink: 0; margin-top: 2px;
}
.card-title {
  font-size: 14px; font-weight: 500; color: #333;
  line-height: 1.45; margin: 0;
  line-clamp: 2;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-info-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 4px 16px;
  margin-bottom: 8px;
}
.info-cell {
  font-size: 12px; color: #888;
}
.info-cell em { color: #999; font-style: normal; }
.card-footer-meta {
  display: flex; justify-content: space-between;
  font-size: 11px; color: #bbb;
  padding-top: 8px;
  border-top: 1px solid #f5f5f5;
}

/* 右侧精选专题推荐侧栏 */
.topic-sidebar {
  width: 280px; flex-shrink: 0;
  background: #FFFBF0;
  border-radius: 10px;
  padding: 16px;
}
.sidebar-section { margin-bottom: 18px; }
.sidebar-section:last-child { margin-bottom: 0; }

.sidebar-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12px;
}
.sidebar-header h4 {
  font-size: 16px; color: #E65100; margin: 0; font-weight: 700;
}
.sidebar-more {
  font-size: 12px; color: #999; cursor: pointer;
}

/* 精品成套资料推荐 */
.suite-recommend-item {
  display: flex; gap: 10px; cursor: pointer;
  padding: 10px; border-radius: 8px;
  background: rgba(255,255,255,0.7);
  transition: background 0.15s; align-items: flex-start;
  margin-bottom: 10px;
}
.suite-recommend-item:last-child { margin-bottom: 0; }
.suite-recommend-item:hover { background: #fff; }
.recommend-badge {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 20px; height: 20px;
  background: linear-gradient(135deg,#FFB74D,#FF9800);
  color: #fff; border-radius: 50%;
  font-size: 11px; font-weight: 700; flex-shrink: 0;
}
.recommend-content { flex: 1; min-width: 0; }
.recommend-title {
  font-size: 13px; color: #333; line-height: 1.4;
  margin: 0 0 3px; display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden;
}
.recommend-count {
  font-size: 12px; color: #E65100; font-weight: 600;
}
.sidebar-list {
  display: flex; flex-direction: column; gap: 10px;
  margin-bottom: 14px;
}
.sidebar-item {
  display: flex; gap: 10px; cursor: pointer;
  padding: 6px; border-radius: 6px;
  transition: background 0.15s;
}
.sidebar-item:hover { background: rgba(255,255,255,0.7); }
.sidebar-cover {
  width: 44px; height: 52px; border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.sidebar-content { flex: 1; min-width: 0; }
.sidebar-item-title {
  font-size: 13px; color: #333; line-height: 1.35;
  margin: 0 0 3px; display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden;
}
.sidebar-date { font-size: 11px; color: #bbb; }

.sidebar-links {
  display: flex; flex-direction: column; gap: 6px;
  padding-top: 10px;
  border-top: 1px dashed #EEE5C4;
}
.sidebar-link {
  display: flex; align-items: center; gap: 6px;
  font-size: 12px; color: #888;
  text-decoration: none;
  line-height: 1.5;
  white-space: nowrap;
  overflow: hidden; text-overflow: ellipsis;
}
.sidebar-link:hover { color: var(--color-primary, #4361EE); }
.link-dot {
  width: 4px; height: 4px; border-radius: 50%;
  background: #ccc; flex-shrink: 0;
}

@media (max-width: 1100px) {
  .topic-cards { grid-template-columns: 1fr; }
  .topic-sidebar { display: none; }
}

/* ===== 资源详情弹窗样式 ===== */
.resource-detail-content { padding: 8px 0; }
.resource-detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px 24px;
}
.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}
.detail-label {
  color: #909399;
  min-width: 36px;
  flex-shrink: 0;
}
.detail-value {
  color: #303133;
  font-weight: 500;
}
.format-badge {
  background: #f0f9eb;
  color: #67c23a;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.stat-blue { color: var(--color-primary, #4361EE); }
.stat-gray { color: #909399; }
.detail-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* ===== 国学阅读·作文深度融合 ===== */
.reading-writing-container {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  overflow: hidden;
}

/* 子标签栏 */
.reading-writing-tabs {
  display: flex;
  gap: 0;
  border-bottom: 1px solid #EEF2F6;
  background: #FAFBFC;
  padding: 0 20px;
}
.rw-tab {
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 500;
  color: #8B96A5;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: -1px;
  outline: none;
}
.rw-tab:hover {
  color: var(--color-primary, #4361EE);
}
.rw-tab.active {
  color: var(--color-primary, #4361EE);
  border-bottom-color: var(--color-primary, #4361EE);
  font-weight: 600;
}

/* 国学阅读筛选栏 */
.sinology-filter-bar {
  padding-bottom: 12px;
}

/* 国学阅读卡片网格 */
.sinology-card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding: 0 20px 20px;
  min-height: 200px;
}
.sinology-card {
  background: #fff;
  border: 1px solid #EEF2F6;
  border-radius: 10px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
}
.sinology-card:hover {
  border-color: var(--color-primary, #4361EE);
  box-shadow: 0 4px 16px rgba(67, 97, 238, 0.12);
  transform: translateY(-2px);
}
.sinology-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.sinology-genre-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  color: #E65100;
  background: #FFF3E0;
}
.sinology-genre-badge.large {
  font-size: 13px;
  padding: 3px 12px;
}
.sinology-dynasty {
  font-size: 12px;
  color: #8B96A5;
}
.sinology-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0 0 8px;
  line-height: 1.45;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.sinology-card-excerpt {
  font-size: 13px;
  color: #8B96A5;
  line-height: 1.55;
  margin: 0 0 12px;
  flex: 1;
}
.sinology-card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #B0A898;
  padding-top: 10px;
  border-top: 1px solid #F5F5F5;
}
.difficulty-tag {
  color: #F59E0B;
  font-size: 12px;
  letter-spacing: 1px;
}

/* 作文筛选栏 */
.composition-filter-bar {
  padding-bottom: 12px;
}

/* 详情弹窗 */
.sinology-detail {
  padding: 4px 0;
}
.sinology-detail-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid #EEF2F6;
}
.sinology-detail-meta {
  font-size: 13px;
  color: #8B96A5;
}
.sinology-detail-section {
  margin-bottom: 18px;
}
.sinology-detail-section h5 {
  font-size: 14px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0 0 8px;
}
.sinology-detail-section p {
  font-size: 14px;
  color: #555;
  line-height: 1.75;
  margin: 0;
  white-space: pre-wrap;
}
.key-phrases-text {
  color: var(--color-primary, #4361EE) !important;
}

@media (max-width: 1100px) {
  .sinology-card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 680px) {
  .sinology-card-grid {
    grid-template-columns: 1fr;
  }
}
</style>
