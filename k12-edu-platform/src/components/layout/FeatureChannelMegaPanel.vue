<template>
  <motion.div
    class="feature-mega-panel"
    :initial="{ opacity: 0, y: 8, scale: 0.98 }"
    :animate="{ opacity: 1, y: 0, scale: 1 }"
    :transition="{ duration: 0.22, ease: 'easeOut' }"
  >
    <div class="mega-head">
      <motion.div class="mega-head-text" :initial="{ opacity: 0, x: -6 }" :animate="{ opacity: 1, x: 0 }">
        <h3>特色资源频道</h3>
        <p>德育 · 文化 · 学业，五大精品频道一站直达</p>
      </motion.div>
      <router-link to="/feature" class="mega-view-all" @click="emit('navigate')">
        查看全部
        <el-icon><ArrowRight /></el-icon>
      </router-link>
    </div>

    <div v-for="(group, gi) in channelGroups" :key="group.key" class="mega-group">
      <motion.div
        class="mega-group-label"
        :initial="{ opacity: 0 }"
        :animate="{ opacity: 1 }"
        :transition="{ delay: 0.04 * gi }"
      >
        <span class="group-name">{{ group.label }}</span>
        <span class="group-desc">{{ group.desc }}</span>
      </motion.div>
      <div class="mega-cards" :class="{ 'mega-cards--solo': group.channels.length === 1 }">
        <button
          v-for="(ch, ci) in group.channels"
          :key="ch.navCommand"
          type="button"
          class="mega-card"
          :style="{ '--ch-color': ch.color, '--ch-gradient': ch.gradient }"
          @click="goChannel(ch.navCommand)"
        >
          <motion.span
            class="mega-card-icon"
            :initial="{ opacity: 0, scale: 0.9 }"
            :animate="{ opacity: 1, scale: 1 }"
            :transition="{ delay: 0.05 * (gi + ci) }"
          >{{ ch.icon }}</motion.span>
          <span class="mega-card-body">
            <strong>{{ ch.name }}</strong>
            <span class="mega-card-desc">{{ ch.shortDesc }}</span>
            <span class="mega-card-meta">
              <span v-if="ch.tier === 'flagship'" class="flagship-badge">旗舰专页</span>
              <span class="count">{{ ch.resourceCount }} 资源</span>
            </span>
          </span>
          <el-icon class="mega-card-arrow"><ArrowRight /></el-icon>
        </button>
      </div>
    </div>

    <router-link
      :to="{ path: spotlight.path, query: spotlight.query }"
      class="mega-spotlight"
      @click="emit('navigate')"
    >
      <span class="spotlight-emoji">{{ spotlight.emoji }}</span>
      <span class="spotlight-text">
        <strong>本月推荐 · {{ spotlight.title }}</strong>
        <span>{{ spotlight.desc }}</span>
      </span>
      <span class="spotlight-cta">去看看 →</span>
    </router-link>
  </motion.div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import { motion } from 'motion-v'
import {
  getFeatureChannelGroups,
  FEATURE_CHANNEL_SPOTLIGHT,
  resolveFeatureChannelPath,
} from '@/constants/featureChannelRegistry'

const emit = defineEmits<{ navigate: [] }>()

const router = useRouter()
const channelGroups = getFeatureChannelGroups()
const spotlight = FEATURE_CHANNEL_SPOTLIGHT

function goChannel(command: string) {
  emit('navigate')
  router.push(resolveFeatureChannelPath(command))
}
</script>

<style scoped>
.feature-mega-panel {
  width: 560px;
  max-width: calc(100vw - 32px);
  padding: 18px 20px 16px;
  box-sizing: border-box;
}

.mega-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--border-light, #eef0f6);
}

.mega-head-text h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px;
}

.mega-head-text p {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0;
}

.mega-view-all {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary, #4361ee);
  text-decoration: none;
  white-space: nowrap;
  padding: 6px 10px;
  border-radius: 8px;
  transition: background 0.2s;
}

.mega-view-all:hover {
  background: var(--color-primary-bg, #ebf0ff);
}

.mega-group {
  margin-bottom: 14px;
}

.mega-group-label {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}

.group-name {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.04em;
}

.group-desc {
  font-size: 11px;
  color: var(--text-secondary);
}

.mega-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.mega-cards--solo {
  grid-template-columns: 1fr;
}

.mega-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--border-light, #eef0f6);
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  text-align: left;
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}

.mega-card:hover {
  border-color: var(--ch-color);
  box-shadow: 0 8px 24px rgba(67, 97, 238, 0.12);
  transform: translateY(-2px);
}

.mega-card-icon {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: var(--ch-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.mega-card-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.mega-card-body strong {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
}

.mega-card-desc {
  font-size: 11px;
  color: var(--text-secondary);
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.mega-card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
}

.flagship-badge {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 4px;
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  color: #b45309;
  font-weight: 600;
}

.mega-card-meta .count {
  font-size: 11px;
  font-weight: 600;
  color: var(--ch-color);
}

.mega-card-arrow {
  flex-shrink: 0;
  margin-top: 4px;
  color: var(--text-placeholder);
  font-size: 14px;
}

.mega-spotlight {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
  padding: 12px 14px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
  border: 1px solid #ddd6fe;
  text-decoration: none;
  color: inherit;
  transition: box-shadow 0.2s;
}

.mega-spotlight:hover {
  box-shadow: 0 4px 16px rgba(124, 58, 237, 0.15);
}

.spotlight-emoji {
  font-size: 28px;
  flex-shrink: 0;
}

.spotlight-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.spotlight-text strong {
  font-size: 13px;
  color: #5b21b6;
}

.spotlight-text span:last-child {
  font-size: 11px;
  color: var(--text-secondary);
}

.spotlight-cta {
  font-size: 12px;
  font-weight: 600;
  color: #7c3aed;
  white-space: nowrap;
}

@media (max-width: 600px) {
  .feature-mega-panel {
    width: 100%;
    padding: 14px;
  }
  .mega-cards {
    grid-template-columns: 1fr;
  }
}
</style>
