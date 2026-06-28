<template>
  <div class="prep-album-list">
    <motion.div class="filter-bar">
      <el-select v-model="filter.gradeStage" placeholder="学段" clearable style="width: 100px" @change="load">
        <el-option label="小学" value="primary" />
        <el-option label="初中" value="junior" />
        <el-option label="高中" value="senior" />
      </el-select>
      <el-input
        v-model="filter.keyword"
        placeholder="搜索精品专辑"
        clearable
        style="flex: 1; max-width: 220px"
        @keyup.enter="load"
      />
      <el-button type="primary" @click="load">搜索</el-button>
    </motion.div>
    <motion.div v-loading="loading" class="album-grid">
      <article v-for="a in albums" :key="a.id" class="album-card">
        <span class="icon">{{ a.icon || '💎' }}</span>
        <div class="info">
          <h4>{{ a.title }}</h4>
          <p>{{ a.resourceCount || 0 }} 个资源 · {{ a.region }}</p>
        </div>
        <el-button type="primary" plain size="small" :loading="addingId === a.id" @click="addAlbum(a)">
          专辑入篮
        </el-button>
      </article>
      <el-empty v-if="!loading && !albums.length" description="暂无专辑" />
    </motion.div>
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
import { ElMessage } from 'element-plus'
import { topicApi, type TopicAlbumItem } from '@/api/topic'
import { usePrepBasketStore } from '@/store/prepBasket'

const prepBasket = usePrepBasketStore()
const loading = ref(false)
const addingId = ref<number | null>(null)
const albums = ref<TopicAlbumItem[]>([])
const total = ref(0)
const filter = reactive({
  gradeStage: 'junior',
  keyword: '',
  current: 1,
  size: 8,
})

async function load() {
  loading.value = true
  try {
    const res = await topicApi.listAlbums({ ...filter })
    albums.value = res.data.data?.records || []
    total.value = res.data.data?.total || 0
  } finally {
    loading.value = false
  }
}

function onPage(p: number) {
  filter.current = p
  load()
}

async function addAlbum(a: TopicAlbumItem) {
  addingId.value = a.id
  try {
    await prepBasket.addItem({
      itemType: 'album',
      refId: a.id,
      title: a.title,
      subtitle: `专辑 · ${a.resourceCount || 0} 个资源`,
      metaJson: JSON.stringify({ category: a.category, region: a.region }),
    })
    const detail = await topicApi.getAlbum(a.id)
    const resources = detail.data.data?.resources || []
    for (const r of resources.slice(0, 20)) {
      const exists = prepBasket.items.some(
        (i) => i.itemType === 'resource' && i.refId === r.id
      )
      if (exists) continue
      await prepBasket.addItem({
        itemType: 'resource',
        refId: r.id,
        title: r.title,
        subtitle: `来自专辑「${a.title}」`,
        coverUrl: r.coverUrl,
      })
    }
    ElMessage.success(`专辑已入篮，并加入 ${Math.min(resources.length, 20)} 项资源`)
  } finally {
    addingId.value = null
  }
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
.album-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.album-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border: 1px solid var(--border-light);
  border-radius: 10px;
}
.icon {
  font-size: 28px;
}
.info {
  flex: 1;
  min-width: 0;
}
.info h4 {
  margin: 0 0 4px;
  font-size: 15px;
}
.info p {
  margin: 0;
  font-size: 12px;
  color: var(--text-secondary);
}
.pager {
  margin-top: 16px;
  justify-content: center;
}
</style>
