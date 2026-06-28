<template>
  <footer class="app-footer">
    <div class="footer-inner">
      <div class="footer-grid">
        <!-- 平台介绍 -->
        <div class="footer-section">
          <div class="footer-brand">
            <span class="logo-icon">📚</span>
            <span>新课堂教育</span>
          </div>
          <p class="footer-desc">
            面向小学、初中、高中及美术、舞蹈全学段的一站式教育资源共享与服务平台，为百万教师用户提供优质教学资源。
          </p>
        </div>

        <!-- 学段资源 -->
        <div class="footer-section">
          <h4>学段资源</h4>
          <ul>
            <li><router-link to="/subject/primary/chinese">小学专区</router-link></li>
            <li><router-link to="/subject/junior/chinese">初中专区</router-link></li>
            <li><router-link to="/subject/senior/chinese">高中专区</router-link></li>
            <li><router-link to="/subject/art/art">美术专区</router-link></li>
            <li><router-link to="/subject/dance/dance">舞蹈专区</router-link></li>
          </ul>
        </div>

        <!-- 核心功能 -->
        <div class="footer-section">
          <h4>核心功能</h4>
          <ul>
            <li><router-link to="/lesson">备课中心</router-link></li>
            <li><router-link to="/lesson/smart">智能备课</router-link></li>
            <li><router-link to="/exam">试题试卷</router-link></li>
            <li><router-link to="/exam/smart">智能组卷</router-link></li>
          </ul>
        </div>

        <!-- 服务支持 -->
        <div class="footer-section">
          <h4>服务支持</h4>
          <ul>
            <li><router-link to="/help">帮助中心</router-link></li>
            <li><router-link to="/membership">会员服务</router-link></li>
            <li><router-link to="/news">教育资讯</router-link></li>
            <li><a href="#">名师堂</a></li>
          </ul>
        </div>

        <!-- 特色频道 -->
        <div class="footer-section">
          <h4>特色频道</h4>
          <ul>
            <li><router-link to="/feature">全部频道</router-link></li>
            <li v-for="ch in featureChannels" :key="ch.navCommand">
              <router-link :to="ch.path">{{ ch.name }}</router-link>
            </li>
          </ul>
        </div>
      </div>

      <div class="footer-bottom">
        <p>&copy; 2026 新课堂教育 All Rights Reserved. | 为教育赋能，让教学更高效</p>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { FEATURE_CHANNELS } from '@/constants/featureChannelRegistry'

const featureChannels = FEATURE_CHANNELS
</script>

<style scoped>
.app-footer {
  background: linear-gradient(135deg, #1A1A2E 0%, #16213E 100%);
  color: #a0a0b8;
  padding: 48px 0 24px;
  margin-top: 60px;
}

.footer-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
}

.footer-grid {
  display: grid;
  grid-template-columns: 1.5fr repeat(4, 1fr);
  gap: 32px;
  margin-bottom: 40px;
}

.footer-section h4 {
  color: #ffffff;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 16px;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #ffffff;
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 12px;
}
.logo-icon { font-size: 24px; }

.footer-desc {
  font-size: 13px;
  line-height: 1.8;
  color: #8080a0;
}

.footer-section ul {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.footer-section a {
  color: #8080a0;
  font-size: 13px;
  transition: color 0.2s;
}
.footer-section a:hover {
  color: #ffffff;
}

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  padding-top: 20px;
  text-align: center;
  font-size: 13px;
  color: #606080;
}

@media (max-width: 768px) {
  .footer-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
