<template>
  <div class="prep-basket-page">
    <div class="container">
      <nav class="breadcrumb">
        <router-link to="/lesson">备课中心</router-link>
        <span>/</span>
        <span>资料篮</span>
      </nav>
      <header class="page-title-row">
        <h1>📋 我的资料篮</h1>
        <el-button
          type="success"
          size="large"
          :loading="prepBasket.downloading"
          :disabled="!prepBasket.downloadableItems.length"
          @click="prepBasket.downloadAllAsZip"
        >
          📥 批量下载 ZIP
        </el-button>
      </header>
      <div class="page-layout">
        <main class="main-list card">
          <PrepBasketPanel style="min-height: 500px" />
        </main>
        <aside class="tips card">
          <h3>使用说明</h3>
          <ul>
            <li>在资源详情页点击「加入资料篮」添加课件、试卷等资源</li>
            <li>在备课中心「选题组卷」勾选试题加入资料篮</li>
            <li>试题达到 1 题以上可进入组卷，支持导出 Word / 答案 / 打印</li>
            <li>资源/试卷支持一键打包 ZIP（最多 50 个，含下载说明）</li>
            <li>登录后资料篮云端保存，未登录仅保存在本机</li>
          </ul>
          <router-link to="/lesson">
            <el-button type="primary" style="width: 100%">返回备课中心</el-button>
          </router-link>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { usePrepBasketStore } from '@/store/prepBasket'
import PrepBasketPanel from './components/PrepBasketPanel.vue'

const router = useRouter()
const prepBasket = usePrepBasketStore()

onMounted(() => prepBasket.fetchBasket())
</script>

<style scoped>
.prep-basket-page {
  padding: 24px 0 48px;
  min-height: 100%;
  background: var(--bg-page);
}
.breadcrumb {
  font-size: 14px;
  margin-bottom: 12px;
}
.breadcrumb a {
  color: var(--color-primary);
  text-decoration: none;
}
.page-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
.page-title-row h1 {
  margin: 0;
}
.page-layout {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 20px;
  margin-top: 20px;
}
.tips {
  padding: 20px;
}
.tips h3 {
  margin: 0 0 12px;
}
.tips ul {
  padding-left: 18px;
  font-size: 13px;
  line-height: 1.8;
  color: var(--text-secondary);
  margin-bottom: 20px;
}
@media (max-width: 900px) {
  .page-layout {
    grid-template-columns: 1fr;
  }
}
</style>
