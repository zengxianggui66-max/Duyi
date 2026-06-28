<template>
  <div class="header-actions">
    <el-button type="primary" size="large" round :loading="downloading" @click="$emit('download')">
      <el-icon><Download /></el-icon> 下载
    </el-button>
    <el-button size="large" round :type="isCollected ? 'warning' : ''" @click="$emit('collect')">
      <el-icon><Star v-if="!isCollected" /><StarFilled v-else /></el-icon>
      {{ isCollected ? '已收藏' : '收藏' }}
    </el-button>
    <el-button size="large" round @click="$emit('share')">
      <el-icon><Share /></el-icon> 分享
    </el-button>
    <el-button size="large" round @click="$emit('add-to-folder')">
      <el-icon><FolderAdd /></el-icon> 加入资料篮
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { Download, Star, StarFilled, Share, FolderAdd } from '@element-plus/icons-vue'

defineProps<{ isCollected: boolean; downloading: boolean }>()
defineEmits<{ download: []; collect: []; share: []; 'add-to-folder': [] }>()
</script>

<style scoped>
.header-actions { display: flex; flex-wrap: wrap; gap: 10px; flex-shrink: 0; }
</style>
