<template>
  <div class="prep-school-list">
    <motion.div class="filter-bar">
      <el-select v-model="filter.gradeStage" placeholder="学段" clearable style="width: 100px" @change="load">
        <el-option label="小学" value="primary" />
        <el-option label="初中" value="junior" />
        <el-option label="高中" value="senior" />
      </el-select>
      <el-input
        v-model="filter.keyword"
        placeholder="搜索名校试卷"
        clearable
        style="flex: 1; max-width: 220px"
        @keyup.enter="load"
      />
      <el-button type="primary" @click="load">搜索</el-button>
    </motion.div>
    <div v-loading="loading" class="paper-grid">
      <article v-for="r in papers" :key="r.id" class="paper-card">
        <h4>{{ r.title }}</h4>
        <p class="meta">{{ r.region }} · {{ r.gradeStage }} · {{ r.resourceForm }}</p>
        <div class="actions">
          <el-button type="primary" plain size="small" @click="addPaper(r)">整卷入篮</el-button>
          <router-link :to="`/topic-zone/resource/${r.id}`">
            <el-button link size="small">详情</el-button>
          </router-link>
        </div>
      </article>
      <el-empty v-if="!loading && !papers.length" description="暂无套卷" />
    </div>
    <el-pagination
      v-if="total > filter.size!"
      background
      layout="prev, pager, next"
      :total="total"
      :page-size="filter.size"
      :current-page="filter.current"
      class="pager"
      @current-change="onPage"
    />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { motion } from 'motion-v'
import { topicApi, type TopicResourceItem } from '@/api/topic'
import { usePrepBasketStore } from '@/store/prepBasket'

const prepBasket = usePrepBasketStore()
const loading = ref(false)
const papers = ref<TopicResourceItem[]>([])
const total = ref(0)
const filter = reactive({
  gradeStage: 'junior',
  resourceForm: 'exam',
  keyword: '',
  current: 1,
  size: 8,
})

async function load() {
  loading.value = true
  try {
    const res = await topicApi.listResources({ ...filter })
    papers.value = res.data.data?.records || []
    total.value = res.data.data?.total || 0
  } finally {
    loading.value = false
  }
}

function onPage(p: number) {
  filter.current = p
  load()
}

async function addPaper(r: TopicResourceItem) {
  await prepBasket.addItem({
    itemType: 'paper',
    refId: r.id,
    title: r.title,
    subtitle: `${r.region || ''} · ${r.schoolYear || ''} · 试卷`,
    coverUrl: r.coverUrl,
    metaJson: JSON.stringify({ resourceForm: r.resourceForm, fileUrl: r.fileUrl }),
  })
}

onMounted(load)
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.paper-grid {
  display: grid;
  gap: 12px;
}
.paper-card {
  padding: 14px;
  border: 1px solid var(--border-light);
  border-radius: 10px;
  background: var(--bg-page, #fafafa);
}
.paper-card h4 {
  margin: 0 0 8px;
  font-size: 15px;
}
.meta {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0 0 10px;
}
.actions {
  display: flex;
  gap: 8px;
  align-items: center;
}
.pager {
  margin-top: 16px;
  justify-content: center;
}
</style>
