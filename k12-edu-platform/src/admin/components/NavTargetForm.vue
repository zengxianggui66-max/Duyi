<template>
  <div class="nav-target-form">
    <el-form-item label="跳转类型" required>
      <el-select v-model="model.type" style="width: 100%" @change="onTypeChange">
        <el-option label="学科浏览 browse" value="browse" />
        <el-option label="范围内搜索 search" value="search" />
        <el-option label="站内路由 route" value="route" />
        <el-option label="外链 external" value="external" />
        <el-option label="首页锚点 scroll" value="scroll" />
        <el-option label="会员页 vip" value="vip" />
      </el-select>
    </el-form-item>

    <template v-if="model.type === 'route'">
      <el-form-item label="路由路径" required>
        <el-input v-model="model.routePath" placeholder="/lesson" />
      </el-form-item>
    </template>

    <template v-else-if="model.type === 'external'">
      <el-form-item label="外链 URL" required>
        <el-input v-model="model.externalUrl" placeholder="https://..." />
      </el-form-item>
      <el-form-item label="新窗口打开">
        <el-switch v-model="model.openInNewTab" />
      </el-form-item>
    </template>

    <template v-else-if="model.type === 'scroll'">
      <el-form-item label="锚点 ID" required>
        <el-input v-model="model.scrollTarget" placeholder="exam-module" />
      </el-form-item>
    </template>

    <template v-else-if="model.type === 'browse' || model.type === 'search'">
      <el-form-item label="学段">
        <el-select v-model="model.stageKey" style="width: 100%">
          <el-option v-for="s in stageOptions" :key="s.key" :label="s.name" :value="s.key" />
        </el-select>
      </el-form-item>
      <el-form-item label="学科 key">
        <el-input v-model="model.subjectKey" placeholder="chinese" />
      </el-form-item>
      <el-form-item label="版本 key">
        <el-input v-model="model.versionKey" placeholder="tongbian2024" />
      </el-form-item>
      <el-form-item label="册别名">
        <el-input v-model="model.volumeName" placeholder="一年级上册" />
      </el-form-item>
      <el-form-item v-if="model.type === 'search'" label="搜索词" required>
        <el-input v-model="model.keyword" placeholder="教案" />
      </el-form-item>
      <el-form-item label="栏目 module">
        <el-input v-model="queryModule" placeholder="同步备课" />
      </el-form-item>
      <el-form-item v-if="model.type === 'search'" label="搜索引擎">
        <el-select v-model="model.searchEngine" style="width: 100%">
          <el-option label="MySQL（默认）" value="mysql" />
          <el-option label="auto（预留）" value="auto" />
        </el-select>
      </el-form-item>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { stages } from '@/config/subjectConfig'
import type { NavTarget, NavTargetType } from '@/types/homeOps'

const model = defineModel<NavTarget>({ required: true })

const stageOptions = stages

const queryModule = computed({
  get: () => model.value.query?.module ?? '',
  set: (val: string) => {
    const nextQuery = { ...(model.value.query ?? {}) }
    if (val) {
      nextQuery.module = val
    } else {
      delete nextQuery.module
    }
    model.value = {
      ...model.value,
      query: Object.keys(nextQuery).length ? nextQuery : undefined,
    }
  },
})

function defaultNavTarget(type: NavTargetType): NavTarget {
  switch (type) {
    case 'browse':
      return {
        type,
        stageKey: 'primary',
        subjectKey: 'chinese',
        versionKey: 'tongbian2024',
        query: { module: '同步备课' },
      }
    case 'search':
      return {
        type,
        stageKey: 'primary',
        subjectKey: 'chinese',
        versionKey: 'tongbian2024',
        keyword: '',
        searchEngine: 'mysql',
        query: { module: '同步备课' },
      }
    case 'route':
      return { type, routePath: '/' }
    case 'external':
      return { type, externalUrl: '', openInNewTab: true }
    case 'scroll':
      return { type, scrollTarget: 'exam-module' }
    case 'vip':
      return { type: 'vip' }
    default:
      return { type: 'route', routePath: '/' }
  }
}

function onTypeChange(type: NavTargetType) {
  model.value = defaultNavTarget(type)
}

watch(
  () => model.value.type,
  (type) => {
    if (!type) model.value = defaultNavTarget('route')
  },
  { immediate: true },
)
</script>

<style scoped>
.nav-target-form :deep(.el-form-item) {
  margin-bottom: 12px;
}
</style>
