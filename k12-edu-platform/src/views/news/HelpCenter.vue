<template>
  <div class="help-page">
    <section class="help-banner">
      <div class="container">
        <router-link to="/" class="breadcrumb-link">首页</router-link>
        <span class="breadcrumb-sep">/</span>
        <span>帮助中心</span>
        <h1>💬 帮助中心</h1>
        <p>使用教程、客户服务、视频帮助，快速上手新课堂教育平台</p>
      </div>
    </section>

    <div class="container" style="padding: 32px 24px;">
      <div class="help-grid">
        <div class="help-card card">
          <div class="help-icon">📖</div>
          <h3>使用教程</h3>
          <ul>
            <li>如何注册与登录</li>
            <li>如何搜索资源</li>
            <li>如何下载资源</li>
            <li>如何使用智能备课</li>
            <li>如何使用智能组卷</li>
          </ul>
        </div>
        <div class="help-card card">
          <div class="help-icon">🎬</div>
          <h3>视频帮助</h3>
          <ul>
            <li>新用户入门指南</li>
            <li>备课中心使用教程</li>
            <li>智能组卷操作演示</li>
            <li>高级搜索技巧</li>
            <li>会员权益介绍</li>
          </ul>
        </div>
        <div class="help-card card">
          <div class="help-icon">🎓</div>
          <h3>客户学院</h3>
          <ul>
            <li>教学资源平台使用技巧</li>
            <li>高效备课方法论</li>
            <li>教育信息化前沿趋势</li>
            <li>名师经验分享</li>
            <li>优秀教学案例分析</li>
          </ul>
        </div>
        <div class="help-card card">
          <div class="help-icon">📞</div>
          <h3>客户服务</h3>
          <ul>
            <li>在线客服：7x24小时</li>
            <li>客服邮箱：support@k12.com</li>
            <li>客服热线：400-888-0000</li>
            <li>工作日 9:00 - 18:00</li>
            <li>意见反馈</li>
          </ul>
        </div>
      </div>

      <!-- FAQ -->
      <div id="login-help" class="section legal-section">
        <h2 class="section-title">🔐 登录与账号</h2>
        <div class="legal-card card">
          <h3>忘记密码怎么办？</h3>
          <p>推荐使用「短信登录」验证码快捷登录，无需记忆密码。登录后可在「个人中心 → 账号安全」中修改登录密码。如仍无法登录，请联系客服 400-888-0000。</p>
        </div>
      </div>

      <div class="section">
        <h2 class="section-title">❓ 常见问题</h2>
        <el-collapse>
          <el-collapse-item title="如何注册成为平台用户？" name="1">
            <p>点击页面右上角「注册」，使用手机号验证码或账号密码即可完成教师账号注册并自动登录。已有账号请直接登录。</p>
          </el-collapse-item>
          <el-collapse-item title="免费用户和会员用户有什么区别？" name="2">
            <p>免费用户可以浏览所有资源，每天可免费下载3个基础资源。会员用户享受无限下载、智能备课、智能组卷、优先客服等专属特权。</p>
          </el-collapse-item>
          <el-collapse-item title="如何使用智能备课功能？" name="3">
            <p>进入备课中心 → 智能备课页面，选择学段、学科、年级，输入课题名称，选择生成类型（课件/教案/导学案/练习），点击「AI智能生成」即可。</p>
          </el-collapse-item>
          <el-collapse-item title="智能组卷支持哪些题型？" name="4">
            <p>目前支持选择题、填空题、判断题、简答题、解答题五大题型，可根据需求自由配置各题型数量和分值。</p>
          </el-collapse-item>
          <el-collapse-item title="上传的资源多久可以审核通过？" name="5">
            <p>上传资源后，系统会自动进行AI初审（约5-10分钟），通过后进入人工复核阶段，通常1-2个工作日内完成审核。</p>
          </el-collapse-item>
        </el-collapse>
      </div>

      <div id="terms" class="section legal-section">
        <h2 class="section-title">📄 用户协议</h2>
        <div class="legal-card card">
          <p class="legal-meta">更新日期：2024-06-01</p>
          <p>欢迎使用新课堂教育平台。使用本平台服务即表示您同意遵守相关法律法规及平台规则，合法、合规地使用教学资源，不得用于侵权、违法或损害他人权益的行为。</p>
          <p>教师用户上传资源须保证拥有相应版权或授权；学生、家长用户应合理使用下载资源，仅限个人学习用途。</p>
        </div>
      </div>

      <div id="privacy" class="section legal-section">
        <h2 class="section-title">🔒 隐私政策</h2>
        <div class="legal-card card">
          <p class="legal-meta">更新日期：2024-06-01</p>
          <p>我们重视您的个人信息保护。注册、登录时收集的手机号、身份信息仅用于账号认证、安全验证与服务提供，不会出售给第三方。</p>
          <p>短信验证码用于身份核验；您可在个人中心管理账号信息。如需注销账号或行使个人信息权利，请联系客服。</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

onMounted(() => {
  nextTick(() => {
    const hash = route.hash?.replace('#', '')
    if (!hash) return
    document.getElementById(hash)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
})
</script>

<style scoped>
.help-banner {
  background: linear-gradient(135deg, #10B981, #059669);
  color: #fff;
  padding: 40px 0;
}
.breadcrumb-link { color: rgba(255,255,255,0.8); }
.breadcrumb-sep { margin: 0 8px; opacity: 0.6; }
.help-banner h1 { font-size: 32px; font-weight: 700; margin: 12px 0 8px; }
.help-banner p { opacity: 0.9; }

.help-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 48px;
}
.help-card { padding: 24px; text-decoration: none; color: var(--text-primary); }
.help-icon { font-size: 36px; margin-bottom: 12px; }
.help-card h3 { font-size: 16px; font-weight: 700; margin-bottom: 12px; }
.help-card ul { list-style: none; }
.help-card li {
  padding: 6px 0;
  font-size: 13px;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
  transition: color 0.2s;
}
.help-card li:hover { color: var(--color-primary); }
.help-card li:last-child { border-bottom: none; }

.section-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 16px;
}

.legal-section {
  margin-top: 40px;
  scroll-margin-top: 80px;
}

.legal-card {
  padding: 24px;
  line-height: 1.8;
  color: var(--text-regular);
  font-size: 14px;
}

.legal-card h3 {
  margin: 0 0 12px;
  font-size: 16px;
  color: var(--text-primary);
}

.legal-card p {
  margin: 0 0 12px;
}

.legal-card p:last-child {
  margin-bottom: 0;
}

.legal-meta {
  font-size: 12px;
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .help-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
