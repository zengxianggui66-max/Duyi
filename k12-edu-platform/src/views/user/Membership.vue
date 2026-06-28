<template>
  <div class="membership-page">
    <section class="member-banner">
      <div class="container" style="text-align: center;">
        <h1>👑 会员服务</h1>
        <p>畅享千万级优质教学资源下载，解锁智能备课与智能组卷等专属特权</p>
      </div>
    </section>

    <div class="container" style="padding: 48px 24px;">
      <div class="plan-grid">
        <div class="plan-card card">
          <div class="plan-badge">免费版</div>
          <div class="plan-price">
            <span class="price">¥0</span>
            <span class="period">永久免费</span>
          </div>
          <ul class="plan-features">
            <li class="enabled">✅ 浏览所有资源</li>
            <li class="enabled">✅ 每日免费下载3个</li>
            <li>❌ 智能备课功能</li>
            <li>❌ 智能组卷功能</li>
            <li>❌ 优先客服支持</li>
            <li>❌ 批量下载</li>
          </ul>
          <el-button round size="large" style="width: 100%;">当前方案</el-button>
        </div>

        <div class="plan-card card popular">
          <div class="plan-badge hot">🔥 基础会员</div>
          <div class="plan-price">
            <span class="price">¥99</span>
            <span class="period">/年</span>
          </div>
          <ul class="plan-features">
            <li class="enabled">✅ 浏览所有资源</li>
            <li class="enabled">✅ 每日下载50个</li>
            <li class="enabled">✅ 智能备课功能</li>
            <li class="enabled">✅ 智能组卷功能</li>
            <li>❌ 优先客服支持</li>
            <li>❌ 批量下载</li>
          </ul>
          <el-button type="primary" round size="large" style="width: 100%;" @click="handleOrder(1)" :loading="ordering">立即开通</el-button>
        </div>

        <div class="plan-card card">
          <div class="plan-badge">💎 高级会员</div>
          <div class="plan-price">
            <span class="price">¥199</span>
            <span class="period">/年</span>
          </div>
          <ul class="plan-features">
            <li class="enabled">✅ 浏览所有资源</li>
            <li class="enabled">✅ 无限下载</li>
            <li class="enabled">✅ 智能备课功能</li>
            <li class="enabled">✅ 智能组卷功能</li>
            <li class="enabled">✅ 优先客服支持</li>
            <li class="enabled">✅ 批量下载</li>
          </ul>
          <el-button type="warning" round size="large" style="width: 100%;" @click="handleOrder(2)" :loading="ordering">立即开通</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { memberApi } from '@/api'
import type { MemberLevel } from '@/api/member'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const ordering = ref(false)

async function handleOrder(memberLevel: MemberLevel) {
  ordering.value = true
  try {
    await memberApi.createOrder(memberLevel)
    ElMessage.success('开通成功！')
    router.push('/profile')
  } catch (e: any) {
    ElMessage.error(e.message || '开通失败，请先登录')
  } finally {
    ordering.value = false
  }
}
</script>

<style scoped>
.member-banner {
  background: linear-gradient(135deg, #F59E0B, #EF4444, #8B5CF6);
  color: #fff;
  padding: 48px 0;
}
.member-banner h1 { font-size: 36px; font-weight: 700; margin-bottom: 12px; }
.member-banner p { font-size: 16px; opacity: 0.9; }

.plan-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  max-width: 960px;
  margin: 0 auto;
}
.plan-card {
  padding: 32px;
  text-align: center;
  border-radius: var(--radius-lg);
}
.plan-card.popular {
  border: 2px solid var(--color-primary);
  transform: scale(1.05);
  box-shadow: 0 8px 30px rgba(67, 97, 238, 0.2);
}
.plan-badge {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 16px;
}
.plan-badge.hot { color: var(--color-primary); }
.plan-price { margin-bottom: 24px; }
.price { font-size: 40px; font-weight: 800; color: var(--color-primary); }
.period { font-size: 14px; color: var(--text-secondary); }
.plan-features {
  list-style: none;
  text-align: left;
  margin-bottom: 24px;
}
.plan-features li {
  padding: 8px 0;
  font-size: 14px;
  color: var(--text-secondary);
}
.plan-features li.enabled { color: var(--text-primary); font-weight: 500; }

@media (max-width: 768px) {
  .plan-grid { grid-template-columns: 1fr; }
  .plan-card.popular { transform: none; }
}
</style>

