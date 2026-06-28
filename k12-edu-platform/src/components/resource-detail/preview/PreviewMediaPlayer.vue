<template>
  <div v-if="previewType === 'video'" class="preview-video">
    <video :src="fileUrl" controls width="100%" :poster="coverUrl || ''">
      您的浏览器不支持视频播放
    </video>
  </div>

  <div v-else-if="previewType === 'audio'" class="preview-audio">
    <div class="audio-player">
      <div class="audio-info">
        <span class="audio-title">{{ title }}</span>
        <span class="audio-duration">时长 {{ audioDuration }}</span>
      </div>
      <audio
        ref="audioRef"
        :src="fileUrl"
        @loadedmetadata="$emit('audio-loaded')"
        @timeupdate="$emit('audio-timeupdate')"
      />
      <div class="audio-controls">
        <button class="play-btn" type="button" @click="$emit('toggle-audio')">
          <el-icon size="24">
            <VideoPlay v-if="!isPlaying" />
            <VideoPause v-else />
          </el-icon>
        </button>
        <div class="progress-bar" @click="$emit('seek', $event)">
          <div class="progress" :style="{ width: audioProgress + '%' }" />
        </div>
        <span class="time-display">{{ currentTime }} / {{ audioDuration }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { VideoPlay, VideoPause } from '@element-plus/icons-vue'

defineProps<{
  previewType: 'video' | 'audio'
  fileUrl?: string
  coverUrl?: string
  title: string
  isPlaying: boolean
  audioProgress: number
  currentTime: string
  audioDuration: string
}>()

defineEmits<{
  'toggle-audio': []
  'audio-loaded': []
  'audio-timeupdate': []
  seek: [event: MouseEvent]
}>()

const audioRef = ref<HTMLAudioElement | null>(null)
defineExpose({ audioRef })
</script>

<style scoped>
.preview-video video {
  border-radius: 8px;
  width: 100%;
}
.preview-audio .audio-player {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 24px;
}
.audio-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}
.audio-title {
  font-weight: 600;
}
.audio-duration {
  color: #909399;
}
.audio-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}
.play-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--el-color-primary);
  border: none;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.progress-bar {
  flex: 1;
  height: 6px;
  background: #e4e7ed;
  border-radius: 3px;
  cursor: pointer;
}
.progress {
  height: 100%;
  background: var(--el-color-primary);
  border-radius: 3px;
}
.time-display {
  color: #909399;
  font-size: 13px;
  white-space: nowrap;
}
</style>
