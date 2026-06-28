<template>
  <div class="about-page">
    <section class="about-banner">
      <div class="container" style="text-align: center;">
        <h1>关于我们</h1>
        <p>打造中国领先的新课堂教育资源生态平台</p>
      </div>
    </section>

    <div class="container" style="padding: 48px 24px;">
      <!-- 平台介绍 -->
      <div class="about-section card">
        <h2>🏫 平台简介</h2>
        <p>新课堂教育致力于为全国小学、初中、高中及美术、舞蹈教师、学生及家长提供一站式教学资源服务。平台汇聚千万级优质教学资源，覆盖全学科、全学段、全版本，并依托人工智能技术打造智能备课、智能组卷等创新教学工具。</p>
        <div class="about-stats">
          <div class="stat-item">
            <span class="stat-num">1000万+</span>
            <span class="stat-label">优质资源</span>
          </div>
          <div class="stat-item">
            <span class="stat-num">500万+</span>
            <span class="stat-label">注册用户</span>
          </div>
          <div class="stat-item">
            <span class="stat-num">15</span>
            <span class="stat-label">学科覆盖</span>
          </div>
          <div class="stat-item">
            <span class="stat-num">3</span>
            <span class="stat-label">学段覆盖</span>
          </div>
        </div>
      </div>

      <!-- 核心理念 -->
      <div class="about-section card">
        <h2>💡 核心理念</h2>
        <div class="concept-grid">
          <div class="concept-item">
            <div class="concept-icon">🎯</div>
            <h4>精准匹配</h4>
            <p>基于AI和大数据，精准匹配教师教学需求与学生个性化学习需求</p>
          </div>
          <div class="concept-item">
            <div class="concept-icon">🤖</div>
            <h4>智能驱动</h4>
            <p>智能备课、智能组卷、智能推荐，让教学更高效</p>
          </div>
          <div class="concept-item">
            <div class="concept-icon">🌍</div>
            <h4>开放共享</h4>
            <p>构建开放共享的教育资源生态，促进优质教育资源均衡化</p>
          </div>
          <div class="concept-item">
            <div class="concept-icon">🔒</div>
            <h4>品质保障</h4>
            <p>严格的质量审核体系，确保每一份资源的专业性和准确性</p>
          </div>
        </div>
      </div>

      <!-- 联系方式 -->
      <div class="about-section card">
        <h2>📞 联系我们</h2>
        <div class="contact-list">
          <div class="contact-item"><span>🏢</span> 地址：北京市海淀区中关村科技园</div>
          <div class="contact-item"><span>📞</span> 电话：400-888-0000</div>
          <div class="contact-item"><span>📧</span> 邮箱：contact@k12edu.com</div>
          <div class="contact-item"><span>🌐</span> 官网：www.k12edu.com</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.about-banner {
  background: linear-gradient(135deg, #1A1A2E, #16213E);
  color: #fff;
  padding: 48px 0;
}
.about-banner h1 { font-size: 36px; font-weight: 700; margin-bottom: 12px; }
.about-banner p { font-size: 16px; opacity: 0.8; }

.about-section { padding: 36px; margin-bottom: 24px; }
.about-section h2 { font-size: 22px; font-weight: 700; margin-bottom: 20px; color: var(--color-primary); }
.about-section > p { font-size: 15px; line-height: 1.8; color: var(--text-regular); margin-bottom: 24px; }

.about-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.stat-item {
  text-align: center;
  padding: 20px;
  background: var(--color-primary-bg);
  border-radius: var(--radius-md);
}
.stat-num { display: block; font-size: 28px; font-weight: 800; color: var(--color-primary); }
.stat-label { font-size: 14px; color: var(--text-secondary); }

.concept-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.concept-item { text-align: center; padding: 20px; }
.concept-icon { font-size: 40px; margin-bottom: 12px; }
.concept-item h4 { font-size: 16px; font-weight: 700; margin-bottom: 8px; }
.concept-item p { font-size: 13px; color: var(--text-secondary); line-height: 1.6; }

.contact-list { display: flex; flex-direction: column; gap: 12px; }
.contact-item {
  font-size: 15px;
  color: var(--text-regular);
  display: flex;
  align-items: center;
  gap: 12px;
}

@media (max-width: 768px) {
  .about-stats, .concept-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
