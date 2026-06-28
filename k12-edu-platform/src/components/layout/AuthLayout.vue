<template>
  <div class="auth-layout" :class="layoutClass">
    <header class="auth-layout__header">
      <router-link to="/" class="auth-layout__brand">
        <span class="auth-layout__brand-icon">📚</span>
        <span class="auth-layout__brand-text">新课堂教育</span>
      </router-link>
      <router-link to="/" class="auth-layout__home-link">返回首页</router-link>
    </header>

    <div class="auth-layout__center">
      <div class="auth-layout__shell">
        <aside class="auth-layout__promo">
          <div class="auth-layout__promo-brand">
            <span class="auth-layout__promo-brand-icon">📚</span>
            <span class="auth-layout__promo-brand-text">新课堂教育</span>
          </div>
          <p class="auth-layout__promo-tag">{{ promoTag }}</p>
          <h1 class="auth-layout__promo-title">{{ promoTitle }}</h1>
          <ul class="auth-layout__promo-list">
            <li v-for="item in AUTH_PROMO_ITEMS" :key="item">
              <span class="auth-layout__promo-check" aria-hidden="true">✓</span>
              <span>{{ item }}</span>
            </li>
          </ul>
          <p class="auth-layout__promo-foot">{{ AUTH_PROMO_FOOTNOTE }}</p>
        </aside>

        <main class="auth-layout__panel">
          <router-view />
        </main>
      </div>
    </div>

    <footer class="auth-layout__legal">
      <router-link :to="AUTH_TERMS_PATH">用户协议</router-link>
      <span class="auth-layout__legal-sep">|</span>
      <router-link :to="AUTH_PRIVACY_PATH">隐私政策</router-link>
      <span class="auth-layout__legal-sep">|</span>
      <span>© 2026 新课堂教育</span>
      <span class="auth-layout__legal-sep">|</span>
      <span>京ICP备00000000号</span>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { AUTH_PRIVACY_PATH, AUTH_TERMS_PATH } from '@/constants/authLegal'
import { AUTH_PROMO_FOOTNOTE, AUTH_PROMO_ITEMS } from '@/constants/authPromo'

const route = useRoute()

const promoTitle = computed(() => {
  if (route.path.startsWith('/register')) return '欢迎注册'
  if (route.path.startsWith('/bind-phone')) return '绑定手机号'
  if (route.query.intent === 'admin') return '管理端登录'
  return '欢迎登录'
})

const promoTag = computed(() =>
  route.query.intent === 'admin' ? '运营管理平台' : '教师备课平台',
)

const layoutClass = computed(() => ({
  'auth-layout--register': route.path.startsWith('/register'),
  'auth-layout--login': route.path === '/login',
}))
</script>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(ellipse 80% 60% at 20% 0%, rgba(67, 97, 238, 0.08), transparent 55%),
    radial-gradient(ellipse 60% 50% at 90% 100%, rgba(124, 58, 237, 0.06), transparent 50%),
    linear-gradient(165deg, #eef1f7 0%, #e8edf5 45%, #eef1f7 100%);
}

.auth-layout__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 40px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid rgba(232, 236, 244, 0.8);
}

.auth-layout__brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}

.auth-layout__brand-icon {
  font-size: 24px;
}

.auth-layout__brand-text {
  font-size: 18px;
  font-weight: 800;
  background: linear-gradient(135deg, #4361EE, #7C3AED);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.auth-layout__home-link {
  font-size: 14px;
  color: var(--text-secondary, #8E8EA0);
  text-decoration: none;
}

.auth-layout__home-link:hover {
  color: var(--color-primary, #4361EE);
}

.auth-layout__center {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}

.auth-layout__shell {
  display: flex;
  width: 100%;
  max-width: 960px;
  height: 600px;
  min-height: 600px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow:
    0 24px 64px rgba(26, 26, 46, 0.12),
    0 0 0 1px rgba(255, 255, 255, 0.6);
  background: #fff;
}

.auth-layout--register .auth-layout__shell {
  height: 660px;
  min-height: 660px;
}

.auth-layout__promo {
  flex: 0 0 44%;
  padding: 48px 40px;
  background: linear-gradient(155deg, #4f6ef7 0%, #4361EE 42%, #3250D3 72%, #2a3fb8 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
  align-self: stretch;
}

.auth-layout__promo-brand {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.auth-layout__promo-brand-icon {
  font-size: 28px;
  line-height: 1;
}

.auth-layout__promo-brand-text {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0.02em;
  color: #fff;
}

.auth-layout__promo::before {
  content: '';
  position: absolute;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.14);
  top: -80px;
  right: -60px;
}

.auth-layout__promo::after {
  content: '';
  position: absolute;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
  bottom: 40px;
  left: -40px;
}

.auth-layout__promo-tag {
  position: relative;
  margin: 0 0 12px;
  font-size: 13px;
  opacity: 0.9;
}

.auth-layout__promo-title {
  position: relative;
  margin: 0 0 32px;
  font-size: 30px;
  font-weight: 700;
  line-height: 1.25;
  letter-spacing: 0.02em;
}

.auth-layout__promo-list {
  position: relative;
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.auth-layout__promo-list li {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 14px;
  line-height: 1.5;
  opacity: 0.95;
}

.auth-layout__promo-check {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  margin-top: 2px;
}

.auth-layout__promo-foot {
  position: relative;
  margin: 32px 0 0;
  font-size: 12px;
  opacity: 0.75;
  line-height: 1.6;
}

.auth-layout__panel {
  flex: 1;
  min-width: 0;
  min-height: 0;
  padding: 0 48px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  align-self: stretch;
  overflow-x: hidden;
  overflow-y: auto;
}

.auth-layout__legal {
  padding: 16px 24px 24px;
  text-align: center;
  font-size: 12px;
  color: var(--text-secondary, #8E8EA0);
}

.auth-layout__legal a {
  color: var(--text-secondary, #8E8EA0);
  text-decoration: none;
}

.auth-layout__legal a:hover {
  color: var(--color-primary, #4361EE);
}

.auth-layout__legal-sep {
  margin: 0 8px;
  opacity: 0.5;
}

@media (max-width: 768px) {
  .auth-layout__header {
    padding: 12px 16px;
  }

  .auth-layout__center {
    padding: 16px;
    align-items: flex-start;
  }

  .auth-layout__shell {
    flex-direction: column;
    height: auto;
    min-height: auto;
  }

  .auth-layout__promo {
    flex: none;
    padding: 28px 24px;
  }

  .auth-layout__promo-title {
    font-size: 22px;
    margin-bottom: 16px;
  }

  .auth-layout__promo-list {
    gap: 10px;
  }

  .auth-layout__promo-foot {
    margin-top: 16px;
  }

  .auth-layout__panel {
    padding: 28px 24px;
    justify-content: center;
    padding-top: 32px;
    padding-bottom: 32px;
  }

  .auth-layout__legal {
    font-size: 11px;
    line-height: 1.8;
  }
}
</style>
