<template>
  <div class="site-header">
    <div class="header-inner">
      <!-- Logo -->
      <div class="site-logo" @click="router.push('/')">
        <span class="logo-icon">📚</span>
        <span class="logo-text">新课堂教育</span>
      </div>

      <!-- 搜索区域 -->
      <div class="search-area">
        <div class="search-box">
          <input
            v-model="keyword"
            type="text"
            class="search-input"
            placeholder="搜索试卷、教案、课件..."
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">
            <span class="search-icon">🔍</span>
            <span>搜索</span>
          </button>
        </div>
        <!-- 热门搜索词 -->
        <div class="hot-search">
          <span class="hot-label">热门：</span>
          <span
            v-for="word in hotWords"
            :key="word"
            class="hot-word"
            @click="quickSearch(word)"
          >{{ word }}</span>
        </div>
      </div>

      <!-- 快捷功能按钮 -->
      <div class="header-actions">
        <button class="action-btn data-blue-btn" @click="$emit('go-data-blue')">
          <span class="btn-icon">📋</span>
          <span class="btn-text">资料篮</span>
        </button>
        <button class="action-btn upload-btn" @click="$emit('go-upload')">
          <span class="btn-icon">⬆️</span>
          <span class="btn-text">上传资源</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

defineProps<{
  hotWords: string[]
}>()

defineEmits<{
  'go-data-blue': []
  'go-upload': []
}>()

const router = useRouter()
const keyword = ref('')

function handleSearch() {
  if (keyword.value.trim()) {
    router.push(`/search?q=${encodeURIComponent(keyword.value.trim())}`)
  }
}

function quickSearch(word: string) {
  keyword.value = word
  handleSearch()
}
</script>

<style scoped>
.site-header {
  background: #FFF8E7;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 100;
}

.header-inner {
  max-width: 1440px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.site-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  flex-shrink: 0;
  transition: opacity 0.2s;
}

.site-logo:hover {
  opacity: 0.85;
}

.logo-icon {
  font-size: 36px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
  color: #1a5cbf;
  letter-spacing: 1px;
}

.search-area {
  flex: 1;
  max-width: 560px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin: 0 24px;
}

.search-box {
  display: flex;
  background: #ffffff;
  border: 2px solid #E8DCC8;
  border-radius: 40px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.2s;
}

.search-box:focus-within {
  border-color: #1a5cbf;
  box-shadow: 0 4px 16px rgba(26, 92, 191, 0.15);
}

.search-input {
  flex: 1;
  padding: 12px 20px;
  border: none;
  outline: none;
  font-size: 15px;
  color: #2C3E50;
  background: transparent;
}

.search-input::placeholder {
  color: #B8A990;
}

.search-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #1a5cbf, #2d7dd2);
  border: none;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.search-btn:hover {
  background: linear-gradient(135deg, #2d7dd2, #3d8fd9);
  transform: translateY(-1px);
}

.search-icon {
  font-size: 16px;
}

.hot-search {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-left: 8px;
  flex-wrap: wrap;
}

.hot-label {
  font-size: 12px;
  color: #8B7355;
}

.hot-word {
  font-size: 12px;
  color: #1a5cbf;
  cursor: pointer;
  padding: 2px 8px;
  border-radius: 12px;
  background: rgba(26, 92, 191, 0.08);
  transition: all 0.2s;
}

.hot-word:hover {
  background: rgba(26, 92, 191, 0.15);
  color: #2d7dd2;
}

.header-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  border-radius: 40px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.data-blue-btn {
  background: linear-gradient(135deg, #1a5cbf, #2d7dd2);
  color: #fff;
}

.data-blue-btn:hover {
  background: linear-gradient(135deg, #2d7dd2, #3d8fd9);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(26, 92, 191, 0.3);
}

.action-btn.upload-btn {
  background: linear-gradient(135deg, #52c41a, #389e0d);
  color: #fff;
}

.action-btn.upload-btn:hover {
  background: linear-gradient(135deg, #73d13d, #52c41a);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
}

.btn-icon {
  font-size: 16px;
}
</style>
