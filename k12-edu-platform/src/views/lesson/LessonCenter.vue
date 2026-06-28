<template>
  <div class="lesson-page">
    <section class="lesson-banner">
      <div class="container">
        <router-link to="/" class="breadcrumb-link">首页</router-link>
        <span class="breadcrumb-sep">/</span>
        <span>备课中心</span>
        <h1>📝 备课中心</h1>
        <p>智能备课、组卷系统、名校套卷、精品专辑，一站式教学资源解决方案</p>
      </div>
    </section>

    <div class="container" style="padding: 32px 24px;">
      <!-- 功能入口 -->
      <div class="func-grid">
        <router-link to="/lesson/smart" class="func-card">
          <div class="func-icon">🤖</div>
          <h3>智能备课</h3>
          <p>AI一键生成课件教案，智能整合备课素材</p>
        </router-link>
        <router-link to="/exam/smart" class="func-card">
          <div class="func-icon">🎯</div>
          <h3>智能组卷</h3>
          <p>基于知识图谱智能组卷，难度精准控制</p>
        </router-link>
        <div class="func-card">
          <div class="func-icon">🏫</div>
          <h3>名校套卷</h3>
          <p>全国名校同步试卷、月考卷、期末卷</p>
        </div>
        <div class="func-card">
          <div class="func-icon">💎</div>
          <h3>精品专辑</h3>
          <p>系列化教学资源、备考专题合集</p>
        </div>
      </div>

      <!-- 资料类型大全 -->
      <div class="section">
        <h2 class="section-title">📂 资料类型大全</h2>
        <p class="section-desc">涵盖教学全场景资源类型</p>
        <div class="type-grid">
          <div v-for="t in materialTypes" :key="t.key" class="type-card card">
            <div class="type-icon">{{ t.icon }}</div>
            <h4>{{ t.name }}</h4>
            <p>{{ t.desc }}</p>
            <span class="type-count">{{ t.count }} 资源</span>
          </div>
        </div>
      </div>

      <!-- 升学备考专区 -->
      <div class="section">
        <h2 class="section-title">🎓 升学备考专区</h2>
        <p class="section-desc">覆盖小升初、中考、高考全备考阶段</p>
        <div class="exam-zones">
          <div class="zone-card">
            <div class="zone-header spring">
              <h3>🏫 小升初备考</h3>
            </div>
            <ul class="zone-list">
              <li><router-link to="/exam?type=spring_entrance">真题汇编</router-link></li>
              <li><router-link to="/exam?type=mock">模拟试卷</router-link></li>
              <li><router-link to="/exam">专项复习</router-link></li>
              <li><router-link to="/news">择校指导</router-link></li>
            </ul>
          </div>
          <div class="zone-card">
            <div class="zone-header zhongkao">
              <h3>🏅 中考备考</h3>
            </div>
            <ul class="zone-list">
              <li><router-link to="/exam?type=zhongkao">考纲解读</router-link></li>
              <li><router-link to="/exam?type=zhongkao">一轮复习</router-link></li>
              <li><router-link to="/exam?type=zhongkao">二轮专题</router-link></li>
              <li><router-link to="/exam?type=zhongkao">三轮冲刺</router-link></li>
              <li><router-link to="/exam?type=zhongkao">中考真题</router-link></li>
            </ul>
          </div>
          <div class="zone-card">
            <div class="zone-header gaokao">
              <h3>🏆 高考备考</h3>
            </div>
            <ul class="zone-list">
              <li><router-link to="/news">高考资讯</router-link></li>
              <li><router-link to="/exam?type=gaokao">一轮复习</router-link></li>
              <li><router-link to="/exam?type=gaokao">二轮专题</router-link></li>
              <li><router-link to="/exam?type=gaokao">三轮冲刺</router-link></li>
              <li><router-link to="/exam?type=gaokao">真题精讲</router-link></li>
              <li><router-link to="/news">志愿填报</router-link></li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const materialTypes = [
  { key: 'exam', icon: '📝', name: '试题试卷', desc: '同步练习、单元测试、月考、期中、期末、模拟、真题', count: '120万+' },
  { key: 'courseware', icon: '📊', name: '教学课件', desc: '同步课件、复习课件、专题课件', count: '85万+' },
  { key: 'lessonplan', icon: '📋', name: '教案学案', desc: '教学设计、导学案、备课讲义', count: '92万+' },
  { key: 'material', icon: '📷', name: '教学素材', desc: '图片、文字、视频、音频、动画素材', count: '45万+' },
  { key: 'video', icon: '🎬', name: '音视频资源', desc: '教学微课、课堂实录、名师讲课视频', count: '38万+' },
  { key: 'other', icon: '📂', name: '其他资源', desc: '备课综合、知识点总结、解题技巧', count: '56万+' },
]
</script>

<style scoped>
.lesson-banner {
  background: linear-gradient(135deg, #7C3AED, #4361EE);
  color: #fff;
  padding: 40px 0;
}
.breadcrumb-link { color: rgba(255,255,255,0.8); }
.breadcrumb-sep { margin: 0 8px; opacity: 0.6; }
.lesson-banner h1 { font-size: 32px; font-weight: 700; margin: 12px 0 8px; }
.lesson-banner p { opacity: 0.9; }

.func-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 48px;
}
.func-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 32px 24px;
  text-align: center;
  text-decoration: none;
  color: var(--text-primary);
  border: 1px solid var(--border-light);
  transition: all 0.25s;
}
.func-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary);
}
.func-icon { font-size: 40px; margin-bottom: 12px; }
.func-card h3 { font-size: 17px; font-weight: 700; margin-bottom: 8px; }
.func-card p { font-size: 13px; color: var(--text-secondary); line-height: 1.5; }

.type-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}
.type-card {
  padding: 24px;
  text-decoration: none;
  color: var(--text-primary);
}
.type-icon { font-size: 36px; margin-bottom: 10px; }
.type-card h4 { font-size: 16px; font-weight: 700; margin-bottom: 6px; }
.type-card p { font-size: 13px; color: var(--text-secondary); margin-bottom: 10px; line-height: 1.5; }
.type-count { font-size: 13px; color: var(--color-primary); font-weight: 600; }

.exam-zones {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}
.zone-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--border-light);
}
.zone-header {
  padding: 20px;
  color: #fff;
  text-align: center;
}
.zone-header.spring { background: linear-gradient(135deg, #FF6B6B, #ee5a24); }
.zone-header.zhongkao { background: linear-gradient(135deg, #4ECDC4, #44bd9e); }
.zone-header.gaokao { background: linear-gradient(135deg, #45B7D1, #6C5CE7); }
.zone-header h3 { font-size: 18px; font-weight: 700; }
.zone-list {
  list-style: none;
  padding: 16px 20px;
}
.zone-list li {
  padding: 8px 0;
  border-bottom: 1px solid var(--border-light);
}
.zone-list li:last-child { border-bottom: none; }
.zone-list a {
  font-size: 14px;
  color: var(--text-regular);
  text-decoration: none;
  transition: color 0.2s;
}
.zone-list a:hover { color: var(--color-primary); }

@media (max-width: 768px) {
  .func-grid, .type-grid, .exam-zones { grid-template-columns: 1fr; }
}
</style>
