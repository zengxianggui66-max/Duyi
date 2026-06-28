<template>
  <div class="top-nav">
    <div class="top-nav-inner">
      <div class="nav-left">
        <!-- 学段切换 -->
        <div class="stage-bar">
          <button
            v-for="stage in stages"
            :key="stage.key"
            :class="['stage-btn', { active: currentStage === stage.key }]"
            @click="$emit('switch-stage', stage.key)"
          >
            {{ stage.name }}
          </button>
        </div>

        <!-- 功能入口 -->
        <div class="func-bar">
          <button
            v-for="item in menuItems"
            :key="item.key"
            :class="['func-btn', { active: currentFuncKey === item.key }]"
            @click="$emit('update:currentFuncKey', item.key)"
          >
            {{ item.name }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Stage {
  key: string
  name: string
}

interface MenuItem {
  key: string
  name: string
}

defineProps<{
  stages: Stage[]
  currentStage: string
  menuItems: MenuItem[]
  currentFuncKey: string
}>()

defineEmits<{
  'switch-stage': [key: string]
  'update:currentFuncKey': [key: string]
}>()
</script>

<style scoped>
.top-nav {
  background: #fff;
  border-bottom: 1px solid #E4E7ED;
  position: sticky;
  top: 0;
  z-index: 10;
}

.top-nav-inner {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 48px;
  flex: 1;
}

.stage-bar {
  display: flex;
  gap: 4px;
  background: #F5F7FA;
  padding: 4px;
  border-radius: 40px;
}

.stage-btn {
  padding: 6px 20px;
  border: none;
  background: transparent;
  border-radius: 32px;
  font-size: 15px;
  font-weight: 500;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s ease;
}

.stage-btn:hover {
  color: #409EFF;
  background: rgba(64, 158, 255, 0.08);
}

.stage-btn.active {
  background: #409EFF;
  color: #fff;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

.func-bar {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-left: auto;
  justify-content: flex-end;
  transform: translateX(-33%);
}

@media (max-width: 1200px) {
  .func-bar {
    transform: none;
  }
}

.func-btn {
  padding: 6px 16px;
  border: none;
  background: transparent;
  border-radius: 24px;
  font-size: 15px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}

.func-btn:hover {
  background: #ECF5FF;
  color: #409EFF;
}

.func-btn.active {
  background: #ECF5FF;
  color: #409EFF;
  font-weight: 600;
}
</style>
