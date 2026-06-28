<template>
  <aside class="prep-basket-panel card">
    <header class="panel-head">
      <h3>📋 资料篮</h3>
      <span class="count-badge">{{ prepBasket.totalCount }}</span>
    </header>

    <el-tabs v-model="activeTab" class="basket-tabs">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane :label="`试题 ${prepBasket.questionCount}`" name="question" />
      <el-tab-pane :label="`资源 ${prepBasket.resourceCount}`" name="resource" />
    </el-tabs>

    <motion.div v-loading="prepBasket.loading" class="item-list">
      <p v-if="!filteredItems.length" class="empty-tip">暂无内容，浏览资源或试题后点击「加入资料篮」</p>
      <motion.div
        v-for="item in filteredItems"
        :key="item.id || `${item.itemType}-${item.refId}`"
        class="basket-item"
        layout
      >
        <div class="item-main">
          <span class="type-tag">{{ typeLabel(item.itemType) }}</span>
          <p class="item-title">{{ item.title }}</p>
          <p v-if="item.subtitle" class="item-sub">{{ item.subtitle }}</p>
        </div>
        <el-button type="danger" link size="small" @click="prepBasket.removeItem(item.id!)">
          移除
        </el-button>
      </motion.div>
    </motion.div>

    <footer class="panel-foot">
      <el-button size="small" :disabled="!prepBasket.totalCount" @click="prepBasket.clearAll">
        清空
      </el-button>
      <el-button
        size="small"
        type="success"
        :loading="prepBasket.downloading"
        :disabled="!prepBasket.downloadableItems.length"
        @click="prepBasket.downloadAllAsZip"
      >
        批量下载
      </el-button>
      <el-button
        type="primary"
        size="small"
        :disabled="prepBasket.questionCount < 1"
        @click="goAssemble"
      >
        去组卷 ({{ prepBasket.questionCount }})
      </el-button>
    </footer>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { motion } from 'motion-v'
import { usePrepBasketStore } from '@/store/prepBasket'
import type { BasketItemType } from '@/api/prep'

import { useRouter } from 'vue-router'

const router = useRouter()
defineEmits<{ 'go-assemble': [] }>()

function goAssemble() {
  router.push('/lesson/assemble')
}

const prepBasket = usePrepBasketStore()
const activeTab = ref<'all' | 'question' | 'resource'>('all')

const filteredItems = computed(() => {
  if (activeTab.value === 'all') return prepBasket.items
  return prepBasket.items.filter((i) => i.itemType === activeTab.value)
})

function typeLabel(t: BasketItemType) {
  const map: Record<string, string> = {
    resource: '资源',
    question: '试题',
    paper: '试卷',
    album: '专辑',
  }
  return map[t] || t
}

onMounted(() => prepBasket.fetchBasket())
</script>

<style scoped>
.prep-basket-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 420px;
  padding: 16px;
  background: var(--bg-card, #fff);
  border: 1px solid var(--border-light, #eee);
  border-radius: 12px;
}
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.panel-head h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
}
.count-badge {
  background: var(--color-primary, #2563eb);
  color: #fff;
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 999px;
}
.basket-tabs {
  margin-bottom: 8px;
}
.item-list {
  flex: 1;
  overflow-y: auto;
  min-height: 200px;
  max-height: calc(100vh - 320px);
}
.empty-tip {
  font-size: 13px;
  color: var(--text-secondary);
  text-align: center;
  padding: 24px 8px;
  line-height: 1.6;
}
.basket-item {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  padding: 10px 0;
  border-bottom: 1px dashed var(--border-light);
}
.item-main {
  flex: 1;
  min-width: 0;
}
.type-tag {
  font-size: 11px;
  color: var(--color-primary);
  background: #eff6ff;
  padding: 2px 6px;
  border-radius: 4px;
}
.item-title {
  font-size: 13px;
  font-weight: 600;
  margin: 6px 0 4px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.item-sub {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0;
}
.panel-foot {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--border-light);
}
.panel-foot .el-button:nth-child(3) {
  flex: 1;
  min-width: 100px;
}
</style>
